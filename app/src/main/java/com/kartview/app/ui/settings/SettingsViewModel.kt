package com.kartview.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.kartview.app.data.db.GlassPreset
import com.kartview.app.data.db.PresetDao
import com.kartview.app.data.model.GlassSettings
import com.kartview.app.data.model.toPreset
import com.kartview.app.data.model.toSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val dao: PresetDao) : ViewModel() {

    private val _settings = MutableStateFlow(GlassSettings())
    val settings: StateFlow<GlassSettings> = _settings.asStateFlow()

    val presets: StateFlow<List<GlassPreset>> = dao.observePresets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            dao.getActive()?.let { active -> _settings.value = active.toSettings() }
        }
    }

    fun update(transform: (GlassSettings) -> GlassSettings) {
        _settings.value = transform(_settings.value)
    }

    fun savePreset(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            dao.saveAndActivate(_settings.value.toPreset(trimmed))
        }
    }

    fun applyPreset(preset: GlassPreset) {
        viewModelScope.launch {
            dao.activate(preset)
            _settings.value = preset.toSettings()
        }
    }

    fun deletePreset(preset: GlassPreset) {
        viewModelScope.launch { dao.delete(preset) }
    }

    companion object {
        fun factory(dao: PresetDao) = viewModelFactory {
            initializer { SettingsViewModel(dao) }
        }
    }
}
