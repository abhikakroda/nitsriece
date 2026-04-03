package com.example.timetable

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.timetable.data.AppRepository
import com.example.timetable.ui.MainScreen
import com.example.timetable.ui.AppViewModel
import com.example.timetable.ui.AppViewModelFactory
import com.example.timetable.notifications.NotificationChannels
import com.example.timetable.notifications.OngoingClassNotifier
import com.example.timetable.ui.theme.TimetableTheme
import com.google.firebase.messaging.FirebaseMessaging
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build

class MainActivity : ComponentActivity() {
    override fun onResume() {
        super.onResume()
        OngoingClassNotifier.updateAndSchedule(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = AppRepository(applicationContext)
        NotificationChannels.ensureCreated(this)
        OngoingClassNotifier.updateAndSchedule(this)

        // Auto-clean saved notifications older than the retention window.
        lifecycleScope.launch(Dispatchers.IO) {
            runCatching {
                repository.applyUiRefreshDefaultsIfNeeded()
                repository.applyVersionFiveDefaultsIfNeeded()
                repository.pruneSavedNotifications()
            }
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("MainActivity", "FCM token: ${task.result}")
            } else {
                Log.w("MainActivity", "Fetching FCM registration token failed", task.exception)
            }
        }
        
        FirebaseMessaging.getInstance().subscribeToTopic("all").addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("MainActivity", "Subscribed to FCM topic: all")
            } else {
                Log.e("MainActivity", "Failed to subscribe to FCM topic", task.exception)
            }
        }
        setContent {
            val context = LocalContext.current
            val notifPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
                onResult = { granted ->
                    Log.d("MainActivity", "POST_NOTIFICATIONS granted=$granted")
                }
            )

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= 33) {
                    val granted = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                    if (!granted) {
                        notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }

            val rememberedRepository = remember { repository }
            val appViewModel: AppViewModel = viewModel(factory = AppViewModelFactory(rememberedRepository))
            val darkMode by appViewModel.darkMode.collectAsStateWithLifecycle()
            val themeColor by appViewModel.themeColor.collectAsStateWithLifecycle()
            val minimalistMode by appViewModel.minimalistMode.collectAsStateWithLifecycle()
            val compactMode by appViewModel.compactMode.collectAsStateWithLifecycle()

            TimetableTheme(
                darkTheme = darkMode,
                themeColor = themeColor,
                isMinimalist = minimalistMode,
                compactMode = compactMode
            ) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MainScreen(appViewModel = appViewModel)
                }
            }
        }
    }
}
