package com.finmojarra.app.data.auth

/**
 * Contrato entre la interfaz y la base de datos.
 *
 * La UI solo conoce esta interfaz. Para conectar la base de datos real basta con
 * crear otra clase que la implemente (por ejemplo `ApiAuthRepository`) y cambiarla
 * en [com.finmojarra.app.di.AppContainer]. Ninguna pantalla necesita modificarse.
 *
 * Cada función debe devolver [AuthResult.Failure] con el [AuthError] adecuado en vez
 * de lanzar excepciones: sin red -> [AuthError.NoConnection]; fallo del servidor o de
 * la base de datos -> [AuthError.ServerError].
 */
interface AuthRepository {
    /** [identifier] puede ser el nombre de usuario o el correo. */
    suspend fun login(identifier: String, password: String): AuthResult<Session>

    suspend fun register(data: RegisterData): AuthResult<Session>

    suspend fun requestPasswordReset(email: String): AuthResult<Unit>

    /** Inicia sesión (o crea la cuenta) con el ID token que devuelve Google. */
    suspend fun signInWithGoogle(idToken: String): AuthResult<Session>
}
