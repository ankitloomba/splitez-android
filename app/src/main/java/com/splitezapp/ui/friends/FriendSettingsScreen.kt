package com.splitezapp.ui.friends

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
import com.splitezapp.data.models.*
import com.splitezapp.ui.components.AvatarView
import com.splitezapp.ui.theme.*
import kotlin.math.abs

@Composable
fun FriendSettingsScreen(
    friend: Friend,
    onBack: () -> Unit,
    onGroupTap: (String) -> Unit = {}
) {
    var showRemoveDialog by remember { mutableStateOf(false) }
    var showBlockDialog by remember { mutableStateOf(false) }

    val commonGroups = remember {
        SampleData.groups.filter { group ->
            group.members?.any { it.id == friend.id } == true
        }
    }

    if (showRemoveDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveDialog = false },
            title = { Text("Remove ${friend.firstName}?") },
            text = { Text("This will remove ${friend.firstName} from your friends list. You can add them back later.") },
            confirmButton = {
                TextButton(onClick = { showRemoveDialog = false; onBack() }) {
                    Text("Remove", color = Negative)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showBlockDialog) {
        AlertDialog(
            onDismissRequest = { showBlockDialog = false },
            title = { Text("Block ${friend.firstName}?") },
            text = { Text("Blocked users cannot see your profile or send you friend requests.") },
            confirmButton = {
                TextButton(onClick = { showBlockDialog = false; onBack() }) {
                    Text("Block", color = Negative)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBlockDialog = false }) { Text("Cancel") }
            }
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
                Text("Friend Settings", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.size(48.dp))
            }

            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarView(friend.toUserSummary(), size = 56.dp)
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(friend.displayName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    friend.phone?.let {
                        Text(it, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                    }
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
                // Ad-free upgrade card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFFFF3E0)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, null, tint = Color(0xFFFF9800), modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Go Ad-Free", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("Remove ads · ₹99/mo", fontSize = 12.sp, color = TextSecondary)
                        }
                        OutlinedButton(
                            onClick = {},
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Primary)
                        ) {
                            Text("Upgrade", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                }

                // Actions
                SectionLabel("ACTIONS")

                ActionRow(Icons.Default.PersonRemove, "Remove ${friend.firstName} from list", Negative) {
                    showRemoveDialog = true
                }
                RowDivider()
                ActionRow(Icons.Default.Block, "Block ${friend.firstName}", Negative) {
                    showBlockDialog = true
                }
                RowDivider()
                ActionRow(Icons.Default.Report, "Report ${friend.firstName}", Negative) {}

                // Groups in common
                if (commonGroups.isNotEmpty()) {
                    SectionLabel("GROUPS IN COMMON")

                    commonGroups.forEachIndexed { index, group ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onGroupTap(group.id) }
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(40.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = Primary.copy(alpha = 0.12f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("🏠", fontSize = 18.sp)
                                }
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(group.name, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                group.memberCount?.let {
                                    Text("$it member${if (it == 1) "" else "s"}", fontSize = 12.sp, color = TextSecondary)
                                }
                            }
                            Icon(Icons.Default.ChevronRight, null, tint = TextTertiary)
                        }
                        if (index < commonGroups.lastIndex) {
                            RowDivider()
                        }
                    }
                }

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun ActionRow(icon: ImageVector, title: String, color: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Text(title, fontSize = 14.sp, color = color, modifier = Modifier.weight(1f))
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
