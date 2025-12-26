package com.example.personallevelingsystem.model

import kotlinx.serialization.Serializable

@Serializable
data class FoodAnalysisResponse(
    val dish_name: String,
    val ingredients: List<Ingredient>
)

@Serializable
data class Ingredient(
    val name: String,
    val estimated_weight_g: Double,
    val calories: Double,
    val protein_g: Double,
    val carbs_g: Double,
    val fat_g: Double,
    val fiber_g: Double
)
