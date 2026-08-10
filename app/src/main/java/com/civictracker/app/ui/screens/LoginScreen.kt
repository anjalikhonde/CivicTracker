package com.civictracker.app.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.civictracker.app.ui.theme.*
import com.civictracker.app.ui.viewmodel.LoginUiState
import com.civictracker.app.ui.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit
) {
    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Success) {
            onLoginSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "CivicLink",
            style = MaterialTheme.typography.headlineLarge,
            color = AccentGreen,
            fontWeight = FontWeight.Bold
        )
        Text(
            "EMPOWERING CITIZENS FOR A BETTER TOMORROW",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            modifier = Modifier.padding(top = 8.dp, bottom = 48.dp),
            textAlign = TextAlign.Center
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DividerGray, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                if (uiState !is LoginUiState.OtpSent) {
                    Text(
                        "LOGIN WITH PHONE",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(20.dp))
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("PHONE NUMBER") },
                        placeholder = { Text("+91 00000 00000") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        leadingIcon = { Icon(Icons.Default.Phone, null, tint = AccentGreen, modifier = Modifier.size(20.dp)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentGreen,
                            unfocusedBorderColor = DividerGray,
                            focusedLabelColor = AccentGreen,
                            unfocusedLabelColor = TextSecondary,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = { 
                            if (context is Activity) {
                                viewModel.sendOtp(phoneNumber, context)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentGreen,
                            disabledContainerColor = DividerGray
                        ),
                        enabled = phoneNumber.length >= 10 && uiState !is LoginUiState.Loading
                    ) {
                        if (uiState is LoginUiState.Loading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black, strokeWidth = 2.dp)
                        } else {
                            Text("SEND OTP", style = MaterialTheme.typography.labelLarge, color = Color.Black)
                        }
                    }
                } else {
                    Text(
                        "VERIFY OTP",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextPrimary
                    )
                    Text(
                        "AUTHENTICATION CODE SENT TO $phoneNumber",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Spacer(Modifier.height(24.dp))
                    OutlinedTextField(
                        value = otpCode,
                        onValueChange = { otpCode = it },
                        label = { Text("OTP CODE") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = { Icon(Icons.Default.Sms, null, tint = AccentGreen, modifier = Modifier.size(20.dp)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentGreen,
                            unfocusedBorderColor = DividerGray,
                            focusedLabelColor = AccentGreen,
                            unfocusedLabelColor = TextSecondary,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = { viewModel.verifyOtp(otpCode) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentGreen,
                            disabledContainerColor = DividerGray
                        ),
                        enabled = otpCode.length == 6 && uiState !is LoginUiState.Loading
                    ) {
                        if (uiState is LoginUiState.Loading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black, strokeWidth = 2.dp)
                        } else {
                            Text("VERIFY & LOGIN", style = MaterialTheme.typography.labelLarge, color = Color.Black)
                        }
                    }
                    TextButton(
                        onClick = { viewModel.resetState() },
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)
                    ) {
                        Text("CHANGE PHONE NUMBER", style = MaterialTheme.typography.labelSmall, color = AccentGreen)
                    }
                }
            }
        }

        if (uiState is LoginUiState.Error) {
            Spacer(Modifier.height(16.dp))
            Surface(
                color = UrgencyRed.copy(alpha = 0.1f),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.border(1.dp, UrgencyRed.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            ) {
                Text(
                    (uiState as LoginUiState.Error).message.uppercase(),
                    color = UrgencyRed,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}
