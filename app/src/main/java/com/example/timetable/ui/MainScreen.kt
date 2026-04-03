package com.example.timetable.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.timetable.ui.components.GlassCard
import com.example.timetable.ui.components.LiquidGlassSurface
import com.example.timetable.ui.screens.AnalyticsScreen
import com.example.timetable.ui.screens.DashboardScreen
import com.example.timetable.ui.screens.ExamDateEditorScreen
import com.example.timetable.ui.screens.MessMenuEditorScreen
import com.example.timetable.ui.screens.MessMenuScreen
import com.example.timetable.ui.screens.NotesScreen
import com.example.timetable.ui.screens.NotificationsScreen
import com.example.timetable.ui.screens.PdfViewerScreen
import com.example.timetable.ui.screens.ProfileScreen
import com.example.timetable.ui.screens.ReminderSettingsScreen
import com.example.timetable.ui.screens.ScheduleScreen
import com.example.timetable.ui.screens.SyllabusScreen
import com.example.timetable.ui.screens.TimetableEditorScreen
import com.example.timetable.ui.screens.WeeklyScheduleEditorScreen
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Home", Icons.Default.Home)
    object Schedule : Screen("schedule", "Calendar", Icons.Default.DateRange)
    object MessMenu : Screen("mess_menu", "Mess", Icons.Default.Restaurant)
    object Analytics : Screen("analytics", "Analytics", Icons.Default.Analytics)
    object Profile : Screen("profile", "Profile", Icons.Default.Settings)
    object Notifications : Screen("notifications", "Alerts", Icons.Default.Notifications)
    object WeeklyScheduleEditor : Screen("weekly_schedule_editor", "Weekly Schedule", Icons.Default.DateRange)
    object TimetableEditor : Screen("timetable_editor", "Edit Timetable", Icons.Default.DateRange)
    object MessMenuEditor : Screen("mess_menu_editor", "Edit Menu", Icons.Default.Restaurant)
    object ExamDateEditor : Screen("exam_date_editor", "Exam Planner", Icons.Default.DateRange)
    object ReminderSettings : Screen("reminder_settings", "Reminders", Icons.Default.Notifications)
    object Syllabus : Screen("syllabus", "Library", Icons.Default.Book)
    object Notes : Screen("notes", "Notes", Icons.Default.NoteAlt)
    object PdfViewer : Screen("pdf_viewer", "PDF", Icons.Default.Book)
}

private val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Schedule,
    Screen.Analytics,
    Screen.MessMenu
)

private val screenCatalog = listOf(
    Screen.Dashboard,
    Screen.Schedule,
    Screen.MessMenu,
    Screen.Analytics,
    Screen.Profile,
    Screen.Notifications,
    Screen.WeeklyScheduleEditor,
    Screen.TimetableEditor,
    Screen.MessMenuEditor,
    Screen.ExamDateEditor,
    Screen.ReminderSettings,
    Screen.Syllabus,
    Screen.Notes,
    Screen.PdfViewer
)

private data class DrawerDestination(
    val screen: Screen,
    val subtitle: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(appViewModel: AppViewModel) {
    val navController = rememberNavController()
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val compactMode by appViewModel.compactMode.collectAsStateWithLifecycle()
    val currentDestination = navBackStackEntry?.destination
    val currentScreen = remember(currentDestination) {
        screenCatalog.firstOrNull { screen ->
            currentDestination?.hierarchy?.any { it.route == screen.route } == true
        }
    }
    val primaryRoutes = remember(bottomNavItems) { bottomNavItems.map { it.route }.toSet() }
    val isPrimaryRoute = currentDestination?.hierarchy?.any { it.route in primaryRoutes } == true
    val showBottomBar = isPrimaryRoute
    val drawerItems = remember {
        listOf(
            DrawerDestination(Screen.MessMenu, "Meals and hostel menu"),
            DrawerDestination(Screen.Notifications, "Announcements and reminders"),
            DrawerDestination(Screen.Syllabus, "Documents and study material"),
            DrawerDestination(Screen.Profile, "Preferences and configuration")
        )
    }

    fun navigateToPrimary(screen: Screen) {
        if (screen == Screen.Dashboard) {
            val popped = navController.popBackStack(Screen.Dashboard.route, inclusive = false)
            if (!popped) {
                navController.navigate(Screen.Dashboard.route) {
                    launchSingleTop = true
                }
            }
        } else {
            navController.navigate(screen.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = isPrimaryRoute,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.85f),
                drawerContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                drawerContentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.98f))
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    DrawerHeroCard()
                    Spacer(modifier = Modifier.height(18.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    drawerItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true
                        NavigationDrawerItem(
                            modifier = Modifier.padding(top = 10.dp),
                            label = {
                                Column {
                                    Text(item.screen.label, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        text = item.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            },
                            selected = selected,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                scope.launch { drawerState.close() }
                                navController.navigate(item.screen.route) {
                                    launchSingleTop = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.screen.icon,
                                    contentDescription = item.screen.label
                                )
                            },
                            shape = RoundedCornerShape(22.dp),
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                unselectedContainerColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {},
            bottomBar = {
                AnimatedVisibility(
                    visible = showBottomBar,
                    enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut()
                ) {
                    ShellBottomDock(
                        currentDestination = currentDestination,
                        compactMode = compactMode,
                        onSelect = { screen ->
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            navigateToPrimary(screen)
                        }
                    )
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = innerPadding.calculateTopPadding(),
                        bottom = innerPadding.calculateBottomPadding()
                    )
            ) {
                composable(Screen.Dashboard.route) {
                    DashboardScreen(
                        appViewModel = appViewModel,
                        onOpenMenu = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            scope.launch { drawerState.open() }
                        },
                        onNavigateToSettings = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            navController.navigate(Screen.Profile.route)
                        },
                        onNavigateToReminderSettings = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            navController.navigate(Screen.ReminderSettings.route)
                        },
                        onNavigateToMessMenu = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            navController.navigate(Screen.MessMenu.route) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToAnalytics = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            navController.navigate(Screen.Analytics.route) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToSyllabus = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            navController.navigate(Screen.Syllabus.route)
                        },
                        onNavigateToExamPlanner = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            navController.navigate(Screen.ExamDateEditor.route)
                        },
                        onNavigateToNotes = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            navigateToPrimary(Screen.Notes)
                        },
                        onNavigateToNotifications = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            navController.navigate(Screen.Notifications.route)
                        }
                    )
                }
                composable(Screen.Schedule.route) {
                    ScheduleScreen(
                        appViewModel = appViewModel,
                        onEditTimetable = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            navController.navigate(Screen.TimetableEditor.route)
                        },
                        onEditWeeklySchedule = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            navController.navigate(Screen.WeeklyScheduleEditor.route)
                        }
                    )
                }
                composable(Screen.MessMenu.route) {
                    MessMenuScreen(
                        appViewModel = appViewModel,
                        onEditMenu = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            navController.navigate(Screen.MessMenuEditor.route)
                        }
                    )
                }
                composable(Screen.Analytics.route) {
                    AnalyticsScreen(appViewModel = appViewModel)
                }
                composable(Screen.Profile.route) {
                    ProfileScreen(
                        appViewModel = appViewModel,
                        onBack = { navController.popBackStack() },
                        onNavigateToEditTimetable = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            navController.navigate(Screen.TimetableEditor.route)
                        },
                        onNavigateToEditWeeklySchedule = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            navController.navigate(Screen.WeeklyScheduleEditor.route)
                        },
                        onNavigateToEditMessMenu = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            navController.navigate(Screen.MessMenuEditor.route)
                        },
                        onNavigateToExamDate = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            navController.navigate(Screen.ExamDateEditor.route)
                        }
                    )
                }
                composable(Screen.WeeklyScheduleEditor.route) {
                    WeeklyScheduleEditorScreen(appViewModel = appViewModel, onBack = { navController.popBackStack() })
                }
                composable(Screen.TimetableEditor.route) {
                    TimetableEditorScreen(appViewModel = appViewModel, onBack = { navController.popBackStack() })
                }
                composable(Screen.MessMenuEditor.route) {
                    MessMenuEditorScreen(appViewModel = appViewModel, onBack = { navController.popBackStack() })
                }
                composable(Screen.ExamDateEditor.route) {
                    ExamDateEditorScreen(appViewModel = appViewModel, onBack = { navController.popBackStack() })
                }
                composable(Screen.ReminderSettings.route) {
                    ReminderSettingsScreen(appViewModel = appViewModel, onBack = { navController.popBackStack() })
                }
                composable(Screen.Syllabus.route) {
                    SyllabusScreen(
                        appViewModel = appViewModel,
                        onBack = { navController.popBackStack() },
                        onOpenPdf = { subject, uriString ->
                            navController.currentBackStackEntry?.savedStateHandle?.set("pdf_subject", subject)
                            navController.currentBackStackEntry?.savedStateHandle?.set("pdf_uri", uriString)
                            navController.navigate(Screen.PdfViewer.route)
                        }
                    )
                }
                composable(Screen.Notes.route) {
                    NotesScreen(
                        appViewModel = appViewModel,
                        onBack = { navController.popBackStack() },
                        useShellHeader = false
                    )
                }
                composable(Screen.PdfViewer.route) {
                    val previousEntry = navController.previousBackStackEntry
                    val subject = previousEntry?.savedStateHandle?.get<String>("pdf_subject").orEmpty()
                    val uriString = previousEntry?.savedStateHandle?.get<String>("pdf_uri").orEmpty()
                    PdfViewerScreen(
                        subject = subject,
                        uriString = uriString,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Screen.Notifications.route) {
                    NotificationsScreen(
                        appViewModel = appViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

@Composable
private fun DrawerHeroCard() {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp)) {
            Text(
                text = "Timetable",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "A rebuilt campus dashboard with a cleaner shell, stronger contrast, and faster navigation.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ShellTopBar(
    title: String,
    subtitle: String,
    showBack: Boolean,
    onBack: () -> Unit,
    onMenu: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LiquidGlassSurface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            blurRadius = 14.dp,
            elevation = 10.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = if (showBack) onBack else onMenu,
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(
                        imageVector = if (showBack) Icons.AutoMirrored.Filled.ArrowBackIos else Icons.Default.Menu,
                        contentDescription = if (showBack) "Back" else "Menu",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun ShellBottomDock(
    currentDestination: androidx.navigation.NavDestination?,
    compactMode: Boolean,
    onSelect: (Screen) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = if (compactMode) 12.dp else 16.dp, vertical = if (compactMode) 8.dp else 10.dp),
        shape = RoundedCornerShape(30.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
        tonalElevation = 0.dp,
        shadowElevation = 12.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (compactMode) 6.dp else 8.dp, vertical = if (compactMode) 6.dp else 8.dp),
            horizontalArrangement = Arrangement.spacedBy(if (compactMode) 4.dp else 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomNavItems.forEach { screen ->
                val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(22.dp),
                    color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f) else Color.Transparent,
                    border = BorderStroke(
                        1.dp,
                        if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.28f) else Color.Transparent
                    ),
                    onClick = { if (!selected) onSelect(screen) }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = if (compactMode) 6.dp else 8.dp, vertical = if (compactMode) 6.dp else 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(if (compactMode) 2.dp else 4.dp)
                    ) {
                        Icon(
                            imageVector = screen.icon,
                            contentDescription = screen.label,
                            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(if (compactMode) 20.dp else 22.dp)
                        )
                        Text(
                            text = screen.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

private fun shellSubtitle(screen: Screen?): String = when (screen) {
    Screen.Dashboard -> "Live campus overview"
    Screen.Schedule -> "Timetable and focus view"
    Screen.Notes -> "Saved class notes"
    Screen.Analytics -> "Attendance insights"
    Screen.MessMenu -> "Meals and mess plan"
    Screen.Profile -> "Preferences and setup"
    Screen.Notifications -> "Alerts and announcements"
    Screen.Syllabus -> "Library and PDFs"
    Screen.ReminderSettings -> "Notification timing"
    Screen.TimetableEditor -> "Manage class slots"
    Screen.WeeklyScheduleEditor -> "Weekly structure"
    Screen.MessMenuEditor -> "Edit meal timeline"
    Screen.ExamDateEditor -> "Exam planner"
    Screen.PdfViewer -> "Document preview"
    null -> "Campus command center"
}
