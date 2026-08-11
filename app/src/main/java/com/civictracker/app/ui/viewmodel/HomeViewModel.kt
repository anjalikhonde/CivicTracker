package com.civictracker.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.civictracker.app.data.auth.SessionManager
import com.civictracker.app.data.model.Issue
import com.civictracker.app.data.repository.SupabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val supabaseRepository: SupabaseRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _issues = MutableStateFlow<List<Issue>>(emptyList())
    val issues: StateFlow<List<Issue>> = _issues

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    val userRole: StateFlow<String> = sessionManager.userRole

    init {
        refreshIssues()
    }

    fun refreshIssues() {
        viewModelScope.launch {
            _isLoading.value = true
            val fetchedIssues = supabaseRepository.getAllIssues()
            _issues.value = fetchedIssues
            _isLoading.value = false
        }
    }
}
