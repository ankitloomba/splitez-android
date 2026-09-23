package com.splitezapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.splitezapp.data.api.ApiClient
import com.splitezapp.data.models.*
import com.splitezapp.ui.auth.AuthViewModel
import com.splitezapp.ui.auth.LoginScreen
import com.splitezapp.ui.auth.RegisterScreen
import com.splitezapp.ui.activity.ActivityScreen
import com.splitezapp.ui.exports.ExportScreen
import com.splitezapp.ui.expenses.AddExpenseScreen
import com.splitezapp.ui.friends.FriendLedgerScreen
import com.splitezapp.ui.friends.FriendSettingsScreen
import com.splitezapp.ui.friends.FriendsScreen
import com.splitezapp.ui.groups.GroupDetailScreen
import com.splitezapp.ui.groups.GroupsScreen
import com.splitezapp.ui.imports.ImportScreen
import com.splitezapp.ui.notifications.NotificationsScreen
import com.splitezapp.ui.settings.AccountScreen
import com.splitezapp.ui.settings.SecurityScreen
import com.splitezapp.ui.settings.SettingsScreen
import com.splitezapp.ui.theme.*
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

sealed class NavDestination {
    data object Tabs : NavDestination()
    data class GroupDetail(val groupId: String) : NavDestination()
    data class FriendLedger(val friendId: String) : NavDestination()
    data class AddExpense(val prefillFriendId: String? = null, val editExpenseId: String? = null) : NavDestination()
    data object Account : NavDestination()
    data object Settings : NavDestination()
    data object Security : NavDestination()
    data object Notifications : NavDestination()
    data object Export : NavDestination()
    data object Import : NavDestination()
    data class FriendSettings(val friendId: String) : NavDestination()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(authVM: AuthViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var navDest by remember { mutableStateOf<NavDestination>(NavDestination.Tabs) }
    val screens = listOf("friends", "groups", "activity")

    LaunchedEffect(Unit) {
        AnalyticsTracker.startSession()
        AnalyticsTracker.trackScreen("friends")
    }

    LaunchedEffect(selectedTab) {
        if (selectedTab < screens.size) AnalyticsTracker.trackScreen(screens[selectedTab])
    }

    when (val dest = navDest) {
        is NavDestination.GroupDetail -> GroupDetailScreen(
            groupId = dest.groupId,
            onBack = { navDest = NavDestination.Tabs }
        )
        is NavDestination.FriendLedger -> {
            val friend = SampleData.friends.find { it.id == dest.friendId }
            if (friend != null) {
                FriendLedgerScreen(
                    friend = friend,
                    onBack = { navDest = NavDestination.Tabs },
                    onAddExpense = { f ->
                        navDest = NavDestination.AddExpense(prefillFriendId = f.id)
                    },
                    onExpenseTap = { expense ->
                        navDest = NavDestination.AddExpense(editExpenseId = expense.id)
                    },
                    onSettings = { navDest = NavDestination.FriendSettings(friend.id) }
                )
            }
        }
        is NavDestination.AddExpense -> {
            val prefillFriend = dest.prefillFriendId?.let { id ->
                SampleData.friends.find { it.id == id }
            }
            val editExpense = dest.editExpenseId?.let { id ->
                (SampleData.recentExpenses + ExpenseStore.expenses).find { it.id == id }
            }
            AddExpenseScreen(
                onDismiss = { navDest = NavDestination.Tabs },
                prefillFriend = prefillFriend,
                editExpense = editExpense
            )
        }
        is NavDestination.Account -> AccountScreen(
            user = authVM.currentUser,
            onBack = { navDest = NavDestination.Tabs },
            onNavigate = { navDest = it },
            onLogout = { authVM.logout(); navDest = NavDestination.Tabs }
        )
        is NavDestination.Settings -> SettingsScreen(
            user = authVM.currentUser,
            onLogout = { authVM.logout() },
            onNavigate = { navDest = it }
        )
        is NavDestination.Security -> SecurityScreen(
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
        is NavDestination.FriendSettings -> {
            val friend = SampleData.friends.find { it.id == dest.friendId }
            if (friend != null) {
                FriendSettingsScreen(
                    friend = friend,
                    onBack = { navDest = NavDestination.FriendLedger(friend.id) },
                    onGroupTap = { navDest = NavDestination.GroupDetail(it) }
                )
            }
        }
        is NavDestination.Tabs -> {
            Scaffold(
                bottomBar = {
                    NavigationBar(
                        containerColor = Color.White,
                        contentColor = Muted
                    ) {
                        val navColors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Primary,
                            selectedTextColor = Primary,
                            unselectedIconColor = Muted,
                            unselectedTextColor = Muted,
                            indicatorColor = Color.Transparent
                        )
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            icon = { Icon(Icons.Default.People, "Friends") },
                            label = { Text("Friends") },
                            colors = navColors
                        )
                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            icon = { Icon(Icons.Default.Group, "Groups") },
                            label = { Text("Groups") },
                            colors = navColors
                        )
                        NavigationBarItem(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            icon = { Icon(Icons.Default.Notifications, "Activity") },
                            label = { Text("Activity") },
                            colors = navColors
                        )
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { navDest = NavDestination.AddExpense() },
                        containerColor = Primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(Icons.Default.Add, "Add expense", tint = Color.White)
                    }
                }
            ) { padding ->
                Box(modifier = Modifier.padding(padding)) {
                    when (selectedTab) {
                        0 -> FriendsScreen(
                            onFriendTap = { friend ->
                                navDest = NavDestination.FriendLedger(friend.id)
                            },
                            onNotifications = { navDest = NavDestination.Notifications },
                            onMenuNavigate = { navDest = it },
                            onLogout = { authVM.logout() }
                        )
                        1 -> GroupsScreen(
                            onGroupTap = { navDest = NavDestination.GroupDetail(it) }
                        )
                        2 -> ActivityScreen(
                            onActivityTap = { entityId ->
                                val expense = (SampleData.recentExpenses + ExpenseStore.expenses)
                                    .find { it.id == entityId }
                                if (expense != null) {
                                    navDest = NavDestination.AddExpense(editExpenseId = entityId)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
