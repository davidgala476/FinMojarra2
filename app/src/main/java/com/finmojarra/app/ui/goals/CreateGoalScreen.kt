package com.finmojarra.app.ui.goals

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finmojarra.app.R
import com.finmojarra.app.ui.components.DialogActionButton
import com.finmojarra.app.ui.components.FinDialog
import com.finmojarra.app.ui.components.FinLinkText
import com.finmojarra.app.ui.components.FinPrimaryButton
import com.finmojarra.app.ui.components.FinTextField
import com.finmojarra.app.ui.theme.FinFieldBorder
import com.finmojarra.app.ui.theme.FinInk
import com.finmojarra.app.ui.theme.FinMutedText
import com.finmojarra.app.ui.theme.FinTeal
import com.finmojarra.app.ui.theme.FinWhite

/**
 * Crear meta: nombre, cuánto cuesta, cuánto piensa invertir por semana y una imagen de la
 * galería. Al terminar muestra "Meta creada"; si falta algo, "Completa todos los datos...".
 */
@Composable
fun CreateGoalScreen(
    viewModel: CreateGoalViewModel,
    photoPath: String,
    photoStamp: Long,
    onBack: () -> Unit,
    onOpenProfile: () -> Unit,
    onCreated: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Selector de fotos del sistema: no necesita permisos.
    val pickImage = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> viewModel.onImageSelected(uri) }
    val openGallery = {
        pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    val shape = RoundedCornerShape(12.dp)

    GoalsScaffold(
        onBack = onBack,
        onOpenProfile = onOpenProfile,
        photoPath = photoPath,
        photoStamp = photoStamp,
        isLoading = state.isSaving
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.goals_create_title),
                style = MaterialTheme.typography.titleMedium,
                color = FinInk
            )
            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(FinWhite)
                    .border(1.dp, FinFieldBorder, shape)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        GoalBadgeIcon()
                        Text(
                            text = stringResource(R.string.goals_form_name),
                            style = MaterialTheme.typography.titleMedium,
                            color = FinInk,
                            modifier = Modifier.padding(start = 10.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    FinTextField(
                        value = state.name,
                        onValueChange = viewModel::onNameChange,
                        placeholder = stringResource(R.string.goals_name_placeholder),
                        errorText = state.nameError?.let { stringResource(it) }
                    )
                }

                Column {
                    FinTextField(
                        value = state.amount,
                        onValueChange = viewModel::onAmountChange,
                        label = stringResource(R.string.goals_form_cost),
                        placeholder = stringResource(R.string.goals_amount_placeholder),
                        errorText = state.amountError?.let { stringResource(it) },
                        keyboardType = KeyboardType.Number
                    )
                    state.amount.toLongOrNull()?.let { value ->
                        Row(
                            modifier = Modifier.padding(start = 5.dp, top = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.goals_your_goal),
                                style = MaterialTheme.typography.bodySmall,
                                color = FinMutedText
                            )
                            Text(
                                text = formatMoney(value.toDouble()),
                                style = MaterialTheme.typography.bodySmall,
                                color = FinTeal
                            )
                        }
                    }
                }

                Column {
                    FinTextField(
                        value = state.weekly,
                        onValueChange = viewModel::onWeeklyChange,
                        label = stringResource(R.string.goals_form_weekly),
                        placeholder = stringResource(R.string.goals_amount_placeholder),
                        errorText = state.weeklyError?.let { stringResource(it) },
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                        keyboardActions = KeyboardActions(onDone = { viewModel.save() })
                    )
                    state.weekly.toLongOrNull()?.let { value ->
                        Text(
                            text = formatMoney(value.toDouble()),
                            style = MaterialTheme.typography.bodySmall,
                            color = FinTeal,
                            modifier = Modifier.padding(start = 5.dp, top = 6.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = stringResource(R.string.goals_form_image),
                        style = MaterialTheme.typography.titleMedium,
                        color = FinInk,
                        modifier = Modifier.padding(start = 5.dp, bottom = 8.dp)
                    )
                    ImagePickerBox(
                        imageUri = state.imageUri,
                        onClick = openGallery
                    )
                    if (state.imageUri != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            FinLinkText(
                                text = stringResource(R.string.goals_image_change),
                                onClick = openGallery
                            )
                            FinLinkText(
                                text = stringResource(R.string.goals_image_remove),
                                onClick = viewModel::removeImage
                            )
                        }
                    }
                }

                FinPrimaryButton(
                    text = stringResource(R.string.goals_create_button),
                    onClick = viewModel::save
                )
            }
        }
    }

    if (state.showIncomplete) {
        FinDialog(
            title = stringResource(R.string.goals_incomplete),
            onDismiss = viewModel::dismissIncomplete
        ) {
            DialogActionButton(
                text = stringResource(R.string.action_retry),
                onClick = viewModel::dismissIncomplete,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (state.created) {
        FinDialog(title = stringResource(R.string.goals_created), onDismiss = onCreated) {
            DialogActionButton(
                text = stringResource(R.string.action_back),
                onClick = onCreated,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    state.message?.let { messageRes ->
        FinDialog(title = stringResource(messageRes), onDismiss = viewModel::dismissMessage) {
            DialogActionButton(
                text = stringResource(R.string.action_accept),
                onClick = viewModel::dismissMessage,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/** Recuadro para elegir la imagen: muestra el icono de imagen o la foto elegida. */
@Composable
private fun ImagePickerBox(imageUri: android.net.Uri?, onClick: () -> Unit) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(4f / 3f)
            .clip(shape)
            .border(1.dp, FinFieldBorder, shape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (imageUri != null) {
            GoalPickedImage(uri = imageUri, modifier = Modifier.fillMaxWidth().aspectRatio(4f / 3f))
        } else {
            Icon(
                painter = painterResource(R.drawable.ic_image),
                contentDescription = stringResource(R.string.goals_form_image),
                tint = FinInk,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}
