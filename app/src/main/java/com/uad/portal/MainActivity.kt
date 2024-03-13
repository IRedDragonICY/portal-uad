package com.uad.portal

import HomeView
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.uad.portal.ui.theme.PortalUADTheme
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    private val mainViewModel by lazy { ViewModelProvider(this)[MainViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainViewModel.initSessionManager(this)
        setContent {
            PortalUADTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        AppContent(mainViewModel)
                    }
                }
            }
        }

        val attendanceWorkRequest = PeriodicWorkRequestBuilder<AttendanceWorker>(2, TimeUnit.MINUTES).build()
        WorkManager.getInstance(this).enqueue(attendanceWorkRequest)

        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channelId = "attendance_notifications"
        val name = "Attendance Notifications"
        val descriptionText = "Notifications for attendance checks"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    @Composable
    private fun AppContent(mainViewModel: MainViewModel) {
        when {
            mainViewModel.isAttendanceScreen.value -> AttendanceView(mainViewModel)
            mainViewModel.isSettingsScreen.value -> SettingsView(mainViewModel)
            mainViewModel.isLoggedInState.value -> HomeView(mainViewModel)
            else -> LoginView(mainViewModel)
        }
    }
}
