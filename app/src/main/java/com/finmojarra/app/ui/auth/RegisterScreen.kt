package com.finmojarra.app.ui.auth

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finmojarra.app.R
import com.finmojarra.app.ui.components.AuthErrorDialog
import com.finmojarra.app.ui.components.AuthScreenFrame
import com.finmojarra.app.ui.components.FinLogo
import com.finmojarra.app.ui.components.FinPrimaryButton
import com.finmojarra.app.ui.components.FinTextField
import com.finmojarra.app.ui.theme.FinInk
import com.finmojarra.app.ui.theme.FinMojarraTheme

/** Frame "Registrar" de Figma. */
@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onBack: () -> Unit,
    onRegistered: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.registered) {
        if (state.registered) onRegistered()
    }

    RegisterContent(
        state = state,
        onFullNameChange = viewModel::onFullNameChange,
        onEmailChange = viewModel::onEmailChange,
        onPhoneChange = viewModel::onPhoneChange,
        onPasswordChange = viewModel::onPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onSubmit = viewModel::submit,
        onBack = onBack,
        onDismissError = viewModel::dismissError
    )
}

@Composable
fun RegisterContent(
    state: RegisterUiState,
    onFullNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
    onDismissError: () -> Unit
) {
    AuthScreenFrame(onBack = onBack, isLoading = state.isLoading) {
        FinLogo()
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.register_title),
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
            color = FinInk
        )
        Spacer(modifier = Modifier.height(24.dp))

        FinTextField(
            label = stringResource(R.string.register_name_label),
            value = state.fullName,
            onValueChange = onFullNameChange,
            placeholder = stringResource(R.string.register_name_placeholder),
            errorText = state.fullNameError?.let { stringResource(it) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        FinTextField(
            label = stringResource(R.string.register_email_label),
            value = state.email,
            onValueChange = onEmailChange,
            placeholder = stringResource(R.string.register_email_placeholder),
            errorText = state.emailError?.let { stringResource(it) },
            keyboardType = KeyboardType.Email,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        FinTextField(
            label = stringResource(R.string.register_phone_label),
            value = state.phone,
            onValueChange = onPhoneChange,
            placeholder = stringResource(R.string.register_phone_placeholder),
            errorText = state.phoneError?.let { stringResource(it) },
            keyboardType = KeyboardType.Phone,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        FinTextField(
            label = stringResource(R.string.register_password_label),
            value = state.password,
            onValueChange = onPasswordChange,
            placeholder = stringResource(R.string.register_password_placeholder),
            errorText = state.passwordError?.let { stringResource(it) },
            isPassword = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        FinTextField(
            label = stringResource(R.string.register_confirm_label),
            value = state.confirmPassword,
            onValueChange = onConfirmPasswordChange,
            placeholder = stringResource(R.string.register_confirm_placeholder),
            errorText = state.confirmPasswordError?.let { stringResource(it) },
            isPassword = true,
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(onDone = { onSubmit() }),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        FinPrimaryButton(
            text = stringResource(R.string.register_button),
            onClick = onSubmit,
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
}

@Preview(showSystemUi = true)
@Composable
private fun RegisterPreview() {
    FinMojarraTheme {
        RegisterContent(
            state = RegisterUiState(),
            onFullNameChange = {},
            onEmailChange = {},
            onPhoneChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onSubmit = {},
            onBack = {},
            onDismissError = {}
        )
    }
}
