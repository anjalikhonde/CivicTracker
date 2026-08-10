package com.civictracker.app.ui.screens

data class AuditEntry(
    val date: String,
    val title: String,
    val description: String,
    val isCompleted: Boolean = true
)

data class EnhancedIssue(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val latitude: Double,
    val longitude: Double,
    val imageUrl: String,
    val status: String,
    val urgency: Int,
    val phase: Int,
    val ward: String,
    val timeAgo: String,
    val upvotes: Int,
    val sla: String? = null,
    val department: String? = null,
    val timeline: List<AuditEntry> = emptyList()
)

data class DepartmentStat(
    val name: String,
    val resolved: Int,
    val avgDays: Int,
    val overdue: Int
)

object MockData {
    val sampleIssues = listOf(
        EnhancedIssue(
            id = "1",
            title = "Damaged Streetlight - Oak St.",
            description = "Exposed wiring and no illumination for 3 consecutive nights. Potential safety hazard for...",
            category = "ROAD",
            latitude = 19.0760,
            longitude = 72.8777,
            imageUrl = "https://images.unsplash.com/photo-1542838132-92c53300491e?auto=format&fit=crop&q=80&w=1000",
            status = "ASSIGNED",
            urgency = 88,
            phase = 3,
            ward = "Ward 4",
            timeAgo = "2h ago",
            upvotes = 124,
            sla = "04:22:16",
            department = "Sanitation Dept",
            timeline = listOf(
                AuditEntry("MAR 11, 09:12 AM", "Government Routes to Sanitation", "Internal dispatch protocol initiated. High priority flag attached."),
                AuditEntry("MAR 11, 04:30 PM", "Community Consensus Reached", "Issue validated by 12 residents in Ward 4. Urgency score updated to 88.")
            )
        ),
        EnhancedIssue(
            id = "2",
            title = "Minor Pipe Leak - 5th Ave",
            description = "Slow leak observed near the main fire hydrant. Water pooling but not obstructing traffic.",
            category = "WATER",
            latitude = 19.0850,
            longitude = 72.8850,
            imageUrl = "https://images.unsplash.com/photo-1584467541268-b040f83be3fd?auto=format&fit=crop&q=80&w=1000",
            status = "REPORTED",
            urgency = 42,
            phase = 1,
            ward = "Ward 4",
            timeAgo = "6h ago",
            upvotes = 45
        ),
        EnhancedIssue(
            id = "3",
            title = "Deep Pothole - West Bridge",
            description = "Large pothole causing vehicle diversions. Multiple reports submitted by commuters this...",
            category = "ROAD",
            latitude = 19.0600,
            longitude = 72.8600,
            imageUrl = "https://images.unsplash.com/photo-1515162816999-a0c47dc192f7?auto=format&fit=crop&q=80&w=1000",
            status = "VALIDATED",
            urgency = 75,
            phase = 2,
            ward = "Ward 4",
            timeAgo = "12h ago",
            upvotes = 89
        )
    )

    val departmentStats = listOf(
        DepartmentStat("Public Works", 142, 5, 12),
        DepartmentStat("Water & Sewage", 89, 3, 4),
        DepartmentStat("Sanitation", 210, 2, 0)
    )
}
