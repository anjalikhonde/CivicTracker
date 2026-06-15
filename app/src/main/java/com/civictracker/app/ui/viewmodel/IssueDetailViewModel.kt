package com.civictracker.app.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
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
class IssueDetailViewModel @Inject constructor(
    private val repository: IssueRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val issueId: String? = savedStateHandle["issueId"]
    private val _issue = MutableStateFlow<Issue?>(null)
    val issue: StateFlow<Issue?> = _issue

    init {
        issueId?.let { id ->
            viewModelScope.launch {
                _issue.value = repository.getIssueById(id)
            }
        }
    }

    fun upvote() {
        issueId?.let { id ->
            viewModelScope.launch {
                repository.upvoteIssue(id)
                _issue.value = repository.getIssueById(id)
            }
        }
    }
}
