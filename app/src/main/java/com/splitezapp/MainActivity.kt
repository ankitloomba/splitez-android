package com.splitezapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.splitezapp.data.api.ApiClient
import com.splitezapp.ui.auth.AuthViewModel
import com.splitezapp.ui.auth.LoginScreen
import com.splitezapp.ui.auth.RegisterScreen
import com.splitezapp.ui.exports.ExportScreen
import com.splitezapp.ui.finances.FinancesScreen
import com.splitezapp.ui.groups.GroupDetailScreen
import com.splitezapp.ui.groups.GroupsScreen
import com.splitezapp.ui.home.HomeScreen
import com.splitezapp.ui.imports.ImportScreen
import com.splitezapp.ui.notifications.NotificationsScreen
import com.splitezapp.ui.settings.SettingsScreen
import com.splitezapp.ui.theme.SplitEZTheme
import com.splitezapp.ui.trips.TripDetailScreen
import com.splitezapp.ui.trips.TripsScreen
import com.splitezapp.data.analytics.AnalyticsTracker
import com.splitezapp.push.PushNotificationService

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApiClient.init(applicationContext)
        AnalyticsTracker.registerInstall(applicationContext)
        PushNotificationService.createNotificationChannel(this)
        PushNotificationService.registerToken(this)
        enableEdgeToEdge()

        setContent {
            SplitEZTheme {
                val authVM: AuthViewModel = viewModel()

                LaunchedEffect(Unit) { authVM.checkAuth() }

                if (authVM.isLoggedIn) {
                    MainScreen(authVM)
                } else {
                    AuthFlow(authVM)
                }
            }
        }
    }
}

@Composable
fun AuthFlow(authVM: AuthViewModel) {
    var showRegister by remember { mutableStateOf(false) }

    if (showRegister) {
        RegisterScreen(authVM, onBack = { showRegister = false })
    } else {
        LoginScreen(authVM, onNavigateToRegister = { showRegister = true })
    }
}

// Simple navigation state
sealed class NavDestination {
    data object Tabs : NavDestination()
    data class GroupDetail(val groupId: String) : NavDestination()
    data class TripDetail(val tripId: String) : NavDestination()
    data object Notifications : NavDestination()
    data object Export : NavDestination()
    data object Import : NavDestination()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(authVM: AuthViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var navDest by remember { mutableStateOf<NavDestination>(NavDestination.Tabs) }
    val screens = listOf("home", "groups", "trips", "finances", "settings")

    LaunchedEffect(Unit) {
        AnalyticsTracker.startSession()
        AnalyticsTracker.trackScreen("home")
    }

    LaunchedEffect(selectedTab) {
        AnalyticsTracker.trackScreen(screens[selectedTab])
    }

    when (val dest = navDest) {
        is NavDestination.GroupDetail -> GroupDetailScreen(
            groupId = dest.groupId,
            onBack = { navDest = NavDestination.Tabs }
        )
        is NavDestination.TripDetail -> TripDetailScreen(
            tripId = dest.tripId,
            onBack = { navDest = NavDestination.Tabs }
        )
        is NavDestination.Notifications -> NotificationsScreen(
            onBack = { navDest = NavDestination.Tabs }
        )
        is NavDestination.Export -> ExportScreen(
            onBack = { navDest = NavDestination.Tabs }
        )
        is NavDestination.Import -> ImportScreen(
            onBack = { navDest = NavDestination.Tabs }
        )
        is NavDestination.Tabs -> {
            Scaffold(
                bottomBar = {
                    NavigationBar {
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            icon = { Icon(Icons.Default.Home, "Home") },
                            label = { Text("Home") }
                        )
                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            icon = { Icon(Icons.Default.Group, "Groups") },
                            label = { Text("Groups") }
                        )
                        NavigationBarItem(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            icon = { Icon(Icons.Default.Flight, "Trips") },
                            label = { Text("Trips") }
                        )
                        NavigationBarItem(
                            selected = selectedTab == 3,
                            onClick = { selectedTab = 3 },
                            icon = { Icon(Icons.Default.BarChart, "Finances") },
                            label = { Text("Finances") }
                        )
                        NavigationBarItem(
                            selected = selectedTab == 4,
                            onClick = { selectedTab = 4 },
                            icon = { Icon(Icons.Default.MoreHoriz, "More") },
                            label = { Text("More") }
                        )
                    }
                }
            ) { padding ->
                when (selectedTab) {
                    0 -> HomeScreen(authVM.currentUser)
                    1 -> GroupsScreen(
                        onGroupTap = { navDest = NavDestination.GroupDetail(it) }
                    )
                    2 -> TripsScreen(
                        onTripTap = { navDest = NavDestination.TripDetail(it) }
                    )
                    3 -> FinancesScreen()
                    4 -> SettingsScreen(
                        user = authVM.currentUser,
                        onLogout = { authVM.logout() },
                        onNavigate = { navDest = it }
                    )
                }
            }
        }
    }
}
