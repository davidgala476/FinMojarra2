package com.finmojarra.app.ui.profile

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finmojarra.app.R
import com.finmojarra.app.ui.components.CloudDecoration
import com.finmojarra.app.ui.components.LoadingOverlay
import com.finmojarra.app.ui.theme.FinBackground
import com.finmojarra.app.ui.theme.FinError
import com.finmojarra.app.ui.theme.FinFieldBorder
import com.finmojarra.app.ui.theme.FinInk
import com.finmojarra.app.ui.theme.FinMutedText
import com.finmojarra.app.ui.theme.FinPlaceholder
import com.finmojarra.app.ui.theme.FinSlate900
import com.finmojarra.app.ui.theme.FinTeal
import com.finmojarra.app.ui.theme.FinWhite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val CardShape = RoundedCornerShape(8.dp)

/**
 * Marco de las pantallas del perfil: fondo claro, nubes, barra superior con flecha de volver
 * y el título "FinMojarra" centrado, y el contenido con scroll.
 */
@Composable
fun ProfileScaffold(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
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
            // Barra superior
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .height(48.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 20.dp)
                        .size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_chevron_left),
                        contentDescription = stringResource(R.string.action_back),
                        tint = FinSlate900,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleMedium,
                    color = FinInk,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 40.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                content = content
            )
        }

        if (isLoading) {
            LoadingOverlay()
        }
    }
}

/** Foto de perfil circular. Si no hay foto muestra un icono de persona sobre fondo teal. */
@Composable
fun ProfileAvatar(
    photoPath: String,
    photoStamp: Long,
    modifier: Modifier = Modifier,
    size: Dp = 140.dp
) {
    val bitmap by produceState<ImageBitmap?>(null, photoPath, photoStamp) {
        value = if (photoPath.isBlank()) {
            null
        } else {
            withContext(Dispatchers.IO) {
                try {
                    BitmapFactory.decodeFile(photoPath)?.asImageBitmap()
                } catch (e: Exception) {
                    null
                }
            }
        }
    }

    val photo = bitmap
    if (photo != null) {
        Image(
            bitmap = photo,
            contentDescription = stringResource(R.string.profile_photo_description),
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(size)
                .clip(CircleShape)
        )
    } else {
        Box(
            modifier = modifier
                .size(size)
                .background(FinTeal, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_person),
                contentDescription = stringResource(R.string.profile_photo_description),
                tint = FinWhite,
                modifier = Modifier.size(size * 0.6f)
            )
        }
    }
}

/** Tarjeta blanca con borde gris (la base de todos los apartados del perfil). */
@Composable
fun ProfileCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val clickable = if (onClick != null) {
        Modifier.clickable(role = Role.Button, onClick = onClick)
    } else {
        Modifier
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(FinWhite)
            .border(1.dp, FinFieldBorder, CardShape)
            .then(clickable)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        content = content
    )
}

/** Tarjeta de solo lectura: etiqueta en negrita y el valor debajo. Si tiene [onClick] es tocable. */
@Composable
fun ProfileInfoCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    labelColor: Color = FinInk,
    valueColor: Color = FinInk,
    onClick: (() -> Unit)? = null
) {
    ProfileCard(modifier = modifier, onClick = onClick) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
            color = labelColor
        )
        val empty = value.isBlank()
        Text(
            text = if (empty) placeholder else value,
            style = MaterialTheme.typography.bodyMedium,
            color = if (empty) FinMutedText else valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** Tarjeta con un campo de texto adentro (etiqueta + "Introduce tu ..."). */
@Composable
fun ProfileInputCard(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    errorText: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.Sentences,
    imeAction: ImeAction = ImeAction.Next,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CardShape)
                .background(FinWhite)
                .border(1.dp, if (errorText != null) FinError else FinFieldBorder, CardShape)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                color = FinInk
            )
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = FinInk),
                cursorBrush = SolidColor(FinTeal),
                keyboardOptions = KeyboardOptions(
                    keyboardType = keyboardType,
                    capitalization = capitalization,
                    imeAction = imeAction
                ),
                keyboardActions = keyboardActions,
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyMedium,
                                color = FinPlaceholder,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
        if (errorText != null) {
            Text(
                text = errorText,
                style = MaterialTheme.typography.bodySmall,
                color = FinError,
                modifier = Modifier.padding(start = 5.dp, top = 4.dp)
            )
        }
    }
}

/** Tarjeta que abre un menú para elegir una opción (por ejemplo, el tipo de vía). */
@Composable
fun ProfileDropdownCard(
    label: String,
    selected: String,
    options: List<String>,
    placeholder: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    errorText: String? = null
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CardShape)
                    .background(FinWhite)
                    .border(1.dp, if (errorText != null) FinError else FinFieldBorder, CardShape)
                    .clickable(role = Role.DropdownList) { expanded = true }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                    color = FinInk
                )
                Text(
                    text = (if (selected.isBlank()) placeholder else selected) + "  ›",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selected.isBlank()) FinPlaceholder else FinInk,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, style = MaterialTheme.typography.bodyMedium) },
                        onClick = {
                            onSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
        if (errorText != null) {
            Text(
                text = errorText,
                style = MaterialTheme.typography.bodySmall,
                color = FinError,
                modifier = Modifier.padding(start = 5.dp, top = 4.dp)
            )
        }
    }
}

/** Botón "Confirmar" del diseño: blanco, borde gris, texto negro. */
@Composable
fun ProfileConfirmButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .widthIn(min = 140.dp)
            .height(40.dp)
            .clip(CardShape)
            .background(FinWhite)
            .border(1.dp, FinFieldBorder, CardShape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (enabled) FinInk else FinPlaceholder
        )
    }
}

/** Línea divisoria con un punto al centro (separa "datos actuales" de "Cambiar datos"). */
@Composable
fun ProfileDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(FinFieldBorder)
        )
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(FinBackground, CircleShape)
                .border(1.dp, FinFieldBorder, CircleShape)
        )
    }
}

/** Texto pequeño "Cambiar datos" que va debajo del divisor. */
@Composable
fun ProfileSectionCaption(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = FinInk,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 5.dp)
    )
}

/** Título centrado bajo la foto ("Tu información", "Número celular", ...). */
@Composable
fun ProfileHeading(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = FinInk,
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth()
    )
}

/** Columna con el espaciado estándar entre tarjetas. */
@Composable
fun ProfileCardColumn(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        content = content
    )
}

/** Hueco vertical. */
@Composable
fun VSpace(height: Dp) {
    Spacer(modifier = Modifier.height(height))
}
