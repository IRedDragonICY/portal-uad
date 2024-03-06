package com.uad.portal

import SessionManager
import UserInfo
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.uad.portal.ui.theme.PortalUADTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import coil.compose.rememberImagePainter

class MainActivity : ComponentActivity() {
    private lateinit var sessionManager: SessionManager
    private lateinit var auth: Auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(this)
        auth = Auth()
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
        val userInfoState = remember { mutableStateOf(sessionManager.loadUserInfo() ?: UserInfo()) }
        val coroutineScope = rememberCoroutineScope()

        if (isLoggedInState.value) {
            LoggedInScreen(userInfoState, isLoggedInState, coroutineScope)
        } else {
            LoginForm(userInfoState, isLoggedInState, coroutineScope)
        }
    }

    @Composable
    fun LoggedInScreen(
        userInfoState: MutableState<UserInfo>,
        isLoggedInState: MutableState<Boolean>,
        coroutineScope: CoroutineScope
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)) {
            Text("You are logged in as ${userInfoState.value.username}")
            Text("Your IPK is ${userInfoState.value.ipk}")
            Text("Your total SKS is ${userInfoState.value.sks}")
            if (userInfoState.value.avatarUrl?.isNotEmpty() == true) {
                Image(
                    painter = rememberImagePainter(userInfoState.value.avatarUrl),
                    contentDescription = "User Avatar",
                    modifier = Modifier.size(100.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                coroutineScope.launch {
                    val isLoggedOut = auth.logoutPortal()
                    if (isLoggedOut) {
                        sessionManager.clearSession()
                        isLoggedInState.value = false
                        userInfoState.value = UserInfo()
                    }
                }
            }) {
                Text("Logout")
            }
        }
    }

    @Composable
    fun LoginForm(
        userInfoState: MutableState<UserInfo>,
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
                        val response = auth.loginPortal(loginUsernameState.value, passwordState.value)
                        val sessionCookie = response.cookie("portal_session")
                        sessionManager.saveSession(sessionCookie)
                        val (isLoggedIn, errorMessage) = auth.checkLogin(response)
                        isLoggedInState.value = isLoggedIn
                        loginErrorMessageState.value = errorMessage
                        if (isLoggedIn) {
                            val userInfo = auth.getUserInfo(sessionCookie)
                            sessionManager.saveUserInfo(userInfo)
                            userInfoState.value = userInfo
                        }
                    }
                }),
                visualTransformation = PasswordVisualTransformation(),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                coroutineScope.launch {
                    val response = auth.loginPortal(loginUsernameState.value, passwordState.value)
                    val sessionCookie = response.cookie("portal_session")
                    sessionManager.saveSession(sessionCookie)
                    val (isLoggedIn, errorMessage) = auth.checkLogin(response)
                    isLoggedInState.value = isLoggedIn
                    loginErrorMessageState.value = errorMessage
                    if (isLoggedIn) {
                        val userInfo = auth.getUserInfo(sessionCookie)
                        sessionManager.saveUserInfo(userInfo)
                        userInfoState.value = userInfo
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
}