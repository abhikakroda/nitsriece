package com.example.timetable.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.core.edit
import com.example.timetable.data.AppDay
import com.example.timetable.data.AssignmentItem
import com.example.timetable.data.AssignmentStatus
import com.example.timetable.data.ClassSlot
import com.example.timetable.data.DefaultTimetable
import com.example.timetable.data.appDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.absoluteValue

object NotificationScheduler {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val TimetableJson = stringPreferencesKey("timetable_json")
    private val AssignmentsJson = stringPreferencesKey("assignments_json")
    private val ExamDateMillis = longPreferencesKey("exam_date_millis")
    private val RemindersEnabled = booleanPreferencesKey("reminders_enabled")
    private val ReminderMinutesBefore = longPreferencesKey("reminder_minutes_before")
    private val ScheduledAlarmIds = stringSetPreferencesKey("scheduled_alarm_ids")
    private const val AttendanceEveningCheckId = "attendance_evening_check"
    private val schedulerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun rescheduleAll(context: Context) {
        val appContext = context.applicationContext
        schedulerScope.launch {
            NotificationChannels.ensureCreated(context)

            val prefs = appContext.appDataStore.data.first()
            val enabled = prefs[RemindersEnabled] ?: false
            val minutesBefore = prefs[ReminderMinutesBefore] ?: 10L
            val examDateMillis = prefs[ExamDateMillis]
            val rawTimetable = prefs[TimetableJson]
            val rawAssignments = prefs[AssignmentsJson]
            val assignments = if (rawAssignments.isNullOrBlank()) {
                emptyList()
            } else {
                runCatching { json.decodeFromString<List<AssignmentItem>>(rawAssignments) }.getOrDefault(emptyList())
            }

            val timetable = if (rawTimetable.isNullOrBlank()) {
                DefaultTimetable
            } else {
                runCatching { json.decodeFromString<Map<AppDay, List<ClassSlot>>>(rawTimetable) }
                    .getOrDefault(DefaultTimetable)
            }

            val oldIds = prefs[ScheduledAlarmIds] ?: emptySet()
            oldIds.forEach { cancelById(appContext, it) }

            val newIds = buildSet {
                if (enabled) {
                    timetable.forEach { (day, slots) ->
                        slots.forEach { slot ->
                            add(scheduleClass(appContext, day, slot, minutesBefore))
                        }
                    }
                    add(scheduleEveningAttendanceCheck(appContext))
                }
                if (examDateMillis != null) {
                    scheduleExam(appContext, examDateMillis)?.let { add(it) }
                }
            }

            appContext.appDataStore.edit { it[ScheduledAlarmIds] = newIds }
            assignments.forEach { rescheduleAssignmentAlarms(appContext, it) }
        }
    }

    fun cancelAll(context: Context) {
        runBlocking {
            val prefs = context.appDataStore.data.first()
            val ids = prefs[ScheduledAlarmIds] ?: emptySet()
            ids.forEach { cancelById(context, it) }
            context.appDataStore.edit { it[ScheduledAlarmIds] = emptySet() }
        }
    }

    fun cancelScheduledById(context: Context, id: String): Boolean {
        if (id.isBlank()) return false
        cancelById(context, id)
        runBlocking {
            val prefs = context.appDataStore.data.first()
            val existing = prefs[ScheduledAlarmIds] ?: emptySet()
            context.appDataStore.edit { it[ScheduledAlarmIds] = existing - id }
        }
        return true
    }

    fun buildClassReminderId(dayName: String, startTime: String, subject: String): String {
        return "class:${dayName}:${startTime}:${subject}"
    }

    fun scheduleCustomInMinutes(
        context: Context,
        title: String,
        text: String,
        delayMinutes: Long
    ): String {
        NotificationChannels.ensureCreated(context)
        val safeDelay = delayMinutes.coerceAtLeast(1L)
        val id = "custom:${System.currentTimeMillis()}:${title.hashCode().absoluteValue}"
        val triggerAt = System.currentTimeMillis() + safeDelay * 60_000L

        setAlarm(context, id, triggerAt) {
            putExtra(ReminderReceiver.EXTRA_TYPE, ReminderReceiver.TYPE_EXAM)
            putExtra(ReminderReceiver.EXTRA_ID, id)
            putExtra(ReminderReceiver.EXTRA_TITLE, title.ifBlank { "Custom Reminder" })
            putExtra(ReminderReceiver.EXTRA_TEXT, text.ifBlank { "This is your scheduled reminder." })
        }

        runBlocking {
            val prefs = context.appDataStore.data.first()
            val existing = prefs[ScheduledAlarmIds] ?: emptySet()
            context.appDataStore.edit { it[ScheduledAlarmIds] = existing + id }
        }

        return id
    }

    fun scheduleCustomAtTime(
        context: Context,
        title: String,
        text: String,
        triggerAtMillis: Long,
        notificationType: String = ReminderReceiver.TYPE_EXAM,
        alarmId: String? = null
    ): String? {
        NotificationChannels.ensureCreated(context)
        if (triggerAtMillis <= System.currentTimeMillis()) return null

        val id = alarmId ?: "custom_at:${triggerAtMillis}:${title.hashCode().absoluteValue}"
        setAlarm(context, id, triggerAtMillis) {
            putExtra(ReminderReceiver.EXTRA_TYPE, notificationType)
            putExtra(ReminderReceiver.EXTRA_ID, id)
            putExtra(ReminderReceiver.EXTRA_TITLE, title.ifBlank { "Reminder" })
            putExtra(ReminderReceiver.EXTRA_TEXT, text.ifBlank { "You have an upcoming task." })
        }

        runBlocking {
            val prefs = context.appDataStore.data.first()
            val existing = prefs[ScheduledAlarmIds] ?: emptySet()
            context.appDataStore.edit { it[ScheduledAlarmIds] = existing + id }
        }

        return id
    }

    fun rescheduleAssignmentAlarms(context: Context, item: AssignmentItem) {
        cancelAssignmentAlarms(context, item.id)
        if (item.status == AssignmentStatus.Done) return
        val due = item.dueAtMillis
        if (due <= System.currentTimeMillis()) return
        if (item.reminderOffsetsMinutes.isEmpty()) return
        for (offsetMin in item.reminderOffsetsMinutes.distinct().sortedDescending()) {
            val trigger = due - offsetMin * 60_000L
            if (trigger <= System.currentTimeMillis()) continue
            val id = "assignment:${item.id}:$offsetMin"
            val label = when (offsetMin) {
                10080L -> "1 week before"
                1440L -> "1 day before"
                60L -> "1 hour before"
                15L -> "15 minutes before"
                else -> "${offsetMin}m before"
            }
            scheduleCustomAtTime(
                context = context,
                title = "Due: ${item.title}",
                text = "${item.subject.ifBlank { "Assignment" }} • $label",
                triggerAtMillis = trigger,
                notificationType = ReminderReceiver.TYPE_ASSIGNMENT,
                alarmId = id
            )
        }
    }

    fun cancelAssignmentAlarms(context: Context, assignmentId: String) {
        val prefs = runBlocking { context.appDataStore.data.first() }
        val existing = prefs[ScheduledAlarmIds] ?: emptySet()
        val prefix = "assignment:$assignmentId:"
        existing.filter { it.startsWith(prefix) }.forEach { cancelScheduledById(context, it) }
    }

    fun scheduleNextOccurrenceFromReceiver(
        context: Context,
        id: String,
        dayName: String,
        startTime: String,
        minutesBefore: Long,
        title: String,
        text: String
    ) {
        runBlocking {
            val prefs = context.appDataStore.data.first()
            val enabled = prefs[RemindersEnabled] ?: false
            if (!enabled) return@runBlocking
        }

        val day = runCatching { AppDay.valueOf(dayName) }.getOrNull() ?: return
        val triggerAt = computeNextTriggerAtMillis(
            day = day,
            startTime = startTime,
            minutesBefore = minutesBefore,
            allowImmediateIfInsideWindow = false
        ) ?: return
        setAlarm(context, id, triggerAt) {
            putExtra(ReminderReceiver.EXTRA_TYPE, ReminderReceiver.TYPE_CLASS)
            putExtra(ReminderReceiver.EXTRA_ID, id)
            putExtra(ReminderReceiver.EXTRA_DAY, day.name)
            putExtra(ReminderReceiver.EXTRA_START_TIME, startTime)
            putExtra(ReminderReceiver.EXTRA_MINUTES_BEFORE, minutesBefore)
            putExtra(ReminderReceiver.EXTRA_TITLE, title)
            putExtra(ReminderReceiver.EXTRA_TEXT, text)
            // (Note: we don't have the room mapped efficiently here, but we could add it back)
        }
    }

    fun scheduleNextEveningAttendanceCheck(context: Context, triggerAtMillis: Long) {
        setAlarm(context, AttendanceEveningCheckId, triggerAtMillis) {
            putExtra(ReminderReceiver.EXTRA_TYPE, ReminderReceiver.TYPE_ATTENDANCE_CHECK)
            putExtra(ReminderReceiver.EXTRA_ID, AttendanceEveningCheckId)
        }
    }

    private fun scheduleClass(context: Context, day: AppDay, slot: ClassSlot, minutesBefore: Long): String {
        val id = "class:${day.name}:${slot.startTime}:${slot.subject}"
        val triggerAt = computeNextTriggerAtMillis(
            day = day,
            startTime = slot.startTime,
            minutesBefore = minutesBefore,
            allowImmediateIfInsideWindow = true
        ) ?: return id
        val slangTitles = listOf(
            "Uthja bhai, class time ho gaya!",
            "Bhai padhai chhod, class shuru hone wali hai!",
            "Chal nikal, ${slot.subject} ki class aa gayi!",
            "Alarm baj gaya, ab mujre band kar!",
            "${slot.subject} wale sir wait kar rahe hain tujhe!"
        )
        val slangTexts = listOf(
            "${slot.time} • Room ${slot.room} • Bhai ${minutesBefore} min bacha hai!",
            "${slot.time} • Room ${slot.room} • Late mat ho yaar, attendance kaatenge!",
            "${slot.time} • Room ${slot.room} • Nikal abhi, proxy nahi chalegi!",
            "${slot.time} • Room ${slot.room} • Bhai bhai bhai, chal jaldi!"
        )
        val title = slangTitles.random()
        val text = slangTexts.random()
        setAlarm(context, id, triggerAt) {
            putExtra(ReminderReceiver.EXTRA_TYPE, ReminderReceiver.TYPE_CLASS)
            putExtra(ReminderReceiver.EXTRA_ID, id)
            putExtra(ReminderReceiver.EXTRA_DAY, day.name)
            putExtra(ReminderReceiver.EXTRA_START_TIME, slot.startTime)
            putExtra(ReminderReceiver.EXTRA_MINUTES_BEFORE, minutesBefore)
            putExtra(ReminderReceiver.EXTRA_TITLE, title)
            putExtra(ReminderReceiver.EXTRA_TEXT, text)
            putExtra(ReminderReceiver.EXTRA_ROOM, slot.room)
        }
        return id
    }

    private fun scheduleEveningAttendanceCheck(context: Context): String {
        val cal = Calendar.getInstance().apply {
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.HOUR_OF_DAY, 18)
            set(Calendar.MINUTE, 0)
        }
        if (cal.timeInMillis <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        scheduleNextEveningAttendanceCheck(context, cal.timeInMillis)
        return AttendanceEveningCheckId
    }

    private fun scheduleExam(context: Context, examDateMillis: Long): String? {
        val id = "exam:${examDateMillis}"
        val cal = Calendar.getInstance().apply {
            timeInMillis = examDateMillis
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, -1)
        }
        if (cal.timeInMillis <= System.currentTimeMillis()) return null

        setAlarm(context, id, cal.timeInMillis) {
            putExtra(ReminderReceiver.EXTRA_TYPE, ReminderReceiver.TYPE_EXAM)
            putExtra(ReminderReceiver.EXTRA_ID, id)
            putExtra(ReminderReceiver.EXTRA_TITLE, "Kal exam hai bhai! 📚")
            putExtra(ReminderReceiver.EXTRA_TEXT, "Raat bhar jaagna padega, uthh aur padh le abhi se!")
        }
        return id
    }

    private fun cancelById(context: Context, id: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context,
            id.hashCode().absoluteValue,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or pendingIntentImmutableFlag()
        )
        alarmManager.cancel(pi)
    }

    private fun computeNextTriggerAtMillis(
        day: AppDay,
        startTime: String,
        minutesBefore: Long,
        allowImmediateIfInsideWindow: Boolean
    ): Long? {
        val startMinutes = parseTimeToMinutes(startTime) ?: return null
        val target = Calendar.getInstance().apply {
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val targetDow = when (day) {
            AppDay.Mon -> Calendar.MONDAY
            AppDay.Tue -> Calendar.TUESDAY
            AppDay.Wed -> Calendar.WEDNESDAY
            AppDay.Thu -> Calendar.THURSDAY
            AppDay.Fri -> Calendar.FRIDAY
            AppDay.Sat -> Calendar.SATURDAY
            AppDay.Sun -> Calendar.SUNDAY
        }

        target.set(Calendar.DAY_OF_WEEK, targetDow)
        target.set(Calendar.HOUR_OF_DAY, startMinutes / 60)
        target.set(Calendar.MINUTE, startMinutes % 60)

        val classStartAt = target.timeInMillis
        var triggerAt = classStartAt - minutesBefore * 60_000L
        val now = System.currentTimeMillis()
        if (allowImmediateIfInsideWindow && now in triggerAt until classStartAt) {
            return now + 5_000L
        }
        if (triggerAt <= now) triggerAt += 7L * 24L * 60L * 60L * 1000L
        return triggerAt
    }

    private inline fun setAlarm(context: Context, id: String, triggerAtMillis: Long, fillIntent: Intent.() -> Unit) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply(fillIntent)
        val pi = PendingIntent.getBroadcast(
            context,
            id.hashCode().absoluteValue,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or pendingIntentImmutableFlag()
        )
        runCatching {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
        }.getOrElse {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
        }
    }

    private fun pendingIntentImmutableFlag(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0

    private fun parseTimeToMinutes(text: String): Int? {
        val parts = text.trim().split(":")
        if (parts.size != 2) return null
        val h = parts[0].toIntOrNull() ?: return null
        val m = parts[1].toIntOrNull() ?: return null
        if (h !in 0..23 || m !in 0..59) return null
        return h * 60 + m
    }
}
