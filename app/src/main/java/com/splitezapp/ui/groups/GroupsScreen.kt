package com.splitezapp.ui.groups

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.splitezapp.data.api.ApiClient
import com.splitezapp.data.models.*
import com.splitezapp.ui.components.EmptyState
import com.splitezapp.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(onGroupTap: (String) -> Unit = {}) {
    var groups by remember { mutableStateOf<List<Group>>(emptyList()) }
    var showCreate by remember { mutableStateOf(false) }
    var isSearchExpanded by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    var showOverflowMenu by remember { mutableStateOf(false) }
    var activeFilter by remember { mutableStateOf("All") }
    val filterOptions = listOf("All", "Active", "Settled")

    LaunchedEffect(Unit) {
        try { groups = ApiClient.api.getGroups() } catch (_: Exception) {}
        if (groups.isEmpty()) groups = SampleData.groups
    }

    val filteredGroups = remember(searchText, activeFilter, groups) {
        var result = groups.toList()
        if (searchText.isNotEmpty()) {
            result = result.filter { it.name.contains(searchText, ignoreCase = true) }
        }
        result
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
                Text("Groups", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { }) {
                    Icon(Icons.Default.AutoAwesome, "AI", tint = Color.White)
                }
                IconButton(onClick = { showCreate = true }) {
                    Icon(Icons.Default.AddCircleOutline, "Create group", tint = Color.White)
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
                            onClick = { showOverflowMenu = false },
                            leadingIcon = { Icon(Icons.Default.Security, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Export data") },
                            onClick = { showOverflowMenu = false },
                            leadingIcon = { Icon(Icons.Default.FileUpload, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Import data") },
                            onClick = { showOverflowMenu = false },
                            leadingIcon = { Icon(Icons.Default.FileDownload, null) }
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
                    placeholder = { Text("Search groups", color = TextTertiary) },
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

            // Filter pills
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filterOptions) { option ->
                    val isSelected = activeFilter == option
                    FilterChip(
                        selected = isSelected,
                        onClick = { activeFilter = option },
                        label = {
                            Text(option, fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal)
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
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("All groups", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        " · ${filteredGroups.size}",
                        fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextSecondary
                    )
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = {}) {
                        Icon(Icons.Default.Sort, null, tint = Primary, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Sort", color = Primary)
                    }
                }

                if (filteredGroups.isEmpty()) {
                    EmptyState("No groups yet", "👥")
                } else {
                    LazyColumn {
                        items(filteredGroups, key = { it.id }) { group ->
                            if (filteredGroups.indexOf(group) > 0) HorizontalDivider(modifier = Modifier.padding(start = 76.dp))
                            GroupListRow(group = group, onClick = { onGroupTap(group.id) })
                        }
                    }
                }
            }
        }
    }

    if (showCreate) {
        CreateGroupDialog(
            onDismiss = { showCreate = false },
            onCreated = {
                showCreate = false
                try { groups = ApiClient.api.getGroups() } catch (_: Exception) {}
            }
        )
    }
}

@Composable
fun CreateGroupDialog(onDismiss: () -> Unit, onCreated: suspend () -> Unit) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Group") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text("Group Name") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = description, onValueChange = { description = it },
                    label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    scope.launch {
                        try {
                            ApiClient.api.createGroup(CreateGroupRequest(name, description.ifEmpty { null }))
                            onCreated()
                        } catch (_: Exception) {}
                    }
                },
                enabled = name.isNotEmpty()
            ) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun GroupListRow(group: Group, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                group.name.take(1).uppercase(),
                color = Primary, fontWeight = FontWeight.Bold, fontSize = 18.sp
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(group.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text(
                "${group.memberCount ?: 0} members",
                fontSize = 13.sp, color = TextSecondary
            )
        }
        Icon(
            Icons.Default.ChevronRight, null,
            tint = TextTertiary, modifier = Modifier.size(20.dp)
        )
    }
}

private val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main)
private suspend fun launch(block: suspend () -> Unit) { block() }
