package com.finmojarra.app.data.auth

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Implementación real de [AuthRepository] con Firebase Authentication (correo + contraseña).
 *
 * Limitaciones actuales (Firebase Auth solo maneja correo y contraseña):
 *  - El login funciona con CORREO. Si el usuario escribe un nombre de usuario (sin "@")
 *    se responde [AuthError.InvalidCredentials]. Para loguear con usuario hace falta guardar
 *    el usuario en una base de datos (Firestore) y buscar su correo.
 *  - También permite entrar con Google ([signInWithGoogle]); Firebase crea la cuenta la primera vez.
 *  - El nombre completo se guarda como `displayName`. El teléfono del registro lo guarda
 *    el perfil en Firestore (FirestoreProfileDetailsRepository).
 */
class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : AuthRepository {

    override suspend fun login(identifier: String, password: String): AuthResult<Session> {
        val email = identifier.trim()
        if (!email.contains('@')) {
            return AuthResult.Failure(AuthError.InvalidCredentials)
        }
        return try {
            val user = auth.signInWithEmailAndPassword(email, password).awaitResult().user
                ?: return AuthResult.Failure(AuthError.ServerError("NO_USER"))
            AuthResult.Success(user.toSession(fallbackEmail = email))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            // Contraseña incorrecta o correo mal formado: para el login es lo mismo.
            AuthResult.Failure(AuthError.InvalidCredentials)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            AuthResult.Failure(e.toAuthError())
        }
    }

    override suspend fun register(data: RegisterData): AuthResult<Session> {
        return try {
            val user = auth.createUserWithEmailAndPassword(data.email.trim(), data.password)
                .awaitResult().user
                ?: return AuthResult.Failure(AuthError.ServerError("NO_USER"))

            // Guardar el nombre; si falla, la cuenta ya existe y no se considera error de registro.
            try {
                val profile = UserProfileChangeRequest.Builder()
                    .setDisplayName(data.fullName.trim())
                    .build()
                user.updateProfile(profile).awaitResult()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
            }

            AuthResult.Success(
                user.toSession(fallbackEmail = data.email.trim(), fallbackName = data.fullName.trim())
            )
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            AuthResult.Failure(e.toAuthError())
        }
    }

    override suspend fun requestPasswordReset(email: String): AuthResult<Unit> {
        return try {
            auth.sendPasswordResetEmail(email.trim()).awaitResult()
            AuthResult.Success(Unit)
        } catch (e: FirebaseAuthInvalidUserException) {
            // No revelamos si el correo existe o no.
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            AuthResult.Failure(e.toAuthError())
        }
    }

    override suspend fun signInWithGoogle(idToken: String): AuthResult<Session> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val user = auth.signInWithCredential(credential).awaitResult().user
                ?: return AuthResult.Failure(AuthError.ServerError("NO_USER"))
            AuthResult.Success(user.toSession(fallbackEmail = user.email.orEmpty()))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            // Un token de Google rechazado no es "usuario o contraseña incorrecta".
            Log.e("FinMojarra", "Google rechazado por Firebase", e)
            AuthResult.Failure(AuthError.ServerError(e.errorCode))
        } catch (e: FirebaseAuthInvalidUserException) {
            Log.e("FinMojarra", "Cuenta de Google no válida", e)
            AuthResult.Failure(AuthError.ServerError(e.errorCode))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            AuthResult.Failure(e.toAuthError())
        }
    }

    private fun FirebaseUser.toSession(
        fallbackEmail: String,
        fallbackName: String? = null
    ): Session {
        val mail = email ?: fallbackEmail
        val username = mail.substringBefore('@').lowercase()
        return Session(
            userId = uid,
            displayName = displayName?.takeIf { it.isNotBlank() } ?: fallbackName ?: username,
            username = username,
            email = mail
        )
    }

    private fun Exception.toAuthError(): AuthError {
        Log.e("FinMojarra", "Error de Firebase Auth", this)
        return mapToAuthError()
    }

    private fun Exception.mapToAuthError(): AuthError = when (this) {
        is FirebaseNetworkException -> AuthError.NoConnection
        is FirebaseTooManyRequestsException -> AuthError.TooManyAttempts
        is FirebaseAuthUserCollisionException -> AuthError.UserAlreadyExists
        is FirebaseAuthInvalidUserException,
        is FirebaseAuthInvalidCredentialsException -> {
            val code = (this as FirebaseAuthException).errorCode
            // Un correo mal escrito al registrarse no es "credenciales incorrectas".
            if (code == "ERROR_INVALID_EMAIL" && this is FirebaseAuthInvalidCredentialsException) {
                AuthError.ServerError(code)
            } else {
                AuthError.InvalidCredentials
            }
        }
        is FirebaseAuthException ->
            if (errorCode == "ERROR_NETWORK_REQUEST_FAILED") AuthError.NoConnection
            else AuthError.ServerError(errorCode)
        else -> AuthError.ServerError("${javaClass.simpleName}: ${message?.take(90)}")
    }
}

/** Convierte un [Task] de Google en una función suspendida (sin dependencias extra). */
internal suspend fun <T> Task<T>.awaitResult(): T = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { cont.resume(it) }
    addOnFailureListener { cont.resumeWithException(it) }
    addOnCanceledListener { cont.cancel() }
}
