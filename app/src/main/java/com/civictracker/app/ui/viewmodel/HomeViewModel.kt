package com.civictracker.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.civictracker.app.data.model.Issue
import com.civictracker.app.data.repository.IssueRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: IssueRepository
) : ViewModel() {

    val issues: StateFlow<List<Issue>> = repository.issues
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refreshIssues()
    }

    fun refreshIssues() {
        viewModelScope.launch {
            repository.refreshIssues()
        }
    }
}
