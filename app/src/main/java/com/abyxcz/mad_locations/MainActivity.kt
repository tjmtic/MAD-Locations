package com.abyxcz.mad_locations

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.abyxcz.mad_locations.maps.GeofenceScreen
import com.abyxcz.mad_locations.maps.LocationMapView
import com.abyxcz.mad_locations.ui.theme.MAD_LocationsTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "TAG-MainActivity"
    }

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel by viewModels<MainViewModel>()
        lateinit var state : MainViewModelUiState

        lifecycleScope.launch{
            viewModel.state.collect{
                state = it
            }
        }

        setContent {
            MAD_LocationsTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when(state.viewState){
                        is MainViewModelViewState.Default, MainViewModelViewState.Location -> {
                            Permission(permissions = listOf(android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION),
                            rationale = "Necessary to View the Map with your Location!",
                            permissionNotAvailableContent = {Greeting("MISSING LOCATION PERMISSIONS")},
                            content = {
                                LocationMapView()
                            })
                        }
                        is MainViewModelViewState.Geofence -> {
                            GeofenceScreen()
                        }
                    }
                }
            }
        }
    }

    fun initLoc() {
        (application as MainApplication).initLoc()
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