import android.Manifest
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

    val mapView = rememberMapViewWithUserLocation(context, coroutineScope)

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

    DisposableEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            coroutineScope.launch {

                // Crea un marcador para el punto específico
                val pointMarker = Marker(mapView).apply {
                    position = GeoPoint(41.5632, 2.0089) // Coordenadas de Terrassa, Barcelona, España
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = "Point Marker"
                }
                mapView.overlays.add(pointMarker)
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

            // Crea un nuevo MyLocationNewOverlay y lo añade al mapa
            val myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), this)
            myLocationOverlay.enableMyLocation()
            overlays.add(myLocationOverlay)

            controller.setZoom(9.0)
        }
    }
}