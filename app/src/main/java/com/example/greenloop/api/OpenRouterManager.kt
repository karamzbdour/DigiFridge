package com.example.greenloop.api

import android.graphics.Bitmap
import android.util.Base64
import com.example.greenloop.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.ByteArrayOutputStream

object OpenRouterManager {

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://openrouter.ai/api/v1/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val service = retrofit.create(OpenRouterService::class.java)

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    suspend fun analyzeImage(bitmap: Bitmap, prompt: String): String? {
        val base64Image = bitmapToBase64(bitmap)
        val request = OpenRouterRequest(
            messages = listOf(
                Message(
                    content = listOf(
                        MessageContent(type = "text", text = prompt),
                        MessageContent(
                            type = "image_url",
                            imageUrl = ImageUrl(url = "data:image/jpeg;base64,$base64Image")
                        )
                    )
                )
            )
        )

        return try {
            val response = service.getCompletion(
                apiKey = "Bearer ${BuildConfig.OPENROUTER_API_KEY}",
                request = request
            )
            response.choices.firstOrNull()?.message?.content
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}
