package com.example.personallevelingsystem.ui.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personallevelingsystem.R
import com.example.personallevelingsystem.model.Mission
import com.example.personallevelingsystem.model.MissionCategory
import com.example.personallevelingsystem.ui.compose.theme.AccentViolet
import com.example.personallevelingsystem.ui.compose.theme.AlertOrange
import com.example.personallevelingsystem.ui.compose.theme.CalmBlue
import com.example.personallevelingsystem.ui.compose.theme.CrimsonRed
import com.example.personallevelingsystem.ui.compose.theme.HologramText
import com.example.personallevelingsystem.ui.compose.theme.TelemetryGreen
import com.example.personallevelingsystem.ui.compose.theme.TextSecondary
import com.example.personallevelingsystem.ui.compose.theme.TextTertiary
import com.example.personallevelingsystem.ui.compose.theme.tabular

/** Top three open daily missions and the XP still on the table. Tap opens the Missions tab. */
@Composable
fun TopMissionsCard(
    incompleteMissions: List<Mission>,
    totalDaily: Int,
    onOpenMissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalXp = incompleteMissions.sumOf { it.reward }
    val doneCount = totalDaily - incompleteMissions.size
    val allClear = incompleteMissions.isEmpty()

    ArcCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onOpenMissions,
        accent = if (allClear) TelemetryGreen else CrimsonRed
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.dash_todays_ops).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = AccentViolet,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = stringResource(R.string.dash_cleared, doneCount, totalDaily),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Text(
                text = if (allClear) stringResource(R.string.dash_all_clear)
                else stringResource(R.string.dash_xp_available, totalXp),
                style = MaterialTheme.typography.titleMedium.tabular,
                color = if (allClear) TelemetryGreen else AlertOrange
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (allClear) {
            Text(
                text = stringResource(R.string.dash_slate_cleared),
                style = MaterialTheme.typography.bodyMedium,
                color = HologramText.copy(alpha = 0.85f)
            )
        } else {
            incompleteMissions.take(3).forEach { mission ->
                MissionRow(mission)
                Spacer(modifier = Modifier.height(6.dp))
            }
            val remaining = incompleteMissions.size - 3
            if (remaining > 0) {
                Text(
                    text = stringResource(R.string.dash_more, remaining),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
            }
        }
    }
}

@Composable
private fun MissionRow(mission: Mission) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Spacer(
            modifier = Modifier
                .size(6.dp)
                .background(categoryColor(mission.category), RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = mission.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = HologramText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "+${mission.reward}",
            style = MaterialTheme.typography.labelMedium.tabular,
            color = CrimsonRed
        )
    }
}

fun categoryColor(category: MissionCategory): Color = when (category) {
    MissionCategory.BODY -> CrimsonRed
    MissionCategory.MIND -> AccentViolet
    MissionCategory.NUTRITION -> TelemetryGreen
    MissionCategory.RECOVERY -> CalmBlue
    MissionCategory.DISCIPLINE -> AlertOrange
    MissionCategory.PROGRESS -> Color(0xFFE6E6E6)
}

@Composable
fun MissionCategory.label(): String = stringResource(
    when (this) {
        MissionCategory.BODY -> R.string.category_body
        MissionCategory.MIND -> R.string.category_mind
        MissionCategory.NUTRITION -> R.string.category_nutrition
        MissionCategory.RECOVERY -> R.string.category_recovery
        MissionCategory.DISCIPLINE -> R.string.category_discipline
        MissionCategory.PROGRESS -> R.string.category_progress
    }
)
