package com.kveld9.fcmetrix.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun NumField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    gold: Boolean = false,
    width: Dp? = null,
    maxDigits: Int = 3,
    onFocusChanged: (Boolean) -> Unit = {},
    imeAction: ImeAction = ImeAction.Next,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    focusRequester: FocusRequester = remember { FocusRequester() },
    testTag: String = ""
) {
    val themeColor = if (gold) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
    var isFocused by remember { mutableStateOf(false) }
    val shakeOffset = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    val triggerShake = {
        scope.launch {
            shakeOffset.snapTo(0f)
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 280
                    val smooth = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
                    -6f at 70 using smooth
                    6f at 140 using smooth
                    -3f at 210 using smooth
                    0f at 280
                }
            )
        }
    }

    Column(
        modifier = if (width != null) modifier.width(width) else modifier,
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        BasicTextField(
            value = value,
            onValueChange = { raw ->
                val digits = raw.filter { it.isDigit() }
                if (digits.length > maxDigits) {
                    triggerShake()
                } else {
                    onValueChange(digits)
                }
            },
            textStyle = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = if (isFocused) themeColor else MaterialTheme.colorScheme.onSurface,
                fontFamily = FontFamily.Monospace,
                fontFeatureSettings = "tnum"
            ),
            cursorBrush = SolidColor(themeColor),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = imeAction
            ),
            keyboardActions = keyboardActions,
            modifier = Modifier
                .fillMaxWidth()
                .offset(x = shakeOffset.value.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .border(
                    width = 2.dp,
                    color = if (isFocused) themeColor else Color.Transparent,
                    shape = RoundedCornerShape(12.dp),
                )
                .padding(vertical = 10.dp)
                .testTag(testTag)
                .focusRequester(focusRequester)
                .onFocusChanged { focusState ->
                    isFocused = focusState.isFocused
                    onFocusChanged(focusState.isFocused)
                },
        )
    }
}
