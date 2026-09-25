package com.splitezapp.ui.friends

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.nfc.NfcAdapter
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.splitezapp.data.models.SampleData
import com.splitezapp.ui.theme.*
import kotlin.math.abs

private enum class AddFriendTab(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    EMAIL("Email", Icons.Default.Email),
    SCAN("Scan QR", Icons.Default.QrCodeScanner),
    MY_QR("My QR", Icons.Default.QrCode2),
    NFC("Tap", Icons.Default.Nfc),
    CODE("Enter Code", Icons.Default.Password)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFriendSheet(
    onDismiss: () -> Unit,
    onFriendAdded: () -> Unit = {}
) {
    val context = LocalContext.current
    var activeTab by remember { mutableStateOf(AddFriendTab.EMAIL) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                Modifier
                    .padding(top = 10.dp, bottom = 4.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .background(Color(0xFFE0E0E0), RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Text(
                "Add Friend",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
            )

            // Tab row
            val tabs = AddFriendTab.entries
            ScrollableTabRow(
                selectedTabIndex = tabs.indexOf(activeTab),
                containerColor = Color.White,
                contentColor = Primary,
                edgePadding = 8.dp,
                indicator = { tabPositions ->
                    if (tabs.indexOf(activeTab) < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[tabs.indexOf(activeTab)]),
                            color = Primary
                        )
                    }
                }
            ) {
                tabs.forEach { tab ->
                    Tab(
                        selected = activeTab == tab,
                        onClick = { activeTab = tab },
                        selectedContentColor = Primary,
                        unselectedContentColor = TextSecondary
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
                        ) {
                            Icon(tab.icon, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.height(4.dp))
                            Text(tab.label, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            HorizontalDivider()

            // Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                when (activeTab) {
                    AddFriendTab.EMAIL -> EmailTab(onDismiss, onFriendAdded, context)
                    AddFriendTab.SCAN -> ScanQrTab(onDismiss, onFriendAdded)
                    AddFriendTab.MY_QR -> MyQrTab()
                    AddFriendTab.NFC -> NfcTapTab(context)
                    AddFriendTab.CODE -> EnterCodeTab(onDismiss, onFriendAdded)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// EMAIL TAB
// ---------------------------------------------------------------------------

@Composable
private fun EmailTab(
    onDismiss: () -> Unit,
    onFriendAdded: () -> Unit,
    context: Context
) {
    var email by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedGroupIds by remember { mutableStateOf(setOf<String>()) }
    var showGroupPicker by remember { mutableStateOf(false) }
    val groups = remember { SampleData.groups }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it; error = null },
            placeholder = { Text("friend@example.com") },
            label = { Text("Email address") },
            leadingIcon = { Icon(Icons.Default.Email, null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary.copy(alpha = 0.5f),
                unfocusedBorderColor = Color(0xFFE0E0E0)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        if (error != null) {
            Spacer(Modifier.height(4.dp))
            Text(error!!, fontSize = 12.sp, color = Negative)
        }

        Spacer(Modifier.height(12.dp))

        // Also add to group
        OutlinedButton(
            onClick = { showGroupPicker = !showGroupPicker },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (selectedGroupIds.isNotEmpty()) Primary else Color(0xFFE0E0E0)
            )
        ) {
            Icon(
                Icons.Default.GroupAdd, null,
                modifier = Modifier.size(16.dp),
                tint = if (selectedGroupIds.isNotEmpty()) Primary else TextSecondary
            )
            Spacer(Modifier.width(8.dp))
            Text(
                if (selectedGroupIds.isEmpty()) "Also add to a group (optional)"
                else "${selectedGroupIds.size} group${if (selectedGroupIds.size == 1) "" else "s"} selected",
                color = if (selectedGroupIds.isNotEmpty()) Primary else TextSecondary,
                fontSize = 14.sp
            )
            Spacer(Modifier.weight(1f))
            Icon(
                if (showGroupPicker) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                null, modifier = Modifier.size(16.dp),
                tint = TextSecondary
            )
        }

        AnimatedVisibility(visible = showGroupPicker) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(Modifier.height(8.dp))
                groups.forEach { group ->
                    val isSelected = group.id in selectedGroupIds
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                selectedGroupIds = if (isSelected)
                                    selectedGroupIds - group.id
                                else
                                    selectedGroupIds + group.id
                            }
                            .background(
                                if (isSelected) Primary.copy(alpha = 0.06f) else Color.Transparent
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Group, null, tint = Primary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(group.name, fontSize = 14.sp, modifier = Modifier.weight(1f))
                        if (isSelected) Icon(Icons.Default.CheckCircle, null, tint = Primary, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                if (email.contains("@")) {
                    onFriendAdded()
                    onDismiss()
                } else {
                    error = "Please enter a valid email address."
                }
            },
            enabled = email.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
            Text("Send friend request", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(Modifier.height(16.dp))

        // Divider
        Row(verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text("  or share invite  ", fontSize = 12.sp, color = TextTertiary)
            HorizontalDivider(modifier = Modifier.weight(1f))
        }

        Spacer(Modifier.height(16.dp))

        InviteCodeCard(context = context)
    }
}

// ---------------------------------------------------------------------------
// SCAN QR TAB
// ---------------------------------------------------------------------------

@Composable
private fun ScanQrTab(
    onDismiss: () -> Unit,
    onFriendAdded: () -> Unit
) {
    val context = LocalContext.current
    var scannedCode by remember { mutableStateOf<String?>(null) }
    var hasPermission by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    // GMS Code Scanner
    val scannerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        result.data?.getStringExtra("SCAN_RESULT")?.let { code ->
            scannedCode = code
        }
    }

    LaunchedEffect(Unit) {
        val granted = androidx.core.content.ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.CAMERA
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        hasPermission = granted
        if (!granted) permissionLauncher.launch(android.Manifest.permission.CAMERA)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            "Scan a friend's QR code",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Ask your friend to show their QR code from the \"My QR\" tab",
            fontSize = 13.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(20.dp))

        if (scannedCode != null) {
            // Scanned result
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE8F5E9), RoundedCornerShape(16.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(40.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Code scanned!", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(scannedCode!!, fontSize = 13.sp, color = TextSecondary)
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { onFriendAdded(); onDismiss() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Add Friend", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            TextButton(onClick = { scannedCode = null }) {
                Text("Scan again")
            }
        } else {
            // QR viewfinder placeholder — wire to GMS Code Scanner
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                // Scanner frame corners
                QrScannerFrame()
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    // Launch GMS Code Scanner
                    try {
                        val options = com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions.Builder()
                            .setBarcodeFormats(com.google.mlkit.vision.barcode.common.Barcode.FORMAT_QR_CODE)
                            .build()
                        val scanner = com.google.mlkit.vision.codescanner.GmsBarcodeScanning.getClient(context, options)
                        scanner.startScan()
                            .addOnSuccessListener { barcode ->
                                scannedCode = barcode.rawValue
                            }
                            .addOnFailureListener { /* dismissed */ }
                    } catch (_: Exception) { }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Icon(Icons.Default.QrCodeScanner, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Open Scanner", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

@Composable
private fun QrScannerFrame() {
    val cornerColor = Primary
    val cornerSize = 28.dp
    val strokeWidth = 3.dp

    Box(modifier = Modifier.size(240.dp)) {
        Icon(
            Icons.Default.CameraAlt, null,
            tint = Color(0xFFBDBDBD),
            modifier = Modifier.size(48.dp).align(Alignment.Center)
        )
        // Corners drawn via Canvas
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val cs = cornerSize.toPx()
            val sw = strokeWidth.toPx()
            val color = android.graphics.Color.valueOf(
                cornerColor.red, cornerColor.green, cornerColor.blue
            ).toArgb()
            val paint = androidx.compose.ui.graphics.Paint().apply {
                this.color = androidx.compose.ui.graphics.Color(color)
                this.strokeWidth = sw
                style = androidx.compose.ui.graphics.PaintingStyle.Stroke
                strokeCap = StrokeCap.Round
            }
            // Top-left
            drawContext.canvas.drawLine(androidx.compose.ui.geometry.Offset(0f, cs), androidx.compose.ui.geometry.Offset(0f, 0f), paint)
            drawContext.canvas.drawLine(androidx.compose.ui.geometry.Offset(0f, 0f), androidx.compose.ui.geometry.Offset(cs, 0f), paint)
            // Top-right
            drawContext.canvas.drawLine(androidx.compose.ui.geometry.Offset(size.width - cs, 0f), androidx.compose.ui.geometry.Offset(size.width, 0f), paint)
            drawContext.canvas.drawLine(androidx.compose.ui.geometry.Offset(size.width, 0f), androidx.compose.ui.geometry.Offset(size.width, cs), paint)
            // Bottom-left
            drawContext.canvas.drawLine(androidx.compose.ui.geometry.Offset(0f, size.height - cs), androidx.compose.ui.geometry.Offset(0f, size.height), paint)
            drawContext.canvas.drawLine(androidx.compose.ui.geometry.Offset(0f, size.height), androidx.compose.ui.geometry.Offset(cs, size.height), paint)
            // Bottom-right
            drawContext.canvas.drawLine(androidx.compose.ui.geometry.Offset(size.width, size.height - cs), androidx.compose.ui.geometry.Offset(size.width, size.height), paint)
            drawContext.canvas.drawLine(androidx.compose.ui.geometry.Offset(size.width - cs, size.height), androidx.compose.ui.geometry.Offset(size.width, size.height), paint)
        }
    }
}

// ---------------------------------------------------------------------------
// MY QR TAB
// ---------------------------------------------------------------------------

@Composable
private fun MyQrTab() {
    val context = LocalContext.current
    val inviteCode = rememberInviteCode()
    val qrContent = "splitez://add?code=$inviteCode"
    val qrBitmap = remember(qrContent) { generateQrBitmap(qrContent, 400) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            "Your QR Code",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Let friends scan this to add you",
            fontSize = 13.sp,
            color = TextSecondary
        )

        Spacer(Modifier.height(20.dp))

        // QR code card
        Box(
            modifier = Modifier
                .size(220.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(16.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (qrBitmap != null) {
                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = "QR Code",
                    modifier = Modifier.size(188.dp)
                )
            } else {
                CircularProgressIndicator(color = Primary)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Invite code display
        Box(
            modifier = Modifier
                .background(Primary.copy(alpha = 0.07f), RoundedCornerShape(12.dp))
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(
                inviteCode,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Primary,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 4.sp
            )
        }

        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = {
                    val clip = android.content.ClipData.newPlainText("Invite Code", inviteCode)
                    (context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager)
                        .setPrimaryClip(clip)
                },
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.weight(1f).height(48.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Primary)
            ) {
                Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(16.dp), tint = Primary)
                Spacer(Modifier.width(6.dp))
                Text("Copy", color = Primary, fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT,
                            "Add me on SplitEZ! Use invite code: $inviteCode\nOr scan my QR code in the app.")
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Share via"))
                },
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.weight(1f).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Share", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// NFC TAP TAB
// ---------------------------------------------------------------------------

@Composable
private fun NfcTapTab(context: Context) {
    val nfcAdapter = remember { NfcAdapter.getDefaultAdapter(context) }
    val nfcAvailable = nfcAdapter != null
    val nfcEnabled = nfcAdapter?.isEnabled == true
    var isListening by remember { mutableStateOf(false) }
    val inviteCode = rememberInviteCode()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            "Tap to Connect",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Hold your phones back-to-back to add each other instantly",
            fontSize = 13.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        // NFC animation circle
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(
                    if (isListening && nfcEnabled) Primary.copy(alpha = 0.12f)
                    else Color(0xFFF5F5F5)
                )
                .border(
                    2.dp,
                    if (isListening && nfcEnabled) Primary.copy(alpha = 0.4f) else Color(0xFFEEEEEE),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Nfc, null,
                    modifier = Modifier.size(56.dp),
                    tint = if (isListening && nfcEnabled) Primary else Color(0xFFBDBDBD)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    if (!nfcAvailable) "Not supported"
                    else if (!nfcEnabled) "NFC is off"
                    else if (isListening) "Listening…"
                    else "Ready",
                    fontSize = 13.sp,
                    color = if (isListening && nfcEnabled) Primary else TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        when {
            !nfcAvailable -> {
                Text(
                    "This device doesn't support NFC. Use QR code or invite code instead.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
            !nfcEnabled -> {
                Text(
                    "NFC is turned off. Enable it in Settings to use tap-to-add.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = {
                        context.startActivity(Intent(android.provider.Settings.ACTION_NFC_SETTINGS))
                    },
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Open NFC Settings")
                }
            }
            else -> {
                if (!isListening) {
                    Button(
                        onClick = { isListening = true },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Icon(Icons.Default.Nfc, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Start Tap Mode", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                } else {
                    Text(
                        "Hold phones together now",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Primary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Keep NFC areas (usually back of phone) touching until connected",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = { isListening = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// ENTER CODE TAB
// ---------------------------------------------------------------------------

@Composable
private fun EnterCodeTab(
    onDismiss: () -> Unit,
    onFriendAdded: () -> Unit
) {
    var code by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedGroupIds by remember { mutableStateOf(setOf<String>()) }
    var showGroupPicker by remember { mutableStateOf(false) }
    val groups = remember { SampleData.groups }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            "Enter friend's invite code",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Ask your friend for their 6-character code from the \"My QR\" tab",
            fontSize = 13.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = code.uppercase(),
            onValueChange = { if (it.length <= 6) { code = it.uppercase(); error = null } },
            placeholder = { Text("A B C 1 2 3", fontFamily = FontFamily.Monospace, letterSpacing = 6.sp) },
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 6.sp,
                color = Primary,
                textAlign = TextAlign.Center
            ),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary.copy(alpha = 0.5f),
                unfocusedBorderColor = Color(0xFFE0E0E0),
                focusedContainerColor = Primary.copy(alpha = 0.04f),
                unfocusedContainerColor = Primary.copy(alpha = 0.02f)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        if (error != null) {
            Spacer(Modifier.height(4.dp))
            Text(error!!, fontSize = 12.sp, color = Negative)
        }

        Spacer(Modifier.height(12.dp))

        // Also add to group
        OutlinedButton(
            onClick = { showGroupPicker = !showGroupPicker },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (selectedGroupIds.isNotEmpty()) Primary else Color(0xFFE0E0E0)
            )
        ) {
            Icon(Icons.Default.GroupAdd, null, modifier = Modifier.size(16.dp),
                tint = if (selectedGroupIds.isNotEmpty()) Primary else TextSecondary)
            Spacer(Modifier.width(8.dp))
            Text(
                if (selectedGroupIds.isEmpty()) "Also add to a group (optional)"
                else "${selectedGroupIds.size} group${if (selectedGroupIds.size == 1) "" else "s"} selected",
                color = if (selectedGroupIds.isNotEmpty()) Primary else TextSecondary, fontSize = 14.sp
            )
            Spacer(Modifier.weight(1f))
            Icon(
                if (showGroupPicker) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                null, modifier = Modifier.size(16.dp), tint = TextSecondary
            )
        }

        AnimatedVisibility(visible = showGroupPicker) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(Modifier.height(8.dp))
                groups.forEach { group ->
                    val isSelected = group.id in selectedGroupIds
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                selectedGroupIds = if (isSelected)
                                    selectedGroupIds - group.id else selectedGroupIds + group.id
                            }
                            .background(if (isSelected) Primary.copy(alpha = 0.06f) else Color.Transparent)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Group, null, tint = Primary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(group.name, fontSize = 14.sp, modifier = Modifier.weight(1f))
                        if (isSelected) Icon(Icons.Default.CheckCircle, null, tint = Primary, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                if (code.length == 6) {
                    onFriendAdded(); onDismiss()
                } else {
                    error = "Code must be 6 characters."
                }
            },
            enabled = code.length == 6,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
            Text("Add Friend", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

// ---------------------------------------------------------------------------
// SHARED: Invite Code Card
// ---------------------------------------------------------------------------

@Composable
private fun InviteCodeCard(context: Context) {
    val inviteCode = rememberInviteCode()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Your invite code", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
        Spacer(Modifier.height(8.dp))
        Text(
            inviteCode,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Primary,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 4.sp,
            modifier = Modifier
                .background(Primary.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                .padding(horizontal = 24.dp, vertical = 10.dp)
        )
        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = {
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT,
                        "Join me on SplitEZ! Use my invite code: $inviteCode\n\nDownload SplitEZ and enter this code to connect.")
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(sendIntent, "Share invite"))
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Primary)
        ) {
            Icon(Icons.Default.Share, null, tint = Primary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("Share invite link", fontWeight = FontWeight.SemiBold, color = Primary)
        }
    }
}

// ---------------------------------------------------------------------------
// HELPERS
// ---------------------------------------------------------------------------

@Composable
private fun rememberInviteCode(): String {
    return remember {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val seed = abs((SampleData.currentUser.id + "invite").hashCode())
        buildString {
            var s = seed
            repeat(6) { append(chars[s % chars.length]); s /= chars.length }
        }
    }
}

private fun generateQrBitmap(content: String, size: Int): Bitmap? {
    return try {
        val hints = mapOf(EncodeHintType.MARGIN to 1)
        val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bmp.setPixel(x, y, if (bitMatrix[x, y]) 0xFF1A1F3C.toInt() else 0xFFFFFFFF.toInt())
            }
        }
        bmp
    } catch (_: Exception) { null }
}
