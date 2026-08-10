package com.civictracker.app.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.civictracker.app.data.model.Issue
import com.civictracker.app.data.repository.SupabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class IssueDetailUiState {
    object Loading : IssueDetailUiState()
    data class Success(val issue: Issue) : IssueDetailUiState()
    data class Error(val message: String) : IssueDetailUiState()
}

@HiltViewModel
class IssueDetailViewModel @Inject constructor(
    private val supabaseRepository: SupabaseRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val issueId: String? = savedStateHandle["issueId"]
    
    private val _uiState = MutableStateFlow<IssueDetailUiState>(IssueDetailUiState.Loading)
    val uiState: StateFlow<IssueDetailUiState> = _uiState.asStateFlow()

    init {
        fetchIssue()
    }

    fun fetchIssue() {
        val id = issueId ?: return
        viewModelScope.launch {
            _uiState.value = IssueDetailUiState.Loading
            val issue = supabaseRepository.getIssueById(id)
            if (issue != null) {
                _uiState.value = IssueDetailUiState.Success(issue)
            } else {
                _uiState.value = IssueDetailUiState.Error("Issue not found")
            }
        }
    }

    fun upvote() {
        val currentState = _uiState.value
        if (currentState is IssueDetailUiState.Success) {
            val issue = currentState.issue
            val newUpvotes = (issue.upvotes ?: 0) + 1
            
            // Optimistic update
            _uiState.value = IssueDetailUiState.Success(issue.copy(upvotes = newUpvotes))
            
            viewModelScope.launch {
                try {
                    supabaseRepository.upvoteIssue(issue.id, issue.upvotes ?: 0)
                    // Refresh from server to sync
                    val updatedIssue = supabaseRepository.getIssueById(issue.id)
                    if (updatedIssue != null) {
                        _uiState.value = IssueDetailUiState.Success(updatedIssue)
                    }
                } catch (e: Exception) {
                    // Rollback on failure
                    _uiState.value = IssueDetailUiState.Success(issue)
                }
            }
        }
    }

    fun deleteIssue() {
        val id = issueId ?: return
        viewModelScope.launch {
            try {
                supabaseRepository.deleteIssue(id)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
