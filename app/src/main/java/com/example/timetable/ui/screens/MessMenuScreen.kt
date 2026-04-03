package com.example.timetable.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.timetable.data.*
import com.example.timetable.ui.AppViewModel
import com.example.timetable.ui.components.DashboardVibeContainer
import com.example.timetable.ui.components.pressScaleClickable
import com.example.timetable.ui.theme.*
import java.util.*

@Composable
fun MessMenuScreen(appViewModel: AppViewModel, onEditMenu: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val calendar = Calendar.getInstance()
    val today = when (calendar.get(Calendar.DAY_OF_WEEK)) {
        Calendar.SUNDAY -> AppFullDay.Sun
        Calendar.MONDAY -> AppFullDay.Mon
        Calendar.TUESDAY -> AppFullDay.Tue
        Calendar.WEDNESDAY -> AppFullDay.Wed
        Calendar.THURSDAY -> AppFullDay.Thu
        Calendar.FRIDAY -> AppFullDay.Fri
        Calendar.SATURDAY -> AppFullDay.Sat
        else -> AppFullDay.Mon
    }

    var selectedDay by remember { mutableStateOf(today) }
    var selectedMealFilter by remember { mutableStateOf<AppMealType?>(null) }
    val messMenu by appViewModel.messMenu.collectAsStateWithLifecycle()
    val messPref by appViewModel.messPref.collectAsStateWithLifecycle()
    val meals = messMenu[selectedDay] ?: emptyList()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)
    val nowMinutes = hour * 60 + minute

    val currentMealType = remember(nowMinutes) {
        when {
            nowMinutes < (9 * 60 + 30) -> AppMealType.breakfast
            nowMinutes < (14 * 60) -> AppMealType.lunch
            nowMinutes < (21 * 60) -> AppMealType.dinner
            else -> null
        }
    }
    val sliderDays = remember { listOf(AppFullDay.Mon, AppFullDay.Tue, AppFullDay.Wed, AppFullDay.Thu, AppFullDay.Fri, AppFullDay.Sat, AppFullDay.Sun) }
    val dayDates = remember(today) {
        val cal = Calendar.getInstance()
        val todayIdx = sliderDays.indexOf(today).coerceAtLeast(0)
        sliderDays.associateWith { day ->
            val dayIdx = sliderDays.indexOf(day)
            (cal.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, dayIdx - todayIdx)
            }.get(Calendar.DAY_OF_MONTH).toString()
        }
    }
    val mealsToShow = remember(selectedDay, meals, selectedMealFilter) {
        if (selectedMealFilter == null) meals else meals.filter { it.type == selectedMealFilter }
    }

    DashboardVibeContainer {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 18.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
        item {
            MessHeroCard(
                selectedDay = selectedDay.fullName,
                currentMealLabel = currentMealType?.name?.replaceFirstChar { it.uppercase() } ?: "No live meal",
                preference = if (messPref == "veg") "Vegetarian" else "Non-Vegetarian",
                onEditMenu = onEditMenu
            )
        }

        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(sliderDays.size) { index ->
                    val day = sliderDays[index]
                    val isSelected = selectedDay == day
                    val isToday = day == today
                    val bgColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface.copy(alpha = 0.84f)
                    val fgColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    val dateNum = dayDates[day].orEmpty()

                    Box(
                        modifier = Modifier
                            .widthIn(min = 78.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(bgColor)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                            .pressScaleClickable(
                                pressedScale = 0.98f,
                                hapticType = HapticFeedbackType.TextHandleMove
                            ) { selectedDay = day }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                day.shortName,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = fgColor
                            )
                            Text(
                                text = dateNum,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                                color = fgColor.copy(alpha = if (isSelected || isToday) 1f else 0.7f)
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

        if (selectedDay == today) {
            val currentMeal = currentMealType?.let { type -> mealsToShow.find { it.type == type } }
            if (currentMeal != null) {
                item {
                    CurrentMealBanner(currentMeal, currentMeal.type, messPref == "nonveg")
                }
            }
        }

        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    FilterChip(
                        selected = selectedMealFilter == null,
                        onClick = { selectedMealFilter = null },
                        label = { Text("All") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedMealFilter == AppMealType.breakfast,
                        onClick = { selectedMealFilter = AppMealType.breakfast },
                        label = { Text("Breakfast") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedMealFilter == AppMealType.lunch,
                        onClick = { selectedMealFilter = AppMealType.lunch },
                        label = { Text("Lunch") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedMealFilter == AppMealType.dinner,
                        onClick = { selectedMealFilter = AppMealType.dinner },
                        label = { Text("Dinner") }
                    )
                }
                if (selectedDay != today) {
                    item {
                        AssistChip(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedDay = today
                            },
                            label = { Text("Today") }
                        )
                    }
                }
            }
        }

        item {
            Text(
                when {
                    selectedDay != today -> "MEALS"
                    else -> "ALL MEALS"
                },
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
            )
        }

        if (mealsToShow.isEmpty()) {
            item {
                EmptyTodayMealsCard(isToday = selectedDay == today)
            }
        }

        itemsIndexed(mealsToShow) { index, meal ->
            val isLive = selectedDay == today && currentMealType != null && meal.type == currentMealType
            TimelineMealCard(meal, isLive, messPref == "nonveg", isLast = index == mealsToShow.lastIndex)
        }
        }
    }
}

@Composable
private fun MessHeroCard(
    selectedDay: String,
    currentMealLabel: String,
    preference: String,
    onEditMenu: () -> Unit
) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDarkTheme) 0.8f else 0.5f))
    ) {
        Column(
            modifier = Modifier
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
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.68f)
                            )
                        }
                    )
                )
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "Mess Menu",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        selectedDay,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .pressScaleClickable { onEditMenu() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Edit,
                        null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HeroPill(label = preference, containerColor = MaterialTheme.colorScheme.primaryContainer, textColor = MaterialTheme.colorScheme.primary)
                HeroPill(label = currentMealLabel, containerColor = WarningYellow.copy(alpha = 0.12f), textColor = WarningYellow)
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
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

@Composable
private fun EmptyTodayMealsCard(isToday: Boolean) {
    val title = if (isToday) "No Meals Added For Today" else "No Meals Added"
    val subtitle = if (isToday) {
        "No meal items are configured for today."
    } else {
        "No meal items are configured for this day."
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Current meal banner ──

@Composable
private fun CurrentMealBanner(meal: AppMeal, mealType: AppMealType, defaultNonVeg: Boolean) {
    val items = if (defaultNonVeg && meal.nonVegItems != null) meal.nonVegItems else meal.items
    val (icon, accent) = mealIconAndColor(mealType)
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(
            1.dp,
            accent.copy(alpha = if (isDarkTheme) 0.42f else 0.24f)
        )
    ) {
        Column(
            Modifier
                .background(
                    Brush.linearGradient(
                        colors = if (isDarkTheme) {
                            listOf(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.98f),
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                                accent.copy(alpha = 0.12f)
                            )
                        } else {
                            listOf(
                                MaterialTheme.colorScheme.surface,
                                accent.copy(alpha = 0.08f)
                            )
                        }
                    )
                )
                .padding(18.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(accent.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, null, tint = accent, modifier = Modifier.size(22.dp))
                    }
                    Column {
                        Text(
                            "Serving Now",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            meal.label,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(accent.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(
                            Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(accent)
                        )
                        Text(
                            "LIVE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = accent
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items.take(4).forEach { item ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.68f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            item,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }
                if (items.size > 4) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.68f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "+${items.size - 4}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ── Timeline meal card ──

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TimelineMealCard(
    meal: AppMeal,
    isLive: Boolean,
    defaultNonVeg: Boolean,
    isLast: Boolean
) {
    var showNonVeg by remember(meal.type, defaultNonVeg) { mutableStateOf(defaultNonVeg) }
    var expanded by remember(meal.type) { mutableStateOf(false) }
    val currentItems = if (showNonVeg && meal.nonVegItems != null) meal.nonVegItems else meal.items
    val visibleItems = if (expanded) currentItems else currentItems.take(6)
    val (icon, accentColor) = mealIconAndColor(meal.type)
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val timeText = when (meal.type) {
        AppMealType.breakfast -> "7:30 – 9:30 AM"
        AppMealType.lunch -> "12:30 – 2:30 PM"
        AppMealType.dinner -> "7:30 – 9:00 PM"
    }

    Row(modifier = Modifier.fillMaxWidth()) {
        // Timeline dot + line
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(
                        if (isLive) accentColor
                        else MaterialTheme.colorScheme.outlineVariant
                    )
                    .then(
                        if (isLive) Modifier.border(2.dp, accentColor.copy(alpha = 0.3f), CircleShape)
                        else Modifier
                    )
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(140.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        // Card
        Card(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isLive) {
                    if (isDarkTheme) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.98f) else accentColor.copy(alpha = 0.04f)
                } else {
                    if (isDarkTheme) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.96f) else MaterialTheme.colorScheme.surface
                }
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isLive) accentColor.copy(alpha = 0.2f)
                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDarkTheme) 0.8f else 0.5f)
            )
        ) {
            Column(Modifier.padding(16.dp)) {
                // Header
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(accentColor.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, null, tint = accentColor, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text(
                                meal.label,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Schedule,
                                    null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    timeText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Veg/Non-veg toggle
                    if (meal.nonVegItems != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (showNonVeg) ErrorRed.copy(alpha = 0.08f)
                                    else SuccessGreen.copy(alpha = 0.08f)
                                )
                                .clickable { showNonVeg = !showNonVeg }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                if (showNonVeg) "Non-Veg" else "Veg",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (showNonVeg) ErrorRed else SuccessGreen
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Food items as flow chips
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    visibleItems.forEach { item ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                item,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                AnimatedVisibility(
                    visible = currentItems.size > 6,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    TextButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(if (expanded) "Show less" else "Show ${currentItems.size - 6} more")
                    }
                }
            }
        }
    }
}

private fun mealIconAndColor(type: AppMealType): Pair<ImageVector, Color> = when (type) {
    AppMealType.breakfast -> Icons.Default.BakeryDining to WarningYellow
    AppMealType.lunch -> Icons.Default.LunchDining to SuccessGreen
    AppMealType.dinner -> Icons.Default.DinnerDining to InfoBlue
}
