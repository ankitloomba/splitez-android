package com.splitezapp.ui.groups

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
fun GroupDetailScreen(groupId: String, onBack: () -> Unit) {
    var group by remember { mutableStateOf<Group?>(null) }
    var expenses by remember { mutableStateOf<List<Expense>>(emptyList()) }
    var balances by remember { mutableStateOf<List<Balance>>(emptyList()) }
    var debts by remember { mutableStateOf<List<SimplifiedDebt>>(emptyList()) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddExpense by remember { mutableStateOf(false) }
    var showSettle by remember { mutableStateOf<SimplifiedDebt?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(groupId) {
        try {
            group = ApiClient.api.getGroup(groupId)
            expenses = ApiClient.api.getExpenses(mapOf("groupId" to groupId))
            balances = ApiClient.api.getBalances(mapOf("groupId" to groupId))
            debts = ApiClient.api.getSimplifiedDebts(groupId)
        } catch (_: Exception) {}
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(group?.name ?: "Group") },
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
            // Group header
            group?.let { g ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Primary.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Group, null, tint = Primary,
                            modifier = Modifier.size(32.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(g.name, fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium)
                            g.description?.let {
                                Text(it, fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                            }
                            Text("${g.memberCount ?: g.members?.size ?: 0} members",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                    }
                }
            }

            // Tabs
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 },
                    text = { Text("Expenses") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 },
                    text = { Text("Balances") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 },
                    text = { Text("Settle") })
            }

            when (selectedTab) {
                0 -> {
                    if (expenses.isEmpty()) {
                        EmptyState("No expenses yet", "💸")
                    } else {
                        LazyColumn {
                            items(expenses, key = { it.id }) { exp ->
                                ListItem(
                                    headlineContent = { Text(exp.description) },
                                    supportingContent = {
                                        Text("Paid by ${exp.paidBy?.displayName ?: "Unknown"}")
                                    },
                                    trailingContent = {
                                        Text(exp.amountFormatted, fontWeight = FontWeight.SemiBold,
                                            color = Primary)
                                    },
                                    leadingContent = {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Accent.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) { Text("💰", fontSize = 20.sp) }
                                    }
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                }
                1 -> {
                    if (balances.isEmpty()) {
                        EmptyState("All settled up!", "✅")
                    } else {
                        LazyColumn {
                            items(balances) { bal ->
                                ListItem(
                                    headlineContent = {
                                        Text(bal.user?.displayName ?: "Unknown")
                                    },
                                    trailingContent = {
                                        val color = if (bal.amount >= 0) Positive else Negative
                                        Text(
                                            formatAmount(
                                                kotlin.math.abs(bal.amount),
                                                "INR"
                                            ),
                                            color = color,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    },
                                    supportingContent = {
                                        Text(
                                            if (bal.amount >= 0) "gets back" else "owes",
                                            fontSize = 12.sp
                                        )
                                    },
                                    leadingContent = {
                                        bal.user?.let { AvatarView(it) }
                                    }
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                }
                2 -> {
                    if (debts.isEmpty()) {
                        EmptyState("All settled up!", "🤝")
                    } else {
                        LazyColumn {
                            items(debts) { debt ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AvatarView(debt.from, size = 36.dp)
                                        Spacer(Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                "${debt.from.displayName} → ${debt.to.displayName}",
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                formatAmount(debt.amount, "INR"),
                                                fontWeight = FontWeight.Bold,
                                                color = Negative
                                            )
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        FilledTonalButton(
                                            onClick = { showSettle = debt }
                                        ) {
                                            Text("Settle", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddExpense) {
        AddGroupExpenseDialog(
            groupId = groupId,
            members = group?.members ?: emptyList(),
            onDismiss = { showAddExpense = false },
            onCreated = {
                showAddExpense = false
                scope.launch {
                    try {
                        expenses = ApiClient.api.getExpenses(mapOf("groupId" to groupId))
                        balances = ApiClient.api.getBalances(mapOf("groupId" to groupId))
                        debts = ApiClient.api.getSimplifiedDebts(groupId)
                    } catch (_: Exception) {}
                }
            }
        )
    }

    showSettle?.let { debt ->
        SettleUpDialog(
            debt = debt,
            groupId = groupId,
            onDismiss = { showSettle = null },
            onSettled = {
                showSettle = null
                scope.launch {
                    try {
                        balances = ApiClient.api.getBalances(mapOf("groupId" to groupId))
                        debts = ApiClient.api.getSimplifiedDebts(groupId)
                    } catch (_: Exception) {}
                }
            }
        )
    }
}

@Composable
private fun AddGroupExpenseDialog(
    groupId: String,
    members: List<UserSummary>,
    onDismiss: () -> Unit,
    onCreated: () -> Unit
) {
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
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
                                    groupId = groupId,
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

@Composable
private fun SettleUpDialog(
    debt: SimplifiedDebt,
    groupId: String,
    onDismiss: () -> Unit,
    onSettled: () -> Unit
) {
    var note by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Settle Up") },
        text = {
            Column {
                Text(
                    "${debt.from.displayName} pays ${debt.to.displayName}",
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    formatAmount(debt.amount, "INR"),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = note, onValueChange = { note = it },
                    label = { Text("Note (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                scope.launch {
                    try {
                        ApiClient.api.createSettlement(
                            CreateSettlementRequest(
                                toId = debt.to.id,
                                amount = debt.amount,
                                groupId = groupId,
                                note = note.ifEmpty { null }
                            )
                        )
                        onSettled()
                    } catch (_: Exception) {}
                }
            }) { Text("Confirm") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
