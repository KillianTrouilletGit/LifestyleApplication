package com.example.personallevelingsystem.ui.compose.screens

import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personallevelingsystem.R
import com.example.personallevelingsystem.ui.compose.components.ArcButton
import com.example.personallevelingsystem.ui.compose.components.ArcCard
import com.example.personallevelingsystem.ui.compose.components.JuicyInput
import com.example.personallevelingsystem.ui.compose.theme.AccentViolet
import com.example.personallevelingsystem.ui.compose.theme.DeepViolet
import com.example.personallevelingsystem.ui.compose.theme.DesignSystem
import com.example.personallevelingsystem.ui.compose.theme.PersonalLevelingSystemTheme
import com.example.personallevelingsystem.ui.compose.theme.TextSecondary
import com.example.personallevelingsystem.util.hapticConfirm
import com.example.personallevelingsystem.viewmodel.HealthViewModel
import java.util.Calendar
import java.util.Locale

@Composable
fun SleepScreen(
    viewModel: HealthViewModel,
    onBackClick: () -> Unit
) {
    var duration by remember { mutableStateOf("") }

    SleepContent(
        duration = duration,
        onDurationChange = { duration = it },
        onSave = {
            if (duration.isNotEmpty()) {
                viewModel.saveSleep(duration)
                onBackClick()
            }
        }
    )
}

@Composable
fun SleepContent(
    duration: String,
    onDurationChange: (String) -> Unit,
    onSave: () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val calendar = Calendar.getInstance()
    val timePickerDialog = TimePickerDialog(
        context,
        R.style.NeonDialogTheme,
        { _, selectedHour, selectedMinute ->
            onDurationChange(String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute))
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(DesignSystem.Padding),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ArcCard(modifier = Modifier.fillMaxWidth(), accent = DeepViolet) {
            Text(
                text = stringResource(R.string.sleep_last_night).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = AccentViolet,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.sleep_hint),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth()) {
                JuicyInput(
                    value = duration,
                    onValueChange = {},
                    placeholder = "08:00",
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { timePickerDialog.show() }
                )
            }
        }

        ArcButton(
            text = stringResource(R.string.sleep_log),
            icon = Icons.Rounded.Bedtime,
            showChevron = false,
            enabled = duration.isNotEmpty(),
            onClick = {
                view.hapticConfirm()
                onSave()
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0A10)
@Composable
fun SleepScreenPreview() {
    PersonalLevelingSystemTheme {
        SleepContent(
            duration = "07:30",
            onDurationChange = {},
            onSave = {}
        )
    }
}
