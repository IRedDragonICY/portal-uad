package com.uad.portal

import NetworkHandler
import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.uad.portal.API.*
import com.uad.portal.views.Screen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class MainViewModel : ViewModel() {
    private lateinit var sessionManager: SessionManager
    private val auth = Auth()
    lateinit var reglab: Reglab
    lateinit var portal: portal

    lateinit var networkHandler: NetworkHandler

    val isLoggedInState = mutableStateOf(false)
    val userInfoState = mutableStateOf<UserInfo?>(null)
    val currentScreen = mutableStateOf(Screen.Home)

    fun initSessionManager(context: Context) {
        sessionManager = SessionManager(context)
        reglab = Reglab(sessionManager)
        portal = portal(sessionManager)
        networkHandler = NetworkHandler(context)
        networkHandler.registerNetworkCallback()
        val session = sessionManager.loadPortalSession()
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

    private fun autoLogin() = viewModelScope.launch {
        if (networkHandler.isNetworkAvailable.value == true) {
            val credentials = sessionManager.loadCredentials()
            credentials?.let { login(it) }
        } else {
            Log.e("MainViewModel", "Cannot auto login due to no internet connection")
        }
    }

    fun navigate(screen: Screen) {
        currentScreen.value = screen
    }

    fun logout() = viewModelScope.launch {
        if (auth.logoutPortal()) {
            sessionManager.clearSession()
            isLoggedInState.value = false
            userInfoState.value = null
        }
    }

    suspend fun login(credentials: Credentials): LoginResult = withContext(Dispatchers.IO) {
        val loginResult = auth.login(sessionManager, credentials)
        withContext(Dispatchers.Main) {
            loginResult.userInfo?.let {
                isLoggedInState.value = true
                userInfoState.value = it
            }
        }

        val reglabSession = sessionManager.loadReglabSession()
        if (reglabSession == null) {
            val reglabCredentials = ReglabCredentials(credentials.username, credentials.password)
            val reglabLoginResult = auth.loginReglab(reglabCredentials, sessionManager)
            if (reglabLoginResult.success) {
                // Do something if needed
            }
        }
        return@withContext loginResult
    }

    suspend fun markAttendanceInPortal(klsdtId: String, presklsId: String): Boolean {
        return portal.markAttendance(klsdtId, presklsId)
    }
}
