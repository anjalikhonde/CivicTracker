package com.civictracker.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.civictracker.app.data.auth.SessionManager
import com.civictracker.app.data.model.Issue
import com.civictracker.app.data.repository.SupabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OfficerDashboardViewModel @Inject constructor(
    private val supabaseRepository: SupabaseRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _issues = MutableStateFlow<List<Issue>>(emptyList())
    val issues: StateFlow<List<Issue>> = _issues.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val userRole = sessionManager.userRole

    init {
        // Automatically fetch issues when role is confirmed as officer
        viewModelScope.launch {
            userRole.collect { role ->
                if (role == "officer") {
                    fetchIssues()
                }
            }
        }
    }

    fun fetchIssues() {
        if (userRole.value != "officer") return

        viewModelScope.launch {
            _isLoading.value = true
            val fetchedIssues = supabaseRepository.getAllIssues()
            // Sort by urgency before setting to state
            _issues.value = fetchedIssues.sortedByDescending { calculateUrgency(it) }
            _isLoading.value = false
        }
    }

    fun updateStatus(issueId: String, newStatus: String) {
        if (userRole.value != "officer") return

        viewModelScope.launch {
            supabaseRepository.updateIssueStatus(issueId, newStatus)
            // Update local state immediately for snappy UI
            val currentList = _issues.value.map {
                if (it.id == issueId) it.copy(status = newStatus) else it
            }
            _issues.value = currentList.sortedByDescending { calculateUrgency(it) }
        }
    }

    fun calculateUrgency(issue: Issue): Long {
        if (issue.status == "Resolved") return -1 // Resolved issues go to bottom

        val upvoteWeight = (issue.upvotes ?: 0) * 10L
        
        val hoursElapsed = if (issue.timestamp != null) {
            (System.currentTimeMillis() - issue.timestamp) / 3_600_000
        } else 0
        
        val categoryWeight = when (issue.category?.lowercase()) {
            "water", "drainage", "road" -> 50L
            "waste", "lighting" -> 20L
            else -> 0L
        }

        return upvoteWeight + hoursElapsed + categoryWeight
    }
}
