package com.finmojarra.app.ui.learn

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finmojarra.app.R
import com.finmojarra.app.ui.goals.GoalsScaffold
import com.finmojarra.app.ui.goals.GoalsTab
import com.finmojarra.app.ui.goals.GoalsTabs
import com.finmojarra.app.ui.theme.FinTeal
import com.finmojarra.app.ui.theme.FinWhite

/** Tarjeta blanca con esquinas redondeadas y sombra suave, como en el diseño. */
fun Modifier.learnCard(): Modifier {
    val shape = RoundedCornerShape(16.dp)
    return this
        .shadow(2.dp, shape)
        .background(FinWhite, shape)
}

/**
 * Marco de todas las pantallas de "Vamos a aprender": encabezado, pestañas con
 * "Vamos a aprender" seleccionada y una zona con scroll donde va el contenido.
 */
@Composable
fun LearnFrame(
    photoPath: String,
    photoStamp: Long,
    onBack: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenSavings: () -> Unit,
    onOpenGoals: () -> Unit,
    modifier: Modifier = Modifier,
    spacing: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    GoalsScaffold(
        onBack = onBack,
        onOpenProfile = onOpenProfile,
        photoPath = photoPath,
        photoStamp = photoStamp,
        modifier = modifier
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        GoalsTabs(
            selected = GoalsTab.Learn,
            onSavings = onOpenSavings,
            onGoals = onOpenGoals,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(spacing)
        ) {
            content()
        }
    }
}

/** Botón teal en forma de pastilla con flecha ("Ver video →", "Aprender →"). */
@Composable
fun TealPillButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(containerColor = FinTeal, contentColor = FinWhite),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 0.dp),
        modifier = modifier.height(36.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
            painter = painterResource(R.drawable.ic_arrow_forward),
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
    }
}

/** Título de sección ("Píldoras en un minuto") con subtítulo opcional. */
@Composable
fun LearnSectionTitle(title: String, subtitle: String? = null) {
    Column(modifier = Modifier.padding(horizontal = 4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            ),
            color = com.finmojarra.app.ui.theme.FinInk
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = com.finmojarra.app.ui.theme.FinInk
            )
        }
    }
}
