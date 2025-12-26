package com.example.personallevelingsystem.ui.compose.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.personallevelingsystem.model.Mission
import com.example.personallevelingsystem.ui.compose.components.JuicyButton
import com.example.personallevelingsystem.ui.compose.components.OperatorHeader
import com.example.personallevelingsystem.ui.compose.theme.PlacementSpring
import com.example.personallevelingsystem.ui.compose.theme.DesignSystem
import com.example.personallevelingsystem.ui.compose.theme.PersonalLevelingSystemTheme
import com.example.personallevelingsystem.viewmodel.MissionViewModel

@Composable
fun MissionsListScreen(
    viewModel: MissionViewModel,
    onBackClick: () -> Unit
) {
    val dailyMissions by viewModel.dailyMissions.observeAsState(initial = emptyList())
    val weeklyMissions by viewModel.weeklyMissions.observeAsState(initial = emptyList())

    MissionsListContent(
        dailyMissions = dailyMissions,
        weeklyMissions = weeklyMissions,
        onMissionCheck = { mission ->
            viewModel.completeMission(mission, 1)
        },
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MissionsListContent(
    dailyMissions: List<Mission>,
    weeklyMissions: List<Mission>,
    onMissionCheck: (Mission) -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(DesignSystem.Padding)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            item {
                OperatorHeader(subtitle = "Objectives", title = "Active Missions")
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (dailyMissions.isNotEmpty()) {
                stickyHeader {
                    MissionSectionHeader(title = "DAILY MISSIONS")
                }
                items(dailyMissions, key = { it.id }) { mission ->
                    MissionItem(
                        mission = mission,
                        onCheck = { onMissionCheck(mission) },
                        modifier = Modifier.animateItemPlacement(PlacementSpring)
                    )
                }
            }
            
            if (weeklyMissions.isNotEmpty()) {
                stickyHeader {
                    MissionSectionHeader(title = "WEEKLY MISSIONS")
                }
                items(weeklyMissions, key = { it.id }) { mission ->
                    MissionItem(
                        mission = mission,
                        onCheck = { onMissionCheck(mission) },
                        modifier = Modifier.animateItemPlacement(PlacementSpring)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        JuicyButton(
            onClick = onBackClick,
            text = "CLOSE"
        )
    }
}

@Composable
fun MissionSectionHeader(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(androidx.compose.ui.graphics.Color.Transparent) // Fully transparent for ambient effect
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun MissionItem(
    mission: Mission,
    onCheck: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = mission.description ?: "Unknown Mission",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Checkbox(
            checked = mission.isCompleted,
            onCheckedChange = { onCheck() },
            enabled = !mission.isCompleted,
            colors = CheckboxDefaults.colors(
                checkedColor = com.example.personallevelingsystem.ui.compose.theme.PrimaryAccent, // Neon Cyan
                uncheckedColor = com.example.personallevelingsystem.ui.compose.theme.PrimaryAccent.copy(alpha = 0.5f),
                checkmarkColor = com.example.personallevelingsystem.ui.compose.theme.SpaceBlack
            )
        )
    }
}
