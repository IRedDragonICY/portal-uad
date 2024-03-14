package com.uad.portal

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Connection
import org.jsoup.Jsoup

data class ReglabCredentials(val username: String, val password: String)
data class ReglabLoginResult(val success: Boolean, val errorMessage: String?)

class ReglabAuth {
    companion object {
        private const val LOGIN_URL = "https://reglab.tif.uad.ac.id/login"
        private const val COOKIE_NAME = "remember_web_59ba36addc2b2f9401580f014c7f58ea4e30989d"
    }

    private suspend fun getLoginPage(): Connection.Response = withContext(Dispatchers.IO) {
        return@withContext Jsoup.connect(LOGIN_URL)
            .method(Connection.Method.GET)
            .execute()
    }

    private suspend fun getToken(response: Connection.Response): String = withContext(Dispatchers.IO) {
        val doc = response.parse()
        return@withContext doc.select("input[name=_token]").first()?.attr("value") ?: ""
    }

    private suspend fun executeConnection(url: String, method: Connection.Method, credentials: ReglabCredentials? = null, token: String, cookies: Map<String, String>): Connection.Response = withContext(Dispatchers.IO) {
        return@withContext Jsoup.connect(url)
            .method(method)
            .cookies(cookies)
            .apply {
                if (method == Connection.Method.POST && credentials != null) {
                    data("_token", token)
                    data("email", "${credentials.username}@webmail.uad.ac.id")
                    data("password", credentials.password)
                    data("remember", "on")
                }
            }
            .execute()
    }

    private suspend fun loginReglab(credentials: ReglabCredentials, token: String, cookies: Map<String, String>): Connection.Response = executeConnection(
        LOGIN_URL, Connection.Method.POST, credentials, token, cookies)

    private suspend fun checkLogin(response: Connection.Response): ReglabLoginResult = withContext(Dispatchers.IO) {
        val doc = response.parse()
        val loginForm = doc.select("form.py-2")
        if (loginForm.isNotEmpty()) {
            return@withContext ReglabLoginResult(false, "Login failed")
        }
        return@withContext ReglabLoginResult(true, null)
    }

    suspend fun login(credentials: ReglabCredentials): ReglabLoginResult {
        val loginPageResponse = getLoginPage()
        val token = getToken(loginPageResponse)
        val cookies = loginPageResponse.cookies()
        val response = loginReglab(credentials, token, cookies)
        println(response.cookies())
        val cookieValue = response.cookie(COOKIE_NAME)
        if (cookieValue != null) {
            return checkLogin(response)
        }
        return ReglabLoginResult(false, "Failed to get cookie")
    }
}

