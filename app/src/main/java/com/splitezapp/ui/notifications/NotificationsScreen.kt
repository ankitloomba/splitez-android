package com.splitezapp.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DoneAll
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
import com.splitezapp.data.models.AppNotification
import com.splitezapp.ui.components.EmptyState
import com.splitezapp.ui.theme.DarkBg
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
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                }
                Spacer(Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Notifications", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    if (unreadCount > 0) {
                        Spacer(Modifier.width(8.dp))
                        Badge { Text("$unreadCount") }
                    }
                }
                Spacer(Modifier.weight(1f))
                if (unreadCount > 0) {
                    IconButton(onClick = {
                        scope.launch {
                            try {
                                ApiClient.api.markAllRead()
                                load()
                            } catch (_: Exception) {}
                        }
                    }) {
                        Icon(Icons.Default.DoneAll, "Mark all read", tint = Color.White)
                    }
                } else {
                    Spacer(Modifier.size(48.dp))
                }
            }
        }

        // Content
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = Color.White
        ) {
            if (notifications.isEmpty()) {
                EmptyState("No notifications", "🔔")
            } else {
                LazyColumn {
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
