package com.splitezapp.ui.friends

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    onFriendTap: (Friend) -> Unit,
    onNotifications: () -> Unit = {},
    onMenuNavigate: (com.splitezapp.NavDestination) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val friends = remember { SampleData.friends }
    var searchText by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }
    var sortOption by remember { mutableStateOf("name") }
    var showSortMenu by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    var activeFilter by remember { mutableStateOf("All") }
    val filterOptions = listOf("All", "Owes you", "You owe", "Settled")

    val filteredFriends = remember(searchText, sortOption, activeFilter) {
        var result = friends.toList()
        if (searchText.isNotEmpty()) {
            result = result.filter {
                it.displayName.contains(searchText, ignoreCase = true)
            }
        }
        when (activeFilter) {
            "Owes you" -> result = result.filter { ExpenseStore.balanceForUser(it.id) > 0 }
            "You owe" -> result = result.filter { ExpenseStore.balanceForUser(it.id) < 0 }
            "Settled" -> result = result.filter { ExpenseStore.balanceForUser(it.id) == 0 }
        }
        when (sortOption) {
            "balance" -> result.sortedByDescending { abs(ExpenseStore.balanceForUser(it.id)) }
            "recent" -> result.sortedByDescending { it.lastActiveAt ?: "" }
            else -> result.sortedBy { it.displayName }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Dark header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkBg)
                .padding(horizontal = 20.dp)
                .padding(top = 48.dp, bottom = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Friends", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { onMenuNavigate(com.splitezapp.NavDestination.Account) }) {
                    Icon(Icons.Default.AccountCircle, "Account", tint = Color.White)
                }
                IconButton(onClick = { /* AI action */ }) {
                    Icon(Icons.Default.AutoAwesome, "AI", tint = Color.White)
                }
                IconButton(onClick = { /* Add friend */ }) {
                    Icon(Icons.Default.PersonAdd, "Add Friend", tint = Color.White)
                }
                IconButton(onClick = { isSearchExpanded = !isSearchExpanded; if (!isSearchExpanded) searchText = "" }) {
                    Icon(Icons.Default.Search, "Search", tint = Color.White)
                }
                Box {
                    IconButton(onClick = { showOverflowMenu = true }) {
                        Icon(Icons.Default.MoreVert, "Menu", tint = Color.White)
                    }
                    DropdownMenu(expanded = showOverflowMenu, onDismissRequest = { showOverflowMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Security") },
                            onClick = { showOverflowMenu = false; onMenuNavigate(com.splitezapp.NavDestination.Security) },
                            leadingIcon = { Icon(Icons.Default.Security, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Export data") },
                            onClick = { showOverflowMenu = false; onMenuNavigate(com.splitezapp.NavDestination.Export) },
                            leadingIcon = { Icon(Icons.Default.FileUpload, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Import data") },
                            onClick = { showOverflowMenu = false; onMenuNavigate(com.splitezapp.NavDestination.Import) },
                            leadingIcon = { Icon(Icons.Default.FileDownload, null) }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Log out", color = com.splitezapp.ui.theme.Negative) },
                            onClick = { showOverflowMenu = false; onLogout() },
                            leadingIcon = { Icon(Icons.Default.Logout, null, tint = com.splitezapp.ui.theme.Negative) }
                        )
                    }
                }
            }

            // Collapsible search bar
            if (isSearchExpanded) {
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = { Text("Search friends", color = TextTertiary) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = TextTertiary) },
                    trailingIcon = {
                        IconButton(onClick = { isSearchExpanded = false; searchText = "" }) {
                            Icon(Icons.Default.Close, null, tint = TextTertiary)
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White.copy(alpha = 0.2f),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        focusedContainerColor = Color.White.copy(alpha = 0.1f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(10.dp))

            // Filter pills – liquid glass style
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filterOptions) { option ->
                    val isSelected = activeFilter == option
                    FilterChip(
                        selected = isSelected,
                        onClick = { activeFilter = option },
                        label = {
                            Text(
                                option, fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xCC3366E6),
                            selectedLabelColor = Color.White,
                            containerColor = Color.White.copy(alpha = 0.12f),
                            labelColor = Color.White.copy(alpha = 0.65f)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = Color.White.copy(alpha = 0.15f),
                            selectedBorderColor = Color.White.copy(alpha = 0.35f),
                            borderWidth = 0.5.dp,
                            selectedBorderWidth = 0.5.dp
                        )
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
            Column {
                // Header row
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("All friends", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        " · ${friends.size}",
                        fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextSecondary
                    )
                    Spacer(Modifier.weight(1f))

                    Box {
                        TextButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.Default.Sort, null, tint = Primary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Sort", color = Primary)
                        }
                        DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Name (A–Z)") },
                                onClick = { sortOption = "name"; showSortMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Highest balance") },
                                onClick = { sortOption = "balance"; showSortMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Recently active") },
                                onClick = { sortOption = "recent"; showSortMenu = false }
                            )
                        }
                    }
                }

                if (filteredFriends.isEmpty()) {
                    Text(
                        if (searchText.isEmpty()) "No friends added yet" else "No results",
                        color = TextTertiary, fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)
                    )
                } else {
                    LazyColumn {
                        itemsIndexed(filteredFriends, key = { _, f -> f.id }) { index, friend ->
                            if (index > 0) HorizontalDivider(modifier = Modifier.padding(start = 76.dp))
                            FriendListRow(
                                friend = friend,
                                balance = ExpenseStore.balanceForUser(friend.id),
                                onClick = { onFriendTap(friend) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendListRow(friend: Friend, balance: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarView(friend.toUserSummary(), size = 44.dp)

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(friend.displayName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            val subtitle = buildString {
                friend.groupCount?.let { if (it > 0) append("$it group${if (it == 1) "" else "s"}") }
                friend.phone?.let { if (isNotEmpty()) append(" · "); append(it) }
            }
            if (subtitle.isNotEmpty()) {
                Text(subtitle, fontSize = 12.sp, color = TextSecondary)
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            if (balance == 0) {
                Text("settled up", fontSize = 12.sp, color = TextSecondary)
                Text("₹0", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Positive)
            } else {
                Text(
                    if (balance > 0) "owes you" else "you owe",
                    fontSize = 12.sp, color = TextSecondary
                )
                Text(
                    formatAmount(abs(balance)),
                    fontWeight = FontWeight.Bold, fontSize = 14.sp,
                    color = if (balance > 0) Positive else Negative
                )
            }
        }
    }
}
