package com.uad.portal

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class UserInfo(
    var username: String? = null,
    var avatarUrl: String? = null,
    var ipk: String? = null,
    var sks: String? = null
)

data class Session(
    var session: String? = null,
    var userInfo: UserInfo? = null,
    var credentials: Credentials? = null
)

class SessionManager(context: Context) {
    private val sharedPref = context.getSharedPreferences("Portal UAD", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveSession(session: Session) {
        val sessionJson = gson.toJson(session)
        sharedPref.edit {
            putString("session", sessionJson)
        }
    }

    fun loadSession(): Session? {
        val sessionJson = sharedPref.getString("session", null)
        return if (sessionJson != null) {
            val type = object : TypeToken<Session>() {}.type
            gson.fromJson(sessionJson, type)
        } else {
            null
        }
    }

    fun saveCredentials(credentials: Credentials) {
        val credentialsJson = gson.toJson(credentials)
        sharedPref.edit {
            putString("credentials", credentialsJson)
        }
    }

    fun clearSession() {
        sharedPref.edit {
            remove("session")
            remove("userInfo")
            remove("credentials")
        }
    }
}
