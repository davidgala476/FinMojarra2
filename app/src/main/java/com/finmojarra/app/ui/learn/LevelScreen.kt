package com.finmojarra.app.ui.learn

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finmojarra.app.R
import com.finmojarra.app.data.learn.LearningCatalog
import com.finmojarra.app.data.learn.LearningSteps
import com.finmojarra.app.data.learn.LevelContent
import com.finmojarra.app.data.learn.LearningPath
import com.finmojarra.app.ui.theme.FinAmber
import com.finmojarra.app.ui.theme.FinFieldBorder
import com.finmojarra.app.ui.theme.FinInk
import com.finmojarra.app.ui.theme.FinMutedText
import com.finmojarra.app.ui.theme.FinTeal
import com.finmojarra.app.ui.theme.FinTealDark
import com.finmojarra.app.ui.theme.FinWhite

/**
 * Primer apartado de un nivel: avance del curso y el video destacado. "Ver video" lleva al siguiente paso (píldoras y pregunta).
 */
@Composable
fun LevelScreen(
    level: LevelContent,
    photoPath: String,
    photoStamp: Long,
    onBack: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenSavings: () -> Unit,
    onOpenGoals: () -> Unit,
    completedSteps: Set<String>,
    onWatchVideo: () -> Unit
) {
    val path = LearningCatalog.paths.first { it.id == level.pathId }
        .withCompletedSteps(LearningSteps.count(completedSteps))

    LearnFrame(
        photoPath = photoPath,
        photoStamp = photoStamp,
        onBack = onBack,
        onOpenProfile = onOpenProfile,
        onOpenSavings = onOpenSavings,
        onOpenGoals = onOpenGoals
    ) {
        CourseProgressCard(path)
        FeaturedVideoCard(level, onWatchVideo)
    }
}

@Composable
private fun CourseProgressCard(path: LearningPath) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .learnCard()
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(
            text = stringResource(path.title),
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            ),
            color = FinInk
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.learn_completed, path.completedLessons, path.totalLessons),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = FinMutedText
            )
            Text(
                text = stringResource(R.string.learn_percent, path.percent),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = FinTeal
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        val animatedProgress by animateFloatAsState(targetValue = path.progress, label = "levelProgress")
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = FinTeal,
            trackColor = FinFieldBorder,
            gapSize = 0.dp,
            drawStopIndicator = {}
        )
    }
}

@Composable
private fun FeaturedVideoCard(level: LevelContent, onWatchVideo: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .learnCard()
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(FinTeal.copy(alpha = 0.12f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(FinTeal, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.learn_featured_tag),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = FinTealDark
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.ic_schedule),
                    contentDescription = null,
                    tint = FinMutedText,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(level.duration),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = FinMutedText
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        VideoThumbnail(level = level, onClick = onWatchVideo)

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(level.videoTitle),
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            ),
            color = FinInk
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(level.videoDescription),
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            color = FinMutedText,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(FinTeal.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_person),
                    contentDescription = null,
                    tint = FinTeal,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.learn_author),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = FinInk
                )
                Text(
                    text = stringResource(level.authorMeta),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = FinMutedText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(FinTeal.copy(alpha = 0.12f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_menu_book),
                    contentDescription = null,
                    tint = FinTealDark,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.learn_new_tag),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = FinTealDark
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        TealPillButton(
            text = stringResource(R.string.learn_watch_video),
            onClick = onWatchVideo,
            modifier = Modifier.align(Alignment.End)
        )
    }
}

/**
 * Miniatura del video: fondo oscuro con una tarjeta decorativa, botón de play al centro y
 * la duración arriba a la derecha. Es un dibujo hecho con Compose (no hay archivo de video).
 */
@Composable
private fun VideoThumbnail(level: LevelContent, onClick: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 10f)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(Color(0xFF06101A), Color(0xFF12303A))))
            .clickable(
                onClickLabel = stringResource(R.string.learn_play_description),
                role = Role.Button,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(width = 170.dp, height = 104.dp)
                .rotate(-14f)
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF3A4A55), Color(0xFF141E26))))
                .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(12.dp))
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.padding(12.dp)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
                    .size(width = 28.dp, height = 20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(FinAmber.copy(alpha = 0.85f))
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(56.dp)
                .background(FinTeal, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_play),
                contentDescription = stringResource(R.string.learn_play_description),
                tint = FinWhite,
                modifier = Modifier.size(30.dp)
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_schedule),
                contentDescription = null,
                tint = FinWhite,
                modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(level.duration),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = FinWhite
            )
        }
    }
}
