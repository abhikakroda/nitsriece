package com.example.timetable.ui.screens

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.ExposedDropdownMenuBoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import java.util.Locale

internal fun canonicalizeSubjectName(input: String, existingSubjects: Collection<String>): String {
    val trimmed = input.trim()
    if (trimmed.isEmpty()) return trimmed

    val normalizedInput = normalizeSubjectKey(trimmed)
    val inputInitials = subjectInitials(trimmed)

    val candidates = existingSubjects
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .distinct()

    candidates.firstOrNull { normalizeSubjectKey(it) == normalizedInput }?.let { return it }
    candidates.firstOrNull { subjectInitials(it) == inputInitials && inputInitials.isNotEmpty() }?.let { return it }
    candidates.firstOrNull {
        val candidateKey = normalizeSubjectKey(it)
        candidateKey.contains(normalizedInput) || normalizedInput.contains(candidateKey)
    }?.let { return it }

    return trimmed
}

internal fun subjectSuggestions(input: String, existingSubjects: Collection<String>): List<String> {
    val trimmed = input.trim()
    if (trimmed.isEmpty()) return emptyList()

    val normalizedInput = normalizeSubjectKey(trimmed)
    val inputInitials = subjectInitials(trimmed)

    return existingSubjects
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .distinct()
        .filterNot { it.equals(trimmed, ignoreCase = true) }
        .filter {
            val key = normalizeSubjectKey(it)
            val initials = subjectInitials(it)
            key.contains(normalizedInput) ||
                normalizedInput.contains(key) ||
                (inputInitials.isNotEmpty() && initials == inputInitials)
        }
        .sortedBy { normalizeSubjectKey(it).length }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SubjectInputField(
    value: String,
    onValueChange: (String) -> Unit,
    suggestions: List<String>,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(value, suggestions) {
        expanded = value.isNotBlank() && suggestions.isNotEmpty()
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it && suggestions.isNotEmpty() }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text("Subject") },
            singleLine = true,
            trailingIcon = {
                if (suggestions.isNotEmpty()) {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            },
            modifier = modifier.menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            suggestions.take(6).forEach { suggestion ->
                DropdownMenuItem(
                    text = { Text(suggestion) },
                    onClick = {
                        onValueChange(suggestion)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun normalizeSubjectKey(value: String): String =
    value
        .lowercase(Locale.getDefault())
        .replace(Regex("[^a-z0-9]+"), " ")
        .trim()
        .replace(Regex("\\s+"), " ")

private fun subjectInitials(value: String): String =
    normalizeSubjectKey(value)
        .split(" ")
        .filter { it.isNotEmpty() }
        .joinToString("") { it.first().toString() }
