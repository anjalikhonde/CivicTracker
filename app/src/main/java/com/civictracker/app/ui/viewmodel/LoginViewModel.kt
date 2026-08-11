package com.civictracker.app.ui.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.civictracker.app.data.auth.SessionManager
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    private var verificationId: String? = null

    fun sendOtp(phone: String, activity: Activity) {
        _uiState.value = LoginUiState.Loading

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                _uiState.value = LoginUiState.Error(e.message ?: "Verification failed")
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                this@LoginViewModel.verificationId = verificationId
                _uiState.value = LoginUiState.OtpSent
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyOtp(otp: String) {
        val vid = verificationId ?: run {
            _uiState.value = LoginUiState.Error("Invalid verification ID")
            return
        }
        _uiState.value = LoginUiState.Loading
        val credential = PhoneAuthProvider.getCredential(vid, otp)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    val userId = user?.uid ?: ""
                    viewModelScope.launch {
                        if (userId.isNotEmpty()) {
                            sessionManager.fetchAndSetRole(userId)
                        }
                        _uiState.value = LoginUiState.Success(userId)
                    }
                } else {
                    _uiState.value = LoginUiState.Error(task.exception?.message ?: "Sign in failed")
                }
            }
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    object OtpSent : LoginUiState()
    data class Success(val userId: String) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}
