package com.eurogame.scorecard.presentation.templatebrowser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eurogame.scorecard.data.storage.TemplateInfo
import com.eurogame.scorecard.data.storage.TemplateStorageManager
import com.eurogame.scorecard.data.xml.ScorecardTemplate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TemplateBrowserState(
    val templates: List<TemplateInfo> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedTemplate: ScorecardTemplate? = null,
    val deleteConfirmation: String? = null
)

class TemplateBrowserViewModel(
    private val storageManager: TemplateStorageManager
) : ViewModel() {

    private val _state = MutableStateFlow(TemplateBrowserState())
    val state: StateFlow<TemplateBrowserState> = _state.asStateFlow()

    init {
        loadTemplates()
    }

    fun loadTemplates() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val templates = storageManager.getAllTemplates()
                _state.value = _state.value.copy(
                    templates = templates.sortedByDescending { it.lastModified },
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Failed to load templates: ${e.message}"
                )
            }
        }
    }

    fun selectTemplate(fileName: String) {
        viewModelScope.launch {
            try {
                val template = storageManager.loadTemplate(fileName)
                _state.value = _state.value.copy(selectedTemplate = template)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Failed to load template: ${e.message}"
                )
            }
        }
    }

    fun showDeleteConfirmation(fileName: String) {
        _state.value = _state.value.copy(deleteConfirmation = fileName)
    }

    fun cancelDelete() {
        _state.value = _state.value.copy(deleteConfirmation = null)
    }

    fun deleteTemplate(fileName: String) {
        viewModelScope.launch {
            try {
                val success = storageManager.deleteTemplate(fileName)
                if (success) {
                    _state.value = _state.value.copy(deleteConfirmation = null)
                    loadTemplates()
                } else {
                    _state.value = _state.value.copy(
                        deleteConfirmation = null,
                        error = "Failed to delete template"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    deleteConfirmation = null,
                    error = "Error deleting template: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun clearSelectedTemplate() {
        _state.value = _state.value.copy(selectedTemplate = null)
    }
}
