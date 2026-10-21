package com.finmojarra.app.ui.auth

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finmojarra.app.R
import com.finmojarra.app.ui.components.AuthErrorDialog
import com.finmojarra.app.ui.components.AuthScreenFrame
import com.finmojarra.app.ui.components.DialogActionButton
import com.finmojarra.app.ui.components.FinDialog
import com.finmojarra.app.ui.components.FinLogo
import com.finmojarra.app.ui.components.FinPrimaryButton
import com.finmojarra.app.ui.components.FinTextField
import com.finmojarra.app.ui.theme.FinMojarraTheme

/** Frame "Recuperar contraseña" de Figma. */
@Composable
fun RecoverScreen(
    viewModel: RecoverViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    RecoverContent(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onRecoverPassword = viewModel::recoverPassword,
        onBack = onBack,
        onDismissError = viewModel::dismissError,
        onDismissInfo = viewModel::dismissInfo
    )
}

@Composable
fun RecoverContent(
    state: RecoverUiState,
    onEmailChange: (String) -> Unit,
    onRecoverPassword: () -> Unit,
    onBack: () -> Unit,
    onDismissError: () -> Unit,
    onDismissInfo: () -> Unit
) {
    AuthScreenFrame(onBack = onBack, isLoading = state.isLoading) {
        FinLogo()
        Spacer(modifier = Modifier.height(24.dp))

        FinTextField(
            label = stringResource(R.string.recover_password_title),
            value = state.email,
            onValueChange = onEmailChange,
            placeholder = stringResource(R.string.recover_password_placeholder),
            errorText = state.emailError?.let { stringResource(it) },
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(onDone = { onRecoverPassword() }),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        FinPrimaryButton(
            text = stringResource(R.string.recover_password_button),
            onClick = onRecoverPassword,
            enabled = !state.isLoading
        )
    }

    state.error?.let { error ->
        AuthErrorDialog(
            error = error,
            onRetry = onDismissError,
            onRegister = onDismissError
        )
    }

    state.infoMessage?.let { message ->
        FinDialog(title = stringResource(message), onDismiss = onDismissInfo) {
            DialogActionButton(
                text = stringResource(R.string.action_accept),
                onClick = onDismissInfo,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun RecoverPreview() {
    FinMojarraTheme {
        RecoverContent(
            state = RecoverUiState(),
            onEmailChange = {},
            onRecoverPassword = {},
            onBack = {},
            onDismissError = {},
            onDismissInfo = {}
        )
    }
}
