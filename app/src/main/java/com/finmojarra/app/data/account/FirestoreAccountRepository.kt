package com.finmojarra.app.data.account

import android.util.Log
import com.finmojarra.app.data.auth.awaitResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.WriteBatch
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.cancellation.CancellationException

/**
 * Cuenta de ahorros guardada en Firestore:

 * Cada ingreso o retiro es UNA escritura en lote (batch): suma o resta al saldo con increment()
 * y agrega el movimiento. O se guardan las dos cosas o ninguna, así el saldo y el historial
 * nunca quedan desfasados. increment() suma en el servidor, así que dos dispositivos no se
 * pisan el saldo. Sin internet el lote queda en cola y se sube solo.
 */
class FirestoreAccountRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AccountRepository {

    override fun observeAccount(): Flow<AccountResult> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(AccountResult.Failure)
            close()
            return@callbackFlow
        }

        // Se emite cuando ya llegaron las dos partes: el saldo y la lista de movimientos.
        var balance: Double? = null
        var movements: List<Movement>? = null
        fun emit() {
            val b = balance
            val m = movements
            if (b != null && m != null) trySend(AccountResult.Success(b, m))
        }

        val balanceRegistration = summaryDoc(uid).addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                Log.e(TAG, "No se pudo leer el saldo", error)
                trySend(AccountResult.Failure)
                return@addSnapshotListener
            }
            // Sin documento todavía (cuenta nueva) el saldo es 0.
            balance = (snapshot.get("balance") as? Number)?.toDouble() ?: 0.0
            emit()
        }

        val movementsRegistration = movementsCollection(uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(MAX_MOVEMENTS)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    Log.e(TAG, "No se pudieron leer los movimientos", error)
                    trySend(AccountResult.Failure)
                    return@addSnapshotListener
                }
                movements = snapshot.documents.mapNotNull { it.toMovement() }
                emit()
            }

        awaitClose {
            balanceRegistration.remove()
            movementsRegistration.remove()
        }
    }

    override suspend fun move(type: MovementType, amount: Double): AccountWriteResult {
        val uid = auth.currentUser?.uid
            ?: return AccountWriteResult.Failure(AccountWriteResult.Reason.NoSession)

        val batch = db.batch()
        addMovement(batch, uid, type, amount, goalName = null)
        return write { batch.commit().awaitResult() }
    }

    override suspend fun transferToGoal(goalId: String, goalName: String, delta: Double): AccountWriteResult {
        val uid = auth.currentUser?.uid
            ?: return AccountWriteResult.Failure(AccountWriteResult.Reason.NoSession)

        val batch = db.batch()
        // Sube (o baja) lo ahorrado en la meta...
        batch.update(goalDoc(uid, goalId), "savedAmount", FieldValue.increment(delta))
        // ...y en la misma escritura baja (o sube) el saldo de la cuenta y deja el movimiento.
        val type = if (delta > 0) MovementType.GoalDeposit else MovementType.GoalWithdraw
        addMovement(batch, uid, type, kotlin.math.abs(delta), goalName)
        return write { batch.commit().awaitResult() }
    }

    override suspend fun deleteGoalReturningMoney(
        goalId: String,
        goalName: String,
        savedAmount: Double
    ): AccountWriteResult {
        val uid = auth.currentUser?.uid
            ?: return AccountWriteResult.Failure(AccountWriteResult.Reason.NoSession)

        val batch = db.batch()
        batch.delete(goalDoc(uid, goalId))
        if (savedAmount > 0.0) {
            addMovement(batch, uid, MovementType.GoalWithdraw, savedAmount, goalName)
        }
        return write { batch.commit().awaitResult() }
    }

    /**
     * Agrega a firebase el cambio del saldo y el movimiento del historial.
     * Los ingresos y los retiros de metas suman o restan con increment() en el servidor.
     */
    private fun addMovement(
        batch: WriteBatch,
        uid: String,
        type: MovementType,
        amount: Double,
        goalName: String?
    ) {
        val signed = if (type.addsToBalance) amount else -amount
        batch.set(
            summaryDoc(uid),
            mapOf(
                "balance" to FieldValue.increment(signed),
                "updatedAt" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        )
        val data = mutableMapOf<String, Any>(
            "type" to type.key,
            "amount" to amount,
            "createdAt" to FieldValue.serverTimestamp()
        )
        if (goalName != null) data["goalName"] = goalName
        batch.set(movementsCollection(uid).document(), data)
    }

    /**
     * Sin internet Firestore nunca responde: tras el tiempo de espera se da por buena, porque
     * el cambio queda en cola y se sube solo al volver la conexión. Los errores reales
     * (por ejemplo, reglas que no lo permiten) sí llegan antes.
     */
    private suspend fun write(block: suspend () -> Unit): AccountWriteResult = try {
        withTimeoutOrNull(WRITE_TIMEOUT_MS) { block() }
        AccountWriteResult.Success
    } catch (e: Exception) {
        if (e is CancellationException) throw e
        Log.e(TAG, "No se pudo guardar el movimiento", e)
        val denied = e is FirebaseFirestoreException &&
            e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED
        AccountWriteResult.Failure(
            if (denied) AccountWriteResult.Reason.PermissionDenied else AccountWriteResult.Reason.Unknown
        )
    }

    private fun summaryDoc(uid: String) =
        db.collection(USERS).document(uid).collection(ACCOUNT).document(SUMMARY)

    private fun goalDoc(uid: String, goalId: String) =
        db.collection(USERS).document(uid).collection(GOALS).document(goalId)

    private fun movementsCollection(uid: String) =
        db.collection(USERS).document(uid).collection(MOVEMENTS)

    private fun DocumentSnapshot.toMovement(): Movement? {
        val type = MovementType.fromKey(getString("type")) ?: return null
        val amount = (get("amount") as? Number)?.toDouble() ?: return null
        return Movement(
            id = id,
            type = type,
            amount = amount,
            goalName = getString("goalName"),
            createdAtMillis = getTimestamp(
                "createdAt",
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            )?.toDate()?.time
        )
    }

    private companion object {
        const val TAG = "FinMojarra"
        const val USERS = "users"
        const val ACCOUNT = "account"
        const val SUMMARY = "summary"
        const val MOVEMENTS = "movements"
        const val GOALS = "goals"
        const val MAX_MOVEMENTS = 500L
        const val WRITE_TIMEOUT_MS = 4_000L
    }
}
