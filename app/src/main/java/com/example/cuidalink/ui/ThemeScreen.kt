package com.example.cuidalink.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cuidalink.ui.theme.CuidaGreen
import com.example.cuidalink.ui.theme.CuidaSurfaceFaint
import com.example.cuidalink.ui.theme.CuidaTextPrimary
import com.example.cuidalink.ui.theme.CuidaTextSecondary
import com.example.cuidalink.ui.theme.Urbanist
import com.example.cuidalink.viewmodel.ThemeMode

/** Pantalla de seleccion de tema: oscuro, claro o seguir al sistema. */
@Composable
fun ThemeScreen(
    themeMode: ThemeMode,
    onSelectMode: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CuidaSurfaceFaint)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(44.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = CuidaTextPrimary
                )
            }
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "Tema",
                fontFamily = Urbanist,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CuidaTextPrimary
            )
        }

        Spacer(modifier = Modifier.size(8.dp))

        ThemeOptionRow(
            label = "Activado",
            selected = themeMode == ThemeMode.DARK,
            onClick = { onSelectMode(ThemeMode.DARK) }
        )
        ThemeOptionRow(
            label = "Desactivado",
            selected = themeMode == ThemeMode.LIGHT,
            onClick = { onSelectMode(ThemeMode.LIGHT) }
        )
        ThemeOptionRow(
            label = "Predeterminado del sistema",
            selected = themeMode == ThemeMode.SYSTEM,
            onClick = { onSelectMode(ThemeMode.SYSTEM) }
        )

        Text(
            text = "Ajustaremos el aspecto en función de la configuración del sistema del dispositivo.",
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            fontSize = 14.sp,
            color = CuidaTextSecondary
        )
    }
}

@Composable
private fun ThemeOptionRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 18.dp)
            .semantics { contentDescription = label },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = CuidaTextPrimary
        )
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = CuidaGreen,
                unselectedColor = CuidaTextSecondary
            )
        )
    }
}
