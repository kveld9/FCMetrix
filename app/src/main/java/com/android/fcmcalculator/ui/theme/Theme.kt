package com.android.fcmcalculator.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val FcmScheme = darkColorScheme(
    primary = Green,
    onPrimary = Color(0xFF021007),
    primaryContainer = GreenDim,
    onPrimaryContainer = TextPrimary,
    secondary = Green,
    onSecondary = Color(0xFF021007),
    secondaryContainer = Color(0xFF1A3326),
    onSecondaryContainer = Color(0xFFEAF3ED),
    background = Bg,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = Surface2,
    onSurfaceVariant = TextDim,
    outline = Line,
    outlineVariant = Color(0xFF2A332E),
    error = Danger,
    onError = Color(0xFF021007),
)

@Composable
fun FcmTheme(
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            dynamicDarkColorScheme(context)
        }
        else -> FcmScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}