package com.uad.portal

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsView(mainViewModel: MainViewModel) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)) {
        Button(onClick = { mainViewModel.onBackFromSettings() }) {
            Text("Kembali")
        }
    }
}