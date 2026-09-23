package com.splitezapp.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.splitezapp.ui.theme.*

data class SessionInfo(
    val id: String,
    val deviceName: String,
    val subtitle: String,
    val isCurrentDevice: Boolean,
    val isActive: Boolean
)

@Composable
fun SecurityScreen(onBack: () -> Unit) {
    var biometricEnabled by remember { mutableStateOf(true) }
    var appLockEnabled by remember { mutableStateOf(false) }

    val sessions = remember {
        listOf(
            SessionInfo("s1", "iPhone 15 Pro", "Active now · this device", isCurrentDevice = true, isActive = true),
            SessionInfo("s2", "Chrome · Windows", "Last active 2 days ago", isCurrentDevice = false, isActive = false)
        )
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
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                }
                Spacer(Modifier.weight(1f))
                Text(
                    "Security", color = Color.White, fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.size(48.dp))
            }
        }

        // Content
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(top = 8.dp).verticalScroll(rememberScrollState())) {
                // Change password
                SecurityRow(
                    icon = Icons.Default.Lock,
                    title = "Change password",
                    subtitle = "Last changed 3 months ago",
                    onClick = {}
                ) {
                    Icon(Icons.Default.ChevronRight, null, tint = TextTertiary)
                }
                RowDivider()

                // Biometric login
                SecurityRow(
                    icon = Icons.Default.Fingerprint,
                    title = "Biometric login",
                    subtitle = "Face ID / fingerprint",
                    onClick = { biometricEnabled = !biometricEnabled }
                ) {
                    Switch(
                        checked = biometricEnabled,
                        onCheckedChange = { biometricEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Primary,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color.LightGray
                        )
                    )
                }
                RowDivider()

                // App lock
                SecurityRow(
                    icon = Icons.Default.PhonelinkLock,
                    title = "App lock",
                    subtitle = "Require PIN on every open",
                    onClick = { appLockEnabled = !appLockEnabled }
                ) {
                    Switch(
                        checked = appLockEnabled,
                        onCheckedChange = { appLockEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Primary,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color.LightGray
                        )
                    )
                }

                // Sessions section
                Spacer(Modifier.height(16.dp))
                Text(
                    "SESSIONS", fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                    color = Primary, letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )

                sessions.forEach { session ->
                    SessionRow(session, onRevoke = {})
                    if (session.id != sessions.last().id) RowDivider()
                }

                Spacer(Modifier.height(16.dp))

                // Log out all other devices
                OutlinedButton(
                    onClick = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Negative),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Negative)
                ) {
                    Text("Log out all other devices", fontWeight = FontWeight.SemiBold)
                }

                // Account section
                Spacer(Modifier.height(24.dp))
                Text(
                    "ACCOUNT", fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                    color = Primary, letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {}
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.DeleteForever, null, tint = Negative, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(14.dp))
                    Text("Delete account", color = Negative, fontSize = 15.sp)
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.ChevronRight, null, tint = TextTertiary)
                }
            }
        }
    }
}

@Composable
private fun SecurityRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Primary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, fontSize = 12.sp, color = TextSecondary)
        }
        trailing()
    }
}

@Composable
private fun SessionRow(session: SessionInfo, onRevoke: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (session.isCurrentDevice) Icons.Default.PhoneAndroid else Icons.Default.Computer,
            null, tint = TextSecondary, modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(session.deviceName, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(
                session.subtitle, fontSize = 12.sp,
                color = if (session.isActive) Positive else TextSecondary
            )
        }
        if (session.isCurrentDevice) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Positive)
            )
        } else {
            Text(
                "Revoke", fontSize = 14.sp, color = Negative,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable(onClick = onRevoke)
            )
        }
    }
}

@Composable
private fun RowDivider() {
    HorizontalDivider(modifier = Modifier.padding(start = 56.dp, end = 20.dp))
}
