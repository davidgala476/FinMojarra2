package com.finmojarra.app.data.goals

import android.net.Uri
import kotlinx.coroutines.flow.Flow

/** Contrato de las metas: leer, crear (con imagen) y mover dinero (ingresar / retirar). */
interface GoalsRepository {
    /** Emite la lista de metas del usuario y se actualiza sola cuando cambian en la nube. */
    fun observeGoals(): Flow<GoalsResult>

    /**
     * Crea una meta nueva con 0 ahorrado. [imageUri] es la imagen elegida en la galería
     * (opcional); se reduce y se guarda junto a la meta.
     */
    suspend fun createGoal(
        name: String,
        targetAmount: Double,
        weeklyAmount: Double,
        imageUri: Uri?
    ): GoalWriteResult

    /** Suma [delta] al ahorro de la meta (negativo para retirar). */
    suspend fun addToSaved(goalId: String, delta: Double): GoalWriteResult
}
