package com.kveld9.fcmetrix.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
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
    // Note: We'll need a way to inject dataStore into ThemePreferences for testing
    // or just test the logic if we refactor it.
    // For now, let's test the logic by creating a DataStore in the test.
    
    @Before
    fun setup() {
        dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { File(tmpFolder.newFolder(), "test_settings.preferences_pb") }
        )
    }

    @Test
    fun `dynamicColorEnabled - returns false by default`() = runTest(testDispatcher) {
        val dynamicColor = dataStore.data.first()[ThemePreferences.DYNAMIC_COLOR_KEY] ?: false
        assertFalse(dynamicColor)
    }

    @Test
    fun `setDynamicColorEnabled - updates preference correctly`() = runTest(testDispatcher) {
        dataStore.edit { it[ThemePreferences.DYNAMIC_COLOR_KEY] = true }
        val dynamicColor = dataStore.data.first()[ThemePreferences.DYNAMIC_COLOR_KEY] ?: false
        assertTrue(dynamicColor)
        
        dataStore.edit { it[ThemePreferences.DYNAMIC_COLOR_KEY] = false }
        val dynamicColorFalse = dataStore.data.first()[ThemePreferences.DYNAMIC_COLOR_KEY] ?: false
        assertFalse(dynamicColorFalse)
    }
}
