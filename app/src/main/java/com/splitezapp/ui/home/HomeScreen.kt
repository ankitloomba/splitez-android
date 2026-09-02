package com.splitezapp.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.splitezapp.data.api.ApiClient
import com.splitezapp.data.models.*
import com.splitezapp.ui.components.AvatarView
import com.splitezapp.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(user: UserProfile?) {
    var balances by remember { mutableStateOf<List<Balance>>(emptyList()) }
    var activities by remember { mutableStateOf<List<Activity>>(emptyList()) }
    var dashboardElements by remember { mutableStateOf<List<DashboardElement>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try { balances = ApiClient.api.getBalances() } catch (_: Exception) {}
        try { activities = ApiClient.api.getFeed(mapOf("limit" to "10")).items } catch (_: Exception) {}
        try { dashboardElements = ApiClient.api.getDashboardElements("home") } catch (_: Exception) {}
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("SplitEZ") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Greeting
            item {
                Text(
                    "Hi, ${user?.firstName ?: "there"}!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            // Dashboard Elements
            if (dashboardElements.isNotEmpty()) {
                items(dashboardElements, key = { it.id }) { element ->
                    DashboardElementCard(element)
                }
            }

            // Balances
            item {
                Text("Balances", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            if (balances.isEmpty()) {
                item {
                    Text("All settled up! 🎉", color = Color.Gray)
                }
            } else {
                items(balances, key = { it.userId }) { balance ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            balance.user?.let { AvatarView(it, size = 36.dp) }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(balance.user?.displayName ?: "", modifier = Modifier.weight(1f))
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    formatAmount(kotlin.math.abs(balance.amount)),
                                    fontWeight = FontWeight.Bold,
                                    color = if (balance.amount >= 0) Positive else Negative
                                )
                                Text(
                                    if (balance.amount >= 0) "owes you" else "you owe",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }

            // Activity
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Recent Activity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            if (activities.isEmpty()) {
                item { Text("No recent activity", color = Color.Gray) }
            } else {
                items(activities, key = { it.id }) { activity ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        activity.user?.let { AvatarView(it, size = 32.dp) }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(activityText(activity), style = MaterialTheme.typography.bodyMedium)
                            Text(activity.createdAt.take(10), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardElementCard(element: DashboardElement) {
    val bgColor = when (element.type) {
        "announcement" -> Primary.copy(alpha = 0.1f)
        "tip" -> Color(0xFFFFF3CD)
        "spotlight" -> Accent.copy(alpha = 0.1f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            element.title?.let {
                Text(
                    it,
                    style = if (element.type == "greeting") MaterialTheme.typography.headlineSmall
                            else MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            element.subtitle?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(it, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
            element.body?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(it, style = MaterialTheme.typography.bodyMedium)
            }
            element.cta?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it, style = MaterialTheme.typography.labelLarge, color = Primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun activityText(a: Activity): String {
    val name = a.user?.firstName ?: "Someone"
    return when (a.type) {
        "EXPENSE_CREATED" -> "$name added an expense"
        "SETTLEMENT_COMPLETED" -> "$name settled up"
        "GROUP_CREATED" -> "$name created a group"
        "TRIP_CREATED" -> "$name created a trip"
        "GROUP_MEMBER_ADDED" -> "$name joined a group"
        "TRIP_MEMBER_ADDED" -> "$name joined a trip"
        else -> "$name did something"
    }
}
