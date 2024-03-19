package com.uad.portal.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.uad.portal.MainViewModel


@Composable
fun HomeView(mainViewModel: MainViewModel) {
    val ipkVisible = remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            mainViewModel.userInfoState.value?.avatarUrl?.let {
                if (it.isNotEmpty()) {
                    Image(
                        painter = rememberImagePainter(it),
                        contentDescription = "User Avatar",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text("Halo, ${mainViewModel.userInfoState.value?.username ?: "Unknown"} 🤚", style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .clickable { ipkVisible.value = !ipkVisible.value },
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (ipkVisible.value) {
                        Text("IPK: ${mainViewModel.userInfoState.value?.ipk ?: "Unknown"}", style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold))
                    } else {
                        Text("Tekan untuk melihat IPK", style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)) // Add this line
                    }
                }
            }

            Spacer(modifier = Modifier.width(24.dp))

            Card(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("Total SKS: ${mainViewModel.userInfoState.value?.sks ?: "Unknown"}", style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { mainViewModel.navigate(Screen.Attendance) }) {
                Icon(Icons.Filled.CalendarToday, contentDescription = "Absensi")
            }

            IconButton(onClick = { mainViewModel.navigate(Screen.Reglab) }) {
                Icon(Icons.Filled.Book, contentDescription = "Reglab")
            }

            IconButton(onClick = { mainViewModel.navigate(Screen.Settings) }) {
                Icon(Icons.Filled.Settings, contentDescription = "Settings")
            }

            IconButton(onClick = mainViewModel::logout) {
                Icon(Icons.Filled.ExitToApp, contentDescription = "Logout")
            }
        }
    }
}
