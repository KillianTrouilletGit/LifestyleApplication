package com.example.personallevelingsystem.ui.compose.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personallevelingsystem.model.Program
import com.example.personallevelingsystem.model.ProgramWithSessions
import com.example.personallevelingsystem.model.SessionWithExercises
import com.example.personallevelingsystem.ui.compose.components.JuicyButton
import com.example.personallevelingsystem.ui.compose.components.JuicyCard
import com.example.personallevelingsystem.ui.compose.theme.AccentViolet
import com.example.personallevelingsystem.ui.compose.theme.DesignSystem
import com.example.personallevelingsystem.ui.compose.theme.PersonalLevelingSystemTheme
import com.example.personallevelingsystem.ui.compose.theme.PlacementSpring
import com.example.personallevelingsystem.ui.compose.theme.TextSecondary

import androidx.compose.foundation.ExperimentalFoundationApi

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SelectSessionScreen(
    programs: List<ProgramWithSessions>, // Will come from ViewModel
    onSessionClick: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(DesignSystem.Padding)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            programs.filter { it.sessions.isNotEmpty() }.forEach { programWithSessions ->
                item(key = "program_${programWithSessions.program.id}") {
                    ProgramSectionHeader(
                        name = programWithSessions.program.name,
                        sessionCount = programWithSessions.sessions.size
                    )
                }
                items(
                    items = programWithSessions.sessions,
                    key = { "session_${it.session.id}" }
                ) { sessionWithExercises ->
                    SessionItem(
                        sessionWithExercises = sessionWithExercises,
                        onClick = { onSessionClick(sessionWithExercises.session.id) },
                        modifier = Modifier.animateItem(placementSpec = PlacementSpring)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgramSectionHeader(name: String, sessionCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = AccentViolet,
            letterSpacing = 1.5.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "$sessionCount SESSIONS",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}

@Composable
fun SessionItem(
    sessionWithExercises: SessionWithExercises,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    JuicyCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sessionWithExercises.session.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${sessionWithExercises.exercises.size} exercises",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "START >",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SelectSessionScreenPreview() {
    PersonalLevelingSystemTheme {
        SelectSessionScreen(
            programs = listOf(
                ProgramWithSessions(
                    program = Program(id = 1, name = "Spartan Protocol"),
                    sessions = emptyList()
                )
            ),
            onSessionClick = {}
        )
    }
}
