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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.data.model.EventStatus
import com.example.ui.components.EventCard
import com.example.ui.components.EventStatusBadge
import com.example.ui.theme.AntiqueGold
import com.example.ui.theme.BorderDark
import com.example.ui.theme.DarkBloodRed
import com.example.ui.theme.DarkObsidian
import com.example.ui.theme.PrimaryCrimson
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
fun EventsScreen(
    events: List<CaduceusEvent>,
    onSelectEvent: (CaduceusEvent) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }
    var selectedCategory by remember { mutableStateOf("ALL") }

    val statusFilters = listOf("ALL", "LIVE NOW", "STARTING SOON", "UPCOMING", "COMPLETED")
    val categories = remember(events) {
        listOf("ALL") + events.map { it.category }.filter { it.isNotBlank() }.distinct()
    }

    val filteredEvents = events.filter { event ->
        val matchesQuery = searchQuery.isBlank() ||
            event.title.contains(searchQuery, ignoreCase = true) ||
            event.venueName.contains(searchQuery, ignoreCase = true) ||
            event.category.contains(searchQuery, ignoreCase = true)

        val matchesStatus = when (selectedFilter) {
            "LIVE NOW" -> event.status == EventStatus.LIVE_NOW.name
            "STARTING SOON" -> event.status == EventStatus.STARTING_SOON.name
            "UPCOMING" -> event.status == EventStatus.UPCOMING.name
            "COMPLETED" -> event.status == EventStatus.COMPLETED.name
            else -> true
        }

        val matchesCategory = selectedCategory == "ALL" ||
            event.category.equals(selectedCategory, ignoreCase = true)

        matchesQuery && matchesStatus && matchesCategory
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "EVENT SCHEDULES & LINEUP",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontFamily = MaterialTheme.typography.displaySmall.fontFamily,
                    color = TextWhite,
                    letterSpacing = 1.sp
                ),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Stay Tuned Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("events_stay_tuned_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.verticalGradient(listOf(AntiqueGold, DarkBloodRed)),
                    width = 1.dp
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Schedule Pending",
                            tint = AntiqueGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "STAY TUNED FOR THE FULL SCHEDULE",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = MaterialTheme.typography.displaySmall.fontFamily,
                                color = AntiqueGold,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "CADUCEUS 2026 begins on 17th December 2026. The complete competition lineup, round timings, and venue schedule will be updated here once finalized.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextLightGrey,
                            lineHeight = 19.sp
                        )
                    )
                }
            }
        }

        if (events.size > 1) {
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by event, venue or category...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = AntiqueGold
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("event_search_bar"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AntiqueGold,
                        unfocusedBorderColor = BorderDark,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        cursorColor = PrimaryCrimson
                    ),
                    singleLine = true
                )
            }

            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(statusFilters) { filter ->
                        val isSelected = selectedFilter == filter
                        Surface(
                            color = if (isSelected) PrimaryCrimson else SurfaceCard,
                            shape = RoundedCornerShape(8.dp),
                            border = CardDefaults.outlinedCardBorder().copy(width = 1.dp),
                            modifier = Modifier.clickable { selectedFilter = filter }
                        ) {
                            Text(
                                text = filter,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSelected) TextWhite else TextLightGrey,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }

            if (categories.size > 2) {
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { cat ->
                            val isSelected = selectedCategory == cat
                            Surface(
                                color = if (isSelected) DarkBloodRed else DarkObsidian,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.clickable { selectedCategory = cat }
                            ) {
                                Text(
                                    text = cat,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSelected) AntiqueGold else TextMuted
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        if (filteredEvents.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (events.isEmpty()) {
                            "Stay tuned for the full schedule of CADUCEUS 2026 starting 17th December 2026."
                        } else {
                            "No events match your criteria."
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(filteredEvents, key = { it.id }) { event ->
                val isConcert = event.id == "mohammed_irfan_live" ||
                    event.title.contains("MOHAMMED IRFAN", ignoreCase = true)

                if (isConcert) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clickable { onSelectEvent(event) }
                            .testTag("events_concert_poster_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.verticalGradient(listOf(AntiqueGold, PrimaryCrimson)),
                            width = 1.dp
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(color = DarkBloodRed, shape = RoundedCornerShape(6.dp)) {
                                    Text(
                                        text = "STAR NIGHT • 19TH DEC 2026",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = AntiqueGold,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                EventStatusBadge(status = event.status)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Image(
                                painter = painterResource(id = R.drawable.mohammed_irfan_poster),
                                contentDescription = "Mohammed Irfan Live in Concert on 19th December 2026",
                                contentScale = ContentScale.FillWidth,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = event.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${event.venueName} • 19 Dec 2026",
                                style = MaterialTheme.typography.bodySmall.copy(color = AntiqueGold)
                            )
                        }
                    }
                } else {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        EventCard(
                            event = event,
                            onClick = { onSelectEvent(event) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EventDetailScreen(
    event: CaduceusEvent,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var isReminderSet by remember { mutableStateOf(false) }
    var showPosterZoom by remember { mutableStateOf(false) }
    val isConcert = event.id == "mohammed_irfan_live" ||
        event.title.contains("MOHAMMED IRFAN", ignoreCase = true)

    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTimeMillis = System.currentTimeMillis()
        }
    }

    val (days, hours, minutes, seconds, isStarted) = remember(currentTimeMillis, event.startTime) {
        val targetMillis = try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("Asia/Kolkata")
            }
            val cleanDate = event.startTime.substringBefore("+").substringBefore("Z")
            sdf.parse(cleanDate)?.time ?: (System.currentTimeMillis() + 3600000L)
        } catch (e: Exception) {
            System.currentTimeMillis() + 3600000L
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

    val formattedSchedule = remember(event.startTime, event.endTime) {
        try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("Asia/Kolkata")
            }
            val dateFormatter = SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("Asia/Kolkata")
            }
            val timeFormatter = SimpleDateFormat("HH:mm", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("Asia/Kolkata")
            }
            val startParsed = parser.parse(event.startTime.substringBefore("+").substringBefore("Z"))
            val endParsed = parser.parse(event.endTime.substringBefore("+").substringBefore("Z"))
            if (startParsed != null && endParsed != null) {
                "${dateFormatter.format(startParsed)} to ${timeFormatter.format(endParsed)} IST"
            } else {
                event.startTime.substringAfter("T").take(5) + " to " + event.endTime.substringAfter("T").take(5)
            }
        } catch (_: Exception) {
            event.startTime.substringAfter("T").take(5) + " to " + event.endTime.substringAfter("T").take(5)
        }
    }

    if (showPosterZoom && isConcert) {
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
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = { showPosterZoom = false }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Poster",
                                tint = TextWhite
                            )
                        }
                    }
                    Image(
                        painter = painterResource(id = R.drawable.mohammed_irfan_poster),
                        contentDescription = "Mohammed Irfan Live in Concert Poster",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.5.dp, AntiqueGold, RoundedCornerShape(16.dp))
                    )
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
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("event_detail_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextWhite
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "EVENT DOSSIER",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = MaterialTheme.typography.displaySmall.fontFamily,
                    color = AntiqueGold,
                    letterSpacing = 1.sp
                )
            )
        }

        // Hero Card
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
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(color = DarkBloodRed, shape = RoundedCornerShape(6.dp)) {
                        Text(
                            text = event.category.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = AntiqueGold,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    EventStatusBadge(status = event.status)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                )

                if (isConcert) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Image(
                        painter = painterResource(id = R.drawable.mohammed_irfan_poster),
                        contentDescription = "Mohammed Irfan Live in Concert on 19th December 2026",
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, AntiqueGold, RoundedCornerShape(12.dp))
                            .clickable { showPosterZoom = true }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Individual Countdown Timer
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkObsidian),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isStarted as Boolean) "STATUS: EVENT UNDERWAY" else "STARTS IN (IST):",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isStarted) AntiqueGold else TextMuted,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        if (!isStarted) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = String.format(
                                    "%02d DAYS : %02d HRS : %02d MINS : %02d SECS",
                                    days,
                                    hours,
                                    minutes,
                                    seconds
                                ),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Venue & Timing details
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Venue",
                        tint = AntiqueGold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "VENUE",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                        )
                        Text(
                            text = event.venueName,
                            style = MaterialTheme.typography.titleSmall.copy(color = TextWhite)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Timing",
                        tint = AntiqueGold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "SCHEDULE (IST)",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                        )
                        Text(
                            text = formattedSchedule,
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextWhite)
                        )
                    }
                }
                if (event.coordinatorName.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Coordinator",
                            tint = AntiqueGold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "EVENT COORDINATOR",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                            )
                            Text(
                                text = "${event.coordinatorName} (${event.coordinatorPhone})",
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextWhite)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Description & Rules
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "EVENT OVERVIEW",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = AntiqueGold,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextLightGrey,
                        lineHeight = 22.sp
                    )
                )

                if (event.rules.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "RULES & REGULATIONS",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = AntiqueGold,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = event.rules,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextLightGrey,
                            lineHeight = 20.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Action Buttons: Directions, Share, Reminder
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    try {
                        val mapIntent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("geo:0,0?q=${Uri.encode(event.venueName)}")
                        )
                        context.startActivity(mapIntent)
                    } catch (_: Exception) {}
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("event_directions_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryCrimson)
            ) {
                Icon(imageVector = Icons.Default.Directions, contentDescription = "Map")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "GET DIRECTIONS TO VENUE",
                    style = MaterialTheme.typography.labelMedium.copy(color = TextWhite)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { isReminderSet = !isReminderSet },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("event_reminder_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isReminderSet) DarkBloodRed else DarkObsidian
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Reminder",
                        tint = if (isReminderSet) AntiqueGold else TextLightGrey
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isReminderSet) "REMINDER SET" else "SET REMINDER",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isReminderSet) AntiqueGold else TextLightGrey
                        )
                    )
                }

                Button(
                    onClick = {
                        try {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Join me at '${event.title}' at CADUCEUS 2026! Venue: ${event.venueName}. Where stars align with darkness."
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Event"))
                        } catch (_: Exception) {}
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("event_share_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkObsidian)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = TextLightGrey
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SHARE",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextLightGrey)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
