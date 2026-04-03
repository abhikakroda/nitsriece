package com.example.timetable.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.timetable.MainActivity
import com.example.timetable.R
import com.example.timetable.data.AppDay
import com.example.timetable.data.AppRepository
import com.example.timetable.data.to12Hour
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.ceil

object OngoingClassNotifier {
    private const val NOTIFICATION_ID = 42001
    private const val REQUEST_CODE = 42002
    private val notifierScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var updateJob: Job? = null

    fun updateAndSchedule(context: Context) {
        val appContext = context.applicationContext
        updateJob?.cancel()
        updateJob = notifierScope.launch {
            NotificationChannels.ensureCreated(appContext)

            // If DND mode is on, cancel any ongoing notification and skip
            val repo = AppRepository(appContext)
            val dndEnabled = repo.dndMode.first()
            if (dndEnabled) {
                NotificationManagerCompat.from(appContext).cancel(NOTIFICATION_ID)
                scheduleNextTick(appContext, System.currentTimeMillis() + 60_000L)
                return@launch
            }

            val timetable = repo.timetable.first()
            val now = Calendar.getInstance()
            val day = when (now.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> AppDay.Mon
                Calendar.TUESDAY -> AppDay.Tue
                Calendar.WEDNESDAY -> AppDay.Wed
                Calendar.THURSDAY -> AppDay.Thu
                Calendar.FRIDAY -> AppDay.Fri
                Calendar.SATURDAY -> AppDay.Sat
                Calendar.SUNDAY -> AppDay.Sun
                else -> null
            }

            val nowMins = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
            val slotsToday = day?.let { d -> timetable[d].orEmpty() }.orEmpty()

            val currentClass = slotsToday.firstOrNull { slot ->
                val start = parseMinutes(slot.startTime) ?: return@firstOrNull false
                val end = parseMinutes(slot.endTime) ?: return@firstOrNull false
                nowMins in start until end
            }

            val nextClass = slotsToday.firstOrNull { slot ->
                val start = parseMinutes(slot.startTime) ?: return@firstOrNull false
                start > nowMins
            }

            val activeClass = currentClass ?: nextClass

            if (activeClass != null) {
                val start = parseMinutes(activeClass.startTime) ?: 0
                val end = parseMinutes(activeClass.endTime) ?: (start + 1)
                val duration = (end - start).coerceAtLeast(1)
                val isLive = currentClass != null
                val elapsed = if (isLive) (nowMins - start).coerceIn(0, duration) else 0
                val remainingMins = if (isLive) {
                    (end - nowMins).coerceAtLeast(0)
                } else {
                    (start - nowMins).coerceAtLeast(0)
                }

                val contentIntent = PendingIntent.getActivity(
                    appContext,
                    0,
                    Intent(appContext, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or immutableFlag()
                )

                val canNotify = if (Build.VERSION.SDK_INT >= 33) {
                    ContextCompat.checkSelfPermission(
                        appContext,
                        android.Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                } else true

                if (canNotify) {
                    val progressPercent =
                        if (isLive) ((elapsed * 100f) / duration.toFloat()).toInt().coerceIn(0, 100) else 0
                    val statusText = if (isLive) "LIVE NOW" else "UP NEXT"
                    val attendanceUnits = ceil(duration / 50.0).toInt().coerceAtLeast(1)
                    val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now.time)
                    val slotKey = "${activeClass.subject}:${activeClass.startTime}"

                    val collapsed = RemoteViews(appContext.packageName, R.layout.notification_live_class_collapsed).apply {
                        setTextViewText(R.id.live_status, statusText)
                        setTextViewText(R.id.live_title, activeClass.subject)
                        setTextViewText(
                            R.id.live_subtitle,
                            "${activeClass.startTime.to12Hour()} - ${activeClass.endTime.to12Hour()} • Room ${activeClass.room}"
                        )
                    }
                    val expanded = RemoteViews(appContext.packageName, R.layout.notification_live_class_expanded).apply {
                        setTextViewText(R.id.live_status, statusText)
                        setTextViewText(
                            R.id.live_progress_text,
                            if (isLive) "$remainingMins min left" else "Starts in $remainingMins min"
                        )
                        setTextViewText(R.id.live_title, activeClass.subject)
                        setTextViewText(
                            R.id.live_subtitle,
                            if (isLive) {
                                activeClass.faculty?.let { "With $it" } ?: "Attendance and class progress"
                            } else {
                                activeClass.faculty?.let { "By $it" } ?: "Open schedule for details"
                            }
                        )
                        setTextViewText(
                            R.id.live_time,
                            "${activeClass.startTime.to12Hour()} - ${activeClass.endTime.to12Hour()}"
                        )
                        setTextViewText(R.id.live_room, activeClass.room)

                        setProgressBar(R.id.live_progress_bar, 100, progressPercent, false)
                        setViewVisibility(R.id.live_progress_bar, if (isLive) View.VISIBLE else View.GONE)
                    }

                    val notification = NotificationCompat.Builder(appContext, NotificationChannels.ONGOING_CLASS_ID)
                        .setSmallIcon(android.R.drawable.ic_popup_reminder)
                        .setContentTitle(if (isLive) "Now: ${activeClass.subject}" else "Up next: ${activeClass.subject}")
                        .setContentText(
                            "${activeClass.startTime.to12Hour()} - ${activeClass.endTime.to12Hour()} • Room ${activeClass.room}"
                        )
                        .setProgress(if (isLive) duration else 1, elapsed, false)
                        .setOngoing(true)
                        .setOnlyAlertOnce(true)
                        .setSilent(true)
                        .setPriority(NotificationCompat.PRIORITY_LOW)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setCategory(if (isLive) NotificationCompat.CATEGORY_PROGRESS else NotificationCompat.CATEGORY_EVENT)
                        .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                        .setCustomContentView(collapsed)
                        .setCustomBigContentView(expanded)
                        .setContentIntent(contentIntent)
                        .apply {
                            if (isLive) {
                                addAction(
                                    android.R.drawable.checkbox_on_background,
                                    "Present",
                                    attendanceActionPendingIntent(
                                        context = appContext,
                                        requestCode = 1,
                                        action = AttendanceActionReceiver.ACTION_MARK_PRESENT,
                                        dateKey = dateKey,
                                        slotKey = slotKey,
                                        subject = activeClass.subject,
                                        units = attendanceUnits
                                    )
                                )
                                addAction(
                                    android.R.drawable.ic_delete,
                                    "Absent",
                                    attendanceActionPendingIntent(
                                        context = appContext,
                                        requestCode = 2,
                                        action = AttendanceActionReceiver.ACTION_MARK_ABSENT,
                                        dateKey = dateKey,
                                        slotKey = slotKey,
                                        subject = activeClass.subject,
                                        units = attendanceUnits
                                    )
                                )
                            }
                        }
                        .build()

                    NotificationManagerCompat.from(appContext).notify(NOTIFICATION_ID, notification)
                }
                else {
                    // If the user denied notification permissions, don't leave a stale notification behind.
                    NotificationManagerCompat.from(appContext).cancel(NOTIFICATION_ID)
                }
            } else {
                NotificationManagerCompat.from(appContext).cancel(NOTIFICATION_ID)
            }

            scheduleNextTick(appContext, System.currentTimeMillis() + 60_000L)
        }
    }

    fun scheduleNextTick(context: Context, triggerAtMillis: Long) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, OngoingClassUpdateReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or immutableFlag()
        )
        runCatching {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
        }.getOrElse {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
        }
    }

    private fun parseMinutes(time: String): Int? {
        val p = time.split(":")
        if (p.size != 2) return null
        val h = p[0].toIntOrNull() ?: return null
        val m = p[1].toIntOrNull() ?: return null
        if (h !in 0..23 || m !in 0..59) return null
        return h * 60 + m
    }

    private fun immutableFlag(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0

    private fun attendanceActionPendingIntent(
        context: Context,
        requestCode: Int,
        action: String,
        dateKey: String,
        slotKey: String,
        subject: String,
        units: Int
    ): PendingIntent {
        val intent = Intent(context, AttendanceActionReceiver::class.java).apply {
            putExtra(AttendanceActionReceiver.EXTRA_ACTION, action)
            putExtra(AttendanceActionReceiver.EXTRA_DATE_KEY, dateKey)
            putExtra(AttendanceActionReceiver.EXTRA_SLOT_KEY, slotKey)
            putExtra(AttendanceActionReceiver.EXTRA_SUBJECT, subject)
            putExtra(AttendanceActionReceiver.EXTRA_UNITS, units)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or immutableFlag()
        )
    }
}
