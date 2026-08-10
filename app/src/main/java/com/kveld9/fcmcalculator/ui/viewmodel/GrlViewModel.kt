package com.kveld9.fcmcalculator.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kveld9.fcmcalculator.domain.GrlCalculator
import com.kveld9.fcmcalculator.ui.model.GrlUiState
import com.kveld9.fcmcalculator.ui.model.PlayerData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class GrlViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(GrlUiState())
    // Exponemos el estado directamente. El cálculo se hace en cada actualización.
    val uiState: StateFlow<GrlUiState> = _uiState.asStateFlow()

    fun onGrlChanged(playerId: String, newGrl: String) {
        updatePlayer(playerId) { it.copy(grl = newGrl) }
    }

    fun onRangoChanged(playerId: String, newRango: String) {
        updatePlayer(playerId) { player ->
            val saneado = GrlCalculator.sanearRango(newRango)
            val updatedGrl = GrlCalculator.ajustarGrlPorRango(
                grlActual = player.grl,
                rangoActual = player.rango,
                nuevoRango = saneado
            )
            player.copy(rango = saneado, grl = updatedGrl)
        }
    }

    /**
     * Llamado cuando el campo de texto pierde el foco para aplicar límites.
     */
    fun onGrlFocusLost(playerId: String, currentGrl: String) {
        val saneado = GrlCalculator.sanearGrl(currentGrl)
        if (saneado != currentGrl) {
            updatePlayer(playerId) { it.copy(grl = saneado) }
        }
    }

    fun updateAllRanks(newRank: String) {
        val saneado = GrlCalculator.sanearRango(newRank)
        _uiState.update { state ->
            val newTitulares = state.titulares.map { it.applyRank(saneado) }
            val newSuplentes = state.suplentes.map { it.applyRank(saneado) }
            
            state.copy(
                titulares = newTitulares,
                suplentes = newSuplentes,
                result = calculateResult(newTitulares, newSuplentes)
            )
        }
    }

    fun addSubstitute() {
        _uiState.update { state ->
            if (state.suplentes.size < GrlCalculator.SUPLENTES_MAX) {
                val newSuplentes = state.suplentes + PlayerData()
                state.copy(
                    suplentes = newSuplentes,
                    result = calculateResult(state.titulares, newSuplentes)
                )
            } else state
        }
    }

    fun removeSubstitute(playerId: String) {
        _uiState.update { state ->
            val newSuplentes = state.suplentes.filterNot { it.id == playerId }
            state.copy(
                suplentes = newSuplentes,
                result = calculateResult(state.titulares, newSuplentes)
            )
        }
    }

    fun clearAll() {
        _uiState.update { 
            GrlUiState() 
        }
    }

    private fun updatePlayer(playerId: String, transform: (PlayerData) -> PlayerData) {
        _uiState.update { state ->
            val isTitular = state.titulares.any { it.id == playerId }
            val newTitulares: List<PlayerData>
            val newSuplentes: List<PlayerData>

            if (isTitular) {
                newTitulares = state.titulares.map { 
                    if (it.id == playerId) transform(it) else it 
                }
                newSuplentes = state.suplentes
            } else {
                newTitulares = state.titulares
                newSuplentes = state.suplentes.map { 
                    if (it.id == playerId) transform(it) else it 
                }
            }

            state.copy(
                titulares = newTitulares,
                suplentes = newSuplentes,
                result = calculateResult(newTitulares, newSuplentes)
            )
        }
    }

    private fun calculateResult(titulares: List<PlayerData>, suplentes: List<PlayerData>): GrlCalculator.Result {
        return GrlCalculator.calcular(
            titulares = titulares.map { it.toDomain() },
            suplentes = suplentes.map { it.toDomain() }
        )
    }

    private fun PlayerData.toDomain() = GrlCalculator.Player(
        grl = grl.toDoubleOrNull(),
        rango = rango.toDoubleOrNull() ?: 0.0,
    )

    private fun PlayerData.applyRank(newRank: String): PlayerData {
        val updatedGrl = GrlCalculator.ajustarGrlPorRango(grl, rango, newRank)
        return copy(rango = newRank, grl = updatedGrl)
    }
}
