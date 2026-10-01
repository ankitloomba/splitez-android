package com.splitezapp.data.models

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.splitezapp.data.api.ApiClient

object ExpenseStore {
    val expenses: SnapshotStateList<Expense> = mutableStateListOf()
    val balances: SnapshotStateList<Balance> = mutableStateListOf()
    val activities: SnapshotStateList<Activity> = mutableStateListOf()

    suspend fun reload() {
        try {
            val fetched = ApiClient.api.getExpenses()
            if (fetched.isNotEmpty()) {
                expenses.clear(); expenses.addAll(fetched)
            }
        } catch (_: Exception) {}
        try {
            val fetched = ApiClient.api.getBalances()
            if (fetched.isNotEmpty()) {
                balances.clear(); balances.addAll(fetched)
            }
        } catch (_: Exception) {}
        try {
            val page = ApiClient.api.getFeed()
            if (page.data.isNotEmpty()) {
                activities.clear(); activities.addAll(page.data)
            }
        } catch (_: Exception) {}
    }

    fun updateExpense(expense: Expense) {
        val index = expenses.indexOfFirst { it.id == expense.id }
        if (index >= 0) expenses[index] = expense
    }

    fun addExpense(expense: Expense) {
        expenses.add(0, expense)
    }

    fun balanceForUser(userId: String): Int {
        return balances.firstOrNull { it.userId == userId }?.amount ?: 0
    }

    fun recordSettlement(friendId: String, amount: Int) {
        val index = balances.indexOfFirst { it.userId == friendId }
        if (index >= 0) {
            val old = balances[index]
            val newAmount = if (old.amount > 0) maxOf(0, old.amount - amount) else minOf(0, old.amount + amount)
            balances[index] = Balance(userId = old.userId, user = old.user, amount = newAmount)
        }
    }
}
