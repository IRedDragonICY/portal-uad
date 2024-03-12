package com.uad.portal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun LoginView(
    userInfoState: MutableState<UserInfo?>,
    isLoggedInState: MutableState<Boolean>,
    coroutineScope: CoroutineScope,
    auth: Auth,
    sessionManager: SessionManager
) {

    val (credentials, setCredentials) = remember { mutableStateOf(Credentials("", "")) }
    val (passwordVisibility, setPasswordVisibility) = remember { mutableStateOf(false) }
    val (loginErrorMessage, setLoginErrorMessage) = remember { mutableStateOf("") }

    val passwordFocusRequester = remember { FocusRequester() }

    val attemptLogin = {
        coroutineScope.launch {
            val (userInfo, errorMessage) = auth.login(sessionManager, credentials)
            if (userInfo != null) {
                isLoggedInState.value = true
                userInfoState.value = userInfo
            } else {
                setLoginErrorMessage(errorMessage ?: "Failed to login")
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)) {
        OutlinedTextField(
            value = credentials.username,
            onValueChange = { setCredentials(credentials.copy(username = it)) },
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
            value = credentials.password,
            onValueChange = { setCredentials(credentials.copy(password = it)) },
            label = { Text("Password") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { attemptLogin() }),
            visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { setPasswordVisibility(!passwordVisibility) }) {
                    Icon(
                        imageVector = if (passwordVisibility) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (passwordVisibility) "Hide password" else "Show password"
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
        Button(onClick = { attemptLogin() }) { Text("Login") }
        if (loginErrorMessage.isNotEmpty()) {
            Text(loginErrorMessage, color = MaterialTheme.colorScheme.error)
        }
    }
}
