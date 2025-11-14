package com.eurogame.scorecard.presentation.scorecard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.eurogame.scorecard.domain.model.Player
import com.eurogame.scorecard.domain.model.ScoreCategory
import com.eurogame.scorecard.presentation.components.BackgroundImage
import com.eurogame.scorecard.presentation.components.CategoryIcon
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScorecardScreen(
    viewModel: ScorecardViewModel = koinViewModel(),
    onNewGame: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var showNewGameDialog by remember { mutableStateOf(false) }

    if (showNewGameDialog) {
        AlertDialog(
            onDismissRequest = { showNewGameDialog = false },
            title = { Text("Start New Game") },
            text = { Text("Are you sure you want to start a new game? All scores will be lost.") },
            confirmButton = {
                TextButton(onClick = {
                    showNewGameDialog = false
                    onNewGame()
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewGameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (state.error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Error") },
            text = { Text(state.error ?: "") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.game?.name ?: "Scorecard") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    TextButton(onClick = { showNewGameDialog = true }) {
                        Text("New Game", color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            )
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (state.game == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("No active game found")
                    Button(onClick = onNewGame) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create New Game")
                    }
                }
            }
        } else {
            val game = state.game!!
            val winner = viewModel.getWinner()

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Game Header with Background Image
                if (!game.backgroundImageUrl.isNullOrBlank() || game.description.isNotBlank() ||
                    game.subtitle != null || game.designer != null) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                        ) {
                            BackgroundImage(
                                imageUrl = game.backgroundImageUrl,
                                contentDescription = "Game background",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                alpha = 0.5f
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (game.subtitle != null) {
                                    Text(
                                        text = game.subtitle,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (game.backgroundImageUrl != null) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                if (game.description.isNotBlank()) {
                                    Text(
                                        text = game.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (game.backgroundImageUrl != null) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    )
                                }

                                if (game.designer != null || game.studio != null) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (game.designer != null) {
                                            Text(
                                                text = "By ${game.designer}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (game.backgroundImageUrl != null) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        if (game.studio != null) {
                                            Text(
                                                text = "• ${game.studio}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (game.backgroundImageUrl != null) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(4.dp)) }

                // Header Row
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Category",
                                modifier = Modifier.weight(2f),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                            game.players.forEach { player ->
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = player.name,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleSmall,
                                        textAlign = TextAlign.Center
                                    )
                                    if (winner?.id == player.id) {
                                        Text(
                                            text = "👑",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Category Rows
                items(game.categories) { category ->
                    CategoryRow(
                        category = category,
                        players = game.players,
                        scores = game.scores,
                        onScoreChange = { playerId, score ->
                            viewModel.updateScore(playerId, category.id, score)
                        }
                    )
                }

                // Total Row
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Victory Points",
                                modifier = Modifier.weight(2f),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            game.players.forEach { player ->
                                Text(
                                    text = viewModel.getPlayerTotal(player).toString(),
                                    modifier = Modifier.weight(1f),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleLarge,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun CategoryRow(
    category: ScoreCategory,
    players: List<Player>,
    scores: Map<Pair<Long, Long>, Int>,
    onScoreChange: (Long, Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box {
            // Background image if available
            if (!category.backgroundImageUrl.isNullOrBlank()) {
                BackgroundImage(
                    imageUrl = category.backgroundImageUrl,
                    contentDescription = "Background for ${category.title}",
                    modifier = Modifier
                        .matchParentSize()
                        .clip(CardDefaults.shape),
                    alpha = 0.15f
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(2f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category icon
                    if (!category.iconUrl.isNullOrBlank()) {
                        CategoryIcon(
                            iconUrl = category.iconUrl,
                            contentDescription = "${category.title} icon",
                            size = 40.dp
                        )
                    }

                    Column {
                        Text(
                            text = category.title,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        if (!category.subtitle.isNullOrBlank()) {
                            Text(
                                text = category.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (!category.description.isNullOrBlank()) {
                            Text(
                                text = category.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                players.forEach { player ->
                    var scoreText by remember(scores[Pair(player.id, category.id)]) {
                        mutableStateOf((scores[Pair(player.id, category.id)] ?: 0).toString())
                    }

                    OutlinedTextField(
                        value = scoreText,
                        onValueChange = { newValue ->
                            scoreText = newValue
                            val score = newValue.toIntOrNull() ?: 0
                            onScoreChange(player.id, score)
                        },
                        modifier = Modifier.weight(1f),
                        textStyle = LocalTextStyle.current.copy(
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        )
                    )
                }
            }
        }
    }
}
