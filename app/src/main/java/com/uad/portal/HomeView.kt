package com.uad.portal

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun HomeView(
    userInfoState: MutableState<UserInfo?>,
    isLoggedInState: MutableState<Boolean>,
    coroutineScope: CoroutineScope,
    onAttendanceClick: () -> Unit,
    onSettingsClick: () -> Unit,
    auth: Auth,
    sessionManager: SessionManager
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