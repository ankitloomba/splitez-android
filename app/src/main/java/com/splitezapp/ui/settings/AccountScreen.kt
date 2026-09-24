package com.splitezapp.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.splitezapp.NavDestination
import com.splitezapp.data.models.UserProfile
import com.splitezapp.ui.theme.*

@Composable
fun AccountScreen(
    user: UserProfile?,
    onBack: () -> Unit,
    onNavigate: (NavDestination) -> Unit,
    onLogout: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Dark header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkBg)
                .padding(horizontal = 20.dp)
                .padding(top = 48.dp, bottom = 32.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                }
                Spacer(Modifier.weight(1f))
                Text("Account", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.size(48.dp))
            }

            Spacer(Modifier.height(16.dp))

            // Avatar + name
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        user?.firstName?.take(1)?.uppercase() ?: "A",
                        color = Color.White, fontWeight = FontWeight.Bold, fontSize = 28.sp
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    user?.displayName ?: "User",
                    color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold
                )
                user?.email?.let {
                    Text(it, color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp)
                }
            }
        }

        // Content
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                SectionLabel("PROFILE")
                AccountRow(Icons.Default.Person, "Edit profile", "Name, photo, phone") {
                    onNavigate(NavDestination.EditProfile)
                }
                RowDivider()
                AccountRow(Icons.Default.CurrencyExchange, "Currency", user?.currency ?: "INR") {}

                SectionLabel("PREFERENCES")
                AccountRow(Icons.Default.Notifications, "Notifications", "Push, email, reminders") {
                    onNavigate(NavDestination.Notifications)
                }
                RowDivider()
                AccountRow(Icons.Default.Security, "Security", "Password, biometric, sessions") {
                    onNavigate(NavDestination.Security)
                }

                SectionLabel("DATA")
                AccountRow(Icons.Default.FileUpload, "Export data", "CSV or PDF") {
                    onNavigate(NavDestination.Export)
                }
                RowDivider()
                AccountRow(Icons.Default.FileDownload, "Import data", "CSV, Excel, Splitwise") {
                    onNavigate(NavDestination.Import)
                }

                Spacer(Modifier.height(24.dp))

                // Log out
                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Negative),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Negative)
                ) {
                    Icon(Icons.Default.Logout, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Log out", fontWeight = FontWeight.SemiBold)
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    "SplitEZ v1.0.0",
                    fontSize = 12.sp, color = TextTertiary,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun AccountRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
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
        Icon(Icons.Default.ChevronRight, null, tint = TextTertiary)
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
        color = Primary, letterSpacing = 0.5.sp,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
    )
}

@Composable
private fun RowDivider() {
    HorizontalDivider(modifier = Modifier.padding(start = 56.dp, end = 20.dp))
}
