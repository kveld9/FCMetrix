package com.kveld9.fcmetrix.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kveld9.fcmetrix.R
import com.kveld9.fcmetrix.domain.GrlCalculator
import androidx.compose.ui.tooling.preview.Preview
import com.kveld9.fcmetrix.ui.theme.FcmTheme
import kotlinx.coroutines.launch

@Composable
fun GrlCard(
    result: GrlCalculator.Result,
    teamName: String? = null,
    onCapture: (android.graphics.Bitmap) -> Unit = {}
) {
    val graphicsLayer = rememberGraphicsLayer()
    val coroutineScope = rememberCoroutineScope()

    val totalDesc = if (result.grlGlobal != null) {
        stringResource(
            R.string.desc_grl_card_complete,
            result.grlGlobal,
            String.format(java.util.Locale.US, "%.2f", result.promedioBase ?: 0.0),
            String.format(java.util.Locale.US, "%.2f", result.promedioRango ?: 0.0)
        )
    } else {
        stringResource(R.string.desc_grl_card_incomplete, result.faltantes)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        // CAPTURE AREA: Only this internal Box is recorded
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawWithCache {
                    onDrawWithContent {
                        graphicsLayer.record {
                            this@onDrawWithContent.drawContent()
                        }
                        drawLayer(graphicsLayer)
                    }
                }
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .semantics(mergeDescendants = true) {
                    contentDescription = totalDesc
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (!teamName.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                CircleShape
                            )
                            .padding(horizontal = 14.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = teamName.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.2.sp,
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.grl_global),
                    fontSize = 11.sp,
                    letterSpacing = 1.2.sp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                    fontWeight = FontWeight.SemiBold,
                )

                if (result.grlGlobal != null) {
                    CompleteSquadResult(result)
                } else {
                    IncompleteSquadTracker(titularesCargados = result.titularesCargados)
                }

                GrlHint(result)
            }
        }

        // UI ONLY: The camera button is outside the capture area
        if (result.grlGlobal != null) {
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                        onCapture(bitmap)
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = stringResource(R.string.desc_share),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun CompleteSquadResult(result: GrlCalculator.Result) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.height(156.dp)
    ) {
        Text(
            text = (result.grlGlobal ?: "--").toString(),
            style = TextStyle(
                fontSize = 100.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                lineHeight = 100.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.testTag("grl_global_value")
        )
        DetailRow(base = result.promedioBase, rank = result.promedioRango)
    }
}

@Composable
private fun IncompleteSquadTracker(titularesCargados: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.height(156.dp)
    ) {
        // 11 Orbes tácticos representando la alineación
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            (0 until 11).forEach { index ->
                val isFilled = index < titularesCargados
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(
                            if (isFilled) MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        )
                        .border(
                            width = 1.dp,
                            color = if (isFilled) MaterialTheme.colorScheme.primary 
                                    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                            shape = CircleShape
                        )
                )
            }
        }

        Text(
            text = stringResource(R.string.players_ready_count, titularesCargados),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun DetailRow(base: Double?, rank: Double?) {
    val colorOnSurface = MaterialTheme.colorScheme.onSurface
    val labelText = stringResource(R.string.base_avg_label)
    val rankText = stringResource(R.string.rank_avg_label)
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.padding(bottom = 6.dp).clearAndSetSemantics { }
    ) {
        val styleLabel = SpanStyle(
            color = colorOnSurface.copy(alpha = 0.5f),
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp
        )
        val styleValue = SpanStyle(
            color = if (base != null) colorOnSurface.copy(alpha = 0.8f) else colorOnSurface.copy(alpha = 0.2f),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )

        Text(
            text = buildAnnotatedString {
                withStyle(styleLabel) { append("$labelText ") }
                withStyle(styleValue) { 
                    append(if (base != null) String.format(java.util.Locale.US, "%.2f", base) else "--")
                }
                withStyle(styleLabel) { append("  •  ") }
                withStyle(styleLabel) { append("$rankText ") }
                withStyle(styleValue) { 
                    append(if (rank != null) String.format(java.util.Locale.US, "%.2f", rank) else "--")
                }
            }
        )
    }
}

@Composable
private fun GrlHint(result: GrlCalculator.Result) {
    if (result.grlGlobal == null) {
        val faltan = result.faltantes
        val fullText = stringResource(R.string.missing_players_to_calc, faltan).uppercase()
        val numberString = faltan.toString()
        val startIndex = fullText.indexOf(numberString)

        val annotatedString = buildAnnotatedString {
            if (startIndex != -1) {
                append(fullText.substring(0, startIndex))
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                    append(numberString)
                }
                append(fullText.substring(startIndex + numberString.length))
            } else {
                append(fullText)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 12.dp)
                .height(68.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.06f))
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = annotatedString,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("grl_missing_hint")
            )
        }
        return
    }

    val pBase = result.puntosGrl
    val pRango = result.puntosRango
    val esRangoMejor = result.esMejoraPorRango

    val hintDesc = if (esRangoMejor) {
        stringResource(R.string.desc_recommended_rank, pRango ?: 0)
    } else {
        stringResource(R.string.desc_recommended_base, pBase ?: 0)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 12.dp)
            .height(68.dp)
            .semantics { contentDescription = hintDesc },
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PathColumn(
            modifier = Modifier.weight(1f),
            title = stringResource(R.string.rank_path),
            pointsText = when {
                pRango != null -> stringResource(R.string.plus_rank_points, pRango)
                result.rangoMaximo -> "MAX"
                else -> "--"
            },
            isFastest = esRangoMejor && pRango != null,
            isCompleted = result.rangoMaximo
        )

        PathColumn(
            modifier = Modifier.weight(1f),
            title = stringResource(R.string.base_path),
            pointsText = if (pBase != null) stringResource(R.string.plus_base_points, pBase) else "--",
            isFastest = !esRangoMejor && pBase != null,
            isCompleted = false
        )
    }
}

@Composable
private fun PathColumn(
    modifier: Modifier,
    title: String,
    pointsText: String,
    isFastest: Boolean,
    isCompleted: Boolean = false
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f))
            .border(
                1.dp,
                if (isFastest) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) 
                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f),
                RoundedCornerShape(12.dp)
            )
            .padding(vertical = 8.dp, horizontal = 4.dp)
            .clearAndSetSemantics { },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        
        Text(
            text = pointsText,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            color = when {
                isCompleted -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                isFastest -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            },
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 2.dp)
        )

        if (isFastest) {
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = stringResource(R.string.fastest_label),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GrlCardPreview() {
    FcmTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            GrlCard(
                result = GrlCalculator.Result(
                    grlGlobal = 126,
                    titularesCargados = 11,
                    faltantes = 0,
                    puntosGrl = 4,
                    puntosRango = null,
                    esMejoraPorRango = false,
                    rangoMaximo = true,
                    promedioBase = 120.73,
                    promedioRango = 5.0
                ),
                teamName = "Ultimate Team 2026",
                onCapture = {}
            )
        }
    }
}
