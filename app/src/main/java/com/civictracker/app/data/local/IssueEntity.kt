package com.civictracker.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "issues")
data class IssueEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val category: String,
    val latitude: Double,
    val longitude: Double,
    val imageUrl: String?,
    val status: String,
    val timestamp: Long,
    val userId: String
)
