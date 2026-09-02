package com.splitezapp.ui.trips

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.splitezapp.data.api.ApiClient
import com.splitezapp.data.models.*
import com.splitezapp.ui.components.EmptyState
import com.splitezapp.ui.theme.Accent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripsScreen() {
    var trips by remember { mutableStateOf<List<Trip>>(emptyList()) }
    var showCreate by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try { trips = ApiClient.api.getTrips() } catch (_: Exception) {}
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Trips") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreate = true }) {
                Icon(Icons.Default.Add, "Create trip")
            }
        }
    ) { padding ->
        if (trips.isEmpty()) {
            EmptyState("No trips yet", "✈️")
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(trips, key = { it.id }) { trip ->
                    ListItem(
                        headlineContent = { Text(trip.name) },
                        supportingContent = { trip.destination?.let { Text(it) } },
                        leadingContent = {
                            Icon(Icons.Default.Flight, null, tint = Accent,
                                modifier = Modifier.size(40.dp))
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }

    if (showCreate) {
        CreateTripDialog(
            onDismiss = { showCreate = false },
            onCreated = {
                showCreate = false
                try { trips = ApiClient.api.getTrips() } catch (_: Exception) {}
            }
        )
    }
}

@Composable
fun CreateTripDialog(onDismiss: () -> Unit, onCreated: suspend () -> Unit) {
    var name by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Trip") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text("Trip Name") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = destination, onValueChange = { destination = it },
                    label = { Text("Destination") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    scope.launch {
                        try {
                            ApiClient.api.createTrip(CreateTripRequest(name, destination.ifEmpty { null }))
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
