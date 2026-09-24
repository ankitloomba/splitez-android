package com.splitezapp.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.splitezapp.data.models.UserProfile
import com.splitezapp.ui.theme.*

@Composable
fun EditProfileScreen(
    user: UserProfile?,
    onBack: () -> Unit
) {
    var fullName by remember { mutableStateOf(user?.displayName ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var phone by remember { mutableStateOf(user?.phone ?: "") }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val isEmailVerified = email == user?.email && user?.email != null
    val isPhoneVerified = phone == user?.phone && user?.phone != null

    Column(modifier = Modifier.fillMaxSize()) {
        // Dark header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkBg)
                .padding(horizontal = 20.dp)
                .padding(top = 48.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Nav bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                }
                Spacer(Modifier.weight(1f))
                Text("Edit profile", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.size(48.dp))
            }

            Spacer(Modifier.height(16.dp))

            // Avatar with camera icon
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(Primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        user?.firstName?.take(1)?.uppercase() ?: "A",
                        color = Color.White, fontWeight = FontWeight.Bold, fontSize = 36.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(DarkBg)
                        .border(2.dp, DarkBg, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CameraAlt, "Change photo",
                        tint = Color.White, modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Text("Change photo", color = TextSecondary, fontSize = 12.sp)
        }

        // White content
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(top = 24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Full Name
                Text(
                    "FULL NAME", fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                    color = Primary, letterSpacing = 0.5.sp
                )
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    placeholder = { Text("Full name") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary.copy(alpha = 0.3f),
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(20.dp))

                // Email
                Text(
                    "EMAIL", fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                    color = Primary, letterSpacing = 0.5.sp
                )
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("Email address") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isEmailVerified) Color(0xFFE0E0E0) else Color(0xFFFF9800).copy(alpha = 0.5f),
                        unfocusedBorderColor = if (isEmailVerified) Color(0xFFE0E0E0) else Color(0xFFFF9800).copy(alpha = 0.5f)
                    ),
                    trailingIcon = {
                        if (!isEmailVerified) {
                            TextButton(onClick = {}) {
                                Text("Verify", color = Primary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                if (!isEmailVerified) {
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Info, null,
                            tint = Color(0xFFFF9800), modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Not verified · we'll send a link to this email",
                            fontSize = 12.sp, color = Color(0xFFFF9800)
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Phone
                Text(
                    "PHONE", fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                    color = Primary, letterSpacing = 0.5.sp
                )
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    placeholder = { Text("Phone number") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFE0E0E0),
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    ),
                    leadingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Spacer(Modifier.width(12.dp))
                            Text("+91", fontSize = 16.sp, color = TextSecondary)
                            Spacer(Modifier.width(8.dp))
                            Divider(
                                modifier = Modifier
                                    .height(20.dp)
                                    .width(1.dp)
                            )
                        }
                    },
                    trailingIcon = {
                        if (isPhoneVerified) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Check, null,
                                    tint = Positive, modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("Verified", color = Positive, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Changing your number needs an OTP",
                    fontSize = 12.sp, color = TextTertiary
                )

                Spacer(Modifier.height(40.dp))

                // Save button
                Button(
                    onClick = { onBack() },
                    enabled = fullName.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Save changes", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                // Delete account
                Spacer(Modifier.height(16.dp))
                TextButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Delete account", color = Negative, fontWeight = FontWeight.SemiBold)
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Account") },
            text = { Text("This action cannot be undone. All your data will be permanently deleted.") },
            confirmButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Delete", color = Negative)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
