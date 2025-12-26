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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personallevelingsystem.ui.compose.components.JuicyButton
import com.example.personallevelingsystem.ui.compose.components.OperatorHeader
import com.example.personallevelingsystem.ui.compose.theme.DesignSystem
import com.example.personallevelingsystem.ui.compose.theme.PersonalLevelingSystemTheme
import com.example.personallevelingsystem.viewmodel.TrainingViewModel
import kotlinx.coroutines.delay

@Composable
fun FlexibilityScreen(
    viewModel: TrainingViewModel,
    onBackClick: () -> Unit
) {
    FlexibilityContent(
        onSave = { duration ->
            viewModel.saveFlexibilityTraining(duration)
            onBackClick()
        },
        onBackClick = onBackClick
    )
}

@Composable
fun FlexibilityContent(
    onSave: (Long) -> Unit,
    onBackClick: () -> Unit
) {
    var isRunning by remember { mutableStateOf(false) }
    var startTime by remember { mutableStateOf(0L) }
    var elapsedTime by remember { mutableStateOf(0L) }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            startTime = System.currentTimeMillis() - elapsedTime
            while (isRunning) {
                elapsedTime = System.currentTimeMillis() - startTime
                delay(100L) // Update every 100ms
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(DesignSystem.Padding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OperatorHeader(subtitle = "Mobility Protocol", title = "Flexibility")

        Spacer(modifier = Modifier.weight(1f))

        // Timer Display
        val seconds = (elapsedTime / 1000) % 60
        val minutes = (elapsedTime / (1000 * 60)) % 60
        val hours = (elapsedTime / (1000 * 60 * 60))
        val timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds)

        Text(
            text = timeString,
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                fontFeatureSettings = "tnum"
            ),
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.weight(1f))

        if (!isRunning) {
            JuicyButton(
                text = if (elapsedTime > 0) "RESUME PROTOCOL" else "INITIATE PROTOCOL",
                onClick = { isRunning = true },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
             JuicyButton(
                text = "PAUSE",
                onClick = { isRunning = false },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        JuicyButton(
            text = "COMPLETE & SAVE",
            onClick = {
                isRunning = false
                onSave(elapsedTime)
            },
            enabled = !isRunning && elapsedTime > 0, // Only save when paused and has time
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        JuicyButton(
            text = "ABORT / RETURN",
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FlexibilityScreenPreview() {
    PersonalLevelingSystemTheme {
        FlexibilityContent(onSave = {}, onBackClick = {})
    }
}
