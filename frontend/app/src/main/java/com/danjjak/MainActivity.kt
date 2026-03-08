package com.danjjak

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.danjjak.ui.dashboard.DashboardScreen
import com.danjjak.ui.timeline.TimelineScreen
import com.danjjak.ui.registration.EventRegistrationScreen
import com.danjjak.ui.theme.DanjjakTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DanjjakTheme {
                var currentScreen by remember { mutableStateOf("dashboard") }

                Scaffold(
                    bottomBar = {
                        NavigationBar(
                            containerColor = Color.White,
                            tonalElevation = 8.dp
                        ) {
                            NavigationBarItem(
                                selected = currentScreen == "dashboard",
                                onClick = { currentScreen = "dashboard" },
                                icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                                label = { Text("대시보드") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF6750A4),
                                    selectedTextColor = Color(0xFF6750A4),
                                    indicatorColor = Color(0xFFEADDFF)
                                )
                            )
                            NavigationBarItem(
                                selected = currentScreen == "registration",
                                onClick = { currentScreen = "registration" },
                                icon = { Icon(Icons.Default.EditNote, contentDescription = "Registration") },
                                label = { Text("기록하기") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF6750A4),
                                    selectedTextColor = Color(0xFF6750A4),
                                    indicatorColor = Color(0xFFEADDFF)
                                )
                            )
                            NavigationBarItem(
                                selected = currentScreen == "timeline",
                                onClick = { currentScreen = "timeline" },
                                icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Timeline") },
                                label = { Text("타임라인") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF6750A4),
                                    selectedTextColor = Color(0xFF6750A4),
                                    indicatorColor = Color(0xFFEADDFF)
                                )
                            )
                        }
                    }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier.padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        when (currentScreen) {
                            "dashboard" -> DashboardScreen()
                            "registration" -> EventRegistrationScreen()
                            "timeline" -> TimelineScreen()
                        }
                    }
                }
            }
        }
    }
}
