package com.example.personallevelingsystem.ui.compose.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personallevelingsystem.model.Exercise
import com.example.personallevelingsystem.ui.compose.components.ArcButton
import com.example.personallevelingsystem.ui.compose.components.ArcCard
import com.example.personallevelingsystem.ui.compose.components.ArcCheckBox
import com.example.personallevelingsystem.ui.compose.components.ArcGhostButton
import com.example.personallevelingsystem.ui.compose.components.ArcProgressBar
import com.example.personallevelingsystem.ui.compose.components.ArcStepper
import com.example.personallevelingsystem.ui.compose.theme.AccentViolet
import com.example.personallevelingsystem.ui.compose.theme.CrimsonRed
import com.example.personallevelingsystem.ui.compose.theme.DesignSystem
import com.example.personallevelingsystem.ui.compose.theme.HologramText
import com.example.personallevelingsystem.ui.compose.theme.Motion
import com.example.personallevelingsystem.ui.compose.theme.PersonalLevelingSystemTheme
import com.example.personallevelingsystem.ui.compose.theme.SignalCyan
import com.example.personallevelingsystem.ui.compose.theme.TelemetryGreen
import com.example.personallevelingsystem.ui.compose.theme.TextSecondary
import com.example.personallevelingsystem.ui.compose.theme.tabular
import com.example.personallevelingsystem.util.hapticConfirm
import com.example.personallevelingsystem.util.hapticReward
import com.example.personallevelingsystem.viewmodel.TrainingViewModel
import com.example.personallevelingsystem.viewmodel.TrainingViewModel.TrainingSetState
import kotlinx.coroutines.delay

private const val REST_BETWEEN_SETS_MS = 90_000L
private const val REST_BETWEEN_EXERCISES_MS = 180_000L
private const val WEIGHT_STEP_KG = 2.5f

@Composable
fun TrainingSessionScreen(
    viewModel: TrainingViewModel,
    onBackClick: () -> Unit
) {
    val currentExercises by viewModel.currentExercises.observeAsState(initial = emptyList())
    val currentIndex by viewModel.currentExerciseIndex.observeAsState(initial = 0)
    val currentSets by viewModel.currentSets.observeAsState(initial = emptyList())

    val sessionFinished by viewModel.sessionFinished.observeAsState(initial = false)
    LaunchedEffect(sessionFinished) {
        if (sessionFinished) {
            onBackClick()
        }
    }

    TrainingSessionContent(
        currentExercises = currentExercises,
        currentIndex = currentIndex,
        currentSets = currentSets,
        onRepsChange = { index, newValue ->
            val updatedList = currentSets.toMutableList()
            updatedList[index] = currentSets[index].copy(reps = newValue)
            viewModel.saveCurrentSetState(updatedList)
        },
        onWeightChange = { index, newValue ->
            val updatedList = currentSets.toMutableList()
            updatedList[index] = currentSets[index].copy(weight = newValue)
            viewModel.saveCurrentSetState(updatedList)
        },
        onNextExercise = { viewModel.nextExercise() },
        onBackClick = {
            viewModel.stopTimerService()
            onBackClick()
        }
    )
}

@Composable
fun TrainingSessionContent(
    currentExercises: List<Exercise>,
    currentIndex: Int,
    currentSets: List<TrainingSetState>,
    onRepsChange: (Int, String) -> Unit,
    onWeightChange: (Int, String) -> Unit,
    onNextExercise: () -> Unit,
    onBackClick: () -> Unit
) {
    KeepScreenOn()
    val view = LocalView.current

    var elapsedMs by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        val startTime = System.currentTimeMillis()
        while (true) {
            elapsedMs = System.currentTimeMillis() - startTime
            delay(1000)
        }
    }

    var restEndAt by remember { mutableStateOf<Long?>(null) }
    var restTotalMs by remember { mutableLongStateOf(0L) }
    var restRemainingMs by remember { mutableLongStateOf(0L) }
    LaunchedEffect(restEndAt) {
        val end = restEndAt ?: return@LaunchedEffect
        while (true) {
            val remaining = end - System.currentTimeMillis()
            if (remaining <= 0L) {
                restRemainingMs = 0L
                restEndAt = null
                view.hapticReward()
                delay(180)
                view.hapticReward()
                break
            }
            restRemainingMs = remaining
            delay(200)
        }
    }
    fun startRest(durationMs: Long) {
        restTotalMs = durationMs
        restRemainingMs = durationMs
        restEndAt = System.currentTimeMillis() + durationMs
    }

    val doneSets = remember(currentIndex) { mutableStateListOf<Int>() }

    if (currentExercises.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "No exercises found or still loading…",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
        return
    }
    val exercise = currentExercises.getOrNull(currentIndex) ?: return
    val isLast = currentIndex >= currentExercises.size - 1
    val setProgress = if (currentSets.isEmpty()) 0f else doneSets.size.toFloat() / currentSets.size
    val sessionProgress = (currentIndex + setProgress) / currentExercises.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = DesignSystem.Padding)
    ) {
        SessionHeader(
            exerciseName = exercise.name,
            index = currentIndex,
            total = currentExercises.size,
            elapsedMs = elapsedMs,
            progress = sessionProgress
        )

        AnimatedVisibility(
            visible = restEndAt != null,
            enter = expandVertically(tween(Motion.Standard)) + fadeIn(tween(Motion.Standard)),
            exit = shrinkVertically(tween(Motion.Standard)) + fadeOut(tween(Motion.Quick))
        ) {
            Column {
                Spacer(modifier = Modifier.height(12.dp))
                RestTimerCard(
                    remainingMs = restRemainingMs,
                    totalMs = restTotalMs,
                    onAddThirty = {
                        restEndAt = restEndAt?.plus(30_000L)
                        restTotalMs += 30_000L
                    },
                    onSkip = { restEndAt = null }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 12.dp)
        ) {
            itemsIndexed(currentSets) { index, set ->
                SetCard(
                    number = index + 1,
                    state = set,
                    done = index in doneSets,
                    onRepsChange = { onRepsChange(index, it) },
                    onWeightChange = { onWeightChange(index, it) },
                    onToggleDone = {
                        if (index in doneSets) {
                            doneSets.remove(index)
                        } else {
                            doneSets.add(index)
                            view.hapticConfirm()
                            val allDone = doneSets.size >= currentSets.size
                            startRest(if (allDone) REST_BETWEEN_EXERCISES_MS else REST_BETWEEN_SETS_MS)
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        ArcButton(
            text = if (isLast) "Finish session" else "Next exercise",
            subtitle = if (isLast) "Save these sets and close out" else "Save sets · then ${currentExercises[currentIndex + 1].name}",
            icon = if (isLast) Icons.Rounded.Flag else Icons.AutoMirrored.Rounded.ArrowForward,
            onClick = {
                view.hapticConfirm()
                if (!isLast && restEndAt == null) startRest(REST_BETWEEN_EXERCISES_MS)
                onNextExercise()
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        ArcGhostButton(
            text = "End session",
            onClick = onBackClick,
            tint = CrimsonRed,
            compact = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun SessionHeader(
    exerciseName: String,
    index: Int,
    total: Int,
    elapsedMs: Long,
    progress: Float
) {
    ArcCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "EXERCISE ${index + 1} / $total",
                    style = MaterialTheme.typography.labelMedium,
                    color = AccentViolet,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = exerciseName,
                    style = MaterialTheme.typography.headlineSmall,
                    color = HologramText
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatTime(elapsedMs),
                    style = MaterialTheme.typography.titleLarge.tabular,
                    color = SignalCyan
                )
                Text(
                    text = "ELAPSED",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        ArcProgressBar(progress = progress, color = CrimsonRed, height = 4.dp)
    }
}

@Composable
private fun RestTimerCard(
    remainingMs: Long,
    totalMs: Long,
    onAddThirty: () -> Unit,
    onSkip: () -> Unit
) {
    val seconds = (remainingMs + 999) / 1000
    ArcCard(modifier = Modifier.fillMaxWidth(), accent = SignalCyan) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "REST",
                    style = MaterialTheme.typography.labelMedium,
                    color = AccentViolet,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "%d:%02d".format(seconds / 60, seconds % 60),
                    style = MaterialTheme.typography.displaySmall.tabular,
                    color = SignalCyan
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                ArcGhostButton(text = "+30 s", onClick = onAddThirty, compact = true)
                Spacer(modifier = Modifier.height(6.dp))
                ArcGhostButton(text = "Skip", onClick = onSkip, compact = true, tint = TextSecondary)
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        ArcProgressBar(
            progress = if (totalMs > 0) remainingMs.toFloat() / totalMs else 0f,
            color = SignalCyan,
            height = 4.dp
        )
    }
}

@Composable
private fun SetCard(
    number: Int,
    state: TrainingSetState,
    done: Boolean,
    onRepsChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onToggleDone: () -> Unit
) {
    val reps = state.reps.toIntOrNull() ?: state.previousReps
    val weight = state.weight.toFloatOrNull() ?: state.previousWeight

    ArcCard(
        modifier = Modifier.fillMaxWidth(),
        accent = if (done) TelemetryGreen else null,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "SET $number",
                style = MaterialTheme.typography.labelMedium,
                color = if (done) TelemetryGreen else AccentViolet,
                letterSpacing = 1.5.sp,
                modifier = Modifier.weight(1f)
            )
            if (state.previousReps > 0) {
                Text(
                    text = "prev ${state.previousReps} × ${formatWeight(state.previousWeight)} kg",
                    style = MaterialTheme.typography.labelSmall.tabular,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            ArcCheckBox(checked = done, onToggle = onToggleDone)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row {
            ArcStepper(
                value = "$reps",
                label = "reps",
                onDecrement = { onRepsChange((reps - 1).coerceAtLeast(0).toString()) },
                onIncrement = { onRepsChange((reps + 1).toString()) },
                enabled = !done,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(20.dp))
            ArcStepper(
                value = formatWeight(weight),
                label = "kg",
                onDecrement = { onWeightChange(formatWeight((weight - WEIGHT_STEP_KG).coerceAtLeast(0f))) },
                onIncrement = { onWeightChange(formatWeight(weight + WEIGHT_STEP_KG)) },
                tint = SignalCyan,
                enabled = !done,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun KeepScreenOn() {
    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }
}

private fun formatWeight(kg: Float): String =
    if (kg % 1f == 0f) kg.toInt().toString() else "%.1f".format(kg)

fun formatTime(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    val hours = (millis / (1000 * 60 * 60))
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0A10)
@Composable
fun TrainingSessionPreview() {
    PersonalLevelingSystemTheme {
        TrainingSessionContent(
            currentExercises = listOf(
                Exercise(name = "Bench Press", sessionId = 1, sets = 3),
                Exercise(name = "Incline Press", sessionId = 1, sets = 3)
            ),
            currentIndex = 0,
            currentSets = listOf(
                TrainingSetState(reps = "10", weight = "100", previousReps = 10, previousWeight = 95f),
                TrainingSetState(reps = "8", weight = "105", previousReps = 8, previousWeight = 100f),
                TrainingSetState(reps = "6", weight = "110", previousReps = 6, previousWeight = 105f)
            ),
            onRepsChange = { _, _ -> },
            onWeightChange = { _, _ -> },
            onNextExercise = {},
            onBackClick = {}
        )
    }
}
