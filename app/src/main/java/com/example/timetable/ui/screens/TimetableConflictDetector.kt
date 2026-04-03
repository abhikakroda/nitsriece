package com.example.timetable.ui.screens

import com.example.timetable.data.AppDay
import com.example.timetable.data.ClassSlot

data class TimetableConflict(
    val message: String,
    val overlappingSlots: List<ClassSlot> = emptyList()
)

fun findTimetableConflict(
    timetable: Map<AppDay, List<ClassSlot>>,
    targetDay: AppDay,
    candidate: ClassSlot,
    ignoreSlot: ClassSlot? = null
): TimetableConflict? {
    val candidateStart = parseTimeToMinutes(candidate.startTime) ?: return TimetableConflict("Start time is invalid.")
    val candidateEnd = parseTimeToMinutes(candidate.endTime) ?: return TimetableConflict("End time is invalid.")
    if (candidateEnd <= candidateStart) {
        return TimetableConflict("End time must be after start time.")
    }

    val daySlots = timetable[targetDay].orEmpty()
        .filterNot { ignoreSlot != null && it == ignoreSlot }

    val overlaps = daySlots.filter { slot ->
        val slotStart = parseTimeToMinutes(slot.startTime) ?: return@filter false
        val slotEnd = parseTimeToMinutes(slot.endTime) ?: return@filter false
        candidateStart < slotEnd && candidateEnd > slotStart
    }

    if (overlaps.isNotEmpty()) {
        return TimetableConflict(
            message = buildString {
                append("Overlaps with ")
                append(overlaps.joinToString { "${it.subject} (${it.startTime}-${it.endTime})" })
            },
            overlappingSlots = overlaps
        )
    }

    val roomConflict = candidate.room.isNotBlank() && daySlots.any { slot ->
        slot.room.equals(candidate.room, ignoreCase = true) &&
            slot.startTime == candidate.startTime &&
            slot.endTime == candidate.endTime
    }
    if (roomConflict) {
        return TimetableConflict("Another class already uses room ${candidate.room} at the same time.")
    }

    return null
}

private fun parseTimeToMinutes(value: String): Int? {
    val parts = value.trim().split(":")
    if (parts.size != 2) return null
    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null
    if (hour !in 0..23 || minute !in 0..59) return null
    return hour * 60 + minute
}
