package com.tasktracker.notifier.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

@Serializable
data class PerplexityRequest(
    val model: String = "sonar-pro",
    val messages: List<Message>,
    val temperature: Double = 0.2,
    val max_tokens: Int = 1000
)

@Serializable
data class Message(
    val role: String,
    val content: String
)

@Serializable
data class PerplexityResponse(
    val id: String,
    val model: String,
    val choices: List<Choice>
)

@Serializable
data class Choice(
    val index: Int,
    val message: Message,
    val finish_reason: String
)

class PerplexityClient(private val apiKey: String) {
    private val logger = KotlinLogging.logger {}

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
        encodeDefaults = true
    }

    private val mediaType = "application/json".toMediaType()

    suspend fun analyzeTaskInsights(tasks: String): String = withContext(Dispatchers.IO) {
        val prompt = buildPrompt(tasks)

        val requestBody = PerplexityRequest(
            messages = listOf(
                Message(
                    role = "system",
                    content = "You are a productivity assistant analyzing task data. Provide brief, actionable insights."
                ),
                Message(
                    role = "user",
                    content = prompt
                )
            )
        )

        val requestJson = json.encodeToString(PerplexityRequest.serializer(), requestBody)

        logger.debug { "Sending request to Perplexity API: $requestJson" }

        val request = Request.Builder()
            .url("https://api.perplexity.ai/chat/completions")
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .post(requestJson.toRequestBody(mediaType))
            .build()

        val response = client.newCall(request).execute()

        val responseBody = response.body?.string()
            ?: throw IOException("Empty response from Perplexity API")

        if (!response.isSuccessful) {
            throw IOException("Perplexity API error ${response.code}: $responseBody")
        }

        val perplexityResponse = json.decodeFromString<PerplexityResponse>(responseBody)

        perplexityResponse.choices.firstOrNull()?.message?.content
            ?: "No insights generated"
    }

    private fun buildPrompt(tasks: String): String = """
        Analyze these tasks and provide a 1-2 sentence summary with the most important insight or action.

        Be extremely concise and actionable.

        Tasks:
        $tasks
    """.trimIndent()
}
