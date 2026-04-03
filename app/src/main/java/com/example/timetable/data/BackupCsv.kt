package com.example.timetable.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val backupJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

@Serializable
data class BackupPreferences(
    val darkMode: Boolean = true,
    val showExamCountdown: Boolean = true,
    val showMessMenuCard: Boolean = true,
    val remindersEnabled: Boolean = false,
    val messPref: String = "veg",
    val reminderMinutesBefore: Long = 10L,
    val themeColor: Long = 0xFFFF3D00,
    val minimalistMode: Boolean = false,
    val compactMode: Boolean = false,
    val dndMode: Boolean = false,
    val userName: String = ""
)

@Serializable
data class AppBackupSnapshot(
    val timetable: Map<AppDay, List<ClassSlot>> = DefaultTimetable,
    val messMenu: Map<AppFullDay, List<AppMeal>> = DefaultMessMenu,
    val assignments: List<AssignmentItem> = emptyList(),
    val attendance: Map<String, AttendanceStats> = emptyMap(),
    val dailyAbsentLogs: Map<String, Set<String>> = emptyMap(),
    val dailyPresentLogs: Map<String, Set<String>> = emptyMap(),
    val canceledClasses: Map<String, Set<String>> = emptyMap(),
    val examDateMillis: Long? = null,
    val examSubject: String? = null,
    val examTime: String? = null,
    val examPlan: ExamPlan = ExamPlan(),
    val semesterStartDateMillis: Long,
    val semesterEndDateMillis: Long,
    val rescheduledClasses: Map<String, ClassSlot> = emptyMap(),
    val classNotes: Map<String, String> = emptyMap(),
    val syllabus: Map<String, List<SyllabusItem>> = emptyMap(),
    val syllabusPdfs: Map<String, String> = emptyMap(),
    val preferences: BackupPreferences = BackupPreferences()
)

private data class CsvRow(
    val section: String,
    val key: String,
    val value: String
)

fun exportAnalyticsCsv(attendance: Map<String, AttendanceStats>): String {
    val rows = buildList {
        add(listOf("subject", "present", "absent", "total", "percentage"))
        attendance.toSortedMap(String.CASE_INSENSITIVE_ORDER).forEach { (subject, stats) ->
            val total = stats.total
            val percentage = if (total == 0) 0.0 else (stats.present.toDouble() / total.toDouble()) * 100.0
            add(
                listOf(
                    subject,
                    stats.present.toString(),
                    stats.absent.toString(),
                    total.toString(),
                    "%.2f".format(java.util.Locale.US, percentage)
                )
            )
        }
    }
    return rows.joinToString(separator = "\n") { row -> row.joinToString(separator = ",", transform = ::csvCell) }
}

fun exportFullBackupCsv(snapshot: AppBackupSnapshot): String {
    val rows = listOf(
        CsvRow("meta", "format", "timetable_backup_csv_v1"),
        CsvRow("meta", "exported_at", System.currentTimeMillis().toString()),
        CsvRow("timetable", "json", backupJson.encodeToString(snapshot.timetable)),
        CsvRow("mess_menu", "json", backupJson.encodeToString(snapshot.messMenu)),
        CsvRow("assignments", "json", backupJson.encodeToString(snapshot.assignments)),
        CsvRow("attendance", "json", backupJson.encodeToString(snapshot.attendance)),
        CsvRow("daily_absent_logs", "json", backupJson.encodeToString(snapshot.dailyAbsentLogs)),
        CsvRow("daily_present_logs", "json", backupJson.encodeToString(snapshot.dailyPresentLogs)),
        CsvRow("canceled_classes", "json", backupJson.encodeToString(snapshot.canceledClasses)),
        CsvRow("exam_legacy", "date_millis", snapshot.examDateMillis?.toString().orEmpty()),
        CsvRow("exam_legacy", "subject", snapshot.examSubject.orEmpty()),
        CsvRow("exam_legacy", "time", snapshot.examTime.orEmpty()),
        CsvRow("exam_plan", "json", backupJson.encodeToString(snapshot.examPlan)),
        CsvRow("semester", "start_millis", snapshot.semesterStartDateMillis.toString()),
        CsvRow("semester", "end_millis", snapshot.semesterEndDateMillis.toString()),
        CsvRow("rescheduled_classes", "json", backupJson.encodeToString(snapshot.rescheduledClasses)),
        CsvRow("class_notes", "json", backupJson.encodeToString(snapshot.classNotes)),
        CsvRow("syllabus", "json", backupJson.encodeToString(snapshot.syllabus)),
        CsvRow("syllabus_pdfs", "json", backupJson.encodeToString(snapshot.syllabusPdfs)),
        CsvRow("preferences", "json", backupJson.encodeToString(snapshot.preferences))
    )

    return buildString {
        appendLine("section,key,value")
        rows.forEach { row ->
            append(csvCell(row.section))
            append(',')
            append(csvCell(row.key))
            append(',')
            append(csvCell(row.value))
            appendLine()
        }
    }.trimEnd()
}

fun importFullBackupCsv(csv: String): AppBackupSnapshot {
    val sanitizedCsv = csv
        .removePrefix("\uFEFF")
        .trim()
    val rows = parseCsv(sanitizedCsv)
    require(rows.isNotEmpty()) { "Backup file is empty." }
    val header = rows.first().map { it.trim().removePrefix("\uFEFF") }
    require(header.size >= 3 && header[0] == "section" && header[1] == "key" && header[2] == "value") {
        "Invalid backup header."
    }

    val dataRows = rows.drop(1)
        .filter { it.size >= 3 }
        .map {
            CsvRow(
                section = it[0].trim(),
                key = it[1].trim(),
                value = it.subList(2, it.size).joinToString(",")
            )
        }

    val format = dataRows.firstOrNull { it.section == "meta" && it.key == "format" }?.value
    require(format == null || format == "timetable_backup_csv_v1") { "Unsupported backup format." }

    fun valueOf(section: String, key: String): String? =
        dataRows.lastOrNull { it.section == section && it.key == key }?.value

    fun <T> decodeOrDefault(raw: String?, default: T, decode: (String) -> T): T {
        if (raw.isNullOrBlank()) return default
        return runCatching { decode(raw) }.getOrDefault(default)
    }

    return AppBackupSnapshot(
        timetable = decodeOrDefault(valueOf("timetable", "json"), DefaultTimetable) {
            backupJson.decodeFromString(it)
        },
        messMenu = decodeOrDefault(valueOf("mess_menu", "json"), DefaultMessMenu) {
            backupJson.decodeFromString(it)
        },
        assignments = decodeOrDefault(valueOf("assignments", "json"), emptyList()) {
            backupJson.decodeFromString(it)
        },
        attendance = decodeOrDefault(valueOf("attendance", "json"), emptyMap()) {
            backupJson.decodeFromString(it)
        },
        dailyAbsentLogs = decodeOrDefault(valueOf("daily_absent_logs", "json"), emptyMap()) {
            backupJson.decodeFromString(it)
        },
        dailyPresentLogs = decodeOrDefault(valueOf("daily_present_logs", "json"), emptyMap()) {
            backupJson.decodeFromString(it)
        },
        canceledClasses = decodeOrDefault(valueOf("canceled_classes", "json"), emptyMap()) {
            backupJson.decodeFromString(it)
        },
        examDateMillis = valueOf("exam_legacy", "date_millis")?.toLongOrNull(),
        examSubject = valueOf("exam_legacy", "subject")?.takeIf { it.isNotBlank() },
        examTime = valueOf("exam_legacy", "time")?.takeIf { it.isNotBlank() },
        examPlan = decodeOrDefault(valueOf("exam_plan", "json"), ExamPlan()) {
            backupJson.decodeFromString(it)
        },
        semesterStartDateMillis = valueOf("semester", "start_millis")?.toLongOrNull()
            ?: run {
                val cal = java.util.Calendar.getInstance().apply {
                    set(java.util.Calendar.YEAR, 2026)
                    set(java.util.Calendar.MONTH, java.util.Calendar.FEBRUARY)
                    set(java.util.Calendar.DAY_OF_MONTH, 10)
                    set(java.util.Calendar.HOUR_OF_DAY, 0)
                    set(java.util.Calendar.MINUTE, 0)
                    set(java.util.Calendar.SECOND, 0)
                    set(java.util.Calendar.MILLISECOND, 0)
                }
                cal.timeInMillis
            },
        semesterEndDateMillis = valueOf("semester", "end_millis")?.toLongOrNull()
            ?: run {
                val cal = java.util.Calendar.getInstance().apply {
                    set(java.util.Calendar.YEAR, 2026)
                    set(java.util.Calendar.MONTH, java.util.Calendar.JULY)
                    set(java.util.Calendar.DAY_OF_MONTH, 10)
                    set(java.util.Calendar.HOUR_OF_DAY, 0)
                    set(java.util.Calendar.MINUTE, 0)
                    set(java.util.Calendar.SECOND, 0)
                    set(java.util.Calendar.MILLISECOND, 0)
                }
                cal.timeInMillis
            },
        rescheduledClasses = decodeOrDefault(valueOf("rescheduled_classes", "json"), emptyMap()) {
            backupJson.decodeFromString(it)
        },
        classNotes = decodeOrDefault(valueOf("class_notes", "json"), emptyMap()) {
            backupJson.decodeFromString(it)
        },
        syllabus = decodeOrDefault(valueOf("syllabus", "json"), emptyMap()) {
            backupJson.decodeFromString(it)
        },
        syllabusPdfs = decodeOrDefault(valueOf("syllabus_pdfs", "json"), emptyMap()) {
            backupJson.decodeFromString(it)
        },
        preferences = decodeOrDefault(valueOf("preferences", "json"), BackupPreferences()) {
            backupJson.decodeFromString(it)
        }
    )
}

private fun csvCell(value: String): String {
    val escaped = value.replace("\"", "\"\"")
    return if (escaped.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
        "\"$escaped\""
    } else {
        escaped
    }
}

private fun parseCsv(input: String): List<List<String>> {
    val rows = mutableListOf<List<String>>()
    val row = mutableListOf<String>()
    val cell = StringBuilder()
    var inQuotes = false
    var i = 0

    while (i < input.length) {
        val ch = input[i]
        when {
            inQuotes && ch == '"' && i + 1 < input.length && input[i + 1] == '"' -> {
                cell.append('"')
                i += 1
            }
            ch == '"' -> inQuotes = !inQuotes
            !inQuotes && ch == ',' -> {
                row += cell.toString()
                cell.setLength(0)
            }
            !inQuotes && ch == '\n' -> {
                row += cell.toString()
                rows += row.toList()
                row.clear()
                cell.setLength(0)
            }
            !inQuotes && ch == '\r' -> Unit
            else -> cell.append(ch)
        }
        i += 1
    }

    if (cell.isNotEmpty() || row.isNotEmpty()) {
        row += cell.toString()
        rows += row.toList()
    }

    return rows.filterNot { it.size == 1 && it[0].isBlank() }
}
