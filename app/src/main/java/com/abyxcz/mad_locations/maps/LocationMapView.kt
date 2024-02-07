package com.abyxcz.mad_locations.maps

import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.abyxcz.data.entity.LocationEntity
import com.abyxcz.mad_locations.LocationService
import com.abyxcz.mad_locations.LocationViewModel
import com.abyxcz.mad_locations.MainActivity
import com.abyxcz.mad_locations.geo.CUSTOM_INTENT_GEOFENCE
import com.abyxcz.mad_locations.geo.GeofenceBroadcastReceiver
import com.abyxcz.mad_locations.geo.GeofenceManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.LocationSource
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.launch


private const val TAG = "TAG-LOCATIONMAPVIEW"
private const val zoom = 11f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationMapView(viewModel: LocationViewModel = hiltViewModel()) {

    val c = LocalContext.current as MainActivity
    val scope = rememberCoroutineScope()

    var isMapLoaded by remember { mutableStateOf(false) }

    //TODO: REMOVE THIS. DO NOT DO THIS.
    var isGeoLoaded by remember { mutableStateOf(false) }

    val geofenceManager = remember { GeofenceManager(c) }
    var geofenceTransitionEventInfo by remember {
        mutableStateOf("")
    }

    val state by viewModel.state.collectAsState()

    // To control and observe the map camera
    val singapore = LatLng(34.0, -118.0)
    val defaultCameraPosition = CameraPosition.fromLatLngZoom(singapore, 11f)
    val cameraPositionState = rememberCameraPositionState {
        position = defaultCameraPosition
    }

    //Handler for GoogleMaps location data
    val locationSource = remember { MyLocationSource() }

    // To show blue dot on map
    val mapProperties by remember { mutableStateOf(MapProperties(isMyLocationEnabled = true)) }


    //Initialize Locations and Service
    LaunchedEffect(true) {
        c.initLoc()

        /*state.geos.map {
            geofenceManager.addGeofence(it.key, Location(it.title!!).apply{ latitude = it.latitude
                                                                                longitude = it.longitude}, it.radius, it.expiration)
        }

        geofenceManager.registerGeofence()*/
    }

    // Clean up
    DisposableEffect(LocalLifecycleOwner.current) {
        onDispose {
            scope.launch(Dispatchers.IO) {
                geofenceManager.deregisterGeofence()
            }
        }
    }

    // Register a local broadcast to receive activity transition updates
    GeofenceBroadcastReceiver(systemAction = CUSTOM_INTENT_GEOFENCE) { event ->
        geofenceTransitionEventInfo = event
    }

    // Update blue dot and camera when the location changes
    LaunchedEffect(state) {
        Log.d(TAG, "Updating blue dot on map...")
        locationSource.onLocationChanged(state.loc)

        Log.d(TAG, "Updating camera position...${state.loc}")
        val cameraPosition = CameraPosition.fromLatLngZoom(LatLng(state.loc.latitude, state.loc.longitude), zoom)

        //cameraPositionState.animate(CameraUpdateFactory.newCameraPosition(cameraPosition), 1_000)

        if(state.geos.isNotEmpty() && !isGeoLoaded){
            state.geos.map {
                geofenceManager.addGeofence(it.key, Location(it.title!!).apply{ latitude = it.latitude
                    longitude = it.longitude}, it.radius, it.expiration)
            }

            geofenceManager.registerGeofence()

            isGeoLoaded = true
        }
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
            state.locs.forEach {
                Marker(
                state = rememberMarkerState(position = LatLng(it.latitude, it.longitude)),
                title = it.title,
                snippet = "${it.description} - Marked at ${it.provider}",
            ) }

            state.geos.forEach{
                Circle(
                    center = LatLng(it.latitude, it.longitude),
                    radius = it.radius.toDouble(),
                    strokeColor = androidx.compose.ui.graphics.Color.Blue,
                    fillColor = androidx.compose.ui.graphics.Color.Cyan.copy(alpha = .2f),
                )
            }
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


    var switchState by remember { mutableStateOf(false) }
    var textState by remember { mutableStateOf("")}
    var descriptionState by remember { mutableStateOf("")}

    val bottomSheetState = rememberBottomSheetScaffoldState()
    val coroutineScope = rememberCoroutineScope()



    BottomSheetScaffold(
            sheetContent = {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = switchState,
                            onCheckedChange = { checked ->
                                switchState = checked
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Enable Feature")
                    }

                    // Form fields
                    TextField(
                        value = textState,
                        onValueChange = { newText -> textState = newText },
                        label = { Text("Enter Title") }
                    )

                    TextField(
                        value = descriptionState,
                        onValueChange = { newText -> descriptionState = newText },
                        label = { Text("Enter Description") }
                    )

                    //Attach Closest GEOLOCATION?

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(onClick = {
                            //Save DATA
                            viewModel.saveNewPlace(switchState, state.loc, textState, descriptionState)

                            //Reset FORM
                            textState = ""
                            descriptionState = ""

                            //Reset UI
                            coroutineScope.launch {
                                bottomSheetState.bottomSheetState.partialExpand()
                            }

                        }) {
                            Text("Submit")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            //Reset FORM
                            textState = ""
                            descriptionState = ""

                            //Reset UI
                            coroutineScope.launch {
                                bottomSheetState.bottomSheetState.partialExpand()
                            }
                        }) {
                            Text("Cancel")
                        }
                    }
                }
            },
            scaffoldState = bottomSheetState,

            ) {
            // Content of the main screen
            // This could be a Scaffold, Column, or any other Composable
            // where the user can trigger the bottom sheet

            Row(modifier = Modifier.wrapContentSize()) {
                Button(onClick = {
                    coroutineScope.launch {
                        //viewModel.saveNewGeofence(state.loc, 100000f, 30 * 60 * 1000, "Test 5")
                        bottomSheetState.bottomSheetState.expand()
                    }
                }, content = {
                    Text("Save This Location")

                    //GeofencingControls()
                })
            }
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