package com.splitezapp.data.models

object SampleData {

    val currentUser = UserSummary(
        id = "u_me",
        firstName = "Ankit",
        lastName = "Loomba",
        profilePicture = null,
        avatar = AvatarData(initials = "AL", backgroundColor = "#6366F1")
    )

    val rahul = UserSummary(
        id = "u_rahul", firstName = "Rahul", lastName = "Sharma",
        avatar = AvatarData(initials = "RS", backgroundColor = "#F59E0B")
    )
    val anita = UserSummary(
        id = "u_anita", firstName = "Anita", lastName = "Verma",
        avatar = AvatarData(initials = "AV", backgroundColor = "#EC4899")
    )
    val priya = UserSummary(
        id = "u_priya", firstName = "Priya", lastName = "Patel",
        avatar = AvatarData(initials = "PP", backgroundColor = "#16A34A")
    )
    val sanjay = UserSummary(
        id = "u_sanjay", firstName = "Sanjay", lastName = "Kumar",
        avatar = AvatarData(initials = "SK", backgroundColor = "#0EA5E9")
    )
    val neha = UserSummary(
        id = "u_neha", firstName = "Neha", lastName = "Gupta",
        avatar = AvatarData(initials = "NG", backgroundColor = "#8B5CF6")
    )
    val vikram = UserSummary(
        id = "u_vikram", firstName = "Vikram", lastName = "Singh",
        avatar = AvatarData(initials = "VS", backgroundColor = "#F87171")
    )
    val deepa = UserSummary(
        id = "u_deepa", firstName = "Deepa", lastName = "Nair",
        avatar = AvatarData(initials = "DN", backgroundColor = "#14B8A6")
    )

    val friends = listOf(
        Friend(id = "u_rahul", firstName = "Rahul", lastName = "Sharma", phone = "+91 99887 76543", email = "rahul.sharma@gmail.com", avatar = AvatarData("RS", "#F59E0B"), isRegistered = true, groupCount = 3, lastActiveAt = "2026-09-16T14:30:00Z"),
        Friend(id = "u_anita", firstName = "Anita", lastName = "Verma", phone = "+91 88776 65432", email = "anita.verma@gmail.com", avatar = AvatarData("AV", "#EC4899"), isRegistered = true, groupCount = 2, lastActiveAt = "2026-09-17T09:15:00Z"),
        Friend(id = "u_priya", firstName = "Priya", lastName = "Patel", phone = "+91 77665 54321", email = "priya.patel@gmail.com", avatar = AvatarData("PP", "#16A34A"), isRegistered = true, groupCount = 2, lastActiveAt = "2026-09-15T20:00:00Z"),
        Friend(id = "u_sanjay", firstName = "Sanjay", lastName = "Kumar", phone = "+91 95544 33210", email = "sanjay.k@gmail.com", avatar = AvatarData("SK", "#0EA5E9"), isRegistered = true, groupCount = 2, lastActiveAt = "2026-09-16T11:00:00Z"),
        Friend(id = "u_neha", firstName = "Neha", lastName = "Gupta", phone = "+91 91234 56789", email = "neha.gupta@gmail.com", avatar = AvatarData("NG", "#8B5CF6"), isRegistered = true, groupCount = 2, lastActiveAt = "2026-09-14T16:45:00Z"),
        Friend(id = "u_vikram", firstName = "Vikram", lastName = "Singh", phone = "+91 93456 78901", email = "vikram.s@gmail.com", avatar = AvatarData("VS", "#F87171"), isRegistered = true, groupCount = 2, lastActiveAt = "2026-09-17T08:30:00Z"),
        Friend(id = "u_deepa", firstName = "Deepa", lastName = "Nair", phone = "+91 96789 01234", email = "deepa.nair@gmail.com", avatar = AvatarData("DN", "#14B8A6"), isRegistered = true, groupCount = 1, lastActiveAt = "2026-09-13T22:00:00Z"),
    )

    val groups = listOf(
        Group(id = "g_flat402", name = "Flat 402", description = "Monthly household expenses", memberCount = 4, members = listOf(currentUser, rahul, anita, sanjay), createdAt = "2025-01-15T10:00:00Z"),
        Group(id = "g_goatrip", name = "Goa Trip 2025", description = "Beach vacation with friends", memberCount = 6, members = listOf(currentUser, rahul, priya, neha, vikram, deepa), createdAt = "2025-03-20T08:30:00Z"),
        Group(id = "g_officelunch", name = "Office Lunch Crew", description = "Daily office lunch splits", memberCount = 5, members = listOf(currentUser, sanjay, neha, vikram, anita), createdAt = "2025-02-10T12:00:00Z"),
        Group(id = "g_weekendfoodies", name = "Weekend Foodies", description = "Weekend restaurant and food adventures", memberCount = 4, members = listOf(currentUser, priya, rahul, deepa), createdAt = "2025-04-05T18:00:00Z"),
        Group(id = "g_roadtrip", name = "Mumbai Road Trip", description = "Road trip expenses", memberCount = 3, members = listOf(currentUser, vikram, sanjay), createdAt = "2025-06-01T07:00:00Z"),
    )

    val balances = listOf(
        Balance(userId = "u_rahul", user = rahul, amount = 145000),
        Balance(userId = "u_priya", user = priya, amount = -32000),
        Balance(userId = "u_anita", user = anita, amount = 78500),
        Balance(userId = "u_sanjay", user = sanjay, amount = -15000),
        Balance(userId = "u_neha", user = neha, amount = 0),
        Balance(userId = "u_vikram", user = vikram, amount = 52000),
        Balance(userId = "u_deepa", user = deepa, amount = -9500),
    )

    val groupBalances = mapOf(
        "g_flat402" to 85000,
        "g_goatrip" to -32000,
        "g_officelunch" to 12500,
        "g_weekendfoodies" to 45000,
        "g_roadtrip" to -8000,
    )

    val recentExpenses = listOf(
        Expense(id = "e1", description = "Grocery – Big Bazaar", amount = 235000, currency = "INR", splitMethod = "equal", category = "groceries", date = "2026-09-17", paidBy = currentUser, createdBy = currentUser, groupId = "g_flat402", createdAt = "2026-09-17T10:30:00Z"),
        Expense(id = "e2", description = "Uber to Airport", amount = 85000, currency = "INR", splitMethod = "equal", category = "transport", date = "2026-09-16", paidBy = rahul, createdBy = rahul, groupId = "g_goatrip", createdAt = "2026-09-16T06:00:00Z"),
        Expense(id = "e3", description = "Pizza Hut lunch", amount = 124000, currency = "INR", splitMethod = "equal", category = "food", note = "Friday treat", date = "2026-09-15", paidBy = currentUser, createdBy = currentUser, groupId = "g_officelunch", createdAt = "2026-09-15T13:00:00Z"),
        Expense(id = "e4", description = "Electricity bill – Sep", amount = 340000, currency = "INR", splitMethod = "equal", category = "utilities", date = "2026-09-14", paidBy = anita, createdBy = anita, groupId = "g_flat402", createdAt = "2026-09-14T18:00:00Z"),
        Expense(id = "e5", description = "Café Mocha – Starbucks", amount = 45000, currency = "INR", splitMethod = "equal", category = "food", date = "2026-09-13", paidBy = priya, createdBy = priya, groupId = "g_weekendfoodies", createdAt = "2026-09-13T16:30:00Z"),
        Expense(id = "e6", description = "Petrol – HP pump", amount = 200000, currency = "INR", splitMethod = "equal", category = "transport", note = "Full tank", date = "2026-09-12", paidBy = vikram, createdBy = vikram, groupId = "g_roadtrip", createdAt = "2026-09-12T09:00:00Z"),
    )

    val activities = listOf(
        Activity(id = "a1", type = "expense_created", entityType = "expense", entityId = "e1", metadata = mapOf("description" to "Grocery – Big Bazaar", "amount" to 2350, "groupName" to "Flat 402"), user = currentUser, createdAt = "2026-09-17T10:30:00Z"),
        Activity(id = "a2", type = "expense_created", entityType = "expense", entityId = "e2", metadata = mapOf("description" to "Uber to Airport", "amount" to 850, "groupName" to "Goa Trip 2025"), user = rahul, createdAt = "2026-09-16T06:00:00Z"),
        Activity(id = "a3", type = "settlement_created", entityType = "settlement", entityId = "s1", metadata = mapOf("amount" to 500, "toName" to "Priya Patel"), user = currentUser, createdAt = "2026-09-16T14:00:00Z"),
        Activity(id = "a4", type = "expense_created", entityType = "expense", entityId = "e3", metadata = mapOf("description" to "Pizza Hut lunch", "amount" to 1240, "groupName" to "Office Lunch Crew"), user = currentUser, createdAt = "2026-09-15T13:00:00Z"),
        Activity(id = "a5", type = "group_created", entityType = "group", entityId = "g_weekendfoodies", metadata = mapOf("name" to "Weekend Foodies"), user = priya, createdAt = "2026-09-15T09:00:00Z"),
        Activity(id = "a6", type = "expense_created", entityType = "expense", entityId = "e4", metadata = mapOf("description" to "Electricity bill – Sep", "amount" to 3400, "groupName" to "Flat 402"), user = anita, createdAt = "2026-09-14T18:00:00Z"),
        Activity(id = "a7", type = "friend_added", entityType = "user", entityId = "u_deepa", metadata = mapOf("name" to "Deepa Nair"), user = deepa, createdAt = "2026-09-13T22:00:00Z"),
        Activity(id = "a8", type = "expense_created", entityType = "expense", entityId = "e5", metadata = mapOf("description" to "Café Mocha – Starbucks", "amount" to 450, "groupName" to "Weekend Foodies"), user = priya, createdAt = "2026-09-13T16:30:00Z"),
    )
}
