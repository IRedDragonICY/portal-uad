package com.uad.portal

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

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

    fun savePortalSession(session: Session) {
        val sessionJson = gson.toJson(session)
        sharedPref.edit {
            putString("portal_session", sessionJson)
        }
    }

    fun loadPortalSession(): Session? {
        val sessionJson = sharedPref.getString("portal_session", null)
        return if (sessionJson != null) {
            val type = object : TypeToken<Session>() {}.type
            gson.fromJson(sessionJson, type)
        } else {
            null
        }
    }

    fun saveReglabSession(session: ReglabSession) {
        val sessionJson = gson.toJson(session)
        sharedPref.edit {
            putString("reglab_session", sessionJson)
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

    fun loadReglabSession(): ReglabSession? {
        val sessionJson = sharedPref.getString("reglab_session", null)
        return if (sessionJson != null) {
            val type = object : TypeToken<ReglabSession>() {}.type
            gson.fromJson(sessionJson, type)
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