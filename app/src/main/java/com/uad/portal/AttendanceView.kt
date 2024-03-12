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
    val information: String,
    val klsdtId: String,
    val presklsId: String
)

@Composable
fun AttendanceView(
    onBack: () -> Unit,
    getAttendanceInfo: (String) -> List<Attendance>,
    markAttendance: (String, String, String) -> Boolean,
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
                    // Add condition to check if attendance is marked or not
                    if (attendance.attendanceStatus == "Not Marked") {
                        Button(onClick = {
                            coroutineScope.launch(Dispatchers.IO) {
                                val success = markAttendance(sessionManager.loadSession()?.session!!, attendance.klsdtId, attendance.presklsId)
                                if (success) {
                                    // Refresh attendance info
                                    attendanceInfo.value = getAttendanceInfo(sessionManager.loadSession()?.session!!)
                                } else {
                                    // Handle failure
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

fun getAttendanceInfo(sessionCookie: String): List<Attendance> {
    fun createAttendance(courseClass: String, semester: String, meetingNumber: Int, meetingDate: String, material: String, attendanceStart: String, attendanceStatus: String, information: String, klsdtId: String, presklsId: String) =
        Attendance(courseClass, semester, meetingNumber, meetingDate, material, attendanceStart, attendanceStatus, information, klsdtId, presklsId)

    fun connectJsoup(url: String, cookie: String, method: Connection.Method): Connection.Response =
        Jsoup.connect(url).cookie("portal_session", cookie).method(method).execute()

    return try {
        val response = connectJsoup("https://portal.uad.ac.id/presensi/Kuliah", sessionCookie, Connection.Method.GET)
        val doc: Document = response.parse()
        val infoElement = doc.select("div.note.note-info")
        if (!infoElement.isEmpty()) {
            if (infoElement.text().contains("Tidak ada Presensi Kelas Matakuliah saat ini.")) {
                return listOf(createAttendance("N/A", "N/A", 0, "N/A", "N/A", "N/A", "N/A", "Tidak ada Presensi Kelas Matakuliah saat ini.", "", ""))
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
            val information = table.select("div.note.note-info").text().replaceFirst("Informasi", "").trim()

            val klsdtId = table.select("input[name=klsdt_id]").`val`()
            val presklsId = table.select("input[name=preskls_id]").`val`()

            // Defaulting attendanceStatus to "Not Marked"
            val attendanceStatus = if (rows.size > 6) rows[6].select("td")[2].text() else "Not Marked"

            createAttendance(courseClass, semester, meetingNumber, meetingDate, material, attendanceStart, attendanceStatus, information, klsdtId, presklsId)
        }

    } catch (e: Exception) {
        listOf(createAttendance("Error: ${e.message}", "", 0, "", "", "", "", "", "", ""))
    }
}

fun markAttendance(sessionCookie: String, klsdtId: String, presklsId: String): Boolean {
    fun connectJsoup(url: String, cookie: String, data: Map<String, String>, method: Connection.Method): Connection.Response =
        Jsoup.connect(url).cookie("portal_session", cookie).data(data).method(method).execute()

    return try {
        val response = connectJsoup(
            "https://portal.uad.ac.id/presensi/Kuliah/index",
            sessionCookie,
            mapOf("klsdt_id" to klsdtId, "preskls_id" to presklsId, "action" to "btnpresensi"),
            Connection.Method.POST
        )
        response.statusCode() == 200
    } catch (e: Exception) {
        false
    }
}
