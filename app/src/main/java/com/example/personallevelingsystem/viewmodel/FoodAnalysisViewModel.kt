package com.example.personallevelingsystem.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personallevelingsystem.BuildConfig
import com.example.personallevelingsystem.model.FoodAnalysisResponse
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

sealed class AnalysisState {
    object Idle : AnalysisState()
    object Loading : AnalysisState()
    data class Success(val result: FoodAnalysisResponse) : AnalysisState()
    data class Error(val message: String) : AnalysisState()
}

class FoodAnalysisViewModel : ViewModel() {

    private val _analysisState = MutableStateFlow<AnalysisState>(AnalysisState.Idle)
    val analysisState: StateFlow<AnalysisState> = _analysisState

    private val modelTiers = listOf(
        "gemini-3-flash",
        "gemini-2.5-flash",
        "gemini-2.5-flash-lite"
    )

    private val json = Json { 
        ignoreUnknownKeys = true 
        coerceInputValues = true
    }

    fun analyzeImage(bitmap: Bitmap, apiKey: String) {
        viewModelScope.launch {
            _analysisState.value = AnalysisState.Loading
            
            var lastError: String = "No models available"
            
            for (modelName in modelTiers) {
                try {
                    val generativeModel = GenerativeModel(
                        modelName = modelName,
                        apiKey = apiKey
                    )

                    val prompt = """
                        Analyze this food image and provide a JSON response with the following structure:
                        {
                          "dish_name": "Name of the dish",
                          "ingredients": [
                            {
                              "name": "Ingredient name",
                              "estimated_weight_g": 0.0,
                              "calories": 0.0,
                              "protein_g": 0.0,
                              "carbs_g": 0.0,
                              "fat_g": 0.0,
                              "fiber_g": 0.0
                            }
                          ]
                        }
                        Be as accurate as possible with the weights and nutritional values. Include fiber_g for each ingredient.
                        Return ONLY the JSON object, no other text.
                    """.trimIndent()

                    val inputContent = content {
                        image(bitmap)
                        text(prompt)
                    }

                    val response = generativeModel.generateContent(inputContent)
                    val responseText = response.text?.trim() ?: throw Exception("Empty response from AI")
                    
                    // Remove markdown code blocks if present
                    val cleanedJson = responseText.removePrefix("```json").removeSuffix("```").trim()
                    
                    val result = json.decodeFromString<FoodAnalysisResponse>(cleanedJson)
                    _analysisState.value = AnalysisState.Success(result)
                    return@launch // Success! exit the loop
                } catch (e: Exception) {
                    lastError = e.message ?: "Unknown error with $modelName"
                    // Continue to next model in loop
                }
            }
            
            // If we're here, all models failed
            _analysisState.value = AnalysisState.Error("All AI models failed. Final error: $lastError")
        }
    }
}
