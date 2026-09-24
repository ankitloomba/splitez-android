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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
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
    var showAddFriend by remember { mutableStateOf(false) }
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
                .padding(top = 48.dp, bottom = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Friends", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { /* AI action */ }) {
                    Icon(Icons.Default.AutoAwesome, "AI", tint = Color.White)
                }
                IconButton(onClick = { showAddFriend = true }) {
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

    if (showAddFriend) {
        AddFriendDialog(
            onDismiss = { showAddFriend = false }
        )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddFriendDialog(onDismiss: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var email by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val inviteCode = remember {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val seed = abs(System.currentTimeMillis().hashCode())
        buildString {
            var s = seed
            repeat(6) {
                append(chars[s % chars.length])
                s /= chars.length
            }
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Add Friend", fontSize = 20.sp, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(8.dp))

            Text("Add a friend by email", fontSize = 14.sp, color = TextSecondary)

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it; error = null },
                placeholder = { Text("friend@example.com") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary.copy(alpha = 0.3f),
                    unfocusedBorderColor = Color(0xFFE0E0E0)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            if (error != null) {
                Spacer(Modifier.height(4.dp))
                Text(error!!, fontSize = 12.sp, color = Negative)
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    if (email.contains("@")) {
                        onDismiss()
                    } else {
                        error = "Please enter a valid email address."
                    }
                },
                enabled = email.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Send friend request", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(Modifier.height(16.dp))

            // Divider with "or"
            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text("  or  ", fontSize = 12.sp, color = TextTertiary)
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(16.dp))

            Text("Share your invite code", fontSize = 14.sp, fontWeight = FontWeight.Medium)

            Spacer(Modifier.height(8.dp))

            Text(
                inviteCode,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Primary,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                modifier = Modifier
                    .background(Primary.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            )

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    val sendIntent = android.content.Intent().apply {
                        action = android.content.Intent.ACTION_SEND
                        putExtra(android.content.Intent.EXTRA_TEXT,
                            "Join me on SplitEZ! Use my invite code: $inviteCode\n\nDownload SplitEZ and enter this code to connect.")
                        type = "text/plain"
                    }
                    context.startActivity(android.content.Intent.createChooser(sendIntent, "Share invite"))
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(28.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Primary)
            ) {
                Icon(Icons.Default.Share, null, tint = Primary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Share invite link", fontWeight = FontWeight.SemiBold, color = Primary)
            }
        }
    }
}
