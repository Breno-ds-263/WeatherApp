package com.weatherapp.ui.nav


import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.weatherapp.ListPage
import com.weatherapp.MapPage
import com.weatherapp.HomePage

@Composable
fun MainNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {

    NavHost(
        navController = navController,
        startDestination = Route.Home
    ) {

        composable<Route.Home> {
            HomePage(modifier = modifier)
        }

        composable<Route.List> {
            ListPage(modifier = modifier)
        }

        composable<Route.Map> {
            MapPage(modifier = modifier)
        }
    }
}