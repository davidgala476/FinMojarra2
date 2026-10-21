package com.finmojarra.app.data.auth

/** Usuario con sesión iniciada. */
data class Session(
    val userId: String,
    val displayName: String,
    val username: String,
    val email: String
)

/** Datos del formulario de registro. */
data class RegisterData(
    val fullName: String,
    val email: String,
    val phone: String,
    val password: String
)

/**
 * Errores de autenticación que la UI sabe mostrar.
 * Cada uno corresponde a un diálogo/estado del diseño de Figma.
 */
sealed interface AuthError {
    /** "Usuario o contraseña incorrecta". */
    data object InvalidCredentials : AuthError

    /** "Demasiados intentos incorrectos, reintente en 5 minutos". */
    data object TooManyAttempts : AuthError

    /** "Login Error conexión". */
    data object NoConnection : AuthError

    /** "Login Error base de datos". [code] solo se muestra en builds de depuración. */
    data class ServerError(val code: String? = null) : AuthError

    /** El correo ya existe (se muestra bajo el campo, no como diálogo). */
    data object UserAlreadyExists : AuthError
}

sealed interface AuthResult<out T> {
    data class Success<T>(val data: T) : AuthResult<T>
    data class Failure(val error: AuthError) : AuthResult<Nothing>
}
