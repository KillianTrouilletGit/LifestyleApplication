package com.example.personallevelingsystem.ui.compose.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.personallevelingsystem.ui.compose.components.JuicyButton
import com.example.personallevelingsystem.ui.compose.components.JuicyInput
import com.example.personallevelingsystem.ui.compose.components.OperatorHeader
import com.example.personallevelingsystem.ui.compose.theme.CalmBlue
import com.example.personallevelingsystem.ui.compose.theme.DesignSystem
import com.example.personallevelingsystem.ui.compose.theme.PersonalLevelingSystemTheme
import com.example.personallevelingsystem.ui.compose.theme.TelemetryGreen
import com.example.personallevelingsystem.viewmodel.HealthViewModel

/** Quick-add presets, in ml — same steps as the notification actions. */
private val QUICK_ADD_ML = listOf(250f, 500f, 750f)

@Composable
fun WaterScreen(
    viewModel: HealthViewModel,
    onBackClick: () -> Unit
) {
    val totalWaterMl by viewModel.totalWaterToday.observeAsState(initial = 0f)
    val targetMl by viewModel.waterTargetMl.observeAsState(initial = 2500f)
    var inputAmount by remember { mutableStateOf("") }

    // Refresh data on enter
    LaunchedEffect(Unit) {
        viewModel.calculateTotalWaterForToday()
    }

    WaterContent(
        totalWaterMl = totalWaterMl,
        targetMl = targetMl,
        inputAmount = inputAmount,
        onInputChange = { inputAmount = it },
        onQuickAdd = { ml -> viewModel.saveWater(ml) },
        onSave = {
            val amount = inputAmount.replace(',', '.').toFloatOrNull()
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
    totalWaterMl: Float,
    targetMl: Float,
    inputAmount: String,
    onInputChange: (String) -> Unit,
    onQuickAdd: (Float) -> Unit,
    onSave: () -> Unit,
    onBackClick: () -> Unit
) {
    val ratio = if (targetMl > 0f) (totalWaterMl / targetMl).coerceIn(0f, 1f) else 0f
    val quotaMet = targetMl > 0f && totalWaterMl >= targetMl

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
                text = "%.2f L".format(totalWaterMl / 1000f),
                style = MaterialTheme.typography.displayMedium,
                color = if (quotaMet) TelemetryGreen else MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { ratio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (quotaMet) TelemetryGreen else CalmBlue,
                trackColor = CalmBlue.copy(alpha = 0.15f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${totalWaterMl.toInt()} / ${targetMl.toInt()} ml" +
                        if (quotaMet) " · QUOTA MET" else "",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            QUICK_ADD_ML.forEach { ml ->
                JuicyButton(
                    text = "+${ml.toInt()} ML",
                    onClick = { onQuickAdd(ml) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        JuicyInput(
            value = inputAmount,
            onValueChange = onInputChange,
            placeholder = "AMOUNT (ML)",
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
            totalWaterMl = 1500f,
            targetMl = 2500f,
            inputAmount = "500",
            onInputChange = {},
            onQuickAdd = {},
            onSave = {},
            onBackClick = {}
        )
    }
}
