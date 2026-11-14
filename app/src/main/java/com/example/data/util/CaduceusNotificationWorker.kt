package com.example.data.util

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.tasks.await

class CaduceusNotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val uid = Firebase.auth.currentUser?.uid ?: return Result.success()
            val dbId = applicationContext.getString(R.string.firestore_database_id)
            val db = FirebaseFirestore.getInstance(dbId)

            val userSnap = db.collection("users").document(uid).get().await()
            val role = userSnap.getString("role") ?: ""
            val approvalStatus = userSnap.getString("approvalStatus") ?: "PENDING"
            val isApprovedOrAdmin = role == "SUPER_ADMIN" ||
                role == "EVENT_ADMIN" ||
                approvalStatus == "APPROVED"
            if (!isApprovedOrAdmin) {
                return Result.success()
            }

            val snapshot = db.collection("notifications")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .await()

            for (doc in snapshot.documents.reversed()) {
                val title = doc.getString("title") ?: continue
                val message = doc.getString("message") ?: continue
                val targetVenue = doc.getString("targetVenue")
                CaduceusNotificationHelper.showHeadsUpNotificationIfUnseen(
                    context = applicationContext,
                    docId = doc.id,
                    title = title,
                    message = message,
                    targetVenue = targetVenue
                )
            }
            Result.success()
        } catch (e: Exception) {
            Log.w("CaduceusNotifWorker", "Background sync skipped: ${e.message}")
            Result.retry()
        }
    }
}

class CaduceusMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "CADUCEUS 2026 Alert"
        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["message"]
            ?: return
        val venue = remoteMessage.data["targetVenue"]
        val docId = remoteMessage.data["notificationId"] ?: remoteMessage.messageId ?: ""

        CaduceusNotificationHelper.showHeadsUpNotificationIfUnseen(
            context = applicationContext,
            docId = docId,
            title = title,
            message = body,
            targetVenue = venue
        )
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("CaduceusFCM", "Refreshed FCM token")
    }
}
