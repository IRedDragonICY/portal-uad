package com.uad.portal

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.uad.portal.ui.theme.PortalUADTheme

class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainViewModel.initSessionManager(this)

        mainViewModel.isNetworkAvailable.observe(this) { isAvailable ->
            if (isAvailable) {
                mainViewModel.initAttendanceWorker(this)
            } else {
                Toast.makeText(this, "Internet connection is not available", Toast.LENGTH_LONG).show()
            }
        }
        mainViewModel.checkNetworkAvailability(this)

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
        when (mainViewModel.currentScreen.value) {
            Screen.Attendance -> AttendanceView(mainViewModel)
            Screen.Settings -> SettingsView(mainViewModel)
            Screen.Reglab -> ReglabView(mainViewModel)
            Screen.Home -> if (mainViewModel.isLoggedInState.value) HomeView(mainViewModel) else LoginView(mainViewModel)
        }
    }
}