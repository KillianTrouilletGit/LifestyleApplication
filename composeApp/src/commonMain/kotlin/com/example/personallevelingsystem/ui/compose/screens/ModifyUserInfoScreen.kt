package com.example.personallevelingsystem.ui.compose.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.personallevelingsystem.model.User
import com.example.personallevelingsystem.ui.compose.components.JuicyButton
import com.example.personallevelingsystem.ui.compose.components.JuicyInput
import com.example.personallevelingsystem.ui.compose.components.OperatorHeader
import com.example.personallevelingsystem.ui.compose.theme.DesignSystem
import com.example.personallevelingsystem.ui.compose.theme.PersonalLevelingSystemTheme
import com.example.personallevelingsystem.viewmodel.UserViewModel

@Composable
fun ModifyUserInfoScreen(
    viewModel: UserViewModel,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val user by viewModel.user.observeAsState()

    // Form state
    var name by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }

    // Initialize state when user loads
    LaunchedEffect(user) {
        user?.let {
            name = it.name
            weight = it.weight.toString()
            height = it.height.toString()
            dob = it.dateOfBirth
        }
    }

    LaunchedEffect(Unit) {
        if (user == null) {
            viewModel.getUserById(1)
        }
    }

    ModifyUserInfoContent(
        name = name,
        onNameChange = { name = it },
        weight = weight,
        onWeightChange = { weight = it },
        height = height,
        onHeightChange = { height = it },
        dob = dob,
        onDobChange = { dob = it },
        onSaveClick = {
             val updatedUser = user?.copy(
                 name = name,
                 weight = weight.toFloatOrNull() ?: 0f,
                 height = height.toFloatOrNull() ?: 0f,
                 dateOfBirth = dob
             ) ?: User(
                 id = 1,
                 name = name,
                 weight = weight.toFloatOrNull() ?: 0f,
                 height = height.toFloatOrNull() ?: 0f,
                 dateOfBirth = dob,
                 xp = 0,
                 level = 1
             )
             
             if (user != null) {
                 viewModel.updateUser(updatedUser)
             } else {
                 viewModel.insertUser(updatedUser)
             }
             onSaveClick()
        },
        onBackClick = onBackClick
    )
}

@Composable
fun ModifyUserInfoContent(
    name: String,
    onNameChange: (String) -> Unit,
    weight: String,
    onWeightChange: (String) -> Unit,
    height: String,
    onHeightChange: (String) -> Unit,
    dob: String,
    onDobChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(DesignSystem.Padding)
            .verticalScroll(rememberScrollState())
    ) {
        OperatorHeader(subtitle = "Credentials", title = "Update Info")

        Spacer(modifier = Modifier.height(24.dp))

        JuicyInput(
            value = name,
            onValueChange = onNameChange,
            placeholder = "FULL NAME",
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        JuicyInput(
            value = weight,
            onValueChange = onWeightChange,
            placeholder = "WEIGHT (KG)",
            modifier = Modifier.fillMaxWidth(),
            keyboardType = KeyboardType.Number
        )

        Spacer(modifier = Modifier.height(16.dp))

        JuicyInput(
            value = height,
            onValueChange = onHeightChange,
            placeholder = "HEIGHT (CM)",
            modifier = Modifier.fillMaxWidth(),
            keyboardType = KeyboardType.Number
        )

        Spacer(modifier = Modifier.height(16.dp))

        JuicyInput(
            value = dob,
            onValueChange = onDobChange,
            placeholder = "DATE OF BIRTH (YYYY-MM-DD)",
            modifier = Modifier.fillMaxWidth(),
            keyboardType = KeyboardType.Number
        )

        Spacer(modifier = Modifier.height(32.dp))

        JuicyButton(
            onClick = onSaveClick,
            text = "SAVE CHANGES",
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        JuicyButton(
            onClick = onBackClick,
            text = "CANCEL",
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ModifyUserInfoPreview() {
    PersonalLevelingSystemTheme {
        ModifyUserInfoContent(
            name = "John Doe",
            onNameChange = {},
            weight = "80.0",
            onWeightChange = {},
            height = "180.0",
            onHeightChange = {},
            dob = "1990-01-01",
            onDobChange = {},
            onSaveClick = {},
            onBackClick = {}
        )
    }
}
