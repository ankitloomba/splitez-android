package com.splitezapp.data.api

import com.splitezapp.data.models.*
import retrofit2.http.*

interface ApiService {
    // Auth
    @POST("auth/register")
    suspend fun register(@Body req: RegisterRequest): AuthTokens

    @POST("auth/login")
    suspend fun login(@Body req: LoginRequest): AuthTokens

    @POST("auth/refresh")
    suspend fun refresh(@Body req: RefreshRequest): AuthTokens

    @POST("auth/verify-email")
    suspend fun verifyEmail(@Body req: VerifyEmailRequest): SuccessResponse

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body req: ForgotPasswordRequest): SuccessResponse

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body req: ResetPasswordRequest): SuccessResponse

    @POST("auth/logout")
    suspend fun logout(): SuccessResponse

    // Users
    @GET("users/me")
    suspend fun getMe(): UserProfile

    @PUT("users/me")
    suspend fun updateMe(@Body req: UpdateUserRequest): UserProfile

    // People
    @GET("people")
    suspend fun getPeople(): List<UserSummary>

    // Groups
    @GET("groups")
    suspend fun getGroups(): List<Group>

    @POST("groups")
    suspend fun createGroup(@Body req: CreateGroupRequest): Group

    @GET("groups/{id}")
    suspend fun getGroup(@Path("id") id: String): Group

    // Trips
    @GET("trips")
    suspend fun getTrips(): List<Trip>

    @POST("trips")
    suspend fun createTrip(@Body req: CreateTripRequest): Trip

    @GET("trips/{id}")
    suspend fun getTrip(@Path("id") id: String): Trip

    // Expenses
    @GET("expenses")
    suspend fun getExpenses(@QueryMap filters: Map<String, String> = emptyMap()): List<Expense>

    @POST("expenses")
    suspend fun createExpense(@Body req: CreateExpenseRequest): Expense

    @GET("expenses/{id}")
    suspend fun getExpense(@Path("id") id: String): Expense

    @DELETE("expenses/{id}")
    suspend fun deleteExpense(@Path("id") id: String): SuccessResponse

    // Balances
    @GET("balances")
    suspend fun getBalances(@QueryMap filters: Map<String, String> = emptyMap()): List<Balance>

    @GET("balances/simplified")
    suspend fun getSimplifiedDebts(@Query("groupId") groupId: String? = null): List<SimplifiedDebt>

    // Settlements
    @GET("settlements")
    suspend fun getSettlements(@Query("groupId") groupId: String? = null): List<Settlement>

    @POST("settlements")
    suspend fun createSettlement(@Body req: CreateSettlementRequest): Settlement

    // Activity
    @GET("activity")
    suspend fun getActivity(@QueryMap params: Map<String, String> = emptyMap()): PaginatedResponse<Activity>

    @GET("activity/feed")
    suspend fun getFeed(@QueryMap params: Map<String, String> = emptyMap()): PaginatedResponse<Activity>

    // Notifications
    @GET("notifications")
    suspend fun getNotifications(@QueryMap params: Map<String, String> = emptyMap()): NotificationListResponse

    @PATCH("notifications/{id}/read")
    suspend fun markRead(@Path("id") id: String): SuccessResponse

    @PATCH("notifications/read-all")
    suspend fun markAllRead(): SuccessResponse

    @POST("notifications/devices")
    suspend fun registerDevice(@Body req: RegisterDeviceRequest): Any

    // Finances
    @GET("finances/income")
    suspend fun getIncome(@QueryMap params: Map<String, String> = emptyMap()): PaginatedResponse<Income>

    @POST("finances/income")
    suspend fun createIncome(@Body req: CreateIncomeRequest): Income

    @DELETE("finances/income/{id}")
    suspend fun deleteIncome(@Path("id") id: String): SuccessResponse

    @GET("finances/expenses")
    suspend fun getPersonalExpenses(@QueryMap params: Map<String, String> = emptyMap()): PaginatedResponse<PersonalExpense>

    @POST("finances/expenses")
    suspend fun createPersonalExpense(@Body req: CreatePersonalExpenseRequest): PersonalExpense

    @DELETE("finances/expenses/{id}")
    suspend fun deletePersonalExpense(@Path("id") id: String): SuccessResponse

    @GET("finances/summary")
    suspend fun getFinancialSummary(@Query("month") month: String? = null): FinancialSummary

    // Categories
    @GET("categories")
    suspend fun getCategories(): List<Category>

    // Promos
    @GET("promos")
    suspend fun getPromos(@Query("screen") screen: String? = null): List<PromotionalBanner>

    // Dashboard Elements
    @GET("dashboard/elements")
    suspend fun getDashboardElements(@Query("screen") screen: String? = null): List<DashboardElement>

    // Analytics
    @POST("analytics/events/batch")
    suspend fun trackEventsBatch(@Body body: com.splitezapp.data.analytics.BatchPayload): SuccessResponse

    @POST("analytics/installs")
    suspend fun registerInstall(@Body body: com.splitezapp.data.analytics.InstallPayload): SuccessResponse

    // Ads
    @GET("ads/placements")
    suspend fun getAdPlacements(
        @Query("screen") screen: String,
        @Query("platform") platform: String = "android"
    ): List<AdPlacement>
}
