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
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.cancellation.CancellationException

/**
 * Cuenta de ahorros guardada en Firestore:
 *
 *  - users/{uid}/account/summary          →  balance (número: el saldo actual), updatedAt
 *  - users/{uid}/movements/{movementId}   →  type ("deposit" o "withdraw"), amount (número, siempre
 *                                            positivo) y createdAt (fecha del servidor)
 *
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

        val signed = if (type == MovementType.Deposit) amount else -amount
        val batch = db.batch()
        batch.set(
            summaryDoc(uid),
            mapOf(
                "balance" to FieldValue.increment(signed),
                "updatedAt" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        )
        batch.set(
            movementsCollection(uid).document(),
            mapOf(
                "type" to type.key,
                "amount" to amount,
                "createdAt" to FieldValue.serverTimestamp()
            )
        )
        return write { batch.commit().awaitResult() }
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

    private fun movementsCollection(uid: String) =
        db.collection(USERS).document(uid).collection(MOVEMENTS)

    private fun DocumentSnapshot.toMovement(): Movement? {
        val type = MovementType.fromKey(getString("type")) ?: return null
        val amount = (get("amount") as? Number)?.toDouble() ?: return null
        return Movement(
            id = id,
            type = type,
            amount = amount,
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
        const val MAX_MOVEMENTS = 30L
        const val WRITE_TIMEOUT_MS = 4_000L
    }
}
