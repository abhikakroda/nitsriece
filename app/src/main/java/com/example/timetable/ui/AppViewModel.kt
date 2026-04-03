package com.example.timetable.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.timetable.data.AppDay
import com.example.timetable.data.AppFullDay
import com.example.timetable.data.AppMeal
import com.example.timetable.data.AppBackupSnapshot
import com.example.timetable.data.AppRepository
import com.example.timetable.data.AssignmentItem
import com.example.timetable.data.AttendanceStats
import com.example.timetable.data.ClassSlot
import com.example.timetable.data.SavedNotification
import com.example.timetable.data.SyllabusItem
import com.example.timetable.data.ExamPlan
import com.example.timetable.data.DefaultMessMenu
import com.example.timetable.data.DefaultTimetable
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(private val repository: AppRepository) : ViewModel() {
    // ... existing properties ...
    val timetable: StateFlow<Map<AppDay, List<ClassSlot>>> =
        repository.timetable.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DefaultTimetable)

    val messMenu: StateFlow<Map<AppFullDay, List<AppMeal>>> =
        repository.messMenu.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DefaultMessMenu)

    val assignments: StateFlow<List<AssignmentItem>> =
        repository.assignments.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val examDateMillis: StateFlow<Long?> =
        repository.examDateMillis.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val examSubject: StateFlow<String?> =
        repository.examSubject.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val examTime: StateFlow<String?> =
        repository.examTime.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val examPlan: StateFlow<ExamPlan> =
        repository.examPlan.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExamPlan())

    val semesterStartDateMillis: StateFlow<Long> =
        repository.semesterStartDateMillis.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    val semesterEndDateMillis: StateFlow<Long> =
        repository.semesterEndDateMillis.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    val rescheduledClasses: StateFlow<Map<String, ClassSlot>> =
        repository.rescheduledClasses.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val syllabus: StateFlow<Map<String, List<SyllabusItem>>> =
        repository.syllabus.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val classNotes: StateFlow<Map<String, String>> =
        repository.classNotes.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val savedNotifications: StateFlow<List<SavedNotification>> =
        repository.savedNotifications.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val syllabusPdfs: StateFlow<Map<String, String>> =
        repository.syllabusPdfs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val darkMode: StateFlow<Boolean> =
        repository.darkMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val showExamCountdown: StateFlow<Boolean> =
        repository.showExamCountdown.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val showMessMenuCard: StateFlow<Boolean> =
        repository.showMessMenuCard.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val remindersEnabled: StateFlow<Boolean> =
        repository.remindersEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val messPref: StateFlow<String> =
        repository.messPref.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "veg")

    val reminderMinutesBefore: StateFlow<Long> =
        repository.reminderMinutesBefore.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 10L)

    val themeColor: StateFlow<Long> =
        repository.themeColor.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0xFF2563EB)

    val updateManifestUrl: StateFlow<String> =
        repository.updateManifestUrl.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    val pendingUpdateDownloadId: StateFlow<Long?> =
        repository.pendingUpdateDownloadId.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val userName: StateFlow<String> =
        repository.userName.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    val minimalistMode: StateFlow<Boolean> =
        repository.minimalistMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val compactMode: StateFlow<Boolean> =
        repository.compactMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val dndMode: StateFlow<Boolean> =
        repository.dndMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val attendance: StateFlow<Map<String, AttendanceStats>> =
        repository.attendance.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val dailyAbsentLogs: StateFlow<Map<String, Set<String>>> =
        repository.dailyAbsentLogs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val dailyPresentLogs: StateFlow<Map<String, Set<String>>> =
        repository.dailyPresentLogs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val canceledClasses: StateFlow<Map<String, Set<String>>> =
        repository.canceledClasses.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())
    
    // ... existing setters ...

    fun setTimetable(value: Map<AppDay, List<ClassSlot>>) {
        viewModelScope.launch { repository.setTimetable(value) }
    }

    fun resetTimetable() {
        viewModelScope.launch { repository.resetTimetable() }
    }

    fun setMessMenu(value: Map<AppFullDay, List<AppMeal>>) {
        viewModelScope.launch { repository.setMessMenu(value) }
    }

    fun resetMessMenu() {
        viewModelScope.launch { repository.resetMessMenu() }
    }

    fun upsertAssignment(item: AssignmentItem) {
        viewModelScope.launch { repository.upsertAssignment(item) }
    }

    fun deleteAssignment(id: String) {
        viewModelScope.launch { repository.deleteAssignment(id) }
    }

    fun setExamDetails(dateMillis: Long?, subject: String?, time: String?) {
        viewModelScope.launch { repository.setExamDetails(dateMillis, subject, time) }
    }

    fun setExamPlan(plan: ExamPlan) {
        viewModelScope.launch { repository.setExamPlan(plan) }
    }

    fun setSemesterStartDate(millis: Long) {
        viewModelScope.launch { repository.setSemesterStartDate(millis) }
    }

    fun setSemesterEndDate(millis: Long) {
        viewModelScope.launch { repository.setSemesterEndDate(millis) }
    }

    fun setRescheduledClass(dateKey: String, slotKey: String, newSlot: ClassSlot) {
        viewModelScope.launch { repository.setRescheduledClass(dateKey, slotKey, newSlot) }
    }

    fun addTemporaryClass(dateKey: String, slot: ClassSlot) {
        val slotKey = "extra_${slot.subject.trim()}:${slot.startTime}"
        viewModelScope.launch { repository.setRescheduledClass(dateKey, slotKey, slot) }
    }

    fun saveSyllabus(subject: String, items: List<SyllabusItem>) {
        viewModelScope.launch { repository.saveSyllabus(subject, items) }
    }

    fun setClassNote(noteKey: String, note: String?) {
        viewModelScope.launch { repository.setClassNote(noteKey, note) }
    }

    fun setSyllabusPdf(subject: String, uriString: String?) {
        viewModelScope.launch { repository.setSyllabusPdf(subject, uriString) }
    }

    fun setDarkMode(value: Boolean) {
        viewModelScope.launch { repository.setDarkMode(value) }
    }

    fun setShowExamCountdown(value: Boolean) {
        viewModelScope.launch { repository.setShowExamCountdown(value) }
    }

    fun setShowMessMenuCard(value: Boolean) {
        viewModelScope.launch { repository.setShowMessMenuCard(value) }
    }

    fun setRemindersEnabled(value: Boolean) {
        viewModelScope.launch { repository.setRemindersEnabled(value) }
    }

    fun setMessPref(value: String) {
        viewModelScope.launch { repository.setMessPref(value) }
    }

    fun setReminderMinutesBefore(value: Long) {
        viewModelScope.launch { repository.setReminderMinutesBefore(value) }
    }

    fun setThemeColor(value: Long) {
        viewModelScope.launch { repository.setThemeColor(value) }
    }

    fun setUpdateManifestUrl(value: String) {
        viewModelScope.launch { repository.setUpdateManifestUrl(value) }
    }

    fun setPendingUpdateDownloadId(value: Long?) {
        viewModelScope.launch { repository.setPendingUpdateDownloadId(value) }
    }

    fun setUserName(value: String) {
        viewModelScope.launch { repository.setUserName(value) }
    }

    fun setMinimalistMode(value: Boolean) {
        viewModelScope.launch { repository.setMinimalistMode(value) }
    }

    fun setCompactMode(value: Boolean) {
        viewModelScope.launch { repository.setCompactMode(value) }
    }

    fun setDndMode(value: Boolean) {
        viewModelScope.launch { repository.setDndMode(value) }
    }

    fun markPresent(subject: String) {
        viewModelScope.launch { repository.markAttendance(subject = subject, isPresent = true) }
    }

    fun markAbsent(subject: String) {
        viewModelScope.launch { repository.markAttendance(subject = subject, isPresent = false) }
    }

    fun markSlotAbsent(dateKey: String, slotKey: String, subject: String, units: Int = 1) {
        viewModelScope.launch { repository.markSlotAbsent(dateKey, slotKey, subject, units) }
    }

    fun undoSlotAbsent(dateKey: String, slotKey: String, subject: String, units: Int = 1) {
        viewModelScope.launch { repository.undoSlotAbsent(dateKey, slotKey, subject, units) }
    }

    fun markSlotPresent(dateKey: String, slotKey: String, subject: String, units: Int = 1) {
        viewModelScope.launch { repository.markSlotPresent(dateKey, slotKey, subject, units) }
    }

    fun undoSlotPresent(dateKey: String, slotKey: String, subject: String, units: Int = 1) {
        viewModelScope.launch { repository.undoSlotPresent(dateKey, slotKey, subject, units) }
    }

    fun markSlotCanceled(dateKey: String, slotKey: String) {
        viewModelScope.launch { repository.markSlotCanceled(dateKey, slotKey) }
    }

    fun undoSlotCanceled(dateKey: String, slotKey: String) {
        viewModelScope.launch { repository.undoSlotCanceled(dateKey, slotKey) }
    }

    fun resetAttendance() {
        viewModelScope.launch { repository.resetAttendance() }
    }

    suspend fun createBackupSnapshot(): AppBackupSnapshot = repository.createBackupSnapshot()

    suspend fun restoreBackupSnapshot(snapshot: AppBackupSnapshot) {
        repository.restoreBackupSnapshot(snapshot)
    }
}

class AppViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
