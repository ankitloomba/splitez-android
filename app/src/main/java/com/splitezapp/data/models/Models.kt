package com.splitezapp.data.models

import com.google.gson.annotations.SerializedName
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

// ── Auth ────────────────────────────────────────────────────────────────
data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
    val user: UserSummary?
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String? = null,
    val phone: String? = null
)

data class LoginRequest(val email: String, val password: String)
data class RefreshRequest(val refreshToken: String)
data class VerifyEmailRequest(val token: String)
data class ForgotPasswordRequest(val email: String)
data class ResetPasswordRequest(val token: String, val password: String)

// ── Users ───────────────────────────────────────────────────────────────
data class UserSummary(
    val id: String,
    val firstName: String,
    val lastName: String? = null,
    val profilePicture: String? = null,
    val avatar: AvatarData? = null
) {
    val displayName: String get() = listOfNotNull(firstName, lastName).joinToString(" ")
}

data class AvatarData(val initials: String, val backgroundColor: String)

data class UserProfile(
    val id: String,
    val email: String?,
    val phone: String?,
    val firstName: String,
    val lastName: String?,
    val profilePicture: String?,
    val avatar: AvatarData?,
    val currency: String,
    val createdAt: String
) {
    val displayName: String get() = listOfNotNull(firstName, lastName).joinToString(" ")
}

data class UpdateUserRequest(
    val firstName: String? = null,
    val lastName: String? = null,
    val currency: String? = null,
    val profilePicture: String? = null
)

// ── Groups ──────────────────────────────────────────────────────────────
data class Group(
    val id: String,
    val name: String,
    val image: String? = null,
    val description: String? = null,
    val memberCount: Int? = null,
    val members: List<UserSummary>? = null,
    val createdAt: String
)

data class CreateGroupRequest(
    val name: String,
    val description: String? = null,
    val image: String? = null,
    val memberIds: List<String>? = null
)

// ── Trips ───────────────────────────────────────────────────────────────
data class Trip(
    val id: String,
    val name: String,
    val destination: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val image: String? = null,
    val memberCount: Int? = null,
    val members: List<UserSummary>? = null,
    val createdAt: String
)

data class CreateTripRequest(
    val name: String,
    val destination: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val memberIds: List<String>? = null
)

// ── Expenses ────────────────────────────────────────────────────────────
data class Expense(
    val id: String,
    val description: String,
    val amount: Int,
    val currency: String = "INR",
    val splitMethod: String,
    val category: String? = null,
    val note: String? = null,
    val date: String,
    val paidBy: UserSummary? = null,
    val createdBy: UserSummary? = null,
    val splits: List<ExpenseSplit>? = null,
    val groupId: String? = null,
    val tripId: String? = null,
    val idempotencyKey: String? = null,
    val createdAt: String
) {
    val amountFormatted: String get() = formatAmount(amount, currency)
}

data class ExpenseSplit(
    val userId: String? = null,
    val user: UserSummary? = null,
    val shareAmount: Int,
    val percentageBps: Int? = null
)

data class SplitParticipant(
    val userId: String,
    val shareAmount: Int? = null,
    val percentageBps: Int? = null
)

data class CreateExpenseRequest(
    val description: String,
    val amount: Int,
    val currency: String? = null,
    val splitMethod: String? = null,
    val category: String? = null,
    val note: String? = null,
    val date: String? = null,
    val paidById: String? = null,
    val groupId: String? = null,
    val tripId: String? = null,
    val participants: List<SplitParticipant>,
    val idempotencyKey: String? = null
)

// ── Balances ────────────────────────────────────────────────────────────
data class Balance(val userId: String, val user: UserSummary?, val amount: Int)
data class SimplifiedDebt(val from: UserSummary, val to: UserSummary, val amount: Int)

// ── Settlements ─────────────────────────────────────────────────────────
data class Settlement(
    val id: String,
    val amount: Int,
    val currency: String = "INR",
    val status: String,
    val groupId: String? = null,
    val note: String? = null,
    val idempotencyKey: String? = null,
    val from: UserSummary? = null,
    val to: UserSummary? = null,
    val createdAt: String,
    val updatedAt: String? = null
) {
    val amountFormatted: String get() = formatAmount(amount, currency)
}

data class CreateSettlementRequest(
    val toId: String,
    val amount: Int,
    val currency: String? = null,
    val groupId: String? = null,
    val note: String? = null,
    val idempotencyKey: String? = null
)

// ── Activity ────────────────────────────────────────────────────────────
data class Activity(
    val id: String,
    val type: String,
    val entityType: String? = null,
    val entityId: String? = null,
    val metadata: Map<String, Any>? = null,
    val user: UserSummary? = null,
    val createdAt: String
)

// ── Notifications ───────────────────────────────────────────────────────
data class AppNotification(
    val id: String,
    val title: String,
    val body: String,
    val type: String,
    val data: Map<String, Any>? = null,
    val isRead: Boolean = false,
    val createdAt: String
)

data class NotificationListResponse(
    val items: List<AppNotification>,
    val nextCursor: String?,
    val unreadCount: Int
)

data class RegisterDeviceRequest(val token: String, val platform: String)

// ── Finances ────────────────────────────────────────────────────────────
data class Income(
    val id: String,
    val amount: Int,
    val type: String,
    val date: String,
    val note: String? = null,
    val createdAt: String? = null
)

data class CreateIncomeRequest(
    val amount: Int,
    val type: String,
    val date: String? = null,
    val note: String? = null
)

data class PersonalExpense(
    val id: String,
    val amount: Int,
    val description: String,
    val category: String? = null,
    val date: String,
    val note: String? = null,
    val createdAt: String? = null
)

data class CreatePersonalExpenseRequest(
    val amount: Int,
    val description: String,
    val category: String? = null,
    val date: String? = null,
    val note: String? = null
)

data class FinancialSummary(
    val totalIncome: Int,
    val totalExpenses: Int,
    val netSavings: Int,
    val month: String?
)

// ── Categories ──────────────────────────────────────────────────────────
data class Category(val id: String, val name: String, val icon: String?, val isSystem: Boolean)

// ── Promos ──────────────────────────────────────────────────────────────
data class PromotionalBanner(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val image: String? = null,
    val cta: String? = null,
    val destination: String? = null,
    val targetScreen: String,
    val priority: Int
)

// ── Dashboard Elements ─────────────────────────────────────────────────
data class DashboardElement(
    val id: String,
    val type: String,
    val title: String? = null,
    val subtitle: String? = null,
    val body: String? = null,
    val image: String? = null,
    val cta: String? = null,
    val destination: String? = null,
    val targetScreen: String = "home",
    val position: Int = 0,
    val config: Map<String, Any>? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val status: String = "Active"
)

// ── Ad Placements ──────────────────────────────────────────────────────
data class AdPlacement(
    val name: String,
    val adType: String,           // banner | interstitial | native | rewarded
    val position: String,
    val adUnitIos: String? = null,
    val adUnitAndroid: String? = null,
    val frequency: Int? = null,
    val adFreeSkip: Boolean? = null,
)

// ── Generic ─────────────────────────────────────────────────────────────
data class SuccessResponse(val success: Boolean)
data class PaginatedResponse<T>(val items: List<T>, val nextCursor: String?)

// ── Helpers ─────────────────────────────────────────────────────────────
fun formatAmount(minorUnits: Int, currencyCode: String = "INR"): String {
    val major = minorUnits / 100.0
    return try {
        val fmt = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        fmt.currency = Currency.getInstance(currencyCode)
        fmt.format(major)
    } catch (_: Exception) {
        "$currencyCode %.2f".format(major)
    }
}
