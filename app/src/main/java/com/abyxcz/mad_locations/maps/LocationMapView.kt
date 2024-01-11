package com.abyxcz.mad_locations.maps

import android.location.Location
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.abyxcz.data.entity.LocationEntity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.LocationSource
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.forEach


private const val TAG = "TAG-LOCATIONMAPVIEW"
private const val zoom = 16f

@Composable
fun LocationMapView(loc : Flow<Location>, locs : Flow<List<LocationEntity>>, saveLocationAction: (Location?) -> Unit) {

    val singapore = LatLng(1.3588227, 103.8742114)
    val defaultCameraPosition = CameraPosition.fromLatLngZoom(singapore, 11f)

    var isMapLoaded by remember { mutableStateOf(false) }

    // To control and observe the map camera
    val cameraPositionState = rememberCameraPositionState {
        position = defaultCameraPosition
    }

    val locationSource = remember { MyLocationSource() }

    // To show blue dot on map
    val mapProperties by remember { mutableStateOf(MapProperties(isMyLocationEnabled = true)) }

    // Collect location updates
    val locationState = loc.collectAsState(initial = newLocation())
    val locationsState = locs.collectAsState(initial = listOf())

    // Update blue dot and camera when the location changes
    LaunchedEffect(locationState.value) {
        Log.d(TAG, "Updating blue dot on map...")
        locationSource.onLocationChanged(locationState.value)

        Log.d(TAG, "Updating camera position...")
        val cameraPosition = CameraPosition.fromLatLngZoom(LatLng(locationState.value.latitude, locationState.value.longitude), zoom)
        //cameraPositionState.animate(CameraUpdateFactory.newCameraPosition(cameraPosition), 1_000)
    }

    // Detect when the map starts moving and print the reason
    LaunchedEffect(cameraPositionState.isMoving) {
        if (cameraPositionState.isMoving) {
            Log.d(TAG, "Map camera started moving due to ${cameraPositionState.cameraMoveStartedReason.name}")
        }
    }

    Box(Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.matchParentSize(),
            cameraPositionState = cameraPositionState,
            onMapLoaded = {
                isMapLoaded = true
            },
            // This listener overrides the behavior for the location button. It is intended to be used when a
            // custom behavior is needed.
            onMyLocationButtonClick = {  Log.d(TAG,"Overriding the onMyLocationButtonClick with this Log"); true },
            locationSource = locationSource,
            properties = mapProperties
        ){
            locationsState.value.forEach {
                Marker(
                state = rememberMarkerState(position = LatLng(it.latitude, it.longitude)),
                title = "Marker at ${it.provider}",
            ) }
        }

        if (!isMapLoaded) {
            AnimatedVisibility(
                modifier = Modifier
                    .matchParentSize(),
                visible = !isMapLoaded,
                enter = EnterTransition.None,
                exit = fadeOut()
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .wrapContentSize()
                )
            }
        }
    }

    Row(modifier = Modifier.wrapContentSize()) {
        Button(onClick = { saveLocationAction(locationSource.curLoc) }, content = {
            Text("Save This Location!")
        })
    }
}

/**
 * A [LocationSource] which allows it's location to be set. In practice you would request location
 * updates (https://developer.android.com/training/location/request-updates).
 */
private class MyLocationSource : LocationSource {

    private var listener: LocationSource.OnLocationChangedListener? = null
    var curLoc: Location? = null

    override fun activate(listener: LocationSource.OnLocationChangedListener) {
        this.listener = listener
    }

    override fun deactivate() {
        listener = null
    }

    fun onLocationChanged(location: Location) {
        listener?.onLocationChanged(location)
        curLoc = location
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