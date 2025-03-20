package com.example.starwarsplanets.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.starwarsplanets.data.models.Planet
import com.example.starwarsplanets.ui.details.PlanetDetailsScreen
import com.example.starwarsplanets.ui.planetlist.PlanetListScreen
import com.example.starwarsplanets.ui.splash.SplashScreen
import com.google.gson.Gson
import java.net.URLDecoder
import java.net.URLEncoder

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object PlanetList : Screen("planet_list")
    object PlanetDetail : Screen("planet_detail/{planetJson}") {
        fun createRoute(planet: Planet): String {
            val planetJson = Gson().toJson(planet)
            val encodedJson = URLEncoder.encode(planetJson, "UTF-8")
            return "planet_detail/$encodedJson"
        }
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashComplete = {
                    navController.navigate(Screen.PlanetList.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.PlanetList.route) {
            PlanetListScreen(
                onPlanetClick = { planet ->
                    navController.navigate(Screen.PlanetDetail.createRoute(planet))
                }
            )
        }

        composable(Screen.PlanetDetail.route) { backStackEntry ->
            val planetJson = backStackEntry.arguments?.getString("planetJson") ?: ""
            val decodedJson = URLDecoder.decode(planetJson, "UTF-8")
            val planet = Gson().fromJson(decodedJson, Planet::class.java)

            PlanetDetailsScreen(
                planet = planet,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}