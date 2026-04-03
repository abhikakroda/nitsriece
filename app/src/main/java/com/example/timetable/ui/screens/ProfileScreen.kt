package com.example.timetable.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.timetable.BuildConfig
import com.example.timetable.ui.AppViewModel
import com.example.timetable.data.exportAnalyticsCsv
import com.example.timetable.data.exportFullBackupCsv
import com.example.timetable.data.importFullBackupCsv
import com.example.timetable.updates.AppUpdateDownloadStatus
import com.example.timetable.updates.AppUpdateManager
import com.example.timetable.updates.AppUpdateManifest
import com.example.timetable.ui.components.DashboardVibeContainer
import com.example.timetable.ui.components.pressScaleClickable
import com.example.timetable.ui.theme.ErrorRed
import com.example.timetable.ui.theme.SuccessGreen
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private data class DirectoryEntry(
    val title: String,
    val subtitle: String,
    val supporting: String
)

@Composable
fun ProfileScreen(
    appViewModel: AppViewModel,
    onBack: () -> Unit,
    onNavigateToEditTimetable: () -> Unit,
    onNavigateToEditWeeklySchedule: () -> Unit,
    onNavigateToEditMessMenu: () -> Unit,
    onNavigateToExamDate: () -> Unit
) {
    val context = LocalContext.current
    val timetable by appViewModel.timetable.collectAsStateWithLifecycle()
    val isDarkMode by appViewModel.darkMode.collectAsStateWithLifecycle()
    val minimalistMode by appViewModel.minimalistMode.collectAsStateWithLifecycle()
    val showExamCountdown by appViewModel.showExamCountdown.collectAsStateWithLifecycle()
    val showMessMenuCard by appViewModel.showMessMenuCard.collectAsStateWithLifecycle()
    val themeColor by appViewModel.themeColor.collectAsStateWithLifecycle()
    val updateManifestUrl by appViewModel.updateManifestUrl.collectAsStateWithLifecycle()
    val pendingUpdateDownloadId by appViewModel.pendingUpdateDownloadId.collectAsStateWithLifecycle()
    val compactMode by appViewModel.compactMode.collectAsStateWithLifecycle()
    val dndMode by appViewModel.dndMode.collectAsStateWithLifecycle()
    val messPref by appViewModel.messPref.collectAsStateWithLifecycle()
    val examPlan by appViewModel.examPlan.collectAsStateWithLifecycle()
    val attendance by appViewModel.attendance.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    var activeDirectoryTitle by remember { mutableStateOf<String?>(null) }
    var showUpdateSourceDialog by remember { mutableStateOf(false) }
    var updateSourceDraft by remember(updateManifestUrl) { mutableStateOf(updateManifestUrl.ifBlank { BuildConfig.UPDATE_MANIFEST_URL }) }
    var checkingForUpdates by remember { mutableStateOf(false) }
    var availableUpdate by remember { mutableStateOf<AppUpdateManifest?>(null) }

    val effectiveUpdateManifestUrl = remember(updateManifestUrl) {
        updateManifestUrl.ifBlank { BuildConfig.UPDATE_MANIFEST_URL }
    }
    var downloadStatus by remember(pendingUpdateDownloadId) {
        mutableStateOf(
            pendingUpdateDownloadId?.let { AppUpdateManager.getDownloadStatus(context, it) }
                ?: AppUpdateDownloadStatus.Idle
        )
    }

    val facultyDirectory = remember(timetable) { buildFacultyDirectory(timetable) }
    val roomDirectory = remember(timetable) { buildRoomDirectory(timetable) }

    val analyticsExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            runCatching {
                val csv = exportAnalyticsCsv(attendance)
                context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { writer ->
                    writer.write(csv)
                } ?: error("Unable to open export file.")
            }.onSuccess {
                Toast.makeText(context, "Analytics CSV exported.", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(context, it.message ?: "Export failed.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val backupExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            runCatching {
                val snapshot = appViewModel.createBackupSnapshot()
                val csv = exportFullBackupCsv(snapshot)
                importFullBackupCsv(csv)
                context.contentResolver.openOutputStream(uri, "wt")?.bufferedWriter(Charsets.UTF_8)?.use { writer ->
                    writer.write(csv)
                    writer.flush()
                } ?: error("Unable to open backup file.")
            }.onSuccess {
                Toast.makeText(context, "Full backup exported.", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(context, it.message ?: "Backup export failed.", Toast.LENGTH_LONG).show()
            }
        }
    }

    val backupImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            runCatching {
                val csv = context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { reader ->
                    reader.readText()
                } ?: error("Unable to read backup file.")
                val snapshot = importFullBackupCsv(csv)
                appViewModel.restoreBackupSnapshot(snapshot)
            }.onSuccess {
                Toast.makeText(context, "Backup imported successfully.", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(context, "Backup import failed: ${it.message ?: "Unknown error"}", Toast.LENGTH_LONG).show()
            }
        }
    }

    DashboardVibeContainer {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .pressScaleClickable(pressedScale = 0.98f, onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "Preferences and configuration",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            ProfileSummaryStrip(
                isDarkMode = isDarkMode,
                showExamCountdown = showExamCountdown,
                showMessMenuCard = showMessMenuCard
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Preferences ──────────────────────────
            item {
                SettingsSectionLabel("Preferences")
                Spacer(Modifier.height(8.dp))
                SettingsSectionCard {
                    Column {
                        SettingsRow(
                            icon = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            title = "Dark Mode",
                            subtitle = if (isDarkMode) "Dark theme enabled" else "Light theme enabled",
                            trailing = {
                                Switch(
                                    checked = isDarkMode,
                                    onCheckedChange = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        appViewModel.setDarkMode(it)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            }
                        )
                        SettingsDivider()
                        SettingsRow(
                            icon = Icons.Default.Timer,
                            title = "Exam Countdown",
                            subtitle = "Show countdown on dashboard",
                            trailing = {
                                Switch(
                                    checked = showExamCountdown,
                                    onCheckedChange = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        appViewModel.setShowExamCountdown(it)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            }
                        )
                        SettingsDivider()
                        SettingsRow(
                            icon = Icons.Default.Restaurant,
                            title = "Today Menu Card",
                            subtitle = "Show current meal on dashboard",
                            trailing = {
                                Switch(
                                    checked = showMessMenuCard,
                                    onCheckedChange = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        appViewModel.setShowMessMenuCard(it)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            }
                        )
                        SettingsDivider()
                        SettingsRow(
                            icon = Icons.Default.InvertColors,
                            title = "Minimalist Theme",
                            subtitle = "Switch to monochrome UI styling",
                            trailing = {
                                Switch(
                                    checked = minimalistMode,
                                    onCheckedChange = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        appViewModel.setMinimalistMode(it)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            }
                        )
                        SettingsDivider()
                        SettingsRow(
                            icon = Icons.Default.ViewAgenda,
                            title = "Compact Mode",
                            subtitle = "Use tighter cards and reduced shape radius",
                            trailing = {
                                Switch(
                                    checked = compactMode,
                                    onCheckedChange = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        appViewModel.setCompactMode(it)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            }
                        )
                        SettingsDivider()
                        SettingsRow(
                            icon = Icons.Default.NotificationsOff,
                            title = "Focus Mode",
                            subtitle = "Hide live class notification while DND mode is on",
                            trailing = {
                                Switch(
                                    checked = dndMode,
                                    onCheckedChange = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        appViewModel.setDndMode(it)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            }
                        )
                        SettingsDivider()
                        SettingsRow(
                            icon = Icons.Default.Palette,
                            title = "Accent Color",
                            subtitle = "Apply a theme accent across the app",
                            trailing = {
                                ThemeColorPicker(
                                    selectedColor = themeColor,
                                    onSelect = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        appViewModel.setThemeColor(it)
                                    }
                                )
                            }
                        )
                    }
                }
            }

            item {
                SettingsSectionLabel("Directory")
                Spacer(Modifier.height(8.dp))
                SettingsSectionCard {
                    Column {
                        SettingsRow(
                            icon = Icons.Default.Person,
                            title = "Faculty Directory",
                            subtitle = "${facultyDirectory.size} teachers linked to timetable classes",
                            onClick = { activeDirectoryTitle = "Faculty Directory" },
                            trailing = {
                                Icon(
                                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                        SettingsDivider()
                        SettingsRow(
                            icon = Icons.Default.LocationOn,
                            title = "Room Directory",
                            subtitle = "${roomDirectory.size} rooms linked to timetable classes",
                            onClick = { activeDirectoryTitle = "Room Directory" },
                            trailing = {
                                Icon(
                                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                    }
                }
            }

            // ── Exams ─────────────────────────────────
            item {
                SettingsSectionLabel("Exams")
                Spacer(Modifier.height(8.dp))
                SettingsSectionCard {
                    val examSummary = remember(examPlan) {
                        val start = examPlan.startDateMillis
                        val end = examPlan.endDateMillis
                        if (start == null || end == null) {
                            "Not set"
                        } else {
                            val fmt = SimpleDateFormat("dd MMM", Locale.getDefault())
                            val count = examPlan.subjectExamDates.size
                            "${fmt.format(Date(start))} - ${fmt.format(Date(end))} • $count subjects"
                        }
                    }
                    SettingsRow(
                        icon = Icons.Default.Event,
                        title = "Exam Planner",
                        subtitle = examSummary,
                        onClick = onNavigateToExamDate,
                        trailing = {
                            Icon(
                                Icons.Default.Edit,
                                null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            // ── Timetable ────────────────────────────
            item {
                SettingsSectionLabel("Timetable")
                Spacer(Modifier.height(8.dp))
                SettingsSectionCard {
                    Column {
                        SettingsRow(
                            icon = Icons.Default.CalendarViewWeek,
                            title = "Edit Weekly Schedule",
                            subtitle = "Move class day, time, and room",
                            onClick = onNavigateToEditWeeklySchedule,
                            trailing = {
                                Icon(
                                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                        SettingsDivider()
                        SettingsRow(
                            icon = Icons.Default.EditCalendar,
                            title = "Edit Timetable",
                            subtitle = "Change subjects, times, rooms",
                            onClick = onNavigateToEditTimetable,
                            trailing = {
                                Icon(
                                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                    }
                }
            }

            // ── Mess Menu ────────────────────────────
            item {
                SettingsSectionLabel("Mess Menu")
                Spacer(Modifier.height(8.dp))
                SettingsSectionCard {
                    Column {
                        SettingsRow(
                            icon = Icons.Default.Restaurant,
                            title = "Default Preference",
                            subtitle = if (messPref == "veg") "Vegetarian" else "Non-Vegetarian",
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                appViewModel.setMessPref(if (messPref == "veg") "nonveg" else "veg")
                            },
                            trailing = {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (messPref == "veg") SuccessGreen.copy(alpha = 0.1f)
                                            else ErrorRed.copy(alpha = 0.1f)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        if (messPref == "veg") "VEG" else "NON-VEG",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (messPref == "veg") SuccessGreen else ErrorRed
                                    )
                                }
                            }
                        )
                        SettingsDivider()
                        SettingsRow(
                            icon = Icons.Default.Description,
                            title = "Edit Meal Items",
                            subtitle = "Customize daily meals",
                            onClick = onNavigateToEditMessMenu,
                            trailing = {
                                Icon(
                                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                    }
                }
            }

            item {
                SettingsSectionLabel("Backup & Restore")
                Spacer(Modifier.height(8.dp))
                SettingsSectionCard {
                    Column {
                        SettingsRow(
                            icon = Icons.Default.Assessment,
                            title = "Export Analytics CSV",
                            subtitle = "Save attendance summary as CSV",
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                analyticsExportLauncher.launch("analytics-${System.currentTimeMillis()}.csv")
                            },
                            trailing = {
                                Icon(
                                    Icons.Default.Download,
                                    null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                        SettingsDivider()
                        SettingsRow(
                            icon = Icons.Default.Archive,
                            title = "Export Full Backup",
                            subtitle = "Timetable, mess, exams, notes, syllabus and settings",
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                backupExportLauncher.launch("timetable-backup-${System.currentTimeMillis()}.csv")
                            },
                            trailing = {
                                Icon(
                                    Icons.Default.UploadFile,
                                    null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                        SettingsDivider()
                        SettingsRow(
                            icon = Icons.Default.Restore,
                            title = "Import Full Backup",
                            subtitle = "Load everything from a backup CSV",
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                backupImportLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "*/*"))
                            },
                            trailing = {
                                Icon(
                                    Icons.Default.FileOpen,
                                    null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                    }
                }
            }

            item {
                SettingsSectionLabel("App Updates")
                Spacer(Modifier.height(8.dp))
                SettingsSectionCard {
                    Column {
                        SettingsRow(
                            icon = Icons.Default.Link,
                            title = "Update Feed",
                            subtitle = effectiveUpdateManifestUrl.ifBlank { "Set a JSON feed URL for latest releases" },
                            onClick = {
                                updateSourceDraft = effectiveUpdateManifestUrl
                                showUpdateSourceDialog = true
                            },
                            trailing = {
                                Icon(
                                    Icons.Default.Edit,
                                    null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                        SettingsDivider()
                        SettingsRow(
                            icon = Icons.Default.SystemUpdate,
                            title = "Check for Updates",
                            subtitle = if (checkingForUpdates) {
                                "Checking latest release..."
                            } else {
                                "Current version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
                            },
                            onClick = {
                                if (checkingForUpdates) return@SettingsRow
                                if (effectiveUpdateManifestUrl.isBlank()) {
                                    Toast.makeText(context, "Set the update feed URL first.", Toast.LENGTH_SHORT).show()
                                    showUpdateSourceDialog = true
                                    return@SettingsRow
                                }
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                scope.launch {
                                    checkingForUpdates = true
                                    runCatching {
                                        AppUpdateManager.fetchManifest(effectiveUpdateManifestUrl)
                                    }.onSuccess { manifest ->
                                        if (AppUpdateManager.isUpdateAvailable(manifest)) {
                                            availableUpdate = manifest
                                        } else {
                                            Toast.makeText(context, "You already have the latest release.", Toast.LENGTH_SHORT).show()
                                        }
                                    }.onFailure {
                                        Toast.makeText(context, it.message ?: "Update check failed.", Toast.LENGTH_LONG).show()
                                    }
                                    checkingForUpdates = false
                                }
                            },
                            trailing = {
                                if (checkingForUpdates) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.DownloadForOffline,
                                        null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        )
                        if (pendingUpdateDownloadId != null) {
                            SettingsDivider()
                            SettingsRow(
                                icon = Icons.Default.InstallMobile,
                                title = "Install Downloaded Update",
                                subtitle = when (downloadStatus) {
                                    AppUpdateDownloadStatus.Pending -> "Download is queued"
                                    AppUpdateDownloadStatus.Running -> "Downloading APK..."
                                    AppUpdateDownloadStatus.Paused -> "Download paused"
                                    AppUpdateDownloadStatus.Successful -> "APK ready. Tap to install"
                                    AppUpdateDownloadStatus.Failed -> "Download failed. Check again"
                                    AppUpdateDownloadStatus.Idle -> "No update download in progress"
                                },
                                onClick = {
                                    val downloadId = pendingUpdateDownloadId ?: return@SettingsRow
                                    downloadStatus = AppUpdateManager.getDownloadStatus(context, downloadId)
                                    when (downloadStatus) {
                                        AppUpdateDownloadStatus.Successful -> {
                                            val launched = AppUpdateManager.promptInstallDownloadedApk(context, downloadId)
                                            if (launched) {
                                                Toast.makeText(context, "Opening installer…", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Allow app installs for Timetable, then tap again.", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                        AppUpdateDownloadStatus.Failed -> {
                                            appViewModel.setPendingUpdateDownloadId(null)
                                            Toast.makeText(context, "Downloaded file is not available anymore.", Toast.LENGTH_SHORT).show()
                                        }
                                        else -> {
                                            Toast.makeText(context, "Update download is not ready yet.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                trailing = {
                                    val tint = when (downloadStatus) {
                                        AppUpdateDownloadStatus.Successful -> SuccessGreen
                                        AppUpdateDownloadStatus.Failed -> ErrorRed
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                    Icon(
                                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        null,
                                        tint = tint
                                    )
                                }
                            )
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Made by Abhishek",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
        }
    }

    val activeDirectoryEntries = when (activeDirectoryTitle) {
        "Faculty Directory" -> facultyDirectory
        "Room Directory" -> roomDirectory
        else -> null
    }

    if (activeDirectoryEntries != null && activeDirectoryTitle != null) {
        AlertDialog(
            onDismissRequest = { activeDirectoryTitle = null },
            title = { Text(activeDirectoryTitle!!, fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (activeDirectoryEntries.isEmpty()) {
                        item {
                            Text(
                                text = "No entries found in the current timetable.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(activeDirectoryEntries.size) { index ->
                            val entry = activeDirectoryEntries[index]
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = entry.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = entry.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = entry.supporting,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (index != activeDirectoryEntries.lastIndex) {
                                SettingsDivider()
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { activeDirectoryTitle = null }) {
                    Text("Close")
                }
            }
        )
    }

    if (showUpdateSourceDialog) {
        AlertDialog(
            onDismissRequest = { showUpdateSourceDialog = false },
            title = { Text("Update Feed URL", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Point this to a JSON file with versionCode, versionName, apkUrl, and optional notes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = updateSourceDraft,
                        onValueChange = { updateSourceDraft = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Manifest URL") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        appViewModel.setUpdateManifestUrl(updateSourceDraft)
                        showUpdateSourceDialog = false
                        Toast.makeText(context, "Update feed saved.", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUpdateSourceDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (availableUpdate != null) {
        val manifest = availableUpdate!!
        AlertDialog(
            onDismissRequest = { availableUpdate = null },
            title = {
                Text(
                    text = manifest.title ?: "Update Available",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Current: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Latest: ${manifest.versionName} (${manifest.versionCode})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (manifest.notes.isNotBlank()) {
                        Text(
                            text = manifest.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val downloadId = AppUpdateManager.enqueueDownload(context, manifest)
                        appViewModel.setPendingUpdateDownloadId(downloadId)
                        downloadStatus = AppUpdateDownloadStatus.Pending
                        availableUpdate = null
                        Toast.makeText(context, "Downloading latest release…", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Download")
                }
            },
            dismissButton = {
                TextButton(onClick = { availableUpdate = null }) {
                    Text("Later")
                }
            }
        )
    }
}

private fun buildFacultyDirectory(timetable: Map<com.example.timetable.data.AppDay, List<com.example.timetable.data.ClassSlot>>): List<DirectoryEntry> {
    return timetable.values
        .flatten()
        .filter { !it.faculty.isNullOrBlank() }
        .groupBy { it.faculty!!.trim() }
        .map { (faculty, slots) ->
            DirectoryEntry(
                title = faculty,
                subtitle = slots.map { it.subject }.distinct().sorted().joinToString(),
                supporting = slots.map { "${it.room} • ${it.startTime}" }.distinct().sorted().joinToString()
            )
        }
        .sortedBy { it.title.lowercase(Locale.getDefault()) }
}

private fun buildRoomDirectory(timetable: Map<com.example.timetable.data.AppDay, List<com.example.timetable.data.ClassSlot>>): List<DirectoryEntry> {
    return timetable.values
        .flatten()
        .filter { it.room.isNotBlank() }
        .groupBy { it.room.trim() }
        .map { (room, slots) ->
            DirectoryEntry(
                title = room,
                subtitle = slots.map { it.subject }.distinct().sorted().joinToString(),
                supporting = slots.mapNotNull { it.faculty?.trim()?.takeIf(String::isNotBlank) }.distinct().sorted().joinToString()
                    .ifBlank { "Faculty not assigned" }
            )
        }
        .sortedBy { it.title.lowercase(Locale.getDefault()) }
}


// ── Reusable settings UI components ──────────────────────────────────────

@Composable
private fun SettingsSectionCard(content: @Composable () -> Unit) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    colors = if (isDarkTheme) {
                        listOf(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.98f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
                        )
                    } else {
                        listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                        )
                    }
                )
            )
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDarkTheme) 0.8f else 0.45f), RoundedCornerShape(22.dp))
    ) {
        content()
    }
}

@Composable
private fun ThemeColorPicker(
    selectedColor: Long,
    onSelect: (Long) -> Unit
) {
    val accents = listOf(
        0xFFFF3D00L,
        0xFF2563EBL,
        0xFF0F766EL,
        0xFF16A34AL,
        0xFFCA8A04L,
        0xFFBE123CL
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        accents.forEach { colorLong ->
            val swatch = Color(colorLong.toInt())
            val selected = selectedColor == colorLong
            Box(
                modifier = Modifier
                    .size(if (selected) 28.dp else 24.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(swatch)
                    .border(
                        width = if (selected) 2.dp else 1.dp,
                        color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(999.dp)
                    )
                    .clickable { onSelect(colorLong) },
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = if (swatch.luminance() > 0.55f) Color.Black else Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileSummaryStrip(
    isDarkMode: Boolean,
    showExamCountdown: Boolean,
    showMessMenuCard: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ProfileStatCard(
            modifier = Modifier.weight(1f),
            label = "Theme",
            value = if (isDarkMode) "Dark" else "Light",
            accent = MaterialTheme.colorScheme.primary
        )
        ProfileStatCard(
            modifier = Modifier.weight(1f),
            label = "Countdown",
            value = if (showExamCountdown) "On" else "Off",
            accent = MaterialTheme.colorScheme.secondary
        )
        ProfileStatCard(
            modifier = Modifier.weight(1f),
            label = "Menu",
            value = if (showMessMenuCard) "On" else "Off",
            accent = if (showMessMenuCard) SuccessGreen else ErrorRed
        )
    }
}

@Composable
private fun ProfileStatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    accent: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.9f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = accent
            )
        }
    }
}

@Composable
private fun SettingsSectionLabel(title: String) {
    Text(
        title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 4.dp, bottom = 0.dp)
    )
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        thickness = 0.5.dp
    )
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.pressScaleClickable(
                        pressedScale = 0.985f,
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        trailing()
    }
}

// Keep old names for backwards compatibility
@Composable
fun SectionHeader(title: String) {
    SettingsSectionLabel(title)
}

@Composable
fun SettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit
) {
    SettingsRow(icon, title, subtitle, modifier, onClick, trailing)
}
