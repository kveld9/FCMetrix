package com.kveld9.fcmetrix.ui.viewmodel

import com.kveld9.fcmetrix.data.LineupRepository
import com.kveld9.fcmetrix.data.local.dao.LineupDao
import com.kveld9.fcmetrix.data.local.entity.TeamEntity
import com.kveld9.fcmetrix.domain.GrlCalculator
import com.kveld9.fcmetrix.domain.model.PlayerData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GrlViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: GrlViewModel
    private lateinit var repository: LineupRepository
    private lateinit var fakeDao: FakeLineupDao

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeDao = FakeLineupDao()
        repository = LineupRepository(fakeDao)
        viewModel = GrlViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // region Fake DAO
    class FakeLineupDao : LineupDao {
        private val teams = MutableStateFlow<Map<String, TeamEntity>>(emptyMap())

        override fun getAllTeams(): Flow<List<TeamEntity>> = MutableStateFlow(teams.value.values.toList())
        override suspend fun getAllTeamsSync(): List<TeamEntity> = teams.value.values.toList()
        override suspend fun getTeamById(id: String): TeamEntity? = teams.value[id]
        override suspend fun insertTeam(team: TeamEntity) {
            teams.value = teams.value + (team.id to team)
        }
        override suspend fun insertTeams(teams: List<TeamEntity>) {
            this.teams.value = this.teams.value + teams.associateBy { it.id }
        }
        override suspend fun deleteTeam(id: String) {
            teams.value = teams.value - id
        }
        override suspend fun deleteAllTeams() {
            teams.value = emptyMap()
        }
    }
    // endregion

    // region Inicialización y Reseteo
    @Test
    fun `initialState - 11 titulares vacios y resultado nulo`() {
        val state = viewModel.uiState.value
        assertEquals(GrlCalculator.TITULARES, state.titulares.size)
        assertTrue(state.suplentes.isEmpty())
        assertNull(state.result.grlGlobal)
        assertEquals(0, state.result.titularesCargados)
    }

    @Test
    fun `clearAll - resetea el estado completamente`() {
        val firstId = viewModel.uiState.value.titulares.first().id
        viewModel.onGrlChanged(firstId, "100")
        viewModel.addSubstitute()
        
        viewModel.clearAll()
        
        val state = viewModel.uiState.value
        assertTrue(state.titulares.all { it.grl.isEmpty() })
        assertTrue(state.suplentes.isEmpty())
        assertNull(state.result.grlGlobal)
    }
    // endregion

    // region Gestión de Titulares
    @Test
    fun `updateGrl - actualiza el jugador correcto y mantiene su ID`() {
        val initialPlayers = viewModel.uiState.value.titulares
        val targetId = initialPlayers[5].id
        
        viewModel.onGrlChanged(targetId, "110")
        
        val updatedPlayers = viewModel.uiState.value.titulares
        assertEquals("110", updatedPlayers[5].grl)
        assertEquals(targetId, updatedPlayers[5].id)
    }

    @Test
    fun `updateGrl - no afecta a otros jugadores`() {
        val players = viewModel.uiState.value.titulares
        val id0 = players[0].id
        val id1 = players[1].id
        
        viewModel.onGrlChanged(id0, "100")
        
        assertEquals("", viewModel.uiState.value.titulares[1].grl)
        assertEquals(id1, viewModel.uiState.value.titulares[1].id)
    }

    @Test
    fun `updateRange - ajusta el GRL visible sin corromper la base`() {
        val id = viewModel.uiState.value.titulares.first().id
        
        viewModel.onGrlChanged(id, "100")
        viewModel.onRangoChanged(id, "5")
        
        val updated = viewModel.uiState.value.titulares.first()
        assertEquals("5", updated.rango)
        assertEquals("105", updated.grl)
        
        viewModel.onRangoChanged(id, "2")
        assertEquals("102", viewModel.uiState.value.titulares.first().grl)
    }
    // endregion

    // region Gestión de Suplentes
    @Test
    fun `addSubstitute - permite agregar hasta 7 suplentes`() {
        repeat(7) { viewModel.addSubstitute() }
        assertEquals(7, viewModel.uiState.value.suplentes.size)
        
        viewModel.addSubstitute()
        assertEquals(7, viewModel.uiState.value.suplentes.size)
    }

    @Test
    fun `removeSubstitute - elimina por ID y actualiza el estado`() {
        viewModel.addSubstitute()
        val id = viewModel.uiState.value.suplentes.first().id
        
        viewModel.removeSubstitute(id)
        assertTrue(viewModel.uiState.value.suplentes.isEmpty())
    }
    // endregion

    // region Actualizaciones en Lote (Batch)
    @Test
    fun `updateAllRanks - afecta a titulares y suplentes manteniendo IDs`() {
        viewModel.addSubstitute()
        val titularId = viewModel.uiState.value.titulares.first().id
        val suplenteId = viewModel.uiState.value.suplentes.first().id
        
        viewModel.onGrlChanged(titularId, "100")
        viewModel.onGrlChanged(suplenteId, "90")
        
        viewModel.updateAllRanks("5")
        
        val state = viewModel.uiState.value
        assertEquals("105", state.titulares.first().grl)
        assertEquals(titularId, state.titulares.first().id)
        assertEquals("95", state.suplentes.first().grl)
        assertEquals(suplenteId, state.suplentes.first().id)
    }
    // endregion

    // region Sanitización e Integración de UI
    @Test
    fun `grlBounds - aplica limites de dominio al perder el foco`() {
        val id = viewModel.uiState.value.titulares.first().id
        
        viewModel.onGrlFocusLost(id, (GrlCalculator.GRL_MAX + 1).toString())
        assertEquals(GrlCalculator.GRL_MAX.toString(), viewModel.uiState.value.titulares.first().grl)
        
        viewModel.onGrlFocusLost(id, (GrlCalculator.GRL_MIN - 1).toString())
        assertEquals(GrlCalculator.GRL_MIN.toString(), viewModel.uiState.value.titulares.first().grl)
    }

    @Test
    fun `negativeValues - asegura que -1 se maneje correctamente segun UX`() {
        val id = viewModel.uiState.value.titulares.first().id
        
        viewModel.onGrlFocusLost(id, "-1")
        assertEquals(GrlCalculator.GRL_MIN.toString(), viewModel.uiState.value.titulares.first().grl)
        
        viewModel.onRangoFocusLost(id, "-1")
        assertEquals(GrlCalculator.RANGO_MIN.toString(), viewModel.uiState.value.titulares.first().rango)
    }

    @Test
    fun `emptyInput - permite vacio durante edicion pero sanea al perder foco`() {
        val id = viewModel.uiState.value.titulares.first().id
        
        viewModel.onRangoChanged(id, "")
        assertEquals("", viewModel.uiState.value.titulares.first().rango)
        
        viewModel.onRangoFocusLost(id, "")
        assertEquals(GrlCalculator.RANGO_MIN.toString(), viewModel.uiState.value.titulares.first().rango)
    }
    // endregion

    // region Reactividad y Secuencia
    @Test
    fun `incompleteLineup - expone grlGlobal null si faltan titulares`() {
        viewModel.onGrlChanged(viewModel.uiState.value.titulares[0].id, "100")
        assertNull(viewModel.uiState.value.result.grlGlobal)
    }

    @Test
    fun `complexFlow - secuencia de multiples eventos mantiene consistencia`() {
        val t0Id = viewModel.uiState.value.titulares[0].id
        
        viewModel.onGrlChanged(t0Id, "100")
        viewModel.onRangoChanged(t0Id, "5")
        viewModel.addSubstitute()
        viewModel.addSubstitute()
        val s0Id = viewModel.uiState.value.suplentes[0].id
        viewModel.onGrlChanged(s0Id, "100")
        val s1Id = viewModel.uiState.value.suplentes[1].id
        viewModel.removeSubstitute(s1Id)
        viewModel.updateAllRanks("3")
        
        val state = viewModel.uiState.value
        assertEquals("3", state.titulares[0].rango)
        assertEquals("103", state.titulares[0].grl)
        assertEquals(1, state.suplentes.size)
    }
    // endregion

    // region Persistencia
    @Test
    fun `saveTeam - no guarda si el nombre esta en blanco`() = runTest {
        viewModel.saveCurrentTeam("   ")
        val teams = fakeDao.getTeamById("any")
        assertNull(teams)
    }

    @Test
    fun `loadTeam - actualiza el estado con los datos del equipo cargado`() = runTest {
        val team = TeamEntity(
            id = "test_id",
            name = "Test Team",
            titulares = List(11) { PlayerData(grl = "110", rango = "5") },
            suplentes = emptyList()
        )
        
        viewModel.loadTeam(team)
        
        val state = viewModel.uiState.value
        assertEquals("Test Team", state.teamName)
        assertEquals("110", state.titulares.first().grl)
        assertEquals("5", state.titulares.first().rango)
        assertNotNull(state.result.grlGlobal)
    }

    @Test
    fun `saveTeam - actualiza el nombre del equipo en el estado actual`() = runTest {
        viewModel.saveCurrentTeam("My New Team")
        advanceUntilIdle()
        assertEquals("My New Team", viewModel.uiState.value.teamName)
    }

    @Test
    fun `deleteTeam - elimina el equipo del repositorio`() = runTest {
        viewModel.saveCurrentTeam("Equipo Temporal")
        advanceUntilIdle()
        val all = fakeDao.getAllTeams().first()
        val teamId = all.first().id
        
        viewModel.deleteTeam(teamId)
        advanceUntilIdle()
        
        assertTrue(fakeDao.getAllTeams().first().isEmpty())
    }
    // endregion

    // region Eventos One-Shot (UiEvent)
    @Test
    fun `grlClamping - emite evento ShowGrlClamping cuando el valor supera el maximo`() = runTest {
        val events = mutableListOf<GrlViewModel.UiEvent>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEvent.toList(events)
        }
        val id = viewModel.uiState.value.titulares.first().id
        
        viewModel.onGrlFocusLost(id, (GrlCalculator.GRL_MAX + 10).toString())
        advanceUntilIdle()
        
        assertTrue(events.any { it is GrlViewModel.UiEvent.ShowGrlClamping })
        job.cancel()
    }

    @Test
    fun `rankClamping - emite evento ShowRankClamping cuando el rango supera el maximo`() = runTest {
        val events = mutableListOf<GrlViewModel.UiEvent>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEvent.toList(events)
        }
        val id = viewModel.uiState.value.titulares.first().id
        
        viewModel.onRangoFocusLost(id, "9")
        advanceUntilIdle()
        
        assertTrue(events.any { it is GrlViewModel.UiEvent.ShowRankClamping })
        job.cancel()
    }
    // endregion
}
