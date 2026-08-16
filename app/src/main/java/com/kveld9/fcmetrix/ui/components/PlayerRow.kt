package com.kveld9.fcmetrix.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kveld9.fcmetrix.R
import kotlinx.coroutines.launch

@Composable
fun PlayerRow(
    numero: Int,
    grl: String,
    rango: String,
    onGrlChange: (String) -> Unit,
    onRangoChange: (String) -> Unit,
    onGrlFocusLost: (String) -> Unit,
    onRangoFocusLost: (String) -> Unit,
    modifier: Modifier = Modifier,
    isSuplente: Boolean = false,
    onRemove: (() -> Unit)? = null,
    scrollState: androidx.compose.foundation.ScrollState? = null,
    grlFocusRequester: FocusRequester = remember { FocusRequester() },
    rangoFocusRequester: FocusRequester = remember { FocusRequester() },
    grlImeAction: ImeAction = ImeAction.Next,
    rangoImeAction: ImeAction = ImeAction.Next,
    grlKeyboardActions: KeyboardActions = KeyboardActions.Default,
    rangoKeyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val playerType = if (isSuplente) stringResource(R.string.suplentes) else stringResource(R.string.titulares)
    val playerDesc = stringResource(R.string.desc_player_row, playerType, numero, grl, rango)

    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()
    var isRowFocused by remember { mutableStateOf(false) }
    var grlFocused by remember { mutableStateOf(false) }
    var rangoFocused by remember { mutableStateOf(false) }

    fun checkRowFocus(isGrl: Boolean, focused: Boolean) {
        if (isGrl) grlFocused = focused else rangoFocused = focused
        val nowFocused = grlFocused || rangoFocused
        if (nowFocused && !isRowFocused) {
            coroutineScope.launch {
                bringIntoViewRequester.bringIntoView()
            }
        }
        isRowFocused = nowFocused
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .bringIntoViewRequester(bringIntoViewRequester)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(12.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = playerDesc
            },
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
                checkRowFocus(isGrl = true, focused = focused)
                if (!focused) {
                    onGrlFocusLost(grl)
                }
            },
            label = stringResource(R.string.grl_label),
            modifier = Modifier.weight(1f),
            imeAction = grlImeAction,
            keyboardActions = grlKeyboardActions,
            focusRequester = grlFocusRequester,
            testTag = "grl_input_$numero"
        )
        NumField(
            value = rango,
            onValueChange = onRangoChange,
            onFocusChanged = { focused ->
                checkRowFocus(isGrl = false, focused = focused)
                if (!focused) {
                    onRangoFocusLost(rango)
                }
            },
            label = stringResource(R.string.rank_label),
            gold = true,
            width = 70.dp,
            maxDigits = 1,
            imeAction = rangoImeAction,
            keyboardActions = rangoKeyboardActions,
            focusRequester = rangoFocusRequester,
            testTag = "rank_input_$numero"
        )
        if (isSuplente && onRemove != null) {
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .minimumInteractiveComponentSize() // Asegura 48dp de target
                    .size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.desc_remove_player, numero),
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
