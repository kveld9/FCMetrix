package com.kveld9.fcmcalculator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kveld9.fcmcalculator.R
import com.kveld9.fcmcalculator.ui.model.PlayerData

@Composable
fun SectionHeader(title: String, badge: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
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
            style = MaterialTheme.typography.labelLarge.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp,
            )
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

@Composable
fun RangoQuickChips(onChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(start = 2.dp, bottom = 10.dp)
        ) {
            Text(
                text = stringResource(R.string.quick_rank),
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.quick_rank_hint),
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

@Composable
fun Actions(onAddSuplente: () -> Unit, onClearAll: () -> Unit) {
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
                text = stringResource(R.string.add_substitute),
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
        Box(
            modifier = Modifier
                .weight(0.6f)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable(onClick = onClearAll)
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.clear_all),
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
fun TitularesList(
    titulares: List<PlayerData>,
    onGrlChange: (String, String) -> Unit,
    onRangoChange: (String, String) -> Unit,
    onGrlFocusLost: (String, String) -> Unit,
    onRangoFocusLost: (String, String) -> Unit,
    scrollState: androidx.compose.foundation.ScrollState,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        titulares.forEachIndexed { index, player ->
            key(player.id) {
                PlayerRow(
                    numero = index + 1,
                    grl = player.grl,
                    rango = player.rango,
                    onGrlChange = { onGrlChange(player.id, it) },
                    onRangoChange = { onRangoChange(player.id, it) },
                    onGrlFocusLost = { onGrlFocusLost(player.id, it) },
                    onRangoFocusLost = { onRangoFocusLost(player.id, it) },
                    scrollState = scrollState,
                )
            }
        }
    }
}

@Composable
fun SuplentesList(
    suplentes: List<PlayerData>,
    onGrlChange: (String, String) -> Unit,
    onRangoChange: (String, String) -> Unit,
    onGrlFocusLost: (String, String) -> Unit,
    onRangoFocusLost: (String, String) -> Unit,
    onRemove: (String) -> Unit,
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
                text = stringResource(R.string.no_substitutes),
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
            key(player.id) {
                PlayerRow(
                    numero = index + 12,
                    grl = player.grl,
                    rango = player.rango,
                    onGrlChange = { onGrlChange(player.id, it) },
                    onRangoChange = { onRangoChange(player.id, it) },
                    onGrlFocusLost = { onGrlFocusLost(player.id, it) },
                    onRangoFocusLost = { onRangoFocusLost(player.id, it) },
                    isSuplente = true,
                    onRemove = { onRemove(player.id) },
                    scrollState = scrollState,
                )
            }
        }
    }
}
