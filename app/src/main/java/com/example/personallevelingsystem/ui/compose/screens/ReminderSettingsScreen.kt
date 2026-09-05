package com.example.personallevelingsystem.ui.compose.screens

import androidx.annotation.StringRes
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Palette
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personallevelingsystem.R
import com.example.personallevelingsystem.ui.compose.components.ArcCard
import com.example.personallevelingsystem.ui.compose.components.ArcListRow
import com.example.personallevelingsystem.ui.compose.components.ArcSegmentedTabs
import com.example.personallevelingsystem.ui.compose.theme.AccentViolet
import com.example.personallevelingsystem.ui.compose.theme.BorderSubtle
import com.example.personallevelingsystem.ui.compose.theme.CrimsonRed
import com.example.personallevelingsystem.ui.compose.theme.DesignSystem
import com.example.personallevelingsystem.ui.compose.theme.HologramText
import com.example.personallevelingsystem.ui.compose.theme.PrimaryAccent
import com.example.personallevelingsystem.ui.compose.theme.RubyRed
import com.example.personallevelingsystem.ui.compose.theme.TextSecondary
import com.example.personallevelingsystem.util.AppLanguage
import com.example.personallevelingsystem.util.MissionPrefs
import com.example.personallevelingsystem.worker.LoggingReminderWorker
import com.example.personallevelingsystem.worker.MissionReminderWorker

private data class SlotEntry(val slot: String, @StringRes val labelRes: Int, @StringRes val hintRes: Int)

private val ALL_SLOTS = listOf(
    SlotEntry(MissionReminderWorker.SLOT_MIDDAY, R.string.slot_midday, R.string.slot_midday_hint),
    SlotEntry(MissionReminderWorker.SLOT_EVENING, R.string.slot_evening, R.string.slot_evening_hint),
    SlotEntry(MissionReminderWorker.SLOT_LAST_CALL_DAILY, R.string.slot_last_call_daily, R.string.slot_last_call_daily_hint),
    SlotEntry(MissionReminderWorker.SLOT_LAST_CALL_WEEKLY, R.string.slot_last_call_weekly, R.string.slot_last_call_weekly_hint),
    SlotEntry(LoggingReminderWorker.SLOT_SLEEP_MORNING, R.string.slot_sleep_morning, R.string.slot_sleep_morning_hint),
    SlotEntry(LoggingReminderWorker.SLOT_WATER_MORNING, R.string.slot_water_morning, R.string.slot_water_morning_hint),
    SlotEntry(LoggingReminderWorker.SLOT_NUTRITION_LUNCH, R.string.slot_nutrition_lunch, R.string.slot_nutrition_lunch_hint),
    SlotEntry(LoggingReminderWorker.SLOT_WATER_AFTERNOON, R.string.slot_water_afternoon, R.string.slot_water_afternoon_hint),
    SlotEntry(LoggingReminderWorker.SLOT_NUTRITION_DINNER, R.string.slot_nutrition_dinner, R.string.slot_nutrition_dinner_hint),
    SlotEntry(LoggingReminderWorker.SLOT_PLANNING_EVENING, R.string.slot_planning_evening, R.string.slot_planning_evening_hint),
    SlotEntry(LoggingReminderWorker.SLOT_WEIGHT_SUNDAY, R.string.slot_weight_sunday, R.string.slot_weight_sunday_hint),
    SlotEntry("morning_briefing", R.string.slot_morning_briefing, R.string.slot_morning_briefing_hint),
    SlotEntry("weekly_debrief", R.string.slot_weekly_debrief, R.string.slot_weekly_debrief_hint)
)

@Composable
fun ReminderSettingsScreen(onOpenStyleLab: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { MissionPrefs.get(context) }

    var notifsOn by remember { mutableStateOf(prefs.areNotificationsEnabled()) }
    var quietOn by remember { mutableStateOf(prefs.areQuietHoursEnabled()) }
    var quietStart by remember { mutableStateOf(prefs.getQuietStartHour()) }
    var quietEnd by remember { mutableStateOf(prefs.getQuietEndHour()) }
    var behindOnly by remember { mutableStateOf(prefs.isBehindOnlyMode()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = DesignSystem.Padding)
    ) {
        item {
            SettingsSection(stringResource(R.string.settings_language)) {
                LanguageCard()
            }
            SettingsSection(stringResource(R.string.settings_appearance)) {
                ArcListRow(
                    title = stringResource(R.string.chrome_style_lab),
                    subtitle = stringResource(R.string.settings_style_lab_sub),
                    icon = Icons.Rounded.Palette,
                    tint = AccentViolet,
                    onClick = onOpenStyleLab
                )
            }
            SettingsSection(stringResource(R.string.settings_global)) {
                ToggleRow(
                    label = stringResource(R.string.settings_notifications),
                    sublabel = stringResource(R.string.settings_notifications_sub),
                    checked = notifsOn,
                    onChange = { notifsOn = it; prefs.setNotificationsEnabled(it) }
                )
                ToggleRow(
                    label = stringResource(R.string.settings_quiet_hours),
                    sublabel = stringResource(R.string.settings_quiet_hours_sub, formatHour(quietStart), formatHour(quietEnd)),
                    checked = quietOn,
                    onChange = { quietOn = it; prefs.setQuietHoursEnabled(it) }
                )
                HourPickerRow(
                    label = stringResource(R.string.settings_quiet_starts),
                    value = quietStart,
                    onChange = { quietStart = it; prefs.setQuietStartHour(it) }
                )
                HourPickerRow(
                    label = stringResource(R.string.settings_quiet_ends),
                    value = quietEnd,
                    onChange = { quietEnd = it; prefs.setQuietEndHour(it) }
                )
                ToggleRow(
                    label = stringResource(R.string.settings_behind_only),
                    sublabel = stringResource(R.string.settings_behind_only_sub),
                    checked = behindOnly,
                    onChange = { behindOnly = it; prefs.setBehindOnlyMode(it) }
                )
            }
            SectionTitle(stringResource(R.string.settings_reminders))
        }
        items(ALL_SLOTS, key = { it.slot }) { entry ->
            var enabled by remember { mutableStateOf(prefs.isSlotEnabled(entry.slot)) }
            ToggleRow(
                label = stringResource(entry.labelRes),
                sublabel = stringResource(entry.hintRes),
                checked = enabled,
                onChange = { enabled = it; prefs.setSlotEnabled(entry.slot, it) }
            )
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun LanguageCard() {
    val context = LocalContext.current
    val current = remember { AppLanguage.current(context) }
    val labels = listOf(
        stringResource(R.string.lang_system),
        stringResource(R.string.lang_en),
        stringResource(R.string.lang_fr)
    )
    ArcCard(modifier = Modifier.fillMaxWidth()) {
        ArcSegmentedTabs(
            options = labels,
            selected = AppLanguage.choices.indexOf(current).coerceAtLeast(0),
            onSelect = { index -> AppLanguage.set(context, AppLanguage.choices[index]) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(
                if (AppLanguage.isSupported) R.string.settings_language_hint
                else R.string.settings_language_unsupported
            ),
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    SectionTitle(title)
    content()
    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        color = AccentViolet,
        style = MaterialTheme.typography.labelMedium,
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
    )
}

@Composable
private fun ToggleRow(label: String, sublabel: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(Color(0x0AFFFFFF), RoundedCornerShape(12.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
            .clickable { onChange(!checked) }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = HologramText, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
            Text(sublabel, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
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
