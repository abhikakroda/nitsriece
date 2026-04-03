package com.example.timetable.data

import kotlinx.serialization.Serializable

@Serializable
data class SavedNotification(
    val id: String,
    val title: String,
    val body: String,
    val receivedAtMillis: Long,
    val sender: String? = null,
    val audience: String? = null, // e.g. "admin" or "all" (if backend sends it)
    val isAdmin: Boolean = false
)

