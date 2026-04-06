package com.danjjak

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.RamenDining
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import com.danjjak.ui.dashboard.DashboardScreen
import com.danjjak.ui.timeline.TimelineScreen
import com.danjjak.ui.registration.EventRegistrationScreen
import com.danjjak.ui.registration.MealRegistrationScreen
import com.danjjak.ui.auth.LoginScreen
import com.danjjak.ui.auth.ConsentScreen
import com.danjjak.ui.theme.DanjjakTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DanjjakTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "login") {
                    composable("login") {
                        LoginScreen(onLoginSuccess = {
                            navController.navigate("consent") {
                                popUpTo("login") { inclusive = true }
                            }
                        })
                    }
                    composable("consent") {
                        ConsentScreen(onConsentComplete = {
                            navController.navigate("main") {
                                popUpTo("consent") { inclusive = true }
                            }
                        })
                    }
                    composable("main") {
                        // Main contains the Bottom Nav
                        MainScreenLayout()
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreenLayout() {
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "dashboard"

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentRoute == "dashboard",
                    onClick = { 
                        bottomNavController.navigate("dashboard") {
                            popUpTo(bottomNavController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("대시보드") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF6750A4),
                        selectedTextColor = Color(0xFF6750A4),
                        indicatorColor = Color(0xFFEADDFF)
                    )
                )
                NavigationBarItem(
                    selected = currentRoute == "registration",
                    onClick = { 
                        bottomNavController.navigate("registration") {
                            popUpTo(bottomNavController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.EditNote, contentDescription = "Registration") },
                    label = { Text("기록하기") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF6750A4),
                        selectedTextColor = Color(0xFF6750A4),
                        indicatorColor = Color(0xFFEADDFF)
                    )
                )
                NavigationBarItem(
                    selected = currentRoute == "meal",
                    onClick = { 
                        bottomNavController.navigate("meal") {
                            popUpTo(bottomNavController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.RamenDining, contentDescription = "Meal") },
                    label = { Text("식단관리") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF6750A4),
                        selectedTextColor = Color(0xFF6750A4),
                        indicatorColor = Color(0xFFEADDFF)
                    )
                )
                NavigationBarItem(
                    selected = currentRoute == "timeline",
                    onClick = { 
                        bottomNavController.navigate("timeline") {
                            popUpTo(bottomNavController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    },
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
            NavHost(navController = bottomNavController, startDestination = "dashboard") {
                composable("dashboard") { DashboardScreen() }
                composable("registration") { EventRegistrationScreen() }
                composable("meal") { MealRegistrationScreen() }
                composable("timeline") { TimelineScreen() }
            }
        }
    }
}
