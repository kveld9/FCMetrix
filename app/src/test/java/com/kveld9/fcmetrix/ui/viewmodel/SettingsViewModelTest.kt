package com.kveld9.fcmetrix.ui.viewmodel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.kveld9.fcmetrix.data.LineupRepository
import com.kveld9.fcmetrix.data.ThemePreferences
import com.kveld9.fcmetrix.data.backup.DuplicatePolicy
import com.kveld9.fcmetrix.data.backup.JsonBackupManager
import com.kveld9.fcmetrix.data.local.dao.LineupDao
import com.kveld9.fcmetrix.data.local.entity.TeamEntity
import com.kveld9.fcmetrix.domain.model.PlayerData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val tmpFolder = TemporaryFolder()

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var themePreferences: ThemePreferences
    private lateinit var fakeDao: FakeLineupDao
    private lateinit var repository: LineupRepository
    private lateinit var viewModel: SettingsViewModel

    class FakeLineupDao : LineupDao {
        val teams = mutableMapOf<String, TeamEntity>()
        private val _flow = MutableStateFlow<List<TeamEntity>>(emptyList())

        private fun updateFlow() {
            _flow.value = teams.values.toList()
        }

        override fun getAllTeams(): Flow<List<TeamEntity>> = _flow
        override suspend fun getAllTeamsSync(): List<TeamEntity> = teams.values.toList()
        override suspend fun getTeamById(id: String): TeamEntity? = teams[id]
        override suspend fun insertTeam(team: TeamEntity) {
            teams[team.id] = team
            updateFlow()
        }
        override suspend fun insertTeams(teams: List<TeamEntity>) {
            teams.forEach { this.teams[it.id] = it }
            updateFlow()
        }
        override suspend fun deleteTeam(id: String) {
            teams.remove(id)
            updateFlow()
        }
        override suspend fun deleteAllTeams() {
            teams.clear()
            updateFlow()
        }
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { File(tmpFolder.newFolder(), "settings.preferences_pb") }
        )
        themePreferences = ThemePreferences(dataStore)
        fakeDao = FakeLineupDao()
        repository = LineupRepository(fakeDao)
        viewModel = SettingsViewModel(
            themePreferences = themePreferences,
            repository = repository,
            backupManager = JsonBackupManager(),
            ioDispatcher = testDispatcher
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `setThemeMode - updates theme mode in state flow`() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.setThemeMode("DARK")
        advanceUntilIdle()
        assertEquals("DARK", viewModel.themeSettings.first().themeMode)
    }

    @Test
    fun `setDynamicColor - updates dynamic color preference`() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.setDynamicColor(false)
        advanceUntilIdle()
        assertFalse(viewModel.themeSettings.first().dynamicColor)
    }

    @Test
    fun `setAmoledBlack - updates amoled black preference`() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.setAmoledBlack(true)
        advanceUntilIdle()
        assertTrue(viewModel.themeSettings.first().amoledBlack)
    }

    @Test
    fun `exportBackup - with existing teams exports valid json and emits success`() = runTest(testDispatcher) {
        repository.saveTeam(
            id = "t1",
            name = "Team Alpha",
            titulares = listOf(PlayerData(grl = "100", rango = "3")),
            suplentes = emptyList()
        )
        advanceUntilIdle()

        val outputStream = ByteArrayOutputStream()
        viewModel.exportBackup(outputStream)
        advanceUntilIdle()

        val exportedJson = outputStream.toString(Charsets.UTF_8.name())
        assertTrue(exportedJson.contains("Team Alpha"))
        assertTrue(exportedJson.contains("FCMetrix"))
    }

    @Test
    fun `startImport - with duplicate teams prompts user with duplicate event`() = runTest(testDispatcher) {
        repository.saveTeam(
            id = "t1",
            name = "Team Alpha",
            titulares = listOf(PlayerData(grl = "100", rango = "3")),
            suplentes = emptyList()
        )
        advanceUntilIdle()

        val backupManager = JsonBackupManager()
        val jsonString = backupManager.exportToJson(
            listOf(
                TeamEntity(
                    id = "t1",
                    name = "Team Alpha",
                    titulares = listOf(PlayerData(grl = "105", rango = "4")),
                    suplentes = emptyList()
                )
            )
        )

        val inputStream = ByteArrayInputStream(jsonString.toByteArray(Charsets.UTF_8))
        viewModel.startImport(inputStream)
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.pendingImportTeams)
        assertEquals(1, viewModel.uiState.value.pendingImportTeams?.size)

        // Apply policy
        viewModel.applyImportPolicy(DuplicatePolicy.OVERWRITE_ALL)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.pendingImportTeams)
        val team = repository.getTeam("t1")
        assertEquals("105", team?.titulares?.first()?.grl)
    }

    @Test
    fun `deleteAllTeams - removes all teams from repository`() = runTest(testDispatcher) {
        repository.saveTeam("t1", "Team 1", emptyList(), emptyList())
        repository.saveTeam("t2", "Team 2", emptyList(), emptyList())
        advanceUntilIdle()
        assertEquals(2, repository.getAllTeamsSync().size)

        viewModel.deleteAllTeams()
        advanceUntilIdle()

        assertEquals(0, repository.getAllTeamsSync().size)
    }
}
