package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ApprovalStatus
import com.example.data.model.CaduceusUser
import com.example.ui.theme.AntiqueGold
import com.example.ui.theme.BorderDark
import com.example.ui.theme.DarkBloodRed
import com.example.ui.theme.DarkObsidian
import com.example.ui.theme.PrimaryCrimson
import com.example.ui.theme.StatusStartingSoon
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextLightGrey
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.VoidBlack

@Composable
fun PendingApprovalScreen(
    user: CaduceusUser,
    isCheckingStatus: Boolean,
    statusMessage: String?,
    onCheckStatus: () -> Unit,
    onSignOut: () -> Unit
) {
    val isRejected = user.approvalStatus == ApprovalStatus.REJECTED.name
    val accentColor = if (isRejected) PrimaryCrimson else AntiqueGold

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
            .padding(20.dp)
            .testTag("pending_approval_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Image(
                painter = painterResource(id = R.drawable.caduceus_logo),
                contentDescription = "CADUCEUS 2026 Official Logo",
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, accentColor, CircleShape),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "CADUCEUS 2026",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = MaterialTheme.typography.displaySmall.fontFamily,
                    color = TextWhite,
                    letterSpacing = 1.5.sp
                )
            )
            Text(
                text = "DEBEN MAHATA GOVT. MEDICAL COLLEGE & HOSPITAL",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = AntiqueGold,
                    letterSpacing = 0.8.sp,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(22.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pending_approval_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.verticalGradient(listOf(accentColor, BorderDark)),
                    width = 1.5.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(DarkObsidian)
                            .border(1.dp, accentColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isRejected) Icons.Default.Block else Icons.Default.HourglassTop,
                            contentDescription = if (isRejected) "Access Not Approved" else "Pending Admin Approval",
                            tint = accentColor,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        color = if (isRejected) PrimaryCrimson.copy(alpha = 0.22f) else DarkBloodRed,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isRejected) "ACCESS NOT APPROVED" else "PENDING ADMIN APPROVAL",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isRejected) PrimaryCrimson else StatusStartingSoon,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (isRejected) {
                            "Verification Revoked or Denied"
                        } else {
                            "Awaiting Organiser Verification"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TextWhite,
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isRejected) {
                            "Your account does not currently have approval to enter CADUCEUS 2026. Please contact the CADUCEUS Organising Committee if you believe this is a mistake."
                        } else {
                            "Your sign-in has been recorded. For attendee security, access to festival schedules, events, Mohammed Irfan Star Night details, and your Digital Pass remains locked until verified by an administrator."
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextLightGrey,
                            textAlign = TextAlign.Center,
                            lineHeight = 19.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Surface(
                        color = DarkObsidian,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = "Account Details",
                                    tint = AntiqueGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "REGISTERED ATTENDEE DETAILS",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = AntiqueGold,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = user.fullName.ifBlank { "Caduceus Attendee" },
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            if (user.email.isNotBlank()) {
                                Text(
                                    text = user.email,
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextLightGrey)
                                )
                            }
                            if (user.festPassId.isNotBlank()) {
                                Text(
                                    text = "Pass ID: ${user.festPassId}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                                )
                            }
                        }
                    }

                    if (!statusMessage.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = statusMessage,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = AntiqueGold,
                                textAlign = TextAlign.Center
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onCheckStatus,
                        enabled = !isCheckingStatus,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("pending_check_status_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryCrimson),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isCheckingStatus) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = TextWhite,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Check Approval Status",
                                tint = TextWhite,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CHECK APPROVAL STATUS",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = onSignOut,
                        enabled = !isCheckingStatus,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("pending_sign_out_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Sign Out",
                            tint = AntiqueGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SIGN OUT",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = AntiqueGold,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "This screen updates automatically as soon as an admin approves your account.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    fontSize = 11.sp
                )
            )
        }
    }
}
