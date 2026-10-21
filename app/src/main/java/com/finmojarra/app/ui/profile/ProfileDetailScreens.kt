package com.finmojarra.app.ui.profile

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finmojarra.app.R
import com.finmojarra.app.ui.components.DialogActionButton
import com.finmojarra.app.ui.components.FinDialog
import com.finmojarra.app.ui.profile.ProfileDetailsViewModel.Companion.FIELD_AGE
import com.finmojarra.app.ui.profile.ProfileDetailsViewModel.Companion.FIELD_CONFIRM
import com.finmojarra.app.ui.profile.ProfileDetailsViewModel.Companion.FIELD_DOC_NUMBER
import com.finmojarra.app.ui.profile.ProfileDetailsViewModel.Companion.FIELD_DOC_TYPE
import com.finmojarra.app.ui.profile.ProfileDetailsViewModel.Companion.FIELD_NEW
import com.finmojarra.app.ui.profile.ProfileDetailsViewModel.Companion.FIELD_PLATE
import com.finmojarra.app.ui.profile.ProfileDetailsViewModel.Companion.FIELD_VIA
import com.finmojarra.app.ui.profile.ProfileDetailsViewModel.Companion.FIELD_VIA_TYPE
import com.finmojarra.app.ui.theme.FinCoral
import java.io.File

/** Tipos de vía para armar la dirección (Colombia). */
private val ViaTypes = listOf(
    "Calle", "Carrera", "Avenida", "Av calle", "Av carrera",
    "Diagonal", "Transversal", "Circular", "Autopista"
)

// ---------------------------------------------------------------------------
// Editar foto
// ---------------------------------------------------------------------------

@Composable
fun EditPhotoScreen(
    viewModel: ProfileDetailsViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Uri donde la cámara deja la foto (se conserva si Android recrea la pantalla).
    var pendingUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    val takePicture = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) viewModel.onPhotoSelected(pendingUri)
    }
    val pickImage = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> viewModel.onPhotoSelected(uri) }

    ProfileScaffold(onBack = onBack, isLoading = state.isLoading) {
        VSpace(16.dp)
        ProfileAvatar(
            photoPath = state.details.photoPath,
            photoStamp = state.details.photoStamp
        )
        VSpace(20.dp)
        ProfileHeading(stringResource(R.string.profile_edit_photo_title))
        VSpace(24.dp)

        ProfileCardColumn {
            ProfileInfoCard(
                label = stringResource(R.string.photo_take),
                value = stringResource(R.string.photo_take_sub),
                onClick = {
                    try {
                        val uri = newCameraUri(context)
                        pendingUri = uri
                        takePicture.launch(uri)
                    } catch (e: Exception) {
                        viewModel.showMessage(R.string.profile_err_camera)
                    }
                }
            )
            ProfileInfoCard(
                label = stringResource(R.string.photo_pick),
                value = stringResource(R.string.photo_pick_sub),
                onClick = {
                    pickImage.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            )
            ProfileInfoCard(
                label = stringResource(R.string.photo_delete),
                value = stringResource(R.string.photo_delete_sub),
                labelColor = FinCoral,
                valueColor = FinCoral,
                onClick = viewModel::removePhoto
            )
        }
        VSpace(16.dp)
    }

    DetailsMessageDialog(state = state, viewModel = viewModel, onBack = onBack)
}

/** Crea el archivo temporal (en la caché) donde la cámara guarda la foto. */
private fun newCameraUri(context: Context): Uri {
    val dir = File(context.cacheDir, "images").apply { mkdirs() }
    val file = File(dir, "camera_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

// ---------------------------------------------------------------------------
// Info demográfica
// ---------------------------------------------------------------------------

@Composable
fun DemographicsScreen(
    viewModel: ProfileDetailsViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val saved = state.details

    var age by rememberSaveable(saved.age) { mutableStateOf(saved.age) }
    var gender by rememberSaveable(saved.gender) { mutableStateOf(saved.gender) }
    var occupation by rememberSaveable(saved.occupation) { mutableStateOf(saved.occupation) }
    var education by rememberSaveable(saved.education) { mutableStateOf(saved.education) }
    var errors by remember { mutableStateOf<FormErrors>(emptyMap()) }

    ProfileScaffold(onBack = onBack, isLoading = state.isLoading) {
        VSpace(16.dp)
        ProfileAvatar(photoPath = saved.photoPath, photoStamp = saved.photoStamp)
        VSpace(20.dp)
        ProfileHeading(stringResource(R.string.demo_title))
        VSpace(24.dp)

        ProfileCardColumn {
            ProfileInputCard(
                label = stringResource(R.string.demo_age),
                value = age,
                onValueChange = {
                    age = it.filter(Char::isDigit).take(3)
                    errors = errors - FIELD_AGE
                },
                placeholder = stringResource(R.string.demo_age_hint),
                errorText = errors[FIELD_AGE]?.let { stringResource(it) },
                keyboardType = KeyboardType.Number
            )
            ProfileInputCard(
                label = stringResource(R.string.demo_gender),
                value = gender,
                onValueChange = { gender = it.take(30) },
                placeholder = stringResource(R.string.demo_gender_hint)
            )
            ProfileInputCard(
                label = stringResource(R.string.demo_occupation),
                value = occupation,
                onValueChange = { occupation = it.take(60) },
                placeholder = stringResource(R.string.demo_occupation_hint)
            )
            ProfileInputCard(
                label = stringResource(R.string.demo_education),
                value = education,
                onValueChange = { education = it.take(60) },
                placeholder = stringResource(R.string.demo_education_hint),
                imeAction = ImeAction.Done
            )
        }
        VSpace(32.dp)
        ProfileConfirmButton(
            text = stringResource(R.string.action_confirm),
            onClick = {
                errors = viewModel.saveDemographics(age, gender, occupation, education)
            }
        )
        VSpace(16.dp)
    }

    DetailsMessageDialog(state = state, viewModel = viewModel, onBack = onBack)
}

// ---------------------------------------------------------------------------
// Número celular
// ---------------------------------------------------------------------------

@Composable
fun PhoneScreen(
    viewModel: ProfileDetailsViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val saved = state.details

    var newPhone by rememberSaveable { mutableStateOf("") }
    var confirmPhone by rememberSaveable { mutableStateOf("") }
    var errors by remember { mutableStateOf<FormErrors>(emptyMap()) }

    ProfileScaffold(onBack = onBack, isLoading = state.isLoading) {
        VSpace(16.dp)
        ProfileAvatar(photoPath = saved.photoPath, photoStamp = saved.photoStamp)
        VSpace(20.dp)
        ProfileHeading(stringResource(R.string.phone_title))
        VSpace(24.dp)

        ProfileInfoCard(
            label = stringResource(R.string.phone_current),
            value = saved.phone,
            placeholder = stringResource(R.string.profile_not_set)
        )
        VSpace(12.dp)
        ProfileDivider()
        VSpace(4.dp)
        ProfileSectionCaption(stringResource(R.string.change_data_caption))
        VSpace(8.dp)

        ProfileCardColumn {
            ProfileInputCard(
                label = stringResource(R.string.phone_new),
                value = newPhone,
                onValueChange = {
                    newPhone = it.filter { c -> c.isDigit() || c == ' ' || c == '+' }.take(16)
                    errors = errors - FIELD_NEW
                },
                placeholder = stringResource(R.string.phone_new_hint),
                errorText = errors[FIELD_NEW]?.let { stringResource(it) },
                keyboardType = KeyboardType.Phone
            )
            ProfileInputCard(
                label = stringResource(R.string.phone_confirm_new),
                value = confirmPhone,
                onValueChange = {
                    confirmPhone = it.filter { c -> c.isDigit() || c == ' ' || c == '+' }.take(16)
                    errors = errors - FIELD_CONFIRM
                },
                placeholder = stringResource(R.string.phone_new_hint),
                errorText = errors[FIELD_CONFIRM]?.let { stringResource(it) },
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Done
            )
        }
        VSpace(32.dp)
        ProfileConfirmButton(
            text = stringResource(R.string.action_confirm),
            onClick = { errors = viewModel.savePhone(newPhone, confirmPhone) }
        )
        VSpace(16.dp)
    }

    DetailsMessageDialog(state = state, viewModel = viewModel, onBack = onBack)
}

// ---------------------------------------------------------------------------
// Dirección
// ---------------------------------------------------------------------------

@Composable
fun AddressScreen(
    viewModel: ProfileDetailsViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val saved = state.details

    var country by rememberSaveable { mutableStateOf("") }
    var department by rememberSaveable { mutableStateOf("") }
    var city by rememberSaveable { mutableStateOf("") }
    var viaType by rememberSaveable { mutableStateOf("") }
    var via by rememberSaveable { mutableStateOf("") }
    var plate by rememberSaveable { mutableStateOf("") }
    var errors by remember { mutableStateOf<FormErrors>(emptyMap()) }

    ProfileScaffold(onBack = onBack, isLoading = state.isLoading) {
        VSpace(16.dp)
        ProfileAvatar(photoPath = saved.photoPath, photoStamp = saved.photoStamp)
        VSpace(20.dp)
        ProfileHeading(stringResource(R.string.address_title))
        VSpace(24.dp)

        ProfileInfoCard(
            label = stringResource(R.string.address_current),
            value = saved.street,
            placeholder = stringResource(R.string.profile_not_set)
        )
        VSpace(12.dp)
        ProfileDivider()
        VSpace(4.dp)
        ProfileSectionCaption(stringResource(R.string.change_data_caption))
        VSpace(8.dp)

        ProfileCardColumn {
            ProfileInputCard(
                label = stringResource(R.string.address_country),
                value = country,
                onValueChange = { country = it.take(40) },
                placeholder = stringResource(R.string.address_country_hint),
                capitalization = KeyboardCapitalization.Words
            )
            ProfileInputCard(
                label = stringResource(R.string.address_department),
                value = department,
                onValueChange = { department = it.take(40) },
                placeholder = stringResource(R.string.address_department_hint),
                capitalization = KeyboardCapitalization.Words
            )
            ProfileInputCard(
                label = stringResource(R.string.address_city),
                value = city,
                onValueChange = { city = it.take(40) },
                placeholder = stringResource(R.string.address_city_hint),
                capitalization = KeyboardCapitalization.Words
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                ProfileDropdownCard(
                    label = stringResource(R.string.address_via_type),
                    selected = viaType,
                    options = ViaTypes,
                    placeholder = stringResource(R.string.address_via_type_hint),
                    onSelected = {
                        viaType = it
                        errors = errors - FIELD_VIA_TYPE
                    },
                    errorText = errors[FIELD_VIA_TYPE]?.let { stringResource(it) },
                    modifier = Modifier.weight(1.1f)
                )
                ProfileInputCard(
                    label = stringResource(R.string.address_via),
                    value = via,
                    onValueChange = {
                        via = it.take(12)
                        errors = errors - FIELD_VIA
                    },
                    placeholder = stringResource(R.string.address_via_hint),
                    errorText = errors[FIELD_VIA]?.let { stringResource(it) },
                    modifier = Modifier.weight(1f)
                )
                ProfileInputCard(
                    label = stringResource(R.string.address_plate),
                    value = plate,
                    onValueChange = {
                        plate = it.take(12)
                        errors = errors - FIELD_PLATE
                    },
                    placeholder = stringResource(R.string.address_plate_hint),
                    errorText = errors[FIELD_PLATE]?.let { stringResource(it) },
                    imeAction = ImeAction.Done,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        VSpace(32.dp)
        ProfileConfirmButton(
            text = stringResource(R.string.action_confirm),
            onClick = {
                errors = viewModel.saveAddress(country, department, city, viaType, via, plate)
            }
        )
        VSpace(16.dp)
    }

    DetailsMessageDialog(state = state, viewModel = viewModel, onBack = onBack)
}

// ---------------------------------------------------------------------------
// Número de documento
// ---------------------------------------------------------------------------

@Composable
fun DocumentScreen(
    viewModel: ProfileDetailsViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val saved = state.details

    var docType by rememberSaveable { mutableStateOf("") }
    var docNumber by rememberSaveable { mutableStateOf("") }
    var errors by remember { mutableStateOf<FormErrors>(emptyMap()) }

    ProfileScaffold(onBack = onBack, isLoading = state.isLoading) {
        VSpace(16.dp)
        ProfileAvatar(photoPath = saved.photoPath, photoStamp = saved.photoStamp)
        VSpace(20.dp)
        ProfileHeading(stringResource(R.string.document_title))
        VSpace(24.dp)

        ProfileInfoCard(
            label = stringResource(R.string.document_current),
            value = saved.documentLabel,
            placeholder = stringResource(R.string.profile_not_set)
        )
        VSpace(12.dp)
        ProfileDivider()
        VSpace(4.dp)
        ProfileSectionCaption(stringResource(R.string.change_data_caption))
        VSpace(8.dp)

        ProfileCardColumn {
            ProfileInputCard(
                label = stringResource(R.string.document_type),
                value = docType,
                onValueChange = {
                    docType = it.take(20)
                    errors = errors - FIELD_DOC_TYPE
                },
                placeholder = stringResource(R.string.document_type_hint),
                errorText = errors[FIELD_DOC_TYPE]?.let { stringResource(it) },
                capitalization = KeyboardCapitalization.Characters
            )
            ProfileInputCard(
                label = stringResource(R.string.document_number),
                value = docNumber,
                onValueChange = {
                    docNumber = it.take(20)
                    errors = errors - FIELD_DOC_NUMBER
                },
                placeholder = stringResource(R.string.document_number_hint),
                errorText = errors[FIELD_DOC_NUMBER]?.let { stringResource(it) },
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Done
            )
        }
        VSpace(32.dp)
        ProfileConfirmButton(
            text = stringResource(R.string.action_confirm),
            onClick = { errors = viewModel.saveDocument(docType, docNumber) }
        )
        VSpace(16.dp)
    }

    DetailsMessageDialog(state = state, viewModel = viewModel, onBack = onBack)
}

// ---------------------------------------------------------------------------
// Diálogo de resultado
// ---------------------------------------------------------------------------

/** Mensaje con "Aceptar". Si la operación terminó bien, al aceptar vuelve al perfil. */
@Composable
private fun DetailsMessageDialog(
    state: ProfileDetailsUiState,
    viewModel: ProfileDetailsViewModel,
    onBack: () -> Unit
) {
    val message = state.message ?: return
    val close = state.closeOnDismiss
    FinDialog(
        title = stringResource(message),
        onDismiss = {
            viewModel.dismissMessage()
            if (close) onBack()
        }
    ) {
        DialogActionButton(
            text = stringResource(R.string.action_accept),
            onClick = {
                viewModel.dismissMessage()
                if (close) onBack()
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
