package com.example.timetable.data

import kotlinx.serialization.Serializable

@Serializable
data class AttendanceStats(
    val present: Int = 0,
    val absent: Int = 0
) {
    val total: Int get() = present + absent
}

/**
 * Records which class slots the user explicitly marked ABSENT for a given date.
 * Any slot NOT in this set by end-of-day is auto-counted as present.
 * Key format: "YYYY-MM-DD:subject:startTime"
 */
@Serializable
data class DailyAttendanceRecord(
    val dateKey: String,          // "2026-02-21"
    val absentSlotKeys: Set<String> = emptySet()  // "subject:startTime"
)
