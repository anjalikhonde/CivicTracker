package com.civictracker.app.data.repository

import com.civictracker.app.data.model.Issue
import com.civictracker.app.data.supabase.SupabaseClient.client
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import java.util.UUID

class SupabaseRepository {

    suspend fun getAllIssues(): List<Issue> {
        return try {
            client.from("issues").select().decodeList<Issue>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getUserIssues(userId: String): List<Issue> {
        return try {
            // Using "user_id" to match the column name in the database/SerialName
            client.from("issues").select {
                filter {
                    eq("user_id", userId)
                }
            }.decodeList<Issue>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getIssueById(id: String): Issue? {
        return try {
            client.from("issues").select {
                filter {
                    eq("id", id)
                }
            }.decodeSingleOrNull<Issue>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun uploadImage(byteArray: ByteArray): String {
        val fileName = "${UUID.randomUUID()}.jpg"
        val bucket = client.storage.from("issue-images")
        bucket.upload(fileName, byteArray)
        return bucket.publicUrl(fileName)
    }

    suspend fun createIssue(issue: Issue) {
        try {
            client.from("issues").insert(issue)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun updateIssueStatus(issueId: String, status: String) {
        try {
            client.from("issues").update(
                {
                    set("status", status)
                }
            ) {
                filter {
                    eq("id", issueId)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun upvoteIssue(issueId: String, currentUpvotes: Int) {
        try {
            client.from("issues").update(
                {
                    set("upvotes", currentUpvotes + 1)
                }
            ) {
                filter {
                    eq("id", issueId)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deleteIssue(issueId: String) {
        try {
            client.from("issues").delete {
                filter {
                    eq("id", issueId)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}
