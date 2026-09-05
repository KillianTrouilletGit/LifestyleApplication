package com.example.personallevelingsystem.ui.compose.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.personallevelingsystem.model.Mission
import com.example.personallevelingsystem.model.MissionCategory
import com.example.personallevelingsystem.model.MissionDifficulty
import com.example.personallevelingsystem.service.MissionAutoCompleter
import com.example.personallevelingsystem.service.MissionProgress
import com.example.personallevelingsystem.ui.compose.components.ArcButton
import com.example.personallevelingsystem.ui.compose.components.ArcCard
import com.example.personallevelingsystem.ui.compose.components.ArcCheckBox
import com.example.personallevelingsystem.ui.compose.components.ArcGhostButton
import com.example.personallevelingsystem.ui.compose.components.ArcProgressBar
import com.example.personallevelingsystem.ui.compose.components.ArcSegmentedTabs
import com.example.personallevelingsystem.ui.compose.components.categoryColor
import com.example.personallevelingsystem.ui.compose.theme.AccentViolet
import com.example.personallevelingsystem.ui.compose.theme.AlertOrange
import com.example.personallevelingsystem.ui.compose.theme.BorderSubtle
import com.example.personallevelingsystem.ui.compose.theme.CrimsonRed
import com.example.personallevelingsystem.ui.compose.theme.DeepViolet
import com.example.personallevelingsystem.ui.compose.theme.DesignSystem
import com.example.personallevelingsystem.ui.compose.theme.HologramText
import com.example.personallevelingsystem.ui.compose.theme.Motion
import com.example.personallevelingsystem.ui.compose.theme.PlacementSpring
import com.example.personallevelingsystem.ui.compose.theme.PrimaryGradient
import com.example.personallevelingsystem.ui.compose.theme.SurfaceElevated
import com.example.personallevelingsystem.ui.compose.theme.TelemetryGreen
import com.example.personallevelingsystem.ui.compose.theme.TextSecondary
import com.example.personallevelingsystem.ui.compose.theme.TextTertiary
import com.example.personallevelingsystem.ui.compose.theme.tabular
import com.example.personallevelingsystem.util.hapticReward
import com.example.personallevelingsystem.viewmodel.MissionViewModel
import kotlinx.coroutines.delay

private val CardShape = RoundedCornerShape(DesignSystem.Radius.card)

@Composable
fun MissionsListScreen(
    viewModel: MissionViewModel,
    onDeeplink: (String) -> Unit = {}
) {
    val dailyMissions by viewModel.dailyMissions.observeAsState(initial = emptyList())
    val weeklyMissions by viewModel.weeklyMissions.observeAsState(initial = emptyList())
    val progress by viewModel.missionProgress.observeAsState(initial = emptyMap())
    val streaks by viewModel.streaks.observeAsState(initial = emptyMap())
    val categoryXp by viewModel.categoryXp.observeAsState(initial = emptyMap())
    val celebration by viewModel.streakCelebration.observeAsState(initial = null)

    Box(modifier = Modifier.fillMaxSize()) {
        MissionsListContent(
            dailyMissions = dailyMissions,
            weeklyMissions = weeklyMissions,
            progress = progress,
            streaks = streaks,
            categoryXp = categoryXp,
            onMissionCheck = { mission -> viewModel.completeMission(mission) },
            onDeeplink = onDeeplink
        )
        StreakCelebrationOverlay(
            streak = celebration,
            onFinished = { viewModel.clearStreakCelebration() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissionsListContent(
    dailyMissions: List<Mission>,
    weeklyMissions: List<Mission>,
    progress: Map<String, MissionProgress>,
    streaks: Map<String, Int>,
    categoryXp: Map<MissionCategory, Int>,
    onMissionCheck: (Mission) -> Unit,
    onDeeplink: (String) -> Unit
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var detailMission by remember { mutableStateOf<Mission?>(null) }
    val dailyDone = dailyMissions.count { it.isCompleted }
    val weeklyDone = weeklyMissions.count { it.isCompleted }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = DesignSystem.Padding)
    ) {
        ArcSegmentedTabs(
            options = listOf(
                "Daily · $dailyDone/${dailyMissions.size}",
                "Weekly · $weeklyDone/${weeklyMissions.size}"
            ),
            selected = tab,
            onSelect = { tab = it },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        AnimatedContent(
            targetState = tab,
            transitionSpec = {
                val direction = if (targetState > initialState) 1 else -1
                (fadeIn(tween(Motion.Standard)) +
                    slideInHorizontally(tween(Motion.Standard)) { direction * it / 10 })
                    .togetherWith(fadeOut(tween(Motion.Quick)))
            },
            label = "missionTabs"
        ) { page ->
            MissionList(
                missions = if (page == 0) dailyMissions else weeklyMissions,
                progress = progress,
                streaks = streaks,
                categoryXp = if (page == 0) categoryXp else emptyMap(),
                onMissionCheck = onMissionCheck,
                onTap = { detailMission = it }
            )
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    detailMission?.let { mission ->
        ModalBottomSheet(
            onDismissRequest = { detailMission = null },
            sheetState = sheetState,
            containerColor = SurfaceElevated
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
private fun MissionList(
    missions: List<Mission>,
    progress: Map<String, MissionProgress>,
    streaks: Map<String, Int>,
    categoryXp: Map<MissionCategory, Int>,
    onMissionCheck: (Mission) -> Unit,
    onTap: (Mission) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        if (categoryXp.values.any { it > 0 }) {
            item(key = "specialization") { SpecializationCard(categoryXp = categoryXp) }
        }
        if (missions.isEmpty()) {
            item(key = "empty") {
                ArcCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Nothing scheduled here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }
        items(missions, key = { it.id }) { mission ->
            MissionCard(
                mission = mission,
                progress = progress[mission.id],
                streak = streaks[mission.id] ?: 0,
                onCheck = { onMissionCheck(mission) },
                onTap = { onTap(mission) },
                modifier = Modifier.animateItem(placementSpec = PlacementSpring)
            )
        }
    }
}

/** Swipe right to complete; the card snaps back and re-renders as done instead of dismissing. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MissionCard(
    mission: Mission,
    progress: MissionProgress?,
    streak: Int,
    onCheck: () -> Unit,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val color = categoryColor(mission.category)
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.StartToEnd && !mission.isCompleted) {
                view.hapticReward()
                onCheck()
            }
            false
        },
        positionalThreshold = { distance -> distance * 0.35f }
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = !mission.isCompleted,
        enableDismissFromEndToStart = false,
        backgroundContent = {
            // Only while dragging: the card surface is slightly translucent and would
            // otherwise ghost the label through.
            if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CardShape)
                    .background(Brush.horizontalGradient(listOf(CrimsonRed, DeepViolet))),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.padding(start = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Complete",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    ) {
        ArcCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = onTap,
            accent = if (mission.isCompleted) TelemetryGreen else color,
            contentPadding = PaddingValues(start = 16.dp, end = 12.dp, top = 12.dp, bottom = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = mission.title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = if (mission.isCompleted) TelemetryGreen else HologramText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (streak >= 2) {
                            Spacer(modifier = Modifier.width(6.dp))
                            StreakPill(streak)
                        }
                        if (mission.difficulty != MissionDifficulty.NORMAL) {
                            Spacer(modifier = Modifier.width(6.dp))
                            DifficultyPill(mission.difficulty)
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = mission.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary.copy(alpha = if (mission.isCompleted) 0.6f else 1f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (progress != null && !mission.isCompleted) {
                        Spacer(modifier = Modifier.height(8.dp))
                        ArcProgressBar(progress = progress.ratio, color = color, height = 4.dp)
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = formatProgress(progress),
                            style = MaterialTheme.typography.labelSmall.tabular,
                            color = TextTertiary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    val multiplier = MissionAutoCompleter.streakMultiplier(streak)
                    val displayXp = (mission.reward * multiplier).toInt()
                    val multSuffix = if (multiplier > 1f) " ×${"%.1f".format(multiplier)}" else ""
                    Text(
                        text = "+$displayXp XP$multSuffix",
                        style = MaterialTheme.typography.labelMedium.tabular,
                        color = CrimsonRed,
                        letterSpacing = 0.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                ArcCheckBox(
                    checked = mission.isCompleted,
                    enabled = !mission.isCompleted,
                    onToggle = {
                        view.hapticReward()
                        onCheck()
                    }
                )
            }
        }
    }
}

private fun formatProgress(p: MissionProgress): String {
    fun fmt(v: Float): String =
        if (v % 1f == 0f || v >= 100f) v.toInt().toString() else "%.1f".format(v)
    val current = fmt(p.current)
    val target = fmt(p.target)
    return if (p.unit.isEmpty()) "$current / $target" else "$current / $target ${p.unit}"
}

@Composable
private fun DifficultyPill(difficulty: MissionDifficulty) {
    val (label, color) = when (difficulty) {
        MissionDifficulty.HARD -> "HARD" to AlertOrange
        MissionDifficulty.ELITE -> "ELITE" to CrimsonRed
        MissionDifficulty.NORMAL -> return
    }
    Box(
        modifier = Modifier
            .border(1.dp, color.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
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
            .background(AlertOrange.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
            .border(1.dp, AlertOrange.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
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
    ArcCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "SPECIALIZATION",
                style = MaterialTheme.typography.labelMedium,
                color = AccentViolet,
                letterSpacing = 1.5.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "%,d XP".format(total),
                style = MaterialTheme.typography.labelMedium.tabular,
                color = HologramText
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        MissionCategory.entries.forEach { category ->
            val xp = categoryXp[category] ?: 0
            val tint = categoryColor(category)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(
                    modifier = Modifier
                        .size(8.dp)
                        .background(tint, RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = category.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    modifier = Modifier.width(80.dp)
                )
                ArcProgressBar(
                    progress = xp.toFloat() / total,
                    color = tint,
                    trackColor = Color.White.copy(alpha = 0.06f),
                    height = 4.dp,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$xp",
                    style = MaterialTheme.typography.labelSmall.tabular,
                    color = HologramText,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(36.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
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
    // WindowInsets report zero inside the sheet's dialog window, so read the real
    // navigation-bar inset from the root view to keep the buttons clear of it.
    val view = LocalView.current
    val density = LocalDensity.current
    val bottomInset = remember(view) {
        val raw = ViewCompat.getRootWindowInsets(view)
            ?.getInsets(WindowInsetsCompat.Type.navigationBars())?.bottom ?: 0
        with(density) { raw.toDp() }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = mission.title,
            color = HologramText,
            style = MaterialTheme.typography.titleLarge
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
                color = AccentViolet,
                style = MaterialTheme.typography.labelMedium,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            ArcProgressBar(progress = progress.ratio, color = categoryColor(mission.category))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatProgress(progress),
                style = MaterialTheme.typography.labelSmall.tabular,
                color = TextSecondary
            )
        }

        mission.tip?.let { tip ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "TIP",
                color = AccentViolet,
                style = MaterialTheme.typography.labelMedium,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = tip,
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        val route = mission.deeplinkRoute
        if (!mission.isCompleted) {
            ArcButton(
                text = "Mark complete",
                icon = Icons.Rounded.Check,
                showChevron = false,
                onClick = {
                    view.hapticReward()
                    onComplete()
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        if (route != null) {
            ArcGhostButton(
                text = "Open ${route.replace('_', ' ').replaceFirstChar { it.uppercase() }}",
                onClick = { onDeeplink(route) },
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(modifier = Modifier.height(8.dp + bottomInset.coerceAtLeast(16.dp)))
    }
}

@Composable
private fun StatChip(label: String, value: String) {
    Box(
        modifier = Modifier
            .background(Color(0x0DFFFFFF), RoundedCornerShape(8.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = label,
                color = AccentViolet,
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

/**
 * Celebration shown when a completed mission keeps its streak alive. Springs in over
 * the list, holds briefly, fades out, then clears via [onFinished]. Never intercepts touches.
 */
@Composable
private fun StreakCelebrationOverlay(streak: Int?, onFinished: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    var shownStreak by remember { mutableIntStateOf(0) }
    val view = LocalView.current

    LaunchedEffect(streak) {
        if (streak != null) {
            shownStreak = streak
            visible = true
            view.hapticReward()
            delay(1600)
            visible = false
            delay(300)
            onFinished()
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AnimatedVisibility(
            visible = visible,
            enter = scaleIn(initialScale = 0.4f, animationSpec = Motion.Playful) + fadeIn(tween(150)),
            exit = fadeOut(tween(250))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .background(SurfaceElevated, RoundedCornerShape(24.dp))
                    .border(
                        1.dp,
                        Brush.linearGradient(
                            listOf(AlertOrange.copy(alpha = 0.7f), CrimsonRed.copy(alpha = 0.35f))
                        ),
                        RoundedCornerShape(24.dp)
                    )
                    .padding(horizontal = 36.dp, vertical = 28.dp)
            ) {
                Text(text = "🔥", fontSize = 54.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$shownStreak",
                    style = MaterialTheme.typography.displayMedium.tabular,
                    color = AlertOrange
                )
                Text(
                    text = "DAY STREAK",
                    style = MaterialTheme.typography.labelMedium,
                    color = AccentViolet,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}
