package com.splitezapp.ui.finances

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.splitezapp.data.api.ApiClient
import com.splitezapp.data.models.*
import com.splitezapp.ui.theme.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancesScreen() {
    var summary by remember { mutableStateOf<FinancialSummary?>(null) }
    var incomes by remember { mutableStateOf<List<Income>>(emptyList()) }
    var personalExpenses by remember { mutableStateOf<List<PersonalExpense>>(emptyList()) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddIncome by remember { mutableStateOf(false) }
    var showAddExpense by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    suspend fun load() {
        val month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
        try { summary = ApiClient.api.getFinancialSummary(month) } catch (_: Exception) {}
        try { incomes = ApiClient.api.getIncome().items } catch (_: Exception) {}
        try { personalExpenses = ApiClient.api.getPersonalExpenses().items } catch (_: Exception) {}
    }

    LaunchedEffect(Unit) { load() }

    Scaffold(
        topBar = { TopAppBar(title = { Text("My Finances") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (selectedTab == 0) showAddIncome = true else showAddExpense = true
            }) { Icon(Icons.Default.Add, "Add") }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Summary card
            summary?.let { s ->
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("This Month", style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Income", style = MaterialTheme.typography.labelSmall)
                                    Text(formatAmount(s.totalIncome), fontWeight = FontWeight.Bold, color = Positive)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Expenses", style = MaterialTheme.typography.labelSmall)
                                    Text(formatAmount(s.totalExpenses), fontWeight = FontWeight.Bold, color = Negative)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Savings", style = MaterialTheme.typography.labelSmall)
                                    Text(formatAmount(kotlin.math.abs(s.netSavings)), fontWeight = FontWeight.Bold,
                                        color = if (s.netSavings >= 0) Positive else Negative)
                                }
                            }
                        }
                    }
                }
            }

            // Tabs
            item {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) { Text("Income", modifier = Modifier.padding(12.dp)) }
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) { Text("Expenses", modifier = Modifier.padding(12.dp)) }
                }
            }

            if (selectedTab == 0) {
                items(incomes, key = { it.id }) { income ->
                    ListItem(
                        headlineContent = { Text(income.type) },
                        supportingContent = { income.note?.let { Text(it) } },
                        trailingContent = { Text(formatAmount(income.amount), color = Positive, fontWeight = FontWeight.Bold) }
                    )
                }
            } else {
                items(personalExpenses, key = { it.id }) { expense ->
                    ListItem(
                        headlineContent = { Text(expense.description) },
                        supportingContent = { expense.category?.let { Text(it) } },
                        trailingContent = { Text(formatAmount(expense.amount), color = Negative, fontWeight = FontWeight.Bold) }
                    )
                }
            }
        }
    }

    if (showAddIncome) {
        AddIncomeDialog(
            onDismiss = { showAddIncome = false },
            onCreated = { scope.launch { showAddIncome = false; load() } }
        )
    }

    if (showAddExpense) {
        AddPersonalExpenseDialog(
            onDismiss = { showAddExpense = false },
            onCreated = { scope.launch { showAddExpense = false; load() } }
        )
    }
}

@Composable
fun AddIncomeDialog(onDismiss: () -> Unit, onCreated: suspend () -> Unit) {
    var amount by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Salary") }
    var note by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val types = listOf("Salary", "Cash", "Pocket Money", "Bonus", "Freelance", "Refund", "Other")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Income") },
        text = {
            Column {
                OutlinedTextField(value = amount, onValueChange = { amount = it },
                    label = { Text("Amount") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                // Simple dropdown for type
                OutlinedTextField(value = type, onValueChange = { type = it },
                    label = { Text("Type (${types.joinToString(", ")})") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = note, onValueChange = { note = it },
                    label = { Text("Note") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                scope.launch {
                    val amt = amount.toDoubleOrNull() ?: return@launch
                    try {
                        ApiClient.api.createIncome(CreateIncomeRequest((amt * 100).toInt(), type, note = note.ifEmpty { null }))
                        onCreated()
                    } catch (_: Exception) {}
                }
            }, enabled = amount.isNotEmpty()) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun AddPersonalExpenseDialog(onDismiss: () -> Unit, onCreated: suspend () -> Unit) {
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
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = amount, onValueChange = { amount = it },
                    label = { Text("Amount") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = category, onValueChange = { category = it },
                    label = { Text("Category") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                scope.launch {
                    val amt = amount.toDoubleOrNull() ?: return@launch
                    try {
                        ApiClient.api.createPersonalExpense(CreatePersonalExpenseRequest(
                            (amt * 100).toInt(), description, category.ifEmpty { null }))
                        onCreated()
                    } catch (_: Exception) {}
                }
            }, enabled = description.isNotEmpty() && amount.isNotEmpty()) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
