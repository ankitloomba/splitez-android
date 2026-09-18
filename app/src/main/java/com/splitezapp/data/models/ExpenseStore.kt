package com.splitezapp.data.models

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import java.text.SimpleDateFormat
import java.util.*

object ExpenseStore {
    val expenses: SnapshotStateList<Expense> = mutableStateListOf<Expense>().apply {
        addAll(SampleData.recentExpenses)
    }
    val balances: SnapshotStateList<Balance> = mutableStateListOf<Balance>().apply {
        addAll(SampleData.balances)
    }
    val activities: SnapshotStateList<Activity> = mutableStateListOf<Activity>().apply {
        addAll(SampleData.activities)
    }

    fun updateExpense(expense: Expense) {
        val index = expenses.indexOfFirst { it.id == expense.id }
        if (index >= 0) expenses[index] = expense
    }

    fun addExpense(expense: Expense) {
        expenses.add(0, expense)

        val participantIds = expense.splits?.mapNotNull { it.userId } ?: emptyList()
        val paidById = expense.paidBy?.id ?: SampleData.currentUser.id
        val splitCount = maxOf(participantIds.size, 1)
        val perPersonShare = expense.amount / splitCount

        for (pid in participantIds) {
            if (pid == paidById) continue
            if (paidById == SampleData.currentUser.id) {
                updateBalance(pid, perPersonShare)
            } else if (pid == SampleData.currentUser.id) {
                updateBalance(paidById, -perPersonShare)
            }
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val activity = Activity(
            id = "a_${UUID.randomUUID().toString().take(8)}",
            type = "expense_created",
            entityType = "expense",
            entityId = expense.id,
            metadata = mapOf(
                "description" to expense.description,
                "amount" to expense.amount,
                "groupName" to (groupName(expense.groupId) ?: "")
            ),
            user = SampleData.currentUser,
            createdAt = sdf.format(Date())
        )
        activities.add(0, activity)
    }

    fun balanceForUser(userId: String): Int {
        return balances.firstOrNull { it.userId == userId }?.amount ?: 0
    }

    private fun updateBalance(userId: String, delta: Int) {
        val index = balances.indexOfFirst { it.userId == userId }
        if (index >= 0) {
            val old = balances[index]
            balances[index] = Balance(userId = old.userId, user = old.user, amount = old.amount + delta)
        }
    }

    private fun groupName(groupId: String?): String? {
        if (groupId == null) return null
        return SampleData.groups.firstOrNull { it.id == groupId }?.name
    }
}
