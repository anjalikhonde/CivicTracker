package com.civictracker.app.data.repository

import com.civictracker.app.data.local.IssueDao
import com.civictracker.app.data.model.Issue
import com.civictracker.app.data.remote.CivicApi
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IssueRepository @Inject constructor(
    private val api: CivicApi,
    private val dao: IssueDao
) {
    val issues: Flow<List<Issue>> = dao.getAllIssues()

    suspend fun refreshIssues() {
        try {
            val remoteIssues = api.getIssues()
            dao.insertIssues(remoteIssues)
        } catch (e: Exception) {
            // Handle error
        }
    }

    suspend fun getUserIssues(userId: String): List<Issue> {
        return try {
            val userIssues = api.getUserIssues(userId)
            dao.insertIssues(userIssues)
            userIssues
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getIssueById(id: String): Issue? {
        return dao.getIssueById(id) ?: try {
            api.getIssueById(id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun reportIssue(
        title: RequestBody,
        description: RequestBody,
        category: RequestBody,
        latitude: RequestBody,
        longitude: RequestBody,
        image: MultipartBody.Part
    ): Result<Issue> {
        return try {
            val response = api.reportIssue(title, description, category, latitude, longitude, image)
            dao.insertIssue(response)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun upvoteIssue(id: String) {
        try {
            val updatedIssue = api.upvoteIssue(id)
            dao.insertIssue(updatedIssue)
        } catch (e: Exception) {
            // Handle error
        }
    }
}
