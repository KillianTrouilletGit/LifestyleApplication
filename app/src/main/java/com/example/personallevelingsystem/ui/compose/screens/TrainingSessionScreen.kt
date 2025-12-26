package com.example.personallevelingsystem.ui.compose.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personallevelingsystem.ui.compose.components.JuicyButton
import com.example.personallevelingsystem.ui.compose.theme.PersonalLevelingSystemTheme
import com.example.personallevelingsystem.ui.compose.theme.ProtocolCyan
import kotlinx.coroutines.delay

data class TrainingSetState(
    var reps: String = "",
    var weight: String = "",
    val previousReps: Int = 0,
    val previousWeight: Float = 0f
)

@Composable
fun TrainingSessionScreen(
    sessionName: String = "Test Session", // Should come from VM
    exerciseName: String = "Bench Press", // Should come from VM
    setsCount: Int = 3,
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

    // Mock State for Sets
    val sets = remember {
        mutableStateListOf<TrainingSetState>().apply {
            repeat(setsCount) {
                add(TrainingSetState())
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Timer Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp, bottom = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = formatTime(timeMillis),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Exercise Name
        Text(
            text = exerciseName,
            style = MaterialTheme.typography.headlineSmall,
            color = ProtocolCyan,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Sets List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            itemsIndexed(sets) { index, set ->
                TrainingSetRow(
                    index = index + 1,
                    state = set,
                    onRepsChange = { sets[index] = set.copy(reps = it) },
                    onWeightChange = { sets[index] = set.copy(weight = it) }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
                JuicyButton(
                    onClick = onNextExercise,
                    text = "NEXT EXERCISE",
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                JuicyButton(
                    onClick = onBackClick,
                    text = "END SESSION", // Simulating 'finish' or 'abort'
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
            color = ProtocolCyan,
            modifier = Modifier.weight(0.8f)
        )

        // Previous stats placeholder
        if (state.previousReps > 0) {
            Text(
                text = "PREV: ${state.previousReps}x${state.previousWeight}kg",
                style = MaterialTheme.typography.labelSmall,
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
        placeholder = { Text(placeholder, style = MaterialTheme.typography.bodySmall) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ProtocolCyan,
            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
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
        TrainingSessionScreen(
            onNextExercise = {},
            onBackClick = {}
        )
    }
}
