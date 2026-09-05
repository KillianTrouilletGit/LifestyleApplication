package com.example.personallevelingsystem.ui.compose.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import com.example.personallevelingsystem.model.Mission
import com.example.personallevelingsystem.ui.compose.components.HeroCard
import com.example.personallevelingsystem.ui.compose.components.QuickLogCard
import com.example.personallevelingsystem.ui.compose.components.TopMissionsCard
import com.example.personallevelingsystem.ui.compose.components.WeeklyTrainingCard
import com.example.personallevelingsystem.ui.compose.theme.DesignSystem
import com.example.personallevelingsystem.ui.compose.theme.Motion
import com.example.personallevelingsystem.ui.compose.theme.PersonalLevelingSystemTheme
import com.example.personallevelingsystem.util.hapticConfirm
import com.example.personallevelingsystem.viewmodel.HealthViewModel
import com.example.personallevelingsystem.viewmodel.MissionViewModel
import com.example.personallevelingsystem.viewmodel.PerformanceState
import com.example.personallevelingsystem.viewmodel.PerformanceViewModel

@Composable
fun MainScreen(
    onNavigate: (String) -> Unit,
    performanceViewModel: PerformanceViewModel,
    healthViewModel: HealthViewModel,
    missionViewModel: MissionViewModel? = null
) {
    val performance by performanceViewModel.uiState.collectAsState()
    val dailyMissions by (missionViewModel?.dailyMissions ?: MutableLiveData(emptyList<Mission>()))
        .observeAsState(emptyList())
    val streaks by (missionViewModel?.streaks ?: MutableLiveData(emptyMap<String, Int>()))
        .observeAsState(emptyMap())
    val waterMl by healthViewModel.totalWaterToday.observeAsState(initial = 0f)
    val waterTargetMl by healthViewModel.waterTargetMl.observeAsState(initial = 2500f)
    val view = LocalView.current

    LaunchedEffect(Unit) {
        performanceViewModel.loadPerformanceData()
        missionViewModel?.refresh()
        healthViewModel.calculateTotalWaterForToday()
    }

    DashboardContent(
        performance = performance,
        dailyMissions = dailyMissions,
        bestStreak = streaks.values.maxOrNull() ?: 0,
        waterMl = waterMl,
        waterTargetMl = waterTargetMl,
        onNavigate = onNavigate,
        onQuickWater = {
            healthViewModel.saveWater(250f)
            view.hapticConfirm()
        }
    )
}

@Composable
fun DashboardContent(
    performance: PerformanceState,
    dailyMissions: List<Mission>,
    bestStreak: Int,
    waterMl: Float,
    waterTargetMl: Float,
    onNavigate: (String) -> Unit,
    onQuickWater: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(DesignSystem.Padding),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Reveal(visible, 0) {
            HeroCard(
                level = performance.level,
                currentXp = performance.currentXp.toInt(),
                requiredXp = performance.requiredXp.toInt(),
                bestStreak = bestStreak,
                activeDays = performance.weeklyTrainingFrequency.count { it > 0.05f }
            )
        }
        Reveal(visible, 1) {
            WeeklyTrainingCard(
                hoursPerDay = performance.weeklyTrainingFrequency,
                labels = performance.weeklyTrainingLabels,
                onClick = { onNavigate("training") }
            )
        }
        if (dailyMissions.isNotEmpty()) {
            Reveal(visible, 2) {
                TopMissionsCard(
                    incompleteMissions = dailyMissions.filter { !it.isCompleted },
                    totalDaily = dailyMissions.size,
                    onOpenMissions = { onNavigate("missions") }
                )
            }
        }
        Reveal(visible, 3) {
            QuickLogCard(
                waterMl = waterMl,
                waterTargetMl = waterTargetMl,
                sleepHours = performance.sleepHours,
                calories = performance.dailyCalories,
                onQuickWater = onQuickWater,
                onLogSleep = { onNavigate("sleep") },
                onLogMeal = { onNavigate("nutrition") }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun Reveal(visible: Boolean, index: Int, content: @Composable () -> Unit) {
    val delay = index * 60
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(Motion.Standard, delayMillis = delay)) +
            slideInVertically(tween(Motion.Standard, delayMillis = delay)) { it / 8 }
    ) {
        content()
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0A10)
@Composable
fun MainScreenPreview() {
    PersonalLevelingSystemTheme {
        DashboardContent(
            performance = PerformanceState(
                level = 23,
                currentXp = 19515f,
                requiredXp = 52900f,
                weeklyTrainingFrequency = listOf(1.2f, 0f, 0.8f, 1.5f, 0f, 0f, 0.6f),
                weeklyTrainingLabels = listOf("M", "T", "W", "T", "F", "S", "S"),
                sleepHours = 7.5f,
                dailyCalories = 1850
            ),
            dailyMissions = emptyList(),
            bestStreak = 9,
            waterMl = 1250f,
            waterTargetMl = 2500f,
            onNavigate = {},
            onQuickWater = {}
        )
    }
}
