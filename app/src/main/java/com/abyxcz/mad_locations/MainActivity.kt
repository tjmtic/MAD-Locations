package com.abyxcz.mad_locations

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.abyxcz.data.db.LocationDB
import com.abyxcz.data.db.LocationDao
import com.abyxcz.data.entity.LocationEntity
import com.abyxcz.mad_locations.maps.BasicMapView
import com.abyxcz.mad_locations.maps.LocationMapView
import com.abyxcz.mad_locations.ui.theme.MAD_LocationsTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "TAG-MainActivity"
    }

    private var loc: Flow<Location> = flowOf()
    private lateinit var locDB: LocationDao
    private lateinit var locs: Flow<List<LocationEntity>>

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locDB = (application as MainApplication).getDB().LocationDao()
        locs = locDB.observeLocations()

        setContent {
            MAD_LocationsTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")

                    Permission(permissions = listOf(android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION),
                        rationale = "Necessary to View the Map with your Location!",
                        permissionNotAvailableContent = {Greeting("MISSING LOCATION PERMISSIONS")},
                        content = {
                            LocationMapView(loc, locs){ location -> saveNewLocation(location) }
                        })

                    LocationMapView(loc = loc, locs = locs) { location -> saveNewLocation(location) }

                }
            }
        }

        initLoc()
    }

    fun saveNewLocation(location: Location?){

        location?.let {
            Log.d(TAG, "SAVEING NEW LOCAITON: ${location}")
            lifecycleScope.launch {
                val locationEntity = LocationEntity(
                    provider = "Saved Location",
                    id = "",
                    latitude = location.latitude,
                    longitude = location.longitude,
                    locationId = "",
                )
                locDB.insertLocation(locationEntity)
            }
        }
    }

    private fun initLoc() {

        Intent(applicationContext, LocationService::class.java).apply {
            action = LocationService.ACTION_START
            startService(this)
        }

        val defaultLocationClient : DefaultLocationClient = DefaultLocationClient(this, FusedLocationProviderClient(this))

        loc = defaultLocationClient.getLocationUpdates(5L)

        lifecycleScope.launch{
            loc.collect{
                println("New Location: $it")
            }
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