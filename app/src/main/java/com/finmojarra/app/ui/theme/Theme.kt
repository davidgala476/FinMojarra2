package com.finmojarra.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = FinTeal,
    onPrimary = FinInk,
    secondary = FinCoral,
    onSecondary = FinWhite,
    background = FinBackground,
    onBackground = FinInk,
    surface = FinWhite,
    onSurface = FinInk,
    outline = FinFieldBorder,
    error = FinError
)

/**
 * Por ahora solo existe el modo claro (el que se está trabajando en Figma).
 * Cuando se diseñe el oscuro, se agrega un `darkColorScheme` y se elige con
 * `isSystemInDarkTheme()`.
 */
@Composable
fun FinMojarraTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = FinTypography,
        content = content
    )
}
