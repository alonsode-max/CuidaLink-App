package com.example.cuidalink.ui

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Abre la ubicación indicada en Google Maps (o el visor de mapas disponible).
 *
 * Usa una URL universal de Google Maps que abre la app si está instalada y, si no,
 * el navegador. Es best-effort: si no hay ninguna app capaz, no hace nada.
 */
fun openInGoogleMaps(context: Context, lat: Double, lng: Double, label: String = "Paciente") {
    // Solo las coordenadas en `query`: si se añade texto (p. ej. el nombre), Google Maps
    // lo trata como búsqueda de dirección y no ubica el punto exacto.
    val uri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng")
    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { context.startActivity(intent) }
}
