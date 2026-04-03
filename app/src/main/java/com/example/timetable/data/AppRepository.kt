package com.example.timetable.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import com.example.timetable.notifications.NotificationScheduler
import com.example.timetable.widget.NextClassWidget
import java.text.SimpleDateFormat
import java.util.*

class AppRepository(private val context: Context) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    private val attendanceMutex = Mutex()
    private val notificationsMutex = Mutex()
    private val savedNotificationsMaxAgeMillis: Long = 7L * 24L * 60L * 60L * 1000L

    private companion object {
        // Semester window requested: 10 Feb 2026 -> 10 Jul 2026 (device local time).
        fun localDateMillis(year: Int, month: Int, day: Int): Long {
            return Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, day)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }

        val DefaultSemesterStartMillis: Long = localDateMillis(2026, Calendar.FEBRUARY, 10)
        val DefaultSemesterEndMillis: Long = localDateMillis(2026, Calendar.JULY, 10)
    }

    private object Keys {
        val TimetableJson = stringPreferencesKey("timetable_json")
        val MessMenuJson = stringPreferencesKey("mess_menu_json")
        val AssignmentsJson = stringPreferencesKey("assignments_json")
        val AttendanceJson = stringPreferencesKey("attendance_json")
        val DailyLogsJson = stringPreferencesKey("daily_logs_json") // Map<dateStr, Set<absentSlotKey>>
        val DailyPresentLogsJson = stringPreferencesKey("daily_present_logs_json") // Map<dateStr, Set<presentSlotKey>>
        val CanceledClassesJson = stringPreferencesKey("canceled_classes_json") // Map<dateStr, Set<slotKey>>
        val ExamDateMillis = longPreferencesKey("exam_date_millis")
        val ExamSubject = stringPreferencesKey("exam_subject")
        val ExamTime = stringPreferencesKey("exam_time")
        val ExamPlanJson = stringPreferencesKey("exam_plan_json")
        val SemesterStartDateMillis = longPreferencesKey("semester_start_date_millis")
        val SemesterEndDateMillis = longPreferencesKey("semester_end_date_millis")
        val RescheduledClassesJson = stringPreferencesKey("rescheduled_classes_json")
        val ClassNotesJson = stringPreferencesKey("class_notes_json")
        val SyllabusJson = stringPreferencesKey("syllabus_json")
        val SyllabusPdfsJson = stringPreferencesKey("syllabus_pdfs_json")
        val DarkMode = booleanPreferencesKey("dark_mode")
        val ShowExamCountdown = booleanPreferencesKey("show_exam_countdown")
        val ShowMessMenuCard = booleanPreferencesKey("show_mess_menu_card")
        val RemindersEnabled = booleanPreferencesKey("reminders_enabled")
        val MessPref = stringPreferencesKey("mess_pref")
        val ReminderMinutesBefore = longPreferencesKey("reminder_minutes_before")
        val ThemeColor = longPreferencesKey("theme_color")
        val SavedNotificationsJson = stringPreferencesKey("saved_notifications_json")
        val UserName = stringPreferencesKey("user_name")
        val MinimalistMode = booleanPreferencesKey("minimalist_mode")
        val CompactMode = booleanPreferencesKey("compact_mode")
        val DndMode = booleanPreferencesKey("dnd_mode")
        val UpdateManifestUrl = stringPreferencesKey("update_manifest_url")
        val PendingUpdateDownloadId = longPreferencesKey("pending_update_download_id")
        val UiRefreshDefaultsApplied = booleanPreferencesKey("ui_refresh_defaults_applied")
        val VersionFiveDefaultsApplied = booleanPreferencesKey("version_five_defaults_applied")
    }

    val timetable: Flow<Map<AppDay, List<ClassSlot>>> =
        context.appDataStore.data.map { prefs ->
            val raw = prefs[Keys.TimetableJson]
            if (raw.isNullOrBlank()) {
                DefaultTimetable
            } else {
                runCatching { json.decodeFromString<Map<AppDay, List<ClassSlot>>>(raw) }
                    .getOrDefault(DefaultTimetable)
            }
        }

    val messMenu: Flow<Map<AppFullDay, List<AppMeal>>> =
        context.appDataStore.data.map { prefs ->
            val raw = prefs[Keys.MessMenuJson]
            if (raw.isNullOrBlank()) {
                DefaultMessMenu
            } else {
                runCatching { json.decodeFromString<Map<AppFullDay, List<AppMeal>>>(raw) }
                    .getOrDefault(DefaultMessMenu)
            }
        }

    val assignments: Flow<List<AssignmentItem>> =
        context.appDataStore.data.map { prefs ->
            val raw = prefs[Keys.AssignmentsJson]
            if (raw.isNullOrBlank()) {
                emptyList()
            } else {
                runCatching { json.decodeFromString<List<AssignmentItem>>(raw) }
                    .getOrDefault(emptyList())
                    .sortedBy { it.dueAtMillis }
            }
        }

    val examDateMillis: Flow<Long?> =
        context.appDataStore.data.map { prefs -> prefs[Keys.ExamDateMillis] }

    val examSubject: Flow<String?> =
        context.appDataStore.data.map { prefs -> prefs[Keys.ExamSubject] }

    val examTime: Flow<String?> =
        context.appDataStore.data.map { prefs -> prefs[Keys.ExamTime] }

    val examPlan: Flow<ExamPlan> =
        context.appDataStore.data.map { prefs ->
            val raw = prefs[Keys.ExamPlanJson]
            if (raw.isNullOrBlank()) {
                ExamPlan()
            } else {
                runCatching { json.decodeFromString<ExamPlan>(raw) }.getOrDefault(ExamPlan())
            }
        }

    val semesterStartDateMillis: Flow<Long> =
        context.appDataStore.data.map { prefs -> prefs[Keys.SemesterStartDateMillis] ?: DefaultSemesterStartMillis }

    val semesterEndDateMillis: Flow<Long> =
        context.appDataStore.data.map { prefs -> prefs[Keys.SemesterEndDateMillis] ?: DefaultSemesterEndMillis }

    val rescheduledClasses: Flow<Map<String, ClassSlot>> =
        context.appDataStore.data.map { prefs ->
            val raw = prefs[Keys.RescheduledClassesJson]
            if (raw.isNullOrBlank()) emptyMap()
            else runCatching { json.decodeFromString<Map<String, ClassSlot>>(raw) }.getOrDefault(emptyMap())
        }

    val syllabus: Flow<Map<String, List<SyllabusItem>>> =
        context.appDataStore.data.map { prefs ->
            val raw = prefs[Keys.SyllabusJson]
            if (raw.isNullOrBlank()) emptyMap()
            else runCatching { json.decodeFromString<Map<String, List<SyllabusItem>>>(raw) }.getOrDefault(emptyMap())
        }

    val classNotes: Flow<Map<String, String>> =
        context.appDataStore.data.map { prefs ->
            val raw = prefs[Keys.ClassNotesJson]
            if (raw.isNullOrBlank()) emptyMap()
            else runCatching { json.decodeFromString<Map<String, String>>(raw) }.getOrDefault(emptyMap())
        }

    val savedNotifications: Flow<List<SavedNotification>> =
        context.appDataStore.data.map { prefs ->
            val raw = prefs[Keys.SavedNotificationsJson]
            if (raw.isNullOrBlank()) emptyList()
            else {
                val decoded = runCatching { json.decodeFromString<List<SavedNotification>>(raw) }.getOrDefault(emptyList())
                val cutoff = System.currentTimeMillis() - savedNotificationsMaxAgeMillis
                decoded.filter { it.receivedAtMillis >= cutoff }
            }
        }

    val syllabusPdfs: Flow<Map<String, String>> =
        context.appDataStore.data.map { prefs ->
            val raw = prefs[Keys.SyllabusPdfsJson]
            if (raw.isNullOrBlank()) emptyMap()
            else runCatching { json.decodeFromString<Map<String, String>>(raw) }.getOrDefault(emptyMap())
        }

    val darkMode: Flow<Boolean> =
        context.appDataStore.data.map { prefs -> prefs[Keys.DarkMode] ?: false }

    val showExamCountdown: Flow<Boolean> =
        context.appDataStore.data.map { prefs -> prefs[Keys.ShowExamCountdown] ?: true }

    val showMessMenuCard: Flow<Boolean> =
        context.appDataStore.data.map { prefs -> prefs[Keys.ShowMessMenuCard] ?: true }

    val remindersEnabled: Flow<Boolean> =
        context.appDataStore.data.map { prefs -> prefs[Keys.RemindersEnabled] ?: false }

    val messPref: Flow<String> =
        context.appDataStore.data.map { prefs -> prefs[Keys.MessPref] ?: "veg" }

    val reminderMinutesBefore: Flow<Long> =
        context.appDataStore.data.map { prefs -> prefs[Keys.ReminderMinutesBefore] ?: 10L }

    val themeColor: Flow<Long> =
        context.appDataStore.data.map { prefs -> prefs[Keys.ThemeColor] ?: 0xFF2563EB } // Default Blue

    val updateManifestUrl: Flow<String> =
        context.appDataStore.data.map { prefs -> prefs[Keys.UpdateManifestUrl].orEmpty() }

    val pendingUpdateDownloadId: Flow<Long?> =
        context.appDataStore.data.map { prefs -> prefs[Keys.PendingUpdateDownloadId] }

    val userName: Flow<String> =
        context.appDataStore.data.map { prefs -> prefs[Keys.UserName].orEmpty() }

    val minimalistMode: Flow<Boolean> =
        context.appDataStore.data.map { prefs -> prefs[Keys.MinimalistMode] ?: false }

    val compactMode: Flow<Boolean> =
        context.appDataStore.data.map { prefs -> prefs[Keys.CompactMode] ?: true }

    val dndMode: Flow<Boolean> =
        context.appDataStore.data.map { prefs -> prefs[Keys.DndMode] ?: false }

    val attendance: Flow<Map<String, AttendanceStats>> =
        context.appDataStore.data.map { prefs ->
            val raw = prefs[Keys.AttendanceJson]
            if (raw.isNullOrBlank()) {
                emptyMap()
            } else {
                runCatching { json.decodeFromString<Map<String, AttendanceStats>>(raw) }
                    .getOrDefault(emptyMap())
            }
        }

    /** Map<dateString "YYYY-MM-DD", Set<"subject:startTime" slots explicitly marked absent>> */
    val dailyAbsentLogs: Flow<Map<String, Set<String>>> =
        context.appDataStore.data.map { prefs ->
            val raw = prefs[Keys.DailyLogsJson]
            if (raw.isNullOrBlank()) emptyMap()
            else runCatching { json.decodeFromString<Map<String, Set<String>>>(raw) }.getOrDefault(emptyMap())
        }

    /** Map<dateString "YYYY-MM-DD", Set<"subject:startTime" slots explicitly marked present>> */
    val dailyPresentLogs: Flow<Map<String, Set<String>>> =
        context.appDataStore.data.map { prefs ->
            val raw = prefs[Keys.DailyPresentLogsJson]
            if (raw.isNullOrBlank()) emptyMap()
            else runCatching { json.decodeFromString<Map<String, Set<String>>>(raw) }.getOrDefault(emptyMap())
        }

    /** Map<dateString "YYYY-MM-DD", Set<"subject:startTime" slots marked canceled>> */
    val canceledClasses: Flow<Map<String, Set<String>>> =
        context.appDataStore.data.map { prefs ->
            val raw = prefs[Keys.CanceledClassesJson]
            if (raw.isNullOrBlank()) emptyMap()
            else runCatching { json.decodeFromString<Map<String, Set<String>>>(raw) }.getOrDefault(emptyMap())
        }

    suspend fun setTimetable(value: Map<AppDay, List<ClassSlot>>) {
        context.appDataStore.edit { prefs ->
            prefs[Keys.TimetableJson] = json.encodeToString(value)
        }
        NotificationScheduler.rescheduleAll(context)
        NextClassWidget.refreshAll(context)
    }

    suspend fun resetTimetable() {
        setTimetable(DefaultTimetable)
    }

    suspend fun setMessMenu(value: Map<AppFullDay, List<AppMeal>>) {
        context.appDataStore.edit { prefs ->
            prefs[Keys.MessMenuJson] = json.encodeToString(value)
        }
        NextClassWidget.refreshAll(context)
    }

    suspend fun resetMessMenu() {
        setMessMenu(DefaultMessMenu)
    }

    suspend fun upsertAssignment(item: AssignmentItem) {
        val normalized = item.copy(
            title = item.title.trim(),
            subject = item.subject.trim(),
            notes = item.notes.trim()
        )
        context.appDataStore.edit { prefs ->
            val raw = prefs[Keys.AssignmentsJson]
            val current = if (raw.isNullOrBlank()) mutableListOf<AssignmentItem>()
            else runCatching { json.decodeFromString<List<AssignmentItem>>(raw).toMutableList() }.getOrDefault(mutableListOf())
            val existingIndex = current.indexOfFirst { it.id == normalized.id }
            if (existingIndex >= 0) current[existingIndex] = normalized else current.add(normalized)
            prefs[Keys.AssignmentsJson] = json.encodeToString(current.sortedBy { it.dueAtMillis })
        }
        NotificationScheduler.rescheduleAssignmentAlarms(context, normalized)
    }

    suspend fun deleteAssignment(id: String) {
        NotificationScheduler.cancelAssignmentAlarms(context, id)
        context.appDataStore.edit { prefs ->
            val raw = prefs[Keys.AssignmentsJson]
            val current = if (raw.isNullOrBlank()) mutableListOf<AssignmentItem>()
            else runCatching { json.decodeFromString<List<AssignmentItem>>(raw).toMutableList() }.getOrDefault(mutableListOf())

            current.removeAll { it.id == id }
            prefs[Keys.AssignmentsJson] = json.encodeToString(current.sortedBy { it.dueAtMillis })
        }
    }

    suspend fun setExamDetails(dateMillis: Long?, subject: String?, time: String?) {
        context.appDataStore.edit { prefs ->
            if (dateMillis == null) prefs.remove(Keys.ExamDateMillis) else prefs[Keys.ExamDateMillis] = dateMillis
            if (subject == null) prefs.remove(Keys.ExamSubject) else prefs[Keys.ExamSubject] = subject
            if (time == null) prefs.remove(Keys.ExamTime) else prefs[Keys.ExamTime] = time
        }
        NotificationScheduler.rescheduleAll(context)
    }

    suspend fun setExamPlan(plan: ExamPlan) {
        val normalizedStart = plan.startDateMillis?.let(::normalizeToStartOfDay)
        val normalizedEnd = plan.endDateMillis?.let(::normalizeToStartOfDay)
        val normalizedSubjectDates = plan.subjectExamDates.mapValues { normalizeToStartOfDay(it.value) }
        val normalizedPlan = plan.copy(
            startDateMillis = normalizedStart,
            endDateMillis = normalizedEnd,
            subjectExamDates = normalizedSubjectDates
        )

        val nowStart = normalizeToStartOfDay(System.currentTimeMillis())
        val nextExam = normalizedSubjectDates
            .filterValues { it >= nowStart }
            .minByOrNull { it.value }

        context.appDataStore.edit { prefs ->
            prefs[Keys.ExamPlanJson] = json.encodeToString(normalizedPlan)
            if (nextExam == null) {
                prefs.remove(Keys.ExamDateMillis)
                prefs.remove(Keys.ExamSubject)
                prefs.remove(Keys.ExamTime)
            } else {
                prefs[Keys.ExamDateMillis] = nextExam.value
                prefs[Keys.ExamSubject] = nextExam.key
                prefs[Keys.ExamTime] = "${normalizedPlan.commonStartTime} - ${normalizedPlan.commonEndTime}"
            }
        }
        NotificationScheduler.rescheduleAll(context)
        NextClassWidget.refreshAll(context)
    }

    suspend fun setSemesterStartDate(millis: Long) {
        context.appDataStore.edit { prefs -> prefs[Keys.SemesterStartDateMillis] = millis }
    }

    suspend fun setSemesterEndDate(millis: Long) {
        context.appDataStore.edit { prefs -> prefs[Keys.SemesterEndDateMillis] = millis }
    }

    suspend fun setRescheduledClass(dateKey: String, slotKey: String, newSlot: ClassSlot) {
        val raw = context.appDataStore.data.first()[Keys.RescheduledClassesJson]
        val map = if (raw.isNullOrBlank()) mutableMapOf()
        else runCatching { json.decodeFromString<Map<String, ClassSlot>>(raw).toMutableMap() }.getOrDefault(mutableMapOf())
        
        map["${dateKey}_${slotKey}"] = newSlot
        
        context.appDataStore.edit { prefs ->
            prefs[Keys.RescheduledClassesJson] = json.encodeToString(map)
        }
    }

    suspend fun saveSyllabus(subject: String, items: List<SyllabusItem>) {
        val raw = context.appDataStore.data.first()[Keys.SyllabusJson]
        val map = if (raw.isNullOrBlank()) mutableMapOf()
        else runCatching { json.decodeFromString<Map<String, List<SyllabusItem>>>(raw).toMutableMap() }.getOrDefault(mutableMapOf())

        map[subject] = items

        context.appDataStore.edit { prefs ->
            prefs[Keys.SyllabusJson] = json.encodeToString(map)
        }
    }

    suspend fun setClassNote(noteKey: String, note: String?) {
        context.appDataStore.edit { prefs ->
            val raw = prefs[Keys.ClassNotesJson]
            val map = if (raw.isNullOrBlank()) mutableMapOf<String, String>()
            else runCatching { json.decodeFromString<Map<String, String>>(raw).toMutableMap() }.getOrDefault(mutableMapOf())

            val trimmed = note?.trim().orEmpty()
            if (trimmed.isEmpty()) map.remove(noteKey) else map[noteKey] = trimmed
            prefs[Keys.ClassNotesJson] = json.encodeToString(map)
        }
    }

    /**
     * Persists incoming cloud notifications so the user can view them later.
     * Kept small (trimmed) to prevent DataStore from growing forever.
     */
    suspend fun addSavedNotification(item: SavedNotification) {
        notificationsMutex.withLock {
            context.appDataStore.edit { prefs ->
                val raw = prefs[Keys.SavedNotificationsJson]
                val current = if (raw.isNullOrBlank()) emptyList()
                else runCatching { json.decodeFromString<List<SavedNotification>>(raw) }.getOrDefault(emptyList())

                val cutoff = System.currentTimeMillis() - savedNotificationsMaxAgeMillis
                val currentRecent = current.filter { it.receivedAtMillis >= cutoff }

                // Put newest first, de-duplicate by id, and cap the list.
                val updated = (listOf(item) + currentRecent)
                    .distinctBy { it.id }
                    .take(100)

                prefs[Keys.SavedNotificationsJson] = json.encodeToString(updated)
            }
        }
    }

    /** Deletes saved notifications older than the retention window. */
    suspend fun pruneSavedNotifications() {
        notificationsMutex.withLock {
            val cutoff = System.currentTimeMillis() - savedNotificationsMaxAgeMillis
            context.appDataStore.edit { prefs ->
                val raw = prefs[Keys.SavedNotificationsJson]
                if (raw.isNullOrBlank()) return@edit

                val decoded = runCatching { json.decodeFromString<List<SavedNotification>>(raw) }.getOrDefault(emptyList())
                val filtered = decoded.filter { it.receivedAtMillis >= cutoff }
                prefs[Keys.SavedNotificationsJson] = json.encodeToString(filtered)
            }
        }
    }

    suspend fun setSyllabusPdf(subject: String, uriString: String?) {
        val raw = context.appDataStore.data.first()[Keys.SyllabusPdfsJson]
        val map = if (raw.isNullOrBlank()) mutableMapOf()
        else runCatching { json.decodeFromString<Map<String, String>>(raw).toMutableMap() }.getOrDefault(mutableMapOf())

        if (uriString.isNullOrBlank()) map.remove(subject) else map[subject] = uriString

        context.appDataStore.edit { prefs ->
            prefs[Keys.SyllabusPdfsJson] = json.encodeToString(map)
        }
    }

    suspend fun setDarkMode(value: Boolean) {
        context.appDataStore.edit { prefs -> prefs[Keys.DarkMode] = value }
    }

    suspend fun applyUiRefreshDefaultsIfNeeded() {
        context.appDataStore.edit { prefs ->
            if (prefs[Keys.UiRefreshDefaultsApplied] == true) return@edit
            prefs[Keys.DarkMode] = false
            prefs[Keys.UiRefreshDefaultsApplied] = true
        }
    }

    suspend fun applyVersionFiveDefaultsIfNeeded() {
        context.appDataStore.edit { prefs ->
            if (prefs[Keys.VersionFiveDefaultsApplied] == true) return@edit
            prefs[Keys.CompactMode] = true
            prefs[Keys.ThemeColor] = 0xFF2563EB
            prefs[Keys.VersionFiveDefaultsApplied] = true
        }
    }

    suspend fun setShowExamCountdown(value: Boolean) {
        context.appDataStore.edit { prefs -> prefs[Keys.ShowExamCountdown] = value }
    }

    suspend fun setShowMessMenuCard(value: Boolean) {
        context.appDataStore.edit { prefs -> prefs[Keys.ShowMessMenuCard] = value }
    }

    suspend fun setRemindersEnabled(value: Boolean) {
        context.appDataStore.edit { prefs -> prefs[Keys.RemindersEnabled] = value }
        NotificationScheduler.rescheduleAll(context)
    }

    suspend fun setMessPref(value: String) {
        context.appDataStore.edit { prefs -> prefs[Keys.MessPref] = value }
    }

    suspend fun setReminderMinutesBefore(value: Long) {
        context.appDataStore.edit { prefs -> prefs[Keys.ReminderMinutesBefore] = value }
        NotificationScheduler.rescheduleAll(context)
    }

    suspend fun setThemeColor(value: Long) {
        context.appDataStore.edit { prefs -> prefs[Keys.ThemeColor] = value }
    }

    suspend fun setUpdateManifestUrl(value: String) {
        val normalized = value.trim()
        context.appDataStore.edit { prefs ->
            if (normalized.isBlank()) prefs.remove(Keys.UpdateManifestUrl)
            else prefs[Keys.UpdateManifestUrl] = normalized
        }
    }

    suspend fun setPendingUpdateDownloadId(value: Long?) {
        context.appDataStore.edit { prefs ->
            if (value == null) prefs.remove(Keys.PendingUpdateDownloadId)
            else prefs[Keys.PendingUpdateDownloadId] = value
        }
    }

    suspend fun setUserName(value: String) {
        val normalized = value.trim()
        context.appDataStore.edit { prefs ->
            if (normalized.isBlank()) prefs.remove(Keys.UserName)
            else prefs[Keys.UserName] = normalized
        }
    }

    suspend fun setMinimalistMode(value: Boolean) {
        context.appDataStore.edit { prefs -> prefs[Keys.MinimalistMode] = value }
    }

    suspend fun setCompactMode(value: Boolean) {
        context.appDataStore.edit { prefs -> prefs[Keys.CompactMode] = value }
    }

    suspend fun setDndMode(value: Boolean) {
        context.appDataStore.edit { prefs -> prefs[Keys.DndMode] = value }
    }

    suspend fun createBackupSnapshot(): AppBackupSnapshot {
        val prefs = context.appDataStore.data.first()
        return AppBackupSnapshot(
            timetable = prefs[Keys.TimetableJson]
                ?.takeIf { it.isNotBlank() }
                ?.let { runCatching { json.decodeFromString<Map<AppDay, List<ClassSlot>>>(it) }.getOrNull() }
                ?: DefaultTimetable,
            messMenu = prefs[Keys.MessMenuJson]
                ?.takeIf { it.isNotBlank() }
                ?.let { runCatching { json.decodeFromString<Map<AppFullDay, List<AppMeal>>>(it) }.getOrNull() }
                ?: DefaultMessMenu,
            assignments = prefs[Keys.AssignmentsJson]
                ?.takeIf { it.isNotBlank() }
                ?.let { runCatching { json.decodeFromString<List<AssignmentItem>>(it) }.getOrNull() }
                ?: emptyList(),
            attendance = decodeAttendanceMap(prefs[Keys.AttendanceJson]),
            dailyAbsentLogs = decodeSlotLogMap(prefs[Keys.DailyLogsJson]),
            dailyPresentLogs = decodeSlotLogMap(prefs[Keys.DailyPresentLogsJson]),
            canceledClasses = decodeSlotLogMap(prefs[Keys.CanceledClassesJson]),
            examDateMillis = prefs[Keys.ExamDateMillis],
            examSubject = prefs[Keys.ExamSubject],
            examTime = prefs[Keys.ExamTime],
            examPlan = prefs[Keys.ExamPlanJson]
                ?.takeIf { it.isNotBlank() }
                ?.let { runCatching { json.decodeFromString<ExamPlan>(it) }.getOrNull() }
                ?: ExamPlan(),
            semesterStartDateMillis = prefs[Keys.SemesterStartDateMillis] ?: DefaultSemesterStartMillis,
            semesterEndDateMillis = prefs[Keys.SemesterEndDateMillis] ?: DefaultSemesterEndMillis,
            rescheduledClasses = prefs[Keys.RescheduledClassesJson]
                ?.takeIf { it.isNotBlank() }
                ?.let { runCatching { json.decodeFromString<Map<String, ClassSlot>>(it) }.getOrNull() }
                ?: emptyMap(),
            classNotes = prefs[Keys.ClassNotesJson]
                ?.takeIf { it.isNotBlank() }
                ?.let { runCatching { json.decodeFromString<Map<String, String>>(it) }.getOrNull() }
                ?: emptyMap(),
            syllabus = prefs[Keys.SyllabusJson]
                ?.takeIf { it.isNotBlank() }
                ?.let { runCatching { json.decodeFromString<Map<String, List<SyllabusItem>>>(it) }.getOrNull() }
                ?: emptyMap(),
                syllabusPdfs = prefs[Keys.SyllabusPdfsJson]
                ?.takeIf { it.isNotBlank() }
                ?.let { runCatching { json.decodeFromString<Map<String, String>>(it) }.getOrNull() }
                ?: emptyMap(),
            preferences = BackupPreferences(
                darkMode = prefs[Keys.DarkMode] ?: false,
                showExamCountdown = prefs[Keys.ShowExamCountdown] ?: true,
                showMessMenuCard = prefs[Keys.ShowMessMenuCard] ?: true,
                remindersEnabled = prefs[Keys.RemindersEnabled] ?: false,
                messPref = prefs[Keys.MessPref] ?: "veg",
                reminderMinutesBefore = prefs[Keys.ReminderMinutesBefore] ?: 10L,
                themeColor = prefs[Keys.ThemeColor] ?: 0xFF2563EB,
                minimalistMode = prefs[Keys.MinimalistMode] ?: false,
                compactMode = prefs[Keys.CompactMode] ?: true,
                dndMode = prefs[Keys.DndMode] ?: false,
                userName = prefs[Keys.UserName].orEmpty()
            )
        )
    }

    suspend fun restoreBackupSnapshot(snapshot: AppBackupSnapshot) {
        context.appDataStore.edit { prefs ->
            prefs[Keys.TimetableJson] = json.encodeToString(snapshot.timetable)
            prefs[Keys.MessMenuJson] = json.encodeToString(snapshot.messMenu)
            prefs[Keys.AssignmentsJson] = json.encodeToString(snapshot.assignments.sortedBy { it.dueAtMillis })
            prefs[Keys.AttendanceJson] = json.encodeToString(snapshot.attendance)
            prefs[Keys.DailyLogsJson] = json.encodeToString(snapshot.dailyAbsentLogs)
            prefs[Keys.DailyPresentLogsJson] = json.encodeToString(snapshot.dailyPresentLogs)
            prefs[Keys.CanceledClassesJson] = json.encodeToString(snapshot.canceledClasses)
            if (snapshot.examDateMillis == null) prefs.remove(Keys.ExamDateMillis) else prefs[Keys.ExamDateMillis] = snapshot.examDateMillis
            if (snapshot.examSubject.isNullOrBlank()) prefs.remove(Keys.ExamSubject) else prefs[Keys.ExamSubject] = snapshot.examSubject
            if (snapshot.examTime.isNullOrBlank()) prefs.remove(Keys.ExamTime) else prefs[Keys.ExamTime] = snapshot.examTime
            prefs[Keys.ExamPlanJson] = json.encodeToString(snapshot.examPlan)
            prefs[Keys.SemesterStartDateMillis] = snapshot.semesterStartDateMillis
            prefs[Keys.SemesterEndDateMillis] = snapshot.semesterEndDateMillis
            prefs[Keys.RescheduledClassesJson] = json.encodeToString(snapshot.rescheduledClasses)
            prefs[Keys.ClassNotesJson] = json.encodeToString(snapshot.classNotes)
            prefs[Keys.SyllabusJson] = json.encodeToString(snapshot.syllabus)
            prefs[Keys.SyllabusPdfsJson] = json.encodeToString(snapshot.syllabusPdfs)
            prefs[Keys.DarkMode] = snapshot.preferences.darkMode
            prefs[Keys.ShowExamCountdown] = snapshot.preferences.showExamCountdown
            prefs[Keys.ShowMessMenuCard] = snapshot.preferences.showMessMenuCard
            prefs[Keys.RemindersEnabled] = snapshot.preferences.remindersEnabled
            prefs[Keys.MessPref] = snapshot.preferences.messPref
            prefs[Keys.ReminderMinutesBefore] = snapshot.preferences.reminderMinutesBefore
            prefs[Keys.ThemeColor] = snapshot.preferences.themeColor
            prefs[Keys.MinimalistMode] = snapshot.preferences.minimalistMode
            prefs[Keys.CompactMode] = snapshot.preferences.compactMode
            prefs[Keys.DndMode] = snapshot.preferences.dndMode
            if (snapshot.preferences.userName.isBlank()) prefs.remove(Keys.UserName) else prefs[Keys.UserName] = snapshot.preferences.userName
        }
        NotificationScheduler.rescheduleAll(context)
    }

    private fun normalizeToStartOfDay(millis: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    private fun decodeAttendanceMap(raw: String?): MutableMap<String, AttendanceStats> {
        if (raw.isNullOrBlank()) return mutableMapOf()
        return runCatching { json.decodeFromString<Map<String, AttendanceStats>>(raw).toMutableMap() }
            .getOrDefault(mutableMapOf())
    }

    private fun decodeSlotLogMap(raw: String?): MutableMap<String, Set<String>> {
        if (raw.isNullOrBlank()) return mutableMapOf()
        return runCatching { json.decodeFromString<Map<String, Set<String>>>(raw).toMutableMap() }
            .getOrDefault(mutableMapOf())
    }

    private fun applyAttendanceDelta(
        map: MutableMap<String, AttendanceStats>,
        subject: String,
        presentDelta: Int = 0,
        absentDelta: Int = 0
    ) {
        val key = subject.trim()
        val prev = map[key] ?: AttendanceStats()
        val next = prev.copy(
            present = (prev.present + presentDelta).coerceAtLeast(0),
            absent = (prev.absent + absentDelta).coerceAtLeast(0)
        )
        map[key] = next
    }

    // Extended markAttendance to support undo (increments of +1 or -1)
    suspend fun markAttendance(subject: String, isPresent: Boolean, increment: Int = 1) {
        attendanceMutex.withLock {
            val current = context.appDataStore.data.first()[Keys.AttendanceJson]
            val map = decodeAttendanceMap(current)
            if (isPresent) {
                applyAttendanceDelta(map, subject, presentDelta = increment)
            } else {
                applyAttendanceDelta(map, subject, absentDelta = increment)
            }
            context.appDataStore.edit { prefs ->
                prefs[Keys.AttendanceJson] = json.encodeToString(map)
            }
        }
    }

    /** Mark a slot as absent in the daily log (absence-only flow) */
    suspend fun markSlotAbsent(dateKey: String, slotKey: String, subject: String, units: Int = 1) {
        attendanceMutex.withLock {
            val prefs = context.appDataStore.data.first()
            val absentLogs = decodeSlotLogMap(prefs[Keys.DailyLogsJson])
            val presentLogs = decodeSlotLogMap(prefs[Keys.DailyPresentLogsJson])
            val attendance = decodeAttendanceMap(prefs[Keys.AttendanceJson])
            val inc = units.coerceAtLeast(1)

            val absentSet = absentLogs[dateKey].orEmpty()
            val presentSet = presentLogs[dateKey].orEmpty()
            val wasAbsent = absentSet.contains(slotKey)
            val wasPresent = presentSet.contains(slotKey)

            if (!wasAbsent) {
                absentLogs[dateKey] = absentSet + slotKey
                applyAttendanceDelta(attendance, subject, absentDelta = inc)
            }
            if (wasPresent) {
                presentLogs[dateKey] = presentSet - slotKey
                applyAttendanceDelta(attendance, subject, presentDelta = -inc)
            }

            context.appDataStore.edit { editPrefs ->
                editPrefs[Keys.DailyLogsJson] = json.encodeToString(absentLogs)
                editPrefs[Keys.DailyPresentLogsJson] = json.encodeToString(presentLogs)
                editPrefs[Keys.AttendanceJson] = json.encodeToString(attendance)
            }
        }
        NextClassWidget.refreshAll(context)
    }

    suspend fun undoSlotAbsent(dateKey: String, slotKey: String, subject: String, units: Int = 1) {
        attendanceMutex.withLock {
            val prefs = context.appDataStore.data.first()
            val absentLogs = decodeSlotLogMap(prefs[Keys.DailyLogsJson])
            val attendance = decodeAttendanceMap(prefs[Keys.AttendanceJson])
            val inc = units.coerceAtLeast(1)

            val absentSet = absentLogs[dateKey].orEmpty()
            if (absentSet.contains(slotKey)) {
                absentLogs[dateKey] = absentSet - slotKey
                applyAttendanceDelta(attendance, subject, absentDelta = -inc)
            }

            context.appDataStore.edit { editPrefs ->
                editPrefs[Keys.DailyLogsJson] = json.encodeToString(absentLogs)
                editPrefs[Keys.AttendanceJson] = json.encodeToString(attendance)
            }
        }
        NextClassWidget.refreshAll(context)
    }

    /** Mark a slot as present in the daily log */
    suspend fun markSlotPresent(dateKey: String, slotKey: String, subject: String, units: Int = 1) {
        attendanceMutex.withLock {
            val prefs = context.appDataStore.data.first()
            val presentLogs = decodeSlotLogMap(prefs[Keys.DailyPresentLogsJson])
            val absentLogs = decodeSlotLogMap(prefs[Keys.DailyLogsJson])
            val attendance = decodeAttendanceMap(prefs[Keys.AttendanceJson])
            val inc = units.coerceAtLeast(1)

            val presentSet = presentLogs[dateKey].orEmpty()
            val absentSet = absentLogs[dateKey].orEmpty()
            val wasPresent = presentSet.contains(slotKey)
            val wasAbsent = absentSet.contains(slotKey)

            if (!wasPresent) {
                presentLogs[dateKey] = presentSet + slotKey
                applyAttendanceDelta(attendance, subject, presentDelta = inc)
            }
            if (wasAbsent) {
                absentLogs[dateKey] = absentSet - slotKey
                applyAttendanceDelta(attendance, subject, absentDelta = -inc)
            }

            context.appDataStore.edit { editPrefs ->
                editPrefs[Keys.DailyPresentLogsJson] = json.encodeToString(presentLogs)
                editPrefs[Keys.DailyLogsJson] = json.encodeToString(absentLogs)
                editPrefs[Keys.AttendanceJson] = json.encodeToString(attendance)
            }
        }
        NextClassWidget.refreshAll(context)
    }

    suspend fun undoSlotPresent(dateKey: String, slotKey: String, subject: String, units: Int = 1) {
        attendanceMutex.withLock {
            val prefs = context.appDataStore.data.first()
            val presentLogs = decodeSlotLogMap(prefs[Keys.DailyPresentLogsJson])
            val attendance = decodeAttendanceMap(prefs[Keys.AttendanceJson])
            val inc = units.coerceAtLeast(1)

            val presentSet = presentLogs[dateKey].orEmpty()
            if (presentSet.contains(slotKey)) {
                presentLogs[dateKey] = presentSet - slotKey
                applyAttendanceDelta(attendance, subject, presentDelta = -inc)
            }

            context.appDataStore.edit { editPrefs ->
                editPrefs[Keys.DailyPresentLogsJson] = json.encodeToString(presentLogs)
                editPrefs[Keys.AttendanceJson] = json.encodeToString(attendance)
            }
        }
        NextClassWidget.refreshAll(context)
    }

    /** Mark a slot as canceled */
    suspend fun markSlotCanceled(dateKey: String, slotKey: String) {
        val raw = context.appDataStore.data.first()[Keys.CanceledClassesJson]
        val map = if (raw.isNullOrBlank()) mutableMapOf()
        else runCatching { json.decodeFromString<Map<String, Set<String>>>(raw).toMutableMap() }.getOrDefault(mutableMapOf())
        
        val existing = map[dateKey] ?: emptySet()
        map[dateKey] = existing + slotKey
        
        context.appDataStore.edit { prefs ->
            prefs[Keys.CanceledClassesJson] = json.encodeToString(map)
        }
        NextClassWidget.refreshAll(context)
    }

    suspend fun undoSlotCanceled(dateKey: String, slotKey: String) {
        val raw = context.appDataStore.data.first()[Keys.CanceledClassesJson]
        val map = if (raw.isNullOrBlank()) mutableMapOf()
        else runCatching { json.decodeFromString<Map<String, Set<String>>>(raw).toMutableMap() }.getOrDefault(mutableMapOf())
        
        val existing = map[dateKey] ?: emptySet()
        map[dateKey] = existing - slotKey
        
        context.appDataStore.edit { prefs ->
            prefs[Keys.CanceledClassesJson] = json.encodeToString(map)
        }
        NextClassWidget.refreshAll(context)
    }

    suspend fun resetAttendance() {
        context.appDataStore.edit { prefs ->
            prefs.remove(Keys.AttendanceJson)
        }
    }
}
