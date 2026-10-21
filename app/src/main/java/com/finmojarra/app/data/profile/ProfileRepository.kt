package com.finmojarra.app.data.profile

/**
 * Contrato de las funciones del perfil. Igual que [com.finmojarra.app.data.auth.AuthRepository],
 * nunca lanza excepciones: devuelve [ProfileResult.Failure].
 */
interface ProfileRepository {
    /** Datos del usuario con sesión activa, o null si no hay ninguno. */
    fun currentProfile(): ProfileInfo?

    suspend fun updateDisplayName(name: String): ProfileResult<ProfileInfo>

    /** Cambia la contraseña (solo cuentas con correo + contraseña). */
    suspend fun changePassword(currentPassword: String, newPassword: String): ProfileResult<Unit>

    /** Envía al correo de la cuenta el enlace para restablecer la contraseña. */
    suspend fun sendPasswordResetEmail(): ProfileResult<Unit>

    /** Elimina la cuenta. [password] es obligatoria si la cuenta tiene contraseña. */
    suspend fun deleteAccount(password: String?): ProfileResult<Unit>

    fun signOut()
}
