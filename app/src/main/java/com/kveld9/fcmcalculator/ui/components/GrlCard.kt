package com.kveld9.fcmcalculator.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kveld9.fcmcalculator.R
import com.kveld9.fcmcalculator.domain.GrlCalculator
import androidx.compose.ui.tooling.preview.Preview
import com.kveld9.fcmcalculator.ui.theme.FcmTheme

@Composable
fun GrlCard(result: GrlCalculator.Result) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
            .padding(horizontal = 24.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.grl_global),
                fontSize = 11.sp,
                letterSpacing = 1.2.sp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = result.grlGlobal?.toString() ?: "--",
                style = TextStyle(
                    fontSize = 110.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    lineHeight = 110.sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(vertical = 0.dp),
            )

            if (result.promedioBase != null && result.promedioRango != null) {
                DetailRow(base = result.promedioBase, rank = result.promedioRango)
            }

            GrlHint(result)
        }
    }
}

@Composable
private fun DetailRow(base: Double, rank: Double) {
    val colorOnSurface = MaterialTheme.colorScheme.onSurface
    val labelText = stringResource(R.string.base_avg_label)
    val rankText = stringResource(R.string.rank_avg_label)
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.padding(bottom = 6.dp)
    ) {
        val styleLabel = SpanStyle(
            color = colorOnSurface.copy(alpha = 0.5f),
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp
        )
        val styleValue = SpanStyle(
            color = colorOnSurface.copy(alpha = 0.8f),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )

        Text(
            text = buildAnnotatedString {
                withStyle(styleLabel) { append("$labelText ") }
                withStyle(styleValue) { append(String.format(java.util.Locale.US, "%.2f", base)) }
                withStyle(styleLabel) { append("  •  ") }
                withStyle(styleLabel) { append("$rankText ") }
                withStyle(styleValue) { append(String.format(java.util.Locale.US, "%.2f", rank)) }
            }
        )
    }
}

@Composable
private fun GrlHint(result: GrlCalculator.Result) {
    if (result.grlGlobal == null) {
        val faltan = result.faltantes
        val fullText = stringResource(R.string.missing_players, faltan).uppercase()
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

        Text(
            text = annotatedString,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 0.dp)
        )
        return
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(top = 12.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val pBase = result.puntosGrl
        val pRango = result.puntosRango
        val esRangoMejor = result.esMejoraPorRango

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
            .padding(vertical = 12.dp, horizontal = 4.dp),
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
                )
            )
        }
    }
}
