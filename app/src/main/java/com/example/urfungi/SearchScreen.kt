package com.example.urfungi

import android.Manifest
import android.content.ContentValues.TAG
import kotlinx.coroutines.launch
import android.content.Context
import android.content.pm.PackageManager
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
import java.util.*
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.lifecycle.lifecycleScope
import coil.compose.rememberImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.FileInputStream

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
    locationManager: LocationManager, onNavigateAway: () -> Unit
) {

    val context = LocalContext.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }

    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    val storageRef = storage.reference
    val coroutineScope = rememberCoroutineScope()
    val sharedPref = context.getSharedPreferences("MyApp", Context.MODE_PRIVATE)
    val username = sharedPref.getString("username", "NoFoundUser")

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var mushroomType by remember { mutableStateOf("") }
    var photoFile by remember { mutableStateOf<File?>(null) }
    var flashEnabled by remember { mutableStateOf(false) }
    var cameraInitialized by remember { mutableStateOf(false) }
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    var locationString by remember { mutableStateOf("Ubicación no disponible") }
    val mushroomData = remember { mutableStateOf(emptyList<Setas>()) }
    var isUploading by remember { mutableStateOf(false) }
    var selectedMushroom by remember { mutableStateOf<Setas?>(null) }


    LaunchedEffect(Unit) {
        mushroomData.value = fetchMushroomData()
    }

    var showDialog by remember { mutableStateOf(false) }

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
                    showDialog = true
                }
            }
        }
    }

    dispatcher?.addCallback(lifecycleOwner, backCallback)

    if (showDialog) {
        AlertDialog(onDismissRequest = { showDialog = false },
            title = { Text("Advertencia") },
            text = { Text("Tienes cambios no guardados. Si sales ahora, perderás estos cambios.") },
            confirmButton = {
                Button(onClick = {
                    showDialog = false
                    backCallback.isEnabled = false // Deshabilita el callback
                    dispatcher?.onBackPressed() // Funciona como el botón de retroceso
                }) {
                    Text("Salir de todos modos")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Cancelar")
                }
            })
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
                        photoFile = createPhotoFile(context)
                        val outputOptions =
                            ImageCapture.OutputFileOptions.Builder(photoFile!!).build()

                        imageCapture.takePicture(outputOptions,
                            ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                    val savedUri =
                                        outputFileResults.savedUri ?: Uri.fromFile(photoFile)
                                    photoFile = savedUri.toFile()
                                    if (photoFile == null) {
                                        Log.e(TAG, "photoFile es null después de capturar la foto")
                                        return
                                    }
                                    capturedImageUri = savedUri
                                    cameraInitialized = false

                                    // Guardar la imagen en la galería
                                    (context as ComponentActivity).lifecycleScope.launch {
                                        withContext(Dispatchers.IO) {
                                            val bitmap = MediaStore.Images.Media.getBitmap(
                                                context.contentResolver, savedUri
                                            )
                                            MediaStore.Images.Media.insertImage(
                                                context.contentResolver,
                                                bitmap,
                                                photoFile!!.name,
                                                "Foto de hongo"
                                            )
                                        }
                                    }

                                    // Obtener la ubicación
                                    if (ActivityCompat.checkSelfPermission(
                                            context, Manifest.permission.ACCESS_FINE_LOCATION
                                        ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                                            context, Manifest.permission.ACCESS_COARSE_LOCATION
                                        ) == PackageManager.PERMISSION_GRANTED
                                    ) {
                                        val location =
                                            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                                        location?.let {
                                            locationString = "${it.latitude}, ${it.longitude}"
                                        }
                                    }
                                }

                                override fun onError(exception: ImageCaptureException) {
                                    Toast.makeText(
                                        context,
                                        "Error al capturar el fungi: ${exception.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            })
                    }, colors = ButtonDefaults.buttonColors(
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
                Image(
                    painter = rememberImagePainter(data = uri),
                    contentDescription = "Captured Image",
                    modifier = Modifier.height(200.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            var showDialog by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { showDialog = true },
                    modifier = Modifier
                        .width(150.dp)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black.copy(alpha = 0.5f), contentColor = Color.White
                    ),
                    shape = RectangleShape
                ) {
                    Text(mushroomType.ifEmpty { "Tipo de seta" })
                }

                Spacer(modifier = Modifier.width(1.dp))

                Button(
                    onClick = {
                        cameraInitialized = true
                    },
                    modifier = Modifier
                        .width(150.dp)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black.copy(alpha = 0.5f), contentColor = Color.White
                    ),
                    shape = RectangleShape
                ) {
                    Text("Camara")
                }
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Selecciona una Seta") },
                    text = {
                        LazyColumn {
                            items(mushroomData.value) { mushroom ->
                                TextButton(onClick = {
                                    selectedMushroom = mushroom
                                    mushroomType = mushroom.Nombre
                                    showDialog = false

                                    // Imprimir todos los datos de la seta seleccionada
                                    Log.d(TAG, "Selected Mushroom: $mushroom")
                                }) {
                                    Text(text = mushroom.Nombre)
                                }
                            }
                        }
                    },
                    confirmButton = { }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Spacer(modifier = Modifier.height(16.dp))
            // Variables para controlar el diálogo y las estrellas
            var showDialogPost by remember { mutableStateOf(false) }

            Button(
                onClick = {
                    coroutineScope.launch {
                        // Subir la foto a Firebase Storage
                        photoFile?.let { file ->
                            val photoRef = storageRef.child("interno/${file.name}")
                            val photoUri = FileProvider.getUriForFile(
                                context, "${context.packageName}.provider", file
                            )
                            val uploadTask = photoRef.putFile(photoUri)

                            isUploading = true // Mostrar el indicador de carga

                            uploadTask.addOnFailureListener { exception ->
                                // Manejar cualquier error que ocurra durante la subida
                                Log.e(TAG, "Error al subir la imagen: ", exception)
                            }.continueWithTask { task ->
                                if (!task.isSuccessful) {
                                    task.exception?.let {
                                        throw it
                                    }
                                }
                                photoRef.downloadUrl
                            }.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val photoUrl = task.result

                                    // Utilizar el objeto Post existente
                                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                                    val post = Post(
                                        Usuario = userId ?: "NoFoundUser",
                                        idSeta = mushroomType,
                                        Titulo = title,
                                        Descripcion = description,
                                        Cordenadas = locationString,
                                        Fecha = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date()),
                                        Foto = photoUrl.toString()
                                    )

                                    // Subir el objeto Post a Firestore
                                    db.collection("posts").add(post)
                                    isUploading = false // Ocultar el indicador de carga

                                    selectedMushroom?.let { mushroom ->
                                        showDialogPost = true


                                    }
                                } else {
                                    Log.e(TAG, "Error al obtener la URL de la imagen: ", task.exception)
                                }
                            }
                        } ?: run {
                            Log.e(TAG, "photoFile es null")
                        }
                    }
                }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black.copy(alpha = 0.5f), contentColor = Color.White
                )
            ) {
                Text("Submit")
            }

            if (isUploading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            if (showDialogPost) {
                AlertDialog(onDismissRequest = { showDialogPost = false },
                    title = { Text("Fungi capturado!!") },
                    text = {
                        Column {
                            val dificultad = selectedMushroom?.Dificultad?.toInt() ?: 0
                            val estrellasFaltantes = 5 - dificultad
                            Text("Dificultad: ${"★".repeat(dificultad)}${"☆".repeat(estrellasFaltantes)}")
                            Text("Toxicidad: ${getToxicityEmoji(selectedMushroom?.Toxicidad ?: "")}")
                            // Obtener la información de la seta seleccionada desde Firebase
                            selectedMushroom?.let { mushroom ->
                                // Muestra la dificultad y toxicidad de la seta (la toxicidad le añade el emoji)
                                showDialogPost = true

                                // Imprimir los valores de dificultad y toxicidad para verificar
                                Log.d(TAG, "Dificultad: ${mushroom.Dificultad}, Toxicidad: ${mushroom.Toxicidad}")
                            }
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            showDialogPost = false
                            title = ""
                            description = ""
                            selectedMushroom = null
                            photoFile = null
                            capturedImageUri = null
                        }) {
                            Text("OK")
                        }
                    })
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

fun getToxicityEmoji(toxicity: String): String {
    return when (toxicity) {
        "Segura" -> "Segura 😊"
        "Leve" -> "Leve 😐"
        "Peligrosa" -> "Peligrosa 😨"
        "Muy peligrosa" -> " Muy peligrosa 😱"
        "Mortal" -> "Mortal 💀"
        else -> ""
    }
}

private suspend fun fetchMushroomData(): List<Setas> {
    val db = FirebaseFirestore.getInstance()
    return try {
        val documents = db.collection("setas").get().await()

        val mushroomList = documents.mapNotNull { document ->
            val id = document.id
            val nombre = document.getString("Nombre")
            val nombreCientifico = document.getString("NombreCientifico")
            val familia = document.getString("Familia")
            val temporada = document.getString("Temporada")
            val imagen = document.getString("Imagen")
            val comestible = document.getString("Comestible")
            val toxicidad = document.getString("Toxicidad")
            val descripcion = document.getString("Descripcion")
            val habitat = document.getString("Habitat")
            val dificultad = document.getString("Dificultad")
            val curiosidades = document.getString("Curiosidades")

            if (id != null && nombre != null && nombreCientifico != null && familia != null && temporada != null && imagen != null && comestible != null && toxicidad != null && descripcion != null && habitat != null && dificultad != null && curiosidades != null) {
                Setas(
                    id, nombre, nombreCientifico, familia, temporada, imagen, comestible, toxicidad, descripcion, habitat, dificultad, curiosidades
                )
            } else null
        }

        Log.d(TAG, "Mushroom List: $mushroomList")

        mushroomList
    } catch (exception: Exception) {
        Log.w(TAG, "Error getting documents: ", exception)
        emptyList()
    }
}

