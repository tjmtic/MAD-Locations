package com.abyxcz.mad_locations.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.abyxcz.mad_locations.maps.GeofenceScreen
import com.abyxcz.mad_locations.maps.LocationMapView

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Location.route
    ) {

        composable(route = Screen.Location.route) {
            LocationMapView(onNavigateToSecondary = { navController.navigate(route = Screen.Geofence.route)})
        }
        composable(route = Screen.Geofence.route) {
            GeofenceScreen(onNavigateToMain = { navController.navigate(route = Screen.Location.route)})
        }
        composable(route = Screen.CurrentLocation.route) {
            GeofenceScreen(onNavigateToMain = { navController.navigate(route = Screen.Main.route)})
        }
        composable(route = Screen.BgLocation.route) {
            GeofenceScreen(onNavigateToMain = { navController.navigate(route = Screen.Main.route)})
        }
        composable(route = Screen.Basic.route) {
            GeofenceScreen(onNavigateToMain = { navController.navigate(route = Screen.Main.route)})
        }

    }
}