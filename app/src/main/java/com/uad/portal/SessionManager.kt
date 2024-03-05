import android.content.Context

class SessionManager(private val context: Context) {
    private val sharedPref = context.getSharedPreferences("MyApp", Context.MODE_PRIVATE)

    fun saveSession(session: String) {
        with (sharedPref.edit()) {
            putString("session", session)
            apply()
        }
    }

    fun saveUserInfo(username: String, avatarUrl: String) {
        with (sharedPref.edit()) {
            putString("username", username)
            putString("avatarUrl", avatarUrl)
            apply()
        }
    }

    fun clearSession() {
        with (sharedPref.edit()) {
            remove("session")
            remove("username")
            remove("avatarUrl")
            apply()
        }
    }

    fun loadSession(): String? {
        return sharedPref.getString("session", null)
    }

    fun loadUserInfo(): Pair<String, String>? {
        val username = sharedPref.getString("username", null)
        val avatarUrl = sharedPref.getString("avatarUrl", null)
        return if (username != null && avatarUrl != null) {
            Pair(username, avatarUrl)
        } else {
            null
        }
    }
}
