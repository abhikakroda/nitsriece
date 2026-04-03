package com.example.timetable.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.timetable.data.AppDay
import com.example.timetable.data.ClassSlot
import com.example.timetable.ui.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableEditorScreen(appViewModel: AppViewModel, onBack: () -> Unit) {
    val timetable by appViewModel.timetable.collectAsState()
    var working by remember(timetable) { mutableStateOf(timetable) }
    var selectedDay by remember { mutableStateOf(AppDay.Mon) }
    val existingSubjects = remember(working) {
        working.values.flatten().map { it.subject }
    }

    var editingIndex by remember { mutableStateOf<Int?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var editDay by remember { mutableStateOf(selectedDay) }
    var dayMenuExpanded by remember { mutableStateOf(false) }
    var subject by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var room by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var faculty by remember { mutableStateOf("") }
    var editorConflict by remember { mutableStateOf<TimetableConflict?>(null) }
    val subjectSuggestions = remember(subject, existingSubjects) {
        subjectSuggestions(subject, existingSubjects)
    }
    val timetableConflicts = remember(working) {
        working.mapValues { (_, slots) -> findDayConflicts(slots) }.filterValues { it.isNotEmpty() }
    }

    fun openEditor(index: Int?) {
        editingIndex = index
        editDay = selectedDay
        dayMenuExpanded = false
        val existing = index?.let { working[selectedDay]?.getOrNull(it) }
        subject = existing?.subject.orEmpty()
        startTime = existing?.startTime.orEmpty()
        endTime = existing?.endTime.orEmpty()
        room = existing?.room.orEmpty()
        type = existing?.type.orEmpty()
        faculty = existing?.faculty.orEmpty()
        editorConflict = null
        showEditor = true
    }

    fun applySlot(index: Int?) {
        val st = startTime.trim()
        val et = endTime.trim()
        val slot = ClassSlot(
            subject = canonicalizeSubjectName(subject, existingSubjects),
            time = formatRange(st, et),
            startTime = st,
            endTime = et,
            room = room.trim(),
            icon = "school",
            iconBg = "bg-primary/10",
            iconColor = "text-primary",
            type = type.trim().ifBlank { null },
            faculty = faculty.trim().ifBlank { null }
        )
        val existing = index?.let { working[selectedDay]?.getOrNull(it) }
        val conflict = findTimetableConflict(
            timetable = working,
            targetDay = editDay,
            candidate = slot,
            ignoreSlot = existing
        )
        if (conflict != null) {
            editorConflict = conflict
            return
        }
        editorConflict = null

        val listCurrentDay = (working[selectedDay] ?: emptyList()).toMutableList()
        val listTargetDay = (working[editDay] ?: emptyList()).toMutableList()

        if (index == null) {
            listTargetDay.add(slot)
            val sortedTarget = listTargetDay.sortedBy { parseTimeToMinutes(it.startTime) ?: Int.MAX_VALUE }
            working = working.toMutableMap().apply { put(editDay, sortedTarget) }
        } else {
            if (editDay == selectedDay) {
                listCurrentDay[index] = slot
                val sortedCurrent = listCurrentDay.sortedBy { parseTimeToMinutes(it.startTime) ?: Int.MAX_VALUE }
                working = working.toMutableMap().apply { put(selectedDay, sortedCurrent) }
            } else {
                listCurrentDay.removeAt(index)
                listTargetDay.add(slot)
                val sortedCurrent = listCurrentDay.sortedBy { parseTimeToMinutes(it.startTime) ?: Int.MAX_VALUE }
                val sortedTarget = listTargetDay.sortedBy { parseTimeToMinutes(it.startTime) ?: Int.MAX_VALUE }
                working = working.toMutableMap().apply {
                    put(selectedDay, sortedCurrent)
                    put(editDay, sortedTarget)
                }
            }
        }
        selectedDay = editDay
    }

    fun deleteSlot(index: Int) {
        val list = (working[selectedDay] ?: emptyList()).toMutableList()
        if (index in list.indices) {
            list.removeAt(index)
            working = working.toMutableMap().apply { put(selectedDay, list) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Timetable", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { appViewModel.resetTimetable() }) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                    }
                    IconButton(
                        onClick = { appViewModel.setTimetable(working) },
                        enabled = working != timetable && timetableConflicts.isEmpty()
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { openEditor(null) },
                modifier = Modifier.padding(bottom = 80.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            val scrollState = rememberScrollState()
            val density = LocalDensity.current
            LaunchedEffect(selectedDay) {
                // Approximate width of a tab (60dp) + spacing (8dp)
                val index = AppDay.entries.indexOf(selectedDay)
                val targetPx = with(density) { (index * 68).dp.toPx() }
                scrollState.animateScrollTo(targetPx.toInt())
            }
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(scrollState), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppDay.entries.forEach { day ->
                    DayTab(
                        day = day,
                        isSelected = selectedDay == day,
                        onClick = { selectedDay = day },
                        modifier = Modifier.width(60.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            val selectedDayConflicts = timetableConflicts[selectedDay].orEmpty()
            if (selectedDayConflicts.isNotEmpty()) {
                ConflictWarningCard(
                    title = "Conflicts on ${selectedDay.fullName}",
                    conflicts = selectedDayConflicts.map { it.label }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            val slots = working[selectedDay] ?: emptyList()
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (slots.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 48.dp), contentAlignment = Alignment.Center) {
                            Text("No classes. Tap + to add.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                        }
                    }
                } else {
                    itemsIndexed(slots) { index, slot ->
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { openEditor(index) }
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(slot.subject, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(
                                        "${slot.time}  •  ${slot.room}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                                IconButton(onClick = { deleteSlot(index) }) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditor) {
        val stOk = parseTimeToMinutes(startTime.trim()) != null
        val etOk = parseTimeToMinutes(endTime.trim()) != null
        val previewSlot = remember(subject, startTime, endTime, room, type, faculty, existingSubjects) {
            val st = startTime.trim()
            val et = endTime.trim()
            if (subject.trim().isEmpty() || room.trim().isEmpty()) null else {
                ClassSlot(
                    subject = canonicalizeSubjectName(subject, existingSubjects),
                    time = formatRange(st, et),
                    startTime = st,
                    endTime = et,
                    room = room.trim(),
                    icon = "school",
                    iconBg = "bg-primary/10",
                    iconColor = "text-primary",
                    type = type.trim().ifBlank { null },
                    faculty = faculty.trim().ifBlank { null }
                )
            }
        }
        val targetSlots = remember(working, editDay, selectedDay, editingIndex) {
            (working[editDay] ?: emptyList()).filterIndexed { index, _ ->
                !(editDay == selectedDay && editingIndex == index)
            }
        }
        val candidateConflicts = remember(previewSlot, targetSlots) {
            previewSlot?.let { findDayConflicts(targetSlots + it) }.orEmpty()
        }
        val formOk = subject.trim().isNotEmpty() &&
            room.trim().isNotEmpty() &&
            stOk &&
            etOk &&
            candidateConflicts.isEmpty()

        AlertDialog(
            onDismissRequest = {
                showEditor = false
            },
            title = { Text(if (editingIndex == null) "Add Class" else "Edit Class", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ExposedDropdownMenuBox(
                        expanded = dayMenuExpanded,
                        onExpandedChange = { dayMenuExpanded = !dayMenuExpanded }
                    ) {
                        OutlinedTextField(
                            value = editDay.fullName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Day") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dayMenuExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = dayMenuExpanded,
                            onDismissRequest = { dayMenuExpanded = false }
                        ) {
                            AppDay.entries.forEach { day ->
                                DropdownMenuItem(
                                    text = { Text(day.fullName) },
                                    onClick = {
                                        editDay = day
                                        dayMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    SubjectInputField(
                        value = subject,
                        onValueChange = { subject = it },
                        suggestions = subjectSuggestions,
                        modifier = Modifier.fillMaxWidth()
                    )
                    editorConflict?.let { conflict ->
                        Text(
                            text = conflict.message,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Start Time (HH:MM)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("End Time (HH:MM)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = room,
                        onValueChange = { room = it },
                        label = { Text("Room") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = type,
                        onValueChange = { type = it },
                        label = { Text("Type (optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = faculty,
                        onValueChange = { faculty = it },
                        label = { Text("Faculty (optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (!stOk || !etOk) {
                        Text(
                            "Time format must be HH:MM (24h).",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    if (candidateConflicts.isNotEmpty()) {
                        ConflictWarningCard(
                            title = "Overlapping class",
                            conflicts = candidateConflicts.map { it.label }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        applySlot(editingIndex)
                        if (editorConflict == null) {
                            showEditor = false
                        }
                    },
                    enabled = formOk
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showEditor = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ConflictWarningCard(
    title: String,
    conflicts: List<String>
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            conflicts.take(3).forEach { conflict ->
                Text(
                    text = conflict,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun parseTimeToMinutes(text: String): Int? {
    val t = text.trim()
    val parts = t.split(":")
    if (parts.size != 2) return null
    val h = parts[0].toIntOrNull() ?: return null
    val m = parts[1].toIntOrNull() ?: return null
    if (h !in 0..23 || m !in 0..59) return null
    return h * 60 + m
}

private fun formatRange(start: String, end: String): String = "${start} – ${end}"

@androidx.compose.runtime.Composable
private fun DayTab(day: AppDay, isSelected: Boolean, onClick: () -> Unit, modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier) {
    val bg by androidx.compose.animation.animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    )
    val textColor by androidx.compose.animation.animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
    )
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(
            text = day.shortName,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}
