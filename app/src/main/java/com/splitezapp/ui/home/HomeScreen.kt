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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // Dark header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkBg)
                    .padding(horizontal = 20.dp)
                    .padding(top = 48.dp, bottom = 32.dp)
            ) {
                // Top bar: logo + icons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        buildAnnotatedString {
                            withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)) {
                                append("Split")
                            }
                            withStyle(SpanStyle(color = PrimaryLight, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)) {
                                append("EZ")
                            }
                        }
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = PrimaryLight)
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = PrimaryLight)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Overall, you are owed",
                    style = MaterialTheme.typography.labelSmall,
                    color = Muted
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        formatAmount(totalOwed),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Positive
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "INR ▾",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Muted,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            }
        }

        // White card area
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-20).dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                // Filter pills
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    filters.forEach { filter ->
                        Text(
                            filter,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = if (filter == activeFilter) Color.White else TextSecondary,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (filter == activeFilter) PillActive else PillInactive)
                                .clickable { activeFilter = filter }
                                .padding(horizontal = 13.dp, vertical = 7.dp)
                        )
                    }
                }

                // Dashboard Elements
                dashboardElements.forEach { element ->
                    DashboardElementCard(element)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Ad banner
                AdBannerSlot(screen = "home", placementName = "home_banner")

                // Groups & trips header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Groups & trips",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "See all",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Primary
                    )
                }

                // Balances
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

                // Recent activity header
                Text(
                    "Recent Activity",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .padding(top = 16.dp, bottom = 8.dp)
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
