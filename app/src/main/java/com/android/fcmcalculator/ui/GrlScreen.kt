package com.android.fcmcalculator.ui

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Stable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.fcmcalculator.domain.GrlCalculator
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun GrlScreen(
    modifier: Modifier = Modifier,
    dynamicColor: Boolean = false,
    onDynamicColorChange: (Boolean) -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val titulares = remember { List(GrlCalculator.TITULARES) { PlayerUi() } }
    val suplentes = remember { mutableStateListOf<PlayerUi>() }

    val result by remember {
        derivedStateOf {
            GrlCalculator.calcular(
                titulares.map { it.toDomain() },
                suplentes.map { it.toDomain() },
            )
        }
    }

    val titularesCargados by remember {
        derivedStateOf { titulares.count { it.grl.isNotBlank() } }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
            .statusBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Header()

            GrlCard(result = result)

            ProgressSection(titulares = titulares)

            RangoQuickChips(
                onChange = { value ->
                    (titulares + suplentes).forEach { it.updateRango(value) }
                },
            )

            SectionHeader(title = "Titulares", badge = "$titularesCargados/11")
            TitularesList(titulares = titulares, scrollState = scrollState)

            SectionHeader(
                title = "Suplentes",
                badge = "${suplentes.size}/${GrlCalculator.SUPLENTES_MAX}",
            )
            SuplentesList(
                suplentes = suplentes,
                onRemove = { index -> suplentes.removeAt(index) },
                scrollState = scrollState,
            )

            Actions(
                onAddSuplente = {
                    if (suplentes.size < GrlCalculator.SUPLENTES_MAX) {
                        suplentes.add(PlayerUi())
                    }
                },
                onClearAll = {
                    titulares.forEach { it.reset() }
                    suplentes.clear()
                },
            )

            Footer()
        }

        // Toggle Monet
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
            ) {
                IconButton(
                    onClick = { onDynamicColorChange(!dynamicColor) },
                ) {
                    Icon(
                        imageVector = if (dynamicColor) Icons.Filled.Palette else Icons.Outlined.Palette,
                        contentDescription = "Dynamic Color",
                        tint = if (dynamicColor) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/* ---------- estado ---------- */

@Stable
class PlayerUi(grl: String = "", rango: String = "0") {
    var grl by mutableStateOf(grl)
    var rango by mutableStateOf(rango)

    fun toDomain() = GrlCalculator.Player(
        grl = grl.toDoubleOrNull(),
        rango = rango.toDoubleOrNull() ?: 0.0,
    )

    fun reset() {
        grl = ""
        rango = "0"
    }

    fun updateRango(newRango: String) {
        val g = grl.toDoubleOrNull()
        val r = rango.toDoubleOrNull() ?: 0.0
        rango = newRango
        if (g != null) {
            val base = g - r
            val nextR = newRango.toDoubleOrNull() ?: 0.0
            grl = (base + nextR).toInt().toString()
        }
    }
}

/* ---------- header ---------- */

@Composable
private fun Header() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(16.dp, CircleShape, clip = false)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "⚽", fontSize = 24.sp)
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Calculadora GRL",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "FC MOBILE • 2026",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            letterSpacing = 1.2.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun Spacer(modifier: Modifier) {
    androidx.compose.foundation.layout.Spacer(modifier)
}

/* ---------- card GRL ---------- */

@Composable
private fun GrlCard(result: GrlCalculator.Result) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "GRL GLOBAL",
                fontSize = 12.sp,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = result.grlGlobal?.toString() ?: "—",
                style = TextStyle(
                    fontSize = 80.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    lineHeight = 80.sp,
                    fontFamily = FontFamily.Monospace,
                    shadow = Shadow(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        blurRadius = 40f
                    )
                ),
                modifier = Modifier.padding(vertical = 12.dp),
            )
            GrlHint(result)
        }
    }
}

@Composable
private fun GrlHint(result: GrlCalculator.Result) {
    if (result.grlGlobal == null) {
        val faltan = result.faltantes
        Text(
            text = "Faltan $faltan jugadores",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        PuntoHint(texto = "de GRL para subir", puntos = result.puntosGrl)
        if (result.rangoMaximo) {
            Text(
                text = "✓ Rango máximo",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        } else if (result.puntosRango != null) {
            PuntoHint(texto = "de Rango para subir", puntos = result.puntosRango)
        }
    }
}

@Composable
private fun PuntoHint(texto: String, puntos: Int?) {
    if (puntos == null) return
    val annotatedString = buildAnnotatedString {
        append("Faltan ")
        withStyle(style = SpanStyle(
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )) {
            append(puntos.toString())
        }
        append(" pts $texto")
    }
    Text(
        text = annotatedString,
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}

/* ---------- progreso ---------- */

@Composable
private fun ProgressSection(titulares: List<PlayerUi>) {
    val cargados = titulares.count { it.grl.isNotBlank() }
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "EQUIPO",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "$cargados / 11",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(if (cargados == 0) 0f else cargados.toFloat() / 11)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

/* ---------- rango rápido ---------- */

@Composable
private fun RangoQuickChips(onChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 2.dp, bottom = 10.dp)
        ) {
            Text(
                text = "RANGO RÁPIDO",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = " (aplicar antes de los GRL)",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            (0..5).forEach { value ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .clickable { onChange(value.toString()) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = value.toString(),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

/* ---------- secciones ---------- */

@Composable
private fun SectionHeader(title: String, badge: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title.uppercase(),
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.2.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 10.dp, vertical = 2.dp)
        ) {
            Text(
                text = badge,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

/* ---------- listas ---------- */

@Composable
private fun TitularesList(
    titulares: List<PlayerUi>,
    scrollState: androidx.compose.foundation.ScrollState,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        titulares.forEachIndexed { index, player ->
            key(index) {
                PlayerRow(
                    numero = index + 1,
                    player = player,
                    isSuplente = false,
                    scrollState = scrollState,
                )
            }
        }
    }
}

@Composable
private fun SuplentesList(
    suplentes: SnapshotStateList<PlayerUi>,
    onRemove: (Int) -> Unit,
    scrollState: androidx.compose.foundation.ScrollState,
) {
    if (suplentes.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Sin suplentes",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            )
        }
        return
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        suplentes.forEachIndexed { index, player ->
            key(player) {
                PlayerRow(
                    numero = index + 12,
                    player = player,
                    isSuplente = true,
                    onRemove = { onRemove(index) },
                    scrollState = scrollState,
                )
            }
        }
    }
}

@Composable
private fun PlayerRow(
    numero: Int,
    player: PlayerUi,
    isSuplente: Boolean,
    onRemove: (() -> Unit)? = null,
    scrollState: androidx.compose.foundation.ScrollState,
) {
    val coroutineScope = rememberCoroutineScope()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = numero.toString(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        NumField(
            value = player.grl,
            onValueChange = { player.grl = it },
            onFocusChanged = { focused ->
                if (focused) {
                    coroutineScope.launch {
                        scrollState.animateScrollTo(scrollState.value + 120)
                    }
                }
            },
            label = "GRL",
            modifier = Modifier.weight(1f),
        )
        NumField(
            value = player.rango,
            onValueChange = { player.updateRango(it) },
            onFocusChanged = { focused ->
                if (focused) {
                    coroutineScope.launch {
                        scrollState.animateScrollTo(scrollState.value + 120)
                    }
                }
            },
            label = "Rango",
            gold = true,
            width = 70.dp,
        )
        if (isSuplente && onRemove != null) {
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/* ---------- campo numerico ---------- */

@Composable
private fun NumField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    gold: Boolean = false,
    width: androidx.compose.ui.unit.Dp? = null,
    onFocusChanged: (Boolean) -> Unit = {},
) {
    val themeColor = if (gold) Color(0xFFD4A843) else MaterialTheme.colorScheme.primary
    var isFocused by remember { mutableStateOf(false) }

    Column(
        modifier = if (width != null) modifier.width(width) else modifier,
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (isFocused) themeColor else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        BasicTextField(
            value = value,
            onValueChange = { raw ->
                val filtered = raw.filter { it.isDigit() }.take(3)
                onValueChange(filtered)
            },
            textStyle = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = if (isFocused) themeColor else MaterialTheme.colorScheme.onSurface,
                fontFamily = FontFamily.Monospace
            ),
            cursorBrush = SolidColor(themeColor),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .border(
                    width = 2.dp,
                    color = if (isFocused) themeColor else Color.Transparent,
                    shape = RoundedCornerShape(12.dp),
                )
                .padding(vertical = 10.dp)
                .onFocusChanged { focusState ->
                    isFocused = focusState.isFocused
                    onFocusChanged(focusState.isFocused)
                    if (!focusState.isFocused) {
                        val numericValue = value.toDoubleOrNull()
                        val clamped = when {
                            value.isBlank() -> ""
                            label == "GRL" -> {
                                val minVal = 47.0
                                val maxVal = 150.0
                                if (numericValue != null) {
                                    max(minVal, min(maxVal, numericValue)).toInt().toString()
                                } else ""
                            }
                            else -> {
                                val minVal = 0.0
                                val maxVal = 5.0
                                if (numericValue != null) {
                                    max(minVal, min(maxVal, numericValue)).toInt().toString()
                                } else ""
                            }
                        }
                        if (clamped != value) {
                            onValueChange(clamped)
                        }
                    }
                },
        )
    }
}

/* ---------- acciones ---------- */

@Composable
private fun Actions(onAddSuplente: () -> Unit, onClearAll: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primary)
                .clickable(onClick = onAddSuplente)
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "+ SUPLENTE",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
        Box(
            modifier = Modifier
                .weight(0.6f)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.errorContainer)
                .clickable(onClick = onClearAll)
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "BORRAR",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
        }
    }
}

/* ---------- footer ---------- */

@Composable
private fun Footer() {
    val uriHandler = LocalUriHandler.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp, bottom = 24.dp)
            .clickable { uriHandler.openUri("https://github.com/kveld9") },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "HECHO POR @KVELD9",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 2.sp
        )
    }
}