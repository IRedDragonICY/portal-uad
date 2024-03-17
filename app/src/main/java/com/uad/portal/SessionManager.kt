package com.uad.portal

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.uad.portal.API.Credentials

class SessionManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    val sharedPref: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "Portal UAD",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    val gson = Gson()

    inline fun <reified T> saveSession(key: String, session: T) {
        val sessionJson = gson.toJson(session)
        sharedPref.edit { putString(key, sessionJson) }
    }

    inline fun <reified T> loadSession(key: String): T? {
        val sessionJson = sharedPref.getString(key, null)
        return if (sessionJson != null) {
            val type = object : TypeToken<T>() {}.type
            gson.fromJson(sessionJson, type)
        } else {
            null
        }
    }

    fun savePortalSession(session: Session) = saveSession("portal_session", session)
    fun loadPortalSession(): Session? = loadSession("portal_session")

    fun saveReglabSession(session: ReglabSession) = saveSession("reglab_session", session)
    fun loadReglabSession(): ReglabSession? = loadSession("reglab_session")

    fun saveCredentials(credentials: Credentials) = saveSession("credentials", credentials)
    fun loadCredentials(): Credentials? = loadSession("credentials")

    fun clearSession() {
        sharedPref.edit { clear() }
    }
}