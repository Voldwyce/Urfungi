package com.example.urfungi

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import kotlinx.coroutines.launch
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.io.IOException
import java.util.*
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.semantics.Role.Companion.Image
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.net.toFile
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import coil.ImageLoader
import coil.compose.rememberImagePainter
import coil.request.ImageRequest
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun SearchScreen() {
    val context = LocalContext.current
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    var location by remember { mutableStateOf<Location?>(null) }

    val locationListener = object : LocationListener {
        override fun onLocationChanged(updatedLocation: Location) {
            location = updatedLocation
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    Box(
        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        MushroomForm(locationManager, onNavigateAway = {})
    }
}

@Composable
fun MushroomForm(
    locationManager: LocationManager,
    onNavigateAway: () -> Unit
) {
    val context = LocalContext.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var mushroomType by remember { mutableStateOf("") }
    var photoFile by remember { mutableStateOf<File?>(null) }
    var flashEnabled by remember { mutableStateOf(false) }
    var cameraInitialized by remember { mutableStateOf(false) }
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    var locationString by remember { mutableStateOf("Ubicación no disponible") }

    var showDialoge by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current

    val dispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    val backCallback = remember {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (title.isBlank() && description.isBlank() && mushroomType.isBlank() && capturedImageUri == null) {
                    // Si todos los campos están vacíos, ejecuta la acción de retroceso directamente
                    this.isEnabled = false
                    dispatcher?.onBackPressed()
                } else {
                    // Si no, muestra el diálogo
                    showDialoge = true
                }
            }
        }
    }

    dispatcher?.addCallback(lifecycleOwner, backCallback)

    if (showDialoge) {
        AlertDialog(
            onDismissRequest = { showDialoge = false },
            title = { Text("Advertencia") },
            text = { Text("Tienes cambios no guardados. Si sales ahora, perderás estos cambios.") },
            confirmButton = {
                Button(onClick = {
                    showDialoge = false
                    backCallback.isEnabled = false // Deshabilita el callback
                    dispatcher?.onBackPressed() // Funciona como el botón de retroceso
                }) {
                    Text("Salir de todos modos")
                }
            },
            dismissButton = {
                Button(onClick = { showDialoge = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (cameraInitialized) {
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView({ previewView }, modifier = Modifier.fillMaxSize()) { view ->
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(view.surfaceProvider)
                }
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                cameraProvider.unbindAll()
                val camera = cameraProvider.bindToLifecycle(
                    context as LifecycleOwner, cameraSelector, preview, imageCapture
                )

                // Activamos el flash
                camera.cameraInfo.hasFlashUnit()?.let { hasFlash ->
                    if (hasFlash) {
                        imageCapture.flashMode =
                            if (flashEnabled) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
                    }
                }
            }

            Button(
                onClick = { cameraInitialized = false },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black.copy(alpha = 0.5f), contentColor = Color.White
                )
            ) {
                Text("X") // Botón de cierre
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { flashEnabled = !flashEnabled },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black.copy(alpha = 0.5f), contentColor = Color.White
                    )
                ) {
                    Text(if (flashEnabled) "Flash On" else "Flash Off") // Botón de flash
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = {
                        var photoFile = createPhotoFile(context)
                        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                        imageCapture.takePicture(
                            outputOptions,
                            ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                    val savedUri =
                                        outputFileResults.savedUri ?: Uri.fromFile(photoFile)
                                    photoFile = savedUri.toFile()
                                    capturedImageUri =
                                        savedUri // Guardar la URI de la imagen capturada
                                    Toast.makeText(
                                        context,
                                        "Fungi capturado!!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    cameraInitialized =
                                        false // Cerrar la cámara después de tomar la foto

                                    // Guardar la imagen en la galería
                                    (context as ComponentActivity).lifecycleScope.launch {
                                        withContext(Dispatchers.IO) {
                                            val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, savedUri)
                                            MediaStore.Images.Media.insertImage(
                                                context.contentResolver,
                                                bitmap,
                                                photoFile.name,
                                                "Foto de hongo"
                                            )
                                        }
                                    }

                                    // Obtener la ubicación
                                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                                        ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                        val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                                        location?.let {
                                            locationString = "${it.latitude}, ${it.longitude}"
                                        }
                                    }
                                }

                                override fun onError(exception: ImageCaptureException) {
                                    Toast.makeText(context, "Error al capturar el fungi: ${exception.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black.copy(alpha = 0.5f), contentColor = Color.White
                    )
                ) {
                    Text("Foto") // Botón de foto
                }
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            capturedImageUri?.let { uri ->
                Image(painter = rememberImagePainter(data = uri), contentDescription = "Captured Image", modifier = Modifier.height(200.dp))
            }
            Spacer(modifier = Modifier.height(16.dp)) // Separación entre la foto y el título
            TextField(value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            // Lista de setas (prueba)
            val mushroomTypes = listOf("Type 1", "Type 2", "Type 3")
            var showDialog by remember { mutableStateOf(false) }
            var selectedTypeIndex by remember { mutableStateOf(0) }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Button(onClick = { showDialog = true },
                    modifier = Modifier
                        .width(150.dp) // Define un ancho fijo
                        .height(50.dp), // Define un alto fijo para hacer el botón cuadrado
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black.copy(alpha = 0.5f), contentColor = Color.White
                    ),
                    shape = RectangleShape // Hace que el botón sea cuadrado sin bordes redondeados
                ) {
                    Text(mushroomType.ifEmpty { "Tipo de seta" })
                }

                Spacer(modifier = Modifier.width(1.dp)) // Reduce aún más el espacio entre los botones

                Button(onClick = {
                    cameraInitialized = true
                }, modifier = Modifier
                    .width(150.dp) // Define un ancho fijo
                    .height(50.dp), // Define un alto fijo para hacer el botón cuadrado
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black.copy(alpha = 0.5f), contentColor = Color.White
                    ),
                    shape = RectangleShape // Hace que el botón sea cuadrado sin bordes redondeados
                ) {
                    Text("Camara")
                }
            }

            if (showDialog) {
                AlertDialog(onDismissRequest = { showDialog = false },
                    title = { Text(text = "Select Mushroom Type") },
                    text = {
                        Column {
                            mushroomTypes.forEachIndexed { index, type ->
                                TextButton(onClick = {
                                    selectedTypeIndex = index
                                    mushroomType = type
                                    showDialog = false
                                }) {
                                    Text(text = type)
                                }
                            }
                        }
                    },
                    confirmButton = { })
            }
            Spacer(modifier = Modifier.height(16.dp))

          /*  TextField(value = locationString,
                onValueChange = {},
                label = { Text("Ubicación") },
                modifier = Modifier.fillMaxWidth(),
                enabled = false // Deshabilitar la edición de este campo
            ) */

            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
            }, modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black.copy(alpha = 0.5f), contentColor = Color.White
                )) {
                Text("Submit")
            }
        }
    }
}

fun createPhotoFile(context: Context): File {
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile(
        "JPEG_${timeStamp}_", /* prefix */
        ".jpg", /* suffix */
        storageDir /* directory */
    )
}


