package com.example.timetable.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.timetable.data.AppDay
import com.example.timetable.data.ExamPlan
import com.example.timetable.data.ClassSlot
import com.example.timetable.data.SavedNotification
import com.example.timetable.data.decodeStoredNote
import com.example.timetable.data.encodeStoredNote
import com.example.timetable.data.to12Hour
import com.example.timetable.ui.AppViewModel
import com.example.timetable.ui.components.pressScaleClickable
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Date
import com.example.timetable.data.AppFullDay
import com.example.timetable.data.AppMeal
import com.example.timetable.data.AppMealType
import com.example.timetable.data.AssignmentItem
import com.example.timetable.data.AssignmentStatus
import java.util.Locale
import com.example.timetable.ui.components.DashboardVibeContainer
import androidx.compose.foundation.BorderStroke

@Composable
fun DashboardScreen(
    appViewModel: AppViewModel,
    onOpenMenu: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToReminderSettings: () -> Unit,
    onNavigateToMessMenu: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToSyllabus: () -> Unit,
    onNavigateToExamPlanner: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onNavigateToNotifications: () -> Unit
) {
    // Keep "LIVE NOW / UP NEXT" accurate while the screen is open.
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (isActive) {
            val current = System.currentTimeMillis()
            // Align updates to the next minute boundary for stable time calculations.
            val delayMs = 60_000L - (current % 60_000L)
            delay(delayMs + 50L)
            nowMillis = System.currentTimeMillis()
        }
    }
    val now = remember(nowMillis) {
        Calendar.getInstance().apply { timeInMillis = nowMillis }
    }
    val nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
    val day = todayDay(now.get(Calendar.DAY_OF_WEEK))

    val timetable by appViewModel.timetable.collectAsStateWithLifecycle()
    val showMessMenuCard by appViewModel.showMessMenuCard.collectAsStateWithLifecycle()
    val messPref by appViewModel.messPref.collectAsStateWithLifecycle()
    val messMenu by appViewModel.messMenu.collectAsStateWithLifecycle()
    val remindersEnabled by appViewModel.remindersEnabled.collectAsStateWithLifecycle()
    val reminderMinutesBefore by appViewModel.reminderMinutesBefore.collectAsStateWithLifecycle()
    val examPlan by appViewModel.examPlan.collectAsStateWithLifecycle()
    val assignments by appViewModel.assignments.collectAsStateWithLifecycle()
    val dailyPresentLogs by appViewModel.dailyPresentLogs.collectAsStateWithLifecycle()
    val classNotes by appViewModel.classNotes.collectAsStateWithLifecycle()
    val savedNotifications by appViewModel.savedNotifications.collectAsStateWithLifecycle()
    val canceledClasses by appViewModel.canceledClasses.collectAsStateWithLifecycle()
    val userName by appViewModel.userName.collectAsStateWithLifecycle()
    var showNameDialog by remember { mutableStateOf(false) }
    var nameDraft by remember(userName) { mutableStateOf(userName) }

    val todayDateKey = remember(nowMillis) {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now.time)
    }
    val todayCanceledSlots = remember(canceledClasses, todayDateKey) {
        canceledClasses[todayDateKey].orEmpty()
    }
    val todayStartMillis = remember(nowMillis) { startOfDayMillis(nowMillis) }
    val isExamWindowActive = remember(examPlan, todayStartMillis) {
        isInExamWindow(examPlan, todayStartMillis)
    }
    val todayExamSubjects = remember(examPlan, todayStartMillis) {
        examPlan.subjectExamDates
            .filterValues { startOfDayMillis(it) == todayStartMillis }
            .keys
            .toList()
    }
    val examTimeText = remember(examPlan.commonStartTime, examPlan.commonEndTime) {
        "${examPlan.commonStartTime.to12Hour()} - ${examPlan.commonEndTime.to12Hour()}"
    }
    val nextExam = remember(examPlan, nowMillis) {
        nextExamInfo(examPlan, nowMillis)
    }
    val nextExamStartsIn = remember(nextExam, nowMillis) {
        nextExam?.startMillis?.let { formatStartsIn(it, nowMillis) }
    }
    val nextExamDateTime = remember(nextExam) {
        nextExam?.startMillis?.let { formatExamDateTime(it) }
    }

    val rawTodayClasses = remember(day, timetable) { day?.let { timetable[it].orEmpty() } ?: emptyList() }
    val todayClasses = if (isExamWindowActive) emptyList() else rawTodayClasses
    val currentClass = remember(nowMinutes, todayClasses, todayCanceledSlots, isExamWindowActive) {
        if (isExamWindowActive) return@remember null
        todayClasses.firstOrNull { slot ->
            val slotKey = "${slot.subject}:${slot.startTime}"
            if (slotKey in todayCanceledSlots) return@firstOrNull false
            val start = parseTimeMins(slot.startTime) ?: return@firstOrNull false
            val end = parseTimeMins(slot.endTime) ?: return@firstOrNull false
            nowMinutes in start..end
        }
    }
    val nextClass = remember(nowMinutes, todayClasses, todayCanceledSlots, isExamWindowActive) {
        if (isExamWindowActive) return@remember null
        todayClasses.firstOrNull { slot ->
            val slotKey = "${slot.subject}:${slot.startTime}"
            if (slotKey in todayCanceledSlots) return@firstOrNull false
            val start = parseTimeMins(slot.startTime) ?: return@firstOrNull false
            start > nowMinutes
        }
    }
    val liveSlotKey = remember(currentClass) { currentClass?.let { "${it.subject}:${it.startTime}" } }
    val liveNoteKey = remember(todayDateKey, liveSlotKey) { liveSlotKey?.let { "${todayDateKey}_$it" } }
    val isLiveAttendanceMarked = remember(dailyPresentLogs, todayDateKey, liveSlotKey) {
        liveSlotKey != null && dailyPresentLogs[todayDateKey].orEmpty().contains(liveSlotKey)
    }
    val liveClassNotePayload = remember(classNotes, liveNoteKey) {
        liveNoteKey?.let { key -> classNotes[key]?.let(::decodeStoredNote) }
    }
    val liveClassNote = liveClassNotePayload?.body.orEmpty()
    val todayFullDay = remember(now.get(Calendar.DAY_OF_WEEK)) {
        when (now.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> AppFullDay.Mon
            Calendar.TUESDAY -> AppFullDay.Tue
            Calendar.WEDNESDAY -> AppFullDay.Wed
            Calendar.THURSDAY -> AppFullDay.Thu
            Calendar.FRIDAY -> AppFullDay.Fri
            Calendar.SATURDAY -> AppFullDay.Sat
            Calendar.SUNDAY -> AppFullDay.Sun
            else -> AppFullDay.Mon
        }
    }

    val todayMeals = remember(todayFullDay, messMenu) { messMenu[todayFullDay] ?: emptyList() }
    val activeMeal = remember(nowMinutes, todayMeals) {
        // Breakfast: 7:30 - 9:00 (450 - 540)
        // Lunch: 12:00 - 14:00 (720 - 840)
        // Dinner: 19:30 - 21:00 (1170 - 1260)
        when {
            nowMinutes in 450..540 -> todayMeals.find { it.type == AppMealType.breakfast }
            nowMinutes in 720..840 -> todayMeals.find { it.type == AppMealType.lunch }
            nowMinutes in 1170..1260 -> todayMeals.find { it.type == AppMealType.dinner }
            else -> null
        }
    }
    val upcomingMeal = remember(nowMinutes, todayMeals, activeMeal) {
        if (activeMeal != null) activeMeal else {
            when {
                nowMinutes < 450 -> todayMeals.find { it.type == AppMealType.breakfast }
                nowMinutes < 720 -> todayMeals.find { it.type == AppMealType.lunch }
                nowMinutes < 1170 -> todayMeals.find { it.type == AppMealType.dinner }
                else -> null
            }
        }
    }
    val todayNotesCount = remember(classNotes, todayDateKey) {
        classNotes.keys.count { it.startsWith("${todayDateKey}_") }
    }
    DashboardVibeContainer(headerHeight = 180.dp) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                DashboardTopActions(
                    onOpenMenu = onOpenMenu,
                    onOpenProfile = {
                        nameDraft = userName
                        onNavigateToSettings()
                    }
                )
            }
            item {
                TodayClassesCard(
                    classes = todayClasses,
                    canceledSlots = todayCanceledSlots,
                    isExamWindowActive = isExamWindowActive,
                    examSubjectsToday = todayExamSubjects,
                    examTimeText = examTimeText
                )
            }
            item {
                HomeShortcutStrip(
                    onNavigateToNotes = onNavigateToNotes,
                    onNavigateToReminderSettings = onNavigateToReminderSettings,
                    onNavigateToNotifications = onNavigateToNotifications,
                    onNavigateToSyllabus = onNavigateToSyllabus
                )
            }
            val isNotesVisible = currentClass != null && liveNoteKey != null
            item {
                AnimatedVisibility(
                    visible = isNotesVisible,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    if (isNotesVisible) {
                        OngoingClassNotesCard(
                            currentClass = currentClass!!,
                            initialNote = liveClassNote,
                            onSave = { noteText ->
                                val pinned = liveClassNotePayload?.pinned ?: false
                                appViewModel.setClassNote(liveNoteKey!!, encodeStoredNote(noteText, pinned))
                            }
                        )
                    }
                }
            }

            val isNowServingVisible = showMessMenuCard && activeMeal != null
            item {
                AnimatedVisibility(
                    visible = isNowServingVisible,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    if (isNowServingVisible) {
                        NowServingCard(meal = activeMeal!!, messPref = messPref)
                    }
                }
            }
        }

        if (showNameDialog) {
            AlertDialog(
                onDismissRequest = { showNameDialog = false },
                title = { Text("Enter your name", fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = nameDraft,
                        onValueChange = { nameDraft = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Your name") }
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            appViewModel.setUserName(nameDraft)
                            showNameDialog = false
                        }
                    ) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = { showNameDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
private fun DashboardTopActions(
    onOpenMenu: () -> Unit,
    onOpenProfile: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f))
        ) {
            Box(
                modifier = Modifier
                    .pressScaleClickable(pressedScale = 0.98f, onClick = onOpenMenu)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f))
        ) {
            Box(
                modifier = Modifier
                    .pressScaleClickable(pressedScale = 0.98f, onClick = onOpenProfile)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Profile",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

private data class QuickActionItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accent: List<Color>,
    val onClick: () -> Unit
)

private fun classCountLabel(count: Int, suffix: String): String {
    val noun = if (count == 1) "class" else "classes"
    return "$count $noun $suffix"
}

@Composable
private fun OngoingClassNotesCard(
    currentClass: ClassSlot,
    initialNote: String,
    onSave: (String) -> Unit
) {
    var noteText by remember(initialNote) { mutableStateOf(initialNote) }
    LaunchedEffect(initialNote) {
        noteText = initialNote
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NoteAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Class Notes",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = currentClass.subject,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(18.dp),
                placeholder = { Text("Add quick notes for the ongoing class") }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (noteText.isBlank()) "Capture summary, doubts, or assignment details" else "Saved notes appear in your Notes page",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = { onSave(noteText) },
                    enabled = noteText.isNotBlank(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Save Notes")
                }
            }
        }
    }
}

@Composable
private fun HeroPill(label: String, containerColor: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(containerColor)
            .padding(horizontal = 11.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun HomeMosaicSection(
    examSubtitle: String,
    reminderSubtitle: String,
    notesSubtitle: String,
    messSubtitle: String,
    analyticsSubtitle: String,
    syllabusSubtitle: String,
    onNavigateToExamPlanner: () -> Unit,
    onNavigateToReminderSettings: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onNavigateToMessMenu: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToSyllabus: () -> Unit,
    onNavigateToNotifications: () -> Unit
) {
    var actionsVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        actionsVisible = true
    }
    val items = listOf(
        QuickActionItem("Notes", notesSubtitle, Icons.Default.NoteAlt, listOf(Color(0xFFEEF2FF), Color(0xFFE0F2FE)), onNavigateToNotes),
        QuickActionItem("Planner", examSubtitle, Icons.Default.Event, listOf(Color(0xFFFFF7ED), Color(0xFFFFEDD5)), onNavigateToExamPlanner),
        QuickActionItem("Analytics", analyticsSubtitle, Icons.Default.Analytics, listOf(Color(0xFFECFDF5), Color(0xFFD1FAE5)), onNavigateToAnalytics),
        QuickActionItem("Syllabus", syllabusSubtitle, Icons.AutoMirrored.Filled.MenuBook, listOf(Color(0xFFF5F3FF), Color(0xFFEDE9FE)), onNavigateToSyllabus),
        QuickActionItem("Reminders", reminderSubtitle, Icons.Default.Notifications, listOf(Color(0xFFFEF2F2), Color(0xFFFFE4E6)), onNavigateToReminderSettings),
        QuickActionItem("Mess", messSubtitle, Icons.Default.Restaurant, listOf(Color(0xFFFEFCE8), Color(0xFFECFCCB)), onNavigateToMessMenu),
        QuickActionItem("Notifications", "Recent updates and alerts", Icons.Default.Sensors, listOf(Color(0xFFF0FDFA), Color(0xFFCCFBF1)), onNavigateToNotifications),
        QuickActionItem("Books", "Study PDFs and references", Icons.Default.Book, listOf(Color(0xFFF8FAFC), Color(0xFFE2E8F0)), onNavigateToSyllabus)
    )
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
        )
        Text(
            text = "The most useful parts of the app, prioritized for today",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            items.chunked(2).forEachIndexed { index, rowItems ->
                AnimatedVisibility(
                    visible = actionsVisible,
                    enter = fadeIn(
                        animationSpec = tween(
                            durationMillis = 420,
                            delayMillis = 60 + (index * 70)
                        )
                    ) + slideInVertically(
                        initialOffsetY = { it / 5 },
                        animationSpec = tween(
                            durationMillis = 420,
                            delayMillis = 60 + (index * 70)
                        )
                    ) + scaleIn(
                        initialScale = 0.96f,
                        animationSpec = tween(
                            durationMillis = 420,
                            delayMillis = 60 + (index * 70)
                        )
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        rowItems.forEach { item ->
                            Box(modifier = Modifier.weight(1f)) {
                                HomeActionCard(
                                    title = item.title,
                                    subtitle = item.subtitle,
                                    icon = item.icon,
                                    accent = item.accent,
                                    onClick = item.onClick
                                )
                            }
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: List<Color>,
    onClick: () -> Unit
) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val containerBrush = if (isDarkTheme) {
        Brush.linearGradient(
            listOf(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.98f),
                MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                MaterialTheme.colorScheme.surface,
                MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
            )
        )
    }
    val accentGlow = if (isDarkTheme) {
        accent.map { it.copy(alpha = 0.28f) }
    } else {
        accent.map { it.copy(alpha = 0.85f) }
    }
    val titleColor = MaterialTheme.colorScheme.onSurface
    val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant
    val iconChipColor = if (isDarkTheme) {
        accent.first().copy(alpha = 0.22f)
    } else {
        Color.White.copy(alpha = 0.75f)
    }
    val iconTint = if (isDarkTheme) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    val arrowTint = if (isDarkTheme) accent.first().copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = if (isDarkTheme) accent.first().copy(alpha = 0.34f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)

    Box(
        modifier = Modifier
            .height(132.dp)
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(26.dp),
                ambientColor = if (isDarkTheme) accent.first().copy(alpha = 0.18f) else Color.Black.copy(alpha = 0.12f),
                spotColor = if (isDarkTheme) accent.last().copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)
            )
            .clip(RoundedCornerShape(26.dp))
            .background(containerBrush)
            .border(
                BorderStroke(1.dp, borderColor),
                RoundedCornerShape(26.dp)
            )
            .pressScaleClickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = accentGlow + Color.Transparent,
                        radius = 520f
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = if (isDarkTheme) {
                            listOf(
                                Color.White.copy(alpha = 0.04f),
                                Color.Transparent,
                                accent.last().copy(alpha = 0.10f)
                            )
                        } else {
                            accentGlow
                        }
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(iconChipColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = arrowTint
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = titleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = subtitleColor,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun TodayClassesCard(
    classes: List<ClassSlot>,
    canceledSlots: Set<String> = emptySet(),
    isExamWindowActive: Boolean,
    examSubjectsToday: List<String>,
    examTimeText: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = "Today's classes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isExamWindowActive) "Schedule paused for exams" else classCountLabel(classes.size, "planned"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (isExamWindowActive) {
                Text(
                    text = "Classes are paused while exams are active.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (examSubjectsToday.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.64f))
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            HeroPill(
                                label = "EXAM MODE",
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                                textColor = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Exam: ${examSubjectsToday.joinToString(", ")}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Time: $examTimeText",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else if (classes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f))
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "No classes scheduled today.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                classes.take(3).forEach { cls ->
                    val slotKey = "${cls.subject}:${cls.startTime}"
                    val isCanceled = slotKey in canceledSlots
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.54f))
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = cls.subject,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isCanceled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                )
                                if (isCanceled) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "CANCELED",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "${cls.startTime.to12Hour()} - ${cls.endTime.to12Hour()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isCanceled) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${cls.room} • ${cls.faculty ?: "TBA"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (cls != classes.take(3).last()) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 2.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                        )
                    }
                }
                if (classes.size > 3) {
                    Text(
                        text = "+ ${classes.size - 3} more classes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HomeShortcutStrip(
    onNavigateToNotes: () -> Unit,
    onNavigateToReminderSettings: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToSyllabus: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Shortcuts",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ShortcutChip(
                label = "Notes",
                icon = Icons.Default.NoteAlt,
                onClick = onNavigateToNotes
            )
            ShortcutChip(
                label = "Reminders",
                icon = Icons.Default.Notifications,
                onClick = onNavigateToReminderSettings
            )
            ShortcutChip(
                label = "Notifications",
                icon = Icons.Default.Sensors,
                onClick = onNavigateToNotifications
            )
            ShortcutChip(
                label = "Syllabus",
                icon = Icons.AutoMirrored.Filled.MenuBook,
                onClick = onNavigateToSyllabus
            )
        }
    }
}

@Composable
private fun ShortcutChip(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f))
    ) {
        Row(
            modifier = Modifier
                .pressScaleClickable(pressedScale = 0.98f, onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private enum class NotificationsPreviewFilter { All, Admin }

@Composable
private fun SavedNotificationsCard(
    notifications: List<SavedNotification>,
    onViewAll: () -> Unit
) {
    var filter by remember { mutableStateOf(NotificationsPreviewFilter.All) }
    val adminCount = notifications.count { it.isAdmin }
    val visible = remember(notifications, filter) {
        val list = when (filter) {
            NotificationsPreviewFilter.All -> notifications
            NotificationsPreviewFilter.Admin -> notifications.filter { it.isAdmin }
        }
        list.sortedByDescending { it.receivedAtMillis }
    }
    val preview = visible.take(4)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Saved Notifications",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (adminCount > 0) "${notifications.size} saved • $adminCount admin" else "${notifications.size} saved",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                TextButton(onClick = onViewAll) {
                    Text("View all")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterChip(
                    selected = filter == NotificationsPreviewFilter.All,
                    onClick = { filter = NotificationsPreviewFilter.All },
                    label = { Text("All (${notifications.size})") }
                )
                FilterChip(
                    selected = filter == NotificationsPreviewFilter.Admin,
                    onClick = { filter = NotificationsPreviewFilter.Admin },
                    label = { Text("Admin ($adminCount)") }
                )
            }

            if (preview.isEmpty()) {
                Text(
                    text = if (filter == NotificationsPreviewFilter.Admin) "No admin notifications yet." else "No saved notifications yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                preview.forEach { notif ->
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val badgeColor = if (notif.isAdmin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(badgeColor.copy(alpha = 0.14f))
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = if (notif.isAdmin) "ADMIN" else "ALL",
                                    color = badgeColor,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = formatNotifTime(notif.receivedAtMillis),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        Text(
                            text = notif.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = notif.body,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                    }
                }
            }
        }
    }
}

private fun formatNotifTime(millis: Long): String {
    return SimpleDateFormat("EEE, hh:mm a", Locale.getDefault()).format(Date(millis))
}

@Composable
private fun SectionHeading(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            style = MaterialTheme.typography.titleSmall
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
            thickness = 1.dp
        )
    }
}

@Composable
private fun CurrentStatusCard(
    currentClass: ClassSlot?,
    nextClass: ClassSlot?,
    nowMinutes: Int,
    isExamWindowActive: Boolean,
    examSubjectsToday: List<String>,
    examTimeText: String,
    nextExamSubject: String?,
    nextExamStartsIn: String?,
    nextExamDateTime: String?,
    isAttendanceMarked: Boolean,
    onMarkAttendance: () -> Unit
) {
    if (isExamWindowActive) {
        val primary = MaterialTheme.colorScheme.primary
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .animateContentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(primary, primary.copy(alpha = 0.9f))
                    )
                )
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                BadgePill(text = "EXAM MODE", isLive = false)
                Text(
                    text = nextExamSubject ?: "No upcoming exam",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (nextExamSubject != null && nextExamStartsIn != null) {
                    Text(
                        text = "Starts in $nextExamStartsIn",
                        color = Color.White.copy(alpha = 0.94f),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = nextExamDateTime ?: examTimeText,
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Text(
                        text = if (examSubjectsToday.isNotEmpty()) {
                            "Today's exam: ${examSubjectsToday.joinToString(", ")} • $examTimeText"
                        } else {
                            "No subject exam scheduled."
                        },
                        color = Color.White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
        return
    }

    val showClass = currentClass ?: nextClass
    val remainingMins = when {
        currentClass != null -> {
            val end = parseTimeMins(currentClass.endTime) ?: nowMinutes
            (end - nowMinutes).coerceAtLeast(0)
        }
        nextClass != null -> {
            val start = parseTimeMins(nextClass.startTime) ?: nowMinutes
            (start - nowMinutes).coerceAtLeast(0)
        }
        else -> 0
    }

    val primary = MaterialTheme.colorScheme.primary

    val progressFraction = remember(currentClass, nowMinutes) {
        if (currentClass == null) return@remember 0f
        val start = parseTimeMins(currentClass.startTime) ?: return@remember 0f
        val end = parseTimeMins(currentClass.endTime) ?: return@remember 0f
        val duration = (end - start).coerceAtLeast(1)
        val elapsed = (nowMinutes - start).coerceIn(0, duration)
        (elapsed.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
    }
    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        label = "classProgress"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .animateContentSize()
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.92f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.98f)
                    )
                )
            )
            .padding(24.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (currentClass != null) "Live Now" else "Up Next",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "$remainingMins mins",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = showClass?.subject ?: "No Classes Scheduled",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = showClass?.let { "Room ${it.room}" } ?: "No room assigned",
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            if (currentClass != null) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onPrimary,
                    trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HeroPill(
                        label = "Now",
                        containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.14f),
                        textColor = MaterialTheme.colorScheme.onPrimary
                    )
                    HeroPill(
                        label = "${currentClass.startTime.to12Hour()} - ${currentClass.endTime.to12Hour()}",
                        containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.14f),
                        textColor = MaterialTheme.colorScheme.onPrimary
                    )
                    HeroPill(
                        label = if (isAttendanceMarked) "Attendance marked" else "Attendance pending",
                        containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.14f),
                        textColor = MaterialTheme.colorScheme.onPrimary
                    )
                }
            } else if (nextClass != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    HeroPill(
                        label = "Next",
                        containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.14f),
                        textColor = MaterialTheme.colorScheme.onPrimary
                    )
                    HeroPill(
                        label = "${nextClass.startTime.to12Hour()} - ${nextClass.endTime.to12Hour()}",
                        containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.14f),
                        textColor = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Button(
                onClick = onMarkAttendance,
                enabled = currentClass != null && !isAttendanceMarked,
                shape = RoundedCornerShape(16.dp),
                border = if (currentClass != null && !isAttendanceMarked) BorderStroke(1.dp, MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)) else null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (currentClass != null && !isAttendanceMarked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f),
                    disabledContainerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f),
                    contentColor = if (currentClass != null && !isAttendanceMarked) primary else MaterialTheme.colorScheme.onPrimary,
                    disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.62f)
                ),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Text(
                    if (isAttendanceMarked) "Attendance Marked" else "Mark Attendance",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun BadgePill(text: String, isLive: Boolean) {
    if (!isLive) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.2f))
                .padding(horizontal = 12.dp, vertical = 5.dp)
        ) {
            Text(
                text = text,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelMedium
            )
        }
        return
    }

    val infiniteTransition = rememberInfiniteTransition(label = "badgePulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    Box(
        modifier = Modifier
            .scale(pulseScale)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = pulseAlpha))
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

private fun todayDay(dayOfWeek: Int): AppDay? = when (dayOfWeek) {
    Calendar.MONDAY -> AppDay.Mon
    Calendar.TUESDAY -> AppDay.Tue
    Calendar.WEDNESDAY -> AppDay.Wed
    Calendar.THURSDAY -> AppDay.Thu
    Calendar.FRIDAY -> AppDay.Fri
    else -> null
}

private fun timeGreeting(now: Calendar = Calendar.getInstance()): String {
    val hour = now.get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> "Good morning,"
        in 12..16 -> "Good afternoon,"
        in 17..21 -> "Good evening,"
        else -> "Welcome back,"
    }
}

private fun parseTimeMins(time: String): Int? {
    val parts = time.split(":")
    if (parts.size != 2) return null
    val hour = parts[0].toIntOrNull() ?: return null
    val min = parts[1].toIntOrNull() ?: return null
    return hour * 60 + min
}

private fun examCardSubtitle(
    examPlan: ExamPlan,
    isExamWindowActive: Boolean,
    todayExamSubjects: List<String>
): String {
    val start = examPlan.startDateMillis
    val end = examPlan.endDateMillis
    if (start == null || end == null) return "Set start/end dates and subject exam dates"
    if (isExamWindowActive) {
        return if (todayExamSubjects.isEmpty()) {
            "Exam window active • No classes today"
        } else {
            "Today: ${todayExamSubjects.joinToString(", ")}"
        }
    }
    return "Window ${formatShortDate(start)} - ${formatShortDate(end)}"
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

private fun isInExamWindow(plan: ExamPlan, dayMillis: Long): Boolean {
    val start = plan.startDateMillis ?: return false
    val end = plan.endDateMillis ?: return false
    val normalizedStart = startOfDayMillis(start)
    val normalizedEnd = startOfDayMillis(end)
    return dayMillis in normalizedStart..normalizedEnd
}

private fun formatShortDate(millis: Long): String {
    return SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(millis))
}

private data class NextExamInfo(
    val subject: String,
    val startMillis: Long
)

private fun nextExamInfo(plan: ExamPlan, nowMillis: Long): NextExamInfo? {
    val startMins = parseTimeMins(plan.commonStartTime) ?: return null
    return plan.subjectExamDates
        .map { (subject, dayMillis) ->
            NextExamInfo(
                subject = subject,
                startMillis = startOfDayMillis(dayMillis) + startMins * 60_000L
            )
        }
        .filter { it.startMillis >= nowMillis }
        .sortedWith(compareBy<NextExamInfo> { it.startMillis }.thenBy { it.subject.lowercase(Locale.getDefault()) })
        .firstOrNull()
}

private fun formatStartsIn(targetMillis: Long, nowMillis: Long): String {
    val totalMinutes = ((targetMillis - nowMillis).coerceAtLeast(0L) / 60_000L).toInt()
    val days = totalMinutes / (24 * 60)
    val hours = (totalMinutes % (24 * 60)) / 60
    val mins = totalMinutes % 60
    return when {
        days > 0 -> "${days}d ${hours}h"
        hours > 0 -> "${hours}h ${mins}m"
        else -> "${mins}m"
    }
}

private fun formatExamDateTime(millis: Long): String {
    return SimpleDateFormat("EEE, dd MMM • hh:mm a", Locale.getDefault()).format(Date(millis))
}

private fun dashboardAttendanceUnits(startTime: String, endTime: String): Int {
    val start = parseTimeMins(startTime) ?: return 1
    val end = parseTimeMins(endTime) ?: return 1
    return kotlin.math.ceil((end - start).coerceAtLeast(1) / 50.0).toInt().coerceAtLeast(1)
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun NowServingCard(meal: AppMeal, messPref: String) {
    val icon = when (meal.type) {
        AppMealType.breakfast -> Icons.Default.BakeryDining
        AppMealType.lunch -> Icons.Default.LunchDining
        AppMealType.dinner -> Icons.Default.DinnerDining
    }
    val timeText = when (meal.type) {
        AppMealType.breakfast -> "7:30 - 9:00 AM"
        AppMealType.lunch -> "12:00 - 2:00 PM"
        AppMealType.dinner -> "7:30 - 9:00 PM"
    }
    val accentColor = MaterialTheme.colorScheme.primary

    val items = if (messPref == "nonveg" && meal.nonVegItems != null && meal.nonVegItems.isNotEmpty()) {
        meal.nonVegItems
    } else {
        meal.items
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pressScaleClickable { },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = meal.label,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = timeText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val displayItems = if (items.isEmpty()) listOf("No items added") else items.take(8)
                displayItems.forEach { item ->
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
