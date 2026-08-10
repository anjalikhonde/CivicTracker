package com.civictracker.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "issues")
data class Issue(
    @PrimaryKey val id: String,
    @SerialName("user_id") val userId: String,
    val title: String,
    val description: String? = null,
    val category: String? = null,
    val priority: String? = "Medium",
    val sentiment: String? = "Neutral",
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    val status: String? = "Pending",
    val upvotes: Int? = 0,
    val timestamp: Long? = System.currentTimeMillis()
)
