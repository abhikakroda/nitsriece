package com.example.timetable.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.timetable.data.AppDay
import com.example.timetable.data.ClassSlot
import com.example.timetable.ui.AppViewModel
import com.example.timetable.ui.components.DashboardVibeContainer
import com.example.timetable.ui.components.pressScaleClickable
import com.example.timetable.ui.theme.ErrorRed
import com.example.timetable.ui.theme.GradientAccentCool
import com.example.timetable.ui.theme.GradientHeroCard
import com.example.timetable.ui.theme.SuccessGreen
import com.example.timetable.ui.theme.WarningYellow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private data class SubjectAttendanceHistory(
    val presentDates: List<String> = emptyList(),
    val absentDates: List<String> = emptyList()
) {
    val total: Int get() = presentDates.size + absentDates.size
    val percentage: Int get() = if (total == 0) 0 else (presentDates.size * 100f / total).toInt()
}

private data class AnalyticsOverview(
    val totalPresent: Int,
    val totalAbsent: Int,
    val totalClasses: Int,
    val percentage: Int,
    val subjectCount: Int,
    val classesNeededFor75: Int,
    val bestPercentage: Int
)

private data class AttendanceTargetPlan(
    val thresholdPercent: Int = 75,
    val canMiss: Int,
    val mustAttend: Int
)

private val AnalyticsTitleStyle
    @Composable get() = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp, lineHeight = 22.sp)

private val AnalyticsBodyStyle
    @Composable get() = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp, lineHeight = 18.sp)

private val AnalyticsCaptionStyle
    @Composable get() = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 15.sp)

private val AnalyticsValueStyle
    @Composable get() = MaterialTheme.typography.headlineSmall.copy(fontSize = 20.sp, lineHeight = 26.sp)

@Composable
private fun analyticsCardBrush(): Brush {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return Brush.linearGradient(
        colors = if (isDarkTheme) {
            listOf(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.98f),
                MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
            )
        } else {
            listOf(
                MaterialTheme.colorScheme.surface,
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
            )
        }
    )
}

@Composable
private fun analyticsHeroBrush(): Brush {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return Brush.linearGradient(
        colors = GradientHeroCard.map { it.copy(alpha = if (isDarkTheme) 0.12f else 0.18f) }
    )
}

@Composable
private fun analyticsAccentBrush(accent: Color): Brush {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return Brush.linearGradient(
        colors = listOf(
            accent.copy(alpha = if (isDarkTheme) 0.2f else 0.28f),
            accent.copy(alpha = if (isDarkTheme) 0.08f else 0.12f)
        )
    )
}

@Composable
fun AnalyticsScreen(appViewModel: AppViewModel) {
    val timetable by appViewModel.timetable.collectAsStateWithLifecycle()
    val rescheduledClasses by appViewModel.rescheduledClasses.collectAsStateWithLifecycle()
    val dailyAbsentLogs by appViewModel.dailyAbsentLogs.collectAsStateWithLifecycle()
    val dailyPresentLogs by appViewModel.dailyPresentLogs.collectAsStateWithLifecycle()
    val canceledClasses by appViewModel.canceledClasses.collectAsStateWithLifecycle()
    val semesterStartDateMillis by appViewModel.semesterStartDateMillis.collectAsStateWithLifecycle()
    val semesterEndDateMillis by appViewModel.semesterEndDateMillis.collectAsStateWithLifecycle()

    val attendanceHistory = remember(
        timetable,
        rescheduledClasses,
        dailyAbsentLogs,
        dailyPresentLogs,
        canceledClasses,
        semesterStartDateMillis,
        semesterEndDateMillis
    ) {
        buildAttendanceHistory(
            timetable = timetable,
            rescheduledClasses = rescheduledClasses,
            dailyAbsentLogs = dailyAbsentLogs,
            dailyPresentLogs = dailyPresentLogs,
            canceledClasses = canceledClasses,
            semesterStartDateMillis = semesterStartDateMillis,
            semesterEndDateMillis = semesterEndDateMillis
        )
    }

    val subjects = remember(timetable, attendanceHistory) {
        orderSubjects((timetable.values.flatten().map { it.subject } + attendanceHistory.keys).distinct())
    }
    val overview = remember(attendanceHistory, subjects) { buildOverview(attendanceHistory, subjects.size) }

    var selectedSubject by remember { mutableStateOf<String?>(null) }

    DashboardVibeContainer {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(132.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
        item {
            AnalyticsHeader()
        }

        if (subjects.isEmpty()) {
            item {
                AnalyticsCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "No attendance data yet",
                            style = AnalyticsTitleStyle,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Mark classes from Schedule and the subject cards will appear here.",
                            style = AnalyticsBodyStyle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            item {
                OverallAttendanceCard(overview = overview)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    SmallStatCard(
                        modifier = Modifier.weight(1f),
                        value = overview.subjectCount.toString(),
                        label = "Subjects",
                        accent = MaterialTheme.colorScheme.primary
                    )
                    SmallStatCard(
                        modifier = Modifier.weight(1f),
                        value = overview.classesNeededFor75.toString(),
                        label = "Need",
                        accent = WarningYellow
                    )
                    SmallStatCard(
                        modifier = Modifier.weight(1f),
                        value = "${overview.bestPercentage}%",
                        label = "Best",
                        accent = SuccessGreen
                    )
                }
            }

            item {
                ThresholdInfoCard(overview = overview)
            }

            item {
                SectionLabel("Subject Breakdown")
            }

            items(subjects) { subject ->
                val history = attendanceHistory[subject] ?: SubjectAttendanceHistory()
                val planner = remember(history) { buildAttendanceTargetPlan(history) }
                SubjectAnalyticsCard(
                    subject = subject,
                    history = history,
                    planner = planner,
                    onClick = { selectedSubject = subject }
                )
            }
        }
        }
    }

    selectedSubject?.let { subject ->
        SubjectHistoryDialog(
            subject = subject,
            history = attendanceHistory[subject] ?: SubjectAttendanceHistory(),
            planner = buildAttendanceTargetPlan(attendanceHistory[subject] ?: SubjectAttendanceHistory()),
            onDismiss = { selectedSubject = null }
        )
    }
}

@Composable
private fun AnalyticsHeader() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.75f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Analytics",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Attendance overview and insights",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun OverallAttendanceCard(overview: AnalyticsOverview) {
    AnalyticsCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Overall Attendance",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp, lineHeight = 22.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AttendanceRing(
                    percentage = overview.percentage,
                    present = overview.totalPresent,
                    total = overview.totalClasses
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OverviewCountRow(
                        icon = Icons.Default.Check,
                        iconTint = SuccessGreen,
                        value = overview.totalPresent.toString(),
                        label = "Present"
                    )
                    OverviewCountRow(
                        icon = Icons.Default.Close,
                        iconTint = ErrorRed,
                        value = overview.totalAbsent.toString(),
                        label = "Absent"
                    )
                    OverviewCountRow(
                        icon = Icons.Default.School,
                        iconTint = MaterialTheme.colorScheme.primary,
                        value = overview.totalClasses.toString(),
                        label = "Total"
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = if (overview.percentage >= 75) {
                                GradientAccentCool.map { it.copy(alpha = 0.16f) }
                            } else {
                                listOf(WarningYellow.copy(alpha = 0.2f), WarningYellow.copy(alpha = 0.08f))
                            }
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = if (overview.percentage >= 75) SuccessGreen.copy(alpha = 0.32f) else WarningYellow.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    text = if (overview.percentage >= 75) {
                        "Above 75% threshold. Keep the streak."
                    } else {
                        "Below 75% threshold. Attend more classes."
                    },
                    style = AnalyticsBodyStyle,
                    fontWeight = FontWeight.SemiBold,
                    color = if (overview.percentage >= 75) SuccessGreen else WarningYellow
                )
            }
        }
    }
}

@Composable
private fun AttendanceRing(
    percentage: Int,
    present: Int,
    total: Int
) {
    val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
    Box(
        modifier = Modifier.size(132.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 12.dp.toPx()
            drawArc(
                color = trackColor,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
                topLeft = Offset(stroke, stroke),
                size = Size(size.width - stroke * 2, size.height - stroke * 2)
            )
            drawArc(
                brush = Brush.linearGradient(
                    colors = if (percentage >= 75) {
                        listOf(SuccessGreen, Color(0xFF10B981))
                    } else {
                        listOf(WarningYellow, Color(0xFFF59E0B))
                    }
                ),
                startAngle = 135f,
                sweepAngle = 270f * (percentage / 100f),
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
                topLeft = Offset(stroke, stroke),
                size = Size(size.width - stroke * 2, size.height - stroke * 2)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 24.sp, lineHeight = 28.sp),
                color = if (percentage >= 75) SuccessGreen else WarningYellow,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$present/$total",
                style = AnalyticsBodyStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun OverviewCountRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    value: String,
    label: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(16.dp))
        }
        Column {
            Text(
                text = value,
                style = AnalyticsValueStyle,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = AnalyticsBodyStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SmallStatCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    accent: Color
) {
    AnalyticsCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                style = AnalyticsValueStyle,
                fontWeight = FontWeight.Bold,
                color = accent
            )
            Text(
                text = label,
                style = AnalyticsBodyStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ThresholdInfoCard(overview: AnalyticsOverview) {
    AnalyticsCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (overview.classesNeededFor75 > 0) WarningYellow.copy(alpha = 0.14f) else SuccessGreen.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = if (overview.classesNeededFor75 > 0) WarningYellow else SuccessGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (overview.classesNeededFor75 > 0) {
                        "Attend ${overview.classesNeededFor75} more classes"
                    } else {
                        "You are above the 75% threshold"
                    },
                    style = AnalyticsTitleStyle,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (overview.classesNeededFor75 > 0) {
                        "to reach the 75% attendance threshold"
                    } else {
                        "Keep attending to stay safe"
                    },
                    style = AnalyticsBodyStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 1.sp
    )
}

@Composable
private fun SubjectAnalyticsCard(
    subject: String,
    history: SubjectAttendanceHistory,
    planner: AttendanceTargetPlan,
    onClick: () -> Unit
) {
    val percentage = history.percentage
    val accent = when {
        percentage >= 75 -> SuccessGreen
        percentage >= 50 -> WarningYellow
        else -> ErrorRed
    }
    val isLab = subject.contains("Lab", ignoreCase = true)

    AnalyticsCard(
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                        text = subject,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp, lineHeight = 20.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isLab) "Lab course" else "Subject trend",
                        style = AnalyticsCaptionStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(accent.copy(alpha = 0.12f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "$percentage%",
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp, lineHeight = 16.sp),
                        fontWeight = FontWeight.Bold,
                        color = accent
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth((percentage / 100f).coerceIn(0f, 1f))
                        .height(8.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Brush.horizontalGradient(listOf(accent, accent.copy(alpha = 0.6f))))
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AttendanceCountText("P ${history.presentDates.size}", SuccessGreen)
                    AttendanceCountText("A ${history.absentDates.size}", ErrorRed)
                    AttendanceCountText("T ${history.total}", MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = buildPlannerSummary(planner),
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp, lineHeight = 15.sp),
                        color = if (planner.mustAttend > 0) WarningYellow else SuccessGreen,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            if (isLab) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "LAB",
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp, lineHeight = 15.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            PlannerPillRow(planner = planner)
        }
    }
}

@Composable
private fun AttendanceCountText(text: String, color: Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp, lineHeight = 15.sp),
        color = color,
        fontWeight = FontWeight.Medium
    )
}

private fun buildPlannerSummary(planner: AttendanceTargetPlan): String {
    return if (planner.mustAttend > 0) {
        "Attend ${planner.mustAttend} to recover"
    } else {
        "Can miss ${planner.canMiss} safely"
    }
}

private fun buildOverview(
    attendanceHistory: Map<String, SubjectAttendanceHistory>,
    subjectCount: Int
): AnalyticsOverview {
    val totalPresent = attendanceHistory.values.sumOf { it.presentDates.size }
    val totalAbsent = attendanceHistory.values.sumOf { it.absentDates.size }
    val totalClasses = totalPresent + totalAbsent
    val percentage = if (totalClasses == 0) 0 else (totalPresent * 100f / totalClasses).toInt()
    val classesNeededFor75 = if (totalClasses == 0) 0 else {
        maxOf(0, kotlin.math.ceil((0.75 * totalClasses - totalPresent) / 0.25).toInt())
    }
    val bestPercentage = attendanceHistory.values.maxOfOrNull { it.percentage } ?: 0

    return AnalyticsOverview(
        totalPresent = totalPresent,
        totalAbsent = totalAbsent,
        totalClasses = totalClasses,
        percentage = percentage,
        subjectCount = subjectCount,
        classesNeededFor75 = classesNeededFor75,
        bestPercentage = bestPercentage
    )
}

@Composable
private fun SubjectHistoryDialog(
    subject: String,
    history: SubjectAttendanceHistory,
    planner: AttendanceTargetPlan,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = subject,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp, lineHeight = 24.sp),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${history.percentage}% attendance",
                    style = AnalyticsBodyStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PlannerDetailCard(planner = planner)
                AttendanceDateSection(
                    title = "Present Dates",
                    icon = Icons.Default.CheckCircle,
                    iconTint = SuccessGreen,
                    emptyText = "No present dates recorded for this subject.",
                    dates = history.presentDates
                )
                AttendanceDateSection(
                    title = "Absent Dates",
                    icon = Icons.Default.Close,
                    iconTint = ErrorRed,
                    emptyText = "No absent dates recorded for this subject.",
                    dates = history.absentDates
                )
            }
        }
    )
}

@Composable
private fun AttendanceDateSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    emptyText: String,
    dates: List<String>
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(18.dp))
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp, lineHeight = 18.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${dates.size} dates",
                    style = AnalyticsCaptionStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (dates.isEmpty()) {
            Text(
                text = emptyText,
                style = AnalyticsCaptionStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            dates.forEach { date ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    iconTint.copy(alpha = 0.18f),
                                    iconTint.copy(alpha = 0.08f)
                                )
                            )
                        )
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = date,
                        style = AnalyticsBodyStyle,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun PlannerPillRow(planner: AttendanceTargetPlan) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AttendanceCountText("Can miss ${planner.canMiss}", SuccessGreen)
        AttendanceCountText("Must attend ${planner.mustAttend}", WarningYellow)
        AttendanceCountText("Target ${planner.thresholdPercent}%", MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun PlannerDetailCard(planner: AttendanceTargetPlan) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Attendance Target Planner",
                style = AnalyticsTitleStyle,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Stay at or above ${planner.thresholdPercent}%",
                style = AnalyticsBodyStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "Miss ${planner.canMiss}",
                style = AnalyticsBodyStyle,
                color = SuccessGreen,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Attend ${planner.mustAttend}",
                style = AnalyticsBodyStyle,
                color = WarningYellow,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun AnalyticsCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val clickableModifier = if (onClick != null) {
        modifier.pressScaleClickable(pressedScale = 0.985f, onClick = onClick)
    } else {
        modifier
    }

    Card(
        modifier = clickableModifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDarkTheme) 0.82f else 0.45f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(analyticsCardBrush())
        ) {
            content()
        }
    }
}

private fun orderSubjects(subjects: List<String>): List<String> {
    val ordered = subjects.toMutableList()
    val dspLabIndex = ordered.indexOf("DSP Lab")
    val vlsiLabIndex = ordered.indexOf("VLSI Design Lab")
    if (dspLabIndex >= 0 && vlsiLabIndex >= 0 && dspLabIndex > vlsiLabIndex) {
        ordered.removeAt(dspLabIndex)
        ordered.add(vlsiLabIndex, "DSP Lab")
    }
    return ordered
}

private fun buildAttendanceHistory(
    timetable: Map<AppDay, List<ClassSlot>>,
    rescheduledClasses: Map<String, ClassSlot>,
    dailyAbsentLogs: Map<String, Set<String>>,
    dailyPresentLogs: Map<String, Set<String>>,
    canceledClasses: Map<String, Set<String>>,
    semesterStartDateMillis: Long,
    semesterEndDateMillis: Long
): Map<String, SubjectAttendanceHistory> {
    if (semesterStartDateMillis <= 0L) return emptyMap()

    val nowCal = Calendar.getInstance()
    val nowMillis = nowCal.timeInMillis
    val endBoundMillis = if (semesterEndDateMillis > 0L) minOf(nowMillis, semesterEndDateMillis) else nowMillis
    val startCal = Calendar.getInstance().apply { timeInMillis = semesterStartDateMillis }
    if (startCal.timeInMillis > endBoundMillis) return emptyMap()

    val currentCal = startCal.clone() as Calendar
    val keyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val displayFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val historyMap = mutableMapOf<String, MutableSubjectHistory>()

    while (currentCal.timeInMillis <= endBoundMillis) {
        val dateKey = keyFormat.format(currentCal.time)
        val displayDate = displayFormat.format(currentCal.time)
        val appDay = when (currentCal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> AppDay.Mon
            Calendar.TUESDAY -> AppDay.Tue
            Calendar.WEDNESDAY -> AppDay.Wed
            Calendar.THURSDAY -> AppDay.Thu
            Calendar.FRIDAY -> AppDay.Fri
            Calendar.SATURDAY -> AppDay.Sat
            Calendar.SUNDAY -> AppDay.Sun
            else -> null
        }

        if (appDay != null) {
            val baseClasses = timetable[appDay].orEmpty()
            val extraClasses = rescheduledClasses
                .filterKeys { it.startsWith("${dateKey}_extra_") }
                .values
            val dayClasses = (baseClasses + extraClasses).sortedBy { parseSubjectTimeMins(it.startTime) ?: Int.MAX_VALUE }
            val absentSlots = dailyAbsentLogs[dateKey].orEmpty()
            val presentSlots = dailyPresentLogs[dateKey].orEmpty()
            val canceledSlots = canceledClasses[dateKey].orEmpty()

            dayClasses.forEach { cls ->
                val slotKey = "${cls.subject}:${cls.startTime}"
                if (slotKey in canceledSlots) return@forEach
                if (!isPastClassForHistory(currentCal, nowCal, cls)) return@forEach

                val history = historyMap.getOrPut(cls.subject) { MutableSubjectHistory() }
                if (slotKey in presentSlots) {
                    history.presentDates += displayDate
                } else if (slotKey in absentSlots) {
                    history.absentDates += displayDate
                }
            }
        }

        currentCal.add(Calendar.DAY_OF_YEAR, 1)
    }

    return historyMap.mapValues { (_, value) ->
        SubjectAttendanceHistory(
            presentDates = value.presentDates.distinct(),
            absentDates = value.absentDates.distinct()
        )
    }
}

private data class MutableSubjectHistory(
    val presentDates: MutableList<String> = mutableListOf(),
    val absentDates: MutableList<String> = mutableListOf()
)

private fun buildAttendanceTargetPlan(
    history: SubjectAttendanceHistory,
    threshold: Double = 0.75
): AttendanceTargetPlan {
    val present = history.presentDates.size.toDouble()
    val absent = history.absentDates.size.toDouble()
    val total = present + absent

    val canMiss = if (present <= 0.0) {
        0
    } else {
        kotlin.math.floor((present / threshold) - total).toInt().coerceAtLeast(0)
    }
    val mustAttend = if (total == 0.0) {
        0
    } else {
        kotlin.math.ceil(((threshold * total) - present) / (1 - threshold)).toInt().coerceAtLeast(0)
    }

    return AttendanceTargetPlan(
        thresholdPercent = (threshold * 100).toInt(),
        canMiss = canMiss,
        mustAttend = mustAttend
    )
}

private fun isPastClassForHistory(classDate: Calendar, nowCal: Calendar, cls: ClassSlot): Boolean {
    val sameDay = classDate.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) &&
        classDate.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR)
    if (!sameDay) return classDate.timeInMillis < nowCal.timeInMillis

    val endMins = parseSubjectTimeMins(cls.endTime) ?: return false
    val nowMins = nowCal.get(Calendar.HOUR_OF_DAY) * 60 + nowCal.get(Calendar.MINUTE)
    return nowMins >= endMins
}

private fun parseSubjectTimeMins(time: String): Int? {
    val parts = time.split(":")
    if (parts.size != 2) return null
    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null
    return hour * 60 + minute
}
