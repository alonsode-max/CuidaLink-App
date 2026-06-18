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
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cuidalink.ui.CalendarScreen
import com.example.cuidalink.ui.ContactsScreen
import com.example.cuidalink.ui.GameScreen
import com.example.cuidalink.ui.LoginScreen
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
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    // Este permiso suele otorgarse al instalar, pero lo incluimos por seguridad
                    permissionsToRequest.add(Manifest.permission.USE_FULL_SCREEN_INTENT)
                }

                if (permissionsToRequest.isNotEmpty()) {
                    permissionLauncher.launch(permissionsToRequest.toTypedArray())
                } else {
                    checkSpecialPermissions(context)
                }
                
                loginViewModel.checkSession()
            }

            CuidaLinkTheme {
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
                        label = { Text("Salir") },
                        selected = false,
                        onClick = { loginViewModel.logout() }
                    )
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
