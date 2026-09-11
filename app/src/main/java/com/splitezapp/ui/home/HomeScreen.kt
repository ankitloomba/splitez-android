package com.splitezapp.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import com.splitezapp.data.api.ApiClient
import com.splitezapp.data.models.*
import com.splitezapp.ui.components.AvatarView
import com.splitezapp.ui.ads.AdBannerSlot
import com.splitezapp.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(user: UserProfile?) {
    var balances by remember { mutableStateOf<List<Balance>>(emptyList()) }
    var activities by remember { mutableStateOf<List<Activity>>(emptyList()) }
    var dashboardElements by remember { mutableStateOf<List<DashboardElement>>(emptyList()) }
    var activeFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Owed", "You owe", "Settled")

    LaunchedEffect(Unit) {
        try { balances = ApiClient.api.getBalances() } catch (_: Exception) {}
        try { activities = ApiClient.api.getFeed(mapOf("limit" to "10")).items } catch (_: Exception) {}
        try { dashboardElements = ApiClient.api.getDashboardElements("home") } catch (_: Exception) {}
    }

    val totalOwed = balances.filter { it.amount > 0 }.sumOf { it.amount }

    val totalYouOwe = balances.filter { it.amount < 0 }.sumOf { kotlin.math.abs(it.amount) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
    ) {
        // Light header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 48.dp, bottom = 16.dp)
            ) {
                // Top bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Home",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                    )
                    Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Primary)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Balance card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Net Balance",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            formatAmount(totalOwed),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Positive
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            Column {
                                Text("You owe", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                Text(formatAmount(totalYouOwe), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = Negative)
                            }
                            Column {
                                Text("Owed to you", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                Text(formatAmount(totalOwed), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = Positive)
                            }
                        }
                    }
                }
            }
        }

        // Content area
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Surface)
            ) {
                // Dashboard Elements
                dashboardElements.forEach { element ->
                    DashboardElementCard(element)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Ad banner
                AdBannerSlot(screen = "home", placementName = "home_banner")

                // Recent Activity header
                Text(
                    "Recent Activity",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .padding(top = 8.dp, bottom = 8.dp)
                )

                if (activities.isEmpty()) {
                    Text(
                        "No recent activity",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                } else {
                    activities.forEach { activity ->
                        ActivityRow(activity)
                    }
                }

                // Balances header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 16.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Balances",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "See all",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Primary
                    )
                }

                if (balances.isEmpty()) {
                    Text(
                        "No outstanding balances",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(20.dp)
                    )
                } else {
                    balances.forEach { balance ->
                        BalanceRow(balance)
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 70.dp),
                            color = Divider
                        )
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun BalanceRow(balance: Balance) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        balance.user?.let { AvatarView(it, size = 36.dp) }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            balance.user?.displayName ?: "",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            formatAmount(kotlin.math.abs(balance.amount)),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium,
            color = if (balance.amount >= 0) Positive else Negative
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            if (balance.amount >= 0) "owes you" else "you owe",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )
    }
}

@Composable
private fun ActivityRow(activity: Activity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        activity.user?.let { AvatarView(it, size = 32.dp) }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(activityText(activity), style = MaterialTheme.typography.bodyMedium)
            Text(
                activity.createdAt.take(10),
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun DashboardElementCard(element: DashboardElement) {
    val bgColor = when (element.type) {
        "announcement" -> Primary.copy(alpha = 0.1f)
        "tip" -> Color(0xFFFFF3CD)
        "spotlight" -> Accent
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(12.dp)
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
