package com.finmojarra.app.data.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Guarda en memoria la sesión actual. Más adelante puede persistirse (DataStore). */
class SessionHolder {
    private val _session = MutableStateFlow<Session?>(null)
    val session: StateFlow<Session?> = _session.asStateFlow()

    fun set(session: Session?) {
        _session.value = session
    }
}
