import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.urfungi.Post
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay




suspend fun FusedLocationProviderClient.awaitLastLocation(context: Context) = suspendCancellableCoroutine<android.location.Location> { continuation ->
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
fun MapScreen() {
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
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            coroutineScope.launch {
                val location = fusedLocationClient.awaitLastLocation(context)
                val geoPoint = GeoPoint(location.latitude, location.longitude)
                mapView.controller.setCenter(geoPoint)
            }
        } else {
            // Manejar el caso en que el usuario rechaza el permiso
        }
    }
    AndroidView({ mapView }) { mapView ->
        // Actualiza la vista del mapa aquí si es necesario
    }

    DisposableEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            coroutineScope.launch {
                val location = fusedLocationClient.awaitLastLocation(context)
                val geoPoint = GeoPoint(location.latitude, location.longitude)
                mapView.controller.setCenter(geoPoint)
            }
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        onDispose { }
    }
}

@Composable
fun rememberMapViewWithUserLocation(
    context: Context,
    coroutineScope: CoroutineScope
): MapView {
    return remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)

            // Create a new MyLocationNewOverlay and add it to the map
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
        post?.id = document.id // Asignar el ID del documento a la propiedad 'id' del post
        post
    }

    // Log the posts
    Log.d("MapScreen", "Posts: $posts")

    return posts
}

@Composable
fun DrawMarkers(mapView: MapView, posts: List<Post>) {
    posts.forEach { post ->
        try {
            val cordenadas = post.cordenadas.split(",")
            val latitud = cordenadas[0].trim().toDouble()
            val longitud = cordenadas[1].trim().toDouble()

            // Log the latitude and longitude values
            Log.d("MapScreen", "Latitud: $latitud, Longitud: $longitud")

            val geoPoint = GeoPoint(latitud, longitud)
            val marker = Marker(mapView).apply {
                position = geoPoint
                title = post.titulo
                subDescription = "${post.fecha}\n${post.descripcion}" // Aquí se muestra la fecha y la descripción
            }
            mapView.overlays.add(marker)

            Log.d("MapScreen", "Marker added: ${marker.position.latitude}, ${marker.position.longitude}, ${marker.title}")
        } catch (e: Exception) {
            // Log the exception
            Log.e("MapScreen", "Error creating marker for post: ${post.id}", e)
        }
    }
}