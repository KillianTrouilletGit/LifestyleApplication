package com.example.personallevelingsystem.ui.compose.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.personallevelingsystem.ui.compose.components.JuicyButton
import com.example.personallevelingsystem.ui.compose.components.OperatorHeader
import com.example.personallevelingsystem.ui.compose.theme.DesignSystem
import com.example.personallevelingsystem.ui.compose.theme.PersonalLevelingSystemTheme

@Composable
fun TrainingScreen(
    onCreateProgramClick: () -> Unit,
    onViewProgramsClick: () -> Unit,
    onStartProgramClick: () -> Unit,
    onStartFlexibilityClick: () -> Unit,
    onStartEnduranceClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(DesignSystem.Padding)
            .verticalScroll(rememberScrollState())
    ) {
        OperatorHeader(subtitle = "Protocol", title = "Training Module")
        
        Spacer(modifier = Modifier.height(24.dp))
        
        JuicyButton(
            onClick = onCreateProgramClick,
            text = "CREATE NEW PROGRAM",
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        JuicyButton(
            onClick = onViewProgramsClick,
            text = "VIEW EXISTING PROGRAMS",
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        JuicyButton(
            onClick = onStartProgramClick,
            text = "START PROGRAM TRAINING",
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        JuicyButton(
            onClick = onStartFlexibilityClick,
            text = "START FLEXIBILITY TRAINING",
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        JuicyButton(
            onClick = onStartEnduranceClick,
            text = "START ENDURANCE TRAINING",
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        JuicyButton(
            onClick = onBackClick,
            text = "RETURN TO MAIN",
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TrainingScreenPreview() {
    PersonalLevelingSystemTheme {
        TrainingScreen({}, {}, {}, {}, {}, {})
    }
}
