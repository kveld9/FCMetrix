package com.kveld9.fcmcalculator.ui.viewmodel

import com.kveld9.fcmcalculator.domain.GrlCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GrlViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: GrlViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = GrlViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has 11 empty starters`() {
        val state = viewModel.uiState.value
        assertEquals(11, state.titulares.size)
        assertTrue(state.suplentes.isEmpty())
        assertNull(state.result.grlGlobal)
        assertEquals(0, state.result.titularesCargados)
    }

    @Test
    fun `adding substitute increases substitute list size`() {
        viewModel.addSubstitute()
        assertEquals(1, viewModel.uiState.value.suplentes.size)
    }

    @Test
    fun `removing substitute works by id`() {
        viewModel.addSubstitute()
        val id = viewModel.uiState.value.suplentes.first().id
        viewModel.removeSubstitute(id)
        assertTrue(viewModel.uiState.value.suplentes.isEmpty())
    }

    @Test
    fun `updating GRL of a player recalculates global GRL when 11 are filled`() {
        val titulares = viewModel.uiState.value.titulares
        
        // Rellenar 11 titulares con GRL 100
        titulares.forEach { player ->
            viewModel.onGrlChanged(player.id, "100")
        }

        val state = viewModel.uiState.value
        assertEquals(11, state.result.titularesCargados)
        assertNotNull(state.result.grlGlobal)
        assertEquals(100, state.result.grlGlobal)
    }

    @Test
    fun `GRL focus lost applies clamping limits`() {
        val player = viewModel.uiState.value.titulares.first()
        
        // Demasiado alto -> 150
        viewModel.onGrlFocusLost(player.id, "200")
        assertEquals("150", viewModel.uiState.value.titulares.first().grl)

        // Demasiado bajo -> 47
        viewModel.onGrlFocusLost(player.id, "10")
        assertEquals("47", viewModel.uiState.value.titulares.first().grl)
    }

    @Test
    fun `changing rank adjusts GRL automatically`() {
        val player = viewModel.uiState.value.titulares.first()
        viewModel.onGrlChanged(player.id, "100") // GRL 100, Rango 0
        
        viewModel.onRangoChanged(player.id, "5") // GRL debería ser 105 (100 base + 5 rango)
        assertEquals("105", viewModel.uiState.value.titulares.first().grl)
    }

    @Test
    fun `clear all resets state to initial`() {
        viewModel.onGrlChanged(viewModel.uiState.value.titulares.first().id, "100")
        viewModel.addSubstitute()
        
        viewModel.clearAll()
        
        val state = viewModel.uiState.value
        assertTrue(state.titulares.all { it.grl.isEmpty() })
        assertTrue(state.suplentes.isEmpty())
    }
}
