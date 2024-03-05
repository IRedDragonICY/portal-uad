
package com.uad.portal

import SessionManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.uad.portal.ui.theme.PortalUADTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import coil.compose.rememberImagePainter


class MainActivity : ComponentActivity() {
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(this)
        setContent {
            PortalUADTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        AppContent()
                    }
                }
            }
        }
    }

    @Composable
    fun AppContent() {
        val isLoggedInState = remember { mutableStateOf(sessionManager.loadSession() != null) }
        val userInfo = sessionManager.loadUserInfo()
        val usernameState = remember { mutableStateOf(userInfo?.first ?: "") }
        val avatarUrlState = remember { mutableStateOf(userInfo?.second ?: "") }
        val coroutineScope = rememberCoroutineScope()

        if (isLoggedInState.value) {
            LoggedInScreen(usernameState, avatarUrlState, isLoggedInState, coroutineScope)
        } else {
            LoginForm(usernameState, avatarUrlState, isLoggedInState, coroutineScope)
        }
    }


    @Composable
    fun LoggedInScreen(
        usernameState: MutableState<String>,
        avatarUrlState: MutableState<String>,
        isLoggedInState: MutableState<Boolean>,
        coroutineScope: CoroutineScope
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)) {
            Text("You are logged in as ${usernameState.value}")
            if (avatarUrlState.value.isNotEmpty()) {
                Image(
                    painter = rememberImagePainter(avatarUrlState.value),
                    contentDescription = "User Avatar",
                    modifier = Modifier.size(100.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                coroutineScope.launch {
                    val isLoggedOut = logoutPortal()
                    if (isLoggedOut) {
                        sessionManager.clearSession()
                        isLoggedInState.value = false
                        usernameState.value = ""
                        avatarUrlState.value = ""
                    }
                }
            }) {
                Text("Logout")
            }
        }
    }


    @Composable
    fun LoginForm(
        usernameState: MutableState<String>,
        avatarUrlState: MutableState<String>,
        isLoggedInState: MutableState<Boolean>,
        coroutineScope: CoroutineScope
    ) {
        val loginUsernameState = remember { mutableStateOf("") }
        val passwordState = remember { mutableStateOf("") }
        val loginErrorMessageState = remember { mutableStateOf("") }

        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)) {
            OutlinedTextField(
                value = loginUsernameState.value,
                onValueChange = { loginUsernameState.value = it },
                label = { Text("Username") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = passwordState.value,
                onValueChange = { passwordState.value = it },
                label = { Text("Password") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    coroutineScope.launch {
                        val response = loginPortal(loginUsernameState.value, passwordState.value)
                        val sessionCookie = response.cookie("portal_session")
                        sessionManager.saveSession(sessionCookie)
                        val (isLoggedIn, errorMessage) = checkLogin(response)
                        isLoggedInState.value = isLoggedIn
                        loginErrorMessageState.value = errorMessage
                        if (isLoggedIn) {
                            val (username, avatarUrl) = getUserInfo(sessionCookie)
                            usernameState.value = username
                            avatarUrlState.value = avatarUrl
                        }
                    }
                }),
                visualTransformation = PasswordVisualTransformation(),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                coroutineScope.launch {
                    val response = loginPortal(loginUsernameState.value, passwordState.value)
                    val sessionCookie = response.cookie("portal_session")
                    sessionManager.saveSession(sessionCookie)
                    val (isLoggedIn, errorMessage) = checkLogin(response)
                    isLoggedInState.value = isLoggedIn
                    loginErrorMessageState.value = errorMessage
                    if (isLoggedIn) {
                        val (username, avatarUrl) = getUserInfo(sessionCookie)
                        sessionManager.saveUserInfo(username, avatarUrl)
                        usernameState.value = username
                        avatarUrlState.value = avatarUrl
                    }
                }
            }) {
                Text("Login")
            }

            if (loginErrorMessageState.value.isNotEmpty()) {
                Text(loginErrorMessageState.value, color = MaterialTheme.colorScheme.error)
            }
        }
    }

    suspend fun loginPortal(username: String, password: String): Connection.Response = withContext(Dispatchers.IO) {
        val loginurl = "https://portal.uad.ac.id/login"
        val response: Connection.Response = Jsoup.connect(loginurl)
            .method(Connection.Method.POST)
            .data("login", username, "password", password)
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

    suspend fun getUserInfo(sessionCookie: String): Pair<String, String> {
        val dashboardUrl = "https://portal.uad.ac.id/dashboard"
        val response: Connection.Response = withContext(Dispatchers.IO) {
            Jsoup.connect(dashboardUrl)
                .cookie("portal_session", sessionCookie)
                .method(Connection.Method.GET)
                .execute()
        }
        val doc: Document = withContext(Dispatchers.IO) { response.parse() }
        val userElement = doc.select("a.dropdown-toggle")
        if (!userElement.isEmpty()) {
            val username = userElement.select("span.username.username-hide-mobile").first()?.text() ?: ""
            val avatarUrl = userElement.select("img.img-circle").first()?.attr("src") ?: ""
            return Pair(username, avatarUrl)
        }
        return Pair("", "")
    }


}
