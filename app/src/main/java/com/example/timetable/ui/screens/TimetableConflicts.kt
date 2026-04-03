package com.example.timetable.ui.screens

import com.example.timetable.data.ClassSlot

data class ScheduleConflict(
    val first: ClassSlot,
    val second: ClassSlot
) {
    val label: String
        get() = "${first.subject} overlaps ${second.subject}"
}

fun findDayConflicts(slots: List<ClassSlot>): List<ScheduleConflict> {
    val sorted = slots.sortedBy { parseConflictMinutes(it.startTime) ?: Int.MAX_VALUE }
    val conflicts = mutableListOf<ScheduleConflict>()
    for (index in sorted.indices) {
        val current = sorted[index]
        val currentStart = parseConflictMinutes(current.startTime) ?: continue
        val currentEnd = parseConflictMinutes(current.endTime) ?: continue
        for (nextIndex in index + 1 until sorted.size) {
            val next = sorted[nextIndex]
            val nextStart = parseConflictMinutes(next.startTime) ?: continue
            val nextEnd = parseConflictMinutes(next.endTime) ?: continue
            if (nextStart >= currentEnd) break
            if (currentStart < nextEnd && nextStart < currentEnd) {
                conflicts += ScheduleConflict(current, next)
            }
        }
    }
    return conflicts
}

private fun parseConflictMinutes(time: String): Int? {
    val parts = time.trim().split(":")
    if (parts.size != 2) return null
    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null
    if (hour !in 0..23 || minute !in 0..59) return null
    return hour * 60 + minute
}
