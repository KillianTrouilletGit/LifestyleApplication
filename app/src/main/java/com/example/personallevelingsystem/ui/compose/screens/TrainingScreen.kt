package com.example.personallevelingsystem.ui.compose.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.automirrored.rounded.ListAlt
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.personallevelingsystem.R
import com.example.personallevelingsystem.ui.compose.components.ArcButton
import com.example.personallevelingsystem.ui.compose.components.ArcListRow
import com.example.personallevelingsystem.ui.compose.components.ArcSectionLabel
import com.example.personallevelingsystem.ui.compose.theme.DesignSystem
import com.example.personallevelingsystem.ui.compose.theme.PersonalLevelingSystemTheme
import com.example.personallevelingsystem.ui.compose.theme.SignalCyan

@Composable
fun TrainingScreen(
    onCreateProgramClick: () -> Unit,
    onViewProgramsClick: () -> Unit,
    onStartProgramClick: () -> Unit,
    onStartFlexibilityClick: () -> Unit,
    onStartEnduranceClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(DesignSystem.Padding)
    ) {
        ArcButton(
            text = stringResource(R.string.train_start_session),
            subtitle = stringResource(R.string.train_start_session_sub),
            icon = Icons.Rounded.PlayArrow,
            onClick = onStartProgramClick,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        ArcSectionLabel(text = stringResource(R.string.train_programs))
        ArcListRow(
            title = stringResource(R.string.train_archive),
            subtitle = stringResource(R.string.train_archive_sub),
            icon = Icons.AutoMirrored.Rounded.ListAlt,
            onClick = onViewProgramsClick
        )
        Spacer(modifier = Modifier.height(10.dp))
        ArcListRow(
            title = stringResource(R.string.train_create),
            subtitle = stringResource(R.string.train_create_sub),
            icon = Icons.Rounded.Add,
            onClick = onCreateProgramClick
        )

        Spacer(modifier = Modifier.height(24.dp))

        ArcSectionLabel(text = stringResource(R.string.train_conditioning))
        ArcListRow(
            title = stringResource(R.string.chrome_flexibility),
            subtitle = stringResource(R.string.train_flex_sub),
            icon = Icons.Rounded.SelfImprovement,
            tint = SignalCyan,
            onClick = onStartFlexibilityClick
        )
        Spacer(modifier = Modifier.height(10.dp))
        ArcListRow(
            title = stringResource(R.string.chrome_endurance),
            subtitle = stringResource(R.string.train_endurance_sub),
            icon = Icons.AutoMirrored.Rounded.DirectionsRun,
            tint = SignalCyan,
            onClick = onStartEnduranceClick
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TrainingScreenPreview() {
    PersonalLevelingSystemTheme {
        TrainingScreen({}, {}, {}, {}, {})
    }
}
