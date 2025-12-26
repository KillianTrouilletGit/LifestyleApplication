package com.example.personallevelingsystem.ui.compose.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.personallevelingsystem.ui.compose.components.JuicyButton
import com.example.personallevelingsystem.ui.compose.theme.DesignSystem
import com.example.personallevelingsystem.ui.compose.theme.PersonalLevelingSystemTheme
import kotlinx.coroutines.delay

import androidx.compose.runtime.livedata.observeAsState
import com.example.personallevelingsystem.viewmodel.TrainingViewModel.TrainingSetState

@Composable
fun TrainingSessionScreen(
    viewModel: com.example.personallevelingsystem.viewmodel.TrainingViewModel,
    onBackClick: () -> Unit
) {
    val currentExercises by viewModel.currentExercises.observeAsState(initial = emptyList())
    val currentIndex by viewModel.currentExerciseIndex.observeAsState(initial = 0)
    val currentSets by viewModel.currentSets.observeAsState(initial = emptyList())
    
    // Navigate back when session is finished
    val sessionFinished by viewModel.sessionFinished.observeAsState(initial = false)
    LaunchedEffect(sessionFinished) {
        if (sessionFinished) {
            onBackClick()
        }
    }
    
    TrainingSessionContent(
        currentExercises = currentExercises,
        currentIndex = currentIndex,
        currentSets = currentSets,
        onRepsChange = { index, newValue -> 
            val updatedList = currentSets.toMutableList()
            updatedList[index] = currentSets[index].copy(reps = newValue)
            viewModel.saveCurrentSetState(updatedList)
        },
        onWeightChange = { index, newValue ->
            val updatedList = currentSets.toMutableList()
            updatedList[index] = currentSets[index].copy(weight = newValue)
            viewModel.saveCurrentSetState(updatedList)
        },
        onNextExercise = { viewModel.nextExercise() },
        onBackClick = {
            viewModel.stopTimerService()
            onBackClick()
        }
    )
}

@Composable
fun TrainingSessionContent(
    currentExercises: List<com.example.personallevelingsystem.model.Exercise>,
    currentIndex: Int,
    currentSets: List<TrainingSetState>,
    onRepsChange: (Int, String) -> Unit,
    onWeightChange: (Int, String) -> Unit,
    onNextExercise: () -> Unit,
    onBackClick: () -> Unit
) {

    
    // Timer Logic
    var timeMillis by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        val startTime = System.currentTimeMillis()
        while (true) {
            timeMillis = System.currentTimeMillis() - startTime
            delay(1000)
        }
    }

    if (currentExercises.isEmpty()) {
        // Loading or Empty State
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No Exercises Found or Loading...", color = MaterialTheme.colorScheme.primary)
            // Ideally provide a back button here too if stuck
             JuicyButton(
                onClick = onBackClick,
                text = "GO BACK",
                modifier = Modifier.padding(top = 100.dp)
            )
        }
        return
    }

    val currentExercise = currentExercises.getOrNull(currentIndex) ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(DesignSystem.Padding)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = formatTime(timeMillis),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = currentExercise.name.uppercase(),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            itemsIndexed(currentSets) { index, setState ->
                TrainingSetRow(
                    index = index + 1,
                    state = setState,
                    onRepsChange = { newValue -> onRepsChange(index, newValue) },
                    onWeightChange = { newValue -> onWeightChange(index, newValue) }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
                JuicyButton(
                    onClick = onNextExercise,
                    text = if (currentIndex < currentExercises.size - 1) "NEXT EXERCISE" else "FINISH SESSION",
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                JuicyButton(
                    onClick = onBackClick,
                    text = "END SESSION",
                    modifier = Modifier.fillMaxWidth() 
                )
            }
        }
    }
}

@Composable
fun TrainingSetRow(
    index: Int,
    state: TrainingSetState,
    onRepsChange: (String) -> Unit,
    onWeightChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "SET $index",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary, // Neon Blue
            modifier = Modifier.weight(0.8f)
        )

        if (state.previousReps > 0) {
            Text(
                text = "PREV: ${state.previousReps}x${state.previousWeight}kg",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1.2f)
            )
        } else {
             Spacer(modifier = Modifier.weight(1.2f))
        }

        CustomInput(
            value = state.reps,
            onValueChange = onRepsChange,
            placeholder = "REPS",
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        )

        CustomInput(
            value = state.weight,
            onValueChange = onWeightChange,
            placeholder = "KG",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun CustomInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyMedium) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent
        ),
        modifier = modifier
    )
}

fun formatTime(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    val hours = (millis / (1000 * 60 * 60))
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

@Preview(showBackground = true)
@Composable
fun TrainingSessionPreview() {
    PersonalLevelingSystemTheme {
        TrainingSessionContent(
            currentExercises = listOf(
                com.example.personallevelingsystem.model.Exercise(name = "Bench Press", sessionId = 1, sets = 3)
            ),
            currentIndex = 0,
            currentSets = listOf(
                TrainingSetState(reps = "10", weight = "100", previousReps = 10, previousWeight = 95f),
                TrainingSetState(reps = "8", weight = "105", previousReps = 8, previousWeight = 100f),
                TrainingSetState(reps = "6", weight = "110", previousReps = 6, previousWeight = 105f)
            ),
            onRepsChange = { _, _ -> },
            onWeightChange = { _, _ -> },
            onNextExercise = {},
            onBackClick = {}
        )
    }
}
