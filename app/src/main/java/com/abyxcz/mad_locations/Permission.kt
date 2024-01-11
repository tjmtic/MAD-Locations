package com.abyxcz.mad_locations

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale


@ExperimentalPermissionsApi
@Composable
fun Permission(
    permissions: List<String> = listOf(android.Manifest.permission.CAMERA),
    rationale: String = "This permission is important for this app. Please grant the permission.",
    permissionNotAvailableContent: @Composable () -> Unit = { },
    content: @Composable () -> Unit = { }
) {
    val permissionsState = rememberMultiplePermissionsState(permissions = permissions)

    LaunchedEffect(key1 = true) {
        permissionsState.launchMultiplePermissionRequest()
    }

    if (permissionsState.allPermissionsGranted) {

        content()
    }

    else {
        permissionsState.permissions.forEach { perm ->
            when {
                perm.status.isGranted -> {
                    // Permission granted, continue
                }
                perm.status.shouldShowRationale -> {
                    // Explain to the user why we need the permissions
                    Rationale(rationale) { permissionsState.launchMultiplePermissionRequest() }
                }
                !perm.status.isGranted -> {
                    // Permission is not granted.
                    // Might want to show a dialog or a message to the user.
                    permissionNotAvailableContent()
                }
            }
        }
    }
}

@Composable
private fun Rationale(
    text: String,
    onRequestPermission: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* Don't */ },
        title = {
            Text(text = "Permission request")
        },
        text = {
            Text(text)
        },
        confirmButton = {
            Button(onClick = onRequestPermission ) {
                Text("Ok")
            }
        }
    )
}