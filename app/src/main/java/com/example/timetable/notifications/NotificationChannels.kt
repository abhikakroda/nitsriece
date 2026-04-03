package com.example.timetable.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannels {
    const val REMINDERS_ID = "reminders"
    const val EXAMS_ID = "exams"
    const val ONGOING_CLASS_ID = "ongoing_class"

    fun ensureCreated(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val reminders = NotificationChannel(
            REMINDERS_ID,
            "Reminders",
            NotificationManager.IMPORTANCE_HIGH
        )
        reminders.description = "Class reminders"

        val exams = NotificationChannel(
            EXAMS_ID,
            "Exams",
            NotificationManager.IMPORTANCE_HIGH
        )
        exams.description = "Exam reminders"

        val ongoingClass = NotificationChannel(
            ONGOING_CLASS_ID,
            "Ongoing Class",
            NotificationManager.IMPORTANCE_LOW
        )
        ongoingClass.description = "Current class progress notification"
        ongoingClass.setShowBadge(false)

        manager.createNotificationChannel(reminders)
        manager.createNotificationChannel(exams)
        manager.createNotificationChannel(ongoingClass)
    }
}
