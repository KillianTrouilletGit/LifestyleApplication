package com.example.personallevelingsystem.ui.compose.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.personallevelingsystem.model.Meal
import com.example.personallevelingsystem.ui.compose.components.JuicyButton
import com.example.personallevelingsystem.ui.compose.components.JuicyInput
import com.example.personallevelingsystem.ui.compose.components.JuicyCard
import com.example.personallevelingsystem.ui.compose.components.OperatorHeader
import com.example.personallevelingsystem.ui.compose.theme.DesignSystem
import com.example.personallevelingsystem.ui.compose.theme.PersonalLevelingSystemTheme
import com.example.personallevelingsystem.viewmodel.HealthViewModel

@Composable
fun NutritionScreen(
    viewModel: HealthViewModel,
    onBackClick: () -> Unit
) {
    var foodQuery by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    val loggedMeal by viewModel.nutritionResult.observeAsState()

    NutritionContent(
        foodQuery = foodQuery,
        onQueryChange = { foodQuery = it },
        quantity = quantity,
        onQuantityChange = { quantity = it },
        onSearchAndLog = {
            val qty = quantity.toDoubleOrNull()
            if (foodQuery.isNotEmpty() && qty != null) {
                viewModel.fetchAndSaveMeal(foodQuery, qty)
            }
        },
        loggedMeal = loggedMeal,
        onBackClick = onBackClick
    )
}

@Composable
fun NutritionContent(
    foodQuery: String,
    onQueryChange: (String) -> Unit,
    quantity: String,
    onQuantityChange: (String) -> Unit,
    onSearchAndLog: () -> Unit,
    loggedMeal: Meal?,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(DesignSystem.Padding)
    ) {
        OperatorHeader(subtitle = "Fuel Management", title = "Nutrition Monitor")

        Spacer(modifier = Modifier.height(24.dp))

        JuicyInput(
            value = foodQuery,
            onValueChange = onQueryChange,
            placeholder = "FOOD ITEM NAME",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        JuicyInput(
            value = quantity,
            onValueChange = onQuantityChange,
            placeholder = "QUANTITY (GRAMS)",
            keyboardType = KeyboardType.Number,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        JuicyButton(
            text = "ANALYZE & LOG",
            onClick = onSearchAndLog,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (loggedMeal != null) {
            JuicyCard(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "LOGGED MEAL STATS", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Calories: %.1f".format(loggedMeal.calories), color = MaterialTheme.colorScheme.primary)
                    Text(text = "Protein: %.1f g".format(loggedMeal.protein), color = MaterialTheme.colorScheme.onSurface)
                    Text(text = "Carbs: %.1f g".format(loggedMeal.carbs), color = MaterialTheme.colorScheme.onSurface)
                    Text(text = "Fat: %.1f g".format(loggedMeal.fat), color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        JuicyButton(
            text = "RETURN",
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NutritionScreenPreview() {
    PersonalLevelingSystemTheme {
        NutritionContent(
            foodQuery = "Apple",
            onQueryChange = {},
            quantity = "100",
            onQuantityChange = {},
            onSearchAndLog = {},
            loggedMeal = Meal(
                date = 123L, time = "123", calories = 52.0, protein = 0.3, fat = 0.2, carbs = 14.0, fiber = 2.4, balanceIndex = 0.8
            ),
            onBackClick = {}
        )
    }
}
