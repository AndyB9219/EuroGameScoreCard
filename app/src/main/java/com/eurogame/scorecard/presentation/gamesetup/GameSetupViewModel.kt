package com.eurogame.scorecard.presentation.gamesetup

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eurogame.scorecard.data.storage.TemplateStorageManager
import com.eurogame.scorecard.domain.model.CategoryInput
import com.eurogame.scorecard.domain.model.GameSetupData
import com.eurogame.scorecard.domain.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GameSetupState(
    val gameName: String = "",
    val description: String = "",
    val subtitle: String = "",
    val designer: String = "",
    val studio: String = "",
    val minPlayers: String = "",
    val maxPlayers: String = "",
    val backgroundImageUrl: String = "",
    val publishYear: String = "",
    val templateId: String? = null,
    val playerNames: List<String> = listOf("", ""),
    val categories: List<CategoryState> = listOf(CategoryState()),
    val isLoading: Boolean = false,
    val error: String? = null,
    val gameCreated: Boolean = false
)

data class CategoryState(
    val title: String = "",
    val subtitle: String = "",
    val description: String = "",
    val iconUrl: String = "",
    val backgroundImageUrl: String = "",
    val scoringRuleType: String = "",
    val scoreIndex: Int = 0,
    val isOptional: Boolean = false
)

class GameSetupViewModel(
    private val repository: GameRepository,
    private val storageManager: TemplateStorageManager
) : ViewModel() {

    private val _state = MutableStateFlow(GameSetupState())
    val state: StateFlow<GameSetupState> = _state.asStateFlow()

    fun onGameNameChange(name: String) {
        _state.value = _state.value.copy(gameName = name)
    }

    fun onDescriptionChange(description: String) {
        _state.value = _state.value.copy(description = description)
    }

    fun onPlayerNameChange(index: Int, name: String) {
        val updatedPlayers = _state.value.playerNames.toMutableList()
        updatedPlayers[index] = name
        _state.value = _state.value.copy(playerNames = updatedPlayers)
    }

    fun addPlayer() {
        _state.value = _state.value.copy(
            playerNames = _state.value.playerNames + ""
        )
    }

    fun removePlayer(index: Int) {
        if (_state.value.playerNames.size > 2) {
            val updatedPlayers = _state.value.playerNames.toMutableList()
            updatedPlayers.removeAt(index)
            _state.value = _state.value.copy(playerNames = updatedPlayers)
        }
    }

    fun onCategoryTitleChange(index: Int, title: String) {
        val updatedCategories = _state.value.categories.toMutableList()
        updatedCategories[index] = updatedCategories[index].copy(title = title)
        _state.value = _state.value.copy(categories = updatedCategories)
    }

    fun onCategorySubtitleChange(index: Int, subtitle: String) {
        val updatedCategories = _state.value.categories.toMutableList()
        updatedCategories[index] = updatedCategories[index].copy(subtitle = subtitle)
        _state.value = _state.value.copy(categories = updatedCategories)
    }

    fun loadFromTemplate(templateFileName: String) {
        viewModelScope.launch {
            try {
                val template = storageManager.loadTemplate(templateFileName)
                if (template != null) {
                    _state.value = _state.value.copy(
                        gameName = template.game.name,
                        subtitle = template.game.subtitle ?: "",
                        designer = template.game.designer ?: "",
                        studio = template.game.studio ?: "",
                        minPlayers = template.game.minPlayers?.toString() ?: "",
                        maxPlayers = template.game.maxPlayers?.toString() ?: "",
                        backgroundImageUrl = template.game.backgroundImageUrl ?: "",
                        publishYear = template.game.publishYear?.toString() ?: "",
                        templateId = templateFileName,
                        categories = template.categories.map {
                            CategoryState(
                                title = it.name,
                                subtitle = "",
                                description = it.description ?: "",
                                iconUrl = it.iconUrl ?: "",
                                backgroundImageUrl = it.backgroundImageUrl ?: "",
                                scoringRuleType = it.scoringRuleType ?: "",
                                scoreIndex = it.scoreIndex
                            )
                        }
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = "Failed to load template: ${e.message}")
            }
        }
    }

    fun addCategory() {
        _state.value = _state.value.copy(
            categories = _state.value.categories + CategoryState()
        )
    }

    fun removeCategory(index: Int) {
        if (_state.value.categories.size > 1) {
            val updatedCategories = _state.value.categories.toMutableList()
            updatedCategories.removeAt(index)
            _state.value = _state.value.copy(categories = updatedCategories)
        }
    }

    fun createGame() {
        val currentState = _state.value

        // Validate
        if (currentState.gameName.isBlank()) {
            _state.value = currentState.copy(error = "Please enter a game name")
            return
        }

        val validPlayers = currentState.playerNames.filter { it.isNotBlank() }
        if (validPlayers.size < 2) {
            _state.value = currentState.copy(error = "Please enter at least 2 player names")
            return
        }

        val validCategories = currentState.categories.filter { it.title.isNotBlank() }
        if (validCategories.isEmpty()) {
            _state.value = currentState.copy(error = "Please add at least one score category")
            return
        }

        viewModelScope.launch {
            _state.value = currentState.copy(isLoading = true, error = null)
            try {
                val setupData = GameSetupData(
                    gameName = currentState.gameName.trim(),
                    description = currentState.description.trim(),
                    subtitle = currentState.subtitle.takeIf { it.isNotBlank() }?.trim(),
                    designer = currentState.designer.takeIf { it.isNotBlank() }?.trim(),
                    studio = currentState.studio.takeIf { it.isNotBlank() }?.trim(),
                    minPlayers = currentState.minPlayers.toIntOrNull(),
                    maxPlayers = currentState.maxPlayers.toIntOrNull(),
                    backgroundImageUrl = currentState.backgroundImageUrl.takeIf { it.isNotBlank() }?.trim(),
                    publishYear = currentState.publishYear.toIntOrNull(),
                    templateId = currentState.templateId,
                    playerNames = validPlayers.map { it.trim() },
                    categories = validCategories.map {
                        CategoryInput(
                            title = it.title.trim(),
                            subtitle = it.subtitle.takeIf { s -> s.isNotBlank() }?.trim(),
                            description = it.description.takeIf { s -> s.isNotBlank() }?.trim(),
                            iconUrl = it.iconUrl.takeIf { s -> s.isNotBlank() }?.trim(),
                            backgroundImageUrl = it.backgroundImageUrl.takeIf { s -> s.isNotBlank() }?.trim(),
                            scoringRuleType = it.scoringRuleType.takeIf { s -> s.isNotBlank() }?.trim(),
                            scoreIndex = it.scoreIndex
                        )
                    }
                )
                repository.createGame(setupData)
                _state.value = currentState.copy(isLoading = false, gameCreated = true)
            } catch (e: Exception) {
                _state.value = currentState.copy(
                    isLoading = false,
                    error = "Failed to create game: ${e.message}"
                )
            }
        }
    }

    fun resetGameCreated() {
        _state.value = _state.value.copy(gameCreated = false)
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
