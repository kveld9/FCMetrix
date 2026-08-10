package com.kveld9.fcmcalculator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kveld9.fcmcalculator.R
import kotlinx.coroutines.launch

@Composable
fun PlayerRow(
    numero: Int,
    grl: String,
    rango: String,
    onGrlChange: (String) -> Unit,
    onRangoChange: (String) -> Unit,
    onGrlFocusLost: (String) -> Unit,
    modifier: Modifier = Modifier,
    isSuplente: Boolean = false,
    onRemove: (() -> Unit)? = null,
    scrollState: androidx.compose.foundation.ScrollState,
) {
    val coroutineScope = rememberCoroutineScope()

    Row(
        modifier = modifier
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
            value = grl,
            onValueChange = onGrlChange,
            onFocusChanged = { focused ->
                if (focused) {
                    coroutineScope.launch {
                        scrollState.animateScrollTo(scrollState.value + 120)
                    }
                } else {
                    onGrlFocusLost(grl)
                }
            },
            label = stringResource(R.string.grl_label),
            modifier = Modifier.weight(1f),
        )
        NumField(
            value = rango,
            onValueChange = onRangoChange,
            onFocusChanged = { focused ->
                if (focused) {
                    coroutineScope.launch {
                        scrollState.animateScrollTo(scrollState.value + 120)
                    }
                }
            },
            label = stringResource(R.string.rank_label),
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
                    contentDescription = stringResource(R.string.remove),
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
