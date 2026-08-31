package com.kveld9.fcmetrix.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Green,
    onPrimary = Color(0xFF021007),
    primaryContainer = GreenDim,
    onPrimaryContainer = TextPrimary,
    secondary = Green,
    onSecondary = Color(0xFF021007),
    secondaryContainer = Color(0xFF1A3326),
    onSecondaryContainer = Color(0xFFEAF3ED),
    tertiary = Gold,
    onTertiary = Color(0xFF241A00),
    tertiaryContainer = GoldDim,
    onTertiaryContainer = Color(0xFFFFE088),
    background = Bg,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = Surface2,
    onSurfaceVariant = TextDim,
    surfaceDim = Color(0xFF090D0B),
    surfaceBright = Color(0xFF1D2421),
    surfaceContainerLowest = Bg,
    surfaceContainerLow = Color(0xFF0E1311),
    surfaceContainer = Surface,
    surfaceContainerHigh = Surface2,
    surfaceContainerHighest = Color(0xFF222925),
    outline = Line,
    outlineVariant = Color(0xFF2A332E),
    error = Danger,
    onError = Color(0xFF370001),
    errorContainer = Color(0xFF3A1F1A),
    onErrorContainer = Color(0xFFFFDAD4),
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF006D38),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF76FCA2),
    onPrimaryContainer = Color(0xFF00210D),
    secondary = Color(0xFF006D38),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD4E8D7),
    onSecondaryContainer = Color(0xFF0E1F14),
    tertiary = Color(0xFF755B00),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFE088),
    onTertiaryContainer = Color(0xFF241A00),
    background = Color(0xFFF7FBF4),
    onBackground = Color(0xFF181D19),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF181D19),
    surfaceVariant = Color(0xFFDCE5DD),
    onSurfaceVariant = Color(0xFF404943),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF1F5EE),
    surfaceContainer = Color(0xFFEBEFE8),
    surfaceContainerHigh = Color(0xFFE5EAE3),
    surfaceContainerHighest = Color(0xFFDFE4DD),
    outline = Color(0xFF717973),
    outlineVariant = Color(0xFFC0C9C1),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

@Composable
fun FcmTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    amoledBlack: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val baseColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val colorScheme = if (darkTheme && amoledBlack) {
        baseColorScheme.copy(
            background = Color.Black,
            surface = Color.Black,
            surfaceContainer = Color(0xFF121212),
            surfaceContainerLow = Color(0xFF0A0A0A),
            surfaceContainerHigh = Color(0xFF1A1A1A),
            surfaceContainerHighest = Color(0xFF242424)
        )
    } else {
        baseColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode && view.context is Activity) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}