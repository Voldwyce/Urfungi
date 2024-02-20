import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import com.example.urfungi.R
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import android.graphics.Color
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


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
fun MapScreen(ubicacionesRestaurantes: List<Pair<String, String>>) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val coroutineScope = rememberCoroutineScope()
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    val mapView = rememberMapViewWithUserLocation(context, coroutineScope)

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
                val geoPoint = GeoPoint(location.latitude, location.longitude)
                if (mapView.overlays[0] is Marker) {
                    val marker = mapView.overlays[0] as Marker
                    marker.position = geoPoint
                }
                mapView.controller.setCenter(geoPoint)
            }
        } else {
            // Manejar el caso en que el usuario rechaza el permiso
        }
    }
    AndroidView({ mapView }) { mapView ->
        // Actualiza la vista del mapa aquí si es necesario
    }

    // Agregar los marcadores de los restaurantes al mapa cuando el estado de mostrarMarcadores cambie
    if (mostrarMarcadores) {
        restaurantesMarkers = addRestaurantMarkers(mapView, ubicacionesRestaurantes)
    } else {
        restaurantesMarkers?.forEach { mapView.overlays.remove(it) }
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
            coroutineScope.launch {

                // Crea un marcador para el punto específico
                val pointMarker = Marker(mapView).apply {
                    position =
                        GeoPoint(41.5632, 2.0089) // Coordenadas de Terrassa, Barcelona, España
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = "Point Marker"
                }
                mapView.overlays.add(pointMarker)
            }
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        onDispose {
            // No es necesario hacer nada aquí porque el LaunchedEffect se encarga de eliminar la marca
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
            onClick = { mostrarMarcadores = !mostrarMarcadores },
            modifier = Modifier
                .size(120.dp, 50.dp)
        ) {
            Text(if (mostrarMarcadores) "Ocultar Restaurantes" else "Mostrar Restaurantes")
        }
    }
}

private fun addRestaurantMarkers(
    mapView: MapView,
    ubicacionesRestaurantes: List<Pair<String, String>>
): List<Marker> {
    val markers = mutableListOf<Marker>()
    ubicacionesRestaurantes.forEach { ubicacionRestaurante ->
        val marker = Marker(mapView).apply {
            position = GeoPoint(
                ubicacionRestaurante.second.toDouble(),
                ubicacionRestaurante.first.toDouble()
            )
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = "Restaurante"
        }
        mapView.overlays.add(marker)
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

            // Crea un nuevo MyLocationNewOverlay y lo añade al mapa
            val myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), this)
            myLocationOverlay.enableMyLocation()
            overlays.add(myLocationOverlay)

            controller.setZoom(9.0)
        }
    }
}