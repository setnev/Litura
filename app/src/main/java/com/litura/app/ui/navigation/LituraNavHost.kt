package com.litura.app.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.litura.app.ui.screens.badges.BadgesScreen
import com.litura.app.ui.screens.home.HomeScreen
import com.litura.app.ui.screens.library.LibraryScreen
import com.litura.app.ui.screens.profile.ProfileScreen
import com.litura.app.ui.screens.reading.ReadingScreen
import com.litura.app.ui.screens.skills.SkillsScreen

@Composable
fun LituraNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    val currentRoute: Screen? = when (navBackStackEntry?.destination?.route) {
        Screen.Home::class.qualifiedName -> Screen.Home
        Screen.Library::class.qualifiedName -> Screen.Library
        Screen.Badges::class.qualifiedName -> Screen.Badges
        Screen.Skills::class.qualifiedName -> Screen.Skills
        Screen.Profile::class.qualifiedName -> Screen.Profile
        else -> null
    }

    val isReading = navBackStackEntry?.destination?.route?.contains("Reading") == true

    Scaffold(
        bottomBar = {
            if (!isReading) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { screen ->
                        navController.navigate(screen) {
                            popUpTo(Screen.Home) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) {
            composable<Screen.Home> {
                HomeScreen(
                    onStartReading = { bookId ->
                        navController.navigate(Screen.Reading(bookId))
                    }
                )
            }

            composable<Screen.Library> {
                LibraryScreen(
                    onStartReading = { bookId ->
                        navController.navigate(Screen.Reading(bookId))
                    }
                )
            }

            composable<Screen.Badges> {
                BadgesScreen()
            }

            composable<Screen.Skills> {
                SkillsScreen()
            }

            composable<Screen.Profile> {
                ProfileScreen()
            }

            composable<Screen.Reading>(
                enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
                exitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
            ) { backStackEntry ->
                val reading = backStackEntry.toRoute<Screen.Reading>()
                ReadingScreen(
                    bookId = reading.bookId,
                    initialBiteId = reading.biteId,
                    onQuit = { navController.popBackStack() }
                )
            }
        }
    }
}
