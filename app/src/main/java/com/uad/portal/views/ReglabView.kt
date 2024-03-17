package com.uad.portal.views

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uad.portal.MainViewModel
import com.uad.portal.PracticumInfo

@Composable
fun ReglabView(mainViewModel: MainViewModel) {
    val reglab = mainViewModel.reglab
    val scheduleData = remember { mutableStateOf<PracticumInfo?>(null) }

    LaunchedEffect(Unit) {
        try {
            scheduleData.value = reglab.fetchScheduleData()
            println(scheduleData.value)
        } catch (e: Exception) {
            println("Failed to fetch schedule data: ${e.message}")
        }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)) {


        scheduleData.value?.data?.forEach { dataItem ->
            Text(text = "Mata Kuliah: ${dataItem.jadwal_praktikum.praktikum_aktif.matakuliah.nama}")
            Text(text = "Dosen: ${dataItem.jadwal_praktikum.dosen.nama}")
            Text(text = "Hari: ${dataItem.jadwal_praktikum.hari.nama}")
            Text(text = "Jam Mulai: ${dataItem.jadwal_praktikum.jam_mulai}")
            Text(text = "Jam Selesai: ${dataItem.jadwal_praktikum.jam_selesai}")
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)) {
        Button(onClick = { mainViewModel.navigate(Screen.Home) }) {
            Text("Back")
        }
    }
}


