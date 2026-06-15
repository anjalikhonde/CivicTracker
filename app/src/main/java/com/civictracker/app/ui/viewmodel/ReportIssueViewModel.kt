package com.civictracker.app.ui.viewmodel

import android.graphics.Bitmap
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.civictracker.app.data.repository.IssueRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ReportIssueViewModel @Inject constructor(
    private val repository: IssueRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReportUiState>(ReportUiState.Idle)
    val uiState: StateFlow<ReportUiState> = _uiState

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation

    fun setLocation(location: Location) {
        _currentLocation.value = location
    }

    fun submitIssue(title: String, description: String, category: String, bitmap: Bitmap?) {
        val location = _currentLocation.value ?: return
        
        viewModelScope.launch {
            _uiState.value = ReportUiState.Loading

            val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
            val descPart = description.toRequestBody("text/plain".toMediaTypeOrNull())
            val categoryPart = category.toRequestBody("text/plain".toMediaTypeOrNull())
            val latPart = location.latitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val lngPart = location.longitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())

            val imagePart = bitmap?.let {
                val stream = ByteArrayOutputStream()
                it.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                val byteArray = stream.toByteArray()
                MultipartBody.Part.createFormData(
                    "image",
                    "report_${UUID.randomUUID()}.jpg",
                    byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull())
                )
            }

            if (imagePart == null) {
                _uiState.value = ReportUiState.Error("Image is required")
                return@launch
            }

            val result = repository.reportIssue(
                titlePart, descPart, categoryPart, latPart, lngPart, imagePart
            )

            if (result.isSuccess) {
                _uiState.value = ReportUiState.Success
            } else {
                _uiState.value = ReportUiState.Error(result.exceptionOrNull()?.message ?: "Upload failed")
            }
        }
    }
}

sealed class ReportUiState {
    object Idle : ReportUiState()
    object Loading : ReportUiState()
    object Success : ReportUiState()
    data class Error(val message: String) : ReportUiState()
}
