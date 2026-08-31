package com.kveld9.fcmetrix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.profileinstaller.ProfileInstaller
import com.kveld9.fcmetrix.data.LineupRepository
import com.kveld9.fcmetrix.data.ThemePreferences
import com.kveld9.fcmetrix.data.ThemeSettings
import com.kveld9.fcmetrix.data.local.LineupDatabase
import com.kveld9.fcmetrix.ui.GrlScreen
import com.kveld9.fcmetrix.ui.screens.SettingsScreen
import com.kveld9.fcmetrix.ui.theme.FcmTheme
import com.kveld9.fcmetrix.ui.viewmodel.GrlViewModel
import com.kveld9.fcmetrix.ui.viewmodel.GrlViewModelFactory
import com.kveld9.fcmetrix.ui.viewmodel.SettingsViewModel
import com.kveld9.fcmetrix.ui.viewmodel.SettingsViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        ProfileInstaller.writeProfile(this)

        val themePreferences = ThemePreferences(this)
        val database = LineupDatabase.getDatabase(this)
        val repository = LineupRepository(database.lineupDao())
        val grlFactory = GrlViewModelFactory(repository)
        val settingsFactory = SettingsViewModelFactory(themePreferences, repository)

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel(factory = settingsFactory)
            val grlViewModel: GrlViewModel = viewModel(factory = grlFactory)
            val themeSettings by settingsViewModel.themeSettings.collectAsState()

            val isSystemDark = isSystemInDarkTheme()
            val isDarkTheme = when (themeSettings.themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemDark
            }

            var showSettings by remember { mutableStateOf(false) }

            BackHandler(enabled = showSettings) {
                showSettings = false
            }

            val smoothOut = CubicBezierEasing(0.22f, 1.0f, 0.36f, 1.0f)

            FcmTheme(
                darkTheme = isDarkTheme,
                dynamicColor = themeSettings.dynamicColor,
                amoledBlack = themeSettings.amoledBlack
            ) {
                AnimatedContent(
                    targetState = showSettings,
                    transitionSpec = {
                        if (targetState) {
                            (slideInHorizontally(
                                animationSpec = tween(300, easing = smoothOut),
                                initialOffsetX = { it }
                            ) + fadeIn(animationSpec = tween(200))).togetherWith(
                                slideOutHorizontally(
                                    animationSpec = tween(300, easing = smoothOut),
                                    targetOffsetX = { -it / 4 }
                                ) + fadeOut(animationSpec = tween(150))
                            )
                        } else {
                            (slideInHorizontally(
                                animationSpec = tween(300, easing = smoothOut),
                                initialOffsetX = { -it / 4 }
                            ) + fadeIn(animationSpec = tween(200))).togetherWith(
                                slideOutHorizontally(
                                    animationSpec = tween(300, easing = smoothOut),
                                    targetOffsetX = { it }
                                ) + fadeOut(animationSpec = tween(150))
                            )
                        }
                    },
                    label = "screen_nav_transition"
                ) { inSettings ->
                    if (inSettings) {
                        SettingsScreen(
                            viewModel = settingsViewModel,
                            onNavigateBack = { showSettings = false }
                        )
                    } else {
                        GrlScreen(
                            viewModel = grlViewModel,
                            onOpenSettings = { showSettings = true },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}
