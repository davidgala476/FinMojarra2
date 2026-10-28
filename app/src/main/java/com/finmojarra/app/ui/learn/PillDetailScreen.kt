package com.finmojarra.app.ui.learn

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finmojarra.app.R
import com.finmojarra.app.ui.theme.FinAmber
import com.finmojarra.app.ui.theme.FinAmberDark
import com.finmojarra.app.ui.theme.FinInk
import com.finmojarra.app.ui.theme.FinSlate600
import com.finmojarra.app.ui.theme.FinTeal
import com.finmojarra.app.ui.theme.FinTealDark
import com.finmojarra.app.ui.theme.FinWhite

/** Detalle de la píldora "La ilusión del dinero gratis" (se abre con el botón Aprender). */
@Composable
fun PillDetailScreen(
    photoPath: String,
    photoStamp: Long,
    onBack: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenSavings: () -> Unit,
    onOpenGoals: () -> Unit,
    onViewed: () -> Unit
) {
    // Abrir la píldora cuenta como verla.
    LaunchedEffect(Unit) { onViewed() }

    LearnFrame(
        photoPath = photoPath,
        photoStamp = photoStamp,
        onBack = onBack,
        onOpenProfile = onOpenProfile,
        onOpenSavings = onOpenSavings,
        onOpenGoals = onOpenGoals
    ) {
        LearnSectionTitle(
            title = stringResource(R.string.learn_pills_title),
            subtitle = stringResource(R.string.learn_pills_subtitle)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .learnCard()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.learn_pill_category),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = FinTeal
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.learn_pill_title),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = FinInk
            )

            Spacer(modifier = Modifier.height(14.dp))
            QuoteBlock()

            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoCard(
                    icon = R.drawable.ic_credit_card,
                    title = stringResource(R.string.learn_real_title),
                    description = stringResource(R.string.learn_real_desc),
                    titleColor = FinTealDark,
                    background = FinTeal.copy(alpha = 0.12f),
                    border = FinTeal.copy(alpha = 0.35f),
                    modifier = Modifier.weight(1f)
                )
                InfoCard(
                    icon = R.drawable.ic_warning,
                    title = stringResource(R.string.learn_borrowed_title),
                    description = stringResource(R.string.learn_borrowed_desc),
                    titleColor = FinAmberDark,
                    background = FinAmber.copy(alpha = 0.14f),
                    border = FinAmber.copy(alpha = 0.40f),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/** Cita con borde teal a la izquierda. */
@Composable
private fun QuoteBlock() {
    val shape = RoundedCornerShape(10.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .shadow(2.dp, shape)
            .background(FinWhite, shape)
    ) {
        Spacer(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(FinTeal)
        )
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = stringResource(R.string.learn_pill_quote),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = FinInk
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.learn_pill_full),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = com.finmojarra.app.ui.theme.FinMutedText
            )
        }
    }
}

@Composable
private fun InfoCard(
    @DrawableRes icon: Int,
    title: String,
    description: String,
    titleColor: Color,
    background: Color,
    border: Color,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(10.dp)
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(shape)
            .background(background)
            .border(1.dp, border, shape)
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = titleColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = titleColor
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 15.sp),
            color = FinSlate600
        )
    }
}
