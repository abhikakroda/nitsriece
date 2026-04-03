package com.example.timetable.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import com.example.timetable.MainActivity
import com.example.timetable.R
import com.example.timetable.data.AppDay
import com.example.timetable.data.AppFullDay
import com.example.timetable.data.AppMeal
import com.example.timetable.data.AppMealType
import com.example.timetable.data.AppRepository
import com.example.timetable.data.ExamPlan
import com.example.timetable.data.to12Hour
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class NextClassWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        // Launch a coroutine to fetch data
        CoroutineScope(Dispatchers.IO).launch {
            val repository = AppRepository(context)
            val timetable = repository.timetable.first()
            val messMenu = repository.messMenu.first()
            val examPlan = repository.examPlan.first()
            val dailyAbsentLogs = repository.dailyAbsentLogs.first()
            val dailyPresentLogs = repository.dailyPresentLogs.first()
            val canceledClasses = repository.canceledClasses.first()

            val calendar = Calendar.getInstance()
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            val appDay = when (dayOfWeek) {
                Calendar.MONDAY -> AppDay.Mon
                Calendar.TUESDAY -> AppDay.Tue
                Calendar.WEDNESDAY -> AppDay.Wed
                Calendar.THURSDAY -> AppDay.Thu
                Calendar.FRIDAY -> AppDay.Fri
                Calendar.SATURDAY -> AppDay.Sat
                Calendar.SUNDAY -> AppDay.Sun
                else -> AppDay.Mon
            }

            val currentMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
            val todayClasses = timetable[appDay] ?: emptyList()
            val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

            // Find classes that end after now
            val upcomingClasses = todayClasses.filter {
                val end = parseMinutes(it.endTime)
                end > currentMinutes
            }.sortedBy { parseMinutes(it.startTime) }

            val targetClass = upcomingClasses.firstOrNull()
            val nextExam = nextExamInfo(examPlan, calendar.timeInMillis)
            val currentMeal = currentMeal(
                when (dayOfWeek) {
                    Calendar.MONDAY -> AppFullDay.Mon
                    Calendar.TUESDAY -> AppFullDay.Tue
                    Calendar.WEDNESDAY -> AppFullDay.Wed
                    Calendar.THURSDAY -> AppFullDay.Thu
                    Calendar.FRIDAY -> AppFullDay.Fri
                    Calendar.SATURDAY -> AppFullDay.Sat
                    Calendar.SUNDAY -> AppFullDay.Sun
                    else -> AppFullDay.Mon
                },
                messMenu,
                currentMinutes
            )
            val attendanceAlert = attendanceAlert(
                todayClasses = todayClasses,
                dateKey = dateKey,
                nowMinutes = currentMinutes,
                dailyAbsentLogs = dailyAbsentLogs,
                dailyPresentLogs = dailyPresentLogs,
                canceledClasses = canceledClasses
            )

            val views = RemoteViews(context.packageName, R.layout.widget_next_class)
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pending = PendingIntent.getActivity(
                context,
                appWidgetId,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or pendingIntentImmutableFlag()
            )
            views.setOnClickPendingIntent(R.id.widget_root, pending)

            if (nextExam != null) {
                views.setTextViewText(R.id.widget_title, "NEXT EXAM")
                views.setTextViewText(R.id.widget_chip, countdownChipLabel(nextExam.startMillis, calendar.timeInMillis))
                views.setTextViewText(R.id.widget_subject, nextExam.subject)
                views.setTextViewText(R.id.widget_time, formatExamDate(nextExam.startMillis))
                views.setTextViewText(R.id.widget_room, formatExamCountdown(nextExam.startMillis, calendar.timeInMillis))
            } else if (targetClass != null) {
                val start = parseMinutes(targetClass.startTime)
                // If currently happening (start <= now < end), show "NOW HAPPENING"
                // If future (now < start), show "NEXT CLASS"
                val title = if (currentMinutes >= start) "NOW HAPPENING" else "NEXT CLASS"
                val timeStr = "${targetClass.startTime.to12Hour()} – ${targetClass.endTime.to12Hour()}"

                views.setTextViewText(R.id.widget_title, title)
                views.setTextViewText(R.id.widget_chip, "OPEN")
                views.setTextViewText(R.id.widget_subject, targetClass.subject)
                views.setTextViewText(R.id.widget_time, timeStr)
                views.setTextViewText(R.id.widget_room, targetClass.room)
            } else {
                views.setTextViewText(R.id.widget_title, "NO MORE CLASSES")
                views.setTextViewText(R.id.widget_chip, "DONE")
                views.setTextViewText(R.id.widget_subject, "Enjoy your day! 🎉")
                views.setTextViewText(R.id.widget_time, "")
                views.setTextViewText(R.id.widget_room, "")
            }

            views.setTextViewText(
                R.id.widget_meal,
                currentMeal?.let { "Meal • ${it.label}: ${it.items.firstOrNull().orEmpty()}" } ?: "Meal • No active meal right now"
            )
            views.setTextViewText(R.id.widget_attendance, "Attendance • $attendanceAlert")
            views.setTextViewText(
                R.id.widget_exam,
                nextExam?.let { "Exam • ${it.subject} • ${formatExamCountdown(it.startMillis, calendar.timeInMillis)}" }
                    ?: "Exam • No upcoming exam"
            )

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    private fun parseMinutes(time: String): Int {
        val p = time.split(":")
        if (p.size != 2) return 0
        return (p[0].toIntOrNull() ?: 0) * 60 + (p[1].toIntOrNull() ?: 0)
    }

    private fun pendingIntentImmutableFlag(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0

    companion object {
        fun refreshAll(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(
                android.content.ComponentName(context, NextClassWidget::class.java)
            )
            if (ids.isEmpty()) return
            ids.forEach { id ->
                NextClassWidget().updateAppWidget(context, appWidgetManager, id)
            }
        }

        private fun nextExamInfo(plan: ExamPlan, nowMillis: Long): WidgetExamInfo? {
            val startMinutes = parseStaticMinutes(plan.commonStartTime) ?: return null
            return plan.subjectExamDates
                .map { (subject, dayMillis) ->
                    WidgetExamInfo(
                        subject = subject,
                        startMillis = normalizeToStartOfDay(dayMillis) + startMinutes * 60_000L
                    )
                }
                .filter { it.startMillis >= nowMillis }
                .minByOrNull { it.startMillis }
        }

        private fun countdownChipLabel(targetMillis: Long, nowMillis: Long): String {
            val minutes = ((targetMillis - nowMillis).coerceAtLeast(0L) / 60_000L).toInt()
            val days = minutes / (24 * 60)
            val hours = (minutes % (24 * 60)) / 60
            return when {
                days > 0 -> "${days}D LEFT"
                hours > 0 -> "${hours}H LEFT"
                else -> "SOON"
            }
        }

        private fun formatExamCountdown(targetMillis: Long, nowMillis: Long): String {
            val minutes = ((targetMillis - nowMillis).coerceAtLeast(0L) / 60_000L).toInt()
            val days = minutes / (24 * 60)
            val hours = (minutes % (24 * 60)) / 60
            val mins = minutes % 60
            return when {
                days > 0 -> "Starts in ${days}d ${hours}h"
                hours > 0 -> "Starts in ${hours}h ${mins}m"
                else -> "Starts in ${mins}m"
            }
        }

        private fun formatExamDate(millis: Long): String =
            SimpleDateFormat("EEE, dd MMM • hh:mm a", Locale.getDefault()).format(Date(millis))

        private fun currentMeal(
            day: AppFullDay,
            messMenu: Map<AppFullDay, List<AppMeal>>,
            nowMinutes: Int
        ): AppMeal? {
            val meals = messMenu[day].orEmpty()
            return when {
                nowMinutes in 450..540 -> meals.firstOrNull { it.type == AppMealType.breakfast }
                nowMinutes in 720..840 -> meals.firstOrNull { it.type == AppMealType.lunch }
                nowMinutes in 1170..1260 -> meals.firstOrNull { it.type == AppMealType.dinner }
                else -> null
            }
        }

        private fun attendanceAlert(
            todayClasses: List<com.example.timetable.data.ClassSlot>,
            dateKey: String,
            nowMinutes: Int,
            dailyAbsentLogs: Map<String, Set<String>>,
            dailyPresentLogs: Map<String, Set<String>>,
            canceledClasses: Map<String, Set<String>>
        ): String {
            val absent = dailyAbsentLogs[dateKey].orEmpty()
            val present = dailyPresentLogs[dateKey].orEmpty()
            val canceled = canceledClasses[dateKey].orEmpty()
            val pending = todayClasses.count { slot ->
                val end = parseStaticMinutes(slot.endTime) ?: return@count false
                val key = "${slot.subject}:${slot.startTime}"
                end <= nowMinutes && key !in absent && key !in present && key !in canceled
            }
            return if (pending > 0) {
                "$pending class${if (pending == 1) "" else "es"} pending"
            } else {
                "All marked for today"
            }
        }

        private fun parseStaticMinutes(time: String): Int? {
            val p = time.split(":")
            if (p.size != 2) return null
            val h = p[0].toIntOrNull() ?: return null
            val m = p[1].toIntOrNull() ?: return null
            return h * 60 + m
        }

        private fun normalizeToStartOfDay(millis: Long): Long {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = millis
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            return calendar.timeInMillis
        }
    }
}

private data class WidgetExamInfo(
    val subject: String,
    val startMillis: Long
)
