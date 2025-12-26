package com.example.personallevelingsystem.ui.compose.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.personallevelingsystem.ui.compose.components.JuicyButton
import com.example.personallevelingsystem.ui.compose.components.JuicyInput
import com.example.personallevelingsystem.ui.compose.components.OperatorHeader
import com.example.personallevelingsystem.ui.compose.theme.DesignSystem
import com.example.personallevelingsystem.ui.compose.theme.PersonalLevelingSystemTheme
import com.example.personallevelingsystem.viewmodel.HealthViewModel

@Composable
fun WaterScreen(
    viewModel: HealthViewModel,
    onBackClick: () -> Unit
) {
    val totalWater by viewModel.totalWaterToday.observeAsState(initial = 0f)
    var inputAmount by remember { mutableStateOf("") }
    
    // Refresh data on enter
    LaunchedEffect(Unit) {
        viewModel.calculateTotalWaterForToday()
    }

    WaterContent(
        totalWater = totalWater,
        inputAmount = inputAmount,
        onInputChange = { inputAmount = it },
        onSave = {
            val amount = inputAmount.toFloatOrNull()
            if (amount != null && amount > 0) {
                viewModel.saveWater(amount)
                inputAmount = "" // Reset input
            }
        },
        onBackClick = onBackClick
    )
}

@Composable
fun WaterContent(
    totalWater: Float,
    inputAmount: String,
    onInputChange: (String) -> Unit,
    onSave: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(DesignSystem.Padding)
    ) {
        OperatorHeader(subtitle = "Hydration Monitor", title = "H2O Levels")

        Spacer(modifier = Modifier.height(32.dp))

        // Display current status
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center
        ) {
             Text(
                text = "DAILY INTAKE",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = "$totalWater L",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        JuicyInput(
            value = inputAmount,
            onValueChange = onInputChange,
            placeholder = "AMOUNT (LITERS)",
            keyboardType = KeyboardType.Number,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        JuicyButton(
            text = "LOG INTAKE",
            onClick = onSave,
            modifier = Modifier.fillMaxWidth()
        )

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
fun WaterScreenPreview() {
    PersonalLevelingSystemTheme {
        WaterContent(
            totalWater = 1.5f,
            inputAmount = "0.5",
            onInputChange = {},
            onSave = {},
            onBackClick = {}
        )
    }
}
