package com.uad.portal


import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
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
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPref: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "Portal UAD",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

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

    fun loadCredentials(): Credentials? {
        val credentialsJson = sharedPref.getString("credentials", null)
        return if (credentialsJson != null) {
            val type = object : TypeToken<Credentials>() {}.type
            gson.fromJson(credentialsJson, type)
        } else {
            null
        }
    }

    fun clearSession() {
        sharedPref.edit {
            clear()
            apply()
        }
    }
}
