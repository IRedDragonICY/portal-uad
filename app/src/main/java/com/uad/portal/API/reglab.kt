package com.uad.portal.API

import com.google.gson.Gson
import com.uad.portal.PracticumInfo
import com.uad.portal.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class Reglab(private val sessionManager: SessionManager) {

    suspend fun fetchScheduleData(): PracticumInfo = withContext(Dispatchers.IO) {
        val session = sessionManager.loadReglabSession()
        val username = session?.credentials?.username

        if (session != null && username != null) {
            val url = "https://reglab.tif.uad.ac.id/ajax/pemilihan-jadwal-praktikum/$username"
            val response = Jsoup.connect(url)
                .cookie("remember_web_59ba36addc2b2f9401580f014c7f58ea4e30989d", session.session!!)
                .ignoreContentType(true)
                .execute()
            val json = response.body()
            val gson = Gson()
            return@withContext gson.fromJson(json, PracticumInfo::class.java)
        }
        throw Exception("Failed to load session")
    }
}
