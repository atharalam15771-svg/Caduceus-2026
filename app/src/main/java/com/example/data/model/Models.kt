package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class UserRole {
    STUDENT,
    EVENT_ADMIN,
    SUPER_ADMIN
}

enum class ApprovalStatus {
    PENDING,
    APPROVED,
    REJECTED
}

enum class EventStatus(val label: String) {
    LIVE_NOW("LIVE NOW"),
    STARTING_SOON("STARTING SOON"),
    UPCOMING("UPCOMING"),
    POSTPONED("POSTPONED"),
    CANCELLED("CANCELLED"),
    COMPLETED("COMPLETED")
}

@IgnoreExtraProperties
data class CaduceusUser(
    @DocumentId val id: String = "",
    val userId: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val collegeName: String = "",
    val department: String = "",
    val designation: String = "",
    val studentId: String = "",
    val batch: String = "",
    val rollNumber: String = "",
    val isStudent: Boolean = true,
    val profileCompleted: Boolean = false,
    val role: String = UserRole.STUDENT.name,
    val approvalStatus: String = ApprovalStatus.PENDING.name,
    val festPassId: String = "",
    val zodiacSign: String = "Scorpio",
    val photoUrl: String = "",
    val checkedIn: Boolean = false,
    val checkedInBy: String = "",
    @ServerTimestamp val checkedInAt: Timestamp? = null,
    @ServerTimestamp val lastLoginAt: Timestamp? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CaduceusEvent(
    @DocumentId val id: String = "",
    val title: String = "",
    val category: String = "Cultural Performances",
    val description: String = "",
    val venueId: String = "",
    val venueName: String = "Main Auditorium",
    val startTime: String = "", // e.g. "2026-11-20T10:00:00+05:30"
    val endTime: String = "",
    val status: String = EventStatus.UPCOMING.name,
    val coordinatorName: String = "",
    val coordinatorPhone: String = "",
    val rules: String = "",
    val isFeatured: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CaduceusNotification(
    @DocumentId val id: String = "",
    val title: String = "",
    val message: String = "",
    val category: String = "URGENT_CALLOUT", // URGENT_CALLOUT, VENUE_CHANGE, GENERAL
    val targetVenue: String = "",
    val eventId: String = "",
    val isPinned: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CaduceusVenue(
    @DocumentId val id: String = "",
    val name: String = "",
    val description: String = "",
    val locationNote: String = "",
    val mapUrl: String = "",
    val capacity: String = ""
)

@IgnoreExtraProperties
data class FestSettings(
    @DocumentId val id: String = "global",
    val festOpeningTimestamp: String = "2026-12-17T09:00:00+05:30",
    val tagline: String = "WHERE STARS ALIGN WITH DARKNESS",
    val instagramUrl: String = "https://www.instagram.com/caduceus.dmgmch?vrfl=OGtwOGtpZDE3dXBv",
    val facebookUrl: String = "https://facebook.com/caduceus.dmgmch",
    val youtubeUrl: String = "https://youtube.com/@CaduceusFest",
    val officialEmail: String = "caduceus.fest@gmail.com",
    val isRegistrationOpen: Boolean = true,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class ZodiacSignInfo(
    val sign: String,
    val symbol: String,
    val dates: String,
    val element: String,
    val dailyHoroscope: String,
    val luckyColor: String,
    val luckyNumber: Int,
    val darkPersonality: String
)

@IgnoreExtraProperties
data class LeaderboardEntry(
    @DocumentId val id: String = "",
    val competitionName: String = "",
    val participantName: String = "",
    val collegeName: String = "",
    val rank: Long = 1L,
    val points: Long = 100L,
    val isVerified: Boolean = true
)

@IgnoreExtraProperties
data class AppInstallRecord(
    @DocumentId val id: String = "",
    val installId: String = "",
    val userId: String = "",
    val email: String = "",
    val deviceModel: String = "",
    val appVersion: String = "1.0",
    val sdkInt: Long = 0L,
    @ServerTimestamp val installedAt: Timestamp? = null,
    @ServerTimestamp val lastActiveAt: Timestamp? = null
)
