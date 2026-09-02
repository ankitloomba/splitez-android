package com.splitezapp.ui.groups

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.splitezapp.data.api.ApiClient
import com.splitezapp.data.models.*
import com.splitezapp.ui.components.AvatarView
import com.splitezapp.ui.components.EmptyState
import com.splitezapp.ui.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen() {
    var groups by remember { mutableStateOf<List<Group>>(emptyList()) }
    var showCreate by remember { mutableStateOf(false) }
    var selectedGroup by remember { mutableStateOf<Group?>(null) }

    LaunchedEffect(Unit) {
        try { groups = ApiClient.api.getGroups() } catch (_: Exception) {}
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Groups") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreate = true }) {
                Icon(Icons.Default.Add, "Create group")
            }
        }
    ) { padding ->
        if (groups.isEmpty()) {
            EmptyState("No groups yet", "👥")
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(groups, key = { it.id }) { group ->
                    ListItem(
                        headlineContent = { Text(group.name) },
                        supportingContent = { Text("${group.memberCount ?: 0} members") },
                        leadingContent = {
                            Icon(Icons.Default.Group, null, tint = Primary,
                                modifier = Modifier.size(40.dp))
                        },
                        modifier = Modifier.clickable { selectedGroup = group }
                    )
                    HorizontalDivider()
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

private val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main)
private suspend fun launch(block: suspend () -> Unit) { block() }
