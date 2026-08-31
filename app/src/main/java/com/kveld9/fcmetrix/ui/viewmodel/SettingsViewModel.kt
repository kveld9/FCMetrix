package com.kveld9.fcmetrix.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kveld9.fcmetrix.data.LineupRepository
import com.kveld9.fcmetrix.data.ThemePreferences
import com.kveld9.fcmetrix.data.ThemeSettings
import com.kveld9.fcmetrix.data.backup.DuplicatePolicy
import com.kveld9.fcmetrix.data.backup.JsonBackupManager
import com.kveld9.fcmetrix.data.local.entity.TeamEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
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
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.io.OutputStreamWriter

sealed class SettingsUiEvent {
    data class Success(val message: String) : SettingsUiEvent()
    data class Error(val message: String) : SettingsUiEvent()
    data class ImportPromptDuplicate(val teams: List<TeamEntity>) : SettingsUiEvent()
}

data class SettingsUiState(
    val isLoading: Boolean = false,
    val pendingImportTeams: List<TeamEntity>? = null
)

class SettingsViewModel(
    private val themePreferences: ThemePreferences,
    private val repository: LineupRepository,
    private val backupManager: JsonBackupManager = JsonBackupManager(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    val themeSettings: StateFlow<ThemeSettings> = themePreferences.themeSettings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ThemeSettings()
    )

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<SettingsUiEvent>()
    val uiEvent: SharedFlow<SettingsUiEvent> = _uiEvent.asSharedFlow()

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            themePreferences.setThemeMode(mode)
        }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            themePreferences.setDynamicColor(enabled)
        }
    }

    fun setAmoledBlack(enabled: Boolean) {
        viewModelScope.launch {
            themePreferences.setAmoledBlack(enabled)
        }
    }

    fun exportBackup(outputStream: OutputStream) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val jsonString = withContext(ioDispatcher) {
                    val teams = repository.getAllTeamsSync()
                    if (teams.isEmpty()) {
                        throw IllegalStateException("No hay plantillas guardadas para exportar.")
                    }
                    val json = backupManager.exportToJson(teams)
                    OutputStreamWriter(outputStream, Charsets.UTF_8).use { writer ->
                        writer.write(json)
                        writer.flush()
                    }
                    json
                }
                _uiEvent.emit(SettingsUiEvent.Success("Respaldo exportado correctamente."))
            } catch (e: Exception) {
                _uiEvent.emit(SettingsUiEvent.Error(e.localizedMessage ?: "Error al exportar respaldo."))
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun startImport(inputStream: InputStream) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val parsedTeams = withContext(ioDispatcher) {
                    val content = backupManager.readBoundedStream(inputStream)
                    val result = backupManager.parseAndValidateJson(content)
                    result.getOrThrow()
                }

                if (parsedTeams.isEmpty()) {
                    _uiEvent.emit(SettingsUiEvent.Error("El archivo no contiene plantillas válidas."))
                    return@launch
                }

                val existing = withContext(ioDispatcher) { repository.getAllTeamsSync() }
                val hasDuplicates = if (existing.isEmpty()) {
                    false
                } else {
                    val existingIds = existing.map { it.id }.toSet()
                    val existingNames = existing.map { it.name.lowercase().trim() }.toSet()
                    parsedTeams.any { existingIds.contains(it.id) || existingNames.contains(it.name.lowercase().trim()) }
                }

                if (hasDuplicates) {
                    _uiState.update { it.copy(pendingImportTeams = parsedTeams) }
                    _uiEvent.emit(SettingsUiEvent.ImportPromptDuplicate(parsedTeams))
                } else {
                    executeImport(parsedTeams, DuplicatePolicy.OVERWRITE_ALL)
                }
            } catch (e: Exception) {
                _uiEvent.emit(SettingsUiEvent.Error(e.localizedMessage ?: "Error al leer el archivo de respaldo."))
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun applyImportPolicy(policy: DuplicatePolicy) {
        val pending = _uiState.value.pendingImportTeams ?: return
        _uiState.update { it.copy(pendingImportTeams = null) }
        executeImport(pending, policy)
    }

    fun dismissImportPrompt() {
        _uiState.update { it.copy(pendingImportTeams = null) }
    }

    private fun executeImport(teams: List<TeamEntity>, policy: DuplicatePolicy) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val (inserted, skipped) = withContext(ioDispatcher) {
                    repository.importTeams(teams, policy)
                }
                val msg = if (skipped > 0) {
                    "Se importaron $inserted plantillas ($skipped omitidas)."
                } else {
                    "Se importaron $inserted plantillas exitosamente."
                }
                _uiEvent.emit(SettingsUiEvent.Success(msg))
            } catch (e: Exception) {
                _uiEvent.emit(SettingsUiEvent.Error(e.localizedMessage ?: "Error al importar plantillas."))
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun deleteAllTeams() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                withContext(ioDispatcher) {
                    repository.deleteAllTeams()
                }
                _uiEvent.emit(SettingsUiEvent.Success("Todas las plantillas fueron eliminadas."))
            } catch (e: Exception) {
                _uiEvent.emit(SettingsUiEvent.Error(e.localizedMessage ?: "Error al eliminar las plantillas."))
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}

class SettingsViewModelFactory(
    private val themePreferences: ThemePreferences,
    private val repository: LineupRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(themePreferences, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
