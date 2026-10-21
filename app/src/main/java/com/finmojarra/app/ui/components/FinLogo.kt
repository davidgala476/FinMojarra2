package com.finmojarra.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.finmojarra.app.R

@Composable
fun FinLogo(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.logo_finmojarra),
        contentDescription = stringResource(R.string.logo_description),
        contentScale = ContentScale.Fit,
        modifier = modifier
            .size(width = 135.dp, height = 139.dp)
            .clip(RoundedCornerShape(20.dp))
    )
}
