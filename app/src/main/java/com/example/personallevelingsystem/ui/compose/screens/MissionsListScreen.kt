package com.example.personallevelingsystem.ui.compose.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personallevelingsystem.model.Mission
import com.example.personallevelingsystem.model.MissionCategory
import com.example.personallevelingsystem.model.MissionDifficulty
import com.example.personallevelingsystem.service.MissionProgress
import com.example.personallevelingsystem.ui.compose.components.JuicyButton
import com.example.personallevelingsystem.ui.compose.components.OperatorHeader
import com.example.personallevelingsystem.ui.compose.theme.AlertOrange
import com.example.personallevelingsystem.ui.compose.theme.CrimsonRed
import com.example.personallevelingsystem.ui.compose.theme.DarkBlood
import com.example.personallevelingsystem.ui.compose.theme.DesignSystem
import com.example.personallevelingsystem.ui.compose.theme.HologramText
import com.example.personallevelingsystem.ui.compose.theme.PlacementSpring
import com.example.personallevelingsystem.ui.compose.theme.PrimaryAccent
import com.example.personallevelingsystem.ui.compose.theme.PrimaryGradient
import com.example.personallevelingsystem.ui.compose.theme.RubyRed
import com.example.personallevelingsystem.ui.compose.theme.TelemetryGreen
import com.example.personallevelingsystem.viewmodel.MissionViewModel

@Composable
fun MissionsListScreen(
    viewModel: MissionViewModel,
    onBackClick: () -> Unit,
    onDeeplink: (String) -> Unit = {}
) {
    val dailyMissions by viewModel.dailyMissions.observeAsState(initial = emptyList())
    val weeklyMissions by viewModel.weeklyMissions.observeAsState(initial = emptyList())
    val progress by viewModel.missionProgress.observeAsState(initial = emptyMap())
    val streaks by viewModel.streaks.observeAsState(initial = emptyMap())
    val categoryXp by viewModel.categoryXp.observeAsState(initial = emptyMap())

    MissionsListContent(
        dailyMissions = dailyMissions,
        weeklyMissions = weeklyMissions,
        progress = progress,
        streaks = streaks,
        categoryXp = categoryXp,
        onMissionCheck = { mission ->
            viewModel.completeMission(mission, 1)
        },
        onDeeplink = onDeeplink,
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MissionsListContent(
    dailyMissions: List<Mission>,
    weeklyMissions: List<Mission>,
    progress: Map<String, MissionProgress>,
    streaks: Map<String, Int>,
    categoryXp: Map<MissionCategory, Int>,
    onMissionCheck: (Mission) -> Unit,
    onDeeplink: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val dailyDone = dailyMissions.count { it.isCompleted }
    val weeklyDone = weeklyMissions.count { it.isCompleted }
    var detailMission by remember { mutableStateOf<Mission?>(null) }

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

            // Specialization summary
            if (categoryXp.values.any { it > 0 }) {
                item {
                    SpecializationCard(categoryXp = categoryXp)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            if (dailyMissions.isNotEmpty()) {
                item {
                    MissionSectionHeader(
                        title = "DAILY OPS",
                        progress = "$dailyDone / ${dailyMissions.size}"
                    )
                }
                items(dailyMissions, key = { it.id }) { mission ->
                    MissionItem(
                        mission = mission,
                        progress = progress[mission.id],
                        streak = streaks[mission.id] ?: 0,
                        onCheck = { onMissionCheck(mission) },
                        onTap = { detailMission = mission },
                        modifier = Modifier.animateItemPlacement(PlacementSpring)
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }

            if (weeklyMissions.isNotEmpty()) {
                item {
                    MissionSectionHeader(
                        title = "WEEKLY OPS",
                        progress = "$weeklyDone / ${weeklyMissions.size}"
                    )
                }
                items(weeklyMissions, key = { it.id }) { mission ->
                    MissionItem(
                        mission = mission,
                        progress = progress[mission.id],
                        streak = streaks[mission.id] ?: 0,
                        onCheck = { onMissionCheck(mission) },
                        onTap = { detailMission = mission },
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

    val sheetState = rememberModalBottomSheetState()
    detailMission?.let { mission ->
        ModalBottomSheet(
            onDismissRequest = { detailMission = null },
            sheetState = sheetState,
            containerColor = Color(0xFF111111)
        ) {
            MissionDetailSheet(
                mission = mission,
                progress = progress[mission.id],
                streak = streaks[mission.id] ?: 0,
                onComplete = {
                    onMissionCheck(mission)
                    detailMission = null
                },
                onDeeplink = { route ->
                    detailMission = null
                    onDeeplink(route)
                }
            )
        }
    }
}

@Composable
fun MissionSectionHeader(title: String, progress: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = progress,
            style = MaterialTheme.typography.labelMedium,
            color = HologramText.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun MissionItem(
    mission: Mission,
    progress: MissionProgress?,
    streak: Int,
    onCheck: () -> Unit,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryColor = categoryColor(mission.category)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .border(
                width = 1.dp,
                brush = if (mission.isCompleted)
                    Brush.linearGradient(listOf(TelemetryGreen.copy(alpha = 0.5f), TelemetryGreen.copy(alpha = 0.15f)))
                else
                    Brush.linearGradient(listOf(categoryColor.copy(alpha = 0.55f), categoryColor.copy(alpha = 0.1f))),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onTap)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Category accent bar
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(48.dp)
                .background(
                    brush = Brush.verticalGradient(
                        listOf(categoryColor, categoryColor.copy(alpha = 0.3f))
                    ),
                    shape = RoundedCornerShape(2.dp)
                )
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = mission.title.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (mission.isCompleted) TelemetryGreen else HologramText,
                    modifier = Modifier.weight(1f)
                )
                if (streak >= 2) {
                    StreakPill(streak)
                    Spacer(modifier = Modifier.width(6.dp))
                }
                if (mission.difficulty != MissionDifficulty.NORMAL) {
                    DifficultyPill(mission.difficulty)
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = mission.description,
                style = MaterialTheme.typography.bodySmall,
                color = HologramText.copy(alpha = if (mission.isCompleted) 0.4f else 0.7f)
            )

            if (progress != null && !mission.isCompleted) {
                Spacer(modifier = Modifier.height(6.dp))
                ProgressBar(progress = progress, color = categoryColor)
            }

            Spacer(modifier = Modifier.height(4.dp))
            val multiplier = (1f + (streak / 7).coerceAtMost(5) * 0.10f)
            val displayXp = (mission.reward * multiplier).toInt()
            val multSuffix = if (multiplier > 1f) " ×${"%.1f".format(multiplier)}" else ""
            Text(
                text = "+$displayXp XP$multSuffix",
                style = MaterialTheme.typography.labelSmall,
                color = PrimaryAccent,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Custom Gradient Checkbox
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(
                    brush = if (mission.isCompleted) PrimaryGradient
                            else Brush.linearGradient(
                                listOf(Color.Transparent, Color.Transparent)
                            ),
                    shape = RoundedCornerShape(6.dp)
                )
                .border(
                    width = 2.dp,
                    brush = if (mission.isCompleted) Brush.linearGradient(
                                listOf(Color.Transparent, Color.Transparent)
                            )
                            else PrimaryGradient,
                    shape = RoundedCornerShape(6.dp)
                )
                .clickable(enabled = !mission.isCompleted) { onCheck() },
            contentAlignment = Alignment.Center
        ) {
            if (mission.isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun ProgressBar(progress: MissionProgress, color: Color) {
    Column(modifier = Modifier.fillMaxWidth()) {
        LinearProgressIndicator(
            progress = progress.ratio,
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.15f)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = formatProgress(progress),
            style = MaterialTheme.typography.labelSmall,
            color = HologramText.copy(alpha = 0.55f)
        )
    }
}

private fun formatProgress(p: MissionProgress): String {
    val current = if (p.current >= 100f) p.current.toInt().toString() else "%.1f".format(p.current)
    val target = if (p.target >= 100f) p.target.toInt().toString() else "%.1f".format(p.target)
    return if (p.unit.isEmpty()) "$current / $target" else "$current / $target ${p.unit}"
}

@Composable
private fun DifficultyPill(difficulty: MissionDifficulty) {
    val (label, color) = when (difficulty) {
        MissionDifficulty.HARD -> "HARD" to AlertOrange
        MissionDifficulty.ELITE -> "ELITE" to CrimsonRed
        MissionDifficulty.NORMAL -> "" to PrimaryAccent
    }
    if (label.isEmpty()) return
    Box(
        modifier = Modifier
            .border(1.dp, color.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun StreakPill(streak: Int) {
    Box(
        modifier = Modifier
            .background(AlertOrange.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
            .border(1.dp, AlertOrange.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
            .padding(horizontal = 5.dp, vertical = 1.dp)
    ) {
        Text(
            text = "🔥$streak",
            color = AlertOrange,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun SpecializationCard(categoryXp: Map<MissionCategory, Int>) {
    val total = categoryXp.values.sum().coerceAtLeast(1)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                Brush.linearGradient(listOf(CrimsonRed.copy(alpha = 0.5f), DarkBlood.copy(alpha = 0.4f))),
                RoundedCornerShape(10.dp)
            )
            .padding(12.dp)
    ) {
        Text(
            text = "SPECIALIZATION · $total XP TOTAL",
            color = PrimaryAccent,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        MissionCategory.values().forEach { cat ->
            val xp = categoryXp[cat] ?: 0
            val ratio = xp.toFloat() / total
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(categoryColor(cat), RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = cat.name,
                    color = HologramText.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.width(90.dp)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(2.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(ratio)
                            .height(4.dp)
                            .background(
                                Brush.horizontalGradient(listOf(categoryColor(cat), categoryColor(cat).copy(alpha = 0.4f))),
                                RoundedCornerShape(2.dp)
                            )
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$xp",
                    color = HologramText.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun MissionDetailSheet(
    mission: Mission,
    progress: MissionProgress?,
    streak: Int,
    onComplete: () -> Unit,
    onDeeplink: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = mission.title.uppercase(),
            color = PrimaryAccent,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = mission.description,
            color = HologramText.copy(alpha = 0.85f),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatChip("CATEGORY", mission.category.name)
            if (streak >= 1) StatChip("STREAK", "🔥 $streak")
            StatChip("XP", "+${mission.reward}")
        }

        if (progress != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "PROGRESS",
                color = PrimaryAccent,
                style = MaterialTheme.typography.labelMedium,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            ProgressBar(progress = progress, color = categoryColor(mission.category))
        }

        mission.tip?.let { tip ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "TIP",
                color = PrimaryAccent,
                style = MaterialTheme.typography.labelMedium,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = tip,
                color = HologramText.copy(alpha = 0.75f),
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        val route = mission.deeplinkRoute
        if (route != null) {
            JuicyButton(
                text = "OPEN ${route.uppercase()}",
                onClick = { onDeeplink(route) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        if (!mission.isCompleted) {
            JuicyButton(
                text = "MARK COMPLETE",
                onClick = onComplete,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun StatChip(label: String, value: String) {
    Box(
        modifier = Modifier
            .border(1.dp, RubyRed.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = label,
                color = PrimaryAccent,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                letterSpacing = 1.sp
            )
            Text(
                text = value,
                color = HologramText,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun categoryColor(category: MissionCategory): Color = when (category) {
    MissionCategory.BODY -> CrimsonRed
    MissionCategory.MIND -> Color(0xFFB388FF)        // soft violet for mind
    MissionCategory.NUTRITION -> TelemetryGreen
    MissionCategory.RECOVERY -> Color(0xFF40C4FF)    // calm cyan for recovery
    MissionCategory.DISCIPLINE -> AlertOrange
    MissionCategory.PROGRESS -> Color(0xFFE6E6E6)
}
