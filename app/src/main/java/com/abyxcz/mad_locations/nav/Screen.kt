package com.abyxcz.mad_locations.nav

sealed class Screen(val route: String) {
    object Location : Screen("location_map_view")
    object Geofence : Screen("geofence_screen")
    object CurrentLocation : Screen("location/current_location_screen")
    object Basic : Screen("basic_map_view")
    object BgLocation : Screen("bg_location_screen")
}