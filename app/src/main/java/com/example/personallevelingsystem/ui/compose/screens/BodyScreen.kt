package com.example.personallevelingsystem.ui.compose.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.personallevelingsystem.R
import com.example.personallevelingsystem.ui.compose.components.ArcListRow
import com.example.personallevelingsystem.ui.compose.components.ArcSectionLabel
import com.example.personallevelingsystem.ui.compose.theme.AlertOrange
import com.example.personallevelingsystem.ui.compose.theme.DeepViolet
import com.example.personallevelingsystem.ui.compose.theme.DesignSystem
import com.example.personallevelingsystem.ui.compose.theme.SignalCyan

@Composable
fun BodyScreen(onNavigate: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(DesignSystem.Padding),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ArcSectionLabel(text = stringResource(R.string.body_daily_logs))
        ArcListRow(
            title = stringResource(R.string.chrome_water),
            subtitle = stringResource(R.string.body_hydration_sub),
            icon = Icons.Rounded.WaterDrop,
            tint = SignalCyan,
            onClick = { onNavigate("water") }
        )
        ArcListRow(
            title = stringResource(R.string.chrome_sleep),
            subtitle = stringResource(R.string.body_sleep_sub),
            icon = Icons.Rounded.Bedtime,
            tint = DeepViolet,
            onClick = { onNavigate("sleep") }
        )
        ArcListRow(
            title = stringResource(R.string.chrome_nutrition),
            subtitle = stringResource(R.string.body_nutrition_sub),
            icon = Icons.Rounded.Restaurant,
            tint = AlertOrange,
            onClick = { onNavigate("nutrition") }
        )
    }
}
