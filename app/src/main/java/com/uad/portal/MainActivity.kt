package com.uad.portal

import HomeView
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
import com.uad.portal.ui.theme.PortalUADTheme

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
