package com.finmojarra.app.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finmojarra.app.R
import com.finmojarra.app.ui.components.DialogActionButton
import com.finmojarra.app.ui.components.FinDialog
import com.finmojarra.app.ui.components.FinLinkText
import com.finmojarra.app.ui.components.FinTextField
import com.finmojarra.app.ui.theme.FinCoral
import com.finmojarra.app.ui.theme.FinInk

/**
 * Perfil de usuario (diseño de Figma): foto, nombre y una tarjeta por apartado.
 * Cada tarjeta abre su pantalla de cambio, excepto el correo, que solo se muestra.
 */
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onBack: () -> Unit,
    onLoggedOut: () -> Unit,
    onEditPhoto: () -> Unit,
    onOpenDemographics: () -> Unit,
    onOpenPhone: () -> Unit,
    onOpenAddress: () -> Unit,
    onOpenDocument: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.loggedOut) {
        if (state.loggedOut) onLoggedOut()
    }

    ProfileScaffold(onBack = onBack, isLoading = state.isLoading) {
        VSpace(16.dp)

        ProfileAvatar(
            photoPath = state.details.photoPath,
            photoStamp = state.details.photoStamp
        )
        Text(
            text = stringResource(R.string.profile_edit_photo),
            style = MaterialTheme.typography.bodySmall,
            color = FinInk,
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .clickable(role = Role.Button, onClick = onEditPhoto)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        )

        VSpace(8.dp)
        Text(
            text = state.savedName,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = FinInk,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        VSpace(20.dp)

        ProfileCardColumn {
            ProfileInfoCard(
                label = stringResource(R.string.profile_card_you),
                value = stringResource(R.string.profile_card_demographics),
                onClick = onOpenDemographics
            )
            ProfileInfoCard(
                label = stringResource(R.string.profile_card_phone),
                value = state.details.phone,
                placeholder = stringResource(R.string.profile_not_set),
                onClick = onOpenPhone
            )
            // El correo solo se muestra: no abre ninguna pantalla.
            ProfileInfoCard(
                label = stringResource(R.string.profile_card_email),
                value = state.email
            )
            ProfileInfoCard(
                label = stringResource(R.string.profile_card_address),
                value = state.details.locationLabel,
                placeholder = stringResource(R.string.profile_not_set),
                onClick = onOpenAddress
            )
            ProfileInfoCard(
                label = stringResource(R.string.profile_card_document),
                value = state.details.documentLabel,
                placeholder = stringResource(R.string.profile_not_set),
                onClick = onOpenDocument
            )
        }

        VSpace(24.dp)
        FinLinkText(
            text = stringResource(R.string.action_logout),
            onClick = viewModel::openLogout,
            textAlign = TextAlign.Center
        )
        FinLinkText(
            text = stringResource(R.string.profile_delete_account),
            onClick = viewModel::openDeleteAccount,
            textAlign = TextAlign.Center
        )
        VSpace(16.dp)
    }

    ProfileDialogs(state = state, viewModel = viewModel)
}

@Composable
private fun ProfileDialogs(state: ProfileUiState, viewModel: ProfileViewModel) {
    when (state.dialog) {
        ProfileDialog.None -> Unit

        ProfileDialog.ConfirmLogout -> FinDialog(
            title = stringResource(R.string.profile_logout_confirm),
            onDismiss = viewModel::dismissDialog
        ) {
            DialogActionButton(
                text = stringResource(R.string.action_logout),
                onClick = viewModel::confirmLogout,
                modifier = Modifier.fillMaxWidth()
            )
            DialogActionButton(
                text = stringResource(R.string.action_cancel),
                onClick = viewModel::dismissDialog,
                modifier = Modifier.fillMaxWidth()
            )
        }

        ProfileDialog.ChangePassword -> FinDialog(
            title = stringResource(R.string.profile_change_password),
            onDismiss = viewModel::dismissDialog
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FinTextField(
                    value = state.currentPassword,
                    onValueChange = viewModel::onCurrentPasswordChange,
                    placeholder = stringResource(R.string.profile_current_password),
                    errorText = state.currentPasswordError?.let { stringResource(it) },
                    isPassword = true
                )
                FinTextField(
                    value = state.newPassword,
                    onValueChange = viewModel::onNewPasswordChange,
                    placeholder = stringResource(R.string.profile_new_password),
                    errorText = state.newPasswordError?.let { stringResource(it) },
                    isPassword = true
                )
                FinTextField(
                    value = state.confirmPassword,
                    onValueChange = viewModel::onConfirmPasswordChange,
                    placeholder = stringResource(R.string.register_confirm_placeholder),
                    errorText = state.confirmPasswordError?.let { stringResource(it) },
                    isPassword = true,
                    imeAction = ImeAction.Done,
                    keyboardActions = KeyboardActions(onDone = { viewModel.submitChangePassword() })
                )
            }
            DialogActionButton(
                text = stringResource(R.string.profile_update_password),
                onClick = viewModel::submitChangePassword,
                modifier = Modifier.fillMaxWidth()
            )
            DialogActionButton(
                text = stringResource(R.string.action_cancel),
                onClick = viewModel::dismissDialog,
                modifier = Modifier.fillMaxWidth()
            )
        }

        ProfileDialog.DeleteAccount -> FinDialog(
            title = stringResource(R.string.profile_delete_confirm),
            onDismiss = viewModel::dismissDialog
        ) {
            Text(
                text = stringResource(R.string.profile_delete_warning),
                style = MaterialTheme.typography.bodyMedium,
                color = FinInk,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            if (state.hasPassword) {
                FinTextField(
                    value = state.currentPassword,
                    onValueChange = viewModel::onCurrentPasswordChange,
                    placeholder = stringResource(R.string.profile_current_password),
                    errorText = state.currentPasswordError?.let { stringResource(it) },
                    isPassword = true,
                    imeAction = ImeAction.Done,
                    keyboardActions = KeyboardActions(onDone = { viewModel.confirmDeleteAccount() })
                )
            }
            DialogActionButton(
                text = stringResource(R.string.profile_delete_account),
                onClick = viewModel::confirmDeleteAccount,
                modifier = Modifier.fillMaxWidth(),
                contentColor = FinCoral
            )
            DialogActionButton(
                text = stringResource(R.string.action_cancel),
                onClick = viewModel::dismissDialog,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    state.message?.let { messageRes ->
        FinDialog(
            title = stringResource(messageRes),
            onDismiss = viewModel::dismissMessage
        ) {
            DialogActionButton(
                text = stringResource(R.string.action_accept),
                onClick = viewModel::dismissMessage,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
