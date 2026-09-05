package com.example.personallevelingsystem.ui.compose.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personallevelingsystem.R
import com.example.personallevelingsystem.ui.compose.theme.AccentViolet
import com.example.personallevelingsystem.ui.compose.theme.AlertOrange
import com.example.personallevelingsystem.ui.compose.theme.CrimsonRed
import com.example.personallevelingsystem.ui.compose.theme.DeepViolet
import com.example.personallevelingsystem.ui.compose.theme.HologramText
import com.example.personallevelingsystem.ui.compose.theme.SignalCyan
import com.example.personallevelingsystem.ui.compose.theme.TextSecondary
import com.example.personallevelingsystem.ui.compose.theme.TextTertiary
import com.example.personallevelingsystem.ui.compose.theme.tabular

@Composable
fun LevelRing(
    level: Int,
    progress: Float,
    modifier: Modifier = Modifier,
    ringSize: Dp = 124.dp,
    stroke: Dp = 10.dp
) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }
    val animated by animateFloatAsState(
        targetValue = if (started) progress.coerceIn(0f, 1f) else 0f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "levelRing"
    )

    Box(modifier = modifier.size(ringSize), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = stroke.toPx()
            val inset = strokePx / 2
            val arcSize = Size(size.width - strokePx, size.height - strokePx)
            drawArc(
                color = Color.White.copy(alpha = 0.08f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
            if (animated > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(listOf(CrimsonRed, DeepViolet, SignalCyan, CrimsonRed)),
                    startAngle = -90f,
                    sweepAngle = 360f * animated,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.dash_level).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                letterSpacing = 1.5.sp
            )
            Text(
                text = "$level",
                style = MaterialTheme.typography.displaySmall.tabular,
                color = HologramText
            )
        }
    }
}

/** Level ring plus the three numbers that matter today. */
@Composable
fun HeroCard(
    level: Int,
    currentXp: Int,
    requiredXp: Int,
    bestStreak: Int,
    activeDays: Int,
    modifier: Modifier = Modifier
) {
    ArcCard(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LevelRing(
                level = level,
                progress = if (requiredXp > 0) currentXp / requiredXp.toFloat() else 0f
            )
            Spacer(modifier = Modifier.width(20.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatLine(
                    icon = Icons.Rounded.Bolt, tint = AccentViolet,
                    label = stringResource(R.string.dash_xp),
                    value = stringResource(R.string.dash_xp_value, currentXp, requiredXp)
                )
                StatLine(
                    icon = Icons.Rounded.LocalFireDepartment, tint = AlertOrange,
                    label = stringResource(R.string.dash_best_streak),
                    value = if (bestStreak > 0) stringResource(R.string.dash_days_value, bestStreak) else "—"
                )
                StatLine(
                    icon = Icons.Rounded.CalendarToday, tint = SignalCyan,
                    label = stringResource(R.string.dash_active_days),
                    value = stringResource(R.string.dash_of_seven, activeDays)
                )
            }
        }
    }
}

@Composable
private fun StatLine(icon: ImageVector, tint: Color, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.tabular,
                color = HologramText
            )
        }
    }
}

/** The week's training volume, one bar per day, today last. Tap opens the Training tab. */
@Composable
fun WeeklyTrainingCard(
    hoursPerDay: List<Float>,
    labels: List<String>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeDays = hoursPerDay.count { it > 0.05f }
    val total = hoursPerDay.sum()
    val max = hoursPerDay.maxOrNull()?.takeIf { it > 0f } ?: 1f
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }
    val grow by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "weekBars"
    )

    ArcCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.dash_this_week).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = AccentViolet,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = stringResource(R.string.dash_training_volume),
                    style = MaterialTheme.typography.titleMedium,
                    color = HologramText
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(R.string.dash_hours, total),
                    style = MaterialTheme.typography.headlineSmall.tabular,
                    color = SignalCyan
                )
                Text(
                    text = stringResource(R.string.dash_active_days_of, activeDays),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(136.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            hoursPerDay.forEachIndexed { index, hours ->
                val isToday = index == hoursPerDay.lastIndex
                val active = hours > 0.05f
                val ratio = (hours / max).coerceIn(0f, 1f)
                val barHeight = maxOf(6.dp, 84.dp * ratio * grow)
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    if (active) {
                        Text(
                            text = "%.1f".format(hours),
                            style = MaterialTheme.typography.labelSmall.tabular,
                            color = if (isToday) SignalCyan else TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Box(
                        modifier = Modifier
                            .width(18.dp)
                            .height(barHeight)
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 3.dp, bottomEnd = 3.dp))
                            .background(
                                if (active) {
                                    Brush.verticalGradient(listOf(if (isToday) SignalCyan else CrimsonRed, DeepViolet))
                                } else {
                                    SolidColor(Color.White.copy(alpha = 0.08f))
                                }
                            )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = labels.getOrElse(index) { "" },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isToday) HologramText else TextTertiary,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

/** Today's body numbers plus one-tap logging. */
@Composable
fun QuickLogCard(
    waterMl: Float,
    waterTargetMl: Float,
    sleepHours: Float,
    calories: Int,
    onQuickWater: () -> Unit,
    onLogSleep: () -> Unit,
    onLogMeal: () -> Unit,
    modifier: Modifier = Modifier
) {
    ArcCard(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.dash_quick_log).uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = AccentViolet,
            letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row {
            MiniStat(
                label = stringResource(R.string.dash_water),
                value = stringResource(R.string.dash_water_value, waterMl / 1000f, waterTargetMl / 1000f),
                color = SignalCyan,
                modifier = Modifier.weight(1.2f)
            )
            MiniStat(
                label = stringResource(R.string.dash_sleep),
                value = if (sleepHours > 0f) stringResource(R.string.dash_hours, sleepHours) else "—",
                color = AccentViolet,
                modifier = Modifier.weight(1f)
            )
            MiniStat(
                label = stringResource(R.string.dash_fuel),
                value = if (calories > 0) stringResource(R.string.dash_kcal_value, calories) else "—",
                color = AlertOrange,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ArcButton(
                text = stringResource(R.string.dash_add_250),
                onClick = onQuickWater,
                compact = true,
                modifier = Modifier.weight(1.2f)
            )
            ArcGhostButton(
                text = stringResource(R.string.dash_sleep),
                onClick = onLogSleep,
                compact = true,
                modifier = Modifier.weight(1f)
            )
            ArcGhostButton(
                text = stringResource(R.string.dash_log_meal),
                onClick = onLogMeal,
                compact = true,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            letterSpacing = 1.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.tabular,
            color = color
        )
    }
}
