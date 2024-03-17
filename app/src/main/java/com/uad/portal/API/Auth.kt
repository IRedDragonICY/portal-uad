package com.uad.portal.API

import com.uad.portal.ReglabSession
import com.uad.portal.Session
import com.uad.portal.SessionManager
import com.uad.portal.UserInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.io.IOException
import java.net.SocketTimeoutException

data class Credentials(val username: String, val password: String)
data class ReglabCredentials(val username: String, val password: String)

data class LoginResult(val userInfo: UserInfo?, val errorMessage: String?)
data class ReglabLoginResult(val success: Boolean, val errorMessage: String?)

class Auth {
    companion object {
        private const val PORTAL_LOGIN_URL = "https://portal.uad.ac.id/login"
        private const val PORTAL_LOGOUT_URL = "https://portal.uad.ac.id/logout"
        private const val PORTAL_DASHBOARD_URL = "https://portal.uad.ac.id/dashboard"
        private const val REGLAB_LOGIN_URL = "https://reglab.tif.uad.ac.id/login"
        private const val REGLAB_COOKIE_NAME = "remember_web_59ba36addc2b2f9401580f014c7f58ea4e30989d"
    }

    // Portal Auth Methods
    private suspend fun loginPortal(credentials: Credentials) = executeConnection(
        PORTAL_LOGIN_URL,
        Connection.Method.POST,
        portalCredentials = credentials
    )

    suspend fun logoutPortal(): Boolean = executeConnection(PORTAL_LOGOUT_URL, Connection.Method.GET).statusCode() == 200

    private fun checkLogin(response: Connection.Response): LoginResult {
        val doc = response.parse()
        val loginForm = doc.select("div.form-login")
        if (loginForm.isNotEmpty()) {
            val errorElement = doc.select("div.form-group.has-error div.help-block")
            val errorMessage = errorElement.first()?.text() ?: "Unknown error"
            return LoginResult(null, errorMessage)
        }
        return LoginResult(UserInfo(), "")
    }

    private fun getResponseWithCookie(url: String, cookieName: String, cookieValue: String): Connection.Response {
        return try {
            Jsoup.connect(url)
                .cookie(cookieName, cookieValue)
                .method(Connection.Method.GET)
                .execute()
        } catch (e: IOException) {
            when (e) {
                is SocketTimeoutException -> throw SocketTimeoutException("Connection timed out")
                else -> throw IOException("No internet connection", e)
            }
        }
    }

    private suspend fun executeConnection(
        url: String,
        method: Connection.Method,
        portalCredentials: Credentials? = null,
        reglabCredentials: ReglabCredentials? = null,
        token: String? = null,
        cookies: Map<String, String>? = null
    ): Connection.Response = withContext(Dispatchers.IO) {
        return@withContext try {
            Jsoup.connect(url)
                .method(method)
                .timeout(30000) // Set timeout to 30 seconds
                .apply {
                    when {
                        method == Connection.Method.POST && portalCredentials != null -> {
                            data(
                                "login", portalCredentials.username,
                                "password", portalCredentials.password,
                                "remember", "1"
                            )
                        }
                        method == Connection.Method.POST && reglabCredentials != null && token != null && cookies != null -> {
                            cookies(cookies)
                            data(
                                "_token", token,
                                "email", "${reglabCredentials.username}@webmail.uad.ac.id",
                                "password", reglabCredentials.password,
                                "remember", "on"
                            )
                        }
                    }
                }
                .execute()
        } catch (e: IOException) {
            when (e) {
                is SocketTimeoutException -> throw SocketTimeoutException("Connection timed out. Please try again.")
                else -> throw IOException("No internet connection", e)
            }
        }
    }


    private fun getUserInfo(sessionCookie: String): UserInfo {
        val response = getResponseWithCookie(PORTAL_DASHBOARD_URL, "portal_session", sessionCookie)
        val doc = response.parse()
        val userElement = doc.select("a.dropdown-toggle")
        var username = ""
        var avatarUrl = ""
        if (userElement.isNotEmpty()) {
            username = userElement.select("span.username.username-hide-mobile").first()?.text() ?: ""
            avatarUrl = userElement.select("img.img-circle").first()?.attr("src") ?: ""
        }
        val ipk = doc.select("a.dashboard-stat:contains(IP Kumulatif) div.details div.number").first()?.text() ?: ""
        val sks = doc.select("a.dashboard-stat:contains(SKS) div.details div.number").first()?.text() ?: ""

        return UserInfo(username, avatarUrl, ipk, sks)
    }

    private suspend fun processLogin(sessionManager: SessionManager, credentials: Credentials): LoginResult {
        val response = loginPortal(credentials)
        val sessionCookie = response.cookie("portal_session")
        val loginResult = checkLogin(response)
        if (loginResult.userInfo != null && sessionCookie != null) {
            val userInfo = getUserInfo(sessionCookie)
            sessionManager.savePortalSession(Session(sessionCookie, userInfo))
            return LoginResult(userInfo, null)
        }
        return loginResult
    }

    suspend fun login(sessionManager: SessionManager, credentials: Credentials): LoginResult {
        val loginResult = processLogin(sessionManager, credentials)
        if (loginResult.userInfo != null) {
            sessionManager.saveCredentials(credentials)
        }
        return loginResult
    }

    // Reglab Auth Methods
    private fun getLoginPage(): Connection.Response {
        return try {
            Jsoup.connect(REGLAB_LOGIN_URL)
                .method(Connection.Method.GET)
                .execute()
        } catch (e: IOException) {
            when (e) {
                is SocketTimeoutException -> throw SocketTimeoutException("Connection timed out")
                else -> throw IOException("No internet connection", e)
            }
        }
    }

    private fun getToken(response: Connection.Response): String {
        val doc = response.parse()
        return doc.select("input[name=_token]").first()?.attr("value") ?: ""
    }

    private fun loginReglab(credentials: ReglabCredentials, token: String, cookies: Map<String, String>): Connection.Response {
        return try {
            Jsoup.connect(REGLAB_LOGIN_URL)
                .method(Connection.Method.POST)
                .cookies(cookies)
                .apply {
                    data(
                        "_token", token,
                        "email", "${credentials.username}@webmail.uad.ac.id",
                        "password", credentials.password,
                        "remember", "on"
                    )
                }
                .execute()
        } catch (e: IOException) {
            when (e) {
                is SocketTimeoutException -> throw SocketTimeoutException("Connection timed out")
                else -> throw IOException("No internet connection", e)
            }
        }
    }

    private fun checkLoginReglab(response: Connection.Response): ReglabLoginResult {
        val doc = response.parse()
        val loginForm = doc.select("form.py-2")
        if (loginForm.isNotEmpty()) {
            return ReglabLoginResult(false, "Login failed")
        }
        return ReglabLoginResult(true, null)
    }

    fun loginReglab(credentials: ReglabCredentials, sessionManager: SessionManager): ReglabLoginResult {
        val reglabSession = sessionManager.loadReglabSession()
        if (reglabSession != null) {
            val response = getLoginPage()
            val cookieValue = response.cookie(REGLAB_COOKIE_NAME)
            if (cookieValue != null) {
                val loginResult = checkLoginReglab(response)
                if (loginResult.success) {
                    return loginResult
                }
            }
        }
        val loginPageResponse = getLoginPage()
        val token = getToken(loginPageResponse)
        val cookies = loginPageResponse.cookies()
        val response = loginReglab(credentials, token, cookies)
        val cookieValue = response.cookie(REGLAB_COOKIE_NAME)

        if (cookieValue != null) {
            val loginResult = checkLoginReglab(response)
            if (loginResult.success) {
                val newReglabSession = ReglabSession(session = cookieValue, credentials = credentials)
                sessionManager.saveReglabSession(newReglabSession)
            }
            return loginResult
        }
        return ReglabLoginResult(false, "Failed to get cookie")
    }
}
