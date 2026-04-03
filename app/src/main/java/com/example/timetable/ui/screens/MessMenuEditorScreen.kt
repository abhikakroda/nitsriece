package com.example.timetable.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.timetable.data.AppFullDay
import com.example.timetable.data.AppMeal
import com.example.timetable.data.AppMealType
import com.example.timetable.ui.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessMenuEditorScreen(appViewModel: AppViewModel, onBack: () -> Unit) {
    val messMenu by appViewModel.messMenu.collectAsState()
    var working by remember(messMenu) { mutableStateOf(messMenu) }
    var selectedDay by remember { mutableStateOf(AppFullDay.Mon) }

    var showDialog by remember { mutableStateOf(false) }
    var editingType by remember { mutableStateOf<AppMealType?>(null) }
    var itemsText by remember { mutableStateOf("") }
    var hasNonVeg by remember { mutableStateOf(false) }
    var nonVegText by remember { mutableStateOf("") }

    fun openEditor(type: AppMealType) {
        editingType = type
        val meals = working[selectedDay] ?: emptyList()
        val meal = meals.firstOrNull { it.type == type }
            ?: AppMeal(type = type, label = type.name.replaceFirstChar { it.uppercase() }, emoji = "", items = emptyList())
        itemsText = listToText(meal.items)
        hasNonVeg = meal.nonVegItems != null
        nonVegText = listToText(meal.nonVegItems ?: emptyList())
        showDialog = true
    }

    fun saveMeal() {
        val type = editingType ?: return
        val meals = (working[selectedDay] ?: emptyList()).toMutableList()
        val idx = meals.indexOfFirst { it.type == type }
        val old = meals.getOrNull(idx)
        val label = old?.label ?: when (type) {
            AppMealType.breakfast -> "Breakfast"
            AppMealType.lunch -> "Lunch"
            AppMealType.dinner -> "Dinner"
        }
        val emoji = old?.emoji ?: when (type) {
            AppMealType.breakfast -> "☀️"
            AppMealType.lunch -> "🌤️"
            AppMealType.dinner -> "🌙"
        }
        val updated = AppMeal(
            type = type,
            label = label,
            emoji = emoji,
            items = textToList(itemsText),
            nonVegItems = if (hasNonVeg) textToList(nonVegText) else null
        )
        if (idx >= 0) meals[idx] = updated else meals.add(updated)
        val ordered = AppMealType.values().mapNotNull { t -> meals.firstOrNull { it.type == t } }
        working = working.toMutableMap().apply { put(selectedDay, ordered) }
        showDialog = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Mess Menu", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { appViewModel.resetMessMenu() }) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                    }
                    IconButton(onClick = { appViewModel.setMessMenu(working) }, enabled = working != messMenu) {
                        Icon(Icons.Default.Save, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
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
            // MessDayTab replacement
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                AppFullDay.values().forEach { day ->
                    val isSelected = selectedDay == day
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { selectedDay = day }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day.shortName,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    EditMealCard(title = "Breakfast", onClick = { openEditor(AppMealType.breakfast) })
                }
                item {
                    EditMealCard(title = "Lunch", onClick = { openEditor(AppMealType.lunch) })
                }
                item {
                    EditMealCard(title = "Dinner", onClick = { openEditor(AppMealType.dinner) })
                }
            }
        }
    }

    if (showDialog && editingType != null) {
        val type = editingType!!
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Edit ${type.name.replaceFirstChar { it.uppercase() }}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = itemsText,
                        onValueChange = { itemsText = it },
                        label = { Text("Items (one per line)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 6
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Non-Veg List", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        Switch(checked = hasNonVeg, onCheckedChange = { hasNonVeg = it })
                    }
                    if (hasNonVeg) {
                        OutlinedTextField(
                            value = nonVegText,
                            onValueChange = { nonVegText = it },
                            label = { Text("Non-veg items (one per line)") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 4
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { saveMeal() },
                    enabled = textToList(itemsText).isNotEmpty()
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun EditMealCard(title: String, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Tap to edit items", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    }
}

private fun textToList(text: String): List<String> =
    text.lines().map { it.trim() }.filter { it.isNotEmpty() }

private fun listToText(list: List<String>): String = list.joinToString("\n")
