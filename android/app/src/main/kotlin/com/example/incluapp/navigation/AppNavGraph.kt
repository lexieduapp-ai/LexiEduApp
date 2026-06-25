package com.example.incluapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.incluapp.LexiEduContainer
import com.example.incluapp.presentation.help.HelpScreen
import com.example.incluapp.presentation.history.HistoryRoute
import com.example.incluapp.presentation.home.HomeRoute
import com.example.incluapp.presentation.reader.ReaderRoute
import com.example.incluapp.presentation.splash.SplashScreen

@Composable
fun AppNavGraph(
    container: LexiEduContainer,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Splash
    ) {
        composable<Splash> {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(Home) {
                        popUpTo(Splash) { inclusive = true }
                    }
                }
            )
        }

        composable<Home> {
            HomeRoute(
                container = container,
                onNavigateToReader = { imagePath ->
                    navController.navigate(Reader(imagePath = imagePath))
                },
                onNavigateToHistory = {
                    navController.navigate(History)
                },
                onNavigateToHelp = {
                    navController.navigate(Help)
                }
            )
        }

        composable<Reader> { backStackEntry ->
            val route: Reader = backStackEntry.toRoute()
            ReaderRoute(
                container = container,
                readingId = route.readingId,
                imageUri = route.imagePath,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<History> {
            HistoryRoute(
                container = container,
                onNavigateBack = { navController.popBackStack() },
                onOpenReading = { readingId ->
                    navController.navigate(Reader(readingId = readingId))
                }
            )
        }

        composable<Help> {
            HelpScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
