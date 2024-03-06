package com.uad.portal

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class Auth {
    companion object {
        private const val LOGIN_URL = "https://portal.uad.ac.id/login"
        private const val LOGOUT_URL = "https://portal.uad.ac.id/logout"
        private const val DASHBOARD_URL = "https://portal.uad.ac.id/dashboard"
    }

    suspend fun loginPortal(username: String, password: String): Connection.Response = withContext(Dispatchers.IO) {
        return@withContext try {
            Jsoup.connect(LOGIN_URL)
                .method(Connection.Method.POST)
                .data("login", username, "password", password, "remember", "1")
                .execute()
        } catch (e: Exception) {
            // handle exception
            throw e
        }
    }

    suspend fun logoutPortal(): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            Jsoup.connect(LOGOUT_URL)
                .method(Connection.Method.GET)
                .execute()
                .statusCode() == 200
        } catch (e: Exception) {
            // handle exception
            throw e
        }
    }

    suspend fun checkLogin(response: Connection.Response): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val doc: Document = response.parse()
        val loginForm = doc.select("div.form-login")
        if (!loginForm.isEmpty()) {
            val errorElement = doc.select("div.form-group.has-error div.help-block")
            val errorMessage = if (!errorElement.isEmpty()) errorElement.text() else "Unknown error"
            return@withContext Pair(false, errorMessage)
        }
        return@withContext Pair(true, "")
    }

    suspend fun getUserInfo(sessionCookie: String): UserInfo = withContext(Dispatchers.IO) {
        val response = try {
            Jsoup.connect(DASHBOARD_URL)
                .cookie("portal_session", sessionCookie)
                .method(Connection.Method.GET)
                .execute()
        } catch (e: Exception) {
            // handle exception
            throw e
        }

        val doc: Document = response.parse()
        val userElement = doc.select("a.dropdown-toggle")
        var username = ""
        var avatarUrl = ""
        if (!userElement.isEmpty()) {
            username = userElement.select("span.username.username-hide-mobile").first()?.text() ?: ""
            avatarUrl = userElement.select("img.img-circle").first()?.attr("src") ?: ""
        }
        val ipk = doc.select("a.dashboard-stat:contains(IP Kumulatif) div.details div.number").first()?.text() ?: ""
        val sks = doc.select("a.dashboard-stat:contains(SKS) div.details div.number").first()?.text() ?: ""

        return@withContext UserInfo(username, avatarUrl, ipk, sks)
    }
}
