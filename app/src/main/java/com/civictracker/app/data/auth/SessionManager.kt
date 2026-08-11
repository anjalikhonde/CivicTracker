package com.civictracker.app.data.auth

import android.util.Log
import com.civictracker.app.data.model.Profile
import com.civictracker.app.data.repository.SupabaseRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    private val repository: SupabaseRepository,
    private val auth: FirebaseAuth
) {
    private val _userRole = MutableStateFlow("loading")
    val userRole: StateFlow<String> = _userRole.asStateFlow()

    suspend fun fetchAndSetRole(userId: String) {
        try {
            val profile = repository.getProfile(userId)
            val phone = auth.currentUser?.phoneNumber
            
            Log.d("OfficerCheck", "Session check for user: $userId, phone: '$phone'")

            // 1. Check if the phone number is in the officer list
            val isOfficerNumber = if (phone != null) {
                repository.isOfficerNumber(phone)
            } else {
                false
            }
            Log.d("OfficerCheck", "isOfficerNumber in Supabase: $isOfficerNumber")

            if (profile != null) {
                // Profile exists. If they are now an officer but marked as citizen, upgrade them.
                if (isOfficerNumber && profile.role != "officer") {
                    Log.d("OfficerCheck", "Existing citizen upgraded to officer")
                    repository.updateProfileRole(userId, "officer")
                    _userRole.value = "officer"
                } else {
                    _userRole.value = profile.role
                }
            } else {
                // New user logic
                val role = if (isOfficerNumber) "officer" else "citizen"
                Log.d("OfficerCheck", "Creating new profile with role: $role")
                
                val newProfile = Profile(userId = userId, role = role)
                repository.createProfile(newProfile)
                _userRole.value = role
            }
        } catch (e: Exception) {
            Log.e("OfficerCheck", "Error in fetchAndSetRole", e)
            _userRole.value = "citizen"
        }
    }

    fun clearSession() {
        _userRole.value = "loading"
    }
}
