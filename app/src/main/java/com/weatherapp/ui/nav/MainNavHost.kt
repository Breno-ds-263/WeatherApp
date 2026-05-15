package com.weatherapp.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.weatherapp.HomePage
import com.weatherapp.ListPage
import com.weatherapp.MainViewModel
import com.weatherapp.MapPage

@Composable
fun MainNavHost(
    navController: NavHostController,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {

    NavHost(
        navController = navController,
        startDestination = Route.Home
    ) {

        composable<Route.Home> {
            HomePage(
                modifier = modifier,
                viewModel = viewModel
            )
        }

        composable<Route.List> {
            ListPage(
                modifier = modifier,
                viewModel = viewModel
            )
        }

        composable<Route.Map> {
            MapPage(
                modifier = modifier,
                viewModel = viewModel
            )
        }
    }
}