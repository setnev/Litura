package com.litura.app.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.litura.app.ui.components.BookProgressCard
import com.litura.app.ui.components.HealthIndicator

@Composable
fun HomeScreen(
    onStartReading: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Greeting + Health
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = state.greeting,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "${state.totalXp} XP total",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                HealthIndicator(currentHealth = state.health)
            }
        }

        // Streak
        if (state.currentStreak > 0) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Text(
                        text = "${state.currentStreak} day streak!",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }

        // Currently reading
        item {
            Text(
                text = "Currently Reading",
                style = MaterialTheme.typography.titleLarge
            )
        }

        if (state.currentlyReading.isEmpty() && !state.isLoading) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = "No books in progress. Visit the Library to start reading!",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        items(state.currentlyReading) { book ->
            BookProgressCard(
                title = book.title,
                author = book.author,
                competencyPercent = book.competencyPercent,
                xpEarned = book.xpEarned,
                completedBites = book.completedBites,
                totalBites = book.totalBites,
                progressPercent = book.progressPercent,
                onClick = { onStartReading(book.bookId) }
            )
        }

        // Competitive standings
        state.competitive?.let { comp ->
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Competitive Reading",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "${comp.friendsBehind}", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                                Text(text = "Behind you", style = MaterialTheme.typography.labelSmall)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = comp.rankDelta, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.tertiary)
                                Text(text = "Rank change", style = MaterialTheme.typography.labelSmall)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "${comp.friendsAhead}", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.error)
                                Text(text = "Ahead of you", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        comp.highlights.forEach { h ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = h.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
