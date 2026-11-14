package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.CaduceusEvent
import com.example.data.model.CaduceusNotification
import com.example.data.model.EventStatus
import com.example.ui.theme.AntiqueGold
import com.example.ui.theme.BloodRedGlow
import com.example.ui.theme.BorderDark
import com.example.ui.theme.DarkBloodRed
import com.example.ui.theme.DarkObsidian
import com.example.ui.theme.PrimaryCrimson
import com.example.ui.theme.SolarGold
import com.example.ui.theme.StatusCompleted
import com.example.ui.theme.StatusLive
import com.example.ui.theme.StatusStartingSoon
import com.example.ui.theme.StatusUpcoming
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardElevated
import com.example.ui.theme.TextLightGrey
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.VoidBlack

@Composable
fun CaduceusLogoHeader(
    modifier: Modifier = Modifier,
    sizeDp: Int = 42,
    showTagline: Boolean = false
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.caduceus_logo),
            contentDescription = "CADUCEUS 2026 Official Logo",
            modifier = Modifier
                .size(sizeDp.dp)
                .clip(CircleShape)
                .border(1.dp, AntiqueGold.copy(alpha = 0.6f), CircleShape),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = "CADUCEUS 2026",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = MaterialTheme.typography.displaySmall.fontFamily,
                    color = TextWhite,
                    letterSpacing = 1.sp
                )
            )
            if (showTagline) {
                Text(
                    text = "WHERE STARS ALIGN WITH DARKNESS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = AntiqueGold,
                        fontSize = 9.sp,
                        letterSpacing = 0.5.sp
                    )
                )
            }
        }
    }
}

@Composable
fun UrgentCalloutBanner(
    notification: CaduceusNotification,
    onDismiss: () -> Unit,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .scale(pulseScale)
            .testTag("urgent_callout_banner")
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkBloodRed.copy(alpha = 0.95f)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(PrimaryCrimson, AntiqueGold, PrimaryCrimson)),
            width = 1.5.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(PrimaryCrimson),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = "Urgent Announcement",
                    tint = TextWhite,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextLightGrey),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (notification.targetVenue.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Venue",
                            tint = AntiqueGold,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = notification.targetVenue,
                            style = MaterialTheme.typography.labelSmall.copy(color = AntiqueGold)
                        )
                    }
                }
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun EventStatusBadge(status: String) {
    val (bgColor, textColor, label) = when (status) {
        EventStatus.LIVE_NOW.name -> Triple(StatusLive, TextWhite, "🔴 LIVE NOW")
        EventStatus.STARTING_SOON.name -> Triple(StatusStartingSoon, VoidBlack, "🟡 STARTING SOON")
        EventStatus.UPCOMING.name -> Triple(SurfaceCardElevated, StatusUpcoming, "⚪ UPCOMING")
        EventStatus.COMPLETED.name -> Triple(SurfaceCard, StatusCompleted, "✔ COMPLETED")
        EventStatus.POSTPONED.name -> Triple(DarkBloodRed, AntiqueGold, "⚠ POSTPONED")
        EventStatus.CANCELLED.name -> Triple(DarkBloodRed, PrimaryCrimson, "✖ CANCELLED")
        else -> Triple(SurfaceCard, TextLightGrey, status)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.padding(2.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = textColor,
                fontSize = 10.sp
            )
        )
    }
}

@Composable
fun EventCard(
    event: CaduceusEvent,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("event_card_${event.id}")
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(listOf(AntiqueGold.copy(alpha = 0.4f), BorderDark)),
            width = 1.dp
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = DarkBloodRed,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = event.category.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = AntiqueGold,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                EventStatusBadge(status = event.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = event.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = event.description,
                style = MaterialTheme.typography.bodySmall.copy(color = TextLightGrey),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Venue",
                        tint = AntiqueGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = event.venueName,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = AntiqueGold,
                            fontSize = 11.sp
                        )
                    )
                }
                Text(
                    text = event.startTime.substringAfter("T").take(5) + " IST",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextLightGrey,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}
