package com.civictracker.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.civictracker.app.data.model.Issue
import com.civictracker.app.data.repository.IssueRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyIssuesViewModel @Inject constructor(
    private val repository: IssueRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MyIssuesUiState>(MyIssuesUiState.Loading)
    val uiState: StateFlow<MyIssuesUiState> = _uiState

    fun loadUserIssues(userId: String) {
        viewModelScope.launch {
            _uiState.value = MyIssuesUiState.Loading
            try {
                val issues = repository.getUserIssues(userId)
                _uiState.value = MyIssuesUiState.Success(issues)
            } catch (e: Exception) {
                _uiState.value = MyIssuesUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class MyIssuesUiState {
    object Loading : MyIssuesUiState()
    data class Success(val issues: List<Issue>) : MyIssuesUiState()
    data class Error(val message: String) : MyIssuesUiState()
}
