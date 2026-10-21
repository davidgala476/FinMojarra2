package com.finmojarra.app.data.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException

/** Resultado de pedirle a Google el ID token del usuario. */
sealed interface GoogleTokenResult {
    data class Token(val idToken: String) : GoogleTokenResult

    /** El usuario cerró el selector de cuentas: no es un error. */
    data object Cancelled : GoogleTokenResult

    data class Failure(val error: AuthError) : GoogleTokenResult
}

/**
 * Abre el selector de cuentas de Google (Credential Manager) y devuelve el ID token,
 * que luego [AuthRepository.signInWithGoogle] entrega a Firebase.
 *
 * Necesita el "client ID web" del proyecto. El plugin google-services lo genera como
 * `default_web_client_id`, pero solo si el `google-services.json` incluye un `oauth_client`
 * (aparece al habilitar Google en Firebase Authentication y registrar la huella SHA-1).
 * Se busca por nombre para que la app compile aunque todavía no esté configurado.
 */
class GoogleTokenProvider {

    /** [activityContext] debe ser una Activity para que Android pueda mostrar el selector. */
    suspend fun getIdToken(activityContext: Context): GoogleTokenResult {
        val clientId = webClientId(activityContext)
            ?: return GoogleTokenResult.Failure(AuthError.ServerError("GOOGLE_CLIENT_ID_MISSING"))

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(GetSignInWithGoogleOption.Builder(clientId).build())
            .build()

        return try {
            val response = CredentialManager.create(activityContext)
                .getCredential(activityContext, request)
            val credential = response.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                GoogleTokenResult.Token(GoogleIdTokenCredential.createFrom(credential.data).idToken)
            } else {
                GoogleTokenResult.Failure(AuthError.ServerError("GOOGLE_UNEXPECTED_CREDENTIAL"))
            }
        } catch (e: GetCredentialCancellationException) {
            GoogleTokenResult.Cancelled
        } catch (e: NoCredentialException) {
            Log.e(TAG, "Sin cuentas de Google disponibles", e)
            GoogleTokenResult.Failure(AuthError.ServerError("GOOGLE_NO_ACCOUNT"))
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Error de Credential Manager", e)
            GoogleTokenResult.Failure(AuthError.ServerError("GOOGLE_${e.type}".take(60)))
        } catch (e: GoogleIdTokenParsingException) {
            Log.e(TAG, "Token de Google ilegible", e)
            GoogleTokenResult.Failure(AuthError.ServerError("GOOGLE_BAD_TOKEN"))
        }
    }

    @Suppress("DiscouragedApi")
    private fun webClientId(context: Context): String? {
        val id = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        return if (id != 0) context.getString(id) else null
    }

    private companion object {
        const val TAG = "FinMojarra"
    }
}
