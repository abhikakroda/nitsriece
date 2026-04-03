package com.example.timetable.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val notesJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

@Serializable
enum class NoteAttachmentKind {
    IMAGE,
    PDF,
    VOICE
}

@Serializable
data class NoteAttachmentRef(
    val id: String,
    val kind: NoteAttachmentKind,
    /** Shown in UI (e.g. filename or "Voice note"). */
    val displayName: String,
    /** File name inside the note attachment directory. */
    val storedFileName: String
)

@Serializable
data class StoredNotePayload(
    val body: String,
    val pinned: Boolean = false,
    val updatedAt: Long = 0L,
    val attachments: List<NoteAttachmentRef> = emptyList()
)

fun encodeStoredNote(
    body: String,
    pinned: Boolean,
    attachments: List<NoteAttachmentRef> = emptyList()
): String {
    return notesJson.encodeToString(
        StoredNotePayload(
            body = body.trim(),
            pinned = pinned,
            updatedAt = System.currentTimeMillis(),
            attachments = attachments
        )
    )
}

fun decodeStoredNote(raw: String): StoredNotePayload {
    val trimmed = raw.trim()
    return runCatching { notesJson.decodeFromString<StoredNotePayload>(trimmed) }
        .getOrElse { StoredNotePayload(body = trimmed, pinned = false, updatedAt = 0L, attachments = emptyList()) }
}
