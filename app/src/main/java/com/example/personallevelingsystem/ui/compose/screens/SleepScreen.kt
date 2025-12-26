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
fun SleepScreen(
    viewModel: HealthViewModel,
    onBackClick: () -> Unit
) {
    var duration by remember { mutableStateOf("") }

    SleepContent(
        duration = duration,
        onDurationChange = { duration = it },
        onSave = {
            if (duration.isNotEmpty()) {
                viewModel.saveSleep(duration)
                onBackClick() // Go back after saving
            }
        },
        onBackClick = onBackClick
    )
}

@Composable
fun SleepContent(
    duration: String,
    onDurationChange: (String) -> Unit,
    onSave: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(DesignSystem.Padding)
    ) {
        OperatorHeader(subtitle = "Recovery Module", title = "Sleep Monitor")

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "ENTER DURATION (HH:MM)",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        JuicyInput(
            value = duration,
            onValueChange = onDurationChange,
            placeholder = "08:00",
            keyboardType = KeyboardType.Text, // Text for colon
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        JuicyButton(
            text = "LOG REST CYCLE",
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
fun SleepScreenPreview() {
    PersonalLevelingSystemTheme {
        SleepContent(
            duration = "07:30",
            onDurationChange = {},
            onSave = {},
            onBackClick = {}
        )
    }
}
