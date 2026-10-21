package com.finmojarra.app.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finmojarra.app.R
import com.finmojarra.app.data.auth.AuthError
import com.finmojarra.app.ui.components.AuthErrorDialog
import com.finmojarra.app.ui.components.AuthScreenFrame
import com.finmojarra.app.ui.components.FinLinkText
import com.finmojarra.app.ui.components.FinLogo
import com.finmojarra.app.ui.components.FinPrimaryButton
import com.finmojarra.app.ui.components.FinTextField
import com.finmojarra.app.ui.theme.FinMojarraTheme

/** Frame "Login" de Figma (con sus estados de error y de carga). */
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onBack: () -> Unit,
    onForgot: () -> Unit,
    onRegister: () -> Unit,
    onLoggedIn: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.loggedIn) {
        if (state.loggedIn) onLoggedIn()
    }

    LoginContent(
        state = state,
        onIdentifierChange = viewModel::onIdentifierChange,
        onPasswordChange = viewModel::onPasswordChange,
        onSubmit = viewModel::submit,
        onBack = onBack,
        onForgot = onForgot,
        onRegister = onRegister,
        onDismissError = viewModel::dismissError
    )
}

@Composable
fun LoginContent(
    state: LoginUiState,
    onIdentifierChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
    onForgot: () -> Unit,
    onRegister: () -> Unit,
    onDismissError: () -> Unit
) {
    AuthScreenFrame(onBack = onBack, isLoading = state.isLoading) {
        FinLogo()
        Spacer(modifier = Modifier.height(40.dp))

        FinTextField(
            label = stringResource(R.string.login_identifier_label),
            value = state.identifier,
            onValueChange = onIdentifierChange,
            placeholder = stringResource(R.string.login_identifier_placeholder),
            errorText = state.identifierError?.let { stringResource(it) },
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next,
            modifier = Modifier.fillMaxWidth()
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            FinLinkText(
                text = stringResource(R.string.login_forgot_identifier),
                onClick = onForgot,
                textAlign = TextAlign.End
            )
        }

        FinTextField(
            label = stringResource(R.string.login_password_label),
            value = state.password,
            onValueChange = onPasswordChange,
            placeholder = stringResource(R.string.login_password_placeholder),
            errorText = state.passwordError?.let { stringResource(it) },
            isPassword = true,
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(onDone = { onSubmit() }),
            modifier = Modifier.fillMaxWidth()
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            FinLinkText(
                text = stringResource(R.string.login_forgot_password),
                onClick = onForgot,
                textAlign = TextAlign.End
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        FinPrimaryButton(
            text = stringResource(R.string.login_button),
            onClick = onSubmit,
            enabled = !state.isLoading
        )
        Spacer(modifier = Modifier.height(4.dp))

        FinLinkText(
            text = stringResource(R.string.login_no_account),
            onClick = onRegister,
            textAlign = TextAlign.Center
        )
    }

    state.error?.let { error ->
        AuthErrorDialog(
            error = error,
            onRetry = onDismissError,
            onRegister = {
                onDismissError()
                onRegister()
            }
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun LoginPreview() {
    FinMojarraTheme {
        LoginContent(
            state = LoginUiState(),
            onIdentifierChange = {},
            onPasswordChange = {},
            onSubmit = {},
            onBack = {},
            onForgot = {},
            onRegister = {},
            onDismissError = {}
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun LoginInvalidCredentialsPreview() {
    FinMojarraTheme {
        LoginContent(
            state = LoginUiState(error = AuthError.InvalidCredentials),
            onIdentifierChange = {},
            onPasswordChange = {},
            onSubmit = {},
            onBack = {},
            onForgot = {},
            onRegister = {},
            onDismissError = {}
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun LoginLoadingPreview() {
    FinMojarraTheme {
        LoginContent(
            state = LoginUiState(isLoading = true),
            onIdentifierChange = {},
            onPasswordChange = {},
            onSubmit = {},
            onBack = {},
            onForgot = {},
            onRegister = {},
            onDismissError = {}
        )
    }
}
