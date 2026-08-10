package com.civictracker.app.data.remote.ai

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.civictracker.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

@Serializable
data class ClaudeAnalysisResult(
    val category: String,
    val priority: String,
    val sentiment: String,
    val professionalDescription: String = "",
    val identifiedObject: String = "Unknown",
    val imageInsight: String = "No additional insights."
)

@Serializable
data class ClaudeDuplicateResult(
    val isDuplicate: Boolean,
    val matchingId: String? = null
)

object ClaudeAiClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true }
    private val mediaType = "application/json".toMediaType()

    private const val API_URL = "https://api.anthropic.com/v1/messages"
    private const val CLAUDE_MODEL = "claude-3-5-sonnet-20241022"

    private val validCategories = listOf("Road", "Water", "Waste", "Lighting", "Drainage", "Electricity", "General")

    suspend fun analyzeIssue(title: String, description: String, bitmap: Bitmap?): ClaudeAnalysisResult? = withContext(Dispatchers.IO) {
        try {
            val apiKey = BuildConfig.ANTHROPIC_API_KEY
            if (apiKey.isBlank()) {
                Log.e("ClaudeAiClient", "ERROR: ANTHROPIC_API_KEY is BLANK")
                return@withContext null
            }

            Log.d("ClaudeAiClient", "DEBUG: analyzeIssue STARTING for title: $title")

            val prompt = """
                Analyze the following civic complaint evidence and provide a detailed analysis in strict JSON format.
                Return JSON with fields: category, priority, sentiment, professionalDescription, identifiedObject, imageInsight.
                Valid Categories: Road, Water, Electricity, Waste, Lighting, Drainage, General.
                ONLY return JSON. No other text.
            """.trimIndent()

            val base64Image = bitmap?.let {
                val outputStream = ByteArrayOutputStream()
                it.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
                Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
            }

            val messagesJson = buildJsonArray {
                addJsonObject {
                    put("role", "user")
                    putJsonArray("content") {
                        addJsonObject {
                            put("type", "text")
                            put("text", "Input: $title - $description \n\n Task: $prompt")
                        }
                        if (base64Image != null) {
                            addJsonObject {
                                put("type", "image")
                                putJsonObject("source") {
                                    put("type", "base64")
                                    put("media_type", "image/jpeg")
                                    put("data", base64Image)
                                }
                            }
                        }
                    }
                }
            }

            val requestBody = buildJsonObject {
                put("model", CLAUDE_MODEL)
                put("max_tokens", 1024)
                put("messages", messagesJson)
            }.toString()

            val request = Request.Builder()
                .url(API_URL)
                .addHeader("x-api-key", apiKey)
                .addHeader("anthropic-version", "2023-06-01")
                .addHeader("content-type", "application/json")
                .post(requestBody.toRequestBody(mediaType))
                .build()

            Log.d("ClaudeAiClient", "DEBUG: analyzeIssue Executing OkHttp Call...")
            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                Log.d("ClaudeAiClient", "DEBUG: analyzeIssue HTTP ${response.code}")
                Log.d("ClaudeAiClient", "DEBUG: analyzeIssue RAW RESPONSE: $body")
                
                if (!response.isSuccessful) {
                    Log.e("ClaudeAiClient", "ERROR: API failed with code ${response.code}: $body")
                    return@withContext null
                }

                val content = extractTextFromClaudeResponse(body)
                Log.d("ClaudeAiClient", "DEBUG: analyzeIssue EXTRACTED JSON: $content")
                
                if (content != null) {
                    try {
                        val result = json.decodeFromString<ClaudeAnalysisResult>(content)
                        Log.d("ClaudeAiClient", "DEBUG: analyzeIssue SUCCESS - Category: ${result.category}")
                        result
                    } catch (e: Exception) {
                        Log.e("ClaudeAiClient", "ERROR: JSON Decode failed: ${e.message}")
                        null
                    }
                } else {
                    Log.e("ClaudeAiClient", "ERROR: Extracted content was null")
                    null
                }
            }
        } catch (t: Throwable) {
            Log.e("ClaudeAiClient", "FATAL: analyzeIssue Flow Exception: ${t.message}", t)
            null
        }
    }

    suspend fun classifyCategory(description: String): String? = withContext(Dispatchers.IO) {
        try {
            val apiKey = BuildConfig.ANTHROPIC_API_KEY
            if (apiKey.isBlank()) return@withContext null

            val prompt = "Classify this issue into exactly one of: ${validCategories.joinToString()}. Issue: $description. Respond with ONLY the word."

            val messagesJson = buildJsonArray {
                addJsonObject {
                    put("role", "user")
                    putJsonArray("content") {
                        addJsonObject {
                            put("type", "text")
                            put("text", prompt)
                        }
                    }
                }
            }

            val requestBody = buildJsonObject {
                put("model", CLAUDE_MODEL)
                put("max_tokens", 50)
                put("messages", messagesJson)
            }.toString()

            val request = Request.Builder()
                .url(API_URL)
                .addHeader("x-api-key", apiKey)
                .addHeader("anthropic-version", "2023-06-01")
                .post(requestBody.toRequestBody(mediaType))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (!response.isSuccessful) return@withContext null
                val rawResponse = extractTextFromClaudeResponse(body)?.trim() ?: ""
                validCategories.find { it.equals(rawResponse, ignoreCase = true) }
            }
        } catch (t: Throwable) {
            Log.e("ClaudeAiClient", "ERROR: classifyCategory failed: ${t.message}")
            null
        }
    }

    private fun extractTextFromClaudeResponse(responseBody: String): String? {
        return try {
            val root = json.parseToJsonElement(responseBody)
            val text = root.jsonObject["content"]?.jsonArray?.get(0)?.jsonObject?.get("text")?.jsonPrimitive?.content
            if (text == null) return null
            
            when {
                text.contains("```json") -> text.substringAfter("```json").substringBefore("```").trim()
                text.contains("```") -> text.substringAfter("```").substringBefore("```").trim()
                else -> text.trim()
            }
        } catch (e: Exception) { 
            Log.e("ClaudeAiClient", "ERROR: extractText failed: ${e.message}")
            null
        }
    }
}
