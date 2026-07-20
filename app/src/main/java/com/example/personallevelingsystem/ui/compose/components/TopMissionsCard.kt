package com.example.personallevelingsystem.ui.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personallevelingsystem.model.Mission
import com.example.personallevelingsystem.model.MissionCategory
import com.example.personallevelingsystem.ui.compose.theme.AccentViolet
import com.example.personallevelingsystem.ui.compose.theme.AlertOrange
import com.example.personallevelingsystem.ui.compose.theme.BorderSubtle
import com.example.personallevelingsystem.ui.compose.theme.CalmBlue
import com.example.personallevelingsystem.ui.compose.theme.CrimsonRed
import com.example.personallevelingsystem.ui.compose.theme.GlassSurface
import com.example.personallevelingsystem.ui.compose.theme.HologramText
import com.example.personallevelingsystem.ui.compose.theme.PrimaryAccent
import com.example.personallevelingsystem.ui.compose.theme.TelemetryGreen

/**
 * Compact "Today's Ops" card surfaced on MainScreen.
 * Shows the top 3 incomplete daily missions, total XP available, and lets the user
 * jump straight into the full mission list.
 */
@Composable
fun TopMissionsCard(
    incompleteMissions: List<Mission>,
    totalDaily: Int,
    onOpenMissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalXp = incompleteMissions.sumOf { it.reward }
    val doneCount = totalDaily - incompleteMissions.size

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(GlassSurface, RoundedCornerShape(16.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            .clickable(onClick = onOpenMissions)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "TODAY'S OPS",
                color = AccentViolet,
                style = MaterialTheme.typography.labelMedium,
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = if (incompleteMissions.isEmpty()) "ALL CLEAR"
                       else "+$totalXp XP AVAILABLE",
                color = if (incompleteMissions.isEmpty()) TelemetryGreen else AlertOrange,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelMedium
            )
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "$doneCount / $totalDaily cleared",
            color = HologramText.copy(alpha = 0.6f),
            style = MaterialTheme.typography.labelSmall
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (incompleteMissions.isEmpty()) {
            Text(
                text = "Daily slate cleared. Streaks alive.",
                color = HologramText.copy(alpha = 0.85f),
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            incompleteMissions.take(3).forEach { m ->
                MissionRow(m)
                Spacer(modifier = Modifier.height(4.dp))
            }
            val remaining = incompleteMissions.size - 3
            if (remaining > 0) {
                Text(
                    text = "+ $remaining more — tap to open list",
                    color = HologramText.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun MissionRow(mission: Mission) {
    val color = categoryColor(mission.category)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = mission.title,
            color = HologramText,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "+${mission.reward}",
            color = PrimaryAccent,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun categoryColor(category: MissionCategory): Color = when (category) {
    MissionCategory.BODY -> CrimsonRed
    MissionCategory.MIND -> AccentViolet
    MissionCategory.NUTRITION -> TelemetryGreen
    MissionCategory.RECOVERY -> CalmBlue
    MissionCategory.DISCIPLINE -> AlertOrange
    MissionCategory.PROGRESS -> Color(0xFFE6E6E6)
}
