package com.example.timetable.notifications

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.annotation.SuppressLint
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.timetable.MainActivity
import com.example.timetable.data.appDataStore
import com.example.timetable.data.AppDay
import com.example.timetable.data.ClassSlot
import com.example.timetable.data.DefaultTimetable
import com.example.timetable.R
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.math.absoluteValue

class ReminderReceiver : BroadcastReceiver() {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        NotificationChannels.ensureCreated(context)

        val type = intent.getStringExtra(EXTRA_TYPE) ?: return
        val id = intent.getStringExtra(EXTRA_ID) ?: return
        val title = intent.getStringExtra(EXTRA_TITLE) ?: ""
        val text = intent.getStringExtra(EXTRA_TEXT) ?: ""
        val room = intent.getStringExtra(EXTRA_ROOM) ?: ""
        val startTime = intent.getStringExtra(EXTRA_START_TIME) ?: ""

        if (type == TYPE_CLASS || type == TYPE_ATTENDANCE_CHECK || type == TYPE_ASSIGNMENT) {
            val prefs = runBlocking { context.appDataStore.data.first() }
            val dndEnabled = prefs[booleanPreferencesKey("dnd_mode")] ?: false
            if (dndEnabled) return
            val enabled = prefs[booleanPreferencesKey("reminders_enabled")] ?: false
            if (!enabled) return
        }

        val openApp = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            openApp,
            PendingIntent.FLAG_UPDATE_CURRENT or pendingIntentImmutableFlag()
        )

        val channelId = when (type) {
            TYPE_EXAM -> NotificationChannels.EXAMS_ID
            TYPE_ASSIGNMENT -> NotificationChannels.REMINDERS_ID
            else -> NotificationChannels.REMINDERS_ID
        }

        val customLayout = RemoteViews(context.packageName, R.layout.notification_collapsed)
        val customLayoutExpanded = RemoteViews(context.packageName, R.layout.notification_expanded)

        if (type == TYPE_ATTENDANCE_CHECK) {
            val pendingCount = runBlocking { countPendingUnmarkedAttendance(context) }
            val now = java.util.Calendar.getInstance()
            val cutoffToday = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, 23)
                set(java.util.Calendar.MINUTE, 59)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }

            if (pendingCount > 0) {
                customLayout.setTextViewText(R.id.notification_title, "Attendance pending")
                customLayout.setTextViewText(R.id.notification_time, "Action needed")

                customLayoutExpanded.setTextViewText(R.id.notification_title, "Mark attendance")
                customLayoutExpanded.setTextViewText(
                    R.id.notification_text,
                    "You still have $pendingCount class(es) unmarked for today."
                )
                customLayoutExpanded.setTextViewText(R.id.notification_room, "Open Schedule and mark Present/Absent.")
                customLayoutExpanded.setTextViewText(R.id.notification_time, "Repeats every 10 min")
            }

            // Keep reminding every 10 minutes until resolved or end of day.
            if (pendingCount > 0 && now.timeInMillis + 10 * 60_000L < cutoffToday.timeInMillis) {
                NotificationScheduler.scheduleNextEveningAttendanceCheck(
                    context,
                    now.timeInMillis + 10 * 60_000L
                )
            } else {
                // Schedule next daily check at 6 PM (tomorrow if needed).
                val nextDaily = java.util.Calendar.getInstance().apply {
                    set(java.util.Calendar.SECOND, 0)
                    set(java.util.Calendar.MILLISECOND, 0)
                    set(java.util.Calendar.HOUR_OF_DAY, 18)
                    set(java.util.Calendar.MINUTE, 0)
                    if (timeInMillis <= System.currentTimeMillis()) {
                        add(java.util.Calendar.DAY_OF_YEAR, 1)
                    }
                }
                NotificationScheduler.scheduleNextEveningAttendanceCheck(context, nextDaily.timeInMillis)
            }

            if (pendingCount == 0) return
        } else if (type == TYPE_CLASS) {
            customLayout.setTextViewText(R.id.notification_title, title)
            customLayout.setTextViewText(R.id.notification_time, startTime)
            
            customLayoutExpanded.setTextViewText(R.id.notification_title, title)
            customLayoutExpanded.setTextViewText(R.id.notification_text, text)
            customLayoutExpanded.setTextViewText(R.id.notification_room, room.ifEmpty { "TBA" })
            customLayoutExpanded.setTextViewText(R.id.notification_time, startTime)
        } else {
            customLayout.setTextViewText(R.id.notification_title, title)
            customLayout.setTextViewText(R.id.notification_time, "")
            
            customLayoutExpanded.setTextViewText(R.id.notification_title, title)
            customLayoutExpanded.setTextViewText(R.id.notification_text, text)
            customLayoutExpanded.setTextViewText(R.id.notification_room, "")
            customLayoutExpanded.setTextViewText(R.id.notification_time, "")
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(customLayout)
            .setCustomBigContentView(customLayoutExpanded)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setColor(context.getColor(android.R.color.holo_orange_dark))
            .build()

        runCatching {
            NotificationManagerCompat.from(context).notify(id.hashCode().absoluteValue, notification)
        }

        if (type == TYPE_CLASS) {
            val day = intent.getStringExtra(EXTRA_DAY) ?: return
            val classStartTime = intent.getStringExtra(EXTRA_START_TIME) ?: return
            val minutesBefore = intent.getLongExtra(EXTRA_MINUTES_BEFORE, 10L)
            NotificationScheduler.scheduleNextOccurrenceFromReceiver(
                context = context,
                id = id,
                dayName = day,
                startTime = classStartTime,
                minutesBefore = minutesBefore,
                title = title,
                text = text
            )
        }
    }

    companion object {
        const val TYPE_CLASS = "class"
        const val TYPE_EXAM = "exam"
        const val TYPE_ASSIGNMENT = "assignment"
        const val TYPE_ATTENDANCE_CHECK = "attendance_check"

        const val EXTRA_TYPE = "type"
        const val EXTRA_ID = "id"
        const val EXTRA_TITLE = "title"
        const val EXTRA_TEXT = "text"
        const val EXTRA_ROOM = "room"
        const val EXTRA_DAY = "day"
        const val EXTRA_START_TIME = "start_time"
        const val EXTRA_MINUTES_BEFORE = "minutes_before"
    }

    private suspend fun countPendingUnmarkedAttendance(context: Context): Int {
        val prefs = context.appDataStore.data.first()
        val nowCal = java.util.Calendar.getInstance()
        val todayDay = when (nowCal.get(java.util.Calendar.DAY_OF_WEEK)) {
            java.util.Calendar.MONDAY -> AppDay.Mon
            java.util.Calendar.TUESDAY -> AppDay.Tue
            java.util.Calendar.WEDNESDAY -> AppDay.Wed
            java.util.Calendar.THURSDAY -> AppDay.Thu
            java.util.Calendar.FRIDAY -> AppDay.Fri
            java.util.Calendar.SATURDAY -> AppDay.Sat
            java.util.Calendar.SUNDAY -> AppDay.Sun
            else -> null
        } ?: return 0

        val timetableRaw = prefs[stringPreferencesKey("timetable_json")]
        val timetable = if (timetableRaw.isNullOrBlank()) {
            DefaultTimetable
        } else {
            runCatching { json.decodeFromString<Map<AppDay, List<ClassSlot>>>(timetableRaw) }
                .getOrDefault(DefaultTimetable)
        }

        val dateKey = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(nowCal.time)
        val absentMapRaw = prefs[stringPreferencesKey("daily_logs_json")]
        val presentMapRaw = prefs[stringPreferencesKey("daily_present_logs_json")]
        val canceledMapRaw = prefs[stringPreferencesKey("canceled_classes_json")]
        val absentMap = if (absentMapRaw.isNullOrBlank()) emptyMap()
        else runCatching { json.decodeFromString<Map<String, Set<String>>>(absentMapRaw) }.getOrDefault(emptyMap())
        val presentMap = if (presentMapRaw.isNullOrBlank()) emptyMap()
        else runCatching { json.decodeFromString<Map<String, Set<String>>>(presentMapRaw) }.getOrDefault(emptyMap())
        val canceledMap = if (canceledMapRaw.isNullOrBlank()) emptyMap()
        else runCatching { json.decodeFromString<Map<String, Set<String>>>(canceledMapRaw) }.getOrDefault(emptyMap())

        val absentSlots = absentMap[dateKey].orEmpty()
        val presentSlots = presentMap[dateKey].orEmpty()
        val canceledSlots = canceledMap[dateKey].orEmpty()
        val nowMinutes = nowCal.get(java.util.Calendar.HOUR_OF_DAY) * 60 + nowCal.get(java.util.Calendar.MINUTE)

        return timetable[todayDay].orEmpty().count { slot ->
            val endMins = parseTimeToMinutes(slot.endTime) ?: return@count false
            val slotKey = "${slot.subject}:${slot.startTime}"
            endMins <= nowMinutes &&
                slotKey !in absentSlots &&
                slotKey !in presentSlots &&
                slotKey !in canceledSlots
        }
    }

    private fun parseTimeToMinutes(text: String): Int? {
        val parts = text.trim().split(":")
        if (parts.size != 2) return null
        val h = parts[0].toIntOrNull() ?: return null
        val m = parts[1].toIntOrNull() ?: return null
        if (h !in 0..23 || m !in 0..59) return null
        return h * 60 + m
    }
}

private fun pendingIntentImmutableFlag(): Int =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
