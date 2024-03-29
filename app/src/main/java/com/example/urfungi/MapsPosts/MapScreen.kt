package com.example.urfungi.MapsPosts

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.urfungi.Posts.Post
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import com.example.urfungi.R
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay


import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.urfungi.R.drawable.mushroom
import com.example.urfungi.Restaurantes.Restaurantes
import com.google.firebase.firestore.FirebaseFirestore


suspend fun FusedLocationProviderClient.awaitLastLocation(context: Context) =
    suspendCancellableCoroutine<android.location.Location> { continuation ->
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return@suspendCancellableCoroutine
        }
        lastLocation.addOnSuccessListener { location ->
            continuation.resume(location)
        }.addOnFailureListener { e ->
            continuation.resumeWithException(e)
        }
    }

@Composable
fun MapScreen(navController: NavController, lat: Double?, lon: Double?) {
    val latitude = lat
    val longitude = lon

    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val coroutineScope = rememberCoroutineScope()
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    var posts by remember { mutableStateOf(emptyList<Post>()) }

    LaunchedEffect(Unit) {
        posts = loadPosts()
    }

    val mapView = rememberMapViewWithUserLocation(context, coroutineScope)
    DrawMarkers(mapView, posts)

    val db = FirebaseFirestore.getInstance()

    // Estado para controlar la visibilidad de los marcadores
    var mostrarMarcadores by remember { mutableStateOf(true) }

    // Estado para almacenar la referencia a los marcadores de los restaurantes
    var restaurantesMarkers by remember { mutableStateOf<List<Marker>?>(null) }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            coroutineScope.launch {
                val location = fusedLocationClient.awaitLastLocation(context)
                if (location != null && latitude == null && longitude == null) {
                    val geoPoint = GeoPoint(location.latitude, location.longitude)
                    mapView.controller.setCenter(geoPoint)
                }
            }
        } else {
        }
    }


    // Función para mostrar u ocultar los marcadores en el mapa
    fun toggleMarkersVisibility() {
        mostrarMarcadores = !mostrarMarcadores
        if (mostrarMarcadores) {
            restaurantesMarkers?.forEach { mapView.overlays.add(it) }
        } else {
            restaurantesMarkers?.forEach { mapView.overlays.remove(it) }
        }
    }

    AndroidView({ mapView }) { mapView ->
        if (latitude != null && longitude != null) {
            val geoPoint = GeoPoint(latitude, longitude)
            mapView.controller.setCenter(geoPoint)
            mapView.controller.setZoom(15.0)
        } else {
            coroutineScope.launch {
                val location = fusedLocationClient.awaitLastLocation(context)
                if (location != null) {
                    val geoPoint = GeoPoint(location.latitude, location.longitude)
                    mapView.controller.setCenter(geoPoint)
                    mapView.controller.setZoom(15.0)
                }
            }
        }
        Log.d("MapScreen", "Map center: ${mapView.mapCenter.latitude}, ${mapView.mapCenter.longitude}")
        Log.d("MapScreen", "Map zoom level: ${mapView.zoomLevelDouble}")
    }

    DisposableEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Acceder a la colección "restaurantes" de Firebase Firestore
            db.collection("restaurantes")
                .get()
                .addOnSuccessListener { result ->
                    val restaurantes = result.toObjects(Restaurantes::class.java)
                    restaurantesMarkers = addRestaurantMarkers(mapView, restaurantes)
                    // Mostrar u ocultar marcadores dependiendo del estado inicial de mostrarMarcadores
                    toggleMarkersVisibility()
                }
                .addOnFailureListener { exception ->
                    Log.d("MapScreen", "Error getting documents: ", exception)
                }
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        onDispose {
        }
    }

    // Botón para mostrar/ocultar los marcadores
    Box(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        contentAlignment = Alignment.TopEnd
    ) {
        Button(
            onClick = { toggleMarkersVisibility() },
            modifier = Modifier
                .width(150.dp)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black.copy(alpha = 0.5f), contentColor = Color.White
            )
        ) {
            Text(if (mostrarMarcadores) "Ocultar Restaurantes" else "Mostrar Restaurantes")
        }
    }
}

private fun addRestaurantMarkers(
    mapView: MapView,
    restaurantes: List<Restaurantes>
): List<Marker> {
    val markers = mutableListOf<Marker>()
    restaurantes.forEach { restaurante ->
        val marker = Marker(mapView).apply {
            position = GeoPoint(
                restaurante.latitude.toDouble(),
                restaurante.longitude.toDouble()
            )
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = restaurante.NombreRestaurante
        }
        markers.add(marker)
    }
    return markers
}


@Composable
fun rememberMapViewWithUserLocation(
    context: Context,
    coroutineScope: CoroutineScope
): MapView {
    return remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)

            val myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), this)
            myLocationOverlay.enableMyLocation()
            overlays.add(myLocationOverlay)

            setMultiTouchControls(true)
            controller.setZoom(9.0)
        }
    }
}

suspend fun loadPosts(): List<Post> {
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val result = db.collection("posts")
        .whereEqualTo("usuario", userId)
        .get()
        .await()

    val posts = result.documents.mapNotNull { document ->
        val post = document.toObject(Post::class.java)
        post?.id = document.id
        post
    }

    Log.d("loadPosts", "Posts: $posts")

    return posts
}
@SuppressLint("UseCompatLoadingForDrawables")
@Composable
fun DrawMarkers(mapView: MapView, posts: List<Post>) {
    val context = LocalContext.current
    val mushroomDrawable = ContextCompat.getDrawable(context, mushroom)
    if (mushroomDrawable == null) {
        Log.e("DrawMarkers", "Error loading marker icon")
        return
    }

    posts.forEach { post ->
        try {
            val cordenadas = post.cordenadas.split(",")
            val latitud = cordenadas[0].trim().toDouble()
            val longitud = cordenadas[1].trim().toDouble()

            Log.d("DrawMarkers", "Marker coordinates: latitud = $latitud, longitud = $longitud")

            val marker = Marker(mapView).apply {
                position = GeoPoint(latitud, longitud)
                title = post.titulo
                subDescription = "${post.descripcion}\n\n${post.fecha}"
            }

            mapView.overlays.add(marker)

            Log.d("DrawMarkers", "Marker added: ${marker.position.latitude}, ${marker.position.longitude}, ${marker.title}")
        } catch (e: Exception) {
            Log.e("DrawMarkers", "Error creating marker for post: ${post.id}", e)
        }
    }
}