package com.finmojarra.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.finmojarra.app.R
import com.finmojarra.app.ui.components.CloudDecoration
import com.finmojarra.app.ui.theme.FinBackground
import com.finmojarra.app.ui.theme.FinTeal
import com.finmojarra.app.ui.theme.FinWhite

/**
 * Pantalla de inicio tras iniciar sesión: por ahora solo muestra el icono del perfil,
 * que abre la pantalla de perfil de usuario.
 */
@Composable
fun HomeScreen(onOpenProfile: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FinBackground)
    ) {
        CloudDecoration(modifier = Modifier.fillMaxSize())

        IconButton(
            onClick = onOpenProfile,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .safeDrawingPadding()
                .padding(end = 20.dp, top = 12.dp)
                .size(48.dp),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = FinTeal,
                contentColor = FinWhite
            )
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_person),
                contentDescription = stringResource(R.string.home_profile_description),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
