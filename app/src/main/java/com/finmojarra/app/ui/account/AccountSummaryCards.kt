package com.finmojarra.app.ui.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finmojarra.app.R
import com.finmojarra.app.data.account.MonthSummary
import com.finmojarra.app.data.account.WeeklySavings
import com.finmojarra.app.ui.goals.formatMoney
import com.finmojarra.app.ui.home.HomeCard
import com.finmojarra.app.ui.theme.FinError
import com.finmojarra.app.ui.theme.FinFieldBorder
import com.finmojarra.app.ui.theme.FinInk
import com.finmojarra.app.ui.theme.FinMutedText
import com.finmojarra.app.ui.theme.FinTeal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** "Ahorro de esta semana": lo que has pasado a tus metas desde el lunes y lo que falta. */
@Composable
fun WeeklySavingsCard(weekly: WeeklySavings, isLoading: Boolean) {
    HomeCard {
        Text(
            text = stringResource(R.string.home_week_title),
            style = MaterialTheme.typography.bodySmall,
            color = FinMutedText
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = formatMoney(weekly.saved),
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold),
            color = FinInk,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(6.dp))
        when {
            isLoading -> Unit

            !weekly.hasTarget -> Text(
                text = stringResource(R.string.home_week_empty),
                style = MaterialTheme.typography.bodySmall,
                color = FinMutedText
            )

            else -> {
                LinearProgressIndicator(
                    progress = { weekly.progress },
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
                Text(
                    text = stringResource(R.string.home_week_goal, formatMoney(weekly.target)),
                    style = MaterialTheme.typography.bodySmall,
                    color = FinMutedText
                )
                Text(
                    text = if (weekly.missing > 0.0) {
                        stringResource(R.string.home_week_missing, formatMoney(weekly.missing))
                    } else {
                        stringResource(R.string.home_week_done)
                    },
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = if (weekly.missing > 0.0) FinInk else FinTeal
                )
            }
        }
    }
}

/** "Resumen de <mes>": ingresos, retiros y dinero enviado a metas en el mes en curso. */
@Composable
fun MonthSummaryCard(summary: MonthSummary, isLoading: Boolean) {
    val monthName = remember {
        SimpleDateFormat("MMMM", Locale("es", "CO")).format(Date())
            .replaceFirstChar { it.uppercase() }
    }
    HomeCard {
        Text(
            text = stringResource(R.string.home_month_title, monthName),
            style = MaterialTheme.typography.titleMedium,
            color = FinInk
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (!isLoading) {
            SummaryRow(stringResource(R.string.home_month_deposits), "+" + formatMoney(summary.deposited), FinTeal)
            SummaryRow(stringResource(R.string.home_month_withdrawals), "−" + formatMoney(summary.withdrawn), FinError)
            SummaryRow(stringResource(R.string.home_month_goals), formatMoney(summary.toGoals), FinInk)
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = FinMutedText)
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = valueColor,
            maxLines = 1
        )
    }
}
