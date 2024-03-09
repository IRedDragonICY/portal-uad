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


class SessionManager(context: Context) {
    private val sharedPref = context.getSharedPreferences("Portal UAD", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveSession(session: String) {
        sharedPref.edit {
            putString("session", session)
        }
    }

    fun saveUserInfo(userInfo: UserInfo) {
        val userInfoJson = gson.toJson(userInfo)
        sharedPref.edit {
            putString("userInfo", userInfoJson)
        }
    }

    fun saveCredentials(credentials: Credentials) {
        sharedPref.edit {
            putString("username", credentials.username)
            putString("password", credentials.password)
        }
    }

    fun loadCredentials(): Credentials? {
        val username = sharedPref.getString("username", null)
        val password = sharedPref.getString("password", null)
        return if (username != null && password != null) {
            Credentials(username, password)
        } else {
            null
        }
    }

    fun clearSession() {
        sharedPref.edit {
            remove("session")
            remove("userInfo")
            remove("username")
            remove("password")
        }
    }

    fun loadSession(): String? {
        return sharedPref.getString("session", null)
    }

    fun loadUserInfo(): UserInfo {
        val userInfoJson = sharedPref.getString("userInfo", null)
        return if (userInfoJson != null) {
            val type = object : TypeToken<UserInfo>() {}.type
            gson.fromJson(userInfoJson, type)
        } else {
            UserInfo()
        }
    }
}
