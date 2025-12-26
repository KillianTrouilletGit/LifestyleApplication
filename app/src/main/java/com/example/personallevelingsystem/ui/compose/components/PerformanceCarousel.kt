package com.example.personallevelingsystem.ui.compose.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personallevelingsystem.ui.compose.theme.NeonCyan
import com.example.personallevelingsystem.ui.compose.theme.NeonMagenta
import com.example.personallevelingsystem.ui.compose.theme.PrimaryAccent
import com.example.personallevelingsystem.ui.compose.theme.PrimaryGradient
import com.example.personallevelingsystem.ui.compose.theme.DesignSystem

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PerformanceCarousel(
    state: com.example.personallevelingsystem.viewmodel.PerformanceState
) {
    val pagerState = rememberPagerState(pageCount = { 4 })

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp) // Increased height from 140dp
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 8.dp
        ) { page ->
             JuicyCard(
                 onClick = {},
                 modifier = Modifier.fillMaxSize()
             ) {
                 when(page) {
                     0 -> LevelProgressCard(state)
                     1 -> MissionStatsCard(state)
                     2 -> TrainingFrequencyCard(state)
                     3 -> HealthOverviewCard(state)
                 }
             }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Indicators
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pagerState.pageCount) { iteration ->
                val color = if (pagerState.currentPage == iteration) NeonCyan else Color.DarkGray
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(6.dp)
                )
            }
        }
    }
}

@Composable
fun LevelProgressCard(state: com.example.personallevelingsystem.viewmodel.PerformanceState) {
    val progress = if (state.requiredXp > 0) state.currentXp / state.requiredXp else 0f
    
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("CURRENT STATUS", style = MaterialTheme.typography.labelMedium, color = PrimaryAccent)
        Text("LEVEL ${state.level}", style = MaterialTheme.typography.displayMedium, color = NeonMagenta)
        Spacer(modifier = Modifier.height(8.dp))
        Text("XP: ${state.currentXp.toInt()} / ${state.requiredXp.toInt()}", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(0.8f).padding(top=4.dp),
            color = NeonCyan,
            trackColor = Color.DarkGray,
        )
    }
}

@Composable
fun MissionStatsCard(state: com.example.personallevelingsystem.viewmodel.PerformanceState) {
    val efficiencyPercent = (state.missionEfficiency * 100).toInt()
    
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("MISSION EFFICIENCY", style = MaterialTheme.typography.labelMedium, color = PrimaryAccent)
        Text("$efficiencyPercent%", style = MaterialTheme.typography.displayMedium, color = NeonCyan)
        Text("Daily Objectives: ${state.dailyMissionsCompleted}/${state.totalDailyMissions}", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
    }
}

@Composable
fun TrainingFrequencyCard(state: com.example.personallevelingsystem.viewmodel.PerformanceState) {
    val activeDays = state.weeklyTrainingFrequency.count { it > 0.1f }
    val days = listOf("M", "T", "W", "T", "F", "S", "S") // Static labels for now

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("WEEKLY VOLUME", style = MaterialTheme.typography.labelMedium, color = PrimaryAccent)
            Text("$activeDays/7 ACT", style = MaterialTheme.typography.labelSmall, color = Color.White)
        }
        
        Spacer(modifier = Modifier.height(16.dp)) // More space

        val maxVolume = state.weeklyTrainingFrequency.maxOrNull()?.takeIf { it > 0 } ?: 1f

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.fillMaxWidth().height(140.dp) // Increased from 120dp
        ) {
            state.weeklyTrainingFrequency.forEachIndexed { index, rawValue ->
                val dayLabel = days.getOrElse(index) { "-" }
                val heightRatio = (rawValue / maxVolume).coerceIn(0.05f, 1f)
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.wrapContentHeight()
                ) {
                    // Value Label
                    if (rawValue > 0.05f) {
                        Text(
                            text = String.format("%.1f", rawValue),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            color = NeonCyan
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))

                    // Bar
                    Box(modifier = Modifier
                        .width(16.dp)
                        .height(80.dp * heightRatio) // Reduced from 100dp to leave more room
                        .background(
                            brush = if(rawValue > 0.05f) PrimaryGradient else androidx.compose.ui.graphics.SolidColor(Color.DarkGray.copy(alpha=0.3f))
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Text(
                        text = dayLabel, 
                        style = MaterialTheme.typography.labelSmall, 
                        fontSize = 12.sp,
                        color = Color.LightGray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun HealthOverviewCard(state: com.example.personallevelingsystem.viewmodel.PerformanceState) {
     Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(DesignSystem.Padding)
    ) {
        Text("BIO-METRICS", style = MaterialTheme.typography.labelMedium, color = PrimaryAccent)
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            BioMetricItem(label = "SLEEP", value = "${String.format("%.1f", state.sleepHours)}H", color = NeonMagenta)
            BioMetricItem(label = "H2O", value = "${String.format("%.1f", state.waterIntake)}L", color = NeonCyan)
            BioMetricItem(label = "KCAL", value = "${state.dailyCalories}", color = Color.White)
            BioMetricItem(label = "BAL", value = String.format("%.2f", state.dailyBalanceIndex), color = com.example.personallevelingsystem.ui.compose.theme.CyberCyan)
        }
    }
}

@Composable
fun BioMetricItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 10.sp)
        Text(text = value, style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.Bold)
    }
}
