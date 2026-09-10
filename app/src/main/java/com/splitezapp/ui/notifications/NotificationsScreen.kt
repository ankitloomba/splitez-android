package com.splitezapp.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.splitezapp.data.api.ApiClient
import com.splitezapp.data.models.AppNotification
import com.splitezapp.ui.components.EmptyState
import com.splitezapp.ui.theme.Primary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(onBack: () -> Unit) {
    var notifications by remember { mutableStateOf<List<AppNotification>>(emptyList()) }
    var unreadCount by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    suspend fun load() {
        try {
            val resp = ApiClient.api.getNotifications()
            notifications = resp.items
            unreadCount = resp.unreadCount
        } catch (_: Exception) {}
    }

    LaunchedEffect(Unit) { load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Notifications")
                        if (unreadCount > 0) {
                            Spacer(Modifier.width(8.dp))
                            Badge { Text("$unreadCount") }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (unreadCount > 0) {
                        IconButton(onClick = {
                            scope.launch {
                                try {
                                    ApiClient.api.markAllRead()
                                    load()
                                } catch (_: Exception) {}
                            }
                        }) {
                            Icon(Icons.Default.DoneAll, "Mark all read")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (notifications.isEmpty()) {
            Box(modifier = Modifier.padding(padding)) {
                EmptyState("No notifications", "🔔")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(notifications, key = { it.id }) { notif ->
                    NotificationRow(
                        notification = notif,
                        onTap = {
                            if (!notif.isRead) {
                                scope.launch {
                                    try {
                                        ApiClient.api.markRead(notif.id)
                                        load()
                                    } catch (_: Exception) {}
                                }
                            }
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(notification: AppNotification, onTap: () -> Unit) {
    val bgColor = if (!notification.isRead) {
        Primary.copy(alpha = 0.05f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .clickable(onClick = onTap)
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Unread dot
        if (!notification.isRead) {
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Primary)
            )
        } else {
            Spacer(Modifier.size(8.dp))
        }
        Spacer(Modifier.width(12.dp))

        // Icon
        val icon = when (notification.type) {
            "expense_added" -> "💸"
            "settlement_created" -> "🤝"
            "group_invite" -> "👥"
            "trip_invite" -> "✈️"
            "reminder" -> "⏰"
            else -> "🔔"
        }
        Text(icon, fontSize = 24.sp, modifier = Modifier.padding(end = 12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                notification.title,
                fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(2.dp))
            Text(
                notification.body,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                notification.createdAt.take(10),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}
