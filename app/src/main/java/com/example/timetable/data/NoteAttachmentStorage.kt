package com.example.timetable.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.UUID

object NoteAttachmentStorage {
    private const val ROOT = "note_att"

    fun folderNameForNoteKey(noteKey: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(noteKey.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }.take(16)
    }

    fun newPendingSession(): String = "p_" + UUID.randomUUID().toString().replace("-", "").take(12)

    fun sessionKeyForDraft(originalNoteKey: String?): String {
        return if (originalNoteKey != null) folderNameForNoteKey(originalNoteKey) else newPendingSession()
    }

    private fun rootDir(context: Context): File = File(context.filesDir, ROOT)

    fun attachmentDir(context: Context, sessionKey: String): File {
        return File(rootDir(context), sessionKey).apply { mkdirs() }
    }

    fun fileForAttachment(context: Context, noteKey: String, ref: NoteAttachmentRef): File {
        val dir = attachmentDir(context, folderNameForNoteKey(noteKey))
        return File(dir, ref.storedFileName)
    }

    fun fileForSessionAttachment(context: Context, sessionKey: String, ref: NoteAttachmentRef): File {
        return File(attachmentDir(context, sessionKey), ref.storedFileName)
    }

    fun deleteFolderForNoteKey(context: Context, noteKey: String) {
        val dir = File(rootDir(context), folderNameForNoteKey(noteKey))
        if (dir.exists()) dir.deleteRecursively()
    }

    fun deleteSessionFolder(context: Context, sessionKey: String) {
        val dir = File(rootDir(context), sessionKey)
        if (dir.exists()) dir.deleteRecursively()
    }

    fun fileProviderUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    /**
     * After picking a file, copy into the draft session folder and return a ref.
     */
    fun importFromUri(
        context: Context,
        sessionKey: String,
        uri: Uri,
        kind: NoteAttachmentKind
    ): NoteAttachmentRef? {
        return runCatching {
            val cr = context.contentResolver
            val displayName = queryDisplayName(cr, uri) ?: when (kind) {
                NoteAttachmentKind.IMAGE -> "image_${System.currentTimeMillis()}.jpg"
                NoteAttachmentKind.PDF -> "document_${System.currentTimeMillis()}.pdf"
                NoteAttachmentKind.VOICE -> "voice_${System.currentTimeMillis()}.m4a"
            }
            val extFromName = displayName.substringAfterLast('.', "")
            val ext = when {
                extFromName.isNotBlank() -> extFromName.lowercase()
                kind == NoteAttachmentKind.PDF -> "pdf"
                kind == NoteAttachmentKind.VOICE -> "m4a"
                else -> "jpg"
            }
            val id = UUID.randomUUID().toString().replace("-", "").take(10)
            val base = sanitizeFileName(displayName.substringBeforeLast('.').ifBlank { "file" }).take(48)
            val storedFileName = "${id}_$base.$ext"
            val dest = File(attachmentDir(context, sessionKey), storedFileName)
            cr.openInputStream(uri)?.use { input ->
                FileOutputStream(dest).use { output -> input.copyTo(output) }
            } ?: return null
            NoteAttachmentRef(
                id = id,
                kind = kind,
                displayName = displayName.take(120),
                storedFileName = storedFileName
            )
        }.getOrNull()
    }

    fun createVoiceAttachment(
        context: Context,
        sessionKey: String,
        audioFile: File
    ): NoteAttachmentRef? {
        if (!audioFile.exists()) return null
        val id = UUID.randomUUID().toString().replace("-", "").take(10)
        val storedFileName = "${id}_voice.m4a"
        val dest = File(attachmentDir(context, sessionKey), storedFileName)
        runCatching {
            if (dest.exists()) dest.delete()
            audioFile.copyTo(dest, overwrite = true)
            audioFile.delete()
        }.onFailure { return null }
        return NoteAttachmentRef(
            id = id,
            kind = NoteAttachmentKind.VOICE,
            displayName = "Voice note",
            storedFileName = storedFileName
        )
    }

    /**
     * Renames/moves the session folder to match [finalNoteKey] (hash of full map key).
     */
    fun finalizeSessionToNoteKey(context: Context, sessionKey: String, finalNoteKey: String) {
        val targetName = folderNameForNoteKey(finalNoteKey)
        if (sessionKey == targetName) return
        val root = rootDir(context)
        val from = File(root, sessionKey)
        if (!from.exists()) return
        val to = File(root, targetName)
        if (!to.exists()) {
            from.renameTo(to)
            return
        }
        from.listFiles()?.forEach { file ->
            val dest = File(to, file.name)
            if (dest.exists()) dest.delete()
            file.renameTo(dest)
        }
        from.deleteRecursively()
    }

    private fun queryDisplayName(cr: android.content.ContentResolver, uri: Uri): String? {
        if (uri.scheme == android.content.ContentResolver.SCHEME_CONTENT) {
            cr.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
                if (c.moveToFirst()) {
                    val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (idx >= 0) return c.getString(idx)
                }
            }
        }
        return uri.lastPathSegment
    }

    private fun sanitizeFileName(name: String): String {
        return name.replace(Regex("[^a-zA-Z0-9._-]"), "_").take(80)
    }
}
