package com.finmojarra.app.ui.learn

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finmojarra.app.R
import com.finmojarra.app.data.learn.LearningCatalog
import com.finmojarra.app.data.learn.LearningSteps
import com.finmojarra.app.data.learn.LearningPath
import com.finmojarra.app.ui.goals.GoalsScaffold
import com.finmojarra.app.ui.goals.GoalsTab
import com.finmojarra.app.ui.goals.GoalsTabs
import com.finmojarra.app.ui.theme.FinFieldBorder
import com.finmojarra.app.ui.theme.FinInk
import com.finmojarra.app.ui.theme.FinMutedText
import com.finmojarra.app.ui.theme.FinTeal
import com.finmojarra.app.ui.theme.FinWhite
import java.util.Locale

/** "Vamos a aprender": una tarjeta por curso con su avance, XP y próximo hito. */
@Composable
fun LearnScreen(
    photoPath: String,
    photoStamp: Long,
    onBack: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenSavings: () -> Unit,
    onOpenGoals: () -> Unit,
    stepsByPath: Map<String, Set<String>>,
    onOpenPath: (String) -> Unit
) {
    LearnFrame(
        photoPath = photoPath,
        photoStamp = photoStamp,
        onBack = onBack,
        onOpenProfile = onOpenProfile,
        onOpenSavings = onOpenSavings,
        onOpenGoals = onOpenGoals,
        spacing = 20.dp
    ) {
        LearningCatalog.paths.forEach { path ->
            LearningPathCard(
                path = if (path.hasContent) {
                    path.withCompletedSteps(LearningSteps.count(stepsByPath[path.id].orEmpty()))
                } else {
                    path
                },
                onClick = if (path.hasContent) ({ onOpenPath(path.id) }) else null
            )
        }
    }
}

@Composable
private fun LearningPathCard(path: LearningPath, onClick: (() -> Unit)?) {
    val click = if (onClick != null) {
        Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(role = Role.Button, onClick = onClick)
    } else {
        Modifier
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .learnCard()
            .then(click)
            .padding(horizontal = 16.dp, vertical = 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            StarBadge()
            Text(
                text = stringResource(path.title),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = FinInk,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp, end = 8.dp, top = 28.dp)
            )
            RankPill(text = stringResource(path.badge))
        }

        Spacer(modifier = Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(
                    R.string.learn_completed,
                    path.completedLessons,
                    path.totalLessons
                ),
                style = MaterialTheme.typography.bodySmall,
                color = FinMutedText
            )
            Text(
                text = stringResource(R.string.learn_percent, path.percent),
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = FinTeal
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        val animatedProgress by animateFloatAsState(targetValue = path.progress, label = "pathProgress")
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = FinTeal,
            trackColor = FinFieldBorder,
            gapSize = 0.dp,
            drawStopIndicator = {}
        )

        Spacer(modifier = Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = stringResource(R.string.learn_xp_earned),
                style = MaterialTheme.typography.bodySmall,
                color = FinMutedText
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(R.string.learn_next_milestone),
                    style = MaterialTheme.typography.bodySmall,
                    color = FinMutedText
                )
                Text(
                    text = stringResource(R.string.learn_xp_value, formatXp(path.nextMilestoneXp)),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = FinTeal
                )
            }
        }
    }
}

/** Círculo teal con la estrella de la esquina de cada tarjeta. */
@Composable
private fun StarBadge() {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .size(44.dp)
            .background(FinTeal, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_star),
            contentDescription = null,
            tint = FinWhite,
            modifier = Modifier.size(24.dp)
        )
    }
}

/** Insignia de rango ("Mojarra Junior"): pastilla teal con el texto en dos líneas. */
@Composable
private fun RankPill(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall.copy(
            fontSize = 11.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.Bold
        ),
        color = FinWhite,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .widthIn(max = 92.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(FinTeal)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    )
}

/** 1000 -> "1 000" (con espacio fino, como en el diseño, y sin partirse en dos líneas). */
private fun formatXp(value: Int): String =
    String.format(Locale.US, "%,d", value).replace(',', '\u00A0')
