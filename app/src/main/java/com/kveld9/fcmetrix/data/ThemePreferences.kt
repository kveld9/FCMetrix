package com.kveld9.fcmetrix.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class ThemeSettings(
    val dynamicColor: Boolean = true,
    val themeMode: String = "SYSTEM", // "SYSTEM", "DARK", "LIGHT"
    val amoledBlack: Boolean = false
)

class ThemePreferences(
    private val dataStore: DataStore<Preferences>
) {
    constructor(context: Context) : this(context.dataStore)

    companion object {
        val DYNAMIC_COLOR_KEY = booleanPreferencesKey("dynamic_color")
        val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        val AMOLED_BLACK_KEY = booleanPreferencesKey("amoled_black")
    }

    val themeSettings: Flow<ThemeSettings> = dataStore.data
        .map { preferences ->
            ThemeSettings(
                dynamicColor = preferences[DYNAMIC_COLOR_KEY] ?: true,
                themeMode = preferences[THEME_MODE_KEY] ?: "SYSTEM",
                amoledBlack = preferences[AMOLED_BLACK_KEY] ?: false
            )
        }

    suspend fun setDynamicColor(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[DYNAMIC_COLOR_KEY] = enabled
        }
    }

    suspend fun setThemeMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode
        }
    }

    suspend fun setAmoledBlack(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[AMOLED_BLACK_KEY] = enabled
        }
    }
}