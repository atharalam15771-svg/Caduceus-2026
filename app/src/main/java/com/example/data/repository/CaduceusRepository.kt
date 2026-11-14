package com.example.data.repository

import android.content.Context
import android.os.Build
import android.util.Log
import com.example.BuildConfig
import com.example.R
import com.example.data.model.AppInstallRecord
import com.example.data.model.ApprovalStatus
import com.example.data.model.CaduceusEvent
import com.example.data.model.CaduceusNotification
import com.example.data.model.CaduceusUser
import com.example.data.model.CaduceusVenue
import com.example.data.model.EventStatus
import com.example.data.model.FestSettings
import com.example.data.model.LeaderboardEntry
import com.example.data.model.UserRole
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class CaduceusRepository(private val context: Context) {
    private val db: FirebaseFirestore by lazy {
        val dbId = context.getString(R.string.firestore_database_id)
        FirebaseFirestore.getInstance(dbId).apply {
            try {
                firestoreSettings = FirebaseFirestoreSettings.Builder()
                    .setLocalCacheSettings(
                        PersistentCacheSettings.newBuilder()
                            .setSizeBytes(100L * 1024 * 1024) // 100 MB offline cache for campus congestion
                            .build()
                    )
                    .build()
            } catch (_: Exception) {}
        }
    }
    private val auth = Firebase.auth

    companion object {
        const val SUPER_ADMIN_EMAIL = "atharalam15771@gmail.com"
        const val CAMPUS_VENUE_ID = "dmgmch_campus"
        const val CAMPUS_VENUE_NAME = "Deben Mahata Government Medical College And Hospital"
        const val CAMPUS_VENUE_ADDRESS = "Hatuara, Purulia - 723147"

        fun resolveRole(email: String, existingRole: String?): String {
            return when {
                email.equals(SUPER_ADMIN_EMAIL, ignoreCase = true) -> UserRole.SUPER_ADMIN.name
                !existingRole.isNullOrBlank() -> existingRole
                else -> UserRole.STUDENT.name
            }
        }

        fun resolveApprovalStatus(role: String, existingApprovalStatus: String?): String {
            return when {
                role == UserRole.SUPER_ADMIN.name || role == UserRole.EVENT_ADMIN.name ->
                    ApprovalStatus.APPROVED.name
                !existingApprovalStatus.isNullOrBlank() -> existingApprovalStatus
                else -> ApprovalStatus.PENDING.name
            }
        }

        fun isUserAllowedAccess(user: CaduceusUser?): Boolean {
            if (user == null) return false
            if (user.role == UserRole.SUPER_ADMIN.name || user.role == UserRole.EVENT_ADMIN.name) {
                return true
            }
            return user.approvalStatus == ApprovalStatus.APPROVED.name
        }
    }

    // --- User & Profile Management ---

    fun observeCurrentUser(uid: String): Flow<CaduceusUser?> = callbackFlow {
        val docRef = db.collection("users").document(uid)
        val registration = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("CaduceusRepo", "Error listening to user doc: ${error.message}")
                trySend(null)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val user = snapshot.toObject(CaduceusUser::class.java)
                trySend(user)
            } else {
                trySend(null)
            }
        }
        awaitClose { registration.remove() }
    }

    suspend fun registerUserProfile(
        uid: String,
        fullName: String,
        email: String,
        phone: String,
        collegeName: String,
        department: String,
        studentId: String,
        zodiacSign: String
    ): Result<CaduceusUser> = runCatching {
        val docRef = db.collection("users").document(uid)
        val existingSnap = docRef.get().await()
        val existing = existingSnap.toObject(CaduceusUser::class.java)

        val festPassId = existing?.festPassId?.takeIf { it.isNotBlank() }
            ?: ("CAD26-" + uid.take(5).uppercase() + "-" + (1000..9999).random())

        val resolvedEmail = email.ifBlank {
            auth.currentUser?.email?.takeIf { it.isNotBlank() }
                ?: auth.currentUser?.providerData?.firstOrNull { !it.email.isNullOrBlank() }?.email
                ?: existing?.email
                ?: ""
        }

        val role = resolveRole(resolvedEmail, existing?.role)
        val approval = resolveApprovalStatus(role, existing?.approvalStatus)

        val resolvedName = if (fullName.isNotBlank() && fullName != "Caduceus Attendee") {
            fullName
        } else {
            existing?.fullName?.takeIf { it.isNotBlank() } ?: fullName
        }

        val payload = hashMapOf<String, Any>(
            "userId" to uid,
            "fullName" to resolvedName,
            "email" to resolvedEmail,
            "phone" to (phone.ifBlank { existing?.phone ?: "" }),
            "collegeName" to (collegeName.ifBlank { existing?.collegeName ?: "CADUCEUS 2026" }),
            "department" to (department.ifBlank { existing?.department ?: "Cultural Delegate" }),
            "studentId" to (studentId.ifBlank { existing?.studentId ?: "" }),
            "role" to role,
            "approvalStatus" to approval,
            "festPassId" to festPassId,
            "zodiacSign" to (zodiacSign.ifBlank { existing?.zodiacSign ?: "Scorpio" }),
            "photoUrl" to (existing?.photoUrl ?: ""),
            "checkedIn" to (existing?.checkedIn ?: false),
            "checkedInBy" to (existing?.checkedInBy ?: ""),
            "lastLoginAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (!existingSnap.exists()) {
            payload["createdAt"] = FieldValue.serverTimestamp()
        }

        docRef.set(payload, SetOptions.merge()).await()
        CaduceusUser(
            id = uid,
            userId = uid,
            fullName = resolvedName,
            email = resolvedEmail,
            phone = phone.ifBlank { existing?.phone ?: "" },
            collegeName = collegeName.ifBlank { existing?.collegeName ?: "CADUCEUS 2026" },
            department = department.ifBlank { existing?.department ?: "Cultural Delegate" },
            studentId = studentId.ifBlank { existing?.studentId ?: "" },
            role = role,
            approvalStatus = approval,
            festPassId = festPassId,
            zodiacSign = zodiacSign.ifBlank { existing?.zodiacSign ?: "Scorpio" },
            checkedIn = existing?.checkedIn ?: false,
            checkedInBy = existing?.checkedInBy ?: ""
        )
    }

    suspend fun getUserProfileOnce(uid: String): CaduceusUser? {
        return try {
            db.collection("users").document(uid).get().await().toObject(CaduceusUser::class.java)
        } catch (e: Exception) {
            Log.w("CaduceusRepo", "Failed to fetch user profile: ${e.message}")
            null
        }
    }

    suspend fun updateUserZodiac(uid: String, zodiacSign: String) {
        db.collection("users").document(uid).update(
            "zodiacSign", zodiacSign,
            "updatedAt", FieldValue.serverTimestamp()
        ).await()
    }

    // --- App Downloads / Unique Installs Tracking ---

    suspend fun recordAppInstallIfNeeded(uid: String, email: String) {
        try {
            val prefs = context.getSharedPreferences("caduceus_install_prefs", Context.MODE_PRIVATE)
            val existingInstallId = prefs.getString("install_id", null)
            val lastPingUid = prefs.getString("last_ping_uid", null)
            val lastPingMs = prefs.getLong("last_ping_ms", 0L)
            val nowMs = System.currentTimeMillis()

            // Throttle redundant writes across 700+ devices: ping once per 12h or on account switch
            if (existingInstallId != null &&
                lastPingUid == uid &&
                (nowMs - lastPingMs) < 12L * 60 * 60 * 1000
            ) {
                return
            }

            val installId = existingInstallId ?: UUID.randomUUID().toString().also {
                prefs.edit().putString("install_id", it).apply()
            }
            val docRef = db.collection("app_installs").document(installId)
            val snap = docRef.get().await()
            val payload = hashMapOf<String, Any>(
                "installId" to installId,
                "userId" to uid,
                "email" to email,
                "deviceModel" to "${Build.MANUFACTURER} ${Build.MODEL}".trim(),
                "appVersion" to BuildConfig.VERSION_NAME,
                "sdkInt" to Build.VERSION.SDK_INT.toLong(),
                "lastActiveAt" to FieldValue.serverTimestamp()
            )
            if (!snap.exists()) {
                payload["installedAt"] = FieldValue.serverTimestamp()
            }
            docRef.set(payload, SetOptions.merge()).await()
            prefs.edit()
                .putString("last_ping_uid", uid)
                .putLong("last_ping_ms", nowMs)
                .apply()
        } catch (e: Exception) {
            Log.w("CaduceusRepo", "Failed to record install: ${e.message}")
        }
    }

    fun observeAppInstalls(): Flow<List<AppInstallRecord>> = db.collection("app_installs")
        .limit(2000)
        .snapshots()
        .map { snapshot -> snapshot.toObjects(AppInstallRecord::class.java) }

    // --- Admin: All Users List, Approvals & QR Check-In ---

    fun observeAllUsers(): Flow<List<CaduceusUser>> = db.collection("users")
        .limit(2000)
        .snapshots()
        .map { snapshot -> snapshot.toObjects(CaduceusUser::class.java) }

    suspend fun setUserApprovalStatus(targetUid: String, status: ApprovalStatus) {
        db.collection("users").document(targetUid).update(
            "approvalStatus", status.name,
            "updatedAt", FieldValue.serverTimestamp()
        ).await()
    }

    suspend fun setUserRole(targetUid: String, role: UserRole) {
        db.collection("users").document(targetUid).update(
            "role", role.name,
            "updatedAt", FieldValue.serverTimestamp()
        ).await()
    }

    suspend fun markUserCheckedIn(targetUid: String, checkedIn: Boolean, adminEmail: String) {
        val updates = hashMapOf<String, Any?>(
            "checkedIn" to checkedIn,
            "checkedInBy" to if (checkedIn) adminEmail else "",
            "checkedInAt" to if (checkedIn) FieldValue.serverTimestamp() else null,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        db.collection("users").document(targetUid).update(updates).await()
    }

    // --- Events Management ---

    fun observeEvents(): Flow<List<CaduceusEvent>> = db.collection("events")
        .limit(100)
        .snapshots()
        .map { snapshot -> snapshot.toObjects(CaduceusEvent::class.java) }

    suspend fun createOrUpdateEvent(event: CaduceusEvent) {
        val docRef = if (event.id.isBlank()) {
            db.collection("events").document()
        } else {
            db.collection("events").document(event.id)
        }
        val payload = hashMapOf<String, Any>(
            "title" to event.title,
            "category" to event.category,
            "description" to event.description,
            "venueId" to event.venueId,
            "venueName" to event.venueName,
            "startTime" to event.startTime,
            "endTime" to event.endTime,
            "status" to event.status,
            "coordinatorName" to event.coordinatorName,
            "coordinatorPhone" to event.coordinatorPhone,
            "rules" to event.rules,
            "isFeatured" to event.isFeatured,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (event.id.isBlank()) {
            payload["createdAt"] = FieldValue.serverTimestamp()
        }
        docRef.set(payload, SetOptions.merge()).await()
    }

    suspend fun updateEventStatus(eventId: String, newStatus: EventStatus) {
        db.collection("events").document(eventId).update(
            "status", newStatus.name,
            "updatedAt", FieldValue.serverTimestamp()
        ).await()
    }

    suspend fun deleteEvent(eventId: String) {
        db.collection("events").document(eventId).delete().await()
    }

    // --- Notifications & Broadcast System ---

    fun observeNotifications(): Flow<List<CaduceusNotification>> = db.collection("notifications")
        .orderBy("createdAt", Query.Direction.DESCENDING)
        .limit(40)
        .snapshots()
        .map { snapshot -> snapshot.toObjects(CaduceusNotification::class.java) }

    suspend fun broadcastNotification(
        title: String,
        message: String,
        category: String,
        targetVenue: String,
        eventId: String,
        isPinned: Boolean
    ) {
        val payload = hashMapOf<String, Any>(
            "title" to title,
            "message" to message,
            "category" to category,
            "targetVenue" to targetVenue,
            "eventId" to eventId,
            "isPinned" to isPinned,
            "createdAt" to FieldValue.serverTimestamp()
        )
        db.collection("notifications").document().set(payload).await()
    }

    suspend fun deleteNotification(notificationId: String) {
        db.collection("notifications").document(notificationId).delete().await()
    }

    // --- Venues Directory ---

    fun observeVenues(): Flow<List<CaduceusVenue>> = db.collection("venues")
        .snapshots()
        .map { snapshot -> snapshot.toObjects(CaduceusVenue::class.java) }

    suspend fun saveVenue(venue: CaduceusVenue) {
        val docRef = if (venue.id.isBlank()) {
            db.collection("venues").document()
        } else {
            db.collection("venues").document(venue.id)
        }
        val payload = hashMapOf<String, Any>(
            "name" to venue.name,
            "description" to venue.description,
            "locationNote" to venue.locationNote,
            "mapUrl" to venue.mapUrl,
            "capacity" to venue.capacity
        )
        docRef.set(payload).await()
    }

    // --- Festival Settings ---

    fun observeFestSettings(): Flow<FestSettings> = db.collection("fest_settings").document("global")
        .snapshots()
        .map { snapshot ->
            if (snapshot.exists()) {
                val loaded = snapshot.toObject(FestSettings::class.java) ?: FestSettings()
                val canonicalIg = FestSettings().instagramUrl
                if (loaded.instagramUrl.isBlank() ||
                    loaded.instagramUrl == "https://instagram.com/caduceus.dmgmch" ||
                    loaded.instagramUrl == "https://www.instagram.com/caduceus.dmgmch"
                ) {
                    loaded.copy(instagramUrl = canonicalIg)
                } else {
                    loaded
                }
            } else {
                FestSettings()
            }
        }

    suspend fun updateFestSettings(settings: FestSettings) {
        val payload = hashMapOf<String, Any>(
            "festOpeningTimestamp" to settings.festOpeningTimestamp,
            "tagline" to settings.tagline,
            "instagramUrl" to settings.instagramUrl,
            "facebookUrl" to settings.facebookUrl,
            "youtubeUrl" to settings.youtubeUrl,
            "officialEmail" to settings.officialEmail,
            "isRegistrationOpen" to settings.isRegistrationOpen,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        db.collection("fest_settings").document("global").set(payload).await()
    }

    // --- Leaderboard ---

    fun observeLeaderboard(): Flow<List<LeaderboardEntry>> = db.collection("leaderboard")
        .orderBy("rank", Query.Direction.ASCENDING)
        .limit(50)
        .snapshots()
        .map { snapshot -> snapshot.toObjects(LeaderboardEntry::class.java) }

    suspend fun addLeaderboardEntry(entry: LeaderboardEntry) {
        val docRef = if (entry.id.isBlank()) {
            db.collection("leaderboard").document()
        } else {
            db.collection("leaderboard").document(entry.id)
        }
        val payload = hashMapOf<String, Any>(
            "competitionName" to entry.competitionName,
            "participantName" to entry.participantName,
            "collegeName" to entry.collegeName,
            "rank" to entry.rank,
            "points" to entry.points,
            "isVerified" to entry.isVerified
        )
        docRef.set(payload).await()
    }

    // --- Seed Initial Data Cleanly ---

    suspend fun seedInitialDataIfNeeded() {
        try {
            // Update fest_settings if missing or still using old defaults
            val settingsSnap = db.collection("fest_settings").document("global").get().await()
            val existingSettings = settingsSnap.toObject(FestSettings::class.java)
            if (!settingsSnap.exists() ||
                existingSettings?.officialEmail != "caduceus.fest@gmail.com" ||
                existingSettings.festOpeningTimestamp != "2026-12-17T09:00:00+05:30" ||
                existingSettings.instagramUrl != "https://www.instagram.com/caduceus.dmgmch?vrfl=OGtwOGtpZDE3dXBv"
            ) {
                updateFestSettings(FestSettings())
            }

            // Remove legacy sample events if present; keep admin-created events and Mohammed Irfan concert
            val legacyEventTitles = setOf(
                "Football Tournament: Clash of Serpents",
                "Battle of the Bands: Requiem of the Stars",
                "Gothic Runway: Eclipse Couture",
                "Solo Singing: The Siren's Call",
                "Street Dance: Shadows of the Cypher",
                "LAN Gaming: Occult Esports Arena"
            )
            val eventsSnap = db.collection("events").get().await()
            var hasIrfanEvent = false
            for (doc in eventsSnap.documents) {
                val title = doc.getString("title") ?: ""
                if (title in legacyEventTitles) {
                    doc.reference.delete().await()
                } else if (doc.id == "mohammed_irfan_live" || title.contains("MOHAMMED IRFAN", ignoreCase = true)) {
                    hasIrfanEvent = true
                }
            }
            if (!hasIrfanEvent) {
                seedMohammedIrfanConcert()
            }

            // Keep ONLY the single official campus address in venues
            val venuesSnap = db.collection("venues").get().await()
            var hasOfficialCampus = false
            for (doc in venuesSnap.documents) {
                if (doc.id == CAMPUS_VENUE_ID &&
                    doc.getString("name") == CAMPUS_VENUE_NAME &&
                    doc.getString("description") == CAMPUS_VENUE_ADDRESS
                ) {
                    hasOfficialCampus = true
                } else {
                    doc.reference.delete().await()
                }
            }
            if (!hasOfficialCampus) {
                seedSingleCampusVenue()
            }

            // Remove legacy sample notifications and ensure Mohammed Irfan announcement exists
            val legacyNotifTitles = setOf(
                "⚽ FOOTBALL COMPETITION STARTING SOON!",
                "🎸 BATTLE OF THE BANDS IS NOW LIVE!",
                "🚨 VENUE NOTICE: Gothic Runway Entry"
            )
            val notifsSnap = db.collection("notifications").get().await()
            var hasValidNotifs = false
            for (doc in notifsSnap.documents) {
                val title = doc.getString("title") ?: ""
                if (title in legacyNotifTitles) {
                    doc.reference.delete().await()
                } else {
                    hasValidNotifs = true
                }
            }
            if (!hasValidNotifs) {
                seedConcertAnnouncement()
            }

            // Remove legacy sample leaderboard entries
            val legacyLeaderboardTeams = setOf(
                "Infernal Pulse",
                "Crimson Eclipse",
                "Titan Serpents",
                "Shadow Walkers"
            )
            val lbSnap = db.collection("leaderboard").get().await()
            for (doc in lbSnap.documents) {
                val team = doc.getString("participantName") ?: ""
                if (team in legacyLeaderboardTeams) {
                    doc.reference.delete().await()
                }
            }
        } catch (e: Exception) {
            Log.w("CaduceusRepo", "Error seeding data: ${e.message}")
        }
    }

    private suspend fun seedMohammedIrfanConcert() {
        val irfanEvent = CaduceusEvent(
            id = "mohammed_irfan_live",
            title = "MOHAMMED IRFAN LIVE IN CONCERT",
            category = "Star Night",
            description = "Deben Mahata Government Medical College & Hospital (DMGMCH) Caduceus Organising Committee brings Bollywood singing sensation MOHAMMED IRFAN live at CADUCEUS 2026! Stay tuned for the complete festival schedule.",
            venueId = CAMPUS_VENUE_ID,
            venueName = "$CAMPUS_VENUE_NAME, $CAMPUS_VENUE_ADDRESS",
            startTime = "2026-12-19T18:00:00+05:30",
            endTime = "2026-12-19T22:30:00+05:30",
            status = EventStatus.UPCOMING.name,
            coordinatorName = "Caduceus Organising Committee",
            coordinatorPhone = "caduceus.fest@gmail.com",
            rules = "Official CADUCEUS 2026 Digital Fest Pass is mandatory for entry. Full event schedule coming soon!",
            isFeatured = true
        )
        createOrUpdateEvent(irfanEvent)
    }

    private suspend fun seedSingleCampusVenue() {
        val campusVenue = CaduceusVenue(
            id = CAMPUS_VENUE_ID,
            name = CAMPUS_VENUE_NAME,
            description = CAMPUS_VENUE_ADDRESS,
            locationNote = "Official CADUCEUS 2026 Venue",
            mapUrl = "geo:0,0?q=Deben+Mahata+Government+Medical+College+And+Hospital,+Hatuara,+Purulia+723147",
            capacity = ""
        )
        saveVenue(campusVenue)
    }

    private suspend fun seedConcertAnnouncement() {
        broadcastNotification(
            title = "🎤 MOHAMMED IRFAN LIVE AT CADUCEUS 2026!",
            message = "Mark your calendars for 19th December 2026! CADUCEUS 2026 begins on 17th December 2026. Stay tuned for the full competition schedule!",
            category = "URGENT_CALLOUT",
            targetVenue = "$CAMPUS_VENUE_NAME, $CAMPUS_VENUE_ADDRESS",
            eventId = "mohammed_irfan_live",
            isPinned = true
        )
    }
}
