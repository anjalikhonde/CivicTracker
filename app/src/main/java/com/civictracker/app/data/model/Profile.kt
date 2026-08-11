package com.civictracker.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    @SerialName("user_id") val userId: String,
    val role: String = "citizen"
)
