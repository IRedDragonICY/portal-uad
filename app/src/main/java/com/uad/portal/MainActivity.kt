package com.uad.portal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.uad.portal.ui.theme.PortalUADTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var sessionManager: SessionManager
    private lateinit var auth: Auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(this)
        auth = Auth()
        setContent {
            PortalUADTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                )
                {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        AppContent()
                    }
                }
            }
        }
        lifecycleScope.launch {
            auth.autoLogin(sessionManager)
        }
    }


    @Composable
    private fun AppContent() {
        val isLoggedInState = remember { mutableStateOf(sessionManager.loadSession() != null) }
        val userInfoState = remember { mutableStateOf(sessionManager.loadSession()?.userInfo) }
        val coroutineScope = rememberCoroutineScope()
        val isAttendanceScreen = remember { mutableStateOf(false) }
        val isSettingsScreen = remember { mutableStateOf(false) }

        if (isAttendanceScreen.value) {
            AttendanceView(
                onBack = { isAttendanceScreen.value = false },
                getAttendanceInfo = { sessionCookie: String -> getAttendanceInfo(sessionCookie) },
                markAttendance = { sessionCookie, klsdtId, presklsId -> markAttendance(sessionCookie, klsdtId, presklsId) },
                sessionManager = sessionManager
            )
        } else if (isSettingsScreen.value) {
            SettingsView(onBack = { isSettingsScreen.value = false })
        } else if (isLoggedInState.value) {
            HomeView(
                userInfoState,
                isLoggedInState,
                coroutineScope,
                onAttendanceClick = { isAttendanceScreen.value = true },
                onSettingsClick = { isSettingsScreen.value = true },
                auth,
                sessionManager
            )
        } else {
            LoginView(userInfoState, isLoggedInState, coroutineScope, auth, sessionManager)
        }
    }

}