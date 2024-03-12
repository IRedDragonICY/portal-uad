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
import org.jsoup.nodes.Element

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
    val session = sessionManager.loadSession()?.session!!

    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            attendanceInfo.value = getAttendanceInfo(session)
        }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)) {
        Button(onClick = onBack) {
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
                                if (markAttendance(session, attendance.klsdtId, attendance.presklsId)) {
                                    attendanceInfo.value = getAttendanceInfo(session)
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
    Text("Matakuliah / Kelas: ${attendance.courseClass}")
    Text("Semester: ${attendance.semester}")
    Text("Pertemuan ke-: ${attendance.meetingNumber}")
    Text("Tgl. Pertemuan: ${attendance.meetingDate}")
    Text("Materi: ${attendance.material}")
    Text("Mulai Presensi: ${attendance.attendanceStart}")
    Text("Presensi: ${attendance.attendanceStatus}")
    Text("Informasi: ${attendance.information}")
}

fun getAttendanceInfo(sessionCookie: String): List<Attendance> {
    val url = "https://portal.uad.ac.id/presensi/Kuliah"
    val response = connectJsoup(url, sessionCookie, method = Connection.Method.GET)
    return parseAttendanceInfo(response)
}

fun markAttendance(sessionCookie: String, klsdtId: String, presklsId: String): Boolean {
    val url = "https://portal.uad.ac.id/presensi/Kuliah/index"
    val data = mapOf("klsdt_id" to klsdtId, "preskls_id" to presklsId, "action" to "btnpresensi")
    val response = connectJsoup(url, sessionCookie, data, Connection.Method.POST)
    return response.statusCode() == 200
}

private fun connectJsoup(url: String, cookie: String, data: Map<String, String> = emptyMap(), method: Connection.Method): Connection.Response =
    Jsoup.connect(url).cookie("portal_session", cookie).data(data).method(method).execute()

private fun parseAttendanceInfo(response: Connection.Response): List<Attendance> {
    return try {
        val doc: Document = response.parse()
        val infoElement = doc.select("div.note.note-info")
        if (!infoElement.isEmpty() && infoElement.text().contains("Tidak ada Presensi Kelas Matakuliah saat ini.")) {
            return listOf(Attendance("N/A", "N/A", 0, "N/A", "N/A", "N/A", "N/A", "Tidak ada Presensi Kelas Matakuliah saat ini.", "", ""))
        }
        val attendanceTables = doc.select("div.m-heading-1.border-green.m-bordered")
        attendanceTables.map { table -> parseTableToAttendance(table) }
    } catch (e: Exception) {
        listOf(Attendance("Error: ${e.message}", "", 0, "", "", "", "", "", "", ""))
    }
}

private fun parseTableToAttendance(table: Element): Attendance {
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

    val presensiButtonExists = table.select("button[name=action][value=btnpresensi]").isNotEmpty()

    val attendanceStatus = if (presensiButtonExists) "Not Marked" else "Marked"

    return Attendance(courseClass, semester, meetingNumber, meetingDate, material, attendanceStart, attendanceStatus, information, klsdtId, presklsId)
}
