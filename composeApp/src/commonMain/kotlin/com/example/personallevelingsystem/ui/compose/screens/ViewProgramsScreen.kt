package com.example.personallevelingsystem.ui.compose.screens

import androidx.compose.foundation.background
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.personallevelingsystem.R
import com.example.personallevelingsystem.model.Program
import com.example.personallevelingsystem.model.ProgramWithSessions
import com.example.personallevelingsystem.ui.compose.components.JuicyButton
import com.example.personallevelingsystem.ui.compose.components.JuicyCard
import com.example.personallevelingsystem.ui.compose.components.OperatorHeader
import com.example.personallevelingsystem.ui.compose.theme.DesignSystem
import com.example.personallevelingsystem.ui.compose.theme.PersonalLevelingSystemTheme
import com.example.personallevelingsystem.ui.compose.theme.PlacementSpring

import androidx.compose.foundation.ExperimentalFoundationApi

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ViewProgramsScreen(
    programs: List<ProgramWithSessions>, // Will come from ViewModel
    onDeleteProgram: (ProgramWithSessions) -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(DesignSystem.Padding)
    ) {
        OperatorHeader(subtitle = "Database", title = "Program Archive")

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items = programs, key = { it.program.id }) { programWithSessions ->
                ProgramItem(
                    program = programWithSessions.program,
                    sessionCount = programWithSessions.sessions.size,
                    onDelete = { onDeleteProgram(programWithSessions) },
                    modifier = Modifier.animateItemPlacement(PlacementSpring)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        JuicyButton(
            onClick = onBackClick,
            text = "RETURN TO TRAINING",
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ProgramItem(
    program: Program,
    sessionCount: Int,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    JuicyCard(
        onClick = {}, // Non-clickable for now, just informative/deletable
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = program.name.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$sessionCount SESSIONS LOGGED",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(onClick = onDelete) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_close), // Using close as delete for now
                    contentDescription = "Delete Program",
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ViewProgramsScreenPreview() {
    PersonalLevelingSystemTheme {
        ViewProgramsScreen(
            programs = listOf(
                ProgramWithSessions(
                    program = Program(id = 1, name = "Spartan Protocol"),
                    sessions = emptyList()
                )
            ),
            onDeleteProgram = {},
            onBackClick = {}
        )
    }
}
