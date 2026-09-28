package com.splitezapp.ui.exports

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.splitezapp.data.api.ApiClient
import com.splitezapp.ui.theme.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(onBack: () -> Unit) {
    var exporting by remember { mutableStateOf(false) }
    var exportType by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Export Data") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                modifier = Modifier.size(20.dp)
                            )
                        }
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
                "Export your expenses",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Download your data as CSV or PDF report",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontSize = 14.sp
            )
            Spacer(Modifier.height(24.dp))

            // CSV export card
            ExportCard(
                icon = "📄",
                title = "CSV Spreadsheet",
                description = "Export all expenses as a CSV file. Opens in Excel, Google Sheets, or any spreadsheet app.",
                buttonText = "Export CSV",
                isLoading = exporting && exportType == "csv",
                onClick = {
                    exporting = true; exportType = "csv"
                    CoroutineScope(Dispatchers.Main).launch {
                        val result = downloadExport(context, "csv")
                        message = result
                        exporting = false
                    }
                }
            )

            Spacer(Modifier.height(16.dp))

            // PDF export card
            ExportCard(
                icon = "📊",
                title = "PDF Report",
                description = "Generate a formatted PDF report with charts and summaries, ready to share or print.",
                buttonText = "Export PDF",
                isLoading = exporting && exportType == "pdf",
                onClick = {
                    exporting = true; exportType = "pdf"
                    CoroutineScope(Dispatchers.Main).launch {
                        val result = downloadExport(context, "pdf")
                        message = result
                        exporting = false
                    }
                }
            )

            message?.let {
                Spacer(Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Positive.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("✅", fontSize = 18.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(it, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ExportCard(
    icon: String,
    title: String,
    description: String,
    buttonText: String,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(icon, fontSize = 28.sp)
                Spacer(Modifier.width(12.dp))
                Text(title, fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                description,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onClick,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(buttonText)
            }
        }
    }
}

private suspend fun downloadExport(context: Context, format: String): String = withContext(Dispatchers.IO) {
    try {
        val url = ApiClient.baseUrl + "exports/expenses?format=$format"
        val req = Request.Builder().url(url)
            .header("Authorization", "Bearer ${ApiClient.token ?: ""}").build()
        val resp = ApiClient.rawClient.newCall(req).execute()
        if (!resp.isSuccessful) return@withContext "Export failed: ${resp.code}"
        val bytes = resp.body?.bytes() ?: return@withContext "Empty response"
        val filename = "splitez_expenses_${System.currentTimeMillis()}.$format"
        val mimeType = if (format == "pdf") "application/pdf" else "text/csv"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val cv = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, filename)
                put(MediaStore.Downloads.MIME_TYPE, mimeType)
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv)
            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { os -> os.write(bytes) }
                cv.clear(); cv.put(MediaStore.Downloads.IS_PENDING, 0)
                context.contentResolver.update(it, cv, null, null)
            }
        } else {
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            dir.mkdirs()
            java.io.File(dir, filename).writeBytes(bytes)
        }
        "Saved to Downloads: $filename"
    } catch (e: Exception) {
        "Export failed: ${e.message}"
    }
}
