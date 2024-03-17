package com.uad.portal

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
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

    val isLoggedInState = mutableStateOf(false)
    val userInfoState = mutableStateOf<UserInfo?>(null)
    val currentScreen = mutableStateOf(Screen.Home)


    val isNetworkAvailable: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)
    var networkChanged = false
    private var wasNetworkAvailable = false
    private var appJustStarted = true

    fun wasNetworkAvailable(): Boolean {
        return wasNetworkAvailable
    }
    private val networkReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            checkNetworkAvailability(context)
        }
    }

    fun registerNetworkCallback(context: Context) {
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        context.registerReceiver(networkReceiver, filter)
    }

    fun unregisterNetworkCallback(context: Context) {
        context.unregisterReceiver(networkReceiver)
    }




    fun checkNetworkAvailability(context: Context) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val isAvailable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            if (network != null) {
                val activeNetwork: NetworkCapabilities? = connectivityManager.getNetworkCapabilities(network)
                activeNetwork?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
            } else {
                false
            }
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo?.isConnected ?: false
        }

        if (!isAvailable && isNetworkAvailable.value == true && networkChanged) {
            isNetworkAvailable.postValue(false)
        } else if (isAvailable && isNetworkAvailable.value == false && networkChanged) {
            isNetworkAvailable.postValue(true)
        }

        if (appJustStarted) {
            appJustStarted = false
        } else {
            networkChanged = true
        }

        isNetworkAvailable.value = isAvailable
    }

    fun initSessionManager(context: Context) {
        sessionManager = SessionManager(context)
        reglab = Reglab(sessionManager)
        portal = portal(sessionManager)
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
        if (isNetworkAvailable.value == true) {
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
}

