package com.splitezapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.splitezapp.ui.friends.FriendsScreen
import com.splitezapp.ui.groups.GroupDetailScreen
import com.splitezapp.ui.groups.GroupsScreen
import com.splitezapp.ui.imports.ImportScreen
import com.splitezapp.ui.notifications.NotificationsScreen
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
    data object Security : NavDestination()
    data object Notifications : NavDestination()
    data object Export : NavDestination()
    data object Import : NavDestination()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(authVM: AuthViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var navDest by remember { mutableStateOf<NavDestination>(NavDestination.Tabs) }
    var showMore by remember { mutableStateOf(false) }
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
                    }
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
        is NavDestination.Tabs -> {
            Box(modifier = Modifier.fillMaxSize()) {
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
                            NavigationBarItem(
                                selected = false,
                                onClick = { showMore = true },
                                icon = { Icon(Icons.Default.MoreHoriz, "More") },
                                label = { Text("More") },
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
                                }
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

                // More overlay
                if (showMore) {
                    MoreOverlay(
                        user = authVM.currentUser,
                        onDismiss = { showMore = false },
                        onLogout = { authVM.logout(); showMore = false },
                        onNavigate = { dest2 -> showMore = false; navDest = dest2 }
                    )
                }
            }
        }
    }
}

@Composable
private fun MoreOverlay(
    user: UserProfile?,
    onDismiss: () -> Unit,
    onLogout: () -> Unit,
    onNavigate: (NavDestination) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onDismiss)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .clickable(enabled = false, onClick = {}),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Handle
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.LightGray)
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(20.dp))

                // User info
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            user?.firstName?.take(1)?.uppercase() ?: "A",
                            color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            user?.displayName ?: "User",
                            fontWeight = FontWeight.Bold, fontSize = 16.sp
                        )
                        Text(
                            user?.email ?: "",
                            fontSize = 12.sp, color = TextSecondary
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))

                MoreMenuItem(Icons.Default.Notifications, "Notifications") {
                    onNavigate(NavDestination.Notifications)
                }
                MoreMenuItem(Icons.Default.Security, "Security") {
                    onNavigate(NavDestination.Security)
                }
                MoreMenuItem(Icons.Default.FileUpload, "Export data") {
                    onNavigate(NavDestination.Export)
                }
                MoreMenuItem(Icons.Default.FileDownload, "Import data") {
                    onNavigate(NavDestination.Import)
                }

                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))

                MoreMenuItem(Icons.Default.Logout, "Log out", tint = Negative) {
                    onLogout()
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun MoreMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color = OnSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Text(label, fontSize = 15.sp, color = tint)
    }
}
