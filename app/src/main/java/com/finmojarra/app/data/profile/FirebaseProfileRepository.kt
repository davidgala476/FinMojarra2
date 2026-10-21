package com.finmojarra.app.data.profile

import android.util.Log
import com.finmojarra.app.data.auth.awaitResult
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.coroutines.cancellation.CancellationException

/**
 * Perfil con Firebase Authentication: nombre, correo, cambio de contraseña, correo de
 * restablecimiento, cerrar sesión y eliminar cuenta (que también borra el documento del
 * usuario en Firestore). Los datos extra del perfil están en FirestoreProfileDetailsRepository.
 */
class FirebaseProfileRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ProfileRepository {

    override fun currentProfile(): ProfileInfo? = auth.currentUser?.toProfile()

    override suspend fun updateDisplayName(name: String): ProfileResult<ProfileInfo> {
        val user = auth.currentUser ?: return ProfileResult.Failure(ProfileError.NoSession)
        return try {
            val request = UserProfileChangeRequest.Builder().setDisplayName(name.trim()).build()
            user.updateProfile(request).awaitResult()
            ProfileResult.Success(user.toProfile())
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            ProfileResult.Failure(e.toProfileError())
        }
    }

    override suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ): ProfileResult<Unit> {
        val user = auth.currentUser ?: return ProfileResult.Failure(ProfileError.NoSession)
        return try {
            reauthenticate(user, currentPassword)
            user.updatePassword(newPassword).awaitResult()
            ProfileResult.Success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            ProfileResult.Failure(e.toProfileError())
        }
    }

    override suspend fun sendPasswordResetEmail(): ProfileResult<Unit> {
        val email = auth.currentUser?.email
            ?: return ProfileResult.Failure(ProfileError.NoSession)
        return try {
            auth.sendPasswordResetEmail(email).awaitResult()
            ProfileResult.Success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            ProfileResult.Failure(e.toProfileError())
        }
    }

    override suspend fun deleteAccount(password: String?): ProfileResult<Unit> {
        val user = auth.currentUser ?: return ProfileResult.Failure(ProfileError.NoSession)
        return try {
            if (user.hasPasswordProvider()) {
                reauthenticate(user, password.orEmpty())
            }
            deleteCloudData(user.uid)
            user.delete().awaitResult()
            ProfileResult.Success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            ProfileResult.Failure(e.toProfileError())
        }
    }

    /** Borra el documento del usuario. Debe hacerse antes de eliminar la cuenta (las reglas exigen sesión). */
    private suspend fun deleteCloudData(uid: String) {
        try {
            db.collection("users").document(uid).delete().awaitResult()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("FinMojarra", "No se pudieron borrar los datos del perfil en Firestore", e)
        }
    }

    override fun signOut() {
        auth.signOut()
    }

    private suspend fun reauthenticate(user: FirebaseUser, password: String) {
        val email = user.email.orEmpty()
        user.reauthenticate(EmailAuthProvider.getCredential(email, password)).awaitResult()
    }

    private fun FirebaseUser.hasPasswordProvider(): Boolean =
        providerData.any { it.providerId == EmailAuthProvider.PROVIDER_ID }

    private fun FirebaseUser.toProfile(): ProfileInfo {
        val mail = email.orEmpty()
        val username = mail.substringBefore('@').lowercase()
        return ProfileInfo(
            name = displayName?.takeIf { it.isNotBlank() } ?: username,
            email = mail,
            username = username,
            hasPassword = hasPasswordProvider()
        )
    }

    private fun Exception.toProfileError(): ProfileError {
        Log.e("FinMojarra", "Error de perfil", this)
        return when (this) {
            is FirebaseNetworkException -> ProfileError.NoConnection
            is FirebaseTooManyRequestsException -> ProfileError.TooManyAttempts
            is FirebaseAuthRecentLoginRequiredException -> ProfileError.RequiresRecentLogin
            // WeakPassword hereda de InvalidCredentials: debe ir antes.
            is FirebaseAuthWeakPasswordException -> ProfileError.WeakPassword
            is FirebaseAuthInvalidCredentialsException -> ProfileError.WrongPassword
            is FirebaseAuthInvalidUserException -> ProfileError.NoSession
            is FirebaseAuthException ->
                if (errorCode == "ERROR_NETWORK_REQUEST_FAILED") ProfileError.NoConnection
                else ProfileError.Unknown(errorCode)
            else -> ProfileError.Unknown("${javaClass.simpleName}: ${message?.take(90)}")
        }
    }
}
