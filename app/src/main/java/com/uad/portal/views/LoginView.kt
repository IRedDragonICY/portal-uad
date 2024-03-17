package com.uad.portal.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.uad.portal.API.Credentials
import com.uad.portal.MainViewModel
import com.uad.portal.R
import kotlinx.coroutines.launch

@Composable
fun LoginView(mainViewModel: MainViewModel) {
    val (credentials, setCredentials) = remember { mutableStateOf(Credentials("", "")) }
    val (passwordVisibility, setPasswordVisibility) = remember { mutableStateOf(false) }
    val (loginErrorMessage, setLoginErrorMessage) = remember { mutableStateOf("") }

    val passwordFocusRequester = remember { FocusRequester() }

    val attemptLogin = {
        mainViewModel.viewModelScope.launch {
            val loginResult = mainViewModel.login(credentials)
            setLoginErrorMessage(loginResult.errorMessage ?: "")
        }
    }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter = painterResource(id = R.drawable.logo_uad),
                contentDescription = "Universitas Ahmad Dahlan",
                modifier = Modifier
                    .size(192.dp)
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(32.dp))
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
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Password,
                    autoCorrect = false
                ),
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
            ClickableText(
                text = AnnotatedString("Lupa password?"),
                onClick = {
                    // TODO
                },
                modifier = Modifier.align(Alignment.End)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { attemptLogin() },
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text("Login")
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (loginErrorMessage.isNotEmpty()) {
                Text(text = loginErrorMessage, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.CenterHorizontally)) // Removed padding here
                Spacer(modifier = Modifier.height(8.dp))
            }
            Spacer(modifier = Modifier.height(64.dp))
            Text(
                text = "© 2024 Universitas Ahmad Dahlan",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}