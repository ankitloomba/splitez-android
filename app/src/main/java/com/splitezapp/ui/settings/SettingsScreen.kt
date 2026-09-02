package com.splitezapp.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.splitezapp.data.models.UserProfile
import com.splitezapp.ui.components.AvatarView
import com.splitezapp.data.models.UserSummary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    user: UserProfile?,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            // Profile
            user?.let { u ->
                item {
                    ListItem(
                        headlineContent = { Text(u.displayName) },
                        supportingContent = { u.email?.let { Text(it) } },
                        leadingContent = {
                            AvatarView(
                                UserSummary(u.id, u.firstName, u.lastName, u.profilePicture, u.avatar),
                                size = 48.dp
                            )
                        }
                    )
                    HorizontalDivider()
                }
            }

            item {
                ListItem(
                    headlineContent = { Text("Currency") },
                    trailingContent = { Text(user?.currency ?: "INR") },
                    leadingContent = { Icon(Icons.Default.CurrencyExchange, null) }
                )
                HorizontalDivider()
            }

            item {
                ListItem(
                    headlineContent = { Text("Log Out") },
                    leadingContent = {
                        Icon(Icons.AutoMirrored.Filled.Logout, null,
                            tint = MaterialTheme.colorScheme.error)
                    },
                    modifier = Modifier.clickable { onLogout() }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "SplitEZ v1.0.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

private fun Modifier.clickable(onClick: () -> Unit): Modifier {
    return this.then(
        androidx.compose.foundation.clickable { onClick() }
    )
}
