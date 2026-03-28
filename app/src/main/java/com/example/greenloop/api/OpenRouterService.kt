package com.example.greenloop.api

import com.squareup.moshi.Json
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

data class OpenRouterRequest(
    val model: String = "google/gemini-2.0-flash-001",
    val messages: List<Message>
)

data class Message(
    val role: String = "user",
    val content: List<MessageContent>
)

data class MessageContent(
    val type: String,
    val text: String? = null,
    @param:Json(name = "image_url") val imageUrl: ImageUrl? = null
)

data class ImageUrl(
    val url: String
)

data class OpenRouterResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: ResponseMessage
)

data class ResponseMessage(
    val content: String
)

interface OpenRouterService {
    @POST("chat/completions")
    suspend fun getCompletion(
        @Header("Authorization") apiKey: String,
        @Header("HTTP-Referer") referer: String = "https://greenloop.app",
        @Body request: OpenRouterRequest
    ): OpenRouterResponse
}
