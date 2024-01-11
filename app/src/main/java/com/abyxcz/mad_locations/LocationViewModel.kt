package com.abyxcz.mad_locations

import android.location.Location
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.abyxcz.data.dataSource.LocationDataSource
import com.abyxcz.data.dataSource.Result
import com.abyxcz.data.db.LocationDao
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
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(
    private val locDataSource: LocationDataSource,
    locationClient: DefaultLocationClient,
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

    val state: StateFlow<LocationViewModelState> = combine(loc, locs){loc, locs ->
        LocationViewModelState(loc, locs)
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
                locDataSource.saveLocation(locationEntity)
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
    var loc: Location = Location("MyLocationProvider"),
    var locs: List<LocationEntity> = emptyList(),
)