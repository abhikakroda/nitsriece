package com.example.timetable.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.InputChip
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.timetable.data.AssignmentItem
import com.example.timetable.data.AssignmentPriority
import com.example.timetable.data.AssignmentStatus
import com.example.timetable.data.NoteAttachmentRef
import com.example.timetable.data.NoteAttachmentStorage
import com.example.timetable.data.decodeStoredNote
import com.example.timetable.data.encodeStoredNote
import com.example.timetable.ui.AppViewModel
import com.example.timetable.ui.components.DashboardVibeContainer
import com.example.timetable.ui.components.pressScaleClickable
import kotlin.math.absoluteValue
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

private data class SavedNoteEntry(
    val key: String,
    val subject: String,
    val dateKey: String,
    val dateLabel: String,
    val timeRaw: String,
    val timeLabel: String,
    val note: String,
    val pinned: Boolean,
    val updatedAt: Long,
    val sortTime: Long,
    val attachments: List<NoteAttachmentRef> = emptyList()
)

private data class NoteDraft(
    val originalKey: String? = null,
    val title: String = "",
    val dateKey: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time),
    val timeRaw: String = "09:00",
    val note: String = "",
    val pinned: Boolean = false,
    val attachments: List<NoteAttachmentRef> = emptyList(),
    val storageSessionKey: String = NoteAttachmentStorage.newPendingSession()
)

private data class AssignmentDraft(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val subject: String = "",
    val dueDateMillis: Long = Calendar.getInstance().timeInMillis,
    val dueTimeRaw: String = "23:59",
    val priority: AssignmentPriority = AssignmentPriority.Medium,
    val status: AssignmentStatus = AssignmentStatus.Pending,
    val notes: String = "",
    val reminderOffsetsMinutes: List<Long> = emptyList()
)

private enum class NotesSortMode {
    Recent,
    Subject,
    Pinned
}

private enum class NotesWindowFilter {
    All,
    Today,
    ThisWeek
}

@Composable
fun NotesScreen(
    appViewModel: AppViewModel,
    onBack: () -> Unit,
    /** When true, [MainScreen] owns the top bar — hide duplicate back/title. */
    useShellHeader: Boolean = false,
    onOpenAttachmentPdf: (displayName: String, uriString: String) -> Unit = { _, _ -> }
) {
    val androidContext = androidx.compose.ui.platform.LocalContext.current
    val timetable by appViewModel.timetable.collectAsStateWithLifecycle()
    val classNotes by appViewModel.classNotes.collectAsStateWithLifecycle()
    val assignments by appViewModel.assignments.collectAsStateWithLifecycle()
    val notes = remember(classNotes) { buildSavedNoteEntries(classNotes) }
    val suggestedSubjects = remember(timetable) {
        timetable.values
            .flatten()
            .map { it.subject.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .sorted()
    }
    val allSubjects = remember(notes, suggestedSubjects) {
        (notes.map { it.subject } + suggestedSubjects)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .sorted()
    }
    var selectedSubject by remember { mutableStateOf<String?>(null) }
    var sortMode by remember { mutableStateOf(NotesSortMode.Recent) }
    var windowFilter by remember { mutableStateOf(NotesWindowFilter.All) }
    var editingDraft by remember { mutableStateOf<NoteDraft?>(null) }
    var editingAssignment by remember { mutableStateOf<AssignmentDraft?>(null) }
    val todayKey = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
    }
    val currentWeekKeys = remember(todayKey) {
        buildSet {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val calendar = Calendar.getInstance()
            repeat(7) { offset ->
                val copy = calendar.clone() as Calendar
                copy.add(Calendar.DAY_OF_YEAR, -offset)
                add(format.format(copy.time))
            }
        }
    }
    val filteredNotes = remember(notes, selectedSubject, sortMode, windowFilter, todayKey, currentWeekKeys) {
        val base = notes.filter { note ->
            val subjectMatches = selectedSubject == null || note.subject.equals(selectedSubject, ignoreCase = true)
            val windowMatches = when (windowFilter) {
                NotesWindowFilter.All -> true
                NotesWindowFilter.Today -> note.dateKey == todayKey
                NotesWindowFilter.ThisWeek -> note.dateKey in currentWeekKeys
            }
            subjectMatches && windowMatches
        }
        when (sortMode) {
            NotesSortMode.Recent -> base.sortedWith(compareByDescending<SavedNoteEntry> { it.pinned }.thenByDescending { it.sortTime })
            NotesSortMode.Subject -> base.sortedWith(compareBy<SavedNoteEntry> { it.subject.lowercase(Locale.getDefault()) }.thenByDescending { it.sortTime })
            NotesSortMode.Pinned -> base.sortedWith(compareByDescending<SavedNoteEntry> { it.pinned }.thenByDescending { it.sortTime })
        }
    }
    val hasActiveFilters = selectedSubject != null ||
        sortMode != NotesSortMode.Recent ||
        windowFilter != NotesWindowFilter.All
    val pinnedNotes = remember(filteredNotes) { filteredNotes.filter { it.pinned } }
    val groupedNotes = remember(filteredNotes) { filteredNotes.filterNot { it.pinned }.groupBy { it.dateKey } }
    val openAssignments = remember(assignments) { assignments.filter { it.status != AssignmentStatus.Done } }
    val overdueAssignments = remember(assignments) {
        val nowMillis = System.currentTimeMillis()
        assignments.filter { it.status != AssignmentStatus.Done && it.dueAtMillis < nowMillis }
    }

    DashboardVibeContainer {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Transparent
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (useShellHeader) Modifier else Modifier.statusBarsPadding())
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = if (useShellHeader) 8.dp else 12.dp, bottom = 110.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            item {
                if (useShellHeader) {
                    NotesShellToolbar(
                        count = notes.size,
                        visibleCount = filteredNotes.size,
                        onAdd = { editingDraft = NoteDraft() }
                    )
                } else {
                    NotesHeader(
                        onBack = onBack,
                        count = notes.size,
                        visibleCount = filteredNotes.size,
                        onAdd = { editingDraft = NoteDraft() }
                    )
                }
            }
            item {
                NotesFilterPanel(
                    sortMode = sortMode,
                    onSortChange = { sortMode = it },
                    windowFilter = windowFilter,
                    onWindowFilterChange = { windowFilter = it }
                )
            }
            item {
                AssignmentOverviewCard(
                    assignments = assignments,
                    onAdd = {
                        editingAssignment = AssignmentDraft(
                            subject = selectedSubject.orEmpty()
                        )
                    },
                    onEdit = { assignment ->
                        editingAssignment = AssignmentDraft(
                            id = assignment.id,
                            title = assignment.title,
                            subject = assignment.subject,
                            dueDateMillis = assignment.dueAtMillis,
                            dueTimeRaw = millisToTimeText(assignment.dueAtMillis),
                            priority = assignment.priority,
                            status = assignment.status,
                            notes = assignment.notes,
                            reminderOffsetsMinutes = assignment.reminderOffsetsMinutes
                        )
                    },
                    onStatusChange = { assignment, status ->
                        appViewModel.upsertAssignment(assignment.copy(status = status))
                    },
                    onDelete = { assignment ->
                        appViewModel.deleteAssignment(assignment.id)
                    }
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatPill(
                        modifier = Modifier.weight(1f),
                        label = "Saved",
                        value = notes.size.toString()
                    )
                    StatPill(
                        modifier = Modifier.weight(1f),
                        label = "Pinned",
                        value = pinnedNotes.size.toString()
                    )
                    StatPill(
                        modifier = Modifier.weight(1f),
                        label = "Open Tasks",
                        value = openAssignments.size.toString()
                    )
                }
            }
            if (overdueAssignments.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f))
                    ) {
                        Text(
                            text = "${overdueAssignments.size} assignment${if (overdueAssignments.size == 1) "" else "s"} overdue",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            if (allSubjects.isNotEmpty()) {
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = selectedSubject == null,
                                onClick = { selectedSubject = null },
                                label = { Text("All") }
                            )
                        }
                        items(allSubjects, key = { it }) { subject ->
                            FilterChip(
                                selected = selectedSubject == subject,
                                onClick = {
                                    selectedSubject = if (selectedSubject == subject) null else subject
                                },
                                label = { Text(subject) }
                            )
                        }
                    }
                }
            }

            if (filteredNotes.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NoteAlt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Text(
                                text = if (hasActiveFilters) "No notes found" else "No saved notes",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (hasActiveFilters) {
                                    "Try different filters or create a new note."
                                } else {
                                    "Add notes during an ongoing class and they will appear here."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (hasActiveFilters) {
                                    TextButton(
                                        onClick = {
                                            selectedSubject = null
                                            sortMode = NotesSortMode.Recent
                                            windowFilter = NotesWindowFilter.All
                                        }
                                    ) {
                                        Text("Reset filters")
                                    }
                                }
                                Button(onClick = { editingDraft = NoteDraft() }) {
                                    Text("Add note")
                                }
                            }
                        }
                    }
                }
            } else {
                if (pinnedNotes.isNotEmpty()) {
                    item(key = "header_pinned") {
                        Text(
                            text = "Pinned",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    items(pinnedNotes, key = { it.key }) { note ->
                        NoteCard(
                            note = note,
                            onEdit = {
                                editingDraft = NoteDraft(
                                    originalKey = note.key,
                                    title = note.subject,
                                    dateKey = note.dateKey,
                                    timeRaw = note.timeRaw,
                                    note = note.note,
                                    pinned = note.pinned,
                                    attachments = note.attachments,
                                    storageSessionKey = NoteAttachmentStorage.folderNameForNoteKey(note.key)
                                )
                            },
                            onTogglePin = {
                                appViewModel.setClassNote(
                                    note.key,
                                    encodeStoredNote(note.note, !note.pinned, note.attachments)
                                )
                            },
                            onDelete = {
                                NoteAttachmentStorage.deleteFolderForNoteKey(androidContext, note.key)
                                appViewModel.setClassNote(note.key, null)
                            }
                        )
                    }
                }
                groupedNotes.forEach { (dateKey, notesForDate) ->
                    item(key = "header_$dateKey") {
                        Text(
                            text = formatDateLabel(dateKey),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    items(notesForDate, key = { it.key }) { note ->
                        NoteCard(
                            note = note,
                            onEdit = {
                                editingDraft = NoteDraft(
                                    originalKey = note.key,
                                    title = note.subject,
                                    dateKey = note.dateKey,
                                    timeRaw = note.timeRaw,
                                    note = note.note,
                                    pinned = note.pinned
                                )
                            },
                            onTogglePin = {
                                appViewModel.setClassNote(
                                    note.key,
                                    encodeStoredNote(note.note, !note.pinned)
                                )
                            },
                            onDelete = { appViewModel.setClassNote(note.key, null) }
                        )
                    }
                }
            }
            }
        }
    }

    editingDraft?.let { draft ->
        EditNoteDialog(
            initialDraft = draft,
            onDismiss = { editingDraft = null },
            onSave = { updated ->
                val normalizedKey = buildNoteKey(updated.dateKey, updated.title, updated.timeRaw)
                if (updated.originalKey != null && updated.originalKey != normalizedKey) {
                    appViewModel.setClassNote(updated.originalKey, null)
                }
                appViewModel.setClassNote(normalizedKey, encodeStoredNote(updated.note, updated.pinned))
                editingDraft = null
            },
            onDelete = {
                draft.originalKey?.let { appViewModel.setClassNote(it, null) }
                editingDraft = null
            },
            isNew = draft.originalKey == null,
            suggestedSubjects = suggestedSubjects
        )
    }

    editingAssignment?.let { draft ->
        EditAssignmentDialog(
            initialDraft = draft,
            suggestedSubjects = suggestedSubjects,
            onDismiss = { editingAssignment = null },
            onDelete = {
                appViewModel.deleteAssignment(draft.id)
                editingAssignment = null
            },
            onSave = { updated ->
                val dueAtMillis = mergeDateAndTime(updated.dueDateMillis, updated.dueTimeRaw)
                appViewModel.upsertAssignment(
                    AssignmentItem(
                        id = updated.id,
                        title = updated.title.trim(),
                        subject = updated.subject.trim(),
                        dueAtMillis = dueAtMillis,
                        priority = updated.priority,
                        status = updated.status,
                        reminderOffsetsMinutes = updated.reminderOffsetsMinutes.sortedDescending(),
                        notes = updated.notes.trim()
                    )
                )
                editingAssignment = null
            }
        )
    }
}

@Composable
private fun SortChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        shape = RoundedCornerShape(14.dp),
        leadingIcon = if (selected) {
            {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        } else null
    )
}

@Composable
private fun StatPill(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun AssignmentOverviewCard(
    assignments: List<AssignmentItem>,
    onAdd: () -> Unit,
    onEdit: (AssignmentItem) -> Unit,
    onStatusChange: (AssignmentItem, AssignmentStatus) -> Unit,
    onDelete: (AssignmentItem) -> Unit
) {
    val openAssignments = remember(assignments) { assignments.filter { it.status != AssignmentStatus.Done } }
    val upcoming = remember(openAssignments) { openAssignments.sortedBy { it.dueAtMillis }.take(3) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Assignments",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (openAssignments.isEmpty()) {
                            "Track due work, priorities, and status"
                        } else {
                            "${openAssignments.size} open task${if (openAssignments.size == 1) "" else "s"}"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                        .pressScaleClickable(onClick = onAdd),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add assignment",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (upcoming.isEmpty()) {
                Text(
                    text = "No assignments yet. Add one here and it will also feed the daily agenda on home.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                upcoming.forEach { assignment ->
                    AssignmentCard(
                        assignment = assignment,
                        onEdit = { onEdit(assignment) },
                        onStatusChange = { status -> onStatusChange(assignment, status) },
                        onDelete = { onDelete(assignment) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AssignmentCard(
    assignment: AssignmentItem,
    onEdit: () -> Unit,
    onStatusChange: (AssignmentStatus) -> Unit,
    onDelete: () -> Unit
) {
    val priorityColor = when (assignment.priority) {
        AssignmentPriority.Low -> Color(0xFF0F9D58)
        AssignmentPriority.Medium -> Color(0xFFF59E0B)
        AssignmentPriority.High -> MaterialTheme.colorScheme.error
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = assignment.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = buildString {
                            append(formatAssignmentDueLabel(assignment.dueAtMillis))
                            if (assignment.subject.isNotBlank()) {
                                append(" • ")
                                append(assignment.subject)
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(priorityColor.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = assignment.priority.name,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = priorityColor
                    )
                }
            }

            if (assignment.notes.isNotBlank()) {
                Text(
                    text = assignment.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }

            if (assignment.reminderOffsetsMinutes.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(assignment.reminderOffsetsMinutes.distinct()) { offset ->
                        AssistChip(
                            onClick = {},
                            enabled = false,
                            label = { Text(reminderRuleLabel(offset)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.AssignmentTurnedIn,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                InputChip(
                    selected = assignment.status == AssignmentStatus.Pending,
                    onClick = { onStatusChange(AssignmentStatus.Pending) },
                    label = { Text("Pending") },
                    leadingIcon = {
                        Icon(Icons.Default.PendingActions, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                )
                InputChip(
                    selected = assignment.status == AssignmentStatus.InProgress,
                    onClick = { onStatusChange(AssignmentStatus.InProgress) },
                    label = { Text("Doing") },
                    leadingIcon = {
                        Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                )
                InputChip(
                    selected = assignment.status == AssignmentStatus.Done,
                    onClick = { onStatusChange(AssignmentStatus.Done) },
                    label = { Text("Done") },
                    leadingIcon = {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDelete) {
                    Text("Delete")
                }
                OutlinedButton(onClick = onEdit) {
                    Text("Edit")
                }
            }
        }
    }
}

@Composable
private fun NotesShellToolbar(count: Int, visibleCount: Int, onAdd: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (visibleCount == count) {
                    "$count saved class notes"
                } else {
                    "$visibleCount of $count notes shown"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                .pressScaleClickable(onClick = onAdd),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add note",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun NotesHeader(onBack: () -> Unit, count: Int, visibleCount: Int, onAdd: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f), RoundedCornerShape(14.dp))
                .pressScaleClickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(18.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Notes",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (visibleCount == count) {
                    "$count saved class notes"
                } else {
                    "$visibleCount of $count notes shown"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                .pressScaleClickable(onClick = onAdd),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add note",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun NotesFilterPanel(
    sortMode: NotesSortMode,
    onSortChange: (NotesSortMode) -> Unit,
    windowFilter: NotesWindowFilter,
    onWindowFilterChange: (NotesWindowFilter) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Sort",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        SortChip(
                            label = "Recent",
                            selected = sortMode == NotesSortMode.Recent,
                            onClick = { onSortChange(NotesSortMode.Recent) }
                        )
                    }
                    item {
                        SortChip(
                            label = "Subject",
                            selected = sortMode == NotesSortMode.Subject,
                            onClick = { onSortChange(NotesSortMode.Subject) }
                        )
                    }
                    item {
                        SortChip(
                            label = "Pinned",
                            selected = sortMode == NotesSortMode.Pinned,
                            onClick = { onSortChange(NotesSortMode.Pinned) }
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Date Range",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        SortChip(
                            label = "All time",
                            selected = windowFilter == NotesWindowFilter.All,
                            onClick = { onWindowFilterChange(NotesWindowFilter.All) }
                        )
                    }
                    item {
                        SortChip(
                            label = "Today",
                            selected = windowFilter == NotesWindowFilter.Today,
                            onClick = { onWindowFilterChange(NotesWindowFilter.Today) }
                        )
                    }
                    item {
                        SortChip(
                            label = "This week",
                            selected = windowFilter == NotesWindowFilter.ThisWeek,
                            onClick = { onWindowFilterChange(NotesWindowFilter.ThisWeek) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteCard(
    note: SavedNoteEntry,
    onEdit: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit
) {
    val accent = noteAccent(note.subject)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(Brush.horizontalGradient(accent))
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 14.dp, end = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.linearGradient(accent)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NoteAlt,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = note.subject,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${note.dateLabel} • ${note.timeLabel}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AssistChip(
                            onClick = { },
                            enabled = false,
                            label = { Text(note.timeLabel) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                        if (note.pinned) {
                            AssistChip(
                                onClick = { },
                                enabled = false,
                                label = { Text("Pinned") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.PushPin,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }
                }

                Row {
                    IconButton(onClick = onTogglePin) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = if (note.pinned) "Unpin note" else "Pin note",
                            tint = if (note.pinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = "Edit note",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete note",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (note.updatedAt > 0L) {
                    Text(
                        text = "Updated ${formatUpdatedAtLabel(note.updatedAt)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = note.note,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 6,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun EditNoteDialog(
    initialDraft: NoteDraft,
    onDismiss: () -> Unit,
    onSave: (NoteDraft) -> Unit,
    onDelete: () -> Unit,
    isNew: Boolean,
    suggestedSubjects: List<String>
) {
    var draft by remember(initialDraft) { mutableStateOf(initialDraft) }
    var showDatePicker by remember { mutableStateOf(false) }
    val reminderPresets = remember { listOf(15L, 60L, 360L, 1440L, 10080L) }
    LaunchedEffect(initialDraft) {
        draft = initialDraft
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { onSave(draft.copy(title = draft.title.trim(), note = draft.note.trim())) },
                enabled = draft.title.trim().isNotEmpty() && draft.note.trim().isNotEmpty()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!isNew) {
                    TextButton(onClick = onDelete) {
                        Text("Delete")
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        },
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = if (isNew) "Add Note" else "Edit Note",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Save notes with a date and time",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = draft.title,
                    onValueChange = { draft = draft.copy(title = it) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Title / Subject") },
                    placeholder = { Text("Physics, Math, Lab, etc.") }
                )
                if (suggestedSubjects.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(suggestedSubjects, key = { it }) { subject ->
                            FilterChip(
                                selected = draft.title.trim().equals(subject, ignoreCase = true),
                                onClick = { draft = draft.copy(title = subject) },
                                label = { Text(subject) }
                            )
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = formatDateLabel(draft.dateKey),
                        onValueChange = { },
                        modifier = Modifier.weight(1f),
                        readOnly = true,
                        singleLine = true,
                        label = { Text("Date") }
                    )
                    Button(onClick = { showDatePicker = true }) {
                        Text("Pick")
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilterChip(
                        selected = draft.pinned,
                        onClick = { draft = draft.copy(pinned = !draft.pinned) },
                        label = { Text(if (draft.pinned) "Pinned" else "Pin note") }
                    )
                }
                OutlinedTextField(
                    value = draft.timeRaw,
                    onValueChange = { draft = draft.copy(timeRaw = normalizeTimeInput(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Time") },
                    placeholder = { Text("09:00") }
                )
                OutlinedTextField(
                    value = draft.note,
                    onValueChange = { draft = draft.copy(note = it) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 5,
                    maxLines = 8,
                    label = { Text("Note") },
                    placeholder = { Text("Write summary, homework, doubts, or reminders") },
                    supportingText = {
                        Text("${draft.note.length} characters")
                    }
                )
            }
        }
    )

    if (showDatePicker) {
        NoteDatePicker(
            initialDateKey = draft.dateKey,
            onDismiss = { showDatePicker = false },
            onDateSelected = {
                draft = draft.copy(dateKey = it)
                showDatePicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteDatePicker(
    initialDateKey: String,
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    val formatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val initialMillis = remember(initialDateKey) {
        runCatching { formatter.parse(initialDateKey)?.time }.getOrNull() ?: System.currentTimeMillis()
    }
    val state = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val millis = state.selectedDateMillis ?: initialMillis
                onDateSelected(formatter.format(millis))
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    ) {
        DatePicker(state = state)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditAssignmentDialog(
    initialDraft: AssignmentDraft,
    suggestedSubjects: List<String>,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onSave: (AssignmentDraft) -> Unit
) {
    var draft by remember(initialDraft) { mutableStateOf(initialDraft) }
    var showDatePicker by remember { mutableStateOf(false) }
    val reminderPresets = remember { listOf(15L, 60L, 360L, 1440L, 10080L) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialDraft.title.isBlank()) "Add Assignment" else "Edit Assignment",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = draft.title,
                        onValueChange = { draft = draft.copy(title = it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Title") },
                        singleLine = true
                    )
                }
                item {
                    SubjectInputField(
                        value = draft.subject,
                        onValueChange = { draft = draft.copy(subject = it) },
                        suggestions = suggestedSubjects
                    )
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = { showDatePicker = true }
                        ) {
                            Text(formatAssignmentDate(draft.dueDateMillis))
                        }
                        OutlinedTextField(
                            value = draft.dueTimeRaw,
                            onValueChange = { draft = draft.copy(dueTimeRaw = normalizeTimeInput(it)) },
                            modifier = Modifier.weight(1f),
                            label = { Text("Time") },
                            singleLine = true
                        )
                    }
                }
                item {
                    Text(
                        text = "Priority",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssignmentPriority.entries.forEach { priority ->
                            InputChip(
                                selected = draft.priority == priority,
                                onClick = { draft = draft.copy(priority = priority) },
                                label = { Text(priority.name) }
                            )
                        }
                    }
                }
                item {
                    Text(
                        text = "Status",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssignmentStatus.entries.forEach { status ->
                            InputChip(
                                selected = draft.status == status,
                                onClick = { draft = draft.copy(status = status) },
                                label = { Text(status.name) }
                            )
                        }
                    }
                }
                item {
                    Text(
                        text = "Reminder Rules",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(reminderPresets, key = { it }) { offset ->
                            val selected = offset in draft.reminderOffsetsMinutes
                            InputChip(
                                selected = selected,
                                onClick = {
                                    draft = draft.copy(
                                        reminderOffsetsMinutes = if (selected) {
                                            draft.reminderOffsetsMinutes - offset
                                        } else {
                                            (draft.reminderOffsetsMinutes + listOf(offset))
                                                .distinct()
                                                .sortedDescending()
                                        }
                                    )
                                },
                                label = { Text(reminderRuleLabel(offset)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = draft.notes,
                        onValueChange = { draft = draft.copy(notes = it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Notes") },
                        minLines = 3
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(draft) }, enabled = draft.title.isNotBlank()) {
                Text("Save")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (initialDraft.title.isNotBlank()) {
                    TextButton(onClick = onDelete) {
                        Text("Delete")
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )

    if (showDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = draft.dueDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        draft = draft.copy(dueDateMillis = state.selectedDateMillis ?: draft.dueDateMillis)
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = state)
        }
    }
}

private fun buildSavedNoteEntries(classNotes: Map<String, String>): List<SavedNoteEntry> {
    val keyDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val displayDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    return classNotes.mapNotNull { (key, note) ->
        val payload = decodeStoredNote(note)
        val trimmedNote = payload.body.trim()
        if (trimmedNote.isEmpty()) return@mapNotNull null

        val firstUnderscore = key.indexOf('_')
        if (firstUnderscore <= 0) return@mapNotNull null

        val dateKey = key.substring(0, firstUnderscore)
        val remainder = key.substring(firstUnderscore + 1)
        val timeSplit = remainder.lastIndexOf(':')
        if (timeSplit <= 0) return@mapNotNull null

        val parsedDate = runCatching { keyDateFormat.parse(dateKey) }.getOrNull()
        val subject = remainder.substring(0, timeSplit)
        val startTime = remainder.substring(timeSplit + 1)

        SavedNoteEntry(
            key = key,
            subject = subject,
            dateKey = dateKey,
            dateLabel = parsedDate?.let(displayDateFormat::format) ?: dateKey,
            timeRaw = startTime,
            timeLabel = startTime.to12HourLabel(),
            note = trimmedNote,
            pinned = payload.pinned,
            updatedAt = payload.updatedAt,
            sortTime = maxOf(
                payload.updatedAt,
                (parsedDate?.time ?: 0L) + ((parseTimeMins(startTime) ?: 0) * 60_000L)
            )
        )
    }.sortedWith(compareByDescending<SavedNoteEntry> { it.pinned }.thenByDescending { it.sortTime })
}

private fun parseTimeMins(time: String): Int? {
    val parts = time.split(":")
    if (parts.size != 2) return null
    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null
    return hour * 60 + minute
}

private fun String.to12HourLabel(): String {
    val parts = split(":")
    if (parts.size != 2) return this
    val hour = parts[0].toIntOrNull() ?: return this
    val minute = parts[1].toIntOrNull() ?: return this
    val suffix = if (hour >= 12) "PM" else "AM"
    val hour12 = if (hour % 12 == 0) 12 else hour % 12
    return String.format(Locale.getDefault(), "%02d:%02d %s", hour12, minute, suffix)
}

private fun buildNoteKey(dateKey: String, title: String, timeRaw: String): String {
    val safeTitle = title.trim().replace("_", " ").replace(":", " ")
    return "${dateKey}_${safeTitle}:${normalizeTimeInput(timeRaw)}"
}

private fun normalizeTimeInput(value: String): String {
    val filtered = value.filter { it.isDigit() || it == ':' }
    val parts = filtered.split(":")
    if (parts.size < 2) return filtered.take(5)
    val hour = parts[0].take(2).toIntOrNull()?.coerceIn(0, 23) ?: 0
    val minute = parts[1].take(2).toIntOrNull()?.coerceIn(0, 59) ?: 0
    return String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
}

private fun formatDateLabel(dateKey: String): String {
    val source = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val target = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val parsed = runCatching { source.parse(dateKey) }.getOrNull() ?: return dateKey
    return target.format(parsed)
}

private fun formatUpdatedAtLabel(updatedAt: Long): String {
    val now = System.currentTimeMillis()
    val diffMinutes = ((now - updatedAt) / 60_000L).coerceAtLeast(0L)
    return when {
        diffMinutes < 1L -> "just now"
        diffMinutes < 60L -> "${diffMinutes}m ago"
        diffMinutes < 1_440L -> "${diffMinutes / 60L}h ago"
        else -> SimpleDateFormat("dd MMM", Locale.getDefault()).format(updatedAt)
    }
}

private fun mergeDateAndTime(dateMillis: Long, timeRaw: String): Long {
    val calendar = Calendar.getInstance().apply { timeInMillis = dateMillis }
    val normalized = normalizeTimeInput(timeRaw)
    val parts = normalized.split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull()?.coerceIn(0, 23) ?: 0
    val minute = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59) ?: 0
    calendar.set(Calendar.HOUR_OF_DAY, hour)
    calendar.set(Calendar.MINUTE, minute)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

private fun millisToTimeText(millis: Long): String {
    val calendar = Calendar.getInstance().apply { timeInMillis = millis }
    return String.format(
        Locale.getDefault(),
        "%02d:%02d",
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE)
    )
}

private fun formatAssignmentDate(millis: Long): String =
    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(millis)

private fun formatAssignmentDueLabel(millis: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayKey = formatter.format(System.currentTimeMillis())
    val dueKey = formatter.format(millis)
    val dateLabel = if (todayKey == dueKey) {
        "Today"
    } else {
        SimpleDateFormat("dd MMM", Locale.getDefault()).format(millis)
    }
    return "$dateLabel • ${millisToTimeText(millis).to12HourLabel()}"
}

private fun reminderRuleLabel(offsetMinutes: Long): String = when (offsetMinutes) {
    15L -> "15 min before"
    60L -> "1 hour before"
    360L -> "6 hours before"
    1440L -> "1 day before"
    10080L -> "1 week before"
    else -> "$offsetMinutes min before"
}

private fun noteAccent(subject: String): List<Color> {
    val palette = listOf(
        listOf(Color(0xFF4F8CFF), Color(0xFF64D2FF)),
        listOf(Color(0xFF7C5CFF), Color(0xFFB28DFF)),
        listOf(Color(0xFF14B8A6), Color(0xFF5EEAD4)),
        listOf(Color(0xFFF97316), Color(0xFFFBBF24)),
        listOf(Color(0xFFEF4444), Color(0xFFF97393))
    )
    return palette[subject.hashCode().absoluteValue % palette.size]
}
