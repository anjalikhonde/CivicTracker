package com.civictracker.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "issues")
data class Issue(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val description: String,
    val category: String,
    val latitude: Double,
    val longitude: Double,
    val imageUrl: String,
    val status: String, // e.g., "Pending", "In Progress", "Resolved"
    val upvotes: Int,
    val timestamp: Long
)
