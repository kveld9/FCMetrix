package com.kveld9.fcmetrix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.profileinstaller.ProfileInstaller
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kveld9.fcmetrix.data.LineupRepository
import com.kveld9.fcmetrix.data.ThemePreferences
import com.kveld9.fcmetrix.data.local.LineupDatabase
import com.kveld9.fcmetrix.ui.GrlScreen
import com.kveld9.fcmetrix.ui.theme.FcmTheme
import com.kveld9.fcmetrix.ui.viewmodel.GrlViewModel
import com.kveld9.fcmetrix.ui.viewmodel.GrlViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        ProfileInstaller.writeProfile(this)

        val themePreferences = ThemePreferences(this)
        val database = LineupDatabase.getDatabase(this)
        val repository = LineupRepository(database.lineupDao())
        val factory = GrlViewModelFactory(repository)

        setContent {
            val dynamicColor by themePreferences.dynamicColorEnabled.collectAsState(initial = false)
            val scope = rememberCoroutineScope()
            val viewModel: GrlViewModel = viewModel(factory = factory)

            FcmTheme(dynamicColor = dynamicColor) {
                GrlScreen(
                    viewModel = viewModel,
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
