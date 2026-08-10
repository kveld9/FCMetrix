package com.android.fcmcalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.android.fcmcalculator.data.ThemePreferences
import com.android.fcmcalculator.ui.GrlScreen
import com.android.fcmcalculator.ui.theme.FcmTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val themePreferences = ThemePreferences(this)

        setContent {
            val dynamicColor by themePreferences.dynamicColorEnabled.collectAsState(initial = false)
            val scope = rememberCoroutineScope()

            FcmTheme(dynamicColor = dynamicColor) {
                GrlScreen(
                    modifier = Modifier.fillMaxSize(),
                    dynamicColor = dynamicColor,
                    onDynamicColorChange = { enabled ->
                        scope.launch {
                            themePreferences.setDynamicColorEnabled(enabled)
                        }
                    }
                )
            }
        }
    }
}