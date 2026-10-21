package com.finmojarra.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.finmojarra.app.R
import com.finmojarra.app.ui.theme.FinBackground
import com.finmojarra.app.ui.theme.FinInk
import com.finmojarra.app.ui.theme.FinSlate900
import com.finmojarra.app.ui.theme.FinTeal

/**
 * Marco común de las pantallas de acceso: fondo claro, nubes de las esquinas,
 * flecha de volver (opcional), contenido centrado con scroll y pantalla de carga.
 */
@Composable
fun AuthScreenFrame(
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    isLoading: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FinBackground)
    ) {
        CloudDecoration(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 40.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            content = content
        )

        if (onBack != null) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .safeDrawingPadding()
                    .padding(start = 20.dp, top = 12.dp)
                    .size(48.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_left),
                    contentDescription = stringResource(R.string.action_back),
                    tint = FinSlate900,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        if (isLoading) {
            LoadingOverlay()
        }
    }
}

/** "Pantalla de carga": logo, indicador circular teal y el texto "Cargando...". */
@Composable
fun LoadingOverlay(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FinBackground)
            // Bloquea los toques sobre la pantalla que queda debajo.
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            ),
        contentAlignment = Alignment.Center
    ) {
        CloudDecoration(modifier = Modifier.fillMaxSize())
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            FinLogo()
            Spacer(modifier = Modifier.height(72.dp))
            CircularProgressIndicator(
                modifier = Modifier.size(52.dp),
                color = FinTeal,
                trackColor = FinTeal.copy(alpha = 0.2f),
                strokeWidth = 3.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.loading),
                style = MaterialTheme.typography.bodyMedium,
                color = FinInk
            )
        }
    }
}
