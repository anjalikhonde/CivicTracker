package com.civictracker.app.data.remote

import com.civictracker.app.data.model.Issue
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface IssueApi {
    @GET("issues")
    suspend fun getIssues(): List<Issue>

    @POST("issues")
    suspend fun createIssue(@Body issue: Issue): Issue

    @Multipart
    @POST("issues/upload")
    suspend fun uploadImage(
        @Part image: MultipartBody.Part,
        @Part("issueId") issueId: RequestBody
    ): Map<String, String>

    @GET("issues/user/{userId}")
    suspend fun getUserIssues(@Path("userId") userId: String): List<Issue>

    @DELETE("issues/{issueId}")
    suspend fun deleteIssue(@Path("issueId") issueId: String)
}
