package com.example.personallevelingsystem.ui.compose.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personallevelingsystem.R
import com.example.personallevelingsystem.data.ExerciseHistoryPoint
import com.example.personallevelingsystem.model.Exercise
import com.example.personallevelingsystem.model.Program
import com.example.personallevelingsystem.model.ProgramWithSessions
import com.example.personallevelingsystem.model.SessionWithExercises
import com.example.personallevelingsystem.ui.compose.components.JuicyButton
import com.example.personallevelingsystem.ui.compose.components.OperatorHeader
import com.example.personallevelingsystem.ui.compose.theme.AccentViolet
import com.example.personallevelingsystem.ui.compose.theme.BorderSubtle
import com.example.personallevelingsystem.ui.compose.theme.CrimsonRed
import com.example.personallevelingsystem.ui.compose.theme.DeepViolet
import com.example.personallevelingsystem.ui.compose.theme.DesignSystem
import com.example.personallevelingsystem.ui.compose.theme.HologramText
import com.example.personallevelingsystem.ui.compose.theme.PersonalLevelingSystemTheme
import com.example.personallevelingsystem.ui.compose.theme.PlacementSpring
import com.example.personallevelingsystem.ui.compose.theme.SurfaceElevated
import com.example.personallevelingsystem.ui.compose.theme.TelemetryGreen
import com.example.personallevelingsystem.ui.compose.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ViewProgramsScreen(
    programs: List<ProgramWithSessions>, // Will come from ViewModel
    exerciseHistory: List<ExerciseHistoryPoint>,
    onExerciseClick: (Exercise) -> Unit,
    onDeleteProgram: (ProgramWithSessions) -> Unit,
    onBackClick: () -> Unit
) {
    var inspectedExercise by remember { mutableStateOf<Exercise?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(DesignSystem.Padding)
    ) {
        OperatorHeader(subtitle = "Database", title = "Program Archive")

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items = programs, key = { it.program.id }) { programWithSessions ->
                ProgramItem(
                    programWithSessions = programWithSessions,
                    onExerciseClick = { exercise ->
                        inspectedExercise = exercise
                        onExerciseClick(exercise)
                    },
                    onDelete = { onDeleteProgram(programWithSessions) },
                    modifier = Modifier.animateItemPlacement(PlacementSpring)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        JuicyButton(
            onClick = onBackClick,
            text = "RETURN TO TRAINING",
            modifier = Modifier.fillMaxWidth()
        )
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    inspectedExercise?.let { exercise ->
        ModalBottomSheet(
            onDismissRequest = { inspectedExercise = null },
            sheetState = sheetState,
            containerColor = SurfaceElevated
        ) {
            ExerciseHistorySheet(exercise = exercise, points = exerciseHistory)
        }
    }
}

/**
 * Program card: tap to expand into its sessions and exercises.
 * Each exercise row opens the weight-evolution sheet.
 */
@Composable
fun ProgramItem(
    programWithSessions: ProgramWithSessions,
    onExerciseClick: (Exercise) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val program = programWithSessions.program

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceElevated.copy(alpha = 0.55f), RoundedCornerShape(DesignSystem.CornerRadius))
            .border(1.dp, BorderSubtle, RoundedCornerShape(DesignSystem.CornerRadius))
            .clickable { expanded = !expanded }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = program.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${programWithSessions.sessions.size} sessions · tap to ${if (expanded) "collapse" else "inspect"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_close),
                    contentDescription = "Delete Program",
                    tint = TextSecondary
                )
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                programWithSessions.sessions.forEach { sessionWithExercises ->
                    SessionBlock(
                        sessionWithExercises = sessionWithExercises,
                        onExerciseClick = onExerciseClick
                    )
                }
            }
        }
    }
}

@Composable
private fun SessionBlock(
    sessionWithExercises: SessionWithExercises,
    onExerciseClick: (Exercise) -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(
            text = sessionWithExercises.session.name.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = AccentViolet,
            letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        sessionWithExercises.exercises.forEach { exercise ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExerciseClick(exercise) }
                    .padding(vertical = 8.dp, horizontal = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .background(CrimsonRed, RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = HologramText,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${exercise.sets} sets",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "📈",
                    fontSize = 13.sp
                )
            }
        }
    }
}

/** Bottom sheet: weight evolution chart + key stats for one exercise. */
@Composable
private fun ExerciseHistorySheet(exercise: Exercise, points: List<ExerciseHistoryPoint>) {
    // Same Material3 1.2.x workaround as the missions sheet: read the real
    // navigation-bar inset from the root view (WindowInsets report zero here).
    val view = androidx.compose.ui.platform.LocalView.current
    val density = androidx.compose.ui.platform.LocalDensity.current
    val bottomInset = remember(view) {
        val raw = androidx.core.view.ViewCompat.getRootWindowInsets(view)
            ?.getInsets(androidx.core.view.WindowInsetsCompat.Type.navigationBars())?.bottom ?: 0
        with(density) { raw.toDp() }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = exercise.name,
            color = HologramText,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "WEIGHT EVOLUTION",
            color = AccentViolet,
            style = MaterialTheme.typography.labelMedium,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (points.size < 2) {
            Text(
                text = if (points.isEmpty())
                    "No logged sets with weight yet. Finish a session with this exercise to start the curve."
                else
                    "One data point logged — one more session and the curve appears.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            WeightEvolutionChart(points = points)

            Spacer(modifier = Modifier.height(16.dp))

            val first = points.first().topWeight
            val latest = points.last().topWeight
            val best = points.maxOf { it.topWeight }
            val delta = latest - first
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HistoryStat("LATEST", formatKg(latest))
                HistoryStat("BEST", formatKg(best))
                HistoryStat(
                    label = "PROGRESS",
                    value = (if (delta >= 0) "+" else "") + formatKg(delta),
                    valueColor = if (delta >= 0) TelemetryGreen else CrimsonRed
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            val fmt = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
            Text(
                text = "${fmt.format(Date(points.first().date))}  →  ${fmt.format(Date(points.last().date))} · ${points.size} sessions",
                color = TextSecondary,
                style = MaterialTheme.typography.labelSmall
            )
        }

        Spacer(modifier = Modifier.height(16.dp + bottomInset.coerceAtLeast(16.dp)))
    }
}

@Composable
private fun HistoryStat(label: String, value: String, valueColor: Color = HologramText) {
    Box(
        modifier = Modifier
            .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Column {
            Text(
                text = label,
                color = AccentViolet,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                letterSpacing = 1.sp
            )
            Text(
                text = value,
                color = valueColor,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun formatKg(v: Float): String =
    if (v % 1f == 0f) "${v.toInt()} kg" else "%.1f kg".format(v)

/** Sober line chart: red→violet curve of best weight per session. */
@Composable
private fun WeightEvolutionChart(points: List<ExerciseHistoryPoint>) {
    val minW = points.minOf { it.topWeight }
    val maxW = points.maxOf { it.topWeight }
    val range = (maxW - minW).takeIf { it > 0f } ?: 1f

    Row(verticalAlignment = Alignment.Top) {
        // Y extremes
        Column(modifier = Modifier.height(160.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Text(formatKg(maxW), color = TextSecondary, style = MaterialTheme.typography.labelSmall)
            Text(formatKg(minW), color = TextSecondary, style = MaterialTheme.typography.labelSmall)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Canvas(
            modifier = Modifier
                .weight(1f)
                .height(160.dp)
        ) {
            val stepX = if (points.size > 1) size.width / (points.size - 1) else size.width
            val padY = size.height * 0.10f
            val usable = size.height - padY * 2
            fun yOf(w: Float) = padY + usable * (1f - (w - minW) / range)

            // Baseline grid: three hairlines
            for (frac in listOf(0f, 0.5f, 1f)) {
                val y = padY + usable * frac
                drawLine(
                    color = Color.White.copy(alpha = 0.08f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            val path = Path()
            points.forEachIndexed { i, p ->
                val x = stepX * i
                val y = yOf(p.topWeight)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }

            // Soft area fill under the curve
            val area = Path().apply {
                addPath(path)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(
                area,
                Brush.verticalGradient(listOf(CrimsonRed.copy(alpha = 0.16f), Color.Transparent))
            )

            // The curve itself — signature red→violet ramp
            drawPath(
                path,
                brush = Brush.horizontalGradient(listOf(CrimsonRed, DeepViolet)),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Data points get noisy on dense series — the line alone reads better there
            if (points.size <= 24) {
                points.forEachIndexed { i, p ->
                    val c = Offset(stepX * i, yOf(p.topWeight))
                    drawCircle(color = SurfaceElevated, radius = 4.5.dp.toPx(), center = c)
                    drawCircle(
                        color = if (i == points.lastIndex) CrimsonRed else Color.White.copy(alpha = 0.85f),
                        radius = if (i == points.lastIndex) 3.5.dp.toPx() else 2.5.dp.toPx(),
                        center = c
                    )
                }
            } else {
                val last = Offset(stepX * points.lastIndex, yOf(points.last().topWeight))
                drawCircle(color = SurfaceElevated, radius = 5.dp.toPx(), center = last)
                drawCircle(color = CrimsonRed, radius = 3.5.dp.toPx(), center = last)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ViewProgramsScreenPreview() {
    PersonalLevelingSystemTheme {
        ViewProgramsScreen(
            programs = listOf(
                ProgramWithSessions(
                    program = Program(id = 1, name = "Spartan Protocol"),
                    sessions = emptyList()
                )
            ),
            exerciseHistory = emptyList(),
            onExerciseClick = {},
            onDeleteProgram = {},
            onBackClick = {}
        )
    }
}
