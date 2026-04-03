package com.example.timetable.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.timetable.data.ExamPlan
import com.example.timetable.data.to12Hour
import com.example.timetable.ui.AppViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamDateEditorScreen(appViewModel: AppViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val timetable by appViewModel.timetable.collectAsState()
    val examPlan by appViewModel.examPlan.collectAsState()

    val subjects = remember(timetable) {
        timetable.values
            .flatten()
            .map { it.subject.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
    }

    var startDateMillis by remember(examPlan) { mutableStateOf(examPlan.startDateMillis) }
    var endDateMillis by remember(examPlan) { mutableStateOf(examPlan.endDateMillis) }
    var commonStartTime by remember(examPlan) { mutableStateOf(examPlan.commonStartTime) }
    var commonEndTime by remember(examPlan) { mutableStateOf(examPlan.commonEndTime) }

    val subjectDateMap = remember { mutableStateMapOf<String, Long?>() }
    LaunchedEffect(examPlan, subjects) {
        subjectDateMap.clear()
        subjects.forEach { subject ->
            subjectDateMap[subject] = examPlan.subjectExamDates[subject]
        }
    }

    fun pickDate(current: Long?, onSelected: (Long) -> Unit) {
        val cal = Calendar.getInstance().apply {
            timeInMillis = current ?: System.currentTimeMillis()
        }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val picked = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                onSelected(picked.timeInMillis)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun pickTime(current: String, onSelected: (String) -> Unit) {
        val initial = parseTimeParts(current)
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                onSelected(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute))
            },
            initial.first,
            initial.second,
            false
        ).show()
    }

    fun savePlan() {
        if (startDateMillis == null || endDateMillis == null) {
            Toast.makeText(context, "Set exam start and end date", Toast.LENGTH_SHORT).show()
            return
        }

        val normalizedStart = normalizeToStartOfDay(startDateMillis!!)
        val normalizedEnd = normalizeToStartOfDay(endDateMillis!!)
        val actualStart = minOf(normalizedStart, normalizedEnd)
        val actualEnd = maxOf(normalizedStart, normalizedEnd)

        val subjectDates = subjectDateMap
            .mapNotNull { (subject, date) ->
                if (date == null) null else subject to normalizeToStartOfDay(date)
            }
            .toMap()

        appViewModel.setExamPlan(
            ExamPlan(
                startDateMillis = actualStart,
                endDateMillis = actualEnd,
                commonStartTime = commonStartTime,
                commonEndTime = commonEndTime,
                subjectExamDates = subjectDates
            )
        )
        onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Exam Planner", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        appViewModel.setExamPlan(ExamPlan())
                        Toast.makeText(context, "Exam plan cleared", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear")
                    }
                    IconButton(onClick = { savePlan() }) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "Set exam range and per-subject dates. During this range, classes are hidden.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                OutlinedButton(
                    onClick = {
                        val preset = midTermPresetForSixthSem(subjects)
                        startDateMillis = preset.startDateMillis
                        endDateMillis = preset.endDateMillis
                        commonStartTime = preset.commonStartTime
                        commonEndTime = preset.commonEndTime
                        subjectDateMap.clear()
                        subjects.forEach { subject ->
                            subjectDateMap[subject] = preset.subjectExamDates[subject]
                        }
                        Toast.makeText(context, "Mid-term preset loaded", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Load 6th Sem Mid Term Exams")
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Exam Window",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { pickDate(startDateMillis) { startDateMillis = it } },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Start: ${formatDate(startDateMillis)}")
                            }
                            OutlinedButton(
                                onClick = { pickDate(endDateMillis) { endDateMillis = it } },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("End: ${formatDate(endDateMillis)}")
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Common Exam Time",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { pickTime(commonStartTime) { commonStartTime = it } },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.size(6.dp))
                                Text(commonStartTime.to12Hour())
                            }
                            OutlinedButton(
                                onClick = { pickTime(commonEndTime) { commonEndTime = it } },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.size(6.dp))
                                Text(commonEndTime.to12Hour())
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Subject Exam Dates",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (subjects.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Text(
                            text = "No subjects found in timetable.",
                            modifier = Modifier.padding(14.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(subjects) { subject ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = subject,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold
                                )
                                ElevatedAssistChip(
                                    onClick = { pickDate(subjectDateMap[subject]) { picked -> subjectDateMap[subject] = picked } },
                                    label = { Text("Date: ${formatDate(subjectDateMap[subject])}") },
                                    leadingIcon = {
                                        Icon(Icons.Default.EditCalendar, contentDescription = null)
                                    }
                                )
                            }
                            IconButton(onClick = { subjectDateMap[subject] = null }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear date")
                            }
                        }
                    }
                }
            }

            item {
                OutlinedButton(onClick = { savePlan() }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("Save Exam Plan")
                }
            }
        }
    }
}

private fun formatDate(millis: Long?): String {
    return millis?.let {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(it))
    } ?: "Not set"
}

private fun parseTimeParts(time24: String): Pair<Int, Int> {
    val parts = time24.split(":")
    if (parts.size != 2) return 9 to 30
    val hour = parts[0].toIntOrNull() ?: 9
    val min = parts[1].toIntOrNull() ?: 30
    return hour.coerceIn(0, 23) to min.coerceIn(0, 59)
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

private fun midTermPresetForSixthSem(subjects: List<String>): ExamPlan {
    val mappedDates = buildMap<String, Long> {
        val examMap = mapOf(
            "Digital Signal Processing" to localDateMillis(2026, Calendar.APRIL, 6),
            "VLSI Design" to localDateMillis(2026, Calendar.APRIL, 7),
            "Computer Org & Arch" to localDateMillis(2026, Calendar.APRIL, 8),
            "Data Comm. & Networking" to localDateMillis(2026, Calendar.APRIL, 9),
            "Elective-II" to localDateMillis(2026, Calendar.APRIL, 10),
            "Open Elective" to localDateMillis(2026, Calendar.APRIL, 13)
        )
        subjects.forEach { subject ->
            examMap[subject]?.let { put(subject, it) }
        }
    }
    return ExamPlan(
        startDateMillis = localDateMillis(2026, Calendar.APRIL, 6),
        endDateMillis = localDateMillis(2026, Calendar.APRIL, 13),
        commonStartTime = "14:00",
        commonEndTime = "15:15",
        subjectExamDates = mappedDates
    )
}

private fun localDateMillis(year: Int, month: Int, day: Int): Long {
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
