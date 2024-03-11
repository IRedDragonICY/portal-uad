package com.uad.portal

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Connection
import org.jsoup.Jsoup

data class Credentials(val username: String, val password: String)

class Auth {
    companion object {
        private const val LOGIN_URL = "https://portal.uad.ac.id/login"
        private const val LOGOUT_URL = "https://portal.uad.ac.id/logout"
        private const val DASHBOARD_URL = "https://portal.uad.ac.id/dashboard"
    }

    private suspend fun executeConnection(url: String, method: Connection.Method, credentials: Credentials? = null): Connection.Response = withContext(Dispatchers.IO) {
        return@withContext Jsoup.connect(url)
            .method(method)
            .apply { if (method == Connection.Method.POST && credentials != null) data("login", credentials.username, "password", credentials.password, "remember", "1") }
            .execute()
    }

    private suspend fun loginPortal(credentials: Credentials) = executeConnection(LOGIN_URL, Connection.Method.POST, credentials)

    suspend fun logoutPortal(): Boolean = executeConnection(LOGOUT_URL, Connection.Method.GET).statusCode() == 200

    private suspend fun checkLogin(response: Connection.Response): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val doc = response.parse()
        val loginForm = doc.select("div.form-login")
        if (loginForm.isNotEmpty()) {
            val errorElement = doc.select("div.form-group.has-error div.help-block")
            val errorMessage = if (errorElement.isNotEmpty()) errorElement.text() else "Unknown error"
            return@withContext Pair(false, errorMessage)
        }
        return@withContext Pair(true, "")
    }

    private suspend fun getResponseWithCookie(url: String, cookieName: String, cookieValue: String): Connection.Response = withContext(Dispatchers.IO) {
        return@withContext Jsoup.connect(url)
            .cookie(cookieName, cookieValue)
            .method(Connection.Method.GET)
            .execute()
    }

    private suspend fun getUserInfo(sessionCookie: String): UserInfo = withContext(Dispatchers.IO) {
        val response = getResponseWithCookie(DASHBOARD_URL, "portal_session", sessionCookie)
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

        return@withContext UserInfo(username, avatarUrl, ipk, sks)
    }

    private suspend fun processLogin(sessionManager: SessionManager, credentials: Credentials): Pair<UserInfo?, String?> {
        val response = loginPortal(credentials)
        val sessionCookie = response.cookie("portal_session")
        val (isLoggedIn, errorMessage) = checkLogin(response)
        if (isLoggedIn && sessionCookie != null) {
            val userInfo = getUserInfo(sessionCookie)
            sessionManager.saveSession(Session(sessionCookie, userInfo))
            return Pair(userInfo, null)
        }
        return Pair(null, errorMessage)
    }


    suspend fun autoLogin(sessionManager: SessionManager): UserInfo? {
        val credentials = sessionManager.loadCredentials()
        if (credentials != null) {
            val (userInfo, _) = processLogin(sessionManager, credentials)
            return userInfo
        }
        return null
    }

    suspend fun login(sessionManager: SessionManager, credentials: Credentials): Pair<UserInfo?, String?> {
        val (userInfo, errorMessage) = processLogin(sessionManager, credentials)
        if (userInfo != null) {
            sessionManager.saveCredentials(credentials)
        }
        return Pair(userInfo, errorMessage)
    }
}
