package com.example.personallevelingsystem.ui.compose.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.personallevelingsystem.ui.compose.components.ArcButton
import com.example.personallevelingsystem.ui.compose.components.ArcCard
import com.example.personallevelingsystem.ui.compose.components.ArcGhostButton
import com.example.personallevelingsystem.ui.compose.components.ArcListRow
import com.example.personallevelingsystem.ui.compose.components.ArcSectionLabel
import com.example.personallevelingsystem.ui.compose.components.ArcStat
import com.example.personallevelingsystem.ui.compose.theme.AlertOrange
import com.example.personallevelingsystem.ui.compose.theme.ArcStyle
import com.example.personallevelingsystem.ui.compose.theme.ButtonStyle
import com.example.personallevelingsystem.ui.compose.theme.CrimsonRed
import com.example.personallevelingsystem.ui.compose.theme.DeepViolet
import com.example.personallevelingsystem.ui.compose.theme.DesignSystem
import com.example.personallevelingsystem.ui.compose.theme.HologramText
import com.example.personallevelingsystem.ui.compose.theme.SignalCyan
import com.example.personallevelingsystem.ui.compose.theme.TelemetryGreen
import com.example.personallevelingsystem.ui.compose.theme.TextSecondary
import com.example.personallevelingsystem.ui.compose.theme.tabular

/** On-device comparison of the design system. Picking a button style applies it app-wide. */
@Composable
fun StyleLabScreen() {
    val context = LocalContext.current
    val current = ArcStyle.buttonStyle

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(DesignSystem.Padding),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Tap a style to make it the app-wide primary button. It applies instantly and is remembered.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        ArcSectionLabel(text = "Primary button")
        ButtonStyle.entries.forEach { style ->
            val selected = style == current
            ArcCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { ArcStyle.setButtonStyle(context, style) },
                accent = if (selected) CrimsonRed else null
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(style.label, style = MaterialTheme.typography.titleMedium, color = HologramText)
                        Text(style.blurb, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    RadioButton(
                        selected = selected,
                        onClick = { ArcStyle.setButtonStyle(context, style) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = CrimsonRed,
                            unselectedColor = TextSecondary
                        )
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                ArcButton(
                    text = "Start a session",
                    subtitle = "Push day · 5 exercises",
                    icon = Icons.Rounded.PlayArrow,
                    onClick = {},
                    style = style,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ArcButton(
                        text = "+250 ml", onClick = {}, compact = true, style = style,
                        modifier = Modifier.weight(1f)
                    )
                    ArcButton(
                        text = "Log", onClick = {}, compact = true, style = style,
                        icon = Icons.Rounded.Check, showChevron = false,
                        modifier = Modifier.weight(1f)
                    )
                    ArcButton(
                        text = "Off", onClick = {}, compact = true, style = style, enabled = false,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        ArcSectionLabel(text = "Secondary")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ArcGhostButton(
                text = "Abort session", onClick = {}, tint = CrimsonRed, icon = Icons.Rounded.Close,
                modifier = Modifier.weight(1f)
            )
            ArcGhostButton(text = "Skip", onClick = {}, modifier = Modifier.weight(1f))
        }

        ArcSectionLabel(text = "Card & stats")
        ArcCard(modifier = Modifier.fillMaxWidth()) {
            Row {
                ArcStat(label = "Level", value = "12", modifier = Modifier.weight(1f))
                ArcStat(label = "XP", value = "12,480", modifier = Modifier.weight(1f), valueColor = SignalCyan)
                ArcStat(label = "Streak", value = "9 d", modifier = Modifier.weight(1f), valueColor = AlertOrange)
            }
            Spacer(modifier = Modifier.height(14.dp))
            LinearProgressIndicator(
                progress = { 0.62f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = CrimsonRed,
                trackColor = Color.White.copy(alpha = 0.08f)
            )
        }
        ArcListRow(
            title = "List row",
            subtitle = "Icon tile, title, subtitle, chevron",
            icon = Icons.Rounded.Tune,
            onClick = {}
        )

        ArcSectionLabel(text = "Palette")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Swatch(CrimsonRed, "Red")
            Swatch(DeepViolet, "Violet")
            Swatch(SignalCyan, "Cyan")
            Swatch(AlertOrange, "Amber")
            Swatch(TelemetryGreen, "Green")
        }

        ArcSectionLabel(text = "Type")
        ArcCard(modifier = Modifier.fillMaxWidth()) {
            Text("Sora display", style = MaterialTheme.typography.displaySmall, color = HologramText)
            Text("Sora title", style = MaterialTheme.typography.titleLarge, color = HologramText)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Inter body — the quick brown fox jumps over the lazy dog.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "00:45 · 12,480 XP · 82.5 kg",
                style = MaterialTheme.typography.headlineSmall.tabular,
                color = SignalCyan
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun RowScope.Swatch(color: Color, name: String) {
    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(name, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}
