package com.abyxcz.mad_locations

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.abyxcz.mad_locations.maps.LocationMapView
import com.abyxcz.mad_locations.ui.theme.MAD_LocationsTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.android.gms.location.FusedLocationProviderClient


class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "TAG-MainActivity"
    }

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val viewModel = LocationViewModel((application as MainApplication).getDB().LocationDao(),
                                            DefaultLocationClient(this, FusedLocationProviderClient(this)),
                                            5L)

        setContent {
            MAD_LocationsTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    val state by viewModel.state.collectAsState()

                    Greeting("Android")

                    Permission(permissions = listOf(android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION),
                        rationale = "Necessary to View the Map with your Location!",
                        permissionNotAvailableContent = {Greeting("MISSING LOCATION PERMISSIONS")},
                        content = {
                            LocationMapView(state.loc, state.locs){ location -> viewModel.saveNewLocation(location) }
                        })

                    LocationMapView(loc = state.loc, locs = state.locs) { location -> viewModel.saveNewLocation(location) }

                }
            }
        }

        initLoc()
    }

    private fun initLoc() {
        Intent(applicationContext, LocationService::class.java).apply {
            action = LocationService.ACTION_START
            startService(this)
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MAD_LocationsTheme {
        Greeting("Android")
    }
}