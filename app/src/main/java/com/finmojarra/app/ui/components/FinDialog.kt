package com.finmojarra.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.finmojarra.app.ui.theme.FinInk
import com.finmojarra.app.ui.theme.FinSlate900
import com.finmojarra.app.ui.theme.FinWhite

/**
 * Diálogo del diseño ("confirmation-modal"): tarjeta blanca, borde negro de 1dp,
 * radio 20, padding 32, título Inter Bold 20 y acciones debajo.
 */
@Composable
fun FinDialog(
    title: AnnotatedString,
    onDismiss: () -> Unit,
    actions: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .widthIn(max = 332.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = FinWhite,
            border = BorderStroke(1.dp, FinInk),
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = FinSlate900,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                actions()
            }
        }
    }
}

@Composable
fun FinDialog(
    title: String,
    onDismiss: () -> Unit,
    actions: @Composable ColumnScope.() -> Unit
) {
    FinDialog(title = AnnotatedString(title), onDismiss = onDismiss, actions = actions)
}
