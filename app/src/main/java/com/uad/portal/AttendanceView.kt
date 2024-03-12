package com.uad.portal

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document


data class Attendance(
    val courseClass: String,
    val semester: String,
    val meetingNumber: Int,
    val meetingDate: String,
    val material: String,
    val attendanceStart: String,
    val attendanceStatus: String,
    val information: String
)


@Composable
fun AttendanceView(
    onBack: () -> Unit,
    getAttendanceInfo: (String) -> List<Attendance>,
    sessionManager: SessionManager
) {
    val coroutineScope = rememberCoroutineScope()
    val attendanceInfo = remember { mutableStateOf(emptyList<Attendance>()) }

    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            attendanceInfo.value = getAttendanceInfo(sessionManager.loadSession()?.session!!)
        }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)) {
        Button(onClick = onBack) {
            Text("Kembali")
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (attendanceInfo.value.isNotEmpty()) {
            if (attendanceInfo.value[0].information == "Tidak ada Presensi Kelas Matakuliah saat ini.") {
                Text(attendanceInfo.value[0].information)
            } else {
                attendanceInfo.value.forEach { attendance ->
                    Text("Matakuliah / Kelas: ${attendance.courseClass}")
                    Text("Semester: ${attendance.semester}")
                    Text("Pertemuan ke-: ${attendance.meetingNumber}")
                    Text("Tgl. Pertemuan: ${attendance.meetingDate}")
                    Text("Materi: ${attendance.material}")
                    Text("Mulai Presensi: ${attendance.attendanceStart}")
                    Text("Presensi: ${attendance.attendanceStatus}")
                    Text("Informasi: ${attendance.information}")
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

fun getAttendanceInfo(sessionCookie: String): List<Attendance> {
    fun createAttendance(courseClass: String, semester: String, meetingNumber: Int, meetingDate: String, material: String, attendanceStart: String, attendanceStatus: String, information: String) =
        Attendance(courseClass, semester, meetingNumber, meetingDate, material, attendanceStart, attendanceStatus, information)

    fun connectJsoup(url: String, cookie: String, method: Connection.Method): Connection.Response =
        Jsoup.connect(url).cookie("portal_session", cookie).method(method).execute()

    return try {
        val response = connectJsoup("https://portal.uad.ac.id/presensi/Kuliah", sessionCookie, Connection.Method.GET)
        val doc: Document = response.parse()
        val infoElement = doc.select("div.note.note-info")
        if (!infoElement.isEmpty()) {
            if (infoElement.text().contains("Tidak ada Presensi Kelas Matakuliah saat ini.")) {
                return listOf(createAttendance("N/A", "N/A", 0, "N/A", "N/A", "N/A", "N/A", "Tidak ada Presensi Kelas Matakuliah saat ini."))
            }
        }
        val attendanceTables = doc.select("div.m-heading-1.border-green.m-bordered")

        attendanceTables.map { table ->
            val rows = table.select("table.table-hover.table-light tr")
            val courseClass = rows[0].select("td")[2].text()
            val semester = rows[1].select("td")[2].text()
            val meetingNumber = rows[2].select("td")[2].text().toInt()
            val meetingDate = rows[3].select("td")[2].text()
            val material = rows[4].select("td")[2].text()
            val attendanceStart = rows[5].select("td")[2].text()
            val attendanceStatus = rows[6].select("td span").text()
            val information = table.select("div.note.note-info").text().replaceFirst("Informasi", "").trim()

            createAttendance(courseClass, semester, meetingNumber, meetingDate, material, attendanceStart, attendanceStatus, information)
        }
    } catch (e: Exception) {
        listOf(createAttendance("Error: ${e.message}", "", 0, "", "", "", "", ""))
    }
}