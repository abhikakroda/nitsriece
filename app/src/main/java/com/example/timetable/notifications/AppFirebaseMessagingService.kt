package com.example.timetable.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.timetable.MainActivity
import com.example.timetable.data.AppRepository
import com.example.timetable.data.SavedNotification
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.runBlocking
import kotlin.math.absoluteValue

class AppFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "FCM token refreshed: $token")
        // TODO: send this token to your backend to target this user/device.
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        NotificationChannels.ensureCreated(this)

        val data = message.data
        val action = data["action"] ?: data["type"]
        if (!action.isNullOrBlank() && handleCloudAction(action, data)) {
            return
        }

        val title = message.notification?.title
            ?: message.data["title"]
            ?: "Timetable"
        val body = message.notification?.body
            ?: message.data["body"]
            ?: "You have a new update."

        // Persist to DataStore so the user can view "admin/all" notifications later.
        val sender = data["sender"] ?: data["from"] ?: data["sent_by"]
        val audience = data["audience"] ?: data["target"] ?: data["scope"]
        val isAdmin = sender?.lowercase()?.contains("admin") == true ||
            audience?.lowercase()?.contains("admin") == true ||
            data["sent_by_admin"]?.lowercase() == "true"

        val id = message.messageId ?: "${System.currentTimeMillis()}"
        runBlocking {
            runCatching {
                AppRepository(this@AppFirebaseMessagingService).addSavedNotification(
                    SavedNotification(
                        id = id,
                        title = title,
                        body = body,
                        receivedAtMillis = System.currentTimeMillis(),
                        sender = sender,
                        audience = audience,
                        isAdmin = isAdmin
                    )
                )
            }.onFailure {
                Log.e(TAG, "Failed to save FCM notification", it)
            }
        }

        showNotification(title = title, body = body)
    }

    private fun showNotification(title: String, body: String) {
        val canNotify = if (Build.VERSION.SDK_INT >= 33) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true
        if (!canNotify) return

        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPending = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or immutableFlag()
        )

        val notification = NotificationCompat.Builder(this, NotificationChannels.REMINDERS_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openPending)
            .build()

        NotificationManagerCompat.from(this).notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun handleCloudAction(action: String, data: Map<String, String>): Boolean {
        if (action != "cancel_class_notification") return false

        val explicitId = data["id"]?.trim().orEmpty()
        val day = data["day"]?.trim().orEmpty()
        val startTime = data["start_time"]?.trim().orEmpty()
        val subject = data["subject"]?.trim().orEmpty()

        val targetId = when {
            explicitId.isNotBlank() -> explicitId
            day.isNotBlank() && startTime.isNotBlank() && subject.isNotBlank() -> {
                NotificationScheduler.buildClassReminderId(day, startTime, subject)
            }
            else -> return true
        }

        NotificationScheduler.cancelScheduledById(this, targetId)
        NotificationManagerCompat.from(this).cancel(targetId.hashCode().absoluteValue)
        return true
    }

    private fun immutableFlag(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0

    companion object {
        private const val TAG = "AppFCMService"
    }
}
