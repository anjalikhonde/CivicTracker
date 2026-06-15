package com.civictracker.app.data.repository

import com.civictracker.app.data.model.Issue
import com.civictracker.app.data.supabase.SupabaseClient.client
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns

class SupabaseRepository {

    // Get all issues
    suspend fun getAllIssues(): List<Issue> {
        return client.postgrest["issues"]
            .select()
            .decodeList<Issue>()
    }

    // Get issues by user
    suspend fun getUserIssues(userId: String): List<Issue> {
        return client.postgrest["issues"]
            .select { filter { eq("user_id", userId) } }
            .decodeList<Issue>()
    }

    // Create a new issue
    suspend fun createIssue(issue: Issue) {
        client.postgrest["issues"].insert(issue)
    }

    // Update issue status
    suspend fun updateIssueStatus(issueId: String, status: String) {
        client.postgrest["issues"]
            .update({ set("status", status) }) {
                filter { eq("id", issueId) }
            }
    }

    // Upvote an issue
    suspend fun upvoteIssue(issueId: String, currentUpvotes: Int) {
        client.postgrest["issues"]
            .update({ set("upvotes", currentUpvotes + 1) }) {
                filter { eq("id", issueId) }
            }
    }
}