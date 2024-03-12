package com.uad.portal

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class Attendance(
    val courseClass: String,
    val semester: String,
    val meetingNumber: Int,
    val meetingDate: String,
    val material: String,
    val attendanceStart: String,
    val attendanceStatus: String,
    val information: String,
    val klsdtId: String,
    val presklsId: String
)

@Composable
fun AttendanceView(mainViewModel: MainViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val attendanceInfo = remember { mutableStateOf(emptyList<Attendance>()) }
    val sessionCookie = mainViewModel.getSessionCookie()


    if (sessionCookie != null) {
        LaunchedEffect(Unit) {
            coroutineScope.launch(Dispatchers.IO) {
                attendanceInfo.value = mainViewModel.getAttendanceInfo(sessionCookie)
            }
        }

        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)) {
            Button(onClick = mainViewModel::onBackFromAttendance) {
                Text("Kembali")
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (attendanceInfo.value.isNotEmpty()) {
                attendanceInfo.value.forEach { attendance ->
                    if (attendance.information == "Tidak ada Presensi Kelas Matakuliah saat ini.") {
                        Text(attendance.information)
                    } else {
                        DisplayAttendanceInfo(attendance)
                        if (attendance.attendanceStatus == "Not Marked") {
                            Button(onClick = {
                                coroutineScope.launch(Dispatchers.IO) {
                                    if (mainViewModel.markAttendance(sessionCookie, attendance.klsdtId, attendance.presklsId)) {
                                        attendanceInfo.value = mainViewModel.getAttendanceInfo(sessionCookie)
                                    }
                                }
                            }) {
                                Text("Mark Attendance")
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    } else {
        // Tampilkan pesan bahwa sessionCookie adalah null atau minta pengguna untuk masuk
    }
}



@Composable
private fun DisplayAttendanceInfo(attendance: Attendance) {
    Text("Matakuliah / Kelas: ${attendance.courseClass}")
    Text("Semester: ${attendance.semester}")
    Text("Pertemuan ke-: ${attendance.meetingNumber}")
    Text("Tgl. Pertemuan: ${attendance.meetingDate}")
    Text("Materi: ${attendance.material}")
    Text("Mulai Presensi: ${attendance.attendanceStart}")
    Text("Presensi: ${attendance.attendanceStatus}")
    Text("Informasi: ${attendance.information}")
}
