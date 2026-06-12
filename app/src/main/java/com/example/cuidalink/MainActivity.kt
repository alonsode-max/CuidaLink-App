package com.example.cuidalink

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.cuidalink.network.SupabaseConfig
import com.example.cuidalink.ui.CalendarScreen
import com.example.cuidalink.ui.CognitiveCenterScreen
import com.example.cuidalink.ui.ContactsScreen
import com.example.cuidalink.ui.DashboardScreen
import com.example.cuidalink.ui.FloatingNavBar
import com.example.cuidalink.ui.FloatingNavItem
import com.example.cuidalink.ui.GameScreen
import com.example.cuidalink.ui.ProfileScreen
import com.example.cuidalink.ui.theme.CuidaLinkTheme
import com.example.cuidalink.viewmodel.CalendarViewModel
import com.example.cuidalink.viewmodel.GameViewModel
import io.github.jan.supabase.auth.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configuración para mostrar sobre bloqueo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions(),
                onResult = { permissions ->
                    val allGranted = permissions.entries.all { it.value }
                    if (allGranted) {
                        checkSpecialPermissions(context)
                    }
                }
            )

            LaunchedEffect(Unit) {
                val permissionsToRequest = mutableListOf<String>()
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
                // USE_FULL_SCREEN_INTENT no es un permiso de runtime: pedirlo con el
                // diálogo hacía que la solicitud entera se marcara como denegada y
                // bloqueaba la notificación de la alarma. Queda declarado en el manifest.

                if (permissionsToRequest.isNotEmpty()) {
                    permissionLauncher.launch(permissionsToRequest.toTypedArray())
                } else {
                    checkSpecialPermissions(context)
                }
                
                try {
                    SupabaseConfig.client.auth.signInAnonymously()
                } catch (e: Exception) {
                    Log.e("Supabase", "Error: ${e.message}")
                }
            }

            CuidaLinkTheme {
                // ViewModels a nivel de actividad para que su estado se comparta
                // entre pantallas (el dashboard lee los eventos del calendario).
                val gameViewModel: GameViewModel = viewModel()
                val calendarViewModel: CalendarViewModel = viewModel()

                val navController = rememberNavController()
                val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

                // Pestañas principales. El juego de contactos ya no está en
                // la barra: se llega desde el Centro de estimulación cognitiva
                // (pestaña Entrenamiento). Perfil, contactos y el juego se
                // muestran sin barra para evitar distracciones.
                val bottomBarRoutes = setOf("inicio", "entrenamiento", "calendario")

                val navItems = listOf(
                    FloatingNavItem("inicio", "Inicio", Icons.Default.Home),
                    FloatingNavItem("entrenamiento", "Entrenamiento", Icons.Default.Psychology),
                    FloatingNavItem("calendario", "Calendario", Icons.Default.CalendarMonth)
                )

                fun navigateToTab(route: String) {
                    navController.navigate(route) {
                        popUpTo("inicio") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (currentRoute in bottomBarRoutes) {
                            // Barra flotante estilo One UI: píldora con padding
                            // lateral e inferior, despegada de los bordes.
                            FloatingNavBar(
                                items = navItems,
                                currentRoute = currentRoute,
                                onNavigate = { route -> navigateToTab(route) }
                            )
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "inicio",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("inicio") {
                            DashboardScreen(
                                calendarViewModel = calendarViewModel,
                                onOpenCalendar = { navigateToTab("calendario") },
                                onOpenProfile = { navController.navigate("perfil") },
                                onPlay = { navigateToTab("entrenamiento") }
                            )
                        }
                        composable("entrenamiento") {
                            CognitiveCenterScreen(
                                onPlayGame = { navController.navigate("juego") }
                            )
                        }
                        composable("juego") {
                            // Pantalla de juego a foco completo (sin barra),
                            // accesible solo desde el centro de entrenamiento.
                            GameScreen(
                                viewModel = gameViewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("calendario") {
                            CalendarScreen(viewModel = calendarViewModel)
                        }
                        composable("perfil") {
                            ProfileScreen(
                                onBack = { navController.popBackStack() },
                                onOpenContacts = { navController.navigate("contactos") }
                            )
                        }
                        composable("contactos") {
                            ContactsScreen(viewModel = gameViewModel)
                        }
                    }
                }
            }
        }
    }

    private fun checkSpecialPermissions(context: android.content.Context) {
        // Permiso para alarmas exactas (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(AlarmManager::class.java)
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }
        }
    }
}
