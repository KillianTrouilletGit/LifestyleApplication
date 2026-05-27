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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personallevelingsystem.ui.compose.theme.CrimsonRed
import com.example.personallevelingsystem.ui.compose.theme.RubyRed
import com.example.personallevelingsystem.ui.compose.theme.PrimaryAccent
import com.example.personallevelingsystem.ui.compose.theme.PrimaryGradient
import com.example.personallevelingsystem.ui.compose.theme.DesignSystem
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.graphicsLayer

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
                val color = if (pagerState.currentPage == iteration) CrimsonRed else Color.DarkGray
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
    var startAnimation by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { startAnimation = true }

    val animatedLevel by animateIntAsState(targetValue = if (startAnimation) state.level else 0, animationSpec = tween(1000), label = "level")
    val animatedXp by animateFloatAsState(targetValue = if (startAnimation) state.currentXp else 0f, animationSpec = tween(1000), label = "xp")
    
    val progress = if (state.requiredXp > 0) animatedXp / state.requiredXp else 0f
    
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("CURRENT STATUS", style = MaterialTheme.typography.labelMedium, color = PrimaryAccent)
        Text(
            text = "LEVEL $animatedLevel", 
            style = MaterialTheme.typography.displayMedium, 
            modifier = Modifier
                .graphicsLayer(alpha = 0.99f)
                .drawWithCache {
                    onDrawWithContent {
                        drawContent()
                        drawRect(PrimaryGradient, blendMode = BlendMode.SrcIn)
                    }
                }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("XP: ${animatedXp.toInt()} / ${state.requiredXp.toInt()}", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
        
        // Gradient Progress Bar
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(8.dp) // slightly taller for gradient visibility
                .padding(top = 8.dp)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                .background(Color.DarkGray.copy(alpha = 0.5f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .background(PrimaryGradient)
            )
        }
    }
}

@Composable
fun MissionStatsCard(state: com.example.personallevelingsystem.viewmodel.PerformanceState) {
    var startAnimation by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { startAnimation = true }

    val efficiencyPercent = (state.missionEfficiency * 100).toInt()
    val animatedEfficiency by animateIntAsState(targetValue = if (startAnimation) efficiencyPercent else 0, animationSpec = tween(1000), label = "efficiency")
    
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("MISSION EFFICIENCY", style = MaterialTheme.typography.labelMedium, color = PrimaryAccent)
        Text(
            text = "$animatedEfficiency%", 
            style = MaterialTheme.typography.displayMedium, 
            modifier = Modifier
                .graphicsLayer(alpha = 0.99f)
                .drawWithCache {
                    onDrawWithContent {
                        drawContent()
                        drawRect(PrimaryGradient, blendMode = BlendMode.SrcIn)
                    }
                }
        )
        Text("Daily Objectives: ${state.dailyMissionsCompleted}/${state.totalDailyMissions}", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
    }
}

@Composable
fun TrainingFrequencyCard(state: com.example.personallevelingsystem.viewmodel.PerformanceState) {
    val activeDays = state.weeklyTrainingFrequency.count { it > 0.1f }

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
                val dayLabel = state.weeklyTrainingLabels.getOrElse(index) { "-" }
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
                            color = CrimsonRed
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
    var startAnimation by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { startAnimation = true }

    val animatedSleep by animateFloatAsState(targetValue = if (startAnimation) state.sleepHours else 0f, animationSpec = tween(1000), label = "sleep")
    val animatedWater by animateFloatAsState(targetValue = if (startAnimation) state.waterIntake else 0f, animationSpec = tween(1000), label = "water")
    val animatedKcal by animateIntAsState(targetValue = if (startAnimation) state.dailyCalories else 0, animationSpec = tween(1000), label = "kcal")
    val animatedBal by animateFloatAsState(targetValue = if (startAnimation) state.dailyBalanceIndex else 0f, animationSpec = tween(1000), label = "bal")

     Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(DesignSystem.Padding)
    ) {
        Text("BIO-METRICS", style = MaterialTheme.typography.labelMedium, color = PrimaryAccent)
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            BioMetricItem(label = "SLEEP", value = "${String.format("%.1f", animatedSleep)}H", color = RubyRed)
            BioMetricItem(label = "H2O", value = "${String.format("%.1f", animatedWater)}L", color = CrimsonRed)
            BioMetricItem(label = "KCAL", value = "${animatedKcal}", color = Color.White)
            BioMetricItem(label = "BAL", value = String.format("%.2f", animatedBal), color = CrimsonRed)
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
