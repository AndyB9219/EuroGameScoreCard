package com.eurogame.scorecard.presentation.scorecard

import android.content.Context
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eurogame.scorecard.domain.model.Game
import com.eurogame.scorecard.domain.model.Player
import com.eurogame.scorecard.domain.repository.GameRepository
import com.eurogame.scorecard.utils.ScreenshotUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ScorecardState(
    val game: Game? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val exportMessage: String? = null,
    val isExporting: Boolean = false
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

    fun exportScorecard(context: Context, bitmap: ImageBitmap) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isExporting = true)
            try {
                val game = _state.value.game
                val fileName = game?.name?.replace(" ", "_")?.lowercase() ?: "scorecard"
                val result = ScreenshotUtils.saveImageToGallery(context, bitmap, fileName)

                result.fold(
                    onSuccess = { message ->
                        _state.value = _state.value.copy(
                            exportMessage = message,
                            isExporting = false
                        )
                    },
                    onFailure = { exception ->
                        _state.value = _state.value.copy(
                            error = "Failed to export scorecard: ${exception.message}",
                            isExporting = false
                        )
                    }
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Failed to export scorecard: ${e.message}",
                    isExporting = false
                )
            }
        }
    }

    fun clearExportMessage() {
        _state.value = _state.value.copy(exportMessage = null)
    }
}
