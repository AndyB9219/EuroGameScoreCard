package com.eurogame.scorecard.presentation.templatebuilder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eurogame.scorecard.data.storage.TemplateStorageManager
import com.eurogame.scorecard.data.xml.CategoryTemplate
import com.eurogame.scorecard.data.xml.GameTemplate
import com.eurogame.scorecard.data.xml.ScorecardTemplate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TemplateBuilderState(
    val gameName: String = "",
    val subtitle: String = "",
    val designer: String = "",
    val studio: String = "",
    val minPlayers: String = "",
    val maxPlayers: String = "",
    val backgroundImageUrl: String = "",
    val publishYear: String = "",
    val categories: List<CategoryBuilderState> = listOf(CategoryBuilderState()),
    val isSaving: Boolean = false,
    val error: String? = null,
    val templateSaved: Boolean = false
)

data class CategoryBuilderState(
    val name: String = "",
    val description: String = "",
    val iconUrl: String = "",
    val backgroundImageUrl: String = "",
    val scoringRuleType: String = "",
    val scoreIndex: String = "0",
    val isOptional: Boolean = false
)

class TemplateBuilderViewModel(
    private val storageManager: TemplateStorageManager
) : ViewModel() {

    private val _state = MutableStateFlow(TemplateBuilderState())
    val state: StateFlow<TemplateBuilderState> = _state.asStateFlow()

    fun onGameNameChange(name: String) {
        _state.value = _state.value.copy(gameName = name)
    }

    fun onSubtitleChange(subtitle: String) {
        _state.value = _state.value.copy(subtitle = subtitle)
    }

    fun onDesignerChange(designer: String) {
        _state.value = _state.value.copy(designer = designer)
    }

    fun onStudioChange(studio: String) {
        _state.value = _state.value.copy(studio = studio)
    }

    fun onMinPlayersChange(minPlayers: String) {
        _state.value = _state.value.copy(minPlayers = minPlayers)
    }

    fun onMaxPlayersChange(maxPlayers: String) {
        _state.value = _state.value.copy(maxPlayers = maxPlayers)
    }

    fun onBackgroundImageUrlChange(url: String) {
        _state.value = _state.value.copy(backgroundImageUrl = url)
    }

    fun onPublishYearChange(year: String) {
        _state.value = _state.value.copy(publishYear = year)
    }

    fun onCategoryNameChange(index: Int, name: String) {
        val updatedCategories = _state.value.categories.toMutableList()
        updatedCategories[index] = updatedCategories[index].copy(name = name)
        _state.value = _state.value.copy(categories = updatedCategories)
    }

    fun onCategoryDescriptionChange(index: Int, description: String) {
        val updatedCategories = _state.value.categories.toMutableList()
        updatedCategories[index] = updatedCategories[index].copy(description = description)
        _state.value = _state.value.copy(categories = updatedCategories)
    }

    fun onCategoryIconUrlChange(index: Int, url: String) {
        val updatedCategories = _state.value.categories.toMutableList()
        updatedCategories[index] = updatedCategories[index].copy(iconUrl = url)
        _state.value = _state.value.copy(categories = updatedCategories)
    }

    fun onCategoryBackgroundImageUrlChange(index: Int, url: String) {
        val updatedCategories = _state.value.categories.toMutableList()
        updatedCategories[index] = updatedCategories[index].copy(backgroundImageUrl = url)
        _state.value = _state.value.copy(categories = updatedCategories)
    }

    fun onCategoryScoringRuleTypeChange(index: Int, type: String) {
        val updatedCategories = _state.value.categories.toMutableList()
        updatedCategories[index] = updatedCategories[index].copy(scoringRuleType = type)
        _state.value = _state.value.copy(categories = updatedCategories)
    }

    fun onCategoryScoreIndexChange(index: Int, scoreIndex: String) {
        val updatedCategories = _state.value.categories.toMutableList()
        updatedCategories[index] = updatedCategories[index].copy(scoreIndex = scoreIndex)
        _state.value = _state.value.copy(categories = updatedCategories)
    }

    fun addCategory() {
        _state.value = _state.value.copy(
            categories = _state.value.categories + CategoryBuilderState()
        )
    }

    fun removeCategory(index: Int) {
        if (_state.value.categories.size > 1) {
            val updatedCategories = _state.value.categories.toMutableList()
            updatedCategories.removeAt(index)
            _state.value = _state.value.copy(categories = updatedCategories)
        }
    }

    fun saveTemplate() {
        val currentState = _state.value

        // Validate
        if (currentState.gameName.isBlank()) {
            _state.value = currentState.copy(error = "Please enter a game name")
            return
        }

        val validCategories = currentState.categories.filter { it.name.isNotBlank() }
        if (validCategories.isEmpty()) {
            _state.value = currentState.copy(error = "Please add at least one score category")
            return
        }

        viewModelScope.launch {
            _state.value = currentState.copy(isSaving = true, error = null)
            try {
                val gameTemplate = GameTemplate(
                    name = currentState.gameName.trim(),
                    subtitle = currentState.subtitle.takeIf { it.isNotBlank() }?.trim(),
                    designer = currentState.designer.takeIf { it.isNotBlank() }?.trim(),
                    studio = currentState.studio.takeIf { it.isNotBlank() }?.trim(),
                    minPlayers = currentState.minPlayers.toIntOrNull(),
                    maxPlayers = currentState.maxPlayers.toIntOrNull(),
                    backgroundImageUrl = currentState.backgroundImageUrl.takeIf { it.isNotBlank() }?.trim(),
                    publishYear = currentState.publishYear.toIntOrNull()
                )

                val categoryTemplates = validCategories.mapIndexed { index, category ->
                    CategoryTemplate(
                        name = category.name.trim(),
                        description = category.description.takeIf { it.isNotBlank() }?.trim(),
                        iconUrl = category.iconUrl.takeIf { it.isNotBlank() }?.trim(),
                        backgroundImageUrl = category.backgroundImageUrl.takeIf { it.isNotBlank() }?.trim(),
                        scoringRuleType = category.scoringRuleType.takeIf { it.isNotBlank() }?.trim(),
                        scoreIndex = category.scoreIndex.toIntOrNull() ?: index
                    )
                }

                val template = ScorecardTemplate(
                    game = gameTemplate,
                    categories = categoryTemplates
                )

                val fileName = storageManager.generateUniqueFileName(currentState.gameName)
                val success = storageManager.saveTemplate(template, fileName)

                if (success) {
                    _state.value = currentState.copy(
                        isSaving = false,
                        templateSaved = true
                    )
                } else {
                    _state.value = currentState.copy(
                        isSaving = false,
                        error = "Failed to save template"
                    )
                }
            } catch (e: Exception) {
                _state.value = currentState.copy(
                    isSaving = false,
                    error = "Error saving template: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun resetTemplateSaved() {
        _state.value = _state.value.copy(templateSaved = false)
    }
}
