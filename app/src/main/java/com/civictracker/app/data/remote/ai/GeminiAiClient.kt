package com.civictracker.app.data.remote.ai

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.civictracker.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

object GeminiAiClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true }
    private val mediaType = "application/json".toMediaType()

    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"
    private const val GEMINI_MODEL = "gemini-3.5-flash-lite"

    private val validCategories = listOf("Road", "Water", "Waste", "Lighting", "Drainage", "Electricity", "General")

    suspend fun analyzeIssue(title: String, description: String, bitmap: Bitmap?): ClaudeAnalysisResult? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank()) {
            Log.e("GeminiAiClient", "API Key is missing!")
            return@withContext null
        }

        val prompt = """
            Analyze the following civic complaint evidence (description and photo) and provide a detailed analysis in strict JSON format.
            Title: $title
            Description: $description
            
            Valid Categories: Road, Water, Electricity, Waste, Lighting, Drainage, General.
            Valid Priorities: High, Medium, Low.
            Valid Sentiments: Urgent, Angry, Concerned, Neutral.
            
            Tasks:
            1. Detect the correct category from the list above based on the evidence.
            2. Generate a clear, professional, and detailed description of the issue suitable for a formal report.
            
            Return a JSON object with exactly these fields:
            {
              "category": "Suggested Category",
              "priority": "Predicted Priority",
              "sentiment": "Detected Sentiment",
              "professionalDescription": "A professional rewrite of the complaint description",
              "identifiedObject": "Briefly describe the main object/issue identified in the image",
              "imageInsight": "A short helpful sentence about the issue visible in the image"
            }
            Only return the JSON.
        """.trimIndent()

        val requestBody = buildGeminiRequest(prompt, bitmap)
        val url = "$BASE_URL$GEMINI_MODEL:generateContent?key=$apiKey"

        val request = Request.Builder()
            .url(url)
            .post(requestBody.toRequestBody(mediaType))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string()
                if (!response.isSuccessful) {
                    Log.e("GeminiAiClient", "API call failed (${response.code}): $responseBody")
                    return@withContext null
                }
                
                val resultText = extractTextFromGeminiResponse(responseBody)
                resultText?.let { 
                    try {
                        json.decodeFromString<ClaudeAnalysisResult>(it)
                    } catch (e: Exception) {
                        Log.e("GeminiAiClient", "JSON Decode failed for: $it", e)
                        null
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("GeminiAiClient", "Network request failed", e)
            null
        }
    }

    suspend fun classifyCategory(description: String): String? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank()) return@withContext null

        val prompt = """
            Classify the following civic complaint description into EXACTLY one of these categories:
            Road, Water, Waste, Lighting, Drainage, Electricity, General
            
            Description: $description
            
            Respond with ONLY the category word, nothing else - no explanation, no punctuation, just the single category name exactly as spelled in that list.
        """.trimIndent()

        val requestBody = buildGeminiRequest(prompt, null)
        val url = "$BASE_URL$GEMINI_MODEL:generateContent?key=$apiKey"

        val request = Request.Builder()
            .url(url)
            .post(requestBody.toRequestBody(mediaType))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val responseBody = response.body?.string()
                val rawResponse = extractTextFromGeminiResponse(responseBody)?.trim() ?: return@withContext null
                
                validCategories.find { it.equals(rawResponse, ignoreCase = true) }
            }
        } catch (e: Exception) {
            Log.e("GeminiAiClient", "Error in Gemini classification: ${e.message}")
            null
        }
    }

    private fun buildGeminiRequest(prompt: String, bitmap: Bitmap?): String {
        return buildJsonObject {
            putJsonArray("contents") {
                addJsonObject {
                    putJsonArray("parts") {
                        addJsonObject {
                            put("text", prompt)
                        }
                        bitmap?.let {
                            val outputStream = ByteArrayOutputStream()
                            it.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                            val base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
                            addJsonObject {
                                putJsonObject("inline_data") {
                                    put("mime_type", "image/jpeg")
                                    put("data", base64Image)
                                }
                            }
                        }
                    }
                }
            }
        }.toString()
    }

    private fun extractTextFromGeminiResponse(responseBody: String?): String? {
        if (responseBody == null) return null
        return try {
            val root = json.parseToJsonElement(responseBody)
            val text = root.jsonObject["candidates"]
                ?.jsonArray?.get(0)
                ?.jsonObject?.get("content")
                ?.jsonObject?.get("parts")
                ?.jsonArray?.get(0)
                ?.jsonObject?.get("text")
                ?.jsonPrimitive?.content
            
            // Clean markdown
            if (text != null && text.contains("```json")) {
                text.substringAfter("```json").substringBefore("```").trim()
            } else if (text != null && text.contains("```")) {
                text.substringAfter("```").substringBefore("```").trim()
            } else {
                text?.trim()
            }
        } catch (e: Exception) {
            Log.e("GeminiAiClient", "Extracting text failed", e)
            null
        }
    }
}
