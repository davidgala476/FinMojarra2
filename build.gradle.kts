// Archivo de build raíz: solo declara los plugins que usan los módulos.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    // Plugin de servicios de Google (Firebase) - paso 3.1 de la consola
    alias(libs.plugins.google.services) apply false
}
