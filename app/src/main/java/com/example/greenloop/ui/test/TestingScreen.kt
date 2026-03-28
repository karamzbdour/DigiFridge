package com.example.greenloop.ui.test

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.greenloop.api.GeminiUtils
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun TestingScreen() {
    var displayText by remember { mutableStateOf("Waiting for click...") }
    val coroutineScope = rememberCoroutineScope()
    // We use a Column here to stack the Text and Button vertically
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally, // Centers items horizontally
        verticalArrangement = Arrangement.Center // Centers items vertically
    ) {
        Text(
            text = displayText,
            style = MaterialTheme.typography.headlineMedium
        )

        // Adds a little bit of spacing between the text and the button
        Spacer(modifier = Modifier.height(24.dp))

        // Your new button!
        Button(
            onClick = {
                // Put whatever code you want to test right here
                println("The test button was clicked!")
                val query = "Pretend you are a chicken. What is your name?"
                coroutineScope.launch {
                    try {
                        displayText = "Generating model..."
                        val model = Firebase.ai(backend = GenerativeBackend.googleAI())
                            .generativeModel("gemini-2.5-flash")

                        val prompt = "Write a story about a magic backpack in under 20 words."

                        displayText = "Querying model..."
                        val response = model.generateContent(prompt)
                        displayText = "Converting response..."
                        displayText = response.text.toString()
                    } catch (e: Exception) {
                        println("Error: ${e.message}")
                        delay(1000)
                        displayText = "Error"
                    }
                }

            }
        ) {
            Text("Run Test Action")
        }
    }
}