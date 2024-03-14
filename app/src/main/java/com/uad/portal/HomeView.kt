package com.uad.portal

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter

@Composable
fun HomeView(mainViewModel: MainViewModel) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 32.dp)) {
        Text("You are logged in as ${mainViewModel.userInfoState.value?.username ?: "Unknown"}")
        Text("Your IPK is ${mainViewModel.userInfoState.value?.ipk ?: "Unknown"}")
        Text("Your total SKS is ${mainViewModel.userInfoState.value?.sks ?: "Unknown"}")
        mainViewModel.userInfoState.value?.avatarUrl?.let {
            if (it.isNotEmpty()) {
                Image(
                    painter = rememberImagePainter(it),
                    contentDescription = "User Avatar",
                    modifier = Modifier.size(100.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { mainViewModel.navigate(Screen.Attendance) }) {
            Text("Absensi")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { mainViewModel.navigate(Screen.Settings) }) {
            Text("Settings")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = mainViewModel::logout) {
            Text("Logout")
        }
    }
}
