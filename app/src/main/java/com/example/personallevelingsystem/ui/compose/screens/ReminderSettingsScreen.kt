package com.example.personallevelingsystem.ui.compose.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personallevelingsystem.ui.compose.components.JuicyButton
import com.example.personallevelingsystem.ui.compose.components.OperatorHeader
import com.example.personallevelingsystem.ui.compose.theme.CrimsonRed
import com.example.personallevelingsystem.ui.compose.theme.DesignSystem
import com.example.personallevelingsystem.ui.compose.theme.HologramText
import com.example.personallevelingsystem.ui.compose.theme.PrimaryAccent
import com.example.personallevelingsystem.ui.compose.theme.RubyRed
import com.example.personallevelingsystem.util.MissionPrefs
import com.example.personallevelingsystem.worker.LoggingReminderWorker
import com.example.personallevelingsystem.worker.MissionReminderWorker

private data class SlotEntry(val slot: String, val label: String, val hint: String)

private val ALL_SLOTS = listOf(
    SlotEntry(MissionReminderWorker.SLOT_MIDDAY, "Mid-day check-in", "12:30 · daily ops left"),
    SlotEntry(MissionReminderWorker.SLOT_EVENING, "Evening digest", "18:00 · consolidated status"),
    SlotEntry(MissionReminderWorker.SLOT_LAST_CALL_DAILY, "Daily last call", "21:30 · urgent, bypasses quiet hours"),
    SlotEntry(MissionReminderWorker.SLOT_LAST_CALL_WEEKLY, "Weekly last call", "Sun 19:00 · urgent"),
    SlotEntry(LoggingReminderWorker.SLOT_SLEEP_MORNING, "Sleep log", "09:00 · last night's sleep"),
    SlotEntry(LoggingReminderWorker.SLOT_WATER_MORNING, "Water — start of day", "10:00 · if zero entries"),
    SlotEntry(LoggingReminderWorker.SLOT_NUTRITION_LUNCH, "Lunch log", "13:30 · if no meals"),
    SlotEntry(LoggingReminderWorker.SLOT_WATER_AFTERNOON, "Water — behind pace", "15:00 · if <50% of target"),
    SlotEntry(LoggingReminderWorker.SLOT_NUTRITION_DINNER, "Dinner log", "20:30 · if <2 meals"),
    SlotEntry(LoggingReminderWorker.SLOT_PLANNING_EVENING, "Plan tomorrow", "21:00 · set 3 priorities"),
    SlotEntry(LoggingReminderWorker.SLOT_WEIGHT_SUNDAY, "Weekly weigh-in", "Sun 10:00"),
    SlotEntry("morning_briefing", "Morning briefing", "07:30 · recovery + ops + plan"),
    SlotEntry("weekly_debrief", "Weekly debrief", "Sun 20:00 · XP + streaks summary")
)

@Composable
fun ReminderSettingsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { MissionPrefs.get(context) }

    var notifsOn by remember { mutableStateOf(prefs.areNotificationsEnabled()) }
    var quietOn by remember { mutableStateOf(prefs.areQuietHoursEnabled()) }
    var quietStart by remember { mutableStateOf(prefs.getQuietStartHour()) }
    var quietEnd by remember { mutableStateOf(prefs.getQuietEndHour()) }
    var behindOnly by remember { mutableStateOf(prefs.isBehindOnlyMode()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(DesignSystem.Padding)
    ) {
        OperatorHeader(subtitle = "Settings", title = "Reminder Tuning")

        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
            item {
                SettingsSection("GLOBAL") {
                    ToggleRow(
                        label = "Notifications",
                        sublabel = "Master switch. Off = silence everything.",
                        checked = notifsOn,
                        onChange = { notifsOn = it; prefs.setNotificationsEnabled(it) }
                    )
                    ToggleRow(
                        label = "Quiet hours",
                        sublabel = "${formatHour(quietStart)} → ${formatHour(quietEnd)}. Urgent reminders bypass this.",
                        checked = quietOn,
                        onChange = { quietOn = it; prefs.setQuietHoursEnabled(it) }
                    )
                    HourPickerRow(
                        label = "Quiet starts",
                        value = quietStart,
                        onChange = { quietStart = it; prefs.setQuietStartHour(it) }
                    )
                    HourPickerRow(
                        label = "Quiet ends",
                        value = quietEnd,
                        onChange = { quietEnd = it; prefs.setQuietEndHour(it) }
                    )
                    ToggleRow(
                        label = "Behind-only mode",
                        sublabel = "Skip mid-day pings unless 3+ ops are open.",
                        checked = behindOnly,
                        onChange = { behindOnly = it; prefs.setBehindOnlyMode(it) }
                    )
                }
            }
            items(ALL_SLOTS, key = { it.slot }) { entry ->
                var enabled by remember { mutableStateOf(prefs.isSlotEnabled(entry.slot)) }
                ToggleRow(
                    label = entry.label,
                    sublabel = entry.hint,
                    checked = enabled,
                    onChange = { enabled = it; prefs.setSlotEnabled(entry.slot, it) }
                )
            }
        }

        JuicyButton(text = "DONE", onClick = onBackClick, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = title,
        color = PrimaryAccent,
        style = MaterialTheme.typography.labelMedium,
        letterSpacing = 1.5.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
    content()
    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
private fun ToggleRow(label: String, sublabel: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .border(
                1.dp,
                Brush.linearGradient(listOf(RubyRed.copy(alpha = 0.4f), Color.White.copy(alpha = 0.05f))),
                RoundedCornerShape(8.dp)
            )
            .clickable { onChange(!checked) }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = HologramText, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
            Text(sublabel, color = HologramText.copy(alpha = 0.6f), style = MaterialTheme.typography.bodySmall)
        }
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = HologramText,
                checkedTrackColor = CrimsonRed,
                uncheckedThumbColor = HologramText.copy(alpha = 0.6f),
                uncheckedTrackColor = Color.White.copy(alpha = 0.08f)
            )
        )
    }
}

@Composable
private fun HourPickerRow(label: String, value: Int, onChange: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = HologramText.copy(alpha = 0.8f), style = MaterialTheme.typography.bodyMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            StepButton("–") { onChange(((value - 1) + 24) % 24) }
            Spacer(modifier = Modifier.width(12.dp))
            Text(formatHour(value), color = PrimaryAccent, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(12.dp))
            StepButton("+") { onChange((value + 1) % 24) }
        }
    }
}

@Composable
private fun StepButton(label: String, onClick: () -> Unit) {
    Text(
        text = label,
        color = HologramText,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .border(1.dp, RubyRed.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 2.dp)
    )
}

private fun formatHour(hour: Int): String = "%02d:00".format(hour)
