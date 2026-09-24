package com.splitezapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val screens = listOf("friends", "groups", "activity", "account")

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
                    Surface(
                        shadowElevation = 8.dp,
                        color = Color.White.copy(alpha = 0.92f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                                .navigationBarsPadding(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            data class TabItem(val icon: androidx.compose.ui.graphics.vector.ImageVector, val label: String, val tag: Int)
                            val tabs = listOf(
                                TabItem(Icons.Default.People, "Friends", 0),
                                TabItem(Icons.Default.Group, "Groups", 1),
                                TabItem(Icons.Default.Notifications, "Activity", 2),
                                TabItem(Icons.Default.AccountCircle, "Account", 3)
                            )
                            tabs.forEach { tab ->
                                val isSelected = selectedTab == tab.tag
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { selectedTab = tab.tag }
                                        .padding(vertical = 4.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .size(width = 56.dp, height = 32.dp)
                                                    .background(
                                                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                                            colors = listOf(
                                                                Color(0xD93366E6),
                                                                Color(0xB34D80FF)
                                                            )
                                                        ),
                                                        shape = RoundedCornerShape(16.dp)
                                                    )
                                            )
                                        }
                                        Icon(
                                            tab.icon, tab.label,
                                            tint = if (isSelected) Color.White else Muted,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        tab.label,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                        color = if (isSelected) Primary else Muted
                                    )
                                }
                            }
                        }
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
                        3 -> AccountScreen(
                            user = authVM.currentUser,
                            onBack = { selectedTab = 0 },
                            onNavigate = { navDest = it },
                            onLogout = { authVM.logout(); navDest = NavDestination.Tabs }
                        )
                    }
                }
            }
        }
    }
}
