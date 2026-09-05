package com.example.personallevelingsystem.ui.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personallevelingsystem.ui.compose.theme.HologramText
import com.example.personallevelingsystem.ui.compose.theme.SignalCyan
import com.example.personallevelingsystem.ui.compose.theme.TextSecondary
import com.example.personallevelingsystem.ui.compose.theme.TextTertiary
import com.example.personallevelingsystem.ui.compose.theme.tabular

/** Big tabular stopwatch readout for the flexibility and endurance timers. */
@Composable
fun ArcTimerCard(elapsedMs: Long, running: Boolean, modifier: Modifier = Modifier) {
    val seconds = (elapsedMs / 1000) % 60
    val minutes = (elapsedMs / (1000 * 60)) % 60
    val hours = elapsedMs / (1000 * 60 * 60)
    val status = when {
        running -> "RUNNING"
        elapsedMs > 0L -> "PAUSED"
        else -> "READY"
    }

    ArcCard(
        modifier = modifier.fillMaxWidth(),
        accent = if (running) SignalCyan else null,
        contentPadding = PaddingValues(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Spacer(
                modifier = Modifier
                    .size(8.dp)
                    .background(if (running) SignalCyan else TextTertiary, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = status,
                style = MaterialTheme.typography.labelMedium,
                color = if (running) SignalCyan else TextSecondary,
                letterSpacing = 1.5.sp
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "%02d:%02d:%02d".format(hours, minutes, seconds),
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 60.sp).tabular,
            color = HologramText
        )
    }
}
