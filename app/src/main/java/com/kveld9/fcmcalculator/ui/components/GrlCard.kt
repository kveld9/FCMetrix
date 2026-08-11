package com.kveld9.fcmcalculator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shadow
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

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Mostrar la "mejor" mejora (la que requiere menos puntos)
        val mejorPuntos = result.puntosSiguienteGrl
        val esRango = result.esMejoraPorRango

        if (mejorPuntos != null) {
            val suffix = if (esRango) {
                stringResource(R.string.upgrade_ovr_rank_suffix)
            } else {
                stringResource(R.string.upgrade_ovr_base_suffix)
            }
            PuntoHint(texto = suffix, puntos = mejorPuntos)
        }

        if (result.rangoMaximo) {
            Text(
                text = stringResource(R.string.rank_max_reached),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun PuntoHint(texto: String, puntos: Int?) {
    if (puntos == null) return
    val annotatedString = buildAnnotatedString {
        append(stringResource(R.string.missing_points_prefix))
        withStyle(style = SpanStyle(
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )) {
            append(puntos.toString())
        }
        append(texto)
    }
    Text(
        text = annotatedString,
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
        textAlign = TextAlign.Center
    )
}
