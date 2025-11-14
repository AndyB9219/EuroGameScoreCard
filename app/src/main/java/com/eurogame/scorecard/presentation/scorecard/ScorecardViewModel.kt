package com.eurogame.scorecard.presentation.scorecard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eurogame.scorecard.domain.model.Game
import com.eurogame.scorecard.domain.model.Player
import com.eurogame.scorecard.domain.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ScorecardState(
    val game: Game? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class ScorecardViewModel(
    private val repository: GameRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ScorecardState())
    val state: StateFlow<ScorecardState> = _state.asStateFlow()

    init {
        loadActiveGame()
    }

    private fun loadActiveGame() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                repository.getActiveGame().collect { game ->
                    _state.value = _state.value.copy(
                        game = game,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Failed to load game: ${e.message}"
                )
            }
        }
    }

    fun updateScore(playerId: Long, categoryId: Long, score: Int) {
        val game = _state.value.game ?: return
        viewModelScope.launch {
            try {
                repository.updateScore(game.id, playerId, categoryId, score)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Failed to update score: ${e.message}"
                )
            }
        }
    }

    fun getPlayerTotal(player: Player): Int {
        val game = _state.value.game ?: return 0
        return player.getTotalScore(game.scores, game.categories.map { it.id })
    }

    fun getWinner(): Player? {
        val game = _state.value.game ?: return null
        return game.players.maxByOrNull { getPlayerTotal(it) }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
