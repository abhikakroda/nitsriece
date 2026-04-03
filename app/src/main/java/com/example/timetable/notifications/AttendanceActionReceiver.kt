package com.example.timetable.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.runBlocking
import com.example.timetable.data.AppRepository

class AttendanceActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.getStringExtra(EXTRA_ACTION) ?: return
        val dateKey = intent.getStringExtra(EXTRA_DATE_KEY) ?: return
        val slotKey = intent.getStringExtra(EXTRA_SLOT_KEY) ?: return
        val subject = intent.getStringExtra(EXTRA_SUBJECT) ?: return
        val units = intent.getIntExtra(EXTRA_UNITS, 1).coerceAtLeast(1)

        val repository = AppRepository(context.applicationContext)
        runBlocking {
            when (action) {
                ACTION_MARK_PRESENT -> repository.markSlotPresent(dateKey, slotKey, subject, units)
                ACTION_MARK_ABSENT -> repository.markSlotAbsent(dateKey, slotKey, subject, units)
                else -> return@runBlocking
            }
        }
        OngoingClassNotifier.updateAndSchedule(context)
    }

    companion object {
        const val ACTION_MARK_PRESENT = "mark_present"
        const val ACTION_MARK_ABSENT = "mark_absent"
        const val EXTRA_ACTION = "attendance_action"
        const val EXTRA_DATE_KEY = "date_key"
        const val EXTRA_SLOT_KEY = "slot_key"
        const val EXTRA_SUBJECT = "subject"
        const val EXTRA_UNITS = "units"
    }
}
