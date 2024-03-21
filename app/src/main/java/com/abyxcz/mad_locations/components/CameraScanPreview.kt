package com.abyxcz.mad_locations.components


import android.content.Context
import android.media.Image
import android.os.Environment
import android.util.Log
import android.util.Size
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun CameraScanPreview(
    modifier: Modifier = Modifier,
    scaleType: PreviewView.ScaleType = PreviewView.ScaleType.FILL_CENTER,
    cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
    onHideCamera: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val c2 = LocalContext.current

    val imageCapture = remember { ImageCapture.Builder()
        .build() }

    fun hideCamera(qrCode : String){
        //saveImage(c2)
        onHideCamera(qrCode)
        Log.d("TIME123", "Logging for QR CODE:" + qrCode);
    }
    Column() {
        Button(
            content = { Text("Back") },
            onClick = { hideCamera("none selected") },
            enabled = true
        )
       // SaveImageButton()
       // SaveImageButton()
       // SaveImageButton()
       // SaveImageButton()
        AndroidView(
            modifier = modifier.clickable{ println("CLicked preview");saveImage(imageCapture, c2) },
            factory = { context ->
                val previewView = PreviewView(context).apply {
                    this.scaleType = scaleType
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }

                // Preview
                val previewUseCase = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                //val imageCapture = ImageCapture.Builder()
                //    .build()

                val imageAnalysis = ImageAnalysis.Builder()
                    .setTargetResolution(Size(1280, 720))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                /*imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(context), QRCodeImageAnalyzer(object :
                QRCodeFoundListener {
                override fun onQRCodeFound(_qrCode: String) {
                    //qrCode = _qrCode
                    //qrCodeFoundButton!!.visibility = View.VISIBLE
                    println("QR CODE!!:")
                    println(_qrCode);
                    val codeId = _qrCode.split("id=")
                    println(codeId);
                    hideCamera(codeId[1])
                }

                override fun qrCodeNotFound() {
                    //qrCodeFoundButton!!.visibility = View.INVISIBLE
                    println("no QR CODE!!:...")
                   // hideCamera()
                }
            }))*/

                coroutineScope.launch {
                    val cameraProvider = context.getCameraProvider()
                    try {
                        // Must unbind the use-cases before rebinding them.
                        cameraProvider.unbindAll()

                        cameraProvider.bindToLifecycle(
                            lifecycleOwner, cameraSelector, imageAnalysis, imageCapture, previewUseCase
                        )
                    } catch (ex: Exception) {
                        Log.e("CameraPreview", "Use case binding failed", ex)
                    }
                }

                // Capture image when clicked
                previewView.setOnClickListener {
                    println("CLicked preview")
                    saveImage(imageCapture, context)
                   /* val file = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                        "IMG_${System.currentTimeMillis()}.jpg")

                    val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

                    imageCapture.takePicture(
                        outputOptions, ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                // Image saved successfully
                                Log.d("ImageCapture", "Image saved: ${file.absolutePath}")
                            }

                            override fun onError(exception: ImageCaptureException) {
                                // Error occurred while saving image
                                Log.e("ImageCapture", "Error saving image", exception)
                            }
                        }
                    )*/
                }

                previewView
            }
        )

    }

}

fun saveImage(imageCapture: ImageCapture, context: Context) {
    val file = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
        "IMG_${System.currentTimeMillis()}.jpg")

    val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

    imageCapture.takePicture(
        outputOptions, ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                // Image saved successfully
                Log.d("ImageCapture", "Image saved: ${file.absolutePath}")
            }

            override fun onError(exception: ImageCaptureException) {
                // Error occurred while saving image
                Log.e("ImageCapture", "Error saving image", exception)
            }
        }
    )
}


/*val imageCapture = ImageCapture.Builder()
    .build()

fun saveImage(context: Context) {
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
}*/

@Composable
fun SaveImageButton(imageCapture: ImageCapture) {
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
}