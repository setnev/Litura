package com.litura.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalLibrary
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val label: String,
    val route: Screen,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem("Home", Screen.Home, Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem("Library", Screen.Library, Icons.Filled.LocalLibrary, Icons.Outlined.LocalLibrary),
    BottomNavItem("Badges", Screen.Badges, Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents),
    BottomNavItem("Skills", Screen.Skills, Icons.Filled.Psychology, Icons.Outlined.Psychology),
    BottomNavItem("Profile", Screen.Profile, Icons.Filled.Person, Icons.Outlined.Person)
)

@Composable
fun BottomNavBar(
    currentRoute: Screen?,
    onNavigate: (Screen) -> Unit
) {
    NavigationBar {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) }
            )
        }
    }
}
