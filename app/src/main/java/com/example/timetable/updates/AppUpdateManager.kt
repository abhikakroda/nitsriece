package com.example.timetable.updates

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import com.example.timetable.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

private const val ApkMimeType = "application/vnd.android.package-archive"

@Serializable
data class AppUpdateManifest(
    @SerialName("versionCode") val versionCode: Int,
    @SerialName("versionName") val versionName: String,
    @SerialName("apkUrl") val apkUrl: String,
    @SerialName("notes") val notes: String = "",
    @SerialName("title") val title: String? = null
)

enum class AppUpdateDownloadStatus {
    Idle,
    Pending,
    Running,
    Paused,
    Successful,
    Failed
}

object AppUpdateManager {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    suspend fun fetchManifest(manifestUrl: String): AppUpdateManifest = withContext(Dispatchers.IO) {
        require(manifestUrl.isNotBlank()) { "Update feed URL is empty." }
        val connection = (URL(manifestUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 15_000
            setRequestProperty("Accept", "application/json")
        }
        try {
            val code = connection.responseCode
            if (code !in 200..299) {
                error("Update server returned HTTP $code.")
            }
            val payload = connection.inputStream.bufferedReader().use { it.readText() }
            val manifest = json.decodeFromString<AppUpdateManifest>(payload)
            require(manifest.apkUrl.startsWith("http")) { "APK URL must be http or https." }
            manifest
        } finally {
            connection.disconnect()
        }
    }

    fun isUpdateAvailable(manifest: AppUpdateManifest): Boolean =
        manifest.versionCode > BuildConfig.VERSION_CODE

    fun enqueueDownload(context: Context, manifest: AppUpdateManifest): Long {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val safeVersion = manifest.versionName.ifBlank { manifest.versionCode.toString() }
            .replace(Regex("[^A-Za-z0-9._-]"), "-")
        val request = DownloadManager.Request(Uri.parse(manifest.apkUrl))
            .setTitle("Downloading ${manifest.title ?: "Timetable"} $safeVersion")
            .setDescription("Latest release package")
            .setMimeType(ApkMimeType)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
            .setDestinationInExternalFilesDir(
                context,
                Environment.DIRECTORY_DOWNLOADS,
                "timetable-update-$safeVersion.apk"
            )
        return downloadManager.enqueue(request)
    }

    fun getDownloadStatus(context: Context, downloadId: Long): AppUpdateDownloadStatus {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query().setFilterById(downloadId)
        downloadManager.query(query).use { cursor ->
            if (!cursor.moveToFirst()) return AppUpdateDownloadStatus.Failed
            return when (cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))) {
                DownloadManager.STATUS_PENDING -> AppUpdateDownloadStatus.Pending
                DownloadManager.STATUS_RUNNING -> AppUpdateDownloadStatus.Running
                DownloadManager.STATUS_PAUSED -> AppUpdateDownloadStatus.Paused
                DownloadManager.STATUS_SUCCESSFUL -> AppUpdateDownloadStatus.Successful
                else -> AppUpdateDownloadStatus.Failed
            }
        }
    }

    fun promptInstallDownloadedApk(context: Context, downloadId: Long): Boolean {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val status = getDownloadStatus(context, downloadId)
        if (status != AppUpdateDownloadStatus.Successful) return false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !context.packageManager.canRequestPackageInstalls()) {
            val permissionIntent = Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}")
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(permissionIntent)
            return false
        }

        val apkUri = downloadManager.getUriForDownloadedFile(downloadId) ?: return false
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, ApkMimeType)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(installIntent)
        return true
    }
}
