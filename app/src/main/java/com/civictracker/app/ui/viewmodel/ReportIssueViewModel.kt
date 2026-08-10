package com.civictracker.app.ui.viewmodel

import android.graphics.Bitmap
import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.civictracker.app.data.model.Issue
import com.civictracker.app.data.remote.MLApiService
import com.civictracker.app.data.remote.ai.ClaudeAnalysisResult
import com.civictracker.app.data.remote.ai.GeminiAiClient
import com.civictracker.app.data.repository.SupabaseRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ReportIssueViewModel @Inject constructor(
    private val supabaseRepository: SupabaseRepository,
    private val mlApiService: MLApiService,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReportUiState>(ReportUiState.Idle)
    val uiState: StateFlow<ReportUiState> = _uiState

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation

    private val _aiAnalysis = MutableStateFlow<ClaudeAnalysisResult?>(null)
    val aiAnalysis: StateFlow<ClaudeAnalysisResult?> = _aiAnalysis.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _predictedCategory = MutableStateFlow<String?>(null)
    val predictedCategory: StateFlow<String?> = _predictedCategory.asStateFlow()

    private val _isClassifying = MutableStateFlow(false)
    val isClassifying: StateFlow<Boolean> = _isClassifying.asStateFlow()

    private val _similarIssues = MutableStateFlow<List<Issue>>(emptyList())
    val similarIssues: StateFlow<List<Issue>> = _similarIssues.asStateFlow()

    fun setLocation(location: Location) {
        _currentLocation.value = location
        fetchNearbyIssues(location)
    }

    private fun fetchNearbyIssues(location: Location) {
        viewModelScope.launch {
            try {
                val allIssues = supabaseRepository.getAllIssues()
                _similarIssues.value = allIssues.filter { 
                    it.latitude != null && it.longitude != null &&
                    Math.abs(it.latitude!! - location.latitude) < 0.05 &&
                    Math.abs(it.longitude!! - location.longitude) < 0.05
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun classifyComplaint(title: String, description: String) {
        if (title.isBlank() && description.isBlank()) return
        
        viewModelScope.launch {
            _isClassifying.value = true
            Log.i("MLClassify", "ViewModel: Sending to Gemini classification (Title: $title)")
            try {
                val combinedText = "Title: $title. Description: $description"
                val category = GeminiAiClient.classifyCategory(combinedText)
                Log.i("MLClassify", "ViewModel: Gemini predicted category: '$category'")
                _predictedCategory.value = category
            } catch (e: Exception) {
                Log.e("MLClassify", "ViewModel: Classification exception: ${e.message}")
            } finally {
                _isClassifying.value = false
            }
        }
    }

    fun analyzeIssue(description: String, bitmap: Bitmap?) {
        Log.i("SmartCheck", "ViewModel: analyzeIssue started with GeminiAiClient")
        viewModelScope.launch {
            _isAnalyzing.value = true
            try {
                val result = GeminiAiClient.analyzeIssue("Issue Analysis", description, bitmap)
                Log.i("SmartCheck", "ViewModel: Received result from Gemini: $result")
                _aiAnalysis.value = result
            } catch (e: Exception) {
                Log.e("SmartCheck", "ViewModel: analyzeIssue failed: ${e.message}")
            } finally {
                _isAnalyzing.value = false
                Log.i("SmartCheck", "ViewModel: analyzeIssue complete")
            }
        }
    }

    fun submitIssue(title: String, description: String, userCategory: String, priority: String, bitmap: Bitmap?) {
        val user = auth.currentUser
        if (user == null) {
            _uiState.value = ReportUiState.Error("User not logged in")
            return
        }

        val location = _currentLocation.value
        if (location == null) {
            _uiState.value = ReportUiState.Error("Location not found")
            return
        }

        viewModelScope.launch {
            _uiState.value = ReportUiState.Loading

            try {
                var imageUrl: String? = null
                if (bitmap != null) {
                    val stream = ByteArrayOutputStream()
                    // Fix: Convert HARDWARE bitmap to software-backed before compression
                    val softwareBitmap = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O && bitmap.config == Bitmap.Config.HARDWARE) {
                        bitmap.copy(Bitmap.Config.ARGB_8888, false)
                    } else {
                        bitmap
                    }
                    softwareBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                    imageUrl = supabaseRepository.uploadImage(stream.toByteArray())
                }

                val issue = Issue(
                    id = UUID.randomUUID().toString(),
                    userId = user.uid,
                    title = title,
                    description = description,
                    category = userCategory,
                    priority = priority,
                    sentiment = _aiAnalysis.value?.sentiment ?: "Neutral",
                    latitude = location.latitude,
                    longitude = location.longitude,
                    imageUrl = imageUrl,
                    status = "Pending",
                    upvotes = 0,
                    timestamp = System.currentTimeMillis()
                )

                supabaseRepository.createIssue(issue)
                _uiState.value = ReportUiState.Success
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = ReportUiState.Error(e.message ?: "Failed to report issue")
            }
        }
    }
    
    fun clearAiAnalysis() {
        _aiAnalysis.value = null
        _predictedCategory.value = null
    }
}

sealed class ReportUiState {
    object Idle : ReportUiState()
    object Loading : ReportUiState()
    object Success : ReportUiState()
    data class Error(val message: String) : ReportUiState()
}
