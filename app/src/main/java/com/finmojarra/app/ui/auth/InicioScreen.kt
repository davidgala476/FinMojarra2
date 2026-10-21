package com.finmojarra.app.ui.auth

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finmojarra.app.R
import com.finmojarra.app.data.auth.GoogleTokenProvider
import com.finmojarra.app.data.auth.GoogleTokenResult
import com.finmojarra.app.ui.components.AuthErrorDialog
import com.finmojarra.app.ui.components.AuthScreenFrame
import com.finmojarra.app.ui.components.FinLogo
import com.finmojarra.app.ui.components.FinPrimaryButton
import com.finmojarra.app.ui.components.FinTextField
import com.finmojarra.app.ui.theme.FinFieldBorder
import com.finmojarra.app.ui.theme.FinGoogleButton
import com.finmojarra.app.ui.theme.FinInk
import com.finmojarra.app.ui.theme.FinMojarraTheme
import com.finmojarra.app.ui.theme.FinMutedText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/** Frame "Inicio App" de Figma, con el botón "Continuar con Google" activo. */
@Composable
fun InicioScreen(
    viewModel: InicioViewModel,
    googleTokenProvider: GoogleTokenProvider,
    onRegister: (email: String) -> Unit,
    onLogin: () -> Unit,
    onLoggedIn: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = LocalActivity.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.loggedIn) {
        if (state.loggedIn) onLoggedIn()
    }

    InicioContent(
        state = state,
        onRegister = onRegister,
        onLogin = onLogin,
        onGoogle = {
            scope.launch {
                viewModel.onGoogleStarted()
                val result = try {
                    // El selector de cuentas necesita una Activity.
                    googleTokenProvider.getIdToken(activity ?: context)
                } catch (e: CancellationException) {
                    // La pantalla se destruyó (por ejemplo al rotar): no dejar el cargando activo.
                    viewModel.onGoogleResult(GoogleTokenResult.Cancelled)
                    throw e
                }
                viewModel.onGoogleResult(result)
            }
        },
        onDismissError = viewModel::dismissError
    )
}

@Composable
fun InicioContent(
    state: InicioUiState,
    onRegister: (email: String) -> Unit,
    onLogin: () -> Unit,
    onGoogle: () -> Unit,
    onDismissError: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var emailError by rememberSaveable { mutableStateOf<Int?>(null) }

    val submitEmail: () -> Unit = {
        when {
            email.isBlank() -> emailError = R.string.error_field_required
            !Validators.isValidEmail(email) -> emailError = R.string.error_email_invalid
            else -> onRegister(email.trim())
        }
    }

    val termsPrefix = stringResource(R.string.inicio_terms_prefix)
    val termsService = stringResource(R.string.inicio_terms_service)
    val termsAnd = stringResource(R.string.inicio_terms_and)
    val termsPrivacy = stringResource(R.string.inicio_terms_privacy)
    val terms = buildAnnotatedString {
        append(termsPrefix)
        withStyle(SpanStyle(color = FinInk)) { append(termsService) }
        append(termsAnd)
        withStyle(SpanStyle(color = FinInk)) { append(termsPrivacy) }
    }

    AuthScreenFrame(isLoading = state.isLoading) {
        FinLogo()
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.inicio_title),
            style = MaterialTheme.typography.titleMedium,
            color = FinInk
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.inicio_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = FinInk,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))

        FinTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = null
            },
            placeholder = stringResource(R.string.inicio_email_placeholder),
            errorText = emailError?.let { stringResource(it) },
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(onDone = { submitEmail() }),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        FinPrimaryButton(
            text = stringResource(R.string.inicio_register),
            onClick = submitEmail
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = FinFieldBorder)
            Text(
                text = stringResource(R.string.inicio_or),
                style = MaterialTheme.typography.bodySmall,
                color = FinMutedText,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = FinFieldBorder)
        }
        Spacer(modifier = Modifier.height(12.dp))

        FinPrimaryButton(
            text = stringResource(R.string.inicio_login),
            onClick = onLogin
        )
        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onGoogle,
            enabled = !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = FinGoogleButton,
                contentColor = FinInk,
                disabledContainerColor = FinGoogleButton.copy(alpha = 0.6f),
                disabledContentColor = FinInk.copy(alpha = 0.6f)
            )
        ) {
            // TODO: reemplazar la "G" por el logo oficial de Google cuando se agregue el asset.
            Text(
                text = "G",
                style = MaterialTheme.typography.titleMedium,
                color = androidx.compose.ui.graphics.Color(0xFF4285F4),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.inicio_google),
                style = MaterialTheme.typography.labelLarge
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = terms,
            style = MaterialTheme.typography.bodySmall,
            color = FinMutedText,
            textAlign = TextAlign.Center
        )
    }

    state.error?.let { error ->
        AuthErrorDialog(
            error = error,
            onRetry = onDismissError,
            onRegister = onDismissError
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun InicioScreenPreview() {
    FinMojarraTheme {
        InicioContent(
            state = InicioUiState(),
            onRegister = {},
            onLogin = {},
            onGoogle = {},
            onDismissError = {}
        )
    }
}
