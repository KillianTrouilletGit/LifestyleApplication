package com.example.personallevelingsystem.ui.compose.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.personallevelingsystem.R
import com.example.personallevelingsystem.ui.compose.components.ArcButton
import com.example.personallevelingsystem.ui.compose.components.ArcGhostButton
import com.example.personallevelingsystem.ui.compose.components.ArcTimerCard
import com.example.personallevelingsystem.ui.compose.theme.CrimsonRed
import com.example.personallevelingsystem.ui.compose.theme.DesignSystem
import com.example.personallevelingsystem.ui.compose.theme.PersonalLevelingSystemTheme
import com.example.personallevelingsystem.util.hapticConfirm
import com.example.personallevelingsystem.viewmodel.TrainingViewModel
import kotlinx.coroutines.delay

@Composable
fun FlexibilityScreen(
    viewModel: TrainingViewModel,
    onBackClick: () -> Unit
) {
    FlexibilityContent(
        onStartTimer = { title -> viewModel.startTimerService(title) },
        onStopTimer = { viewModel.stopTimerService() },
        onSave = { duration ->
            viewModel.saveFlexibilityTraining(duration)
            onBackClick()
        },
        onBackClick = onBackClick
    )
}

@Composable
fun FlexibilityContent(
    onStartTimer: (String) -> Unit,
    onStopTimer: () -> Unit,
    onSave: (Long) -> Unit,
    onBackClick: () -> Unit
) {
    val view = LocalView.current
    var isRunning by remember { mutableStateOf(false) }
    var startTime by remember { mutableLongStateOf(0L) }
    var elapsedTime by remember { mutableLongStateOf(0L) }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            startTime = System.currentTimeMillis() - elapsedTime
            while (isRunning) {
                elapsedTime = System.currentTimeMillis() - startTime
                delay(100L)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(DesignSystem.Padding)
    ) {
        Spacer(modifier = Modifier.weight(1f))

        ArcTimerCard(elapsedMs = elapsedTime, running = isRunning)

        Spacer(modifier = Modifier.weight(1f))

        ArcButton(
            text = stringResource(
                when {
                    isRunning -> R.string.common_pause
                    elapsedTime > 0L -> R.string.common_resume
                    else -> R.string.flex_start
                }
            ),
            icon = if (isRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
            showChevron = false,
            onClick = {
                view.hapticConfirm()
                if (isRunning) {
                    isRunning = false
                    onStopTimer()
                } else {
                    isRunning = true
                    onStartTimer("Flexibility Training")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        ArcButton(
            text = stringResource(R.string.common_complete_save),
            icon = Icons.Rounded.Check,
            showChevron = false,
            enabled = !isRunning && elapsedTime > 0L,
            onClick = {
                view.hapticConfirm()
                isRunning = false
                onStopTimer()
                onSave(elapsedTime)
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        ArcGhostButton(
            text = stringResource(R.string.common_abort),
            tint = CrimsonRed,
            onClick = {
                onStopTimer()
                onBackClick()
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0A10)
@Composable
fun FlexibilityScreenPreview() {
    PersonalLevelingSystemTheme {
        FlexibilityContent(
            onStartTimer = {},
            onStopTimer = {},
            onSave = {},
            onBackClick = {}
        )
    }
}
