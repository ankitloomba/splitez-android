package com.splitezapp.ui.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.splitezapp.data.models.*
import com.splitezapp.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

enum class ExpenseCategory(val label: String, val icon: String, val color: Color) {
    FOOD("Food", "🍴", Color(0xFFFF9800)),
    TRANSPORT("Transport", "🚗", Color(0xFF6366F1)),
    SHOPPING("Shopping", "🛍", Color(0xFFE91E63)),
    STAY("Stay", "🏠", Color(0xFF4CAF50)),
    ENTERTAINMENT("Entertainment", "🎬", Color(0xFF9C27B0)),
    UTILITIES("Utilities", "⚡", Color(0xFFFFC107)),
    HEALTH("Health", "❤", Color(0xFFF44336)),
    EDUCATION("Education", "📚", Color(0xFF2196F3)),
    TRAVEL("Travel", "✈", Color(0xFF009688)),
    OTHER("Other", "📋", Color(0xFF9E9E9E));

    companion object {
        private val keywords = mapOf(
            FOOD to listOf("dinner", "lunch", "breakfast", "coffee", "restaurant", "pizza", "cafe", "meal", "food", "swiggy", "zomato"),
            TRANSPORT to listOf("uber", "ola", "cab", "taxi", "auto", "bus", "metro", "fuel", "petrol", "diesel", "parking"),
            SHOPPING to listOf("amazon", "flipkart", "clothes", "shoes", "electronics", "mall", "shop"),
            STAY to listOf("hotel", "airbnb", "hostel", "resort", "rent", "oyo"),
            ENTERTAINMENT to listOf("movie", "cinema", "netflix", "concert", "game", "party"),
            UTILITIES to listOf("electricity", "water", "wifi", "internet", "recharge", "gas bill", "maintenance"),
            HEALTH to listOf("doctor", "hospital", "medicine", "pharmacy", "gym"),
            EDUCATION to listOf("book", "course", "tuition", "school", "college"),
            TRAVEL to listOf("flight", "airport", "visa", "ticket", "trip", "vacation"),
        )

        fun detect(text: String): ExpenseCategory {
            val lower = text.lowercase()
            for ((cat, kw) in keywords) {
                if (kw.any { lower.contains(it) }) return cat
            }
            return FOOD
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    onDismiss: () -> Unit,
    prefillFriend: Friend? = null,
    editExpense: Expense? = null
) {
    val editingId = editExpense?.id

    var amountText by remember {
        mutableStateOf(
            if (editExpense != null) String.format("%.2f", editExpense.amount / 100.0) else ""
        )
    }
    var description by remember { mutableStateOf(editExpense?.description ?: "") }
    var selectedCurrency by remember { mutableStateOf(editExpense?.currency ?: "INR") }
    var selectedCategory by remember {
        mutableStateOf(
            editExpense?.category?.let { cat ->
                ExpenseCategory.entries.firstOrNull { it.label.equals(cat, true) }
            } ?: ExpenseCategory.FOOD
        )
    }
    var splitMethod by remember { mutableStateOf(editExpense?.splitMethod?.uppercase() ?: "EQUAL") }
    var note by remember { mutableStateOf(editExpense?.note ?: "") }
    var showNotesField by remember { mutableStateOf(!note.isNullOrEmpty()) }
    var showSplitBreakdown by remember { mutableStateOf(false) }
    var paidByUserId by remember { mutableStateOf(editExpense?.paidBy?.id ?: SampleData.currentUser.id) }
    var selectedParticipantIds by remember {
        mutableStateOf(
            if (editExpense != null) {
                val splitIds = editExpense.splits?.mapNotNull { it.userId }?.toSet() ?: emptySet()
                splitIds.ifEmpty { setOf(SampleData.currentUser.id, editExpense.paidBy?.id ?: SampleData.currentUser.id) }
            } else if (prefillFriend != null) {
                setOf(SampleData.currentUser.id, prefillFriend.id)
            } else {
                setOf(SampleData.currentUser.id)
            }
        )
    }
    var showParticipantPicker by remember { mutableStateOf(false) }
    var showPaidByPicker by remember { mutableStateOf(false) }
    var showGroupPicker by remember { mutableStateOf(false) }
    var selectedGroupIndex by remember { mutableStateOf<Int?>(null) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var participantSearchText by remember { mutableStateOf("") }

    val groups = remember { SampleData.groups }

    // Initialize group index for edit mode
    LaunchedEffect(Unit) {
        if (editExpense?.groupId != null) {
            selectedGroupIndex = groups.indexOfFirst { it.id == editExpense.groupId }.takeIf { it >= 0 }
        }
    }

    val allPeople = remember {
        val seen = mutableSetOf<String>()
        val result = mutableListOf<UserSummary>()
        result.add(SampleData.currentUser); seen.add(SampleData.currentUser.id)
        for (f in SampleData.friends) {
            if (seen.add(f.id)) result.add(f.toUserSummary())
        }
        for (g in SampleData.groups) {
            for (m in g.members ?: emptyList()) {
                if (seen.add(m.id)) result.add(m)
            }
        }
        result
    }

    val participants = allPeople.filter { it.id in selectedParticipantIds }
    val paidByUser = allPeople.firstOrNull { it.id == paidByUserId }

    val currSymbols = mapOf("INR" to "₹", "USD" to "$", "EUR" to "€", "GBP" to "£")
    val currSymbol = currSymbols[selectedCurrency] ?: "₹"

    val amountMinor = ((amountText.toDoubleOrNull() ?: 0.0) * 100).toInt()
    val perPersonAmount = if (participants.isNotEmpty() && amountMinor > 0) {
        val share = (amountText.toDoubleOrNull() ?: 0.0) / participants.size
        "$currSymbol${String.format("%.0f", share)}"
    } else "$currSymbol 0"

    val canSave = (amountText.toDoubleOrNull() ?: 0.0) > 0 &&
        description.isNotBlank() && participants.isNotEmpty()

    fun saveExpense() {
        val amount = ((amountText.toDoubleOrNull() ?: 0.0) * 100).toInt()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val isoFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val splits = participants.map { user ->
            ExpenseSplit(
                userId = user.id, user = user,
                shareAmount = amount / maxOf(participants.size, 1)
            )
        }
        val expense = Expense(
            id = editingId ?: "e_${UUID.randomUUID().toString().take(8)}",
            description = description,
            amount = amount,
            currency = selectedCurrency,
            splitMethod = splitMethod.lowercase(),
            category = selectedCategory.label,
            note = note.ifEmpty { null },
            date = sdf.format(Date()),
            paidBy = paidByUser,
            createdBy = SampleData.currentUser,
            splits = splits,
            groupId = selectedGroupIndex?.let { groups.getOrNull(it)?.id },
            createdAt = isoFmt.format(Date())
        )
        if (editingId != null) {
            ExpenseStore.updateExpense(expense)
        } else {
            ExpenseStore.addExpense(expense)
        }
        onDismiss()
    }

    // Participant picker bottom sheet
    if (showParticipantPicker) {
        ModalBottomSheet(onDismissRequest = { showParticipantPicker = false; participantSearchText = "" }) {
            Column(modifier = Modifier.fillMaxHeight(0.7f)) {
                Text(
                    "Split with", fontWeight = FontWeight.Bold, fontSize = 18.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // Selected chips
                if (selectedParticipantIds.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val selected = allPeople.filter { it.id in selectedParticipantIds }
                        for (user in selected) {
                            InputChip(
                                selected = true,
                                onClick = { selectedParticipantIds = selectedParticipantIds - user.id },
                                label = { Text(user.firstName) },
                                trailingIcon = { Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp)) },
                                avatar = { MiniAvatar(user) }
                            )
                        }
                    }
                    HorizontalDivider()
                }

                // Search bar
                OutlinedTextField(
                    value = participantSearchText,
                    onValueChange = { participantSearchText = it },
                    placeholder = { Text("Search by name or phone") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                val filteredPeople = if (participantSearchText.isBlank()) allPeople
                else allPeople.filter {
                    it.displayName.contains(participantSearchText, true)
                }

                LazyColumn {
                    items(filteredPeople, key = { it.id }) { user ->
                        val isSelected = user.id in selectedParticipantIds
                        ListItem(
                            headlineContent = {
                                Text(
                                    if (user.id == SampleData.currentUser.id) "You" else user.displayName,
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            leadingContent = { MiniAvatar(user) },
                            trailingContent = {
                                if (isSelected) Icon(Icons.Default.CheckCircle, null, tint = Primary)
                                else Icon(Icons.Default.RadioButtonUnchecked, null, tint = Color.Gray)
                            },
                            modifier = Modifier.clickable {
                                selectedParticipantIds = if (isSelected) selectedParticipantIds - user.id
                                else selectedParticipantIds + user.id
                            }
                        )
                    }
                }
            }
        }
    }

    // Paid by picker
    if (showPaidByPicker) {
        ModalBottomSheet(onDismissRequest = { showPaidByPicker = false }) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Paid by", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(8.dp))
                val paidByOptions = if (participants.any { it.id == SampleData.currentUser.id }) participants
                    else listOf(SampleData.currentUser) + participants
                for (user in paidByOptions) {
                    ListItem(
                        headlineContent = {
                            Text(if (user.id == SampleData.currentUser.id) "You" else user.displayName)
                        },
                        leadingContent = { MiniAvatar(user) },
                        trailingContent = {
                            if (user.id == paidByUserId) Icon(Icons.Default.CheckCircle, null, tint = Primary)
                        },
                        modifier = Modifier.clickable { paidByUserId = user.id; showPaidByPicker = false }
                    )
                }
            }
        }
    }

    // Group picker
    if (showGroupPicker) {
        ModalBottomSheet(onDismissRequest = { showGroupPicker = false }) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Select Group", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(8.dp))
                ListItem(
                    headlineContent = { Text("None (no group)") },
                    modifier = Modifier.clickable { selectedGroupIndex = null; showGroupPicker = false }
                )
                groups.forEachIndexed { index, group ->
                    ListItem(
                        headlineContent = { Text(group.name) },
                        supportingContent = { Text("${group.memberCount ?: group.members?.size ?: 0} members") },
                        leadingContent = { Icon(Icons.Default.Group, null, tint = Primary) },
                        trailingContent = {
                            if (selectedGroupIndex == index) Icon(Icons.Default.CheckCircle, null, tint = Primary)
                        },
                        modifier = Modifier.clickable {
                            selectedGroupIndex = index
                            val memberIds = (group.members ?: emptyList()).map { it.id }.toSet()
                            selectedParticipantIds = memberIds
                            showGroupPicker = false
                        }
                    )
                }
            }
        }
    }

    // Category picker
    if (showCategoryPicker) {
        ModalBottomSheet(onDismissRequest = { showCategoryPicker = false }) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Category", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(8.dp))
                ExpenseCategory.entries.forEach { cat ->
                    ListItem(
                        headlineContent = { Text(cat.label) },
                        leadingContent = { Text(cat.icon, fontSize = 20.sp) },
                        trailingContent = {
                            if (cat == selectedCategory) Icon(Icons.Default.CheckCircle, null, tint = Primary)
                        },
                        modifier = Modifier.clickable { selectedCategory = cat; showCategoryPicker = false }
                    )
                }
            }
        }
    }

    // Main content
    Column(modifier = Modifier.fillMaxSize()) {
        // Dark header with amount
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkBg)
                .padding(horizontal = 20.dp)
                .padding(top = 48.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "Close", tint = Color.White)
                }
                Spacer(Modifier.weight(1f))
                Text(
                    if (editExpense != null) "Edit expense" else "New expense",
                    color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp
                )
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.width(48.dp))
            }

            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(currSymbol, color = Color.White.copy(alpha = 0.5f), fontSize = 22.sp)
                Spacer(Modifier.width(4.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    placeholder = { Text("0", color = Color.White.copy(alpha = 0.3f), fontSize = 44.sp, fontWeight = FontWeight.Bold) },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = Color.White, fontSize = 44.sp, fontWeight = FontWeight.Bold
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                    ),
                    modifier = Modifier.width(IntrinsicSize.Min).widthIn(min = 80.dp)
                )
                Spacer(Modifier.width(8.dp))
                AssistChip(
                    onClick = { /* currency picker */ },
                    label = { Text(selectedCurrency, fontSize = 12.sp, color = Color.White) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = Color.White.copy(alpha = 0.15f)),
                    border = null
                )
            }
        }

        // Scrollable form
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .background(Color.White)
                .padding(horizontal = 20.dp)
                .padding(top = 24.dp)
        ) {
            // Category + Description
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(selectedCategory.color.copy(alpha = 0.1f))
                        .clickable { showCategoryPicker = true },
                    contentAlignment = Alignment.Center
                ) { Text(selectedCategory.icon, fontSize = 20.sp) }

                Spacer(Modifier.width(12.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = {
                        description = it
                        selectedCategory = ExpenseCategory.detect(it)
                    },
                    placeholder = { Text("Dinner at Olive Garden") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Paid By + Split participants
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("PAID BY", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TextTertiary, letterSpacing = 0.5.sp)
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { showPaidByPicker = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (paidByUser != null) {
                            MiniAvatar(paidByUser!!)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (paidByUser!!.id == SampleData.currentUser.id) "You" else paidByUser!!.firstName,
                                fontSize = 14.sp, maxLines = 1
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Default.KeyboardArrowDown, null, modifier = Modifier.size(16.dp))
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text("SPLIT", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TextTertiary, letterSpacing = 0.5.sp)
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { showParticipantPicker = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row {
                            participants.take(2).forEach { MiniAvatar(it, Modifier.offset(x = (-3).dp)) }
                            if (participants.size > 2) {
                                Text("+${participants.size - 2}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Default.KeyboardArrowDown, null, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Split method + Group picker
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { /* split method picker */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(when (splitMethod) { "EXACT" -> "Exact"; "PERCENTAGE" -> "Percentage"; else -> "Equally" })
                }
                OutlinedButton(
                    onClick = { showGroupPicker = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(Icons.Default.People, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        selectedGroupIndex?.let { groups.getOrNull(it)?.name } ?: "Select",
                        maxLines = 1
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Each person pays
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showSplitBreakdown = !showSplitBreakdown }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("EACH PERSON PAYS", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TextTertiary, letterSpacing = 0.5.sp)
                    Spacer(Modifier.weight(1f))
                    Icon(
                        if (showSplitBreakdown) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        null, tint = TextTertiary, modifier = Modifier.size(16.dp)
                    )
                }

                if (showSplitBreakdown) {
                    participants.forEachIndexed { index, user ->
                        if (index > 0) HorizontalDivider(modifier = Modifier.padding(start = 52.dp))
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MiniAvatar(user)
                            Spacer(Modifier.width(10.dp))
                            Text(
                                if (user.id == SampleData.currentUser.id) "You" else user.firstName,
                                fontWeight = FontWeight.Medium, fontSize = 14.sp
                            )
                            Spacer(Modifier.weight(1f))
                            Text(perPersonAmount, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            Spacer(Modifier.height(16.dp))

            // Date, Notes row
            Row {
                TextButton(onClick = {}) {
                    Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(16.dp), tint = TextSecondary)
                    Spacer(Modifier.width(6.dp))
                    Text("Today", color = TextSecondary)
                }
                TextButton(onClick = { showNotesField = !showNotesField }) {
                    Icon(Icons.Default.Description, null, modifier = Modifier.size(16.dp), tint = TextSecondary)
                    Spacer(Modifier.width(6.dp))
                    Text("Notes", color = TextSecondary)
                }
                TextButton(onClick = {}) {
                    Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(16.dp), tint = TextSecondary)
                    Spacer(Modifier.width(6.dp))
                    Text("Receipt", color = TextSecondary)
                }
            }

            if (showNotesField) {
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = { Text("Add a note...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2
                )
            }

            Spacer(Modifier.height(80.dp))
        }

        // Save button
        Surface(
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { if (canSave) saveExpense() },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canSave) Primary else Primary.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text("Save expense", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

@Composable
fun MiniAvatar(user: UserSummary, modifier: Modifier = Modifier) {
    val bgColor = if (user.avatar?.backgroundColor != null) {
        try { Color(android.graphics.Color.parseColor(user.avatar.backgroundColor)) }
        catch (_: Exception) { avatarColor(user.id) }
    } else avatarColor(user.id)

    val initials = user.avatar?.initials ?: avatarInitials(user.firstName, user.lastName)

    Box(
        modifier = modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text(initials, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}
