package com.uad.portal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import coil.compose.rememberImagePainter
import com.uad.portal.ui.theme.PortalUADTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

data class Attendance(
    val courseClass: String,
    val semester: String,
    val meetingNumber: Int,
    val meetingDate: String,
    val material: String,
    val attendanceStart: String,
    val attendanceStatus: String,
    val information: String
)

class MainActivity : ComponentActivity() {
    private lateinit var sessionManager: SessionManager
    private lateinit var auth: Auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(this)
        auth = Auth()
        setContent {
            PortalUADTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        AppContent()
                    }
                }
            }
        }
        lifecycleScope.launch {
            auth.autoLogin(sessionManager)
        }
    }

    @Composable
    private fun AppContent() {
        val isLoggedInState = remember { mutableStateOf(sessionManager.loadSession() != null) }
        val userInfoState = remember { mutableStateOf(sessionManager.loadSession()?.userInfo) }
        val coroutineScope = rememberCoroutineScope()
        val isAttendanceScreen = remember { mutableStateOf(false) }
        val isSettingsScreen = remember { mutableStateOf(false) }

        if (isAttendanceScreen.value) {
            AttendanceView(
                onBack = { isAttendanceScreen.value = false },
                getAttendanceInfo = { sessionCookie: String -> getAttendanceInfo(sessionCookie) },
                sessionManager = sessionManager
            )
        }else if (isSettingsScreen.value) {
            SettingsView(onBack = { isSettingsScreen.value = false })
        } else if (isLoggedInState.value) {
            HomeView(
                userInfoState,
                isLoggedInState,
                coroutineScope,
                onAttendanceClick = { isAttendanceScreen.value = true },
                onSettingsClick = { isSettingsScreen.value = true }
            )
        } else {
            LoginView(userInfoState, isLoggedInState, coroutineScope)
        }
    }

    @Composable
    private fun HomeView(
        userInfoState: MutableState<UserInfo?>,
        isLoggedInState: MutableState<Boolean>,
        coroutineScope: CoroutineScope,
        onAttendanceClick: () -> Unit,
        onSettingsClick: () -> Unit

    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)) {
            Text("You are logged in as ${userInfoState.value?.username ?: "Unknown"}")
            Text("Your IPK is ${userInfoState.value?.ipk ?: "Unknown"}")
            Text("Your total SKS is ${userInfoState.value?.sks ?: "Unknown"}")
            userInfoState.value?.avatarUrl?.let {
                if (it.isNotEmpty()) {
                    Image(
                        painter = rememberImagePainter(it),
                        contentDescription = "User Avatar",
                        modifier = Modifier.size(100.dp)
                    )
                }
            }


            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onAttendanceClick) {
                Text("Absensi")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onSettingsClick) {
                Text("Settings")
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
    private fun SettingsView(
        onBack: () -> Unit
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)) {
            Button(onClick = onBack) {
                Text("Kembali")
            }
        }
    }


    @Composable
    private fun LoginView(
        userInfoState: MutableState<UserInfo?>,
        isLoggedInState: MutableState<Boolean>,
        coroutineScope: CoroutineScope
    ) {
        val credentialsState = remember { mutableStateOf(Credentials("", "")) }
        val passwordVisibilityState = remember { mutableStateOf(false) }
        val loginErrorMessageState = remember { mutableStateOf("") }

        val passwordFocusRequester = remember { FocusRequester() }

        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)) {
            OutlinedTextField(
                value = credentialsState.value.username,
                onValueChange = { credentialsState.value = credentialsState.value.copy(username = it) },
                label = { Text("Username") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { passwordFocusRequester.requestFocus() }),
                modifier = Modifier.onPreviewKeyEvent { keyEvent ->
                    if (keyEvent.key == Key.Enter) {
                        passwordFocusRequester.requestFocus()
                        true
                    } else {
                        false
                    }
                },
                leadingIcon = {
                    Icon(Icons.Filled.AccountCircle, contentDescription = "Username")
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = credentialsState.value.password,
                onValueChange = { credentialsState.value = credentialsState.value.copy(password = it) },
                label = { Text("Password") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    coroutineScope.launch {
                        val (userInfo, _) = auth.login(sessionManager, credentialsState.value)
                        if (userInfo != null) {
                            isLoggedInState.value = true
                            userInfoState.value = userInfo
                        } else {
                            loginErrorMessageState.value = "Failed to login"
                        }
                    }
                }),
                visualTransformation = if (passwordVisibilityState.value) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisibilityState.value = !passwordVisibilityState.value }) {
                        Icon(
                            imageVector = if (passwordVisibilityState.value) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (passwordVisibilityState.value) "Hide password" else "Show password"
                        )
                    }
                },
                singleLine = true,
                modifier = Modifier.focusRequester(passwordFocusRequester),
                leadingIcon = {
                    Icon(Icons.Filled.Lock, contentDescription = "Password")
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    coroutineScope.launch {
                        val (userInfo, errorMessage) = auth.login(sessionManager, credentialsState.value)
                        if (userInfo != null) {
                            isLoggedInState.value = true
                            userInfoState.value = userInfo
                        } else {
                            loginErrorMessageState.value = errorMessage ?: "Failed to login"
                        }
                    }
                }
            ) {
                Text("Login")
            }
            if (loginErrorMessageState.value.isNotEmpty()) {
                Text(loginErrorMessageState.value, color = MaterialTheme.colorScheme.error)
            }
        }
    }



}