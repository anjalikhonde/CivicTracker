package com.civictracker.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.civictracker.app.ui.viewmodel.LoginViewModel
import com.civictracker.app.ui.viewmodel.LoginUiState

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit
) {
    var phoneNumber by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Success) {
            onLoginSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Civic Tracker", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(32.dp))

        when (uiState) {
            is LoginUiState.OtpSent -> {
                TextField(
                    value = otp,
                    onValueChange = { otp = it },
                    label = { Text("Enter OTP") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState !is LoginUiState.Loading
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.verifyOtp(otp) },
                    enabled = otp.length == 6 && uiState !is LoginUiState.Loading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Verify & Login")
                }
                TextButton(onClick = { /* Handle Resend or back to phone input if needed */ }) {
                    Text("Change Phone Number")
                }
            }
            else -> {
                TextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number (e.g. +1... )") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState !is LoginUiState.Loading
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { 
                        viewModel.sendOtp(phoneNumber)
                    },
                    enabled = phoneNumber.length >= 10 && uiState !is LoginUiState.Loading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Send OTP")
                }
            }
        }

        if (uiState is LoginUiState.Loading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        }

        if (uiState is LoginUiState.Error) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = (uiState as LoginUiState.Error).message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
