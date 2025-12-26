package com.example.personallevelingsystem.ui.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.personallevelingsystem.data.AppDatabase
import com.example.personallevelingsystem.repository.UserRepository
import com.example.personallevelingsystem.ui.compose.screens.MainScreen
import com.example.personallevelingsystem.ui.compose.screens.MissionsListScreen
import com.example.personallevelingsystem.ui.compose.screens.UserProfileScreen
import com.example.personallevelingsystem.ui.compose.theme.PersonalLevelingSystemTheme
import com.example.personallevelingsystem.viewmodel.MissionViewModel
import com.example.personallevelingsystem.ui.compose.screens.TrainingSessionScreen
import com.example.personallevelingsystem.ui.compose.screens.TrainingScreen
import com.example.personallevelingsystem.viewmodel.UserViewModel

class ComposeMigrationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PersonalLevelingSystemTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "main") {
                    composable("main") {
                        MainScreen(
                            onNavigate = { destination ->
                                navController.navigate(destination)
                            }
                        )
                    }
                    composable("profile") {
                        val viewModel = ViewModelProvider(
                            this@ComposeMigrationActivity,
                            MigrationViewModelFactory(application)
                        )[UserViewModel::class.java]
                        
                        UserProfileScreen(
                            viewModel = viewModel,
                            onBackClick = { navController.popBackStack() },
                            onModifyClick = { /* Navigate to modify */ }
                        )
                    }
                    composable("missions") {
                        val viewModel = ViewModelProvider(
                            this@ComposeMigrationActivity,
                            MigrationViewModelFactory(application)
                        )[MissionViewModel::class.java]

                        MissionsListScreen(
                            viewModel = viewModel,
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                    // Add other screens as needed
                    composable("training") {
                        TrainingScreen(
                            onCreateProgramClick = { /* TODO */ },
                            onViewProgramsClick = { /* TODO */ },
                            onStartProgramClick = { navController.navigate("training_session") },
                            onStartFlexibilityClick = { /* TODO */ },
                            onStartEnduranceClick = { /* TODO */ },
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                    composable("training_session") {
                        TrainingSessionScreen(
                            onNextExercise = { /* Loop or Finish */ },
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}

class MigrationViewModelFactory(private val application: android.app.Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val database = AppDatabase.getDatabase(application)
        val userRepository = UserRepository(database.userDao(), application)

        return when {
            modelClass.isAssignableFrom(UserViewModel::class.java) -> {
                UserViewModel(userRepository) as T
            }
            modelClass.isAssignableFrom(MissionViewModel::class.java) -> {
                MissionViewModel(application, userRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
