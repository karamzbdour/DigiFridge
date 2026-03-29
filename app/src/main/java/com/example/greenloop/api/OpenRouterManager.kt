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

    suspend fun analyzeImage(bitmap: Bitmap): String? {
        val base64Image = bitmapToBase64(bitmap)
        
        val systemPrompt = """
            System Role: You are a Bulletproof Receipt Extraction Specialist for a food-waste prevention app. 
            Your goal is to extract ONLY food items and convert them into high-quality, structured data.

            ### CRITICAL FILTERING RULES:
            1. ONLY INCLUDE FOOD AND BEVERAGES: If it's not edible or drinkable, IGNORE IT.
            2. EXCLUDE DISCOUNTS: Completely ignore lines starting with '-', 'SAVINGS', 'MULTIBUY', 'DISCOUNT', or 'VOUCHER'.
            3. EXCLUDE LOYALTY/POINTS: Ignore Nectar points, Clubcard points, or any reward point balance lines.
            4. EXCLUDE NON-FOOD: Ignore carrier bags, cleaning supplies, toiletries, hardware, or clothing.

            ### EXTRACTION & STANDARDIZATION RULES:
            1. Mandatory Fields: human_readable_name, price, and shop_date.
            2. Date Fallback: If shop_date is missing, use March 28, 2026.
            3. Name Standardization: Convert receipt abbreviations into clean, professional titles. 
               Examples: 
               - "ORG STRAWBRY 1LB" -> "Organic Strawberries"
               - "LURPAK SLTD 250G" -> "Lurpak Salted Butter"
               - "CHICK BRST FLLT" -> "Chicken Breast Fillets"
            4. No Hallucinations: If a value is unknown, set to null. Do not invent data.
            5. Expiry Logic: Calculate 'days_to_expiry' based on standard USDA FoodKeeper shelf-life from the shop_date.

            ### OUTPUT SCHEMA:
            Return ONLY a valid JSON object:
            {
              "metadata": {
                "shop_name": "string",
                "shop_date": "YYYY-MM-DD",
                "total_price": float,
                "currency": "string"
              },
              "items": [
                {
                  "raw_text": "string (original receipt line)",
                  "human_readable_name": "string (cleaned name)",
                  "price": float,
                  "category": "string (e.g., Dairy, Produce, Meat, Bakery, Pantry)",
                  "days_to_expiry": integer
                }
              ]
            }
        """.trimIndent()

        val request = OpenRouterRequest(
            messages = listOf(
                Message(
                    role = "system",
                    content = listOf(MessageContent(type = "text", text = systemPrompt))
                ),
                Message(
                    role = "user",
                    content = listOf(
                        MessageContent(
                            type = "image_url",
                            imageUrl = ImageUrl(url = "data:image/jpeg;base64,${base64Image}")
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

    suspend fun generateContent(prompt: String): String? {
        val request = OpenRouterRequest(
            messages = listOf(
                Message(
                    role = "user",
                    content = listOf(MessageContent(type = "text", text = prompt))
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
            null
        }
    }
}
