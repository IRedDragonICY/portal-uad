package com.uad.portal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import coil.compose.rememberImagePainter
import com.uad.portal.ui.theme.PortalUADTheme
import kotlinx.coroutines.CoroutineScope
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
                onSettingsClick = { isSettingsScreen.value = true }
            )
        } else {
            LoginView(userInfoState, isLoggedInState, coroutineScope, auth, sessionManager)
        }
    }
    @Composable
    private fun HomeView(
        userInfoState: MutableState<UserInfo?>,
        isLoggedInState: MutableState<Boolean>,
        coroutineScope: CoroutineScope,
        onAttendanceClick: () -> Unit,
        onSettingsClick: () -> Unit

    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)) {
            Text("You are logged in as ${userInfoState.value?.username ?: "Unknown"}")
            Text("Your IPK is ${userInfoState.value?.ipk ?: "Unknown"}")
            Text("Your total SKS is ${userInfoState.value?.sks ?: "Unknown"}")
            userInfoState.value?.avatarUrl?.let {
                if (it.isNotEmpty()) {
                    Image(
                        painter = rememberImagePainter(it),
                        contentDescription = "User Avatar",
                        modifier = Modifier.size(100.dp)
                    )
                }
            }


            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onAttendanceClick) {
                Text("Absensi")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onSettingsClick) {
                Text("Settings")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                coroutineScope.launch {
                    val isLoggedOut = auth.logoutPortal()
                    if (isLoggedOut) {
                        sessionManager.clearSession()
                        isLoggedInState.value = false
                        userInfoState.value = UserInfo()
                    }
                }
            }) {
                Text("Logout")
            }
        }
    }
}