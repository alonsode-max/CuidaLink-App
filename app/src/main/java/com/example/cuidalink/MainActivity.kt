package com.example.cuidalink

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.Games
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cuidalink.ui.ContactsScreen
import com.example.cuidalink.ui.GameScreen
import com.example.cuidalink.ui.theme.CuidaLinkTheme
import com.example.cuidalink.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CuidaLinkTheme {
                val viewModel: GameViewModel = viewModel()
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
                                icon = { Icon(Icons.Default.ContactPage, contentDescription = null) },
                                label = { Text("Contactos") },
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 }
                            )
                        }
                    }
                ) { innerPadding ->
                    when (selectedTab) {
                        0 -> GameScreen(
                            modifier = Modifier.padding(innerPadding),
                            viewModel = viewModel
                        )
                        1 -> ContactsScreen(
                            modifier = Modifier.padding(innerPadding),
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}
