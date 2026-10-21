package com.finmojarra.app.ui.profile

import android.net.Uri
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finmojarra.app.R
import com.finmojarra.app.data.profile.ProfileDetails
import com.finmojarra.app.data.profile.ProfileDetailsRepository
import com.finmojarra.app.ui.auth.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Errores de un formulario: nombre del campo -> texto de error. Vacío = todo bien. */
typealias FormErrors = Map<String, Int>

data class ProfileDetailsUiState(
    val details: ProfileDetails = ProfileDetails(),
    val isLoading: Boolean = false,
    /** Mensaje que se muestra en un diálogo con "Aceptar". */
    @StringRes val message: Int? = null,
    /** Si es true, al aceptar el mensaje se vuelve a la pantalla anterior. */
    val closeOnDismiss: Boolean = false
)

/**
 * Lógica de las pantallas de cambio del perfil: foto, info demográfica,
 * teléfono, dirección y documento.
 */
class ProfileDetailsViewModel(
    private val repository: ProfileDetailsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileDetailsUiState(details = repository.details.value))
    val uiState: StateFlow<ProfileDetailsUiState> = _uiState.asStateFlow()

    init {
        repository.reload()
        viewModelScope.launch {
            repository.details.collect { details ->
                _uiState.update { it.copy(details = details) }
            }
        }
    }

    // ---------- Foto ----------

    fun onPhotoSelected(uri: Uri?) {
        if (uri == null || _uiState.value.isLoading) return
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val ok = repository.savePhoto(uri)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    message = if (ok) R.string.profile_msg_photo_saved else R.string.profile_err_photo,
                    closeOnDismiss = ok
                )
            }
        }
    }

    fun removePhoto() {
        val current = _uiState.value
        if (current.isLoading) return
        if (current.details.photoPath.isBlank()) {
            showMessage(R.string.profile_msg_no_photo)
            return
        }
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            repository.removePhoto()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    message = R.string.profile_msg_photo_removed,
                    closeOnDismiss = true
                )
            }
        }
    }

    // ---------- Info demográfica ----------

    fun saveDemographics(
        age: String,
        gender: String,
        occupation: String,
        education: String
    ): FormErrors {
        val errors = mutableMapOf<String, Int>()
        val ageValue = age.trim()
        if (ageValue.isNotEmpty()) {
            val number = ageValue.toIntOrNull()
            if (number == null || number !in 1..120) errors[FIELD_AGE] = R.string.error_age_invalid
        }
        if (errors.isNotEmpty()) return errors

        repository.save {
            it.copy(
                age = ageValue,
                gender = gender.trim(),
                occupation = occupation.trim(),
                education = education.trim()
            )
        }
        showSaved()
        return errors
    }

    // ---------- Teléfono ----------

    fun savePhone(newPhone: String, confirmPhone: String): FormErrors {
        val errors = mutableMapOf<String, Int>()
        val phone = newPhone.trim()
        when {
            phone.isEmpty() -> errors[FIELD_NEW] = R.string.error_field_required
            !Validators.isValidPhone(phone) -> errors[FIELD_NEW] = R.string.error_phone_invalid
        }
        when {
            confirmPhone.isBlank() -> errors[FIELD_CONFIRM] = R.string.error_field_required
            confirmPhone.trim() != phone -> errors[FIELD_CONFIRM] = R.string.error_phone_mismatch
        }
        if (errors.isNotEmpty()) return errors

        repository.save { it.copy(phone = phone) }
        showSaved()
        return errors
    }

    // ---------- Dirección ----------

    /** Los campos que se dejan vacíos conservan el valor que ya estaba guardado. */
    fun saveAddress(
        country: String,
        department: String,
        city: String,
        viaType: String,
        via: String,
        plate: String
    ): FormErrors {
        val errors = mutableMapOf<String, Int>()
        val streetStarted = viaType.isNotBlank() || via.isNotBlank() || plate.isNotBlank()
        if (streetStarted) {
            if (viaType.isBlank()) errors[FIELD_VIA_TYPE] = R.string.error_field_required
            if (via.isBlank()) errors[FIELD_VIA] = R.string.error_field_required
            if (plate.isBlank()) errors[FIELD_PLATE] = R.string.error_field_required
        }
        if (errors.isNotEmpty()) return errors

        val nothingToChange = !streetStarted &&
            country.isBlank() && department.isBlank() && city.isBlank()
        if (nothingToChange) {
            showMessage(R.string.profile_msg_nothing)
            return errors
        }

        repository.save {
            it.copy(
                country = country.trim().ifEmpty { it.country },
                department = department.trim().ifEmpty { it.department },
                city = city.trim().ifEmpty { it.city },
                street = if (streetStarted) {
                    "${viaType.trim()} ${via.trim()} # ${plate.trim()}"
                } else {
                    it.street
                }
            )
        }
        showSaved()
        return errors
    }

    // ---------- Documento ----------

    fun saveDocument(type: String, number: String): FormErrors {
        val errors = mutableMapOf<String, Int>()
        if (type.isBlank()) errors[FIELD_DOC_TYPE] = R.string.error_field_required
        if (number.isBlank()) errors[FIELD_DOC_NUMBER] = R.string.error_field_required
        if (errors.isNotEmpty()) return errors

        repository.save {
            it.copy(documentType = type.trim().uppercase(), documentNumber = number.trim())
        }
        showSaved()
        return errors
    }

    // ---------- Mensajes ----------

    fun showMessage(@StringRes message: Int) {
        _uiState.update { it.copy(message = message, closeOnDismiss = false) }
    }

    private fun showSaved() {
        _uiState.update { it.copy(message = R.string.profile_msg_saved, closeOnDismiss = true) }
    }

    fun dismissMessage() {
        _uiState.update { it.copy(message = null, closeOnDismiss = false) }
    }

    companion object {
        const val FIELD_AGE = "age"
        const val FIELD_NEW = "new"
        const val FIELD_CONFIRM = "confirm"
        const val FIELD_VIA_TYPE = "viaType"
        const val FIELD_VIA = "via"
        const val FIELD_PLATE = "plate"
        const val FIELD_DOC_TYPE = "docType"
        const val FIELD_DOC_NUMBER = "docNumber"
    }
}
