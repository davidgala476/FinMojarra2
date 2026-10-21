package com.finmojarra.app.data.profile

/** Datos del usuario que se muestran en el perfil. */
data class ProfileInfo(
    val name: String,
    val email: String,
    val username: String,
    /** true si la cuenta usa correo + contraseña (false si entra solo con Google). */
    val hasPassword: Boolean
)

/** Errores de las operaciones del perfil. */
sealed interface ProfileError {
    /** La contraseña actual escrita no es correcta. */
    data object WrongPassword : ProfileError

    /** La contraseña nueva no cumple los requisitos de Firebase. */
    data object WeakPassword : ProfileError

    /** Firebase pide haber iniciado sesión hace poco para esta operación. */
    data object RequiresRecentLogin : ProfileError

    data object NoConnection : ProfileError
    data object TooManyAttempts : ProfileError

    /** No hay usuario con sesión activa en Firebase. */
    data object NoSession : ProfileError

    data class Unknown(val code: String? = null) : ProfileError
}

sealed interface ProfileResult<out T> {
    data class Success<T>(val data: T) : ProfileResult<T>
    data class Failure(val error: ProfileError) : ProfileResult<Nothing>
}
