package com.example.personallevelingsystem.ui.compose.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.automirrored.rounded.ListAlt
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.personallevelingsystem.ui.compose.components.ArcButton
import com.example.personallevelingsystem.ui.compose.components.ArcListRow
import com.example.personallevelingsystem.ui.compose.components.ArcSectionLabel
import com.example.personallevelingsystem.ui.compose.theme.DesignSystem
import com.example.personallevelingsystem.ui.compose.theme.PersonalLevelingSystemTheme
import com.example.personallevelingsystem.ui.compose.theme.SignalCyan

@Composable
fun TrainingScreen(
    onCreateProgramClick: () -> Unit,
    onViewProgramsClick: () -> Unit,
    onStartProgramClick: () -> Unit,
    onStartFlexibilityClick: () -> Unit,
    onStartEnduranceClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(DesignSystem.Padding)
    ) {
        ArcButton(
            text = "Start a session",
            subtitle = "Pick a program session and go",
            icon = Icons.Rounded.PlayArrow,
            onClick = onStartProgramClick,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        ArcSectionLabel(text = "Programs")
        ArcListRow(
            title = "Program archive",
            subtitle = "Browse sessions, inspect exercise history",
            icon = Icons.AutoMirrored.Rounded.ListAlt,
            onClick = onViewProgramsClick
        )
        Spacer(modifier = Modifier.height(10.dp))
        ArcListRow(
            title = "Create a program",
            subtitle = "Sessions, exercises and set counts",
            icon = Icons.Rounded.Add,
            onClick = onCreateProgramClick
        )

        Spacer(modifier = Modifier.height(24.dp))

        ArcSectionLabel(text = "Conditioning")
        ArcListRow(
            title = "Flexibility",
            subtitle = "Timed mobility block",
            icon = Icons.Rounded.SelfImprovement,
            tint = SignalCyan,
            onClick = onStartFlexibilityClick
        )
        Spacer(modifier = Modifier.height(10.dp))
        ArcListRow(
            title = "Endurance",
            subtitle = "Timed run with distance",
            icon = Icons.AutoMirrored.Rounded.DirectionsRun,
            tint = SignalCyan,
            onClick = onStartEnduranceClick
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TrainingScreenPreview() {
    PersonalLevelingSystemTheme {
        TrainingScreen({}, {}, {}, {}, {})
    }
}
