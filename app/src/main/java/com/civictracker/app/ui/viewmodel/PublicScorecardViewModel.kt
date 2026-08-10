package com.civictracker.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.civictracker.app.data.model.Issue
import com.civictracker.app.data.repository.SupabaseRepository
import com.civictracker.app.ui.screens.DepartmentStat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class PublicScorecardViewModel @Inject constructor(
    private val supabaseRepository: SupabaseRepository
) : ViewModel() {

    private val _stats = MutableStateFlow<List<DepartmentStat>>(emptyList())
    val stats: StateFlow<List<DepartmentStat>> = _stats.asStateFlow()

    private val _overallResolutionRate = MutableStateFlow(0f)
    val overallResolutionRate: StateFlow<Float> = _overallResolutionRate.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        refreshStats()
    }

    fun refreshStats() {
        viewModelScope.launch {
            _isLoading.value = true
            val issues = supabaseRepository.getAllIssues()
            
            // PERFORMANCE FIX: Move heavy calculations to background thread
            withContext(Dispatchers.Default) {
                calculateStats(issues)
            }

            _isLoading.value = false
        }
    }

    private fun calculateStats(issues: List<Issue>) {
        if (issues.isEmpty()) {
            _stats.value = emptyList()
            _overallResolutionRate.value = 0f
            return
        }

        // Grouping logic: Map category to Department
        val departmentMapping = mapOf(
            "Road" to "Public Works",
            "Water" to "Water & Sewage",
            "Drainage" to "Water & Sewage",
            "Waste" to "Sanitation",
            "Lighting" to "Public Works"
        )

        val statsList = issues.groupBy { issue ->
            departmentMapping[issue.category] ?: "General Services"
        }.map { (deptName, deptIssues) ->
            val resolvedCount = deptIssues.count { it.status == "Resolved" }
            
            // For now, since we don't have resolved_at, we mock avgDays and overdue 
            // based on the timestamp as a placeholder for "real" logic
            val overdueCount = deptIssues.count { issue ->
                issue.status != "Resolved" && 
                (System.currentTimeMillis() - (issue.timestamp ?: 0)) > 2 * 24 * 3600000 // Over 48 hours
            }

            DepartmentStat(
                name = deptName,
                resolved = resolvedCount,
                avgDays = if (resolvedCount > 0) (1..3).random() else 0, // Mocked for now
                overdue = overdueCount
            )
        }

        _stats.value = statsList
        
        val totalResolved = issues.count { it.status == "Resolved" }
        _overallResolutionRate.value = (totalResolved.toFloat() / issues.size) * 100
    }
}
