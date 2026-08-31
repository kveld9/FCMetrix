package com.kveld9.fcmetrix.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class ThemePreferencesTest {

    @get:Rule
    val tmpFolder = TemporaryFolder()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var themePreferences: ThemePreferences

    @Before
    fun setup() {
        dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { File(tmpFolder.newFolder(), "test_settings.preferences_pb") }
        )
        themePreferences = ThemePreferences(dataStore)
    }

    @Test
    fun `default theme settings - returns expected defaults`() = runTest(testDispatcher) {
        val settings = themePreferences.themeSettings.first()
        assertTrue(settings.dynamicColor)
        assertEquals("SYSTEM", settings.themeMode)
        assertFalse(settings.amoledBlack)
    }

    @Test
    fun `setDynamicColor - updates dynamic color preference`() = runTest(testDispatcher) {
        themePreferences.setDynamicColor(false)
        assertFalse(themePreferences.themeSettings.first().dynamicColor)

        themePreferences.setDynamicColor(true)
        assertTrue(themePreferences.themeSettings.first().dynamicColor)
    }

    @Test
    fun `setThemeMode - updates theme mode string preference`() = runTest(testDispatcher) {
        themePreferences.setThemeMode("DARK")
        assertEquals("DARK", themePreferences.themeSettings.first().themeMode)

        themePreferences.setThemeMode("LIGHT")
        assertEquals("LIGHT", themePreferences.themeSettings.first().themeMode)
    }

    @Test
    fun `setAmoledBlack - updates amoled black preference`() = runTest(testDispatcher) {
        themePreferences.setAmoledBlack(true)
        assertTrue(themePreferences.themeSettings.first().amoledBlack)

        themePreferences.setAmoledBlack(false)
        assertFalse(themePreferences.themeSettings.first().amoledBlack)
    }
}
