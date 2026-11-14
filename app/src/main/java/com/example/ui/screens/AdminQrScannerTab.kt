package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.model.ApprovalStatus
import com.example.data.model.CaduceusUser
import com.example.data.repository.CaduceusRepository
import com.example.ui.theme.AntiqueGold
import com.example.ui.theme.BorderDark
import com.example.ui.theme.DarkBloodRed
import com.example.ui.theme.DarkObsidian
import com.example.ui.theme.PrimaryCrimson
import com.example.ui.theme.SolarGold
import com.example.ui.theme.StatusCompleted
import com.example.ui.theme.StatusStartingSoon
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextLightGrey
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.VoidBlack
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.Executors

@Composable
fun QrVerifierTab(
    allUsers: List<CaduceusUser>,
    currentAdminEmail: String,
    repository: CaduceusRepository
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var isCameraActive by remember { mutableStateOf(hasCameraPermission) }
    var queryId by remember { mutableStateOf("") }
    var lastRawQr by remember { mutableStateOf("") }
    var isUpdatingCheckIn by remember { mutableStateOf(false) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        isCameraActive = granted
        if (!granted) {
            Toast.makeText(
                context,
                "Camera permission is required for live QR scanning. You can still verify Pass IDs manually below.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    val matchedUser = remember(queryId, allUsers) {
        findMatchingUser(queryId, allUsers)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "FEST PASS QR SCANNER & GATE VERIFIER",
            style = MaterialTheme.typography.titleMedium.copy(
                color = TextWhite,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = "Scan an attendee's Digital Fest Pass QR code or enter their Pass ID to check verification & admission status.",
            style = MaterialTheme.typography.bodySmall.copy(color = TextLightGrey)
        )

        // Camera Scanner Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("qr_camera_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.verticalGradient(listOf(AntiqueGold, DarkBloodRed)),
                width = 1.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (hasCameraPermission && isCameraActive) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(2.dp, AntiqueGold, RoundedCornerShape(12.dp))
                    ) {
                        CameraQrPreview(
                            onQrDetected = { rawValue ->
                                if (rawValue != lastRawQr) {
                                    lastRawQr = rawValue
                                    queryId = extractPassLookupKey(rawValue)
                                    isCameraActive = false
                                }
                            }
                        )
                        // Viewfinder overlay frame
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(170.dp)
                                .border(2.dp, PrimaryCrimson, RoundedCornerShape(14.dp))
                        )
                        Surface(
                            color = VoidBlack.copy(alpha = 0.72f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "Align Fest Pass QR Code inside frame",
                                style = MaterialTheme.typography.labelSmall.copy(color = AntiqueGold),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { isCameraActive = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("stop_qr_camera_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("PAUSE CAMERA", style = MaterialTheme.typography.labelMedium.copy(color = TextLightGrey))
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkObsidian),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "QR Scanner Ready",
                                tint = AntiqueGold,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (hasCameraPermission) {
                                    "Scanner paused. Tap below to scan another pass."
                                } else {
                                    "Allow camera access to scan Digital Fest Pass QR codes."
                                },
                                style = MaterialTheme.typography.bodySmall.copy(color = TextLightGrey),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (hasCameraPermission) {
                                lastRawQr = ""
                                queryId = ""
                                isCameraActive = true
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("start_qr_camera_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryCrimson),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Open Camera Scanner",
                            tint = TextWhite
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (hasCameraPermission) "OPEN CAMERA QR SCANNER" else "GRANT CAMERA & SCAN QR",
                            style = MaterialTheme.typography.labelLarge.copy(color = TextWhite)
                        )
                    }
                }
            }
        }

        // Manual Lookup Field
        OutlinedTextField(
            value = queryId,
            onValueChange = { queryId = it },
            label = { Text("Pass ID / Scanned Payload (e.g. CAD26-...)") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("qr_input_field"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AntiqueGold,
                unfocusedBorderColor = BorderDark,
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                cursorColor = PrimaryCrimson
            ),
            singleLine = true
        )

        // Verification Result Card
        if (queryId.isNotBlank()) {
            if (matchedUser != null) {
                val isApproved = matchedUser.approvalStatus == ApprovalStatus.APPROVED.name
                val borderColor = if (isApproved) StatusCompleted else StatusStartingSoon
                val checkedInTime = remember(matchedUser.checkedInAt) {
                    matchedUser.checkedInAt?.toDate()?.let { date ->
                        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).apply {
                            timeZone = TimeZone.getTimeZone("Asia/Kolkata")
                        }.format(date) + " IST"
                    } ?: ""
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("qr_verification_result_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isApproved) DarkBloodRed.copy(alpha = 0.4f) else SurfaceCard
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.verticalGradient(listOf(borderColor, AntiqueGold)),
                        width = 1.5.dp
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isApproved) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = "Verification Result",
                                tint = borderColor,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isApproved) "PASS VERIFIED & VALID" else "PASS NOT VERIFIED (${matchedUser.approvalStatus})",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = borderColor,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Attendee: ${matchedUser.fullName}",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = TextWhite,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Email: ${matchedUser.email}",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextLightGrey)
                        )
                        Text(
                            text = "Pass ID: ${matchedUser.festPassId}",
                            style = MaterialTheme.typography.labelMedium.copy(color = AntiqueGold)
                        )
                        Text(
                            text = "Role: ${matchedUser.role} • Status: ${matchedUser.approvalStatus}",
                            style = MaterialTheme.typography.labelSmall.copy(color = SolarGold)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            color = if (matchedUser.checkedIn) StatusCompleted.copy(alpha = 0.18f) else DarkObsidian,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (matchedUser.checkedIn) Icons.Default.Verified else Icons.Default.QrCodeScanner,
                                    contentDescription = "Check-In State",
                                    tint = if (matchedUser.checkedIn) StatusCompleted else TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (matchedUser.checkedIn) {
                                            "ALREADY CHECKED IN AT GATE"
                                        } else {
                                            "NOT YET CHECKED IN"
                                        },
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = if (matchedUser.checkedIn) StatusCompleted else TextWhite,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    if (matchedUser.checkedIn && checkedInTime.isNotBlank()) {
                                        Text(
                                            text = "Verified by ${matchedUser.checkedInBy} • $checkedInTime",
                                            style = MaterialTheme.typography.labelSmall.copy(color = TextLightGrey)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (isApproved) {
                                Button(
                                    onClick = {
                                        isUpdatingCheckIn = true
                                        scope.launch {
                                            try {
                                                val nextState = !matchedUser.checkedIn
                                                repository.markUserCheckedIn(
                                                    targetUid = matchedUser.userId,
                                                    checkedIn = nextState,
                                                    adminEmail = currentAdminEmail
                                                )
                                                Toast.makeText(
                                                    context,
                                                    if (nextState) "Attendee checked in!" else "Check-in cleared.",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                            } finally {
                                                isUpdatingCheckIn = false
                                            }
                                        }
                                    },
                                    enabled = !isUpdatingCheckIn,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("qr_toggle_checkin_button"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (matchedUser.checkedIn) DarkObsidian else PrimaryCrimson
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Check In",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (matchedUser.checkedIn) "UNDO CHECK-IN" else "MARK VERIFIED ENTRY",
                                        style = MaterialTheme.typography.labelSmall.copy(color = TextWhite)
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    lastRawQr = ""
                                    queryId = ""
                                    if (hasCameraPermission) {
                                        isCameraActive = true
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("qr_scan_next_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = DarkBloodRed),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Scan Next",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "SCAN NEXT PASS",
                                    style = MaterialTheme.typography.labelSmall.copy(color = TextWhite)
                                )
                            }
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("qr_invalid_result_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.verticalGradient(listOf(PrimaryCrimson, BorderDark)),
                        width = 1.5.dp
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = "Invalid Pass",
                            tint = PrimaryCrimson,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "UNVERIFIED OR INVALID PASS",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = PrimaryCrimson,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "No registered user matches '$queryId'. Do not grant gate entry.",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextLightGrey),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                lastRawQr = ""
                                queryId = ""
                                if (hasCameraPermission) {
                                    isCameraActive = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkBloodRed),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text("TRY ANOTHER SCAN", style = MaterialTheme.typography.labelSmall.copy(color = TextWhite))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalGetImage::class)
@Composable
private fun CameraQrPreview(
    onQrDetected: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    val barcodeScanner = remember {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        BarcodeScanning.getClient(options)
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                barcodeScanner.close()
                analysisExecutor.shutdown()
            } catch (_: Exception) {}
        }
    }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    imageAnalysis.setAnalyzer(analysisExecutor) { imageProxy ->
                        val mediaImage = imageProxy.image
                        if (mediaImage != null) {
                            val inputImage = InputImage.fromMediaImage(
                                mediaImage,
                                imageProxy.imageInfo.rotationDegrees
                            )
                            barcodeScanner.process(inputImage)
                                .addOnSuccessListener { barcodes ->
                                    val value = barcodes.firstOrNull { !it.rawValue.isNullOrBlank() }?.rawValue
                                    if (!value.isNullOrBlank()) {
                                        onQrDetected(value)
                                    }
                                }
                                .addOnCompleteListener {
                                    imageProxy.close()
                                }
                        } else {
                            imageProxy.close()
                        }
                    }

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
                } catch (_: Exception) {}
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

private fun extractPassLookupKey(rawValue: String): String {
    val trimmed = rawValue.trim()
    if (trimmed.startsWith("CADUCEUS26|", ignoreCase = true)) {
        val parts = trimmed.split("|")
        if (parts.size >= 2 && parts[1].isNotBlank()) {
            return parts[1].trim()
        }
    }
    return trimmed
}

private fun findMatchingUser(query: String, allUsers: List<CaduceusUser>): CaduceusUser? {
    val normalized = extractPassLookupKey(query)
    if (normalized.isBlank()) return null
    val rawParts = query.trim().split("|")
    val uidPart = if (rawParts.size >= 3) rawParts[2].trim() else ""

    return allUsers.firstOrNull { user ->
        user.festPassId.equals(normalized, ignoreCase = true) ||
            user.userId.equals(normalized, ignoreCase = true) ||
            (uidPart.isNotBlank() && user.userId.equals(uidPart, ignoreCase = true)) ||
            user.email.equals(normalized, ignoreCase = true)
    }
}
