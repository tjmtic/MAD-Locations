package com.abyxcz.mad_locations.components

import android.content.Context
import android.os.Environment
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun CameraPreview() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProvider = remember { mutableStateOf<ProcessCameraProvider?>(null) }

    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(key1 = cameraProvider) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val provider = cameraProviderFuture.get()
            cameraProvider.value = provider

            val previewView = PreviewView(context)
            //val binding = CameraPreviewBinding.bind(previewView)
            val preview = androidx.camera.core.Preview.Builder().build()
            preview.setSurfaceProvider(previewView.surfaceProvider)

            try {
                provider.unbindAll()

                // Select back camera
                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build()

                provider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            onDispose {
                cameraExecutor.shutdown()
            }
        }, context.mainExecutor)

        onDispose {
            cameraExecutor.shutdown()
        }
    }

   // AndroidViewBinding(factory = { context -> PreviewView(context) }, modifier = Modifier.fillMaxSize()) { view ->
    //    CameraPreviewBinding.bind(view)
   // }
}

/*
val imageCapture = ImageCapture.Builder()
    .build()

@Composable
fun SaveImageButton() {
    val context = LocalContext.current
    Button(onClick = {
        // Set up image capture use case
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(getOutputFile(context)).build()
        imageCapture.takePicture(outputFileOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exception: ImageCaptureException) {
                    // Handle error
                    println("Exception in Image: ${exception.message}")
                }

                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    // Image saved successfully
                    println("SAVED IMAGE: ${outputFileResults.savedUri}")
                }
            })
    }) {
        Text("Capture Image")
    }
}

// 4. Save Image
private fun getOutputFile(context: Context): File {
    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile("IMG_", ".jpg", storageDir)
}*/