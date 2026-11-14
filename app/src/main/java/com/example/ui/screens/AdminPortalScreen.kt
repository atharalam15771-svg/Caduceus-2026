package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AppInstallRecord
import com.example.data.model.ApprovalStatus
import com.example.data.model.CaduceusEvent
import com.example.data.model.CaduceusNotification
import com.example.data.model.CaduceusUser
import com.example.data.model.EventStatus
import com.example.data.model.FestSettings
import com.example.data.model.UserRole
import com.example.data.repository.CaduceusRepository
import com.example.data.util.CaduceusAudioManager
import com.example.data.util.CaduceusNotificationHelper
import com.example.ui.components.EventStatusBadge
import com.example.ui.theme.AntiqueGold
import com.example.ui.theme.BorderDark
import com.example.ui.theme.DarkBloodRed
import com.example.ui.theme.DarkObsidian
import com.example.ui.theme.PrimaryCrimson
import com.example.ui.theme.SolarGold
import com.example.ui.theme.StatusCompleted
import com.example.ui.theme.StatusStartingSoon
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardElevated
import com.example.ui.theme.TextLightGrey
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.VoidBlack
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Composable
fun AdminPortalScreen(
    currentUser: CaduceusUser,
    allUsers: List<CaduceusUser>,
    appInstalls: List<AppInstallRecord>,
    events: List<CaduceusEvent>,
    notifications: List<CaduceusNotification>,
    festSettings: FestSettings,
    repository: CaduceusRepository,
    onBack: () -> Unit
) {
    val isAuthorized = currentUser.role == UserRole.SUPER_ADMIN.name ||
        currentUser.role == UserRole.EVENT_ADMIN.name

    if (!isAuthorized) {
        AdminUnauthorizedScreen(
            user = currentUser,
            onSwitchAccount = onBack,
            onReturnToApp = onBack
        )
        return
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Users", "Broadcast", "Events", "QR Scanner", "Settings")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("admin_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Festival App",
                    tint = TextWhite
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Image(
                painter = painterResource(id = R.drawable.caduceus_logo),
                contentDescription = "CADUCEUS 2026 Official Logo",
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, AntiqueGold, CircleShape),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "ADMIN COMMAND PORTAL",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = MaterialTheme.typography.displaySmall.fontFamily,
                        color = AntiqueGold,
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    text = "${currentUser.role} • ${currentUser.email}",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextLightGrey),
                    maxLines = 1
                )
            }
            Surface(
                color = DarkBloodRed,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "/admin",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = AntiqueGold,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = DarkObsidian,
            contentColor = AntiqueGold,
            edgePadding = 16.dp
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    modifier = Modifier.testTag("admin_tab_${title.lowercase().replace(" ", "_")}"),
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) AntiqueGold else TextLightGrey
                            )
                        )
                    }
                )
            }
        }

        when (selectedTab) {
            0 -> AdminOverviewTab(
                allUsers = allUsers,
                appInstalls = appInstalls,
                events = events,
                notifications = notifications,
                onJumpToTab = { selectedTab = it }
            )
            1 -> UsersMonitorTab(
                currentUser = currentUser,
                allUsers = allUsers,
                repository = repository
            )
            2 -> BroadcastComposerTab(
                repository = repository,
                events = events,
                notifications = notifications
            )
            3 -> EventManagerTab(
                events = events,
                repository = repository
            )
            4 -> QrVerifierTab(
                allUsers = allUsers,
                currentAdminEmail = currentUser.email,
                repository = repository
            )
            5 -> FestSettingsTab(
                festSettings = festSettings,
                repository = repository
            )
        }
    }
}

@Composable
private fun AdminOverviewTab(
    allUsers: List<CaduceusUser>,
    appInstalls: List<AppInstallRecord>,
    events: List<CaduceusEvent>,
    notifications: List<CaduceusNotification>,
    onJumpToTab: (Int) -> Unit
) {
    val totalDownloads = maxOf(appInstalls.size, allUsers.size)
    val checkedInCount = allUsers.count { it.checkedIn }
    val approvedCount = allUsers.count { it.approvalStatus == ApprovalStatus.APPROVED.name }
    val sortedRecentUsers = remember(allUsers) {
        allUsers.sortedByDescending { it.lastLoginAt?.seconds ?: it.createdAt?.seconds ?: 0L }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "LIVE FESTIVAL TELEMETRY",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = MaterialTheme.typography.displaySmall.fontFamily,
                    color = AntiqueGold,
                    letterSpacing = 1.sp
                )
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AdminMetricCard(
                    title = "TOTAL DOWNLOADS",
                    value = totalDownloads.toString(),
                    subtitle = "${appInstalls.size} tracked devices",
                    icon = Icons.Default.Download,
                    accentColor = AntiqueGold,
                    modifier = Modifier.weight(1f)
                )
                AdminMetricCard(
                    title = "SIGNED-IN USERS",
                    value = allUsers.size.toString(),
                    subtitle = "$approvedCount verified passes",
                    icon = Icons.Default.People,
                    accentColor = PrimaryCrimson,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AdminMetricCard(
                    title = "GATE CHECKED-IN",
                    value = checkedInCount.toString(),
                    subtitle = "QR verified entries",
                    icon = Icons.Default.Verified,
                    accentColor = StatusCompleted,
                    modifier = Modifier.weight(1f)
                )
                AdminMetricCard(
                    title = "ACTIVE EVENTS",
                    value = events.size.toString(),
                    subtitle = "${notifications.size} broadcasts sent",
                    icon = Icons.Default.Event,
                    accentColor = SolarGold,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Text(
                text = "QUICK ADMIN ACTIONS",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = MaterialTheme.typography.displaySmall.fontFamily,
                    color = AntiqueGold,
                    letterSpacing = 1.sp
                )
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onJumpToTab(4) },
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryCrimson),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan QR", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("SCAN PASS QR", style = MaterialTheme.typography.labelSmall.copy(color = TextWhite))
                }
                Button(
                    onClick = { onJumpToTab(2) },
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBloodRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Campaign, contentDescription = "Broadcast", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("SEND ALERT", style = MaterialTheme.typography.labelSmall.copy(color = TextWhite))
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT SIGNED-IN ATTENDEES",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = MaterialTheme.typography.displaySmall.fontFamily,
                        color = AntiqueGold,
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    text = "See All (${allUsers.size})",
                    style = MaterialTheme.typography.labelSmall.copy(color = AntiqueGold),
                    modifier = Modifier.clickable { onJumpToTab(1) }
                )
            }
        }

        if (sortedRecentUsers.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "No signed-in users recorded yet.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(sortedRecentUsers.take(8), key = { it.userId.ifBlank { it.id } }) { attendee ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = attendee.fullName,
                                style = MaterialTheme.typography.titleSmall.copy(color = TextWhite)
                            )
                            Text(
                                text = attendee.email,
                                style = MaterialTheme.typography.bodySmall.copy(color = TextLightGrey)
                            )
                            Text(
                                text = "${attendee.festPassId} • Last active: ${formatIstTimestamp(attendee.lastLoginAt ?: attendee.createdAt)}",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                            )
                        }
                        Surface(
                            color = if (attendee.checkedIn) StatusCompleted else DarkBloodRed,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (attendee.checkedIn) "CHECKED IN" else attendee.role,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (attendee.checkedIn) VoidBlack else AntiqueGold,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(listOf(accentColor, BorderDark)),
            width = 1.dp
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextMuted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.displaySmall.copy(
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(color = accentColor, fontSize = 10.sp)
            )
        }
    }
}

@Composable
private fun UsersMonitorTab(
    currentUser: CaduceusUser,
    allUsers: List<CaduceusUser>,
    repository: CaduceusRepository
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var filterStatus by remember { mutableStateOf("ALL") }

    val filteredList = remember(allUsers, searchQuery, filterStatus) {
        allUsers.filter { user ->
            val matchesQuery = searchQuery.isBlank() ||
                user.fullName.contains(searchQuery, ignoreCase = true) ||
                user.email.contains(searchQuery, ignoreCase = true) ||
                user.festPassId.contains(searchQuery, ignoreCase = true)
            val matchesFilter = when (filterStatus) {
                "APPROVED" -> user.approvalStatus == ApprovalStatus.APPROVED.name
                "CHECKED IN" -> user.checkedIn
                "ADMINS" -> user.role != UserRole.STUDENT.name
                "REVOKED" -> user.approvalStatus == ApprovalStatus.REJECTED.name
                else -> true
            }
            matchesQuery && matchesFilter
        }.sortedByDescending { it.lastLoginAt?.seconds ?: it.createdAt?.seconds ?: 0L }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search name, email or Pass ID...") },
            modifier = Modifier.fillMaxWidth().testTag("admin_users_search"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AntiqueGold,
                unfocusedBorderColor = BorderDark,
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("ALL", "APPROVED", "CHECKED IN", "ADMINS", "REVOKED").forEach { status ->
                val isSelected = filterStatus == status
                Button(
                    onClick = { filterStatus = status },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) PrimaryCrimson else SurfaceCard
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(2.dp)
                ) {
                    Text(
                        text = status,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isSelected) TextWhite else TextLightGrey,
                            fontSize = 8.sp
                        ),
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No matching attendees found.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filteredList, key = { it.userId.ifBlank { it.id } }) { userItem ->
                    val isApproved = userItem.approvalStatus == ApprovalStatus.APPROVED.name
                    val isEventAdmin = userItem.role == UserRole.EVENT_ADMIN.name
                    val isSuperAdmin = userItem.role == UserRole.SUPER_ADMIN.name

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = userItem.fullName,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = TextWhite,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                Surface(
                                    color = if (isApproved) StatusCompleted else PrimaryCrimson,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = if (isApproved) "VERIFIED" else "REVOKED",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isApproved) VoidBlack else TextWhite,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${userItem.email} • ${userItem.role}",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextLightGrey)
                            )
                            Text(
                                text = "Pass: ${userItem.festPassId} • Signed in: ${formatIstTimestamp(userItem.lastLoginAt ?: userItem.createdAt)}",
                                style = MaterialTheme.typography.labelSmall.copy(color = AntiqueGold)
                            )
                            if (userItem.checkedIn) {
                                Text(
                                    text = "✔ Gate Checked-In (${formatIstTimestamp(userItem.checkedInAt)})",
                                    style = MaterialTheme.typography.labelSmall.copy(color = StatusCompleted)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            repository.markUserCheckedIn(
                                                targetUid = userItem.userId,
                                                checkedIn = !userItem.checkedIn,
                                                adminEmail = currentUser.email
                                            )
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (userItem.checkedIn) DarkObsidian else DarkBloodRed
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(4.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = "Check-in", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (userItem.checkedIn) "UNCHECK" else "CHECK IN",
                                        style = MaterialTheme.typography.labelSmall.copy(color = TextWhite, fontSize = 10.sp)
                                    )
                                }

                                if (!isSuperAdmin) {
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                val next = if (isApproved) ApprovalStatus.REJECTED else ApprovalStatus.APPROVED
                                                repository.setUserApprovalStatus(userItem.userId, next)
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isApproved) DarkObsidian else PrimaryCrimson
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isApproved) Icons.Default.Close else Icons.Default.Check,
                                            contentDescription = "Toggle Verification",
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isApproved) "REVOKE" else "VERIFY",
                                            style = MaterialTheme.typography.labelSmall.copy(color = TextWhite, fontSize = 10.sp)
                                        )
                                    }

                                    if (currentUser.role == UserRole.SUPER_ADMIN.name) {
                                        Button(
                                            onClick = {
                                                scope.launch {
                                                    val nextRole = if (isEventAdmin) UserRole.STUDENT else UserRole.EVENT_ADMIN
                                                    repository.setUserRole(userItem.userId, nextRole)
                                                    Toast.makeText(
                                                        context,
                                                        "${userItem.fullName} set to ${nextRole.name}",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceCardElevated),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(4.dp)
                                        ) {
                                            Icon(Icons.Default.AdminPanelSettings, contentDescription = "Role", modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (isEventAdmin) "DEMOTE" else "MAKE ADMIN",
                                                style = MaterialTheme.typography.labelSmall.copy(color = AntiqueGold, fontSize = 9.sp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BroadcastComposerTab(
    repository: CaduceusRepository,
    events: List<CaduceusEvent>,
    notifications: List<CaduceusNotification>
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("🎤 MOHAMMED IRFAN LIVE — 19TH DEC 2026") }
    var message by remember {
        mutableStateOf("Reminder: Bollywood Star Night featuring Mohammed Irfan is on 19th December 2026 at DMGMCH, Hatuara, Purulia. Keep your Digital Fest Pass ready!")
    }
    var targetVenue by remember { mutableStateOf("Deben Mahata Government Medical College And Hospital, Hatuara, Purulia - 723147") }
    var linkedEventId by remember { mutableStateOf(events.firstOrNull()?.id ?: "") }
    var isPinned by remember { mutableStateOf(true) }
    var isSending by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "SEND MOBILE & IN-APP NOTIFICATION",
            style = MaterialTheme.typography.titleMedium.copy(
                color = TextWhite,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = "Dispatches a real Android phone notification tray alert + urgent home banner to every signed-in attendee.",
            style = MaterialTheme.typography.bodySmall.copy(color = TextLightGrey)
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (events.isNotEmpty()) {
            Text(text = "Quick-Link Event:", style = MaterialTheme.typography.labelSmall.copy(color = AntiqueGold))
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                events.take(3).forEach { ev ->
                    Button(
                        onClick = {
                            linkedEventId = ev.id
                            title = "📢 ${ev.title}"
                            targetVenue = ev.venueName
                            message = "${ev.title} update at ${ev.venueName}. Check the Events tab for full details!"
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (linkedEventId == ev.id) DarkBloodRed else SurfaceCard
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(ev.title.take(18), style = MaterialTheme.typography.labelSmall.copy(color = TextWhite))
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        val textColors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AntiqueGold,
            unfocusedBorderColor = BorderDark,
            focusedTextColor = TextWhite,
            unfocusedTextColor = TextWhite,
            focusedLabelColor = AntiqueGold,
            cursorColor = PrimaryCrimson
        )

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Notification Title") },
            modifier = Modifier.fillMaxWidth().testTag("broadcast_title"),
            colors = textColors
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Event Announcement Message") },
            modifier = Modifier.fillMaxWidth().height(110.dp).testTag("broadcast_message"),
            colors = textColors,
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = targetVenue,
            onValueChange = { targetVenue = it },
            label = { Text("Target Venue") },
            modifier = Modifier.fillMaxWidth().testTag("broadcast_venue"),
            colors = textColors
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Pin as Urgent Home Banner",
                style = MaterialTheme.typography.bodySmall.copy(color = TextWhite)
            )
            Switch(
                checked = isPinned,
                onCheckedChange = { isPinned = it },
                colors = SwitchDefaults.colors(checkedThumbColor = AntiqueGold, checkedTrackColor = DarkBloodRed)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (title.isBlank() || message.isBlank()) return@Button
                isSending = true
                scope.launch {
                    try {
                        repository.broadcastNotification(
                            title = title.trim(),
                            message = message.trim(),
                            category = if (isPinned) "URGENT_CALLOUT" else "GENERAL",
                            targetVenue = targetVenue.trim(),
                            eventId = linkedEventId,
                            isPinned = isPinned
                        )
                        CaduceusNotificationHelper.showHeadsUpNotification(
                            context = context,
                            notificationId = (1000..9999).random(),
                            title = title.trim(),
                            message = message.trim(),
                            targetVenue = targetVenue.trim()
                        )
                        CaduceusAudioManager.playAlert(context)
                        Toast.makeText(context, "Mobile & in-app alert sent to all users!", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    } finally {
                        isSending = false
                    }
                }
            },
            enabled = !isSending,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("send_broadcast_button"),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryCrimson),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isSending) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = TextWhite)
            } else {
                Icon(imageVector = Icons.Default.Campaign, contentDescription = "Send")
                Spacer(modifier = Modifier.width(8.dp))
                Text("DISPATCH NOTIFICATION TO EVERYONE", style = MaterialTheme.typography.labelLarge.copy(color = TextWhite))
            }
        }

        if (notifications.isNotEmpty()) {
            Spacer(modifier = Modifier.height(22.dp))
            Text(
                text = "SENT BROADCAST HISTORY (${notifications.size})",
                style = MaterialTheme.typography.titleSmall.copy(color = AntiqueGold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            notifications.forEach { notif ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = notif.title,
                                style = MaterialTheme.typography.titleSmall.copy(color = TextWhite)
                            )
                            Text(
                                text = notif.message,
                                style = MaterialTheme.typography.bodySmall.copy(color = TextLightGrey),
                                maxLines = 2
                            )
                        }
                        IconButton(
                            onClick = {
                                scope.launch {
                                    repository.deleteNotification(notif.id)
                                }
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Notification", tint = PrimaryCrimson)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventManagerTab(
    events: List<CaduceusEvent>,
    repository: CaduceusRepository
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var editingEvent by remember { mutableStateOf<CaduceusEvent?>(null) }
    var showEditorDialog by remember { mutableStateOf(false) }

    if (showEditorDialog) {
        EventEditorDialog(
            initialEvent = editingEvent,
            onDismiss = {
                showEditorDialog = false
                editingEvent = null
            },
            onSave = { savedEvent ->
                scope.launch {
                    try {
                        repository.createOrUpdateEvent(savedEvent)
                        Toast.makeText(context, "Event saved!", Toast.LENGTH_SHORT).show()
                        showEditorDialog = false
                        editingEvent = null
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "MANAGE EVENTS (${events.size})",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
            )
            Button(
                onClick = {
                    editingEvent = null
                    showEditorDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryCrimson),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("admin_new_event_button")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Event", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("NEW EVENT", style = MaterialTheme.typography.labelSmall.copy(color = TextWhite))
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(events, key = { it.id }) { event ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = event.title,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            EventStatusBadge(status = event.status)
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${event.category} • ${event.venueName}",
                            style = MaterialTheme.typography.bodySmall.copy(color = AntiqueGold)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = {
                                    scope.launch { repository.updateEventStatus(event.id, EventStatus.LIVE_NOW) }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = DarkBloodRed),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(2.dp)
                            ) {
                                Text("LIVE NOW", fontSize = 9.sp, color = TextWhite)
                            }
                            Button(
                                onClick = {
                                    scope.launch { repository.updateEventStatus(event.id, EventStatus.STARTING_SOON) }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceCardElevated),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(2.dp)
                            ) {
                                Text("STARTING", fontSize = 9.sp, color = SolarGold)
                            }
                            Button(
                                onClick = {
                                    scope.launch { repository.updateEventStatus(event.id, EventStatus.COMPLETED) }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = DarkObsidian),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(2.dp)
                            ) {
                                Text("COMPLETE", fontSize = 9.sp, color = TextLightGrey)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    editingEvent = event
                                    showEditorDialog = true
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = DarkObsidian),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Event", tint = AntiqueGold, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("EDIT", style = MaterialTheme.typography.labelSmall.copy(color = AntiqueGold))
                            }
                            Button(
                                onClick = {
                                    scope.launch {
                                        repository.deleteEvent(event.id)
                                        Toast.makeText(context, "Event deleted", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = DarkObsidian),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Event", tint = PrimaryCrimson, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("DELETE", style = MaterialTheme.typography.labelSmall.copy(color = PrimaryCrimson))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventEditorDialog(
    initialEvent: CaduceusEvent?,
    onDismiss: () -> Unit,
    onSave: (CaduceusEvent) -> Unit
) {
    var title by remember { mutableStateOf(initialEvent?.title ?: "") }
    var category by remember { mutableStateOf(initialEvent?.category ?: "Cultural") }
    var venueName by remember {
        mutableStateOf(
            initialEvent?.venueName
                ?: "${CaduceusRepository.CAMPUS_VENUE_NAME}, ${CaduceusRepository.CAMPUS_VENUE_ADDRESS}"
        )
    }
    var startTime by remember { mutableStateOf(initialEvent?.startTime ?: "2026-12-17T10:00:00+05:30") }
    var endTime by remember { mutableStateOf(initialEvent?.endTime ?: "2026-12-17T13:00:00+05:30") }
    var description by remember { mutableStateOf(initialEvent?.description ?: "") }
    var rules by remember { mutableStateOf(initialEvent?.rules ?: "") }
    var isFeatured by remember { mutableStateOf(initialEvent?.isFeatured ?: true) }

    val colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = AntiqueGold,
        unfocusedBorderColor = BorderDark,
        focusedTextColor = TextWhite,
        unfocusedTextColor = TextWhite
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceCard,
        title = {
            Text(
                text = if (initialEvent == null) "CREATE NEW EVENT" else "EDIT EVENT",
                style = MaterialTheme.typography.titleMedium.copy(color = AntiqueGold, fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, colors = colors)
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, colors = colors)
                OutlinedTextField(value = venueName, onValueChange = { venueName = it }, label = { Text("Venue") }, colors = colors)
                OutlinedTextField(value = startTime, onValueChange = { startTime = it }, label = { Text("Start (IST ISO)") }, colors = colors)
                OutlinedTextField(value = endTime, onValueChange = { endTime = it }, label = { Text("End (IST ISO)") }, colors = colors)
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, colors = colors, maxLines = 3)
                OutlinedTextField(value = rules, onValueChange = { rules = it }, label = { Text("Rules") }, colors = colors, maxLines = 3)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Featured Event", style = MaterialTheme.typography.bodySmall.copy(color = TextWhite))
                    Switch(checked = isFeatured, onCheckedChange = { isFeatured = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) return@Button
                    val base = initialEvent ?: CaduceusEvent()
                    onSave(
                        base.copy(
                            title = title.trim(),
                            category = category.trim().ifBlank { "Cultural" },
                            venueName = venueName.trim(),
                            startTime = startTime.trim(),
                            endTime = endTime.trim(),
                            description = description.trim(),
                            rules = rules.trim(),
                            isFeatured = isFeatured
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryCrimson)
            ) {
                Text("SAVE EVENT", color = TextWhite)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = TextLightGrey)
            }
        }
    )
}

@Composable
private fun FestSettingsTab(
    festSettings: FestSettings,
    repository: CaduceusRepository
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var openingTimestamp by remember { mutableStateOf(festSettings.festOpeningTimestamp) }
    var tagline by remember { mutableStateOf(festSettings.tagline) }
    var officialEmail by remember { mutableStateOf(festSettings.officialEmail) }
    var instagramUrl by remember { mutableStateOf(festSettings.instagramUrl) }
    var youtubeUrl by remember { mutableStateOf(festSettings.youtubeUrl) }
    var isRegistrationOpen by remember { mutableStateOf(festSettings.isRegistrationOpen) }
    var isSaving by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "FESTIVAL MASTER CONFIGURATION",
            style = MaterialTheme.typography.titleMedium.copy(
                color = TextWhite,
                fontWeight = FontWeight.Bold
            )
        )

        val colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AntiqueGold,
            unfocusedBorderColor = BorderDark,
            focusedTextColor = TextWhite,
            unfocusedTextColor = TextWhite,
            cursorColor = PrimaryCrimson
        )

        OutlinedTextField(
            value = openingTimestamp,
            onValueChange = { openingTimestamp = it },
            label = { Text("Festival Opening Timestamp (IST)") },
            modifier = Modifier.fillMaxWidth(),
            colors = colors
        )
        OutlinedTextField(
            value = tagline,
            onValueChange = { tagline = it },
            label = { Text("Festival Tagline") },
            modifier = Modifier.fillMaxWidth(),
            colors = colors
        )
        OutlinedTextField(
            value = officialEmail,
            onValueChange = { officialEmail = it },
            label = { Text("Official Contact Email") },
            modifier = Modifier.fillMaxWidth(),
            colors = colors
        )
        OutlinedTextField(
            value = instagramUrl,
            onValueChange = { instagramUrl = it },
            label = { Text("Instagram URL") },
            modifier = Modifier.fillMaxWidth(),
            colors = colors
        )
        OutlinedTextField(
            value = youtubeUrl,
            onValueChange = { youtubeUrl = it },
            label = { Text("YouTube URL") },
            modifier = Modifier.fillMaxWidth(),
            colors = colors
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Fest Pass Registration Open",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextWhite)
            )
            Switch(
                checked = isRegistrationOpen,
                onCheckedChange = { isRegistrationOpen = it },
                colors = SwitchDefaults.colors(checkedThumbColor = AntiqueGold, checkedTrackColor = DarkBloodRed)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Button(
            onClick = {
                isSaving = true
                scope.launch {
                    try {
                        repository.updateFestSettings(
                            festSettings.copy(
                                festOpeningTimestamp = openingTimestamp.trim(),
                                tagline = tagline.trim(),
                                officialEmail = officialEmail.trim(),
                                instagramUrl = instagramUrl.trim(),
                                youtubeUrl = youtubeUrl.trim(),
                                isRegistrationOpen = isRegistrationOpen
                            )
                        )
                        Toast.makeText(context, "Festival settings updated!", Toast.LENGTH_SHORT).show()
                    } finally {
                        isSaving = false
                    }
                }
            },
            enabled = !isSaving,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryCrimson),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("SAVE CONFIGURATION", style = MaterialTheme.typography.labelMedium.copy(color = TextWhite))
        }
    }
}

private fun formatIstTimestamp(timestamp: Timestamp?): String {
    if (timestamp == null) return "Just now"
    return try {
        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("Asia/Kolkata")
        }
        sdf.format(timestamp.toDate())
    } catch (_: Exception) {
        "Recorded"
    }
}
