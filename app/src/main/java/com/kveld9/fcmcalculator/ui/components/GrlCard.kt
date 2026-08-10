package com.kveld9.fcmcalculator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.grl_global),
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
            text = stringResource(R.string.missing_players, faltan),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        PuntoHint(texto = stringResource(R.string.points_to_upgrade_grl), puntos = result.puntosGrl)
        if (result.rangoMaximo) {
            Text(
                text = stringResource(R.string.rank_max_reached),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        } else if (result.puntosRango != null) {
            PuntoHint(texto = stringResource(R.string.points_to_upgrade_rango), puntos = result.puntosRango)
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
        append(stringResource(R.string.points_suffix))
        append(texto)
    }
    Text(
        text = annotatedString,
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}
