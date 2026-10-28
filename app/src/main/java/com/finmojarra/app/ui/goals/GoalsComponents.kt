package com.finmojarra.app.ui.goals

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finmojarra.app.R
import com.finmojarra.app.data.goals.GoalImageCodec
import com.finmojarra.app.data.goals.GoalWriteResult
import com.finmojarra.app.ui.components.CloudDecoration
import com.finmojarra.app.ui.components.LoadingOverlay
import com.finmojarra.app.ui.profile.ProfileAvatar
import com.finmojarra.app.ui.theme.FinBackground
import com.finmojarra.app.ui.theme.FinFieldBorder
import com.finmojarra.app.ui.theme.FinInk
import com.finmojarra.app.ui.theme.FinMutedText
import com.finmojarra.app.ui.theme.FinSlate900
import com.finmojarra.app.ui.theme.FinTeal
import com.finmojarra.app.ui.theme.FinWhite
import java.text.NumberFormat
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val colombia = Locale("es", "CO")

/** "$450.000.000" (sin decimales, con puntos de miles). */
fun formatMoney(amount: Double): String =
    "$" + NumberFormat.getIntegerInstance(colombia).format(amount.toLong())

/** Mensaje para cuando falla una escritura de metas. */
@StringRes
fun GoalWriteResult.Reason.messageRes(): Int = when (this) {
    GoalWriteResult.Reason.PermissionDenied -> R.string.goals_err_permission
    GoalWriteResult.Reason.NoSession -> R.string.profile_err_no_session
    GoalWriteResult.Reason.ImageFailed -> R.string.goals_err_image
    GoalWriteResult.Reason.Unknown -> R.string.error_server_body
}

/**
 * Marco de las pantallas de Metas: fondo claro con nubes y barra superior con la flecha de
 * volver, "FinMojarra" al centro y la foto de perfil a la derecha (abre el perfil).
 * [floatingAction] se dibuja flotando abajo a la derecha (el botón "+" de la lista).
 */
@Composable
fun GoalsScaffold(
    onBack: (() -> Unit)?,
    onOpenProfile: () -> Unit,
    photoPath: String,
    photoStamp: Long,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    floatingAction: (@Composable BoxScope.() -> Unit)? = null,
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
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .height(48.dp)
            ) {
                // Sin flecha en el inicio (Ahorros): ahí no hay pantalla a la que volver.
                if (onBack != null) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 12.dp)
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
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleMedium,
                    color = FinInk,
                    modifier = Modifier.align(Alignment.Center)
                )
                ProfileAvatar(
                    photoPath = photoPath,
                    photoStamp = photoStamp,
                    size = 44.dp,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 20.dp)
                        .clip(CircleShape)
                        .clickable(role = Role.Button, onClick = onOpenProfile)
                )
            }
            content()
        }

        floatingAction?.invoke(this)

        if (isLoading) LoadingOverlay()
    }
}

enum class GoalsTab { Savings, Goals, Learn }

/**
 * Pestañas de arriba: "Ahorros | Metas | Vamos a aprender". La pestaña [selected] se ve
 * negra; las otras llevan a su módulo si tienen callback (si es null no hacen nada).
 */
@Composable
fun GoalsTabs(
    selected: GoalsTab,
    modifier: Modifier = Modifier,
    onSavings: (() -> Unit)? = null,
    onGoals: (() -> Unit)? = null,
    onLearn: (() -> Unit)? = null
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TabPill(
            text = stringResource(R.string.goals_tab_savings),
            selected = selected == GoalsTab.Savings,
            onClick = onSavings
        )
        TabPill(
            text = stringResource(R.string.goals_tab_goals),
            selected = selected == GoalsTab.Goals,
            onClick = onGoals
        )
        TabPill(
            text = stringResource(R.string.goals_tab_learn),
            selected = selected == GoalsTab.Learn,
            onClick = onLearn
        )
    }
}

@Composable
private fun TabPill(text: String, selected: Boolean, onClick: (() -> Unit)?) {
    val shape = RoundedCornerShape(50)
    val click = if (onClick != null && !selected) {
        Modifier.clickable(role = Role.Tab, onClick = onClick)
    } else {
        Modifier
    }
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
        color = if (selected) FinWhite else FinInk,
        modifier = Modifier
            .clip(shape)
            .background(if (selected) FinInk else FinWhite)
            .border(1.dp, if (selected) FinInk else FinFieldBorder, shape)
            .then(click)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}

/** Círculo teal con el icono de la meta. */
@Composable
fun GoalBadgeIcon(modifier: Modifier = Modifier, size: Dp = 32.dp) {
    Box(
        modifier = modifier
            .size(size)
            .background(FinTeal, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_flag),
            contentDescription = null,
            tint = FinWhite,
            modifier = Modifier.size(size * 0.6f)
        )
    }
}

/** Etiqueta teal "Meta" de la esquina de cada tarjeta. */
@Composable
fun GoalChip(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.goals_badge),
        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
        color = FinWhite,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(FinTeal)
            .padding(horizontal = 12.dp, vertical = 3.dp)
    )
}

/** "Ahorro actual  $320.000.000" y la barra de progreso debajo. */
@Composable
fun GoalProgress(saved: Double, progress: Float, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.goals_current_saving),
                style = MaterialTheme.typography.bodySmall,
                color = FinMutedText
            )
            Text(
                text = formatMoney(saved),
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                color = FinTeal
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = FinTeal,
            trackColor = FinFieldBorder,
            gapSize = 0.dp,
            drawStopIndicator = {}
        )
    }
}

/** "Falta para alcanzar $130.000.000" a la izquierda y "Meta $450.000.000" a la derecha. */
@Composable
fun GoalStats(remaining: Double, target: Double, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column {
            Text(
                text = stringResource(R.string.goals_remaining),
                style = MaterialTheme.typography.bodySmall,
                color = FinMutedText
            )
            Text(
                text = formatMoney(remaining),
                style = MaterialTheme.typography.titleMedium,
                color = FinInk
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = stringResource(R.string.goals_target),
                style = MaterialTheme.typography.bodySmall,
                color = FinMutedText
            )
            Text(
                text = formatMoney(target),
                style = MaterialTheme.typography.titleMedium,
                color = FinTeal
            )
        }
    }
}

/** Imagen de una meta guardada en Firestore (Base64). Se decodifica fuera del hilo principal. */
@Composable
fun GoalImage(base64: String, modifier: Modifier = Modifier) {
    val bitmap by produceState<ImageBitmap?>(null, base64) {
        value = withContext(Dispatchers.Default) {
            try {
                val bytes = Base64.decode(base64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        }
    }
    FramedImage(bitmap, modifier)
}

/** Vista previa de la imagen que el usuario acaba de elegir en la galería. */
@Composable
fun GoalPickedImage(uri: Uri, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bitmap by produceState<ImageBitmap?>(null, uri) {
        value = withContext(Dispatchers.IO) {
            try {
                GoalImageCodec.decode(context, uri, maxSide = 900)?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        }
    }
    FramedImage(bitmap, modifier)
}

@Composable
private fun FramedImage(bitmap: ImageBitmap?, modifier: Modifier) {
    val shape = RoundedCornerShape(8.dp)
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = stringResource(R.string.goals_image_description),
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(shape)
        )
    } else {
        Box(modifier = modifier.clip(shape).background(FinFieldBorder))
    }
}
