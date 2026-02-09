package com.litura.app.ui.screens.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.litura.app.domain.model.PurchaseState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onStartReading: (String) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showSortMenu by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Library") },
            actions = {
                IconButton(onClick = { viewModel.toggleSearch() }) {
                    Icon(Icons.Filled.Search, "Search")
                }
                IconButton(onClick = { showSortMenu = true }) {
                    Icon(Icons.Filled.SortByAlpha, "Sort")
                }
                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false }
                ) {
                    BookSort.entries.forEach { sort ->
                        DropdownMenuItem(
                            text = { Text(sort.name.replace("_", " ")) },
                            onClick = {
                                viewModel.setSort(sort)
                                showSortMenu = false
                            }
                        )
                    }
                }
            }
        )

        AnimatedVisibility(visible = state.isSearchVisible) {
            TextField(
                value = state.searchQuery,
                onValueChange = { viewModel.search(it) },
                placeholder = { Text("Search books...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true
            )
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.books, key = { it.bookId }) { book ->
                LibraryBookCard(
                    book = book,
                    onExpand = { viewModel.toggleExpanded(book.bookId) },
                    onFavorite = { viewModel.toggleFavorite(book.bookId) },
                    onPurchase = { viewModel.togglePurchase(book.bookId) },
                    onRead = {
                        if (book.purchaseState == PurchaseState.OWNED_DOWNLOADED) {
                            onStartReading(book.bookId)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun LibraryBookCard(
    book: LibraryBookItem,
    onExpand: () -> Unit,
    onFavorite: () -> Unit,
    onPurchase: () -> Unit,
    onRead: () -> Unit
) {
    val titleIcon = when (book.purchaseState) {
        PurchaseState.NOT_OWNED -> Icons.Filled.Cloud
        PurchaseState.OWNED_NOT_DOWNLOADED -> Icons.Filled.LockOpen
        PurchaseState.OWNED_DOWNLOADED -> Icons.Filled.Save
    }

    val purchaseIcon = when (book.purchaseState) {
        PurchaseState.NOT_OWNED -> Icons.Filled.ShoppingCart
        PurchaseState.OWNED_NOT_DOWNLOADED -> Icons.Filled.Lock
        PurchaseState.OWNED_DOWNLOADED -> Icons.Filled.LockOpen
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onRead),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    titleIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = book.title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = book.author,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onFavorite) {
                    Icon(
                        if (book.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (book.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onPurchase) {
                    Icon(purchaseIcon, contentDescription = "Purchase")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bites: ${book.biteCount}",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = if (book.price == 0.0) "Free" else "$${book.price}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = onExpand) {
                    Icon(
                        if (book.isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = "Expand"
                    )
                }
            }

            AnimatedVisibility(visible = book.isExpanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text(
                        text = "Difficulty: ${book.difficultyTier} (Lexile: ${book.lexile})",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Genres: ${book.genres}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Skills: ${book.primarySkills}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
