package com.civictracker.app.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

data class ComplaintClassifyRequest(
    val text: String
)

data class ComplaintClassifyResponse(
    val category: String
)

interface MLApiService {
    @POST("/")
    suspend fun classifyComplaint(
        @Body request: ComplaintClassifyRequest
    ): ComplaintClassifyResponse

    companion object {
        const val BASE_URL = "https://d5wvqs526bveh5mlwrplvjafm40vwudw.lambda-url.ap-southeast-2.on.aws/"
    }
}
