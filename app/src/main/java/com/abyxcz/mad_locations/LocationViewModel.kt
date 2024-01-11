package com.abyxcz.mad_locations

import android.location.Location
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.abyxcz.data.db.LocationDao
import com.abyxcz.data.entity.LocationEntity
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LocationViewModel(
    private val locDao: LocationDao,
    locationClient: DefaultLocationClient,
    interval: Long): ViewModel() {

    companion object {
        private const val TAG = "TAG-LocationViewModel"
    }

    private var loc: Flow<Location> = locationClient.getLocationUpdates(interval).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = newLocation(),
        )

    private var locs: Flow<List<LocationEntity>> = locDao.observeLocations().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
        )

    private val _state: MutableStateFlow<LocationViewModelState> = MutableStateFlow(LocationViewModelState(loc, locs))
    val state: StateFlow<LocationViewModelState> = _state


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

    }

    fun saveLocation(location: Location){

    }

    fun saveNewLocation(location: Location?){

        location?.let {
            Log.d(TAG, "SAVEING NEW LOCAITON: ${location}")
            viewModelScope.launch {
                val locationEntity = LocationEntity(
                    provider = "Saved Location",
                    id = "",
                    latitude = location.latitude,
                    longitude = location.longitude,
                    locationId = "",
                )
                locDao.insertLocation(locationEntity)
            }
        }
    }

    private fun newLocation(): Location {
        val location = Location("MyLocationProvider")
        location.apply {
            //latitude = singapore.latitude + Random.nextFloat()
            //longitude = singapore.longitude + Random.nextFloat()
        }
        return location
    }
}
data class LocationViewModelState(
    var loc: Flow<Location> = flowOf(),
    var locs: Flow<List<LocationEntity>> = flowOf(),
)