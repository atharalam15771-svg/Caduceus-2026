package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.data.model.CaduceusUser
import com.example.data.model.CaduceusVenue
import com.example.data.model.FestSettings
import com.example.data.model.LeaderboardEntry
import com.example.data.model.UserRole
import com.example.data.util.CaduceusAudioManager
import com.example.ui.theme.AntiqueGold
import com.example.ui.theme.BorderDark
import com.example.ui.theme.DarkBloodRed
import com.example.ui.theme.DarkObsidian
import com.example.ui.theme.PrimaryCrimson
import com.example.ui.theme.SolarGold
import com.example.ui.theme.StatusCompleted
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardElevated
import com.example.ui.theme.TextLightGrey
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.VoidBlack

@Composable
fun ProfileAndMoreScreen(
    user: CaduceusUser,
    festSettings: FestSettings,
    venues: List<CaduceusVenue>,
    leaderboard: List<LeaderboardEntry>,
    onOpenDigitalPass: () -> Unit,
    onOpenAdminPortal: () -> Unit,
    onSignOut: () -> Unit,
    onEditPersonalDetails: () -> Unit = {}
) {
    val context = LocalContext.current
    var isAudioEnabled by remember { mutableStateOf(CaduceusAudioManager.isAudioEnabled(context)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "FESTIVAL PROFILE",
            style = MaterialTheme.typography.displaySmall.copy(
                fontFamily = MaterialTheme.typography.displaySmall.fontFamily,
                color = TextWhite,
                letterSpacing = 1.sp
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Profile Badge Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.verticalGradient(listOf(AntiqueGold, BorderDark)),
                width = 1.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(DarkBloodRed)
                            .border(1.dp, AntiqueGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.fullName.take(1).uppercase(),
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = TextWhite,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user.fullName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = TextWhite,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.bodySmall.copy(color = TextLightGrey)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val rollText = user.rollNumber.ifBlank { user.studentId }
                        val secondaryInfo = listOfNotNull(
                            user.collegeName.takeIf { it.isNotBlank() && it != "CADUCEUS 2026" },
                            user.designation.takeIf { it.isNotBlank() },
                            user.batch.takeIf { it.isNotBlank() }?.let { "Batch $it" },
                            rollText.takeIf { it.isNotBlank() }?.let { "Roll $it" }
                        ).joinToString(" • ").ifBlank {
                            "${user.collegeName} • ${user.department}"
                        }
                        Text(
                            text = secondaryInfo,
                            style = MaterialTheme.typography.labelSmall.copy(color = AntiqueGold)
                        )
                    }

                    Surface(
                        color = DarkBloodRed,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = user.role,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SolarGold,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        CaduceusAudioManager.playTap(context)
                        onEditPersonalDetails()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("profile_edit_details_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Personal Details",
                        tint = AntiqueGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (user.profileCompleted) "EDIT PERSONAL DETAILS" else "ADD PERSONAL DETAILS",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = AntiqueGold,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Pass Launch
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenDigitalPass() },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkBloodRed.copy(alpha = 0.7f)),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(listOf(AntiqueGold, PrimaryCrimson)),
                width = 1.dp
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.QrCode2,
                    contentDescription = "Pass",
                    tint = AntiqueGold,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "VIEW DIGITAL FEST PASS",
                        style = MaterialTheme.typography.labelLarge.copy(color = TextWhite)
                    )
                    Text(
                        text = "Registration ID: ${user.festPassId}",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextLightGrey)
                    )
                }
            }
        }

        // Admin Portal Launch (if Admin)
        if (user.role == UserRole.SUPER_ADMIN.name || user.role == UserRole.EVENT_ADMIN.name) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_admin_portal_card")
                    .clickable { onOpenAdminPortal() },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryCrimson.copy(alpha = 0.25f)),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(PrimaryCrimson, AntiqueGold)),
                    width = 1.5.dp
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Admin",
                        tint = PrimaryCrimson,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ORGANISER & ADMIN COMMAND PORTAL",
                            style = MaterialTheme.typography.labelLarge.copy(color = TextWhite)
                        )
                        Text(
                            text = "Approve accounts, broadcast push alerts, edit events & scan QR",
                            style = MaterialTheme.typography.bodySmall.copy(color = AntiqueGold)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Single Official Venue Address
        Text(
            text = "FESTIVAL VENUE & ADDRESS",
            style = MaterialTheme.typography.titleSmall.copy(
                fontFamily = MaterialTheme.typography.displaySmall.fontFamily,
                color = AntiqueGold,
                letterSpacing = 1.sp
            )
        )
        Spacer(modifier = Modifier.height(10.dp))

        val officialVenueName = venues.firstOrNull()?.name?.takeIf { it.isNotBlank() }
            ?: "Deben Mahata Government Medical College And Hospital"
        val officialVenueAddress = venues.firstOrNull()?.description?.takeIf { it.isNotBlank() }
            ?: "Hatuara, Purulia - 723147"

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("profile_venue_address_card"),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Official Venue Address",
                    tint = AntiqueGold
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = officialVenueName,
                        style = MaterialTheme.typography.titleSmall.copy(color = TextWhite)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = officialVenueAddress,
                        style = MaterialTheme.typography.bodySmall.copy(color = TextLightGrey)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        try {
                            val mapIntent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("geo:0,0?q=${Uri.encode("$officialVenueName, $officialVenueAddress")}")
                            )
                            context.startActivity(mapIntent)
                        } catch (_: Exception) {}
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBloodRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("profile_venue_map_button")
                ) {
                    Text("MAP", style = MaterialTheme.typography.labelSmall.copy(color = TextWhite))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Competition Leaderboard Preview
        Text(
            text = "COMPETITION LEADERBOARD",
            style = MaterialTheme.typography.titleSmall.copy(
                fontFamily = MaterialTheme.typography.displaySmall.fontFamily,
                color = AntiqueGold,
                letterSpacing = 1.sp
            )
        )
        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (leaderboard.isEmpty()) {
                    Text(
                        text = "Stay tuned! Verified competition results and points will appear here once CADUCEUS 2026 begins on 17th December 2026.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextLightGrey),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    leaderboard.take(5).forEach { entry ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = if (entry.rank == 1L) AntiqueGold else DarkBloodRed,
                                    shape = CircleShape,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "#${entry.rank}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (entry.rank == 1L) VoidBlack else TextWhite,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = entry.participantName,
                                        style = MaterialTheme.typography.titleSmall.copy(color = TextWhite)
                                    )
                                    Text(
                                        text = "${entry.competitionName} • ${entry.collegeName}",
                                        style = MaterialTheme.typography.bodySmall.copy(color = TextLightGrey)
                                    )
                                }
                            }
                            Text(
                                text = "${entry.points} PTS",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = AntiqueGold,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Social Media & Official Channels
        Text(
            text = "CONNECT WITH CADUCEUS",
            style = MaterialTheme.typography.titleSmall.copy(
                fontFamily = MaterialTheme.typography.displaySmall.fontFamily,
                color = AntiqueGold,
                letterSpacing = 1.sp
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Instagram: @caduceus.dmgmch  •  Email: ${festSettings.officialEmail}",
            style = MaterialTheme.typography.bodySmall.copy(color = TextLightGrey)
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    CaduceusAudioManager.playTap(context)
                    try {
                        val igUrl = festSettings.instagramUrl.ifBlank {
                            "https://www.instagram.com/caduceus.dmgmch?vrfl=OGtwOGtpZDE3dXBv"
                        }
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(igUrl))
                        context.startActivity(intent)
                    } catch (_: Exception) {}
                },
                modifier = Modifier.weight(1f).height(48.dp).testTag("profile_instagram_button"),
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("@caduceus.dmgmch", style = MaterialTheme.typography.labelSmall.copy(color = TextWhite), maxLines = 1)
            }
            Button(
                onClick = {
                    CaduceusAudioManager.playTap(context)
                    try {
                        val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${festSettings.officialEmail}"))
                        context.startActivity(emailIntent)
                    } catch (_: Exception) {}
                },
                modifier = Modifier.weight(1f).height(48.dp).testTag("profile_email_button"),
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Email Fest", style = MaterialTheme.typography.labelSmall.copy(color = TextWhite))
            }
            Button(
                onClick = {
                    CaduceusAudioManager.playTap(context)
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(festSettings.youtubeUrl))
                        context.startActivity(intent)
                    } catch (_: Exception) {}
                },
                modifier = Modifier.weight(1f).height(48.dp).testTag("profile_youtube_button"),
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("YouTube", style = MaterialTheme.typography.labelSmall.copy(color = TextWhite))
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Low-Volume BGM & SFX Preference Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("profile_audio_card"),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = if (isAudioEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                        contentDescription = "Festival Ambient Sound",
                        tint = if (isAudioEnabled) AntiqueGold else TextMuted,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "FESTIVAL BGM & SOUND EFFECTS",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = TextWhite,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = if (isAudioEnabled) "Soft background theme & tactile chimes enabled" else "Muted",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextLightGrey)
                        )
                    }
                }
                Switch(
                    checked = isAudioEnabled,
                    onCheckedChange = {
                        isAudioEnabled = CaduceusAudioManager.toggleAudio(context)
                        if (isAudioEnabled) {
                            CaduceusAudioManager.playTap(context)
                        }
                    },
                    modifier = Modifier.testTag("profile_audio_switch"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AntiqueGold,
                        checkedTrackColor = DarkBloodRed
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sign Out Button
        Button(
            onClick = onSignOut,
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("sign_out_button"),
            colors = ButtonDefaults.buttonColors(containerColor = DarkObsidian),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Sign Out", tint = TextLightGrey)
            Spacer(modifier = Modifier.width(8.dp))
            Text("SIGN OUT FROM CADUCEUS", style = MaterialTheme.typography.labelMedium.copy(color = TextLightGrey))
        }

        Spacer(modifier = Modifier.height(22.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("profile_creator_credit"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Created by",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextMuted,
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "MD ATHAR ALAM",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = AntiqueGold,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
