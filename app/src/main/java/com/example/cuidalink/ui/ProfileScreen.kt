package com.example.cuidalink.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cuidalink.model.ui.PatientProfileUi
import com.example.cuidalink.ui.components.ProfileErrorView
import com.example.cuidalink.ui.components.ShimmerBox
import com.example.cuidalink.ui.icons.HugeIcons
import com.example.cuidalink.ui.theme.CuidaBorderLight
import com.example.cuidalink.ui.theme.CuidaGreen
import com.example.cuidalink.ui.theme.CuidaGreenDark
import com.example.cuidalink.ui.theme.CuidaGreenSurface
import com.example.cuidalink.ui.theme.CuidaRed
import com.example.cuidalink.ui.theme.CuidaRedSurface
import com.example.cuidalink.ui.theme.CuidaSurfaceFaint
import com.example.cuidalink.ui.theme.CuidaTextPrimary
import com.example.cuidalink.ui.theme.CuidaTextSecondary
import com.example.cuidalink.ui.theme.Urbanist
import com.example.cuidalink.viewmodel.PatientProfileViewModel
import com.example.cuidalink.viewmodel.ProfileUiState

private const val FALLBACK = "—"

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onOpenContacts: () -> Unit = {},
    onOpenLinking: () -> Unit = {},
    viewModel: PatientProfileViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CuidaSurfaceFaint)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ProfileTopBar(onBack = onBack)

        when (val current = state) {
            is ProfileUiState.Loading -> ProfileLoading()
            is ProfileUiState.Error -> ProfileErrorView(
                message = current.message,
                onRetry = { viewModel.loadCurrentPatient() }
            )
            is ProfileUiState.Success -> ProfileContent(
                data = current.data,
                onOpenContacts = onOpenContacts,
                onOpenLinking = onOpenLinking
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun ProfileContent(
    data: PatientProfileUi, 
    onOpenContacts: () -> Unit,
    onOpenLinking: () -> Unit
) {
    ProfileIdentity(name = data.name, subtitle = data.email ?: "")
    StatsCardWithPhoto(
        name = data.name,
        age = data.age?.toString() ?: FALLBACK,
        bloodType = data.bloodGroup ?: FALLBACK
    )
    VitalInfoCard(
        allergy = data.allergies ?: "Sin alergias registradas",
        weight = data.weightKg?.let { "${formatNumber(it)} kg" } ?: FALLBACK,
        height = data.heightM?.let { "${formatNumber(it)} m" } ?: FALLBACK
    )
    
    CaretakerInfoCard(
        isLinked = data.isLinked,
        name = data.caretakerName,
        email = data.caretakerEmail,
        onOpenLinking = onOpenLinking
    )

    ContactsButton(onOpenContacts = onOpenContacts)
}

@Composable
private fun CaretakerInfoCard(
    isLinked: Boolean,
    name: String?,
    email: String?,
    onOpenLinking: () -> Unit
) {
    BentoCard(title = "Mi Cuidador") {
        if (isLinked) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(CuidaGreenSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = CuidaGreenDark,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name ?: "Cuidador",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CuidaTextPrimary
                    )
                    Text(
                        text = email ?: FALLBACK,
                        fontSize = 14.sp,
                        color = CuidaTextSecondary
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "No tienes un cuidador vinculado. Por favor, vincula uno.",
                    fontSize = 15.sp,
                    color = CuidaTextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Button(
                    onClick = onOpenLinking,
                    colors = ButtonDefaults.buttonColors(containerColor = CuidaGreen),
                    shape = RoundedCornerShape(percent = 50)
                ) {
                    Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Vincular ahora")
                }
            }
        }
    }
}

@Composable
private fun ProfileLoading() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ShimmerBox(modifier = Modifier.size(width = 200.dp, height = 30.dp))
        ShimmerBox(modifier = Modifier.size(width = 100.dp, height = 18.dp))
    }
    Spacer(modifier = Modifier.height(8.dp))
    ShimmerBox(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        shape = RoundedCornerShape(24.dp)
    )
    repeat(2) {
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
private fun ProfileTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver a inicio",
                tint = CuidaTextPrimary
            )
        }
    }
}

@Composable
private fun ProfileIdentity(name: String, subtitle: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = name,
            fontFamily = Urbanist,
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            color = CuidaTextPrimary
        )
        if (subtitle.isNotBlank()) {
            Text(
                text = subtitle,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = CuidaTextSecondary
            )
        }
    }
}

private val PHOTO_SIZE = 108.dp
private val PHOTO_OVERHANG = 50.dp

@Composable
private fun StatsCardWithPhoto(name: String, age: String, bloodType: String) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = PHOTO_OVERHANG)
                .shadow(4.dp, RoundedCornerShape(24.dp), clip = false)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .padding(top = PHOTO_SIZE - PHOTO_OVERHANG + 20.dp, bottom = 22.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatColumn(
                value = age,
                label = "Años",
                valueColor = CuidaGreen,
                description = "Edad: $age años",
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .height(44.dp)
                    .width(1.dp)
                    .background(CuidaBorderLight)
            )
            StatColumn(
                value = bloodType,
                label = "Grupo Sanguíneo",
                valueColor = CuidaTextPrimary,
                description = "Grupo sanguíneo: $bloodType",
                modifier = Modifier.weight(1f)
            )
        }

        ProfilePhotoWithEdit(name = name)
    }
}

@Composable
private fun ProfilePhotoWithEdit(name: String) {
    var showMenu by remember { mutableStateOf(false) }

    Box(modifier = Modifier.size(PHOTO_SIZE)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .shadow(6.dp, CircleShape)
                .clip(CircleShape)
                .background(Color.White)
                .padding(5.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(CuidaGreenSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = HugeIcons.User,
                    contentDescription = "Foto de perfil de $name",
                    tint = CuidaGreen,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(36.dp)
                .shadow(3.dp, CircleShape)
                .clip(CircleShape)
                .background(CuidaGreen)
                .border(2.dp, Color.White, CircleShape)
                .clickable { showMenu = true }
                .semantics { contentDescription = "Editar perfil" },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            containerColor = Color.White
        ) {
            DropdownMenuItem(
                text = { Text("Editar foto de perfil", fontWeight = FontWeight.SemiBold) },
                onClick = { showMenu = false },
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Person, contentDescription = null, tint = CuidaGreen)
                }
            )
            DropdownMenuItem(
                text = { Text("Editar información del perfil", fontWeight = FontWeight.SemiBold) },
                onClick = { showMenu = false },
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Edit, contentDescription = null, tint = CuidaGreen)
                }
            )
        }
    }
}

@Composable
private fun StatColumn(
    value: String,
    label: String,
    valueColor: Color,
    description: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.semantics { contentDescription = description },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = value,
            fontFamily = Urbanist,
            fontSize = 34.sp,
            fontWeight = FontWeight.ExtraBold,
            color = valueColor
        )
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = CuidaTextSecondary
        )
    }
}

@Composable
private fun VitalInfoCard(allergy: String, weight: String, height: String) {
    BentoCard(title = "Información Vital") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CuidaRedSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = HugeIcons.Heart,
                    contentDescription = null,
                    tint = CuidaRed,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = allergy,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = CuidaRed
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            MeasureItem(label = "Peso", value = weight, modifier = Modifier.weight(1f))
            MeasureItem(label = "Altura", value = height, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun MeasureItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(text = label, fontSize = 13.sp, color = CuidaTextSecondary)
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = CuidaTextPrimary
        )
    }
}

@Composable
private fun ContactsButton(onOpenContacts: () -> Unit) {
    Button(
        onClick = onOpenContacts,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .semantics { contentDescription = "Ver mis contactos" },
        shape = RoundedCornerShape(percent = 50),
        colors = ButtonDefaults.buttonColors(
            containerColor = CuidaGreen,
            contentColor = Color.White
        )
    ) {
        Icon(
            imageVector = HugeIcons.User,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "Ver mis contactos",
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun BentoCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(24.dp), clip = false)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .padding(18.dp)
    ) {
        Text(
            text = title,
            fontFamily = Urbanist,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = CuidaTextPrimary
        )
        Spacer(modifier = Modifier.height(14.dp))
        content()
    }
}

/** Formatea un número: entero si no tiene decimales, si no con un decimal. */
private fun formatNumber(value: Float): String =
    if (value % 1f == 0f) value.toInt().toString() else "%.1f".format(value)
