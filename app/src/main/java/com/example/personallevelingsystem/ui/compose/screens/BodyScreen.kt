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
import androidx.compose.ui.unit.dp
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
        ArcSectionLabel(text = "Daily logs")
        ArcListRow(
            title = "Hydration",
            subtitle = "Quick-add and log intake against today's quota",
            icon = Icons.Rounded.WaterDrop,
            tint = SignalCyan,
            onClick = { onNavigate("water") }
        )
        ArcListRow(
            title = "Sleep",
            subtitle = "Log last night's rest cycle",
            icon = Icons.Rounded.Bedtime,
            tint = DeepViolet,
            onClick = { onNavigate("sleep") }
        )
        ArcListRow(
            title = "Nutrition",
            subtitle = "Meals, macros and AI food analysis",
            icon = Icons.Rounded.Restaurant,
            tint = AlertOrange,
            onClick = { onNavigate("nutrition") }
        )
    }
}
