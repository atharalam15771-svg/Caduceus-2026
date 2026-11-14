package com.example.data.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.MainActivity
import com.example.R
import java.util.concurrent.TimeUnit

object CaduceusNotificationHelper {
    private const val CHANNEL_ID = "caduceus_urgent_broadcasts"
    private const val CHANNEL_NAME = "CADUCEUS 2026 Live Alerts"
    private const val PREFS_NAME = "caduceus_notif_prefs"
    private const val KEY_SEEN_IDS = "seen_notification_ids"
    private const val WORK_NAME = "caduceus_bg_notification_sync"

    fun initNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Mobile notifications for CADUCEUS 2026 events, Mohammed Irfan concert updates, and venue alerts"
                enableVibration(true)
                enableLights(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun scheduleBackgroundSync(context: Context) {
        try {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val workRequest = PeriodicWorkRequestBuilder<CaduceusNotificationWorker>(
                15,
                TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        } catch (_: Exception) {}
    }

    fun hasSeenNotification(context: Context, docId: String): Boolean {
        if (docId.isBlank()) return false
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val seen = prefs.getStringSet(KEY_SEEN_IDS, emptySet()) ?: emptySet()
        return docId in seen
    }

    fun markNotificationSeen(context: Context, docId: String) {
        if (docId.isBlank()) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getStringSet(KEY_SEEN_IDS, emptySet())?.toMutableSet() ?: mutableSetOf()
        current.add(docId)
        // Bound stored IDs to prevent unbounded SharedPreferences growth
        val trimmed = if (current.size > 100) current.toList().takeLast(80).toSet() else current
        prefs.edit().putStringSet(KEY_SEEN_IDS, trimmed).apply()
    }

    fun showHeadsUpNotificationIfUnseen(
        context: Context,
        docId: String,
        title: String,
        message: String,
        targetVenue: String? = null
    ): Boolean {
        if (docId.isNotBlank() && hasSeenNotification(context, docId)) {
            return false
        }
        val notifId = if (docId.isNotBlank()) docId.hashCode() else (1000..99999).random()
        showHeadsUpNotification(context, notifId, title, message, targetVenue)
        if (docId.isNotBlank()) {
            markNotificationSeen(context, docId)
        }
        return true
    }

    fun showHeadsUpNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String,
        targetVenue: String? = null
    ) {
        try {
            initNotificationChannel(context)
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("open_tab", "NOTIFICATIONS")
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val fullText = if (!targetVenue.isNullOrBlank()) {
                "$message\n📍 $targetVenue"
            } else {
                message
            }

            val largeLogo = try {
                BitmapFactory.decodeResource(context.resources, R.drawable.caduceus_logo)
            } catch (_: Exception) {
                null
            }

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_caduceus)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(fullText))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            if (largeLogo != null) {
                builder.setLargeIcon(largeLogo)
            }

            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (_: SecurityException) {
            // Runtime notification permission not granted yet
        } catch (_: Exception) {}
    }
}
