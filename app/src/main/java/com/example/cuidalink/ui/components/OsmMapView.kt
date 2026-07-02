package com.example.cuidalink.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.config.Configuration
import android.preference.PreferenceManager
import android.graphics.Color as AndroidColor

@Composable
fun OsmMapView(
    modifier: Modifier = Modifier,
    patientLocation: GeoPoint?,
    geofenceLocation: GeoPoint?,
    geofenceRadius: Float?,
    onMapClick: (GeoPoint) -> Unit = {}
) {
    val context = LocalContext.current
    Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(15.0)
        }
    }

    DisposableEffect(mapView) {
        onDispose {
            mapView.onDetach()
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { view ->
            view.overlays.clear()

            // Patient Marker
            patientLocation?.let {
                val marker = Marker(view)
                marker.position = it
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                marker.title = "Paciente"
                view.overlays.add(marker)
                view.controller.animateTo(it)
            }

            // Geofence Circle
            if (geofenceLocation != null && geofenceRadius != null) {
                val circle = Polygon.pointsAsCircle(geofenceLocation, geofenceRadius.toDouble())
                val polygon = Polygon(view)
                polygon.points = circle
                polygon.fillPaint.color = AndroidColor.argb(50, 0, 255, 0)
                polygon.outlinePaint.color = AndroidColor.GREEN
                polygon.outlinePaint.strokeWidth = 2f
                view.overlays.add(polygon)
                
                val centerMarker = Marker(view)
                centerMarker.position = geofenceLocation
                centerMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                centerMarker.title = "Centro de Seguridad"
                view.overlays.add(centerMarker)
            }

            // Map click listener for setting geofence
            val mapEventsOverlay = org.osmdroid.views.overlay.MapEventsOverlay(object : org.osmdroid.events.MapEventsReceiver {
                override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                    onMapClick(p)
                    return true
                }
                override fun longPressHelper(p: GeoPoint): Boolean = false
            })
            view.overlays.add(mapEventsOverlay)

            view.invalidate()
        }
    )
}
