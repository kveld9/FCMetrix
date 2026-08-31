package com.kveld9.fcmetrix.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kveld9.fcmetrix.data.LineupRepository
import com.kveld9.fcmetrix.data.local.entity.TeamEntity
import com.kveld9.fcmetrix.domain.GrlCalculator
import com.kveld9.fcmetrix.domain.model.PlayerData
import com.kveld9.fcmetrix.ui.model.GrlUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GrlViewModel(private val repository: LineupRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(GrlUiState())
    val uiState: StateFlow<GrlUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    val allTeams = repository.allTeams.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    sealed class UiEvent {
        data class ShowGrlClamping(val min: Int, val max: Int) : UiEvent()
        data class ShowRankClamping(val min: Int, val max: Int) : UiEvent()
    }

    fun saveCurrentTeam(name: String) {
        if (name.isBlank()) return
        
        viewModelScope.launch {
            repository.saveTeam(
                id = java.util.UUID.randomUUID().toString(),
                name = name,
                titulares = _uiState.value.titulares,
                suplentes = _uiState.value.suplentes
            )
            _uiState.update { it.copy(teamName = name) }
        }
    }

    fun loadTeam(team: TeamEntity) {
        _uiState.update { it.copy(
            teamName = team.name,
            titulares = team.titulares,
            suplentes = team.suplentes,
            result = calculateResult(team.titulares, team.suplentes)
        ) }
    }

    fun deleteTeam(id: String) {
        viewModelScope.launch { repository.deleteTeam(id) }
    }

    fun onGrlChanged(playerId: String, newGrl: String) {
        updatePlayer(playerId) { it.copy(grl = newGrl) }
    }

    fun onRangoChanged(playerId: String, newRango: String) {
        updatePlayer(playerId) { player ->
            val rankToApply = if (newRango.isEmpty()) "" else sanearRango(newRango)
            player.applyRank(rankToApply)
        }
    }

    fun onRangoFocusLost(playerId: String, currentRango: String) {
        val numeric = currentRango.toIntOrNull()
        if (numeric != null && (numeric < GrlCalculator.RANGO_MIN || numeric > GrlCalculator.RANGO_MAX)) {
            viewModelScope.launch {
                _uiEvent.emit(UiEvent.ShowRankClamping(GrlCalculator.RANGO_MIN, GrlCalculator.RANGO_MAX))
            }
        }

        val saneado = sanearRango(currentRango)
        if (saneado != currentRango || currentRango.isEmpty()) {
            updatePlayer(playerId) { it.copy(rango = saneado) }
        }
    }

    fun onGrlFocusLost(playerId: String, currentGrl: String) {
        val numeric = currentGrl.toIntOrNull()
        if (numeric != null && (numeric < GrlCalculator.GRL_MIN || numeric > GrlCalculator.GRL_MAX)) {
            viewModelScope.launch {
                _uiEvent.emit(UiEvent.ShowGrlClamping(GrlCalculator.GRL_MIN, GrlCalculator.GRL_MAX))
            }
        }

        val saneado = sanearGrl(currentGrl)
        if (saneado != currentGrl) {
            updatePlayer(playerId) { it.copy(grl = saneado) }
        }
    }

    fun updateAllRanks(newRank: String) {
        val saneado = sanearRango(newRank)
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
            val newTitulares = if (isTitular) state.titulares.updateById(playerId, transform) else state.titulares
            val newSuplentes = if (!isTitular) state.suplentes.updateById(playerId, transform) else state.suplentes

            state.copy(
                titulares = newTitulares,
                suplentes = newSuplentes,
                result = calculateResult(newTitulares, newSuplentes)
            )
        }
    }

    private fun List<PlayerData>.updateById(id: String, transform: (PlayerData) -> PlayerData): List<PlayerData> =
        map { if (it.id == id) transform(it) else it }

    private fun calculateResult(titulares: List<PlayerData>, suplentes: List<PlayerData>): GrlCalculator.Result {
        return GrlCalculator.calcular(
            titulares = titulares.map { it.toDomain() },
            suplentes = suplentes.map { it.toDomain() }
        )
    }

    private fun PlayerData.toDomain() = GrlCalculator.Player(
        grl = grl.toIntOrNull()?.takeIf { it in GrlCalculator.GRL_MIN..GrlCalculator.GRL_MAX },
        rango = rango.toIntOrNull()?.coerceIn(GrlCalculator.RANGO_MIN, GrlCalculator.RANGO_MAX) ?: 0,
    )

    private fun PlayerData.applyRank(newRank: String): PlayerData {
        val updatedGrlInt = GrlCalculator.ajustarGrlPorRango(
            grlActual = grl.toIntOrNull(),
            rangoActual = rango.toIntOrNull() ?: 0,
            nuevoRango = newRank.toIntOrNull() ?: 0
        )
        return copy(rango = newRank, grl = updatedGrlInt?.toString() ?: grl)
    }

    // region Lógica de Saneamiento (UI Clamping)
    private fun sanitizeBoundedInt(input: String, min: Int, max: Int, defaultIfEmpty: String): String {
        if (input.isBlank()) return defaultIfEmpty
        val numeric = input.trim().filterIndexed { i, c ->
            c.isDigit() || (i == 0 && c == '-')
        }.toIntOrNull() ?: return defaultIfEmpty
        return numeric.coerceIn(min, max).toString()
    }

    private fun sanearGrl(input: String): String =
        sanitizeBoundedInt(input, GrlCalculator.GRL_MIN, GrlCalculator.GRL_MAX, defaultIfEmpty = "")

    private fun sanearRango(input: String): String =
        sanitizeBoundedInt(input, GrlCalculator.RANGO_MIN, GrlCalculator.RANGO_MAX, defaultIfEmpty = "0")
    // endregion
}

class GrlViewModelFactory(private val repository: LineupRepository) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GrlViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GrlViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
