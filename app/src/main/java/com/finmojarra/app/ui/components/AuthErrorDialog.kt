package com.finmojarra.app.ui.components

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.finmojarra.app.BuildConfig
import com.finmojarra.app.R
import com.finmojarra.app.data.auth.AuthError
import com.finmojarra.app.ui.theme.FinCoral
import com.finmojarra.app.ui.theme.FinError

/**
 * Muestra el diálogo que corresponde a cada [AuthError], igual que en Figma:
 *  - Usuario/Contraseña incorrecta -> Reintentar, Registrarse, Salir
 *  - Demasiados intentos ........... -> Salir
 *  - Sin conexión .................. -> Salir
 *  - Error de base de datos ........ -> Salir
 *
 * "Salir" cierra la aplicación.
 */
@Composable
fun AuthErrorDialog(
    error: AuthError,
    onRetry: () -> Unit,
    onRegister: () -> Unit
) {
    val activity = LocalActivity.current
    val exit: () -> Unit = { activity?.finishAffinity() }

    when (error) {
        AuthError.InvalidCredentials -> FinDialog(
            title = stringResource(R.string.error_invalid_credentials),
            onDismiss = onRetry
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DialogActionButton(
                    text = stringResource(R.string.action_retry),
                    onClick = onRetry,
                    modifier = Modifier.weight(1f),
                    borderWidth = 1.5.dp
                )
                DialogActionButton(
                    text = stringResource(R.string.action_register),
                    onClick = onRegister,
                    modifier = Modifier.weight(1f)
                )
            }
            ExitButton(onClick = exit)
        }

        AuthError.TooManyAttempts -> FinDialog(
            title = stringResource(R.string.error_too_many_attempts),
            onDismiss = onRetry
        ) {
            ExitButton(onClick = exit)
        }

        AuthError.NoConnection -> FinDialog(
            title = stringResource(R.string.error_no_connection),
            onDismiss = onRetry
        ) {
            ExitButton(onClick = exit)
        }

        is AuthError.ServerError -> {
            val serverTitle = stringResource(R.string.error_server_title)
            val serverBody = stringResource(R.string.error_server_body)
            val code = error.code
            val title = buildAnnotatedString {
                append(serverTitle)
                // El código técnico solo se muestra en builds de depuración.
                if (BuildConfig.DEBUG && !code.isNullOrBlank()) {
                    append(" ")
                    withStyle(SpanStyle(color = FinError)) { append("($code)") }
                }
                append("\n\n")
                append(serverBody)
            }
            FinDialog(title = title, onDismiss = onRetry) {
                ExitButton(onClick = exit)
            }
        }

        AuthError.UserAlreadyExists -> FinDialog(
            title = stringResource(R.string.error_email_taken),
            onDismiss = onRetry
        ) {
            ExitButton(onClick = exit)
        }
    }
}

@Composable
private fun ExitButton(onClick: () -> Unit) {
    DialogActionButton(
        text = stringResource(R.string.action_exit),
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        contentColor = FinCoral
    )
}
