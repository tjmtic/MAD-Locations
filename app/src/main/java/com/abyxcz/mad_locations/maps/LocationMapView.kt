package com.abyxcz.mad_locations.maps

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.abyxcz.data.entity.LocationEntity
import com.abyxcz.mad_locations.LocationService
import com.abyxcz.mad_locations.LocationViewModel
import com.abyxcz.mad_locations.MainActivity
import com.abyxcz.mad_locations.R
import com.abyxcz.mad_locations.components.CameraScanPreview
import com.abyxcz.mad_locations.geo.BgLocationWorker
import com.abyxcz.mad_locations.geo.CUSTOM_INTENT_GEOFENCE
import com.abyxcz.mad_locations.geo.GeofenceBroadcastReceiver
import com.abyxcz.mad_locations.geo.GeofenceManager
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.LocationSource
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.compose.AdvancedMarker
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Objects
import java.util.concurrent.TimeUnit


private const val TAG = "TAG-LOCATIONMAPVIEW"
private const val zoom = 11f


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationMapView(viewModel: LocationViewModel = hiltViewModel()) {

    val c = LocalContext.current as MainActivity

    val workManager = WorkManager.getInstance(c)
    // Observe the worker state to show enable/disable UI
    //val workerState by workManager.getWorkInfosForUniqueWorkLiveData(BgLocationWorker.workName)
     //   .observeAsState()

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

        Log.d("TIME123", "SCHEDULING WORK!")
        // Schedule a periodic worker to check for location every 15 min
        workManager.enqueueUniquePeriodicWork(
            BgLocationWorker.workName,
            ExistingPeriodicWorkPolicy.UPDATE,
            PeriodicWorkRequestBuilder<BgLocationWorker>(
                15,
                TimeUnit.MINUTES,
            ).build()
        )
    }

    // Clean up
    DisposableEffect(LocalLifecycleOwner.current) {
        onDispose {
            scope.launch(Dispatchers.IO) {
                //Remove geofence watchers
                geofenceManager.deregisterGeofence()

                //workManager.cancelUniqueWork(BgLocationWorker.workName)
            }
        }
    }

    // Register a local broadcast to receive activity transition updates
    GeofenceBroadcastReceiver(systemAction = CUSTOM_INTENT_GEOFENCE) { event ->
        geofenceTransitionEventInfo = event
    }

    // Update blue dot and camera when the location changes
    LaunchedEffect(state) {
       // Log.d(TAG, "Updating blue dot on map...")
        locationSource.onLocationChanged(state.loc)

      //  Log.d(TAG, "Updating camera position...${state.loc}")
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

    // Drawing on the map is accomplished with a child-based API
    val markerClick: (Marker) -> Boolean = {
        Log.d(TAG, "${it.title} was clicked")
        cameraPositionState.projection?.let { projection ->
            Log.d(TAG, "The current projection is: $projection")
        }
        false
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
            state.locs.forEach { loc ->
               /* Marker(
                state = rememberMarkerState(position = LatLng(it.latitude, it.longitude)),
                title = it.title,
                snippet = "${it.description} - Marked at ${it.provider}",
            )*/
                val iconState = remember { mutableStateOf<BitmapDescriptor?>(null) }

                LaunchedEffect(key1 = Unit, block = {
                    iconState.value = loadBitmapDescriptorFromUrl(
                        c,
                        loc.image ?: "https://source.unsplash.com/random/128x128/?arches%20national%20park"
                    )
                })

                iconState.value?.let {
                    Marker(state = rememberMarkerState(position = LatLng(loc.latitude, loc.longitude)), icon = it)
                }

                /*MarkerComposable(
                    state = MarkerState(position = LatLng(it.latitude, it.longitude)),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Philippines",
                        tint = androidx.compose.ui.graphics.Color.Blue,
                        modifier = Modifier.size(64.dp)
                    )
                }*/

                /*val textView = TextView(c)
                textView.text = "Hello!!"
                textView.setBackgroundColor(Color.BLACK)
                textView.setTextColor(Color.YELLOW)

                AdvancedMarker(
                    state = rememberMarkerState(position = LatLng(it.latitude, it.longitude)),
                    onClick = markerClick,
                    collisionBehavior = 1,
                    iconView = textView,
                    title="Marker 4"
                )*/

            }

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

     //   CameraScanPreview(onHideCamera = {})
    }


    /////INPUT FORM
    ///////////////

    var switchState by remember { mutableStateOf(false) }
    var textState by remember { mutableStateOf("")}
    var descriptionState by remember { mutableStateOf("")}
    var radiusState by remember { mutableFloatStateOf(100.0f)}

    val bottomSheetState = rememberBottomSheetScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current
    val file = remember { context.createImageFile() }
    val uri = FileProvider.getUriForFile(
        Objects.requireNonNull(context),
        /*BuildConfig.APPLICATION_ID +*/ "test.provider", file
    )

    //OnSave Date Fields
    var capturedImageUri by remember {
        mutableStateOf<Uri>(Uri.EMPTY)
    }

    var capturedLocation by remember {
        mutableStateOf<Location?>(null)
    }

    var cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) {
            if(it){
                capturedImageUri = uri
                capturedLocation = state.loc
            } else println("No Image Captured.")

            //save location fields

        }


    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (it) {
            Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show()
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }




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

                    when(switchState) {
                        // Form fields
                        true -> {
                            TextField(
                                value = textState,
                                onValueChange = { newText -> textState = newText },
                                label = { Text("Enter Place Title") }
                            )

                            TextField(
                                value = descriptionState,
                                onValueChange = { newText -> descriptionState = newText },
                                label = { Text("Enter Place Description") }
                            )



                        }
                        false -> {

                            TextField(
                                value = textState,
                                onValueChange = { newText -> textState = newText },
                                label = { Text("Enter Location Title") }
                            )

                            TextField(
                                value = descriptionState,
                                onValueChange = { newText -> descriptionState = newText },
                                label = { Text("Enter Location Description") }
                            )

                            if (capturedImageUri.path?.isNotEmpty() == true) {
                                //Text("Image at Uri $uri")
                                //Text("Image at CapturedUri $capturedImageUri")
                                Image(
                                    modifier = Modifier
                                        .padding(16.dp, 8.dp),
                                    painter = rememberAsyncImagePainter(capturedImageUri),
                                    contentDescription = null
                                )


                            }

                            else {
                                Text("No Image")
                            }

                        }

                    }

                    //Attach Closest GEOLOCATION?

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(onClick = {
                            //Save DATA
                            viewModel.saveNewPlace(switchState, capturedLocation, textState, descriptionState, capturedImageUri.toString())

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

                       // bottomSheetState.bottomSheetState.expand()

                        launchCamera(context, uri, cameraLauncher, permissionLauncher)

                    }
                }, content = {
                    Text("Save This Location")
                    //SaveImageButton()
                    //CameraScanPreview(onHideCamera = {})

                    //GeofencingControls()
                })
               // CameraScanPreview(onHideCamera = {})
            }
        }

//////////////////




}

fun launchCamera(context: Context, uri: Uri, cameraLauncher: ManagedActivityResultLauncher<Uri, Boolean>, permissionLauncher: ManagedActivityResultLauncher<String, Boolean>){
    val permissionCheckResult =
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
    if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
        println("Launching Camera for $uri")
        cameraLauncher.launch(uri)
    } else {
        // Request a permission
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }
}

fun Context.createImageFile(): File {
    // Create an image file name
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    val imageFileName = "JPEG_" + timeStamp + "_"
    val image = File.createTempFile(
        imageFileName, /* prefix */
        ".jpg", /* suffix */
        externalCacheDir      /* directory */
    )
    return image
}

suspend fun loadBitmapDescriptorFromUrl(context: Context, imageUrl: String): BitmapDescriptor {
    return withContext(Dispatchers.IO) {
        Glide.with(context)
            .asBitmap()
            //.load(new File(imageUrl))    //load local file uri
            .load(imageUrl)
            .circleCrop()
            .submit()
            .get()
    }
        .let { bitmap ->
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 130, 130, false)
            BitmapDescriptorFactory.fromBitmap(resizedBitmap)
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