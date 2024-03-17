package com.uad.portal

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.uad.portal.API.Credentials

class SessionManager(context: Context) {

    companion object {
        private const val PORTAL_SESSION_KEY = "portal_session"
        private const val REGLAB_SESSION_KEY = "reglab_session"
        private const val CREDENTIALS_KEY = "credentials"
        private const val ENCRYPTED_PREF_NAME = "Portal UAD"
    }

    private val masterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val sharedPref: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            ENCRYPTED_PREF_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private val gson by lazy { Gson() }

    private fun <T> saveSession(key: String, session: T, type: Class<T>) {
        val sessionJson = gson.toJson(session, type)
        sharedPref.edit { putString(key, sessionJson) }
    }

    private fun <T> loadSession(key: String, type: Class<T>): T? {
        val sessionJson = sharedPref.getString(key, null)
        return sessionJson?.let {
            gson.fromJson(it, type)
        }
    }

    private inline fun <reified T> save(key: String, value: T) = saveSession(key, value, T::class.java)
    private inline fun <reified T> load(key: String): T? = loadSession(key, T::class.java)

    fun savePortalSession(session: Session) = save(PORTAL_SESSION_KEY, session)
    fun loadPortalSession(): Session? = load(PORTAL_SESSION_KEY)

    fun saveReglabSession(session: ReglabSession) = save(REGLAB_SESSION_KEY, session)
    fun loadReglabSession(): ReglabSession? = load(REGLAB_SESSION_KEY)

    fun saveCredentials(credentials: Credentials) = save(CREDENTIALS_KEY, credentials)
    fun loadCredentials(): Credentials? = load(CREDENTIALS_KEY)

    fun clearSession() {
        sharedPref.edit { clear() }
    }
}
