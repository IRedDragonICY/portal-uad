package com.uad.portal

import NetworkHandler
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.uad.portal.ui.theme.PortalUADTheme
import com.uad.portal.views.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val networkHandler = NetworkHandler(this)
        mainViewModel.networkHandler = networkHandler
        mainViewModel.initSessionManager(this)

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                launch {
                    delay(1000)
                    mainViewModel.networkHandler.isNetworkAvailable.observe(this@MainActivity) { isAvailable ->
                        if (isAvailable) {
                            mainViewModel.initAttendanceWorker(this@MainActivity)
                            Toast.makeText(this@MainActivity, "Internet connection is now available", Toast.LENGTH_LONG).show()
                        } else if (!isAvailable) {
                            Toast.makeText(this@MainActivity, "Internet connection is not available", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }

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

    override fun onStart() {
        super.onStart()
        mainViewModel.networkHandler.registerNetworkCallback()
    }

    override fun onStop() {
        super.onStop()
        mainViewModel.networkHandler.unregisterNetworkCallback()
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
