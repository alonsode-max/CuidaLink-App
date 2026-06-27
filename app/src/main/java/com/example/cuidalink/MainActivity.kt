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
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.example.cuidalink.network.SupabaseConfig
import com.example.cuidalink.ui.AccessibilityScreen
import com.example.cuidalink.ui.CalendarScreen
import com.example.cuidalink.ui.ThemeScreen
import com.example.cuidalink.ui.CognitiveCenterScreen
import com.example.cuidalink.ui.ContactsScreen
import com.example.cuidalink.ui.DashboardScreen
import com.example.cuidalink.ui.EMERGENCY_PHONE
import com.example.cuidalink.ui.EmergencyHelpScreen
import com.example.cuidalink.ui.EmojiPairsGameScreen
import com.example.cuidalink.ui.FAB_DOCK_OFFSET
import com.example.cuidalink.ui.FloatingNavItem
import com.example.cuidalink.ui.GameScreen
import com.example.cuidalink.ui.LoginScreen
import com.example.cuidalink.ui.RegisterScreen
import com.example.cuidalink.ui.SosBottomBar
import com.example.cuidalink.ui.SosFab
import com.example.cuidalink.ui.ProfileScreen
import com.example.cuidalink.ui.SettingsScreen
import com.example.cuidalink.ui.CaregiverBottomBar
import com.example.cuidalink.ui.caregiver.CaregiverDashboardScreen
import com.example.cuidalink.ui.caregiver.CaregiverHistoryScreen
import com.example.cuidalink.ui.caregiver.CaregiverPatientProfileScreen
import com.example.cuidalink.ui.caregiver.CaregiverProfileScreen
import com.example.cuidalink.ui.caregiver.CaregiverSafeZoneScreen
import com.example.cuidalink.ui.icons.HugeIcons
import com.example.cuidalink.ui.theme.CuidaGreen
import com.example.cuidalink.ui.theme.CuidaGreenSurface
import com.example.cuidalink.ui.theme.CuidaGreenSurfaceHover
import com.example.cuidalink.ui.theme.AccessibilityAppearance
import com.example.cuidalink.ui.theme.CuidaLinkTheme
import com.example.cuidalink.viewmodel.AccessibilityViewModel
import com.example.cuidalink.viewmodel.CalendarViewModel
import com.example.cuidalink.viewmodel.ThemeMode
import com.example.cuidalink.viewmodel.GameViewModel
import com.example.cuidalink.viewmodel.LoginState
import com.example.cuidalink.viewmodel.SessionViewModel
import com.example.cuidalink.viewmodel.UserRole
import io.github.jan.supabase.auth.auth
import com.example.cuidalink.ui.theme.CuidaLinkTheme
import com.example.cuidalink.viewmodel.AuthState
import com.example.cuidalink.viewmodel.GameViewModel
import com.example.cuidalink.viewmodel.LoginViewModel

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
            val loginViewModel: LoginViewModel = viewModel()
            val gameViewModel: GameViewModel = viewModel()
            val authState by loginViewModel.authState.collectAsState()
            
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
                // USE_FULL_SCREEN_INTENT no es permiso de runtime; va declarado en el manifest.

                if (permissionsToRequest.isNotEmpty()) {
                    permissionLauncher.launch(permissionsToRequest.toTypedArray())
                } else {
                    checkSpecialPermissions(context)
                }
                
                loginViewModel.checkSession()
            }

            CuidaLinkTheme {
                // ViewModels a nivel de actividad para que su estado se comparta
                val gameViewModel: GameViewModel = viewModel()
                val calendarViewModel: CalendarViewModel = viewModel()
                // Accesibilidad compartida: el ViewModel alimenta la apariencia global.
                val accessibilityViewModel: AccessibilityViewModel = viewModel()
                val accessibilityState by accessibilityViewModel.uiState.collectAsState()
                // Sesión: el rol (paciente/cuidador) decide qué grafo se muestra.
                val sessionViewModel: SessionViewModel = viewModel()
                // Sesión guardada en disco: `null` mientras carga (splash), luego
                val session by sessionViewModel.uiState.collectAsState()
                // El modo "SYSTEM" sigue el tema del dispositivo; los otros dos
                val darkThemeActive = when (accessibilityState.themeMode) {
                    ThemeMode.DARK -> true
                    ThemeMode.LIGHT -> false
                    ThemeMode.SYSTEM -> isSystemInDarkTheme()
                }

                val navController = rememberNavController()
                val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

                // La app arranca en el login. Tras autenticar, se navega al grafo
                fun goToRoleGraph(role: UserRole) {
                    val target = if (role == UserRole.CUIDADOR) "caregiver_graph" else "patient_graph"
                    navController.navigate(target) {
                        popUpTo("login") { inclusive = true }
                        launchSingleTop = true
                    }
                }
                fun logout() {
                    sessionViewModel.logout()
                    navController.navigate("login") {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                }

                // Pestañas principales de navegación.
                val bottomBarRoutes = setOf("inicio", "entrenamiento", "calendario", "ajustes")

                // Accesos repartidos a los lados del recorte del SOS. Perfil
                val leftNavItems = listOf(
                    FloatingNavItem("inicio", "Inicio", HugeIcons.Home),
                    FloatingNavItem("calendario", "Calendario", HugeIcons.Calendar)
                )
                val rightNavItems = listOf(
                    FloatingNavItem("entrenamiento", "Juegos", HugeIcons.Brain),
                    FloatingNavItem("ajustes", "Ajustes", Icons.Filled.Settings)
                )

                fun navigateToTab(route: String) {
                    navController.navigate(route) {
                        popUpTo("inicio") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }

                // Navbar del CUIDADOR (sin SOS): Inicio, Calendario, Zonas y Ajustes.
                val caregiverBarRoutes = setOf("monitoreo", "cuidador_calendario", "zonas", "cuidador_ajustes")
                val caregiverNavItems = listOf(
                    FloatingNavItem("monitoreo", "Inicio", HugeIcons.Home),
                    FloatingNavItem("cuidador_calendario", "Calendario", HugeIcons.Calendar),
                    FloatingNavItem("zonas", "Zonas", Icons.Filled.LocationOn),
                    FloatingNavItem("cuidador_ajustes", "Ajustes", Icons.Filled.Settings)
                )
                fun navigateCaregiverTab(route: String) {
                    navController.navigate(route) {
                        popUpTo("monitoreo") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }

                // Fondo común: degradado verde visible en todas las pantallas
                val backgroundBrush = Brush.verticalGradient(
                    colorStops = arrayOf(
                        0f to CuidaGreenSurfaceHover,
                        0.55f to CuidaGreenSurface,
                        1f to Color.White
                    )
                )

                AccessibilityAppearance(
                    darkTheme = darkThemeActive,
                    highContrast = accessibilityState.isHighContrastEnabled,
                    textScale = accessibilityState.textSizeMultiplier
                ) {
                val loadedSession = session
                if (loadedSession == null) {
                    // Carga inicial: leyendo la sesión guardada. Splash de marca
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(backgroundBrush),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = CuidaGreen)
                    }
                } else {
                // Grafo inicial según la sesión guardada: si ya hay login, va
                val startGraph = if (loadedSession.isLoggedIn) {
                    if (loadedSession.role == UserRole.CUIDADOR) "caregiver_graph" else "patient_graph"
                } else {
                    "login"
                }
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundBrush),
                    containerColor = Color.Transparent,
                    floatingActionButton = {
                        if (currentRoute in bottomBarRoutes) {
                            // SOS centrado y bajado para sobresalir en la barra.
                            SosFab(
                                // Al activar el SOS se abre el modo de auxilio
                                onClick = { navController.navigate("auxilio") },
                                modifier = Modifier.offset(y = FAB_DOCK_OFFSET)
                            )
                        }
                    },
                    floatingActionButtonPosition = FabPosition.Center,
                    bottomBar = {
                        if (currentRoute in bottomBarRoutes) {
                            // Barra blanca con recorte central para el SOS (paciente).
                            SosBottomBar(
                                leftItems = leftNavItems,
                                rightItems = rightNavItems,
                                currentRoute = currentRoute,
                                onNavigate = { route -> navigateToTab(route) }
                            )
                        } else if (currentRoute in caregiverBarRoutes) {
                            // Misma píldora flotante pero sin SOS (cuidador).
                            CaregiverBottomBar(
                                items = caregiverNavItems,
                                currentRoute = currentRoute,
                                onNavigate = { route -> navigateCaregiverTab(route) }
                            )
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = startGraph,
                        // Solo respetamos el inset superior: el contenido se
                        modifier = Modifier.padding(top = innerPadding.calculateTopPadding())
                    ) {
                      // ----- Login (entrada de la app, fuera de los grafos) -----
                      composable("login") {
                          val loginState by sessionViewModel.loginState.collectAsState()
                          LaunchedEffect(loginState) {
                              val current = loginState
                              if (current is LoginState.Success) {
                                  goToRoleGraph(current.role)
                                  sessionViewModel.consumeLogin()
                              }
                          }
                          LoginScreen(
                              onLogin = { email, password -> sessionViewModel.login(email, password) },
                              isLoading = loginState is LoginState.Loading,
                              errorMessage = (loginState as? LoginState.Error)?.message,
                              onNavigateToRegister = { navController.navigate("registro") }
                          )
                      }

                      // ----- Registro (alta de cuenta, fuera de los grafos) -----
                      composable("registro") {
                          RegisterScreen(
                              onBack = { navController.popBackStack() },
                              onRegistered = { navController.popBackStack() }
                          )
                      }

                      // ----- Grafo del PACIENTE (experiencia simple + SOS) -----
                      navigation(startDestination = "inicio", route = "patient_graph") {
                        composable("inicio") {
                            DashboardScreen(
                                calendarViewModel = calendarViewModel,
                                onOpenCalendar = { navigateToTab("calendario") },
                                onPlay = { navigateToTab("entrenamiento") }
                            )
                        }
                        composable("entrenamiento") {
                            CognitiveCenterScreen(
                                onPlayGame = { navController.navigate("juego") },
                                onPlayEmojiPairs = { navController.navigate("juegoParejas") }
                            )
                        }
                        composable("juego") {
                            // Pantalla de juego a foco completo (sin barra),
                            GameScreen(
                                viewModel = gameViewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("juegoParejas") {
                            // Juego de memoria local (Parejas de Emojis).
                            EmojiPairsGameScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("auxilio") {
                            // Modo de auxilio automático "Ayuda en camino"
                            EmergencyHelpScreen(
                                onCall = {
                                    val intent = Intent(
                                        Intent.ACTION_DIAL,
                                        Uri.parse("tel:$EMERGENCY_PHONE")
                                    )
                                    context.startActivity(intent)
                                },
                                onCancel = { navController.popBackStack() }
                            )
                        }
                        composable("calendario") {
                            CalendarScreen(viewModel = calendarViewModel)
                        }
                        composable("ajustes") {
                            // Pantalla de Ajustes (antes "Perfil" en la barra).
                            SettingsScreen(
                                onBack = { navController.popBackStack() },
                                onOpenProfile = { navController.navigate("perfil") },
                                onOpenAccessibility = { navController.navigate("accesibilidad") },
                                onOpenTheme = { navController.navigate("tema") },
                                onLogout = { logout() }
                            )
                        }
                        composable("tema") {
                            // Selector de tema (claro / oscuro / sistema).
                            ThemeScreen(
                                themeMode = accessibilityState.themeMode,
                                onSelectMode = { accessibilityViewModel.setThemeMode(it) },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("accesibilidad") {
                            // Ajustes de accesibilidad (DataStore + ViewModel).
                            AccessibilityScreen(
                                viewModel = accessibilityViewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("perfil") {
                            ProfileScreen(
                                onBack = { navController.popBackStack() },
                                onOpenContacts = { navController.navigate("contactos") }
                            )
                        }
                        composable("contactos") {
                            ContactsScreen(
                                viewModel = gameViewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                      } // fin patient_graph

                      // ----- Grafo del CUIDADOR (panel de monitoreo) -----
                      navigation(startDestination = "monitoreo", route = "caregiver_graph") {
                        composable("monitoreo") {
                            CaregiverDashboardScreen(
                                onOpenHistory = { navController.navigate("cuidador_historial") },
                                onOpenProfile = { navController.navigate("cuidador_perfil") }
                            )
                        }
                        composable("cuidador_calendario") {
                            // Mismo calendario que el paciente (datos compartidos).
                            CalendarScreen(viewModel = calendarViewModel)
                        }
                        composable("zonas") {
                            CaregiverSafeZoneScreen(
                                onBack = { navigateCaregiverTab("monitoreo") }
                            )
                        }
                        composable("cuidador_ajustes") {
                            // Reutiliza la pantalla de ajustes; el logout vive aquí.
                            SettingsScreen(
                                onBack = { navigateCaregiverTab("monitoreo") },
                                onOpenProfile = { navController.navigate("cuidador_mi_perfil") },
                                onOpenAccessibility = { navController.navigate("accesibilidad") },
                                onOpenTheme = { navController.navigate("tema") },
                                onLogout = { logout() }
                            )
                        }
                        composable("cuidador_historial") {
                            CaregiverHistoryScreen(onBack = { navController.popBackStack() })
                        }
                        composable("cuidador_perfil") {
                            CaregiverPatientProfileScreen(onBack = { navController.popBackStack() })
                        }
                        composable("cuidador_mi_perfil") {
                            CaregiverProfileScreen(onBack = { navController.popBackStack() })
                        }
                      } // fin caregiver_graph
                    }
                if (authState is AuthState.Authenticated) {
                    MainContent(gameViewModel, loginViewModel)
                } else {
                    LoginScreen(loginViewModel)
                }
            }
        }
    }

    @Composable
    private fun MainContent(gameViewModel: GameViewModel, loginViewModel: LoginViewModel) {
        var selectedTab by remember { mutableIntStateOf(0) }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Games, contentDescription = null) },
                        label = { Text("Juego") },
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                        label = { Text("Calendario") },
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.ContactPage, contentDescription = null) },
                        label = { Text("Contactos") },
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Logout, contentDescription = null) },
                        label = { Text("Cerrar sesión") },
                        selected = false,
                        onClick = { loginViewModel.logout() }
                    )
                }
                } // fin rama "sesión cargada"
                }
            }
        ) { innerPadding ->
            when (selectedTab) {
                0 -> GameScreen(Modifier.padding(innerPadding), gameViewModel)
                1 -> CalendarScreen(Modifier.padding(innerPadding))
                2 -> ContactsScreen(Modifier.padding(innerPadding), gameViewModel)
            }
        }
    }

    private fun checkSpecialPermissions(context: android.content.Context) {
        // Permiso para alarmas exactas (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(AlarmManager::class.java)
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }
        }
    }
}
