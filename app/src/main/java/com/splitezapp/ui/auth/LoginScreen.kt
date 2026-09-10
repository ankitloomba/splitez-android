package com.splitezapp.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import com.splitezapp.ui.theme.Negative
import com.splitezapp.ui.theme.Positive
import com.splitezapp.ui.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showForgotDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Icon(
            Icons.Default.AttachMoney,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(64.dp)
        )
        Text("SplitEZ", style = MaterialTheme.typography.headlineLarge)
        Text("Split expenses with ease", style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(40.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        viewModel.error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { viewModel.login(email, password) },
            modifier = Modifier.fillMaxWidth(),
            enabled = email.isNotEmpty() && password.isNotEmpty() && !viewModel.isLoading
        ) {
            if (viewModel.isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp))
            else Text("Log In")
        }

        TextButton(onClick = { showForgotDialog = true }) {
            Text("Forgot Password?")
        }

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        OutlinedButton(onClick = onNavigateToRegister, modifier = Modifier.fillMaxWidth()) {
            Text("Create Account")
        }
    }

    if (showForgotDialog) {
        var forgotEmail by remember { mutableStateOf("") }
        var sent by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showForgotDialog = false },
            title = { Text("Reset Password") },
            text = {
                Column {
                    if (sent) {
                        Text("If an account exists, you'll receive a reset link.")
                    } else {
                        OutlinedTextField(
                            value = forgotEmail,
                            onValueChange = { forgotEmail = it },
                            label = { Text("Email") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                if (!sent) {
                    TextButton(onClick = {
                        viewModel.forgotPassword(forgotEmail)
                        sent = true
                    }) { Text("Send") }
                } else {
                    TextButton(onClick = { showForgotDialog = false }) { Text("OK") }
                }
            },
            dismissButton = {
                if (!sent) TextButton(onClick = { showForgotDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Create Account") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(32.dp)
        ) {
            OutlinedTextField(value = firstName, onValueChange = { firstName = it },
                label = { Text("First Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(value = lastName, onValueChange = { lastName = it },
                label = { Text("Last Name (optional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(value = email, onValueChange = { email = it },
                label = { Text("Email") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(value = password, onValueChange = { password = it },
                label = { Text("Password (min 8 chars)") }, visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(), singleLine = true)

            if (password.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                PasswordStrengthIndicator(password)
            }

            viewModel.error?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.register(email, password, firstName, lastName.ifEmpty { null }) },
                modifier = Modifier.fillMaxWidth(),
                enabled = firstName.isNotEmpty() && email.isNotEmpty() && password.length >= 8 && !viewModel.isLoading
            ) {
                if (viewModel.isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                else Text("Sign Up")
            }
        }
    }
}

// Password strength

private enum class PasswordStrengthLevel(val label: String, val fraction: Float, val color: @Composable () -> Color) {
    WEAK("Weak", 0.25f, { Negative }),
    FAIR("Fair", 0.5f, { Color(0xFFFF9800) }),
    GOOD("Good", 0.75f, { Primary }),
    STRONG("Strong", 1.0f, { Positive });
}

private fun evaluateStrength(password: String): PasswordStrengthLevel {
    var score = 0
    if (password.length >= 8) score++
    if (password.length >= 12) score++
    if (password.any { it.isUpperCase() }) score++
    if (password.any { it.isLowerCase() }) score++
    if (password.any { it.isDigit() }) score++
    if (password.any { !it.isLetterOrDigit() }) score++
    return when (score) {
        in 0..2 -> PasswordStrengthLevel.WEAK
        3 -> PasswordStrengthLevel.FAIR
        in 4..5 -> PasswordStrengthLevel.GOOD
        else -> PasswordStrengthLevel.STRONG
    }
}

@Composable
private fun PasswordStrengthIndicator(password: String) {
    val strength = evaluateStrength(password)
    val color = strength.color()

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // Strength bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(strength.fraction)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
        }

        Text(strength.label, style = MaterialTheme.typography.labelSmall, color = color)

        // Rules
        PasswordRule("At least 8 characters", password.length >= 8)
        PasswordRule("Uppercase letter", password.any { it.isUpperCase() })
        PasswordRule("Lowercase letter", password.any { it.isLowerCase() })
        PasswordRule("Number", password.any { it.isDigit() })
        PasswordRule("Special character", password.any { !it.isLetterOrDigit() })
    }
}

@Composable
private fun PasswordRule(text: String, met: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            if (met) "✓" else "○",
            style = MaterialTheme.typography.labelSmall,
            color = if (met) Positive else MaterialTheme.colorScheme.outline
        )
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            color = if (met) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
        )
    }
}
