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