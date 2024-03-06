package com.uad.portal

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class Auth {

    suspend fun loginPortal(username: String, password: String): Connection.Response = withContext(Dispatchers.IO) {
        val loginurl = "https://portal.uad.ac.id/login"
        val response: Connection.Response = Jsoup.connect(loginurl)
            .method(Connection.Method.POST)
            .data("login", username, "password", password, "remember", "1")
            .execute()
        return@withContext response
    }

    suspend fun logoutPortal(): Boolean = withContext(Dispatchers.IO) {
        val logoutUrl = "https://portal.uad.ac.id/logout"
        val response: Connection.Response = Jsoup.connect(logoutUrl)
            .method(Connection.Method.GET)
            .execute()
        return@withContext response.statusCode() == 200
    }

    suspend fun checkLogin(response: Connection.Response): Pair<Boolean, String> {
        val doc: Document = withContext(Dispatchers.IO) { response.parse() }
        val loginForm = doc.select("div.form-login")
        if (!loginForm.isEmpty()) {
            val errorElement = doc.select("div.form-group.has-error div.help-block")
            val errorMessage = if (!errorElement.isEmpty()) errorElement.text() else "Unknown error"
            return Pair(false, errorMessage)
        }
        return Pair(true, "")
    }

    suspend fun getUserInfo(sessionCookie: String): Triple<String, String, Pair<String, String>> {
        val dashboardUrl = "https://portal.uad.ac.id/dashboard"
        val response: Connection.Response = withContext(Dispatchers.IO) {
            Jsoup.connect(dashboardUrl)
                .cookie("portal_session", sessionCookie)
                .method(Connection.Method.GET)
                .execute()
        }
        val doc: Document = withContext(Dispatchers.IO) { response.parse() }
        val userElement = doc.select("a.dropdown-toggle")
        var username = ""
        var avatarUrl = ""
        if (!userElement.isEmpty()) {
            username = userElement.select("span.username.username-hide-mobile").first()?.text() ?: ""
            avatarUrl = userElement.select("img.img-circle").first()?.attr("src") ?: ""
        }
        val ipk = doc.select("a.dashboard-stat:contains(IP Kumulatif) div.details div.number").first()?.text() ?: ""
        val sks = doc.select("a.dashboard-stat:contains(SKS) div.details div.number").first()?.text() ?: ""


        return Triple(username, avatarUrl, Pair(ipk, sks))
    }

}
