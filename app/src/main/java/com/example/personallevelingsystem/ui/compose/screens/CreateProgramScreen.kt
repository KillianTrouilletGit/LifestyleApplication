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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.personallevelingsystem.R
import com.example.personallevelingsystem.ui.compose.components.JuicyButton
import com.example.personallevelingsystem.ui.compose.components.JuicyCard
import com.example.personallevelingsystem.ui.compose.components.JuicyInput
import com.example.personallevelingsystem.ui.compose.components.OperatorHeader
import com.example.personallevelingsystem.ui.compose.theme.DesignSystem
import com.example.personallevelingsystem.ui.compose.theme.PersonalLevelingSystemTheme
import com.example.personallevelingsystem.viewmodel.TrainingViewModel

@Composable
fun CreateProgramScreen(
    viewModel: TrainingViewModel,
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    var programName by remember { mutableStateOf("") }
    // Using simple data classes for UI state locally to simplify manipulation
    // We map them to ViewModel's NewSession/NewExercise on save
    val sessions = remember { mutableStateListOf<UiSession>() }

    CreateProgramContent(
        programName = programName,
        onProgramNameChange = { programName = it },
        sessions = sessions,
        onAddSession = { sessions.add(UiSession()) },
        onRemoveSession = { sessions.removeAt(it) },
        onUpdateSessionName = { index, name -> sessions[index] = sessions[index].copy(name = name) },
        onAddExercise = { sessionIndex ->
            val exercises = sessions[sessionIndex].exercises.toMutableList()
            exercises.add(UiExercise())
            sessions[sessionIndex] = sessions[sessionIndex].copy(exercises = exercises)
        },
        onRemoveExercise = { sessionIndex, exerciseIndex ->
            val exercises = sessions[sessionIndex].exercises.toMutableList()
            exercises.removeAt(exerciseIndex)
            sessions[sessionIndex] = sessions[sessionIndex].copy(exercises = exercises)
        },
        onUpdateExercise = { sessionIndex, exerciseIndex, name, sets ->
            val exercises = sessions[sessionIndex].exercises.toMutableList()
            exercises[exerciseIndex] = exercises[exerciseIndex].copy(name = name, sets = sets)
            sessions[sessionIndex] = sessions[sessionIndex].copy(exercises = exercises)
        },
        onSave = {
             var isValid = true
             if (programName.isBlank()) isValid = false
             if (sessions.isEmpty()) isValid = false
             
             sessions.forEach { session ->
                 if (session.name.isBlank()) isValid = false
                 if (session.exercises.isEmpty()) isValid = false
                 session.exercises.forEach { ex ->
                     if (ex.name.isBlank()) isValid = false
                 } 
             }

             if (isValid) {
                 val newSessions = sessions.map { uiSession ->
                     TrainingViewModel.NewSession(
                        name = uiSession.name,
                        exercises = uiSession.exercises.map { uiEx ->
                            TrainingViewModel.NewExercise(uiEx.name, uiEx.sets)
                        }.toMutableList()
                     )
                 }
                 viewModel.saveProgram(programName, newSessions)
                 onSaveSuccess()
             }
        },
        onBackClick = onBackClick
    )
}

data class UiSession(
    var name: String = "",
    var exercises: List<UiExercise> = emptyList()
)

data class UiExercise(
    var name: String = "",
    var sets: String = ""
)

@Composable
fun CreateProgramContent(
    programName: String,
    onProgramNameChange: (String) -> Unit,
    sessions: List<UiSession>,
    onAddSession: () -> Unit,
    onRemoveSession: (Int) -> Unit,
    onUpdateSessionName: (Int, String) -> Unit,
    onAddExercise: (Int) -> Unit,
    onRemoveExercise: (Int, Int) -> Unit,
    onUpdateExercise: (Int, Int, String, String) -> Unit,
    onSave: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(DesignSystem.Padding)
    ) {
        OperatorHeader(subtitle = "Architect", title = "Create Program")
        
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                JuicyInput(
                    value = programName,
                    onValueChange = onProgramNameChange,
                    placeholder = "PROGRAM NAME",
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                )
            }

            itemsIndexed(sessions) { sessionIndex, session ->
                JuicyCard(onClick = {}) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            JuicyInput(
                                value = session.name,
                                onValueChange = { onUpdateSessionName(sessionIndex, it) },
                                placeholder = "SESSION NAME (e.g. Legs)",
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { onRemoveSession(sessionIndex) }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_close), // Assuming ic_close exists
                                    contentDescription = "Remove Session",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Exercises
                        session.exercises.forEachIndexed { exerciseIndex, exercise ->
                            Row(
                                modifier = Modifier.padding(bottom = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                JuicyInput(
                                    value = exercise.name,
                                    onValueChange = { onUpdateExercise(sessionIndex, exerciseIndex, it, exercise.sets) },
                                    placeholder = "EXERCISE",
                                    modifier = Modifier.weight(2f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                JuicyInput(
                                    value = exercise.sets,
                                    onValueChange = { onUpdateExercise(sessionIndex, exerciseIndex, exercise.name, it) },
                                    placeholder = "SETS",
                                    keyboardType = KeyboardType.Number,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { onRemoveExercise(sessionIndex, exerciseIndex) }) {
                                     Icon(
                                        painter = painterResource(id = R.drawable.ic_close),
                                        contentDescription = "Remove Exercise",
                                        tint = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            }
                        }
                        
                        JuicyButton(
                            text = "+ ADD EXERCISE",
                            onClick = { onAddExercise(sessionIndex) },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        )
                    }
                }
            }
            
            item {
                JuicyButton(
                    text = "+ ADD SESSION",
                    onClick = onAddSession,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        JuicyButton(
            text = "SAVE PROGRAM",
            onClick = onSave,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        JuicyButton(
            text = "ABORT / RETURN",
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CreateProgramPreview() {
    PersonalLevelingSystemTheme {
        CreateProgramContent(
            programName = "Test Program",
            onProgramNameChange = {},
            sessions = listOf(
                UiSession("Day 1", listOf(UiExercise("Squats", "5")))
            ),
            onAddSession = {},
            onRemoveSession = {},
            onUpdateSessionName = {_,_->},
            onAddExercise = {},
            onRemoveExercise = {_,_ ->},
            onUpdateExercise = {_,_,_,_ ->},
            onSave = {},
            onBackClick = {}
        )
    }
}
