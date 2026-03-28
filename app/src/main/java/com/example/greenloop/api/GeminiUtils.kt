package com.example.greenloop.api
import com.example.greenloop.BuildConfig.GEMINI_API_KEY
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend

class GeminiUtils {
    private val apiKey = GEMINI_API_KEY

    suspend fun queryGemini(prompt: String) {
        val model = Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel("gemini-3-flash-preview")

        val response = model.generateContent(prompt)
        print(response)
    }
}