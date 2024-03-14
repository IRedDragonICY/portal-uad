package com.uad.portal

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.uad.reglab.ReglabAuth
import com.uad.reglab.ReglabCredentials
import kotlinx.coroutines.launch
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.concurrent.TimeUnit

enum class Screen {
    Home,
    Attendance,
    Settings
}

class MainViewModel : ViewModel() {
    private lateinit var sessionManager: SessionManager
    private val auth = Auth()
    private val reglabAuth = ReglabAuth()

    val isLoggedInState = mutableStateOf(false)
    val userInfoState = mutableStateOf<UserInfo?>(null)
    val currentScreen = mutableStateOf(Screen.Home)

    fun initSessionManager(context: Context) {
        sessionManager = SessionManager(context)
        val session = sessionManager.loadSession()
        session?.let {
            isLoggedInState.value = true
            userInfoState.value = it.userInfo
            autoLogin()
        }
    }
    fun initAttendanceWorker(context: Context) {
        val attendanceWorkRequest = PeriodicWorkRequestBuilder<AttendanceWorker>(3, TimeUnit.MINUTES).build()
        WorkManager.getInstance(context).enqueue(attendanceWorkRequest)
    }

    fun autoLogin() = viewModelScope.launch {
        val credentials = sessionManager.loadCredentials()
        credentials?.let {
            login(it)
        }
    }

    fun navigate(screen: Screen) {
        currentScreen.value = screen
    }

    fun logout() = viewModelScope.launch {
        auth.logoutPortal().let { isLoggedOut ->
            if (isLoggedOut) {
                sessionManager.clearSession()
                isLoggedInState.value = false
                userInfoState.value = null
            }
        }
    }

    suspend fun login(credentials: Credentials): LoginResult {
        val loginResult = auth.login(sessionManager, credentials)
        loginResult.userInfo?.let {
            isLoggedInState.value = true
            userInfoState.value = it

            val reglabCredentials = ReglabCredentials(credentials.username, credentials.password)
            val reglabLoginResult = reglabAuth.login(reglabCredentials)

            if (!reglabLoginResult.success) {
                println("Reglab login failed: ${reglabLoginResult.errorMessage}")
            }
        }
        return loginResult
    }


    fun getAttendanceInfo(): List<Attendance> {
        val sessionCookie = sessionManager.loadSession()?.session ?: return emptyList()
        val url = "https://portal.uad.ac.id/presensi/Kuliah"
        val response = connectJsoup(url, sessionCookie, method = Connection.Method.GET)
        return parseAttendanceInfo(response)
    }

    fun markAttendance(klsdtId: String, presklsId: String): Boolean {
        val sessionCookie = sessionManager.loadSession()?.session ?: return false
        val url = "https://portal.uad.ac.id/presensi/Kuliah/index"
        val data = mapOf("klsdt_id" to klsdtId, "preskls_id" to presklsId, "action" to "btnpresensi")
        val response = connectJsoup(url, sessionCookie, data, Connection.Method.POST)
        return response.statusCode() == 200
    }

    private fun connectJsoup(
        url: String,
        cookie: String,
        data: Map<String, String> = emptyMap(),
        method: Connection.Method
    ): Connection.Response =
        Jsoup.connect(url).cookie("portal_session", cookie).data(data).method(method).execute()

    private fun parseAttendanceInfo(response: Connection.Response): List<Attendance> {
        val doc: Document = response.parse()
        val infoElement = doc.select("div.note.note-info")
        if (!infoElement.isEmpty() && infoElement.text().contains("Tidak ada Presensi Kelas Matakuliah saat ini.")) {
            return listOf(Attendance(null, null, 0, null, null, null, null, "Tidak ada Presensi Kelas Matakuliah saat ini.", null, null))
        }
        val attendanceTables = doc.select("div.m-heading-1.border-green.m-bordered")
        return attendanceTables.mapNotNull { table -> parseTableToAttendance(table).getOrNull() }
    }

    private fun parseTableToAttendance(table: Element): Result<Attendance> = runCatching {
        val rows = table.select("table.table-hover.table-light tr")
        val courseClass = rows[0].select("td").getOrNull(2)?.text()
        val semester = rows[1].select("td").getOrNull(2)?.text()
        val meetingNumber = rows[2].select("td").getOrNull(2)?.text()?.toInt()
        val meetingDate = rows[3].select("td").getOrNull(2)?.text()
        val material = rows[4].select("td").getOrNull(2)?.text()
        val attendanceStart = rows[5].select("td").getOrNull(2)?.text()
        val information = table.select("div.note.note-info").text().replaceFirst("Informasi", "").trim()

        val klsdtId = table.select("input[name=klsdt_id]").`val`()
        val presklsId = table.select("input[name=preskls_id]").`val`()

        val presensiButtonExists = table.select("button[name=action][value=btnpresensi]").isNotEmpty()

        val attendanceStatus = if (presensiButtonExists) "Not Marked" else "Marked"

        Attendance(courseClass, semester, meetingNumber, meetingDate, material, attendanceStart, attendanceStatus, information, klsdtId, presklsId)
    }
}