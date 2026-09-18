package com.splitezapp.ui.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.splitezapp.data.models.*
import com.splitezapp.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

@Composable
fun ActivityScreen(onActivityTap: (String) -> Unit) {
    val activities = ExpenseStore.activities
    var activeSort by remember { mutableStateOf("Date") }
    var searchText by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }
    val sortOptions = listOf("Date", "Name", "Type", "Amount")

    val sortedActivities = remember(activeSort, searchText, activities.size) {
        var result = activities.toList()
        if (searchText.isNotEmpty()) {
            val q = searchText.lowercase()
            result = result.filter { a ->
                val desc = (a.metadata?.get("description") as? String) ?: ""
                val name = a.user?.firstName ?: ""
                val group = (a.metadata?.get("groupName") as? String) ?: ""
                desc.lowercase().contains(q) || name.lowercase().contains(q) ||
                    group.lowercase().contains(q) || a.type.lowercase().contains(q)
            }
        }
        when (activeSort) {
            "Name" -> result.sortedBy { it.user?.firstName ?: "" }
            "Type" -> result.sortedBy { it.type }
            "Amount" -> result.sortedByDescending {
                abs((it.metadata?.get("amount") as? Number)?.toInt() ?: 0)
            }
            else -> result.sortedByDescending { it.createdAt }
        }
    }

    val groupedActivities = remember(sortedActivities) {
        val cal = Calendar.getInstance()
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val yesterday = today - 86400000L
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val dateFmt = SimpleDateFormat("MMM d, yyyy", Locale.US)

        val groups = linkedMapOf<String, MutableList<Activity>>()
        for (a in sortedActivities) {
            val date = try { sdf.parse(a.createdAt) } catch (_: Exception) { null }
            val label = if (date != null) {
                val ms = date.time
                when {
                    ms >= today -> "TODAY"
                    ms >= yesterday -> "YESTERDAY"
                    else -> dateFmt.format(date).uppercase()
                }
            } else "OLDER"
            groups.getOrPut(label) { mutableListOf() }.add(a)
        }
        groups.toList()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Dark header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkBg)
                .padding(horizontal = 20.dp)
                .padding(top = 48.dp, bottom = 24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Activity", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { showSearch = !showSearch; if (!showSearch) searchText = "" }) {
                    Icon(Icons.Default.Search, null, tint = Color.White)
                }
            }

            if (showSearch) {
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = { Text("Search activity", color = TextTertiary) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = TextTertiary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White.copy(alpha = 0.2f),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        focusedContainerColor = Color.White.copy(alpha = 0.1f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(12.dp))

            // Sort pills
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(sortOptions) { option ->
                    FilterChip(
                        selected = activeSort == option,
                        onClick = { activeSort = option },
                        label = { Text(option, fontSize = 14.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Primary,
                            selectedLabelColor = Color.White,
                            containerColor = Color.White.copy(alpha = 0.12f),
                            labelColor = Color.White.copy(alpha = 0.7f)
                        ),
                        border = null
                    )
                }
            }
        }

        // Content
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = Color.White
        ) {
            if (activities.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No recent activity", color = TextTertiary, fontSize = 14.sp)
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
                    groupedActivities.forEach { (label, items) ->
                        item {
                            Text(
                                label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                                color = Primary, letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 8.dp)
                            )
                        }
                        items(items, key = { it.id }) { activity ->
                            ActivityRow(
                                activity = activity,
                                onClick = {
                                    activity.entityId?.let { onActivityTap(it) }
                                }
                            )
                            if (activity.id != items.lastOrNull()?.id) {
                                HorizontalDivider(modifier = Modifier.padding(start = 72.dp, end = 20.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityRow(activity: Activity, onClick: () -> Unit) {
    val type = activity.type.lowercase()
    val iconInfo = when (type) {
        "settlement_completed", "settlement_created" -> Triple("✔", Positive, Positive.copy(alpha = 0.12f))
        "expense_created" -> Triple("🍴", Color(0xFFCC9933), Color(0xFFCC9933).copy(alpha = 0.12f))
        "group_created" -> Triple("🏠", Primary, Primary.copy(alpha = 0.12f))
        "friend_added" -> Triple("👤", Primary, Primary.copy(alpha = 0.12f))
        else -> Triple("🔔", Negative, Negative.copy(alpha = 0.12f))
    }

    val name = activity.user?.firstName ?: "Someone"
    val title = buildAnnotatedString {
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(name) }
        when (type) {
            "expense_created" -> {
                val desc = (activity.metadata?.get("description") as? String) ?: "an expense"
                append(" added $desc")
            }
            "settlement_created", "settlement_completed" -> {
                val toName = (activity.metadata?.get("toName") as? String) ?: "someone"
                append(" settled up with $toName")
            }
            "group_created" -> {
                val gn = (activity.metadata?.get("name") as? String) ?: "a group"
                append(" created $gn")
            }
            "friend_added" -> {
                val fn = (activity.metadata?.get("name") as? String) ?: "a friend"
                append(" added $fn")
            }
            else -> append(" updated an activity")
        }
    }

    val subtitle = run {
        val groupName = (activity.metadata?.get("groupName") as? String) ?: ""
        when (type) {
            "settlement_completed", "settlement_created" ->
                if (groupName.isEmpty()) "settlement" else "$groupName · settlement"
            "expense_created" -> groupName.ifEmpty { "expense" }
            else -> groupName
        }
    }

    val amount = run {
        val amt = (activity.metadata?.get("amount") as? Number)?.toInt()
        if (amt != null && amt != 0) {
            val prefix = if (amt > 0) "+ " else "– "
            "$prefix${formatAmount(abs(amt))}"
        } else null
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(iconInfo.third),
            contentAlignment = Alignment.Center
        ) {
            Text(iconInfo.first, fontSize = 20.sp)
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp)
            if (subtitle.isNotEmpty()) {
                Text(subtitle, fontSize = 12.sp, color = TextSecondary)
            }
        }

        if (amount != null) {
            val amtInt = (activity.metadata?.get("amount") as? Number)?.toInt() ?: 0
            Text(
                amount, fontWeight = FontWeight.Bold, fontSize = 14.sp,
                color = if (amtInt >= 0) Positive else Negative
            )
        } else {
            val time = if (activity.createdAt.length >= 16) activity.createdAt.substring(11, 16) else ""
            Text(time, fontSize = 12.sp, color = TextTertiary)
        }
    }
}
