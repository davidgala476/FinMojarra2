package com.finmojarra.app.ui.auth

import android.util.Patterns

object Validators {
    /** Largo mínimo de contraseña. Si cambia, actualiza `error_password_short` en strings.xml. */
    const val MIN_PASSWORD_LENGTH = 8

    fun isValidEmail(value: String): Boolean =
        Patterns.EMAIL_ADDRESS.matcher(value.trim()).matches()

    fun isValidPhone(value: String): Boolean =
        value.count { it.isDigit() } in 7..15
}
