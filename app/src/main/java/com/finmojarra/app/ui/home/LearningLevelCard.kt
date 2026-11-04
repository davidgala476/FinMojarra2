package com.finmojarra.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finmojarra.app.R
import com.finmojarra.app.data.learn.LearningLevel
import com.finmojarra.app.ui.theme.FinFieldBorder
import com.finmojarra.app.ui.theme.FinInk
import com.finmojarra.app.ui.theme.FinMutedText
import com.finmojarra.app.ui.theme.FinTeal

/**
 * Parte superior del inicio: el nivel de aprendizaje del usuario según lo que ha hecho en
 * "Vamos a aprender". Al tocarla abre esa sección.
 */
@Composable
fun LearningLevelCard(
    level: LearningLevel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    HomeCard(modifier = modifier.clickable(role = Role.Button, onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(FinTeal),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = level.level.toString(),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = FinInk
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.home_level_title, level.level, level.maxLevel),
                    fontSize = 12.sp,
                    color = FinMutedText
                )
                Text(
                    text = stringResource(level.rank),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = FinInk
                )
            }
            Text(
                text = stringResource(R.string.learn_xp_value, level.xp.toString()),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = FinInk
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        LinearProgressIndicator(
            progress = { level.progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = FinTeal,
            trackColor = FinFieldBorder,
            gapSize = 0.dp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.home_level_steps, level.completedSteps, level.totalSteps),
                fontSize = 12.sp,
                color = FinMutedText,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (level.nextRank != null) {
                    stringResource(R.string.home_level_next, stringResource(level.nextRank))
                } else {
                    stringResource(R.string.home_level_max)
                },
                fontSize = 12.sp,
                color = FinMutedText
            )
        }
    }
}
