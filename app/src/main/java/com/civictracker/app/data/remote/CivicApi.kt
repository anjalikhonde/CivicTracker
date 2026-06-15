package com.civictracker.app.data.remote

import com.civictracker.app.data.model.Issue
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface CivicApi {
    @GET("issues")
    suspend fun getIssues(): List<Issue>

    @GET("issues/user/{userId}")
    suspend fun getUserIssues(@Path("userId") userId: String): List<Issue>

    @GET("issues/{id}")
    suspend fun getIssueById(@Path("id") id: String): Issue

    @POST("issues")
    suspend fun createIssue(@Body issue: Issue): Issue

    @Multipart
    @POST("issues/report")
    suspend fun reportIssue(
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("category") category: RequestBody,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody,
        @Part image: MultipartBody.Part
    ): Issue

    @POST("issues/{id}/upvote")
    suspend fun upvoteIssue(@Path("id") id: String): Issue

    companion object {
        const val BASE_URL = "http://10.0.2.2:4000/"
    }
}
