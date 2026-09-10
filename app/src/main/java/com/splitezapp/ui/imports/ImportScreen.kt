package com.splitezapp.ui.imports

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.splitezapp.data.api.ApiClient
import com.splitezapp.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(onBack: () -> Unit) {
    var selectedFile by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf("") }
    var importing by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<ImportResult?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            selectedFile = it
            fileName = it.lastPathSegment ?: "file"
            result = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import Expenses") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text(
                "Import from file",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Upload a CSV or Excel file to bulk import expenses. Supports Splitwise export format.",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontSize = 14.sp
            )
            Spacer(Modifier.height(24.dp))

            // Drop zone
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .border(
                        2.dp,
                        if (selectedFile != null) Accent else Primary.copy(alpha = 0.3f),
                        RoundedCornerShape(12.dp)
                    ),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedFile != null)
                        Accent.copy(alpha = 0.05f)
                    else
                        Primary.copy(alpha = 0.03f)
                ),
                onClick = {
                    filePicker.launch(arrayOf(
                        "text/csv",
                        "application/vnd.ms-excel",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        "application/octet-stream"
                    ))
                }
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (selectedFile != null) {
                        Text("📎", fontSize = 36.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            fileName,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(4.dp))
                        TextButton(onClick = {
                            filePicker.launch(arrayOf(
                                "text/csv",
                                "application/vnd.ms-excel",
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                "application/octet-stream"
                            ))
                        }) {
                            Text("Change file")
                        }
                    } else {
                        Icon(
                            Icons.Default.UploadFile, null,
                            tint = Primary.copy(alpha = 0.6f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Tap to select a file",
                            fontWeight = FontWeight.Medium,
                            color = Primary
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "CSV, XLS, XLSX · Max 5 MB",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Supported formats
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Supported formats", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    FormatRow("Splitwise export", "CSV from Splitwise → Settings → Export")
                    FormatRow("Generic CSV", "Columns: Date, Description, Amount, Category")
                    FormatRow("Excel", "XLS/XLSX with the same column layout")
                }
            }

            Spacer(Modifier.height(24.dp))

            // Import button
            Button(
                onClick = {
                    selectedFile?.let { uri ->
                        importing = true
                        result = null
                        scope.launch {
                            result = uploadFile(context, uri, fileName)
                            importing = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedFile != null && !importing,
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                if (importing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Importing…")
                } else {
                    Text("Import Expenses")
                }
            }

            // Result
            result?.let { res ->
                Spacer(Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (res.success) Positive.copy(alpha = 0.1f)
                        else Negative.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (res.success) "✅" else "❌", fontSize = 24.sp)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                if (res.success) "Import successful!" else "Import failed",
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                res.message,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FormatRow(title: String, description: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text("•", modifier = Modifier.padding(end = 8.dp))
        Column {
            Text(title, fontWeight = FontWeight.Medium, fontSize = 13.sp)
            Text(description, fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    }
}

private data class ImportResult(val success: Boolean, val message: String)

private suspend fun uploadFile(
    context: android.content.Context,
    uri: Uri,
    fileName: String
): ImportResult = withContext(Dispatchers.IO) {
    try {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: return@withContext ImportResult(false, "Could not read file")
        val bytes = inputStream.readBytes()
        inputStream.close()

        val mediaType = when {
            fileName.endsWith(".csv", true) -> "text/csv"
            fileName.endsWith(".xlsx", true) ->
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            fileName.endsWith(".xls", true) -> "application/vnd.ms-excel"
            else -> "application/octet-stream"
        }

        val body = bytes.toRequestBody(mediaType.toMediaType())
        val part = MultipartBody.Part.createFormData("file", fileName, body)

        val response = ApiClient.rawClient.newCall(
            okhttp3.Request.Builder()
                .url(ApiClient.baseUrl + "imports/expenses")
                .addHeader("Authorization", "Bearer ${ApiClient.token}")
                .post(
                    MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addPart(part)
                        .build()
                )
                .build()
        ).execute()

        if (response.isSuccessful) {
            ImportResult(true, "Expenses imported successfully")
        } else {
            ImportResult(false, response.body?.string() ?: "Server error ${response.code}")
        }
    } catch (e: Exception) {
        ImportResult(false, e.message ?: "Unknown error")
    }
}
