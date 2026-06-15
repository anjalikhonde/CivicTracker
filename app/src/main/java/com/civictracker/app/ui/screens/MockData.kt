package com.civictracker.app.ui.screens

import com.civictracker.app.data.model.Issue
import java.util.UUID

object MockData {
    val sampleIssues = listOf(
        Issue(
            id = "1",
            userId = "user1",
            title = "Broken Street Light",
            description = "The street light has been flickering and now it is completely off. It's very dark at night.",
            category = "Lighting",
            latitude = 12.9716,
            longitude = 77.5946,
            imageUrl = "https://images.unsplash.com/photo-1542838132-92c53300491e?auto=format&fit=crop&q=80&w=1000",
            status = "Open",
            upvotes = 12,
            timestamp = System.currentTimeMillis() - 86400000
        ),
        Issue(
            id = "2",
            userId = "user2",
            title = "Large Pothole",
            description = "A massive pothole has formed in the middle of the road, causing cars to swerve dangerously.",
            category = "Road",
            latitude = 12.9800,
            longitude = 77.6000,
            imageUrl = "https://images.unsplash.com/photo-1515162816999-a0c47dc192f7?auto=format&fit=crop&q=80&w=1000",
            status = "In Progress",
            upvotes = 45,
            timestamp = System.currentTimeMillis() - 172800000
        ),
        Issue(
            id = "3",
            userId = "user3",
            title = "Overflowing Drain",
            description = "The drainage system is blocked and waste water is spilling onto the sidewalk.",
            category = "Drainage",
            latitude = 12.9600,
            longitude = 77.5800,
            imageUrl = "https://images.unsplash.com/photo-1584467541268-b040f83be3fd?auto=format&fit=crop&q=80&w=1000",
            status = "Resolved",
            upvotes = 8,
            timestamp = System.currentTimeMillis() - 259200000
        )
    )

    data class DepartmentStats(
        val department: String,
        val issuesResolved: Int,
        val avgResolutionTimeDays: Int,
        val overdueCount: Int
    )

    val monthlyStats = listOf(
        DepartmentStats("Public Works (Roads)", 142, 5, 12),
        DepartmentStats("Water & Sewage", 89, 3, 4),
        DepartmentStats("Electricity/Lighting", 210, 2, 0),
        DepartmentStats("Waste Management", 350, 1, 2)
    )
}
