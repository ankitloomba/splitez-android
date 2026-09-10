package com.splitezapp.ui.trips

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.splitezapp.data.api.ApiClient
import com.splitezapp.data.models.*
import com.splitezapp.ui.components.AvatarView
import com.splitezapp.ui.components.EmptyState
import com.splitezapp.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(tripId: String, onBack: () -> Unit) {
    var trip by remember { mutableStateOf<Trip?>(null) }
    var expenses by remember { mutableStateOf<List<Expense>>(emptyList()) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddExpense by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(tripId) {
        try {
            trip = ApiClient.api.getTrip(tripId)
            expenses = ApiClient.api.getExpenses(mapOf("tripId" to tripId))
        } catch (_: Exception) {}
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(trip?.name ?: "Trip") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { showAddExpense = true },
                    containerColor = Primary
                ) {
                    Icon(Icons.Default.Add, "Add expense", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Trip header card
            trip?.let { t ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Primary.copy(alpha = 0.1f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Flight, null,
                                tint = Accent,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    t.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                t.destination?.let {
                                    Text(it, style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row {
                            t.startDate?.let { s ->
                                Text("📅 ${s.take(10)}", fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                t.endDate?.let { e ->
                                    Text(" → ${e.take(10)}", fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                            }
                            Spacer(Modifier.weight(1f))
                            Text("${t.memberCount ?: 0} members", fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }
                }
            }

            // Tab row
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 },
                    text = { Text("Expenses") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 },
                    text = { Text("Members") })
            }

            when (selectedTab) {
                0 -> {
                    if (expenses.isEmpty()) {
                        EmptyState("No expenses yet", "💸")
                    } else {
                        val total = expenses.sumOf { it.amount }
                        // Total bar
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Text(
                                "Total: ${formatAmount(total, expenses.firstOrNull()?.currency ?: "INR")}",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                        LazyColumn {
                            items(expenses, key = { it.id }) { exp ->
                                ExpenseRow(exp)
                                HorizontalDivider()
                            }
                        }
                    }
                }
                1 -> {
                    val members = trip?.members ?: emptyList()
                    if (members.isEmpty()) {
                        EmptyState("No members", "👤")
                    } else {
                        LazyColumn {
                            items(members) { member ->
                                ListItem(
                                    headlineContent = { Text(member.displayName) },
                                    leadingContent = { AvatarView(member) }
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddExpense) {
        AddTripExpenseDialog(
            tripId = tripId,
            members = trip?.members ?: emptyList(),
            onDismiss = { showAddExpense = false },
            onCreated = {
                showAddExpense = false
                scope.launch {
                    try { expenses = ApiClient.api.getExpenses(mapOf("tripId" to tripId)) } catch (_: Exception) {}
                }
            }
        )
    }
}

@Composable
private fun ExpenseRow(expense: Expense) {
    ListItem(
        headlineContent = { Text(expense.description) },
        supportingContent = {
            Text(
                buildString {
                    append("Paid by ${expense.paidBy?.displayName ?: "Unknown"}")
                    expense.category?.let { append(" · $it") }
                },
                fontSize = 12.sp
            )
        },
        trailingContent = {
            Text(
                expense.amountFormatted,
                fontWeight = FontWeight.SemiBold,
                color = Primary
            )
        },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                val icon = when (expense.category?.lowercase()) {
                    "food", "dining" -> "🍔"
                    "transport", "travel" -> "🚕"
                    "stay", "hotel" -> "🏨"
                    "shopping" -> "🛍️"
                    "entertainment" -> "🎬"
                    else -> "💰"
                }
                Text(icon, fontSize = 20.sp)
            }
        }
    )
}

@Composable
fun AddTripExpenseDialog(
    tripId: String,
    members: List<UserSummary>,
    onDismiss: () -> Unit,
    onCreated: () -> Unit
) {
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Expense") },
        text = {
            Column {
                OutlinedTextField(value = description, onValueChange = { description = it },
                    label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = amount, onValueChange = { amount = it },
                    label = { Text("Amount (₹)") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = category, onValueChange = { category = it },
                    label = { Text("Category (optional)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    scope.launch {
                        try {
                            val amountPaise = ((amount.toDoubleOrNull() ?: 0.0) * 100).toInt()
                            val participants = members.map { SplitParticipant(it.id) }
                            ApiClient.api.createExpense(
                                CreateExpenseRequest(
                                    description = description,
                                    amount = amountPaise,
                                    tripId = tripId,
                                    category = category.ifEmpty { null },
                                    participants = participants.ifEmpty {
                                        listOf(SplitParticipant("self"))
                                    }
                                )
                            )
                            onCreated()
                        } catch (_: Exception) {}
                    }
                },
                enabled = description.isNotEmpty() && (amount.toDoubleOrNull() ?: 0.0) > 0
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
