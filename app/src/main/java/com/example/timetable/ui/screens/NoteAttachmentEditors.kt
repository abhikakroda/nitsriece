package com.example.timetable.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.MediaRecorder
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.timetable.data.NoteAttachmentKind
import com.example.timetable.data.NoteAttachmentRef
import com.example.timetable.data.NoteAttachmentStorage
import android.media.MediaPlayer
import java.io.File

@Composable
fun EditNoteAttachmentsSection(
    sessionKey: String,
    attachments: List<NoteAttachmentRef>,
    onAttachmentsChange: (List<NoteAttachmentRef>) -> Unit,
    onOpenPdf: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var previewImageUri by remember { mutableStateOf<Uri?>(null) }
    var voicePlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose {
            voicePlayer?.release()
            voicePlayer = null
        }
    }
    var isRecording by remember { mutableStateOf(false) }
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var recordingFile by remember { mutableStateOf<File?>(null) }

    val pickImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val ref = NoteAttachmentStorage.importFromUri(context, sessionKey, uri, NoteAttachmentKind.IMAGE)
            if (ref != null) onAttachmentsChange(attachments + ref)
        }
    }

    val pickPdf = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val ref = NoteAttachmentStorage.importFromUri(context, sessionKey, uri, NoteAttachmentKind.PDF)
            if (ref != null) onAttachmentsChange(attachments + ref)
        }
    }

    val requestMic = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted && !isRecording) startVoiceRecording(
            context = context,
            sessionKey = sessionKey,
            onRecorderReady = { mr, file ->
                mediaRecorder = mr
                recordingFile = file
                isRecording = true
            }
        )
    }

    fun stopVoice() {
        val mr = mediaRecorder
        val file = recordingFile
        mediaRecorder = null
        recordingFile = null
        isRecording = false
        if (mr != null) {
            runCatching {
                mr.stop()
            }
            runCatching {
                mr.release()
            }
        }
        if (file != null && file.exists()) {
            val ref = NoteAttachmentStorage.createVoiceAttachment(context, sessionKey, file)
            if (ref != null) onAttachmentsChange(attachments + ref)
        }
    }

    fun toggleRecording() {
        if (isRecording) {
            stopVoice()
            return
        }
        val has = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        if (!has) {
            requestMic.launch(Manifest.permission.RECORD_AUDIO)
            return
        }
        startVoiceRecording(context, sessionKey) { mr, file ->
            mediaRecorder = mr
            recordingFile = file
            isRecording = true
        }
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Attachments",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = {
                    pickImage.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            ) {
                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                Text("Image", modifier = Modifier.padding(start = 6.dp))
            }
            OutlinedButton(onClick = { pickPdf.launch("application/pdf") }) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                Text("PDF", modifier = Modifier.padding(start = 6.dp))
            }
            OutlinedButton(onClick = { toggleRecording() }) {
                Icon(
                    imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    if (isRecording) "Stop" else "Voice",
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        }

        if (attachments.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                attachments.forEach { ref ->
                    val file = NoteAttachmentStorage.fileForSessionAttachment(context, sessionKey, ref)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        when (ref.kind) {
                            NoteAttachmentKind.IMAGE -> {
                                val bmp = remember(ref.id, file.absolutePath) {
                                    runCatching {
                                        BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
                                    }.getOrNull()
                                }
                                if (bmp != null) {
                                    Image(
                                        bitmap = bmp,
                                        contentDescription = ref.displayName,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable {
                                                previewImageUri = NoteAttachmentStorage.fileProviderUri(
                                                    context,
                                                    file
                                                )
                                            },
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                            NoteAttachmentKind.PDF -> {
                                AssistChip(
                                    onClick = {
                                        val uri = NoteAttachmentStorage.fileProviderUri(context, file).toString()
                                        onOpenPdf(ref.displayName.ifBlank { "PDF" }, uri)
                                    },
                                    label = {
                                        Text(
                                            ref.displayName.ifBlank { "PDF" },
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                )
                            }
                            NoteAttachmentKind.VOICE -> {
                                AssistChip(
                                    onClick = {
                                        voicePlayer?.release()
                                        voicePlayer = MediaPlayer().apply {
                                            setDataSource(file.absolutePath)
                                            prepare()
                                            start()
                                            setOnCompletionListener { release() }
                                        }
                                    },
                                    label = { Text(ref.displayName, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                )
                            }
                        }
                        IconButton(onClick = {
                            runCatching { if (file.exists()) file.delete() }
                            onAttachmentsChange(attachments.filterNot { it.id == ref.id })
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Remove attachment")
                        }
                    }
                }
            }
        }
    }

    previewImageUri?.let { uri ->
        AlertDialog(
            onDismissRequest = { previewImageUri = null },
            confirmButton = {
                TextButton(onClick = { previewImageUri = null }) {
                    Text("Close")
                }
            },
            text = {
                val pfd = remember(uri) {
                    runCatching {
                        context.contentResolver.openFileDescriptor(uri, "r")
                    }.getOrNull()
                }
                val bmp = remember(uri) {
                    pfd?.use { descriptor ->
                        BitmapFactory.decodeFileDescriptor(descriptor.fileDescriptor)?.asImageBitmap()
                    }
                }
                if (bmp != null) {
                    Image(
                        bitmap = bmp,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text("Could not load image")
                }
            }
        )
    }
}

private fun startVoiceRecording(
    context: android.content.Context,
    sessionKey: String,
    onRecorderReady: (MediaRecorder, File) -> Unit
) {
    val file = File(NoteAttachmentStorage.attachmentDir(context, sessionKey), "rec_${System.currentTimeMillis()}.m4a")
    val mr = MediaRecorder(context).apply {
        setAudioSource(MediaRecorder.AudioSource.MIC)
        setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        setOutputFile(file.absolutePath)
        prepare()
        start()
    }
    onRecorderReady(mr, file)
}
