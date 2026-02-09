package com.litura.app.ui.screens.profile

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ProfileScreen(viewModel: ProfileViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Profile") },
            actions = {
                if (state.devModeEnabled) {
                    IconButton(onClick = { viewModel.toggleTelemetryViewer() }) {
                        Icon(Icons.Filled.BugReport, "Telemetry")
                    }
                }
            }
        )

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar & Name
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = null,
                        modifier = Modifier
                            .size(64.dp)
                            .combinedClickable(
                                onClick = {},
                                onLongClick = { viewModel.toggleDevMode() }
                            ),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        Text(
                            text = state.displayName,
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            text = "${state.subscriptionTier} Plan",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Stats
            item {
                Text("Lifetime Stats", style = MaterialTheme.typography.titleLarge)
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        StatRow("Total XP", "${state.totalXp}")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        StatRow("Bites Completed", "${state.totalBitesCompleted}")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        StatRow("Books Completed", "${state.booksCompleted}")
                    }
                }
            }

            // Streaks
            item {
                Text("Streaks", style = MaterialTheme.typography.titleLarge)
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        StatRow("Current Streak", "${state.currentStreak} days")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        StatRow("Longest Streak", "${state.longestStreak} days")
                    }
                }
            }

            // Dev mode toggle
            item {
                if (state.devModeEnabled) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Developer Mode", style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = state.devModeEnabled,
                                onCheckedChange = { viewModel.toggleDevMode() }
                            )
                        }
                    }
                }
            }

            // Telemetry viewer
            if (state.showTelemetryViewer) {
                item {
                    Text("Telemetry Events", style = MaterialTheme.typography.titleLarge)
                }
                items(state.telemetryEvents) { event ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = event.eventType,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                                        .format(Date(event.timestamp)),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            if (event.bookId != null) {
                                Text(
                                    text = "Book: ${event.bookId}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (event.xpAwarded != null) {
                                Text(
                                    text = "XP: +${event.xpAwarded}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
