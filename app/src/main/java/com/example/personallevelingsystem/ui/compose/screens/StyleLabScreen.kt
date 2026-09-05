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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.personallevelingsystem.R
import com.example.personallevelingsystem.ui.compose.components.ArcButton
import com.example.personallevelingsystem.ui.compose.components.ArcCard
import com.example.personallevelingsystem.ui.compose.components.ArcGhostButton
import com.example.personallevelingsystem.ui.compose.components.ArcListRow
import com.example.personallevelingsystem.ui.compose.components.ArcProgressBar
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
            text = stringResource(R.string.stylelab_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        ArcSectionLabel(text = stringResource(R.string.stylelab_primary))
        ButtonStyle.entries.forEach { style ->
            val selected = style == current
            ArcCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { ArcStyle.setButtonStyle(context, style) },
                accent = if (selected) CrimsonRed else null
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(style.labelRes), style = MaterialTheme.typography.titleMedium, color = HologramText)
                        Text(stringResource(style.blurbRes), style = MaterialTheme.typography.bodySmall, color = TextSecondary)
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
                    text = stringResource(R.string.train_start_session),
                    subtitle = stringResource(R.string.stylelab_sample_start_sub),
                    icon = Icons.Rounded.PlayArrow,
                    onClick = {},
                    style = style,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ArcButton(
                        text = stringResource(R.string.dash_add_250), onClick = {}, compact = true, style = style,
                        modifier = Modifier.weight(1f)
                    )
                    ArcButton(
                        text = stringResource(R.string.stylelab_sample_log), onClick = {}, compact = true, style = style,
                        icon = Icons.Rounded.Check, showChevron = false,
                        modifier = Modifier.weight(1f)
                    )
                    ArcButton(
                        text = stringResource(R.string.stylelab_sample_off), onClick = {}, compact = true, style = style,
                        enabled = false,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        ArcSectionLabel(text = stringResource(R.string.stylelab_secondary))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ArcGhostButton(
                text = stringResource(R.string.common_abort), onClick = {}, tint = CrimsonRed, icon = Icons.Rounded.Close,
                modifier = Modifier.weight(1f)
            )
            ArcGhostButton(text = stringResource(R.string.stylelab_sample_skip), onClick = {}, modifier = Modifier.weight(1f))
        }

        ArcSectionLabel(text = stringResource(R.string.stylelab_card_stats))
        ArcCard(modifier = Modifier.fillMaxWidth()) {
            Row {
                ArcStat(label = stringResource(R.string.dash_level), value = "12", modifier = Modifier.weight(1f))
                ArcStat(label = stringResource(R.string.dash_xp), value = "12,480", modifier = Modifier.weight(1f), valueColor = SignalCyan)
                ArcStat(label = stringResource(R.string.missions_chip_streak), value = "9 d", modifier = Modifier.weight(1f), valueColor = AlertOrange)
            }
            Spacer(modifier = Modifier.height(14.dp))
            ArcProgressBar(progress = 0.62f, color = CrimsonRed, trackColor = Color.White.copy(alpha = 0.08f), height = 6.dp)
        }
        ArcListRow(
            title = stringResource(R.string.stylelab_list_row),
            subtitle = stringResource(R.string.stylelab_list_row_sub),
            icon = Icons.Rounded.Tune,
            onClick = {}
        )

        ArcSectionLabel(text = stringResource(R.string.stylelab_palette))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Swatch(CrimsonRed, stringResource(R.string.color_red))
            Swatch(DeepViolet, stringResource(R.string.color_violet))
            Swatch(SignalCyan, stringResource(R.string.color_cyan))
            Swatch(AlertOrange, stringResource(R.string.color_amber))
            Swatch(TelemetryGreen, stringResource(R.string.color_green))
        }

        ArcSectionLabel(text = stringResource(R.string.stylelab_type))
        ArcCard(modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.stylelab_type_display), style = MaterialTheme.typography.displaySmall, color = HologramText)
            Text(stringResource(R.string.stylelab_type_title), style = MaterialTheme.typography.titleLarge, color = HologramText)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                stringResource(R.string.stylelab_type_body),
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
