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
    val isAttendanceInfoLoaded = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!isAttendanceInfoLoaded.value) {
            coroutineScope.launch(Dispatchers.IO) {
                attendanceInfo.value = mainViewModel.getAttendanceInfo()
                isAttendanceInfoLoaded.value = true
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)) {
        Button(onClick = { mainViewModel.goBack() }) {
            Text("Back")
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
                                if (mainViewModel.markAttendance(attendance.klsdtId, attendance.presklsId)) {
                                    attendanceInfo.value = mainViewModel.getAttendanceInfo()
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
}

@Composable
private fun DisplayAttendanceInfo(attendance: Attendance) {
    if (attendance.information == "Tidak ada Presensi Kelas Matakuliah saat ini.") {
        Text("Informasi: ${attendance.information}")
    } else {
        Text("Matakuliah / Kelas: ${attendance.courseClass}")
        Text("Semester: ${attendance.semester}")
        Text("Pertemuan ke-: ${attendance.meetingNumber}")
        Text("Tgl. Pertemuan: ${attendance.meetingDate}")
        Text("Materi: ${attendance.material}")
        Text("Mulai Presensi: ${attendance.attendanceStart}")
        Text("Presensi: ${attendance.attendanceStatus}")
        Text("Informasi: ${attendance.information}")
    }
}
