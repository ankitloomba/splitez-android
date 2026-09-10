package com.splitezapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
    var showAddExpense by remember { mutableStateOf(false) }
    val screens = listOf("home", "friends", "add", "activity", "settings")

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
                    Box {
                        NavigationBar(
                            containerColor = com.splitezapp.ui.theme.DarkBg,
                            contentColor = com.splitezapp.ui.theme.Muted
                        ) {
                            NavigationBarItem(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                icon = { Icon(Icons.Default.Home, "Home") },
                                label = { Text("Home") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = com.splitezapp.ui.theme.PrimaryLight,
                                    selectedTextColor = com.splitezapp.ui.theme.PrimaryLight,
                                    unselectedIconColor = com.splitezapp.ui.theme.Muted,
                                    unselectedTextColor = com.splitezapp.ui.theme.Muted,
                                    indicatorColor = Color.Transparent
                                )
                            )
                            NavigationBarItem(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                icon = { Icon(Icons.Default.People, "Friends") },
                                label = { Text("Friends") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = com.splitezapp.ui.theme.PrimaryLight,
                                    selectedTextColor = com.splitezapp.ui.theme.PrimaryLight,
                                    unselectedIconColor = com.splitezapp.ui.theme.Muted,
                                    unselectedTextColor = com.splitezapp.ui.theme.Muted,
                                    indicatorColor = Color.Transparent
                                )
                            )
                            // Spacer for floating add button
                            NavigationBarItem(
                                selected = false,
                                onClick = { showAddExpense = true },
                                icon = { Spacer(modifier = Modifier.size(24.dp)) },
                                label = { Text("") },
                                enabled = false
                            )
                            NavigationBarItem(
                                selected = selectedTab == 3,
                                onClick = { selectedTab = 3 },
                                icon = { Icon(Icons.Default.Notifications, "Activity") },
                                label = { Text("Activity") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = com.splitezapp.ui.theme.PrimaryLight,
                                    selectedTextColor = com.splitezapp.ui.theme.PrimaryLight,
                                    unselectedIconColor = com.splitezapp.ui.theme.Muted,
                                    unselectedTextColor = com.splitezapp.ui.theme.Muted,
                                    indicatorColor = Color.Transparent
                                )
                            )
                            NavigationBarItem(
                                selected = selectedTab == 4,
                                onClick = { selectedTab = 4 },
                                icon = { Icon(Icons.Default.MoreHoriz, "More") },
                                label = { Text("More") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = com.splitezapp.ui.theme.PrimaryLight,
                                    selectedTextColor = com.splitezapp.ui.theme.PrimaryLight,
                                    unselectedIconColor = com.splitezapp.ui.theme.Muted,
                                    unselectedTextColor = com.splitezapp.ui.theme.Muted,
                                    indicatorColor = Color.Transparent
                                )
                            )
                        }
                        // Floating Add button
                        FloatingActionButton(
                            onClick = { showAddExpense = true },
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset(y = (-16).dp)
                                .size(52.dp),
                            shape = CircleShape,
                            containerColor = com.splitezapp.ui.theme.Primary,
                            contentColor = Color.White,
                            elevation = FloatingActionButtonDefaults.elevation(
                                defaultElevation = 6.dp
                            )
                        ) {
                            Icon(Icons.Default.Add, "Add expense", modifier = Modifier.size(28.dp))
                        }
                    }
                }
            ) { padding ->
                when (selectedTab) {
                    0 -> HomeScreen(authVM.currentUser)
                    1 -> GroupsScreen(
                        onGroupTap = { navDest = NavDestination.GroupDetail(it) }
                    )
                    3 -> TripsScreen(
                        onTripTap = { navDest = NavDestination.TripDetail(it) }
                    )
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
