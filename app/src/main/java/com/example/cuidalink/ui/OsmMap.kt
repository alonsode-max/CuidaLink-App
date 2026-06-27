package com.example.cuidalink.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polygon

/** Controlador del mapa para botones externos (zoom y centrado). */
class OsmMapController {
    internal var map: MapView? = null

    fun zoomIn() {
        map?.controller?.zoomIn()
    }

    fun zoomOut() {
        map?.controller?.zoomOut()
    }

    fun recenter(point: GeoPoint, zoom: Double) {
        map?.controller?.animateTo(point, zoom, 600L)
    }
}

@Composable
fun rememberOsmMapController(): OsmMapController = remember { OsmMapController() }

/** Mapa OpenStreetMap (osmdroid). No requiere API key ni tarjeta. */
@Composable
fun OsmMap(
    center: GeoPoint,
    zoom: Double,
    modifier: Modifier = Modifier,
    controller: OsmMapController? = null,
    circleRadiusMeters: Double? = null,
    circleStroke: Color = Color(0xFF17A34A),
    circleFill: Color = Color(0x2217A34A)
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember {
        // osmdroid exige un user-agent propio para descargar los tiles de OSM.
        Configuration.getInstance().userAgentValue = context.packageName
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(false)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            // Bloquea el paneo táctil para que el centro siga marcando el punto.
            setOnTouchListener { _, _ -> true }
            this.controller.setZoom(zoom)
            this.controller.setCenter(center)
            controller?.map = this
        }
    }

    // Liga el ciclo de vida del mapa al de la pantalla (osmdroid lo necesita).
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { mapView },
        update = { view ->
            controller?.map = view
            // Refresca el círculo de la zona (si lo hay) sin tocar la cámara.
            view.overlays.removeAll { it is Polygon }
            if (circleRadiusMeters != null && circleRadiusMeters > 0.0) {
                val circle = Polygon().apply {
                    points = Polygon.pointsAsCircle(center, circleRadiusMeters)
                    fillPaint.color = circleFill.toArgb()
                    outlinePaint.color = circleStroke.toArgb()
                    outlinePaint.strokeWidth = 6f
                }
                view.overlays.add(circle)
            }
            view.invalidate()
        }
    )
}
