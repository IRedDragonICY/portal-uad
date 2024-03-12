package com.uad.portal

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class MainViewModel : ViewModel() {
    private lateinit var sessionManager: SessionManager
    private val auth = Auth()

    val isLoggedInState = mutableStateOf(false)
    val userInfoState = mutableStateOf<UserInfo?>(null)
    val isAttendanceScreen = mutableStateOf(false)
    val isSettingsScreen = mutableStateOf(false)

    fun getSessionCookie(): String? {
        val session = sessionManager.loadSession()
        return session?.session
    }


    fun initSessionManager(context: Context) {
        sessionManager = SessionManager(context)
        val session = sessionManager.loadSession()
        if (session != null) {
            isLoggedInState.value = true
            userInfoState.value = session.userInfo
        }
    }


    fun onAttendanceClick() {
        isAttendanceScreen.value = true
    }

    fun onSettingsClick() {
        isSettingsScreen.value = true
    }

    fun onBackFromAttendance() {
        isAttendanceScreen.value = false
    }

    fun onBackFromSettings() {
        isSettingsScreen.value = false
    }

    fun logout() {
        viewModelScope.launch {
            val isLoggedOut = auth.logoutPortal()
            if (isLoggedOut) {
                sessionManager.clearSession()
                isLoggedInState.value = false
                userInfoState.value = null
            }
        }
    }

    fun login(credentials: Credentials) {
        viewModelScope.launch {
            val (userInfo, _) = auth.login(sessionManager, credentials)
            if (userInfo != null) {
                isLoggedInState.value = true
                userInfoState.value = userInfo
            }
        }
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
}
