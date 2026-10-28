package com.finmojarra.app.ui.home

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finmojarra.app.R
import com.finmojarra.app.data.goals.Goal
import com.finmojarra.app.ui.components.FinLinkText
import com.finmojarra.app.ui.goals.GoalsScaffold
import com.finmojarra.app.ui.goals.GoalsTab
import com.finmojarra.app.ui.goals.GoalsTabs
import com.finmojarra.app.ui.goals.GoalsViewModel
import com.finmojarra.app.ui.goals.formatMoney
import com.finmojarra.app.ui.theme.FinFieldBorder
import com.finmojarra.app.ui.theme.FinGoogleButton
import com.finmojarra.app.ui.theme.FinInk
import com.finmojarra.app.ui.theme.FinMutedText
import com.finmojarra.app.ui.theme.FinTeal
import com.finmojarra.app.ui.theme.FinWhite

private const val MAX_BARS = 6
private val ChartHeight = 140.dp
private val AxisWidth = 34.dp

/** Resumen de todas las metas del usuario. */
private data class SavingsSummary(
    val totalSaved: Double,
    val totalTarget: Double,
    val remaining: Double,
    val active: Int,
    val completed: Int,
    val percent: Int
) {
    companion object {
        fun from(goals: List<Goal>): SavingsSummary {
            val saved = goals.sumOf { it.savedAmount }
            val target = goals.sumOf { it.targetAmount }
            val completed = goals.count { it.isCompleted }
            val percent = if (target > 0.0) {
                (saved / target * 100).toInt().coerceIn(0, 100)
            } else {
                0
            }
            return SavingsSummary(
                totalSaved = saved,
                totalTarget = target,
                remaining = goals.sumOf { it.remainingAmount },
                active = goals.size - completed,
                completed = completed,
                percent = percent
            )
        }
    }
}

/**
 * Inicio (pestaña "Ahorros"): cuánto dinero tienes ahorrado en tus metas, accesos a los
 * módulos (metas, nueva meta, perfil) y estadísticas de tus metas.
 */
@Composable
fun HomeScreen(
    viewModel: GoalsViewModel,
    photoPath: String,
    photoStamp: Long,
    onOpenProfile: () -> Unit,
    onOpenGoals: () -> Unit,
    onCreateGoal: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val summary = remember(state.goals) { SavingsSummary.from(state.goals) }

    GoalsScaffold(
        onBack = null,
        onOpenProfile = onOpenProfile,
        photoPath = photoPath,
        photoStamp = photoStamp
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        GoalsTabs(
            selected = GoalsTab.Savings,
            onGoals = onOpenGoals,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Shortcut(R.drawable.ic_flag, stringResource(R.string.home_goals), onOpenGoals)
                Shortcut(R.drawable.ic_add, stringResource(R.string.home_shortcut_new), onCreateGoal)
                Shortcut(R.drawable.ic_person, stringResource(R.string.home_shortcut_profile), onOpenProfile)
            }

            MoneyCard(summary = summary, hasGoals = state.goals.isNotEmpty(), isLoading = state.isLoading)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(FinGoogleButton)
                    .clickable(role = Role.Button, onClick = onOpenGoals),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.home_view_goals),
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = FinInk
                )
            }

            StatsCard(
                goals = state.goals,
                summary = summary,
                isLoading = state.isLoading,
                hasError = state.hasError,
                onRetry = viewModel::retry
            )
        }
    }
}

@Composable
private fun Shortcut(@DrawableRes icon: Int, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(FinWhite)
                .border(1.dp, FinFieldBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = FinInk,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = FinInk
        )
    }
}

@Composable
private fun HomeCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(FinWhite)
            .border(1.dp, FinFieldBorder, shape)
            .padding(16.dp)
    ) { content() }
}

/** "Tu dinero ahorrado": suma de lo ahorrado en todas las metas. */
@Composable
private fun MoneyCard(summary: SavingsSummary, hasGoals: Boolean, isLoading: Boolean) {
    HomeCard {
        Text(
            text = stringResource(R.string.home_money_title),
            style = MaterialTheme.typography.bodySmall,
            color = FinMutedText
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = formatMoney(summary.totalSaved),
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            ),
            color = FinInk,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(6.dp))
        if (hasGoals) {
            LinearProgressIndicator(
                progress = { summary.percent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = FinTeal,
                trackColor = FinFieldBorder,
                gapSize = 0.dp,
                drawStopIndicator = {}
            )
            Spacer(modifier = Modifier.height(6.dp))
        }
        Text(
            text = when {
                hasGoals -> stringResource(R.string.home_money_percent, summary.percent)
                isLoading -> ""
                else -> stringResource(R.string.home_money_empty)
            },
            style = MaterialTheme.typography.bodySmall,
            color = FinTeal
        )
    }
}

/** "Estadísticas de tus metas": totales y una gráfica con el avance de cada meta. */
@Composable
private fun StatsCard(
    goals: List<Goal>,
    summary: SavingsSummary,
    isLoading: Boolean,
    hasError: Boolean,
    onRetry: () -> Unit
) {
    HomeCard {
        Text(
            text = stringResource(R.string.home_stats_title),
            style = MaterialTheme.typography.titleMedium,
            color = FinInk
        )
        Spacer(modifier = Modifier.height(12.dp))

        when {
            isLoading -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ChartHeight),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = FinTeal) }

            hasError -> Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.goals_error),
                    style = MaterialTheme.typography.bodySmall,
                    color = FinMutedText,
                    textAlign = TextAlign.Center
                )
                FinLinkText(text = stringResource(R.string.action_retry), onClick = onRetry)
            }

            goals.isEmpty() -> Text(
                text = stringResource(R.string.home_chart_empty),
                style = MaterialTheme.typography.bodySmall,
                color = FinMutedText,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
            )

            else -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatItem(summary.active.toString(), stringResource(R.string.home_stat_active), Modifier.weight(0.8f))
                    StatItem(summary.completed.toString(), stringResource(R.string.home_stat_completed), Modifier.weight(0.8f))
                    StatItem(formatMoney(summary.remaining), stringResource(R.string.home_stat_remaining), Modifier.weight(1.4f))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.home_chart_title),
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = FinInk
                )
                Spacer(modifier = Modifier.height(8.dp))
                GoalsProgressChart(goals = goals)
                if (goals.size > MAX_BARS) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.home_chart_more, MAX_BARS, goals.size),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        color = FinMutedText
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
            color = FinTeal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = FinMutedText
        )
    }
}

/** Una barra por meta (hasta 6): lo que llevas ahorrado frente al 100% de la meta. */
@Composable
private fun GoalsProgressChart(goals: List<Goal>) {
    val shown = goals.take(MAX_BARS)
    val grid = FinFieldBorder
    val track = FinTeal.copy(alpha = 0.12f)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(ChartHeight)
        ) {
            Column(
                modifier = Modifier
                    .width(AxisWidth)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("100%", "75%", "50%", "25%", "0%").forEach { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        color = FinMutedText
                    )
                }
            }
            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                val top = 7.dp.toPx()
                val bottom = size.height - 7.dp.toPx()
                val height = bottom - top

                for (i in 0..4) {
                    val y = top + height * i / 4f
                    drawLine(grid, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
                }

                val slot = size.width / shown.size
                val barWidth = minOf(slot * 0.5f, 28.dp.toPx())
                val radius = CornerRadius(6.dp.toPx())
                shown.forEachIndexed { index, goal ->
                    val x = slot * index + (slot - barWidth) / 2f
                    drawRoundRect(track, Offset(x, top), Size(barWidth, height), radius)
                    val fill = height * goal.progress
                    if (fill > 0f) {
                        drawRoundRect(FinTeal, Offset(x, bottom - fill), Size(barWidth, fill), radius)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = AxisWidth)
        ) {
            shown.indices.forEach { index ->
                Text(
                    text = stringResource(R.string.home_chart_goal_label, index + 1),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = FinMutedText,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
