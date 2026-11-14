package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.model.CaduceusEvent
import com.example.data.model.CaduceusNotification
import com.example.data.model.CaduceusUser
import com.example.data.model.EventStatus
import com.example.data.model.FestSettings
import com.example.data.model.UserRole
import com.example.data.util.CaduceusAudioManager
import com.example.ui.components.CaduceusLogoHeader
import com.example.ui.components.EventCard
import com.example.ui.components.UrgentCalloutBanner
import com.example.ui.theme.AntiqueGold
import com.example.ui.theme.BorderDark
import com.example.ui.theme.DarkBloodRed
import com.example.ui.theme.DarkObsidian
import com.example.ui.theme.PrimaryCrimson
import com.example.ui.theme.StatusLive
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextLightGrey
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.VoidBlack
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Composable
fun HomeScreen(
    user: CaduceusUser,
    events: List<CaduceusEvent>,
    notifications: List<CaduceusNotification>,
    festSettings: FestSettings,
    onNavigateToEvents: () -> Unit,
    onNavigateToPass: () -> Unit,
    onNavigateToZodiac: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onSelectEvent: (CaduceusEvent) -> Unit
) {
    val context = LocalContext.current
    var dismissedNotifId by remember { mutableStateOf<String?>(null) }
    var showPosterZoom by remember { mutableStateOf(false) }
    var isAudioEnabled by remember { mutableStateOf(CaduceusAudioManager.isAudioEnabled(context)) }

    val latestUrgentNotif = notifications.firstOrNull {
        it.id != dismissedNotifId && (it.isPinned || it.category == "URGENT_CALLOUT")
    }

    // Countdown Timer in IST (Asia/Kolkata) targeting 17th December 2026
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTimeMillis = System.currentTimeMillis()
        }
    }

    val (days, hours, minutes, seconds, isFestStarted) = remember(currentTimeMillis, festSettings.festOpeningTimestamp) {
        val targetMillis = try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("Asia/Kolkata")
            }
            val cleanDate = festSettings.festOpeningTimestamp.substringBefore("+").substringBefore("Z")
            sdf.parse(cleanDate)?.time ?: 1797478200000L
        } catch (e: Exception) {
            1797478200000L // 2026-12-17 09:00:00 IST fallback
        }

        val diff = targetMillis - currentTimeMillis
        if (diff <= 0) {
            listOf(0L, 0L, 0L, 0L, true)
        } else {
            val d = diff / (1000 * 60 * 60 * 24)
            val h = (diff / (1000 * 60 * 60)) % 24
            val m = (diff / (1000 * 60)) % 60
            val s = (diff / 1000) % 60
            listOf(d, h, m, s, false)
        }
    }

    val starNightEvent = remember(events) {
        events.firstOrNull {
            it.id == "mohammed_irfan_live" || it.title.contains("MOHAMMED IRFAN", ignoreCase = true)
        } ?: CaduceusEvent(
            id = "mohammed_irfan_live",
            title = "MOHAMMED IRFAN LIVE IN CONCERT",
            category = "Star Night",
            description = "Caduceus Organising Committee, Deben Mahata Government Medical College and Hospital brings Bollywood playback sensation Mohammed Irfan live on stage on 19th December 2026!",
            rules = "Entry strictly with verified CADUCEUS 2026 Digital Fest Pass.",
            venueId = "main_ground",
            venueName = "DMGMCH Main Festival Grounds",
            startTime = "2026-12-19T19:00:00+05:30",
            endTime = "2026-12-19T22:30:00+05:30",
            status = EventStatus.UPCOMING.name,
            coordinatorName = "Caduceus Organising Committee",
            coordinatorPhone = "caduceus.fest@gmail.com",
            isFeatured = true
        )
    }

    val liveEvents = events.filter {
        it.status == EventStatus.LIVE_NOW.name || it.status == EventStatus.STARTING_SOON.name
    }
    val extraEvents = events.filter {
        it.id != starNightEvent.id &&
            !it.title.contains("MOHAMMED IRFAN", ignoreCase = true)
    }

    if (showPosterZoom) {
        Dialog(
            onDismissRequest = { showPosterZoom = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(VoidBlack.copy(alpha = 0.96f))
                    .clickable { showPosterZoom = false }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = { showPosterZoom = false },
                            modifier = Modifier.testTag("close_poster_zoom_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Poster",
                                tint = TextWhite
                            )
                        }
                    }
                    Image(
                        painter = painterResource(id = R.drawable.mohammed_irfan_poster),
                        contentDescription = "Mohammed Irfan Live in Concert on 19th December 2026 Poster",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.5.dp, AntiqueGold, RoundedCornerShape(16.dp))
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            showPosterZoom = false
                            onSelectEvent(starNightEvent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryCrimson),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = "VIEW CONCERT DETAILS",
                            style = MaterialTheme.typography.labelLarge.copy(color = TextWhite)
                        )
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
            .verticalScroll(rememberScrollState())
    ) {
        // Top App Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CaduceusLogoHeader(sizeDp = 38, showTagline = true)

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        isAudioEnabled = CaduceusAudioManager.toggleAudio(context)
                        if (isAudioEnabled) {
                            CaduceusAudioManager.playTap(context)
                        }
                    },
                    modifier = Modifier.testTag("home_audio_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isAudioEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                        contentDescription = if (isAudioEnabled) "Mute Music and Sound Effects" else "Enable Music and Sound Effects",
                        tint = if (isAudioEnabled) AntiqueGold else TextMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }

                IconButton(
                    onClick = onNavigateToPass,
                    modifier = Modifier.testTag("home_pass_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode2,
                        contentDescription = "Digital Fest Pass",
                        tint = AntiqueGold,
                        modifier = Modifier.size(24.dp)
                    )
                }

                BadgedBox(
                    badge = {
                        if (notifications.isNotEmpty()) {
                            Badge(
                                containerColor = PrimaryCrimson,
                                contentColor = TextWhite
                            ) {
                                Text(text = notifications.size.toString())
                            }
                        }
                    }
                ) {
                    IconButton(
                        onClick = onNavigateToNotifications,
                        modifier = Modifier.testTag("home_notifications_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = TextWhite,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                if (user.role == UserRole.SUPER_ADMIN.name || user.role == UserRole.EVENT_ADMIN.name) {
                    IconButton(
                        onClick = onNavigateToAdmin,
                        modifier = Modifier.testTag("home_admin_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Admin Portal",
                            tint = StatusLive,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // Prominent Admin Command Portal Banner for Authorized Admins
        if (user.role == UserRole.SUPER_ADMIN.name || user.role == UserRole.EVENT_ADMIN.name) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clickable { onNavigateToAdmin() }
                    .testTag("home_admin_banner_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkBloodRed.copy(alpha = 0.85f)),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(AntiqueGold, PrimaryCrimson)),
                    width = 1.dp
                )
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
                            imageVector = Icons.Default.Security,
                            contentDescription = "Admin Portal",
                            tint = AntiqueGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "ADMIN DASHBOARD (/admin)",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = AntiqueGold,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "${user.role} • Tap to manage users, QR pass & alerts",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextWhite, fontSize = 11.sp)
                            )
                        }
                    }
                    Surface(
                        color = PrimaryCrimson,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "OPEN",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextWhite,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Urgent Broadcast Banner if any
        if (latestUrgentNotif != null) {
            UrgentCalloutBanner(
                notification = latestUrgentNotif,
                onDismiss = { dismissedNotifId = latestUrgentNotif.id },
                onClick = onNavigateToNotifications
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Hero Card & Fest Countdown to 17th December 2026
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.verticalGradient(listOf(AntiqueGold, DarkBloodRed)),
                width = 1.dp
            )
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "THE NIGHT IS CALLING...",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = AntiqueGold,
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "CADUCEUS 2026",
                        style = MaterialTheme.typography.displayMedium.copy(
                            color = TextWhite,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = festSettings.tagline,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextLightGrey,
                            fontSize = 11.sp
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    if (isFestStarted as Boolean) {
                        Surface(
                            color = DarkBloodRed,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "THE WAIT IS OVER. CADUCEUS 2026 HAS BEGUN!",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = AntiqueGold,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    } else {
                        Text(
                            text = "17TH DECEMBER 2026 • COMMENCES IN (IST):",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = AntiqueGold,
                                fontSize = 10.sp,
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CountdownUnit(value = days as Long, label = "DAYS")
                            Text(" : ", color = AntiqueGold, fontWeight = FontWeight.Bold)
                            CountdownUnit(value = hours as Long, label = "HOURS")
                            Text(" : ", color = AntiqueGold, fontWeight = FontWeight.Bold)
                            CountdownUnit(value = minutes as Long, label = "MINS")
                            Text(" : ", color = AntiqueGold, fontWeight = FontWeight.Bold)
                            CountdownUnit(value = seconds as Long, label = "SECS")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Star Night Poster Reveal Right After Sign-In
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = "Star Night Concert",
                    tint = AntiqueGold,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "STAR NIGHT • 19TH DEC 2026",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = MaterialTheme.typography.displaySmall.fontFamily,
                        color = AntiqueGold,
                        letterSpacing = 1.sp
                    )
                )
            }
            Text(
                text = "Tap to Zoom",
                style = MaterialTheme.typography.labelSmall.copy(color = TextLightGrey),
                modifier = Modifier.clickable {
                    CaduceusAudioManager.playReveal(context)
                    showPosterZoom = true
                }
            )
        }
        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag("home_concert_poster_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.verticalGradient(listOf(AntiqueGold, PrimaryCrimson)),
                width = 1.5.dp
            )
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            CaduceusAudioManager.playReveal(context)
                            showPosterZoom = true
                        }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.mohammed_irfan_poster),
                        contentDescription = "Mohammed Irfan Live in Concert at CADUCEUS 2026 on 19th December 2026",
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Surface(
                        color = VoidBlack.copy(alpha = 0.72f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ZoomIn,
                                contentDescription = "Expand Poster",
                                tint = AntiqueGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "EXPAND",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = AntiqueGold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "MOHAMMED IRFAN LIVE IN CONCERT",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "19th December 2026 • DMGMCH Main Festival Grounds",
                    style = MaterialTheme.typography.bodySmall.copy(color = AntiqueGold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Caduceus Organising Committee brings Bollywood playback sensation Mohammed Irfan to CADUCEUS 2026.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextLightGrey,
                        lineHeight = 18.sp
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { onSelectEvent(starNightEvent) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("home_concert_details_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryCrimson)
                ) {
                    Text(
                        text = "VIEW CONCERT DOSSIER",
                        style = MaterialTheme.typography.labelLarge.copy(color = TextWhite)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Live & Starting Soon Section (if any live events exist)
        if (liveEvents.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = "Live",
                        tint = StatusLive,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "LIVE & STARTING SOON",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontFamily = MaterialTheme.typography.displaySmall.fontFamily,
                            color = TextWhite,
                            letterSpacing = 1.sp
                        )
                    )
                }
                Text(
                    text = "See All",
                    style = MaterialTheme.typography.labelSmall.copy(color = AntiqueGold),
                    modifier = Modifier.clickable { onNavigateToEvents() }
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(liveEvents) { event ->
                    EventCard(
                        event = event,
                        onClick = { onSelectEvent(event) },
                        modifier = Modifier.width(280.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))
        }

        // Stay Tuned for Full Schedule Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag("home_schedule_notice_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(listOf(DarkBloodRed, BorderDark)),
                width = 1.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Schedule",
                        tint = AntiqueGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "FULL FESTIVAL SCHEDULE",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontFamily = MaterialTheme.typography.displaySmall.fontFamily,
                            color = AntiqueGold,
                            letterSpacing = 1.sp
                        )
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Stay tuned for the full schedule!",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "CADUCEUS 2026 begins on 17th December 2026. Event lineups, competition timings, and venue allocations will appear here as soon as the official schedule is released.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextLightGrey,
                        lineHeight = 19.sp
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onNavigateToEvents,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("home_view_events_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBloodRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Events",
                        tint = AntiqueGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "OPEN EVENTS & SCHEDULE",
                        style = MaterialTheme.typography.labelMedium.copy(color = TextWhite)
                    )
                }
            }
        }

        if (extraEvents.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "UPCOMING EVENTS",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = MaterialTheme.typography.displaySmall.fontFamily,
                    color = TextWhite,
                    letterSpacing = 1.sp
                ),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                extraEvents.take(4).forEach { event ->
                    EventCard(
                        event = event,
                        onClick = { onSelectEvent(event) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Horoscope Teaser Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable { onNavigateToZodiac() },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(listOf(DarkBloodRed, AntiqueGold)),
                width = 1.dp
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "♏", fontSize = 38.sp)
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "THE HOROSCOPE CHAMBER",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = TextWhite,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Discover how the 12 celestial zodiacs align with your festival destiny today.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextLightGrey)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Official Contact & Social Channels
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(listOf(BorderDark, AntiqueGold)),
                width = 1.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "OFFICIAL CHANNELS",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = MaterialTheme.typography.displaySmall.fontFamily,
                        color = AntiqueGold,
                        letterSpacing = 1.sp
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("home_instagram_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkObsidian),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = "Instagram",
                            tint = AntiqueGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "@caduceus.dmgmch",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextWhite),
                            maxLines = 1
                        )
                    }
                    Button(
                        onClick = {
                            try {
                                val emailIntent = Intent(
                                    Intent.ACTION_SENDTO,
                                    Uri.parse("mailto:${festSettings.officialEmail}")
                                )
                                context.startActivity(emailIntent)
                            } catch (_: Exception) {}
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("home_email_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkObsidian),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email",
                            tint = AntiqueGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = festSettings.officialEmail,
                            style = MaterialTheme.typography.labelSmall.copy(color = TextWhite),
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun CountdownUnit(value: Long, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(DarkObsidian)
            .border(1.dp, BorderDark, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = String.format("%02d", value),
            style = MaterialTheme.typography.titleLarge.copy(
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = AntiqueGold,
                fontSize = 9.sp
            )
        )
    }
}
