package com.example.personallevelingsystem.ui.compose.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personallevelingsystem.ui.compose.components.ArcButton
import com.example.personallevelingsystem.ui.compose.components.ArcCard
import com.example.personallevelingsystem.ui.compose.components.ArcProgressBar
import com.example.personallevelingsystem.ui.compose.components.ArcSectionLabel
import com.example.personallevelingsystem.ui.compose.components.JuicyInput
import com.example.personallevelingsystem.ui.compose.theme.AccentViolet
import com.example.personallevelingsystem.ui.compose.theme.DesignSystem
import com.example.personallevelingsystem.ui.compose.theme.PersonalLevelingSystemTheme
import com.example.personallevelingsystem.ui.compose.theme.SignalCyan
import com.example.personallevelingsystem.ui.compose.theme.TelemetryGreen
import com.example.personallevelingsystem.ui.compose.theme.TextSecondary
import com.example.personallevelingsystem.ui.compose.theme.tabular
import com.example.personallevelingsystem.util.hapticConfirm
import com.example.personallevelingsystem.viewmodel.HealthViewModel

/** Quick-add presets, in ml — same steps as the notification actions. */
private val QUICK_ADD_ML = listOf(250f, 500f, 750f)

@Composable
fun WaterScreen(viewModel: HealthViewModel) {
    val totalWaterMl by viewModel.totalWaterToday.observeAsState(initial = 0f)
    val targetMl by viewModel.waterTargetMl.observeAsState(initial = 2500f)
    var inputAmount by remember { mutableStateOf("") }

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
                inputAmount = ""
            }
        }
    )
}

@Composable
fun WaterContent(
    totalWaterMl: Float,
    targetMl: Float,
    inputAmount: String,
    onInputChange: (String) -> Unit,
    onQuickAdd: (Float) -> Unit,
    onSave: () -> Unit
) {
    val view = LocalView.current
    val ratio = if (targetMl > 0f) (totalWaterMl / targetMl).coerceIn(0f, 1f) else 0f
    val quotaMet = targetMl > 0f && totalWaterMl >= targetMl
    val tint = if (quotaMet) TelemetryGreen else SignalCyan

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(DesignSystem.Padding),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ArcCard(modifier = Modifier.fillMaxWidth(), accent = tint) {
            Text(
                text = "TODAY",
                style = MaterialTheme.typography.labelMedium,
                color = AccentViolet,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "%.2f L".format(totalWaterMl / 1000f),
                    style = MaterialTheme.typography.displayMedium.tabular,
                    color = tint
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "target %.1f L".format(targetMl / 1000f),
                    style = MaterialTheme.typography.labelSmall.tabular,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            ArcProgressBar(progress = ratio, color = tint, height = 6.dp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${totalWaterMl.toInt()} / ${targetMl.toInt()} ml" +
                    if (quotaMet) " · quota met" else "",
                style = MaterialTheme.typography.labelSmall.tabular,
                color = TextSecondary
            )
        }

        ArcSectionLabel(text = "Quick add")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            QUICK_ADD_ML.forEach { ml ->
                ArcButton(
                    text = "+${ml.toInt()} ml",
                    onClick = {
                        view.hapticConfirm()
                        onQuickAdd(ml)
                    },
                    compact = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        ArcSectionLabel(text = "Custom amount")
        JuicyInput(
            value = inputAmount,
            onValueChange = onInputChange,
            placeholder = "Amount (ml)",
            keyboardType = KeyboardType.Number,
            modifier = Modifier.fillMaxWidth()
        )
        ArcButton(
            text = "Log intake",
            icon = Icons.Rounded.WaterDrop,
            showChevron = false,
            enabled = inputAmount.isNotBlank(),
            onClick = {
                view.hapticConfirm()
                onSave()
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0A10)
@Composable
fun WaterScreenPreview() {
    PersonalLevelingSystemTheme {
        WaterContent(
            totalWaterMl = 1500f,
            targetMl = 2500f,
            inputAmount = "500",
            onInputChange = {},
            onQuickAdd = {},
            onSave = {}
        )
    }
}
