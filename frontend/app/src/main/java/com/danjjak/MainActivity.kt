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
import com.danjjak.ui.auth.LoginScreen
import com.danjjak.ui.auth.ConsentScreen
import com.danjjak.ui.theme.DanjjakTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DanjjakTheme {
                var currentNavState by remember { mutableStateOf("login") }
                var currentTab by remember { mutableStateOf("dashboard") }

                when (currentNavState) {
                    "login" -> {
                        LoginScreen(onLoginSuccess = {
                            currentNavState = "consent"
                        })
                    }
                    "consent" -> {
                        ConsentScreen(onConsentComplete = {
                            currentNavState = "main"
                        })
                    }
                    "main" -> {
                        Scaffold(
                            bottomBar = {
                                NavigationBar(
                                    containerColor = Color.White,
                                    tonalElevation = 8.dp
                                ) {
                                    NavigationBarItem(
                                        selected = currentTab == "dashboard",
                                        onClick = { currentTab = "dashboard" },
                                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                                        label = { Text("대시보드") },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = Color(0xFF6750A4),
                                            selectedTextColor = Color(0xFF6750A4),
                                            indicatorColor = Color(0xFFEADDFF)
                                        )
                                    )
                                    NavigationBarItem(
                                        selected = currentTab == "registration",
                                        onClick = { currentTab = "registration" },
                                        icon = { Icon(Icons.Default.EditNote, contentDescription = "Registration") },
                                        label = { Text("기록하기") },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = Color(0xFF6750A4),
                                            selectedTextColor = Color(0xFF6750A4),
                                            indicatorColor = Color(0xFFEADDFF)
                                        )
                                    )
                                    NavigationBarItem(
                                        selected = currentTab == "timeline",
                                        onClick = { currentTab = "timeline" },
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
                                when (currentTab) {
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
    }
}
