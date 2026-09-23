package com.splitezapp.ui.friends

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.splitezapp.data.models.*
import com.splitezapp.ui.components.AvatarView
import com.splitezapp.ui.theme.*
import kotlin.math.abs

@Composable
fun FriendLedgerScreen(
    friend: Friend,
    onBack: () -> Unit,
    onAddExpense: (Friend) -> Unit,
    onExpenseTap: (Expense) -> Unit,
    onSettings: () -> Unit = {}
) {
    val storeBalance = ExpenseStore.balanceForUser(friend.id)
    val balance = storeBalance

    val expenses = remember(ExpenseStore.expenses.size) {
        val friendExpenses = SampleData.recentExpenses.filter { exp ->
            exp.paidBy?.id == friend.id || exp.createdBy?.id == friend.id
        }.ifEmpty { SampleData.recentExpenses.take(2) }

        val storeExtra = ExpenseStore.expenses.filter { exp ->
            !friendExpenses.any { it.id == exp.id } &&
                (exp.paidBy?.id == friend.id || exp.createdBy?.id == friend.id ||
                    exp.splits?.any { it.userId == friend.id } == true)
        }
        (friendExpenses + storeExtra).sortedByDescending { it.createdAt }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Dark header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkBg)
                    .padding(horizontal = 20.dp)
                    .padding(top = 48.dp, bottom = 24.dp)
            ) {
                // Nav bar
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Default.Settings, "Settings", tint = Color.White)
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Friend info
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AvatarView(friend.toUserSummary(), size = 56.dp)
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(friend.displayName, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        friend.phone?.let {
                            Text(it, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Balance
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        when {
                            balance > 0 -> "Owes you"
                            balance < 0 -> "You owe"
                            else -> "Settled up"
                        },
                        color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        formatAmount(abs(balance)),
                        color = if (balance >= 0) Positive else Negative,
                        fontSize = 32.sp, fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Action buttons
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = {},
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(24.dp)
                    ) { Text("Send reminder") }
                    Button(
                        onClick = {},
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(24.dp)
                    ) { Text("Settle up", color = OnSurface) }
                }
            }

            // Content card
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = Color.White
            ) {
                Column {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Shared expenses", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.weight(1f))
                        TextButton(onClick = {}) {
                            Icon(Icons.Default.Sort, null, tint = Primary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Sort", color = Primary)
                        }
                    }

                    if (expenses.isEmpty()) {
                        Text(
                            "No shared expenses yet",
                            color = TextTertiary, fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 40.dp)
                        )
                    } else {
                        LazyColumn {
                            itemsIndexed(expenses, key = { _, e -> e.id }) { index, expense ->
                                if (index > 0) HorizontalDivider(modifier = Modifier.padding(start = 72.dp))
                                ExpenseRow(
                                    expense = expense,
                                    friend = friend,
                                    onClick = { onExpenseTap(expense) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating + button
        FloatingActionButton(
            onClick = { onAddExpense(friend) },
            containerColor = Primary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 24.dp)
        ) {
            Icon(Icons.Default.Add, "Add expense", tint = Color.White)
        }
    }
}

@Composable
private fun ExpenseRow(expense: Expense, friend: Friend, onClick: () -> Unit) {
    val iconData = iconForCategory(expense.category)
    val myShare = expense.splits?.firstOrNull { it.userId == friend.id }?.shareAmount
        ?: (expense.amount / maxOf(expense.splits?.size ?: 1, 1))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconData.second.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(iconData.first, fontSize = 18.sp)
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(expense.description, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            val payer = expense.paidBy?.displayName ?: "Someone"
            Text(
                "$payer paid ${expense.amountFormatted}",
                fontSize = 12.sp, color = TextSecondary
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            val isPaidByFriend = expense.paidBy?.id == friend.id
            Text(
                if (isPaidByFriend) "you owe" else "owes you",
                fontSize = 12.sp, color = TextSecondary
            )
            Text(
                formatAmount(abs(myShare)),
                fontWeight = FontWeight.Bold, fontSize = 14.sp,
                color = if (isPaidByFriend) Negative else Positive
            )
        }
    }
}

private fun iconForCategory(category: String?): Pair<String, Color> {
    return when (category?.lowercase()) {
        "food", "dining" -> "🍴" to Color(0xFFFF9800)
        "transport", "travel", "cab" -> "🚗" to Color(0xFF6366F1)
        "shopping" -> "🛍" to Color(0xFFE91E63)
        "entertainment" -> "🎬" to Color(0xFF9C27B0)
        "utilities" -> "⚡" to Color(0xFFFFC107)
        "groceries" -> "🛒" to Color(0xFF4CAF50)
        else -> "🍴" to Color(0xFFFF9800)
    }
}
