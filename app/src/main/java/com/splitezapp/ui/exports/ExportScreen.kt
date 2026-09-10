package com.splitezapp.ui.exports

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.splitezapp.data.api.ApiClient
import com.splitezapp.ui.theme.*

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
                    exporting = true
                    exportType = "csv"
                    val url = ApiClient.baseUrl + "exports/expenses?format=csv"
                    openInBrowser(context, url)
                    exporting = false
                    message = "CSV download started in browser"
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
                    exporting = true
                    exportType = "pdf"
                    val url = ApiClient.baseUrl + "exports/expenses?format=pdf"
                    openInBrowser(context, url)
                    exporting = false
                    message = "PDF download started in browser"
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

private fun openInBrowser(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (_: Exception) {}
}
