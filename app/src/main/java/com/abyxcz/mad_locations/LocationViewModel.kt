package com.abyxcz.mad_locations

import android.location.Location
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.abyxcz.data.dataSource.GeofenceDataSource
import com.abyxcz.data.dataSource.LocationDataSource
import com.abyxcz.data.dataSource.Result
import com.abyxcz.data.db.LocationDao
import com.abyxcz.data.entity.GeofenceEntity
import com.abyxcz.data.entity.LocationEntity
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant

@HiltViewModel
class LocationViewModel @Inject constructor(
    locationClient: DefaultLocationClient,
    var geoDataSource: GeofenceDataSource,
    var locDataSource: LocationDataSource
    ): ViewModel() {

    companion object {
        private const val TAG = "TAG-LocationViewModel"
        private const val interval: Long = 5L
    }

    private var loc: Flow<Location> = locationClient.getLocationUpdates(interval).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = newLocation(),
        )

    private var locs: Flow<List<LocationEntity>> = locDataSource.observeLocations().map{
        when(it) {
            is Result.Success -> {it.data}
            else -> {
                emptyList<LocationEntity>()
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
        )

    private var geos: Flow<List<GeofenceEntity>> = geoDataSource.observeGeofences().map{
        when(it) {
            is Result.Success -> {it.data}
            else -> {
                emptyList<GeofenceEntity>()
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val state: StateFlow<LocationViewModelState> = combine(loc, locs, geos){loc, locs, geos ->
        LocationViewModelState(loc, locs, geos)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LocationViewModelState(),
    )

    init {
        viewModelScope.launch{
            loc.collect{
                println("New Location: $it")
            }
        }
    }

    private fun onEvent(){

    }

    fun saveCurrentLocation(){
        /* TODO */
    }

    fun saveLocation(location: Location){
        /* TODO */
    }

    fun saveNewLocation(location: Location?, title: String, description: String?){

        location?.let {
            Log.d(TAG, "SAVEING NEW LOCAITON: ${location}")

            //TODO: Change Datetime implementation with respect to DB
            val dateNow = Clock.System.now().toJavaInstant()

            viewModelScope.launch {
                val locationEntity = LocationEntity(
                    provider = "Manual",
                    id = "", //TODO convert to UUID implementations
                    createdAt = Date.from(dateNow).time,
                    updatedAt = Date.from(dateNow).time,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    locationId = null, //Different from a UUID?
                    title = title,
                    geolocationId = null,
                    description = description,
                    image = null
                )
                locDataSource.saveLocation(locationEntity)
            }
        }
    }

    fun saveNewLocation(location: Location?){
        saveNewLocation(location, "", null)
    }

    fun saveNewGeofence(location: Location?, radius: Float, expiration: Long, key: String){

        location?.let {
            viewModelScope.launch {

                //TODO: Change Datetime implementation with respect to DB
                val dateNow = Clock.System.now().toJavaInstant()

                val geofenceEntity = GeofenceEntity(
                    provider = "Manual",
                    id = "", //TODO convert to UUID implementations
                    createdAt = Date.from(dateNow).time,
                    updatedAt = Date.from(dateNow).time,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    geofenceId = null, //Different from a UUID?
                    title = "title",
                    description = "description",
                    image = null,
                    radius = radius,
                    expiration = expiration,
                    key = key
                )
                geoDataSource.saveGeofence(geofenceEntity)
            }
        }
    }

    private fun newLocation(): Location {
        val location = Location("NewLocationProvider")
        location.apply {
            latitude = 33.0
            longitude = -120.0
        }
        return location
    }
}
data class LocationViewModelState(
    var loc: Location = Location("NewLocationProvider"),
    var locs: List<LocationEntity> = emptyList(),
    var geos: List<GeofenceEntity> = emptyList(),
)