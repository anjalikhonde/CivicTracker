package com.civictracker.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    fun sendOtp(phone: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            delay(1000) // Mock network delay
            _uiState.value = LoginUiState.OtpSent
        }
    }

    fun verifyOtp(otp: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            delay(1000) // Mock network delay
            _uiState.value = LoginUiState.Success("mock_user_id")
        }
    }
}

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    object OtpSent : LoginUiState()
    data class Success(val userId: String) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}
