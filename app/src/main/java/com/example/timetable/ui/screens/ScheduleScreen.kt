package com.example.timetable.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.timetable.data.AppDay
import com.example.timetable.data.ClassSlot
import com.example.timetable.data.ExamPlan
import com.example.timetable.data.to12Hour
import com.example.timetable.ui.AppViewModel
import com.example.timetable.ui.components.DashboardVibeContainer
import com.example.timetable.ui.components.pressScaleClickable
import com.example.timetable.ui.theme.ErrorRed
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    appViewModel: AppViewModel,
    @Suppress("UNUSED_PARAMETER") onEditTimetable: () -> Unit,
    onEditWeeklySchedule: () -> Unit = {}
) {
    val now = remember { Calendar.getInstance() }
    var selectedDateCalendar by remember { mutableStateOf(now.clone() as Calendar) }
    var showCalendarSheet by remember { mutableStateOf(false) }
    var showAddExtraClassDialog by remember { mutableStateOf(false) }

    val dowSelected = selectedDateCalendar.get(Calendar.DAY_OF_WEEK)

    val todayDateKey = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now.time) }

    val selectedDay = remember(selectedDateCalendar) {
        when (dowSelected) {
            Calendar.MONDAY -> AppDay.Mon; Calendar.TUESDAY -> AppDay.Tue
            Calendar.WEDNESDAY -> AppDay.Wed; Calendar.THURSDAY -> AppDay.Thu
            Calendar.FRIDAY -> AppDay.Fri; Calendar.SATURDAY -> AppDay.Sat
            Calendar.SUNDAY -> AppDay.Sun
            else -> AppDay.Mon
        }
    }
    val timetable by appViewModel.timetable.collectAsStateWithLifecycle()
    val examPlan by appViewModel.examPlan.collectAsStateWithLifecycle()
    val rescheduledClasses by appViewModel.rescheduledClasses.collectAsStateWithLifecycle()
    val classes = timetable[selectedDay] ?: emptyList()
    val existingSubjects = remember(timetable) {
        timetable.values.flatten().map { it.subject }
    }

    val selectedDateKey = remember(selectedDateCalendar) {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDateCalendar.time)
    }
    val selectedDateMillis = remember(selectedDateCalendar) {
        startOfDayMillis(selectedDateCalendar.timeInMillis)
    }
    val isExamWindowDay = remember(examPlan, selectedDateMillis) {
        isInExamWindow(examPlan, selectedDateMillis)
    }
    val examSubjectsOnSelectedDate = remember(examPlan, selectedDateMillis) {
        examPlan.subjectExamDates
            .filterValues { startOfDayMillis(it) == selectedDateMillis }
            .keys
            .toList()
    }
    val extraClasses = remember(rescheduledClasses, selectedDateKey) {
        rescheduledClasses
            .filterKeys { it.startsWith("${selectedDateKey}_extra_") }
            .values
            .sortedBy { scheduleParseMins(it.startTime) ?: Int.MAX_VALUE }
    }
    val effectiveClasses = remember(isExamWindowDay, classes, extraClasses) {
        if (isExamWindowDay) emptyList() else (classes + extraClasses)
            .sortedBy { scheduleParseMins(it.startTime) ?: Int.MAX_VALUE }
    }

    val dailyAbsentLogs by appViewModel.dailyAbsentLogs.collectAsStateWithLifecycle()
    val dailyPresentLogs by appViewModel.dailyPresentLogs.collectAsStateWithLifecycle()
    val canceledClasses by appViewModel.canceledClasses.collectAsStateWithLifecycle()
    val absentSlots = dailyAbsentLogs[selectedDateKey] ?: emptySet()
    val presentSlots = dailyPresentLogs[selectedDateKey] ?: emptySet()
    val canceledSlots = canceledClasses[selectedDateKey] ?: emptySet()

    val haptic = LocalHapticFeedback.current
    val nowMinutes = remember { now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE) }
    val monthYear = remember(selectedDateCalendar) {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(selectedDateCalendar.time)
    }
    val selectedDateLabel = remember(selectedDateCalendar) {
        SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(selectedDateCalendar.time)
    }
    val isSelectedToday = remember(selectedDateKey, todayDateKey) { selectedDateKey == todayDateKey }
    val monthGridDates = remember(selectedDateCalendar) {
        buildMonthGridDates(selectedDateCalendar)
    }
    val focusClass = remember(effectiveClasses, isSelectedToday, nowMinutes) {
        if (effectiveClasses.isEmpty()) return@remember null
        if (!isSelectedToday) {
            effectiveClasses.firstOrNull()
        } else {
            effectiveClasses.firstOrNull { slot ->
                val end = scheduleParseMins(slot.endTime) ?: return@firstOrNull false
                end >= nowMinutes
            }
        }
    }
    val focusClassIsLive = remember(focusClass, isSelectedToday, nowMinutes) {
        if (!isSelectedToday || focusClass == null) return@remember false
        val start = scheduleParseMins(focusClass.startTime) ?: return@remember false
        val end = scheduleParseMins(focusClass.endTime) ?: return@remember false
        nowMinutes in start..end
    }
    val focusLiveProgress = remember(focusClass, focusClassIsLive, nowMinutes) {
        if (!focusClassIsLive || focusClass == null) return@remember null
        val start = scheduleParseMins(focusClass.startTime) ?: return@remember null
        val end = scheduleParseMins(focusClass.endTime) ?: return@remember null
        val total = (end - start).coerceAtLeast(1)
        ((nowMinutes - start).coerceIn(0, total).toFloat() / total.toFloat())
    }

    DashboardVibeContainer {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    ScheduleHeaderCard(
                        monthYear = monthYear,
                        selectedDateLabel = selectedDateLabel,
                        isExamWindowDay = isExamWindowDay,
                        examSubjects = examSubjectsOnSelectedDate,
                        examPlan = examPlan,
                        onOpenDatePicker = { showCalendarSheet = true },
                        onAddExtraClass = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            showAddExtraClassDialog = true
                        },
                        onEditWeeklySchedule = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onEditWeeklySchedule()
                        }
                    )
                }
                item {
                    ScheduleWeekStrip(
                        selectedDateCalendar = selectedDateCalendar,
                        todayDateKey = todayDateKey,
                        onSelectDate = { calendar ->
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedDateCalendar = calendar
                        }
                    )
                }
                item {
                    ScheduleFocusCard(
                        selectedDateLabel = selectedDateLabel,
                        isSelectedToday = isSelectedToday,
                        focusClass = focusClass,
                        focusClassIsLive = focusClassIsLive,
                        focusLiveProgress = focusLiveProgress,
                        isExamWindowDay = isExamWindowDay,
                        examSubjects = examSubjectsOnSelectedDate,
                        onJumpToToday = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedDateCalendar = now.clone() as Calendar
                        }
                    )
                }
                if (effectiveClasses.isEmpty()) {
                    item {
                        EmptyScheduleState(
                            isExamWindowDay = isExamWindowDay,
                            examSubjectsOnSelectedDate = examSubjectsOnSelectedDate,
                            examPlan = examPlan
                        )
                    }
                } else {
                    itemsIndexed(effectiveClasses, key = { _, c -> "${c.subject}${c.startTime}" }) { _, cls ->
                        val slotKey = "${cls.subject}:${cls.startTime}"
                        val attendanceUnits = remember(cls.startTime, cls.endTime) {
                            scheduleAttendanceUnits(cls.startTime, cls.endTime)
                        }
                        val durationMinutes = remember(cls.startTime, cls.endTime) {
                            scheduleDurationMinutes(cls.startTime, cls.endTime)
                        }
                        val isAbsent = absentSlots.contains(slotKey)
                        val isCanceled = canceledSlots.contains(slotKey)
                        val isPresent = presentSlots.contains(slotKey)

                        val isLive = remember(nowMinutes, cls, selectedDateKey, isCanceled) {
                            if (isCanceled) return@remember false
                            if (selectedDateKey != todayDateKey) return@remember false
                            val s = scheduleParseMins(cls.startTime) ?: return@remember false
                            val e = scheduleParseMins(cls.endTime) ?: return@remember false
                            nowMinutes in s..e
                        }
                        val isPastClass = remember(nowMinutes, selectedDateKey) {
                            val e = scheduleParseMins(cls.endTime) ?: return@remember false
                            if (selectedDateKey < todayDateKey) true
                            else if (selectedDateKey == todayDateKey) nowMinutes >= e
                            else false
                        }
                        val liveProgress = remember(isLive, cls.startTime, cls.endTime, nowMinutes) {
                            if (!isLive) return@remember null
                            val start = scheduleParseMins(cls.startTime) ?: return@remember null
                            val end = scheduleParseMins(cls.endTime) ?: return@remember null
                            val total = (end - start).coerceAtLeast(1)
                            ((nowMinutes - start).coerceIn(0, total).toFloat() / total.toFloat())
                        }

                        CleanScheduleCard(
                            cls = cls,
                            isAbsent = isAbsent,
                            isPresent = isPresent,
                            isLive = isLive,
                            isPastClass = isPastClass,
                            isCanceled = isCanceled,
                            durationMinutes = durationMinutes,
                            attendanceUnits = attendanceUnits,
                            liveProgress = liveProgress,
                            onMarkAbsent = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                appViewModel.markSlotAbsent(selectedDateKey, slotKey, cls.subject, attendanceUnits)
                            },
                            onMarkPresent = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                appViewModel.markSlotPresent(selectedDateKey, slotKey, cls.subject, attendanceUnits)
                            },
                            onUndoAbsent = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                appViewModel.undoSlotAbsent(selectedDateKey, slotKey, cls.subject, attendanceUnits)
                            },
                            onUndoPresent = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                appViewModel.undoSlotPresent(selectedDateKey, slotKey, cls.subject, attendanceUnits)
                            },
                            onMarkCanceled = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                appViewModel.markSlotCanceled(selectedDateKey, slotKey)
                            },
                            onUndoCanceled = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                appViewModel.undoSlotCanceled(selectedDateKey, slotKey)
                            }
                        )
                    }
                }
            }

            if (showCalendarSheet) {
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                ModalBottomSheet(
                    onDismissRequest = { showCalendarSheet = false },
                    sheetState = sheetState,
                    dragHandle = { BottomSheetDefaults.DragHandle() },
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Choose a date",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Tap a day to switch the agenda.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        MonthCalendarCard(
                            monthYear = monthYear,
                            selectedDateCalendar = selectedDateCalendar,
                            todayDateKey = todayDateKey,
                            monthGridDates = monthGridDates,
                            onPreviousMonth = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedDateCalendar = (selectedDateCalendar.clone() as Calendar).apply {
                                    add(Calendar.MONTH, -1)
                                    set(Calendar.DAY_OF_MONTH, 1)
                                }
                            },
                            onNextMonth = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedDateCalendar = (selectedDateCalendar.clone() as Calendar).apply {
                                    add(Calendar.MONTH, 1)
                                    set(Calendar.DAY_OF_MONTH, 1)
                                }
                            },
                            onSelectDate = { calendar ->
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedDateCalendar = calendar
                                showCalendarSheet = false
                            }
                        )
                    }
                }
            }

            if (showAddExtraClassDialog) {
                TemporaryClassDialog(
                    existingSubjects = existingSubjects,
                    onDismiss = { showAddExtraClassDialog = false },
                    onSave = { slot ->
                        appViewModel.addTemporaryClass(selectedDateKey, slot)
                        showAddExtraClassDialog = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ScheduleWeekStrip(
    selectedDateCalendar: Calendar,
    todayDateKey: String,
    onSelectDate: (Calendar) -> Unit
) {
    val selectedKey = remember(selectedDateCalendar) {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDateCalendar.time)
    }
    val weekDates = remember(selectedDateCalendar) {
        buildWeekStripDates(selectedDateCalendar)
    }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        itemsIndexed(weekDates) { _, date ->
            val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date.time)
            val isSelected = dateKey == selectedKey
            val isToday = dateKey == todayDateKey
            val containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            val contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

            Box(
                modifier = Modifier
                    .widthIn(min = 78.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(containerColor)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                    .pressScaleClickable(
                        pressedScale = 0.98f,
                        hapticType = HapticFeedbackType.TextHandleMove
                    ) { onSelectDate(date) }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = SimpleDateFormat("EEE", Locale.getDefault()).format(date.time),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = contentColor
                    )
                    Text(
                        text = date.get(Calendar.DAY_OF_MONTH).toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                        color = contentColor.copy(alpha = if (isSelected || isToday) 1f else 0.72f)
                    )
                    if (isToday) {
                        Spacer(Modifier.height(2.dp))
                        Box(
                            Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Color.White else MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleFocusCard(
    selectedDateLabel: String,
    isSelectedToday: Boolean,
    focusClass: ClassSlot?,
    focusClassIsLive: Boolean,
    focusLiveProgress: Float?,
    isExamWindowDay: Boolean,
    examSubjects: List<String>,
    onJumpToToday: () -> Unit
) {
    val title = when {
        isExamWindowDay && examSubjects.isNotEmpty() -> "Exam focus"
        focusClassIsLive -> "Live now"
        isSelectedToday && focusClass != null -> "Up next"
        focusClass != null -> "First class"
        else -> "No classes planned"
    }
    val subtitle = when {
        isExamWindowDay && examSubjects.isNotEmpty() -> examSubjects.joinToString(", ")
        focusClass != null -> "${focusClass.subject} • ${focusClass.startTime.to12Hour()} - ${focusClass.endTime.to12Hour()}"
        else -> "Nothing scheduled for $selectedDateLabel"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
                        )
                    )
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            if (focusClassIsLive && focusLiveProgress != null) {
                Text(
                    text = "Live notification active",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.SemiBold
                )
                LinearProgressIndicator(
                    progress = { focusLiveProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(999.dp)),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f),
                    strokeCap = ProgressIndicatorDefaults.LinearStrokeCap
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when {
                        isExamWindowDay && examSubjects.isNotEmpty() -> "Use this day for revision and exam prep."
                        focusClass != null -> "${focusClass.room} • ${focusClass.faculty ?: "Faculty TBA"}"
                        else -> "Add an extra class or switch to another day."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                if (!isSelectedToday) {
                    Spacer(modifier = Modifier.width(12.dp))
                    TextButton(onClick = onJumpToToday) {
                        Text("Today")
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthCalendarCard(
    monthYear: String,
    selectedDateCalendar: Calendar,
    todayDateKey: String,
    monthGridDates: List<Calendar>,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectDate: (Calendar) -> Unit
) {
    val selectedKey = remember(selectedDateCalendar) {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDateCalendar.time)
    }
    val selectedMonth = selectedDateCalendar.get(Calendar.MONTH)
    val weekdayLabels = remember { listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousMonth) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous month")
                }
                Text(
                    text = monthYear,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onNextMonth) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next month")
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                weekdayLabels.forEach { label ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            monthGridDates.chunked(7).forEach { week ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    week.forEach { date ->
                        val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date.time)
                        val isToday = dateKey == todayDateKey
                        val isSelected = dateKey == selectedKey
                        val inCurrentMonth = date.get(Calendar.MONTH) == selectedMonth
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(18.dp),
                            color = when {
                                isSelected -> MaterialTheme.colorScheme.primaryContainer
                                isToday -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.92f)
                                else -> Color.Transparent
                            },
                            border = BorderStroke(
                                1.dp,
                                when {
                                    isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.38f)
                                    isToday -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.24f)
                                    else -> Color.Transparent
                                }
                            ),
                            onClick = { onSelectDate(date) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = date.get(Calendar.DAY_OF_MONTH).toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                                    color = when {
                                        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                                        isToday -> MaterialTheme.colorScheme.onSecondaryContainer
                                        inCurrentMonth -> MaterialTheme.colorScheme.onSurface
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleHeaderCard(
    monthYear: String,
    selectedDateLabel: String,
    isExamWindowDay: Boolean,
    examSubjects: List<String>,
    @Suppress("UNUSED_PARAMETER") examPlan: ExamPlan,
    onOpenDatePicker: () -> Unit,
    onAddExtraClass: () -> Unit,
    onEditWeeklySchedule: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.68f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
                        )
                    )
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .pressScaleClickable(pressedScale = 0.98f, onClick = onOpenDatePicker)
                ) {
                    Text(
                        text = "TACTICAL VIEW",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Schedule",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = selectedDateLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    onClick = onOpenDatePicker,
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = monthYear,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Tap to change",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f)
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ScheduleActionButton(
                    modifier = Modifier.weight(1f),
                    label = "Add class",
                    icon = Icons.Default.Add,
                    onClick = onAddExtraClass
                )
                ScheduleActionButton(
                    modifier = Modifier.weight(1f),
                    label = "Edit week",
                    icon = Icons.Default.Edit,
                    onClick = onEditWeeklySchedule
                )
            }

            if (isExamWindowDay && examSubjects.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventAvailable,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = "Exam day: ${examSubjects.joinToString(", ")}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "Classes and attendance",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Today,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Tap a day to switch",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleActionButton(
    modifier: Modifier = Modifier,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .pressScaleClickable(pressedScale = 0.96f, onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 13.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.94f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ScheduleDayChip(
    day: AppDay,
    dateNum: Int,
    isToday: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = when {
            isSelected -> MaterialTheme.colorScheme.primaryContainer
            isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
            else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
        },
        border = BorderStroke(
            1.dp,
            when {
                isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.42f)
                isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .width(62.dp)
                .pressScaleClickable(pressedScale = 0.96f, onClick = onClick)
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = day.shortName.take(3),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = dateNum.toString(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isSelected -> MaterialTheme.colorScheme.primary
                            isToday -> MaterialTheme.colorScheme.primary
                            else -> Color.Transparent
                        }
                    )
            )
        }
    }
}

@Composable
private fun EmptyScheduleState(
    isExamWindowDay: Boolean,
    examSubjectsOnSelectedDate: List<String>,
    examPlan: ExamPlan
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(30.dp)
                )
            }
            Text(
                text = if (isExamWindowDay) "No classes scheduled" else "You’re free today",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (isExamWindowDay) {
                    if (examSubjectsOnSelectedDate.isEmpty()) {
                        "Exam window active"
                    } else {
                        "Exam: ${examSubjectsOnSelectedDate.joinToString(", ")} • ${examPlan.commonStartTime.to12Hour()} - ${examPlan.commonEndTime.to12Hour()}"
                    }
                } else {
                    "No classes are scheduled for this day."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CleanScheduleCard(
    cls: ClassSlot,
    isAbsent: Boolean,
    isPresent: Boolean,
    isLive: Boolean,
    isPastClass: Boolean,
    isCanceled: Boolean,
    durationMinutes: Int,
    attendanceUnits: Int,
    liveProgress: Float?,
    onMarkAbsent: () -> Unit,
    onMarkPresent: () -> Unit,
    onUndoAbsent: () -> Unit,
    onUndoPresent: () -> Unit,
    onMarkCanceled: () -> Unit,
    onUndoCanceled: () -> Unit
) {
    val stripeColor = when {
        isCanceled -> ErrorRed.copy(alpha = 0.65f)
        isLive -> MaterialTheme.colorScheme.secondary
        isPastClass || isAbsent || isPresent -> MaterialTheme.colorScheme.outline.copy(alpha = 0.42f)
        else -> MaterialTheme.colorScheme.primary
    }
    val textAlpha = if (isCanceled || (isPastClass && !isLive && !isAbsent && !isPresent)) 0.55f else 1f

    val livePulse = 0.22f

    val cardBorderColor = when {
        isLive && !isCanceled -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)
        isCanceled -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    }

    val gradientColors = when {
        isCanceled -> listOf(
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.surface
        )
        isLive -> listOf(
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.11f),
            MaterialTheme.colorScheme.surface
        )
        else -> listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.04f),
            MaterialTheme.colorScheme.surface
        )
    }

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 6.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, cardBorderColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.linearGradient(colors = gradientColors))
            ) {
                Box(
                    Modifier
                        .width(6.dp)
                        .fillMaxHeight()
                        .clip(
                            RoundedCornerShape(
                                topStart = 20.dp,
                                bottomStart = 20.dp
                            )
                        )
                        .background(stripeColor)
                )

        Column(Modifier.padding(16.dp).fillMaxWidth()) {
            // Top row: badges + time + cancel button
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status badge
                    val (badgeText, badgeBg, badgeFg) = when {
                        isCanceled -> Triple("CANCELED", ErrorRed.copy(alpha = 0.12f), ErrorRed)
                        isLive -> Triple("LIVE", MaterialTheme.colorScheme.secondary.copy(alpha = livePulse), MaterialTheme.colorScheme.secondary)
                        else -> Triple((cls.type ?: "Lecture").uppercase(), MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f), MaterialTheme.colorScheme.secondary)
                    }
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(badgeBg)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            badgeText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = badgeFg,
                            fontSize = 9.sp
                        )
                    }
                    if (isAbsent && !isCanceled) {
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(ErrorRed.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                "ABSENT",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = ErrorRed,
                                fontSize = 9.sp
                            )
                        }
                    } else if (isPresent && !isCanceled) {
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                "PRESENT",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "${cls.startTime.to12Hour()} – ${cls.endTime.to12Hour()}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = if (isLive && !isCanceled) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = textAlpha)
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .clickable { if (isCanceled) onUndoCanceled() else onMarkCanceled() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCanceled) {
                            Icon(
                                Icons.Default.Restore,
                                null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Icon(
                                Icons.Default.Block,
                                null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Subject name
            Text(
                cls.subject,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = textAlpha)
            )

            Spacer(Modifier.height(6.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        "$durationMinutes min",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = textAlpha)
                    )
                }
                if (attendanceUnits > 1) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "$attendanceUnits classes",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = textAlpha)
                        )
                    }
                }
            }

            if (liveProgress != null && !isCanceled) {
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { liveProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(999.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                    strokeCap = ProgressIndicatorDefaults.LinearStrokeCap
                )
            }

            Spacer(Modifier.height(8.dp))

            // Room + Faculty
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = textAlpha)
                    )
                    Text(
                        cls.room,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = textAlpha)
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = textAlpha)
                    )
                    Text(
                        cls.faculty ?: "TBA",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = textAlpha)
                    )
                }
            }

            // Attendance buttons
            if (isPastClass && !isCanceled) {
                Spacer(Modifier.height(14.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)) {
                    if (isAbsent) {
                        // Show ONLY Undo Absent with enhanced styling
                        Button(
                            onClick = onUndoAbsent,
                            modifier = Modifier.height(40.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 2.dp,
                                pressedElevation = 6.dp
                            )
                        ) {
                            Icon(
                                Icons.Default.Restore,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Undo Absent",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else if (isPresent) {
                        // Show ONLY Undo Present with enhanced styling
                        Button(
                            onClick = onUndoPresent,
                            modifier = Modifier.height(40.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 2.dp,
                                pressedElevation = 6.dp
                            )
                        ) {
                            Icon(
                                Icons.Default.Restore,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Undo Present",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        // Show BOTH Mark Present and Mark Absent with enhanced styling
                        OutlinedButton(
                            onClick = onMarkAbsent,
                            modifier = Modifier.height(40.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = ErrorRed
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                ErrorRed.copy(alpha = 0.8f)
                            )
                        ) {
                            Icon(
                                Icons.Default.Block,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Mark Absent",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Button(
                            onClick = onMarkPresent,
                            modifier = Modifier.height(40.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 3.dp,
                                pressedElevation = 7.dp,
                                hoveredElevation = 5.dp
                            )
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Mark Present",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
            }
        }
    }
}

@Composable
private fun TemporaryClassDialog(
    existingSubjects: Collection<String>,
    onDismiss: () -> Unit,
    onSave: (ClassSlot) -> Unit
) {
    var subject by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("14:00") }
    var endTime by remember { mutableStateOf("15:00") }
    var room by remember { mutableStateOf("L-15") }
    var faculty by remember { mutableStateOf("") }
    val subjectSuggestions = remember(subject, existingSubjects) {
        subjectSuggestions(subject, existingSubjects)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val trimmedSubject = subject.trim()
                    if (trimmedSubject.isEmpty()) return@TextButton
                    onSave(
                        ClassSlot(
                            subject = canonicalizeSubjectName(trimmedSubject, existingSubjects),
                            time = "${startTime.to12Hour()} - ${endTime.to12Hour()}",
                            startTime = startTime,
                            endTime = endTime,
                            room = room.trim().ifEmpty { "TBA" },
                            icon = "add",
                            iconBg = "bg-blue-500/10",
                            iconColor = "text-blue-500",
                            type = "Extra",
                            faculty = faculty.trim().ifEmpty { "TBA" }
                        )
                    )
                },
                enabled = subject.trim().isNotEmpty()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = {
            Text("Add Extra Class", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SubjectInputField(
                    value = subject,
                    onValueChange = { subject = it },
                    suggestions = subjectSuggestions,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Start") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("End") },
                        singleLine = true
                    )
                }
                OutlinedTextField(
                    value = room,
                    onValueChange = { room = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Room") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = faculty,
                    onValueChange = { faculty = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Faculty") },
                    singleLine = true
                )
            }
        }
    )
}

private fun scheduleParseMins(t: String): Int? {
    val p = t.split(":")
    if (p.size != 2) return null
    val h = p[0].toIntOrNull() ?: return null
    val m = p[1].toIntOrNull() ?: return null
    return h * 60 + m
}

private fun scheduleAttendanceUnits(startTime: String, endTime: String): Int {
    val minutes = scheduleDurationMinutes(startTime, endTime)
    return kotlin.math.ceil(minutes / 50.0).toInt().coerceAtLeast(1)
}

private fun scheduleDurationMinutes(startTime: String, endTime: String): Int {
    val start = scheduleParseMins(startTime) ?: return 1
    val end = scheduleParseMins(endTime) ?: return 1
    return (end - start).coerceAtLeast(1)
}

private fun startOfDayMillis(millis: Long): Long {
    val cal = Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return cal.timeInMillis
}

private fun buildMonthGridDates(selectedDateCalendar: Calendar): List<Calendar> {
    val monthStart = (selectedDateCalendar.clone() as Calendar).apply {
        set(Calendar.DAY_OF_MONTH, 1)
    }
    val offset = ((monthStart.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY) + 7) % 7
    monthStart.add(Calendar.DAY_OF_YEAR, -offset)
    return List(42) { index ->
        (monthStart.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, index)
        }
    }
}

private fun buildWeekStripDates(selectedDateCalendar: Calendar): List<Calendar> {
    val startOfWeek = (selectedDateCalendar.clone() as Calendar).apply {
        firstDayOfWeek = Calendar.MONDAY
        set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    }
    return List(7) { offset ->
        (startOfWeek.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, offset)
        }
    }
}

private fun isInExamWindow(plan: ExamPlan, dayMillis: Long): Boolean {
    val start = plan.startDateMillis ?: return false
    val end = plan.endDateMillis ?: return false
    val normalizedStart = startOfDayMillis(start)
    val normalizedEnd = startOfDayMillis(end)
    return dayMillis in normalizedStart..normalizedEnd
}
