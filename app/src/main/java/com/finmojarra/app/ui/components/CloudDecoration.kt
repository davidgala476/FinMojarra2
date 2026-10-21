package com.finmojarra.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import com.finmojarra.app.ui.theme.FinTeal

private data class Blob(val x: Float, val y: Float, val radius: Float)

// Medidas en "dp de diseño" sobre el lienzo de Figma de 412 de ancho.
// Esquina superior derecha: y se mide desde el borde superior.
private val TopEndBlobs = listOf(
    Blob(x = 345f, y = 4f, radius = 72f),
    Blob(x = 336f, y = 28f, radius = 54f),
    Blob(x = 378f, y = 52f, radius = 52f),
    Blob(x = 404f, y = 64f, radius = 64f)
)

// Esquina inferior izquierda: y se mide desde el borde inferior.
private val BottomStartBlobs = listOf(
    Blob(x = 0f, y = 40f, radius = 96f),
    Blob(x = 26f, y = 92f, radius = 52f),
    Blob(x = 76f, y = 62f, radius = 50f),
    Blob(x = 118f, y = 38f, radius = 40f),
    Blob(x = 148f, y = 22f, radius = 22f)
)

/**
 * Nubes decorativas de las esquinas, dibujadas con círculos.
 * Es una aproximación del diseño; si exportas las nubes como SVG desde Figma
 * se pueden reemplazar por un vector drawable.
 */
@Composable
fun CloudDecoration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val unit = size.width / 412f
        TopEndBlobs.forEach { blob ->
            drawCircle(
                color = FinTeal,
                radius = blob.radius * unit,
                center = Offset(blob.x * unit, blob.y * unit)
            )
        }
        BottomStartBlobs.forEach { blob ->
            drawCircle(
                color = FinTeal,
                radius = blob.radius * unit,
                center = Offset(blob.x * unit, size.height - blob.y * unit)
            )
        }
    }
}
