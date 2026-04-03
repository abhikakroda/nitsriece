package com.example.timetable.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class OngoingClassUpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        OngoingClassNotifier.updateAndSchedule(context)
    }
}

