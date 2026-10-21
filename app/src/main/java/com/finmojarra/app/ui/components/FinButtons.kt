package com.finmojarra.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finmojarra.app.ui.theme.FinInk
import com.finmojarra.app.ui.theme.FinSlate200
import com.finmojarra.app.ui.theme.FinSlate600
import com.finmojarra.app.ui.theme.FinWhite

/** Botón principal: negro, 40dp de alto, radio 8, texto blanco. */
@Composable
fun FinPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = FinInk,
            contentColor = FinWhite,
            disabledContainerColor = FinInk.copy(alpha = 0.4f),
            disabledContentColor = FinWhite
        ),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

/** Enlace de texto negro. Tiene al menos 40dp de alto para que sea fácil de tocar. */
@Composable
fun FinLinkText(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = FinInk,
        textAlign = textAlign,
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .defaultMinSize(minHeight = 40.dp)
            .padding(horizontal = 4.dp, vertical = 10.dp)
    )
}

/** Botón de los diálogos: 48dp de alto, radio 12, borde gris claro. */
@Composable
fun DialogActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentColor: Color = FinSlate600,
    borderWidth: Dp = 1.dp
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(borderWidth, FinSlate200),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = FinWhite,
            contentColor = contentColor
        ),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
            maxLines = 1
        )
    }
}
