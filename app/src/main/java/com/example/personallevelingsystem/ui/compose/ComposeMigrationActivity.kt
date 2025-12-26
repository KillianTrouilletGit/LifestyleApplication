package com.example.personallevelingsystem.ui.compose

import android.annotation.SuppressLint
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


class ComposeMigrationActivity : ComponentActivity() {
    @SuppressLint("ComposableDestinationInComposeScope")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PersonalLevelingSystemTheme {
                val navController = rememberNavController()
                
                // Wrap content in Ambient Background
                Box(modifier = Modifier.fillMaxSize()) {
                    com.example.personallevelingsystem.ui.compose.components.AmbientBackground()
                    
                    NavHost(navController = navController, startDestination = "main") {
                    composable("main") {
                        val viewModel = ViewModelProvider(
                            this@ComposeMigrationActivity,
                            MigrationViewModelFactory(application)
                        )[com.example.personallevelingsystem.viewmodel.PerformanceViewModel::class.java]

                        MainScreen(
                            onNavigate = { destination ->
                                navController.navigate(destination)
                            },
                            performanceViewModel = viewModel
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
                            onModifyClick = { navController.navigate("modify_user") }
                        )
                    }
                    composable("modify_user") {
                        val viewModel = ViewModelProvider(
                            this@ComposeMigrationActivity,
                            MigrationViewModelFactory(application)
                        )[UserViewModel::class.java]
                        
                        com.example.personallevelingsystem.ui.compose.screens.ModifyUserInfoScreen(
                            viewModel = viewModel,
                            onSaveClick = { navController.popBackStack() },
                            onBackClick = { navController.popBackStack() }
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
                            onCreateProgramClick = { navController.navigate("create_program") },
                            onViewProgramsClick = { navController.navigate("view_programs") },
                            onStartProgramClick = { navController.navigate("select_session") },
                            onStartFlexibilityClick = { navController.navigate("flexibility") },
                            onStartEnduranceClick = { navController.navigate("endurance") },
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                    composable(
                        "training_session/{sessionId}",
                        arguments = listOf(androidx.navigation.navArgument("sessionId") { type = androidx.navigation.NavType.LongType })
                    ) { backStackEntry ->
                        val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: -1L
                        val viewModel = ViewModelProvider(
                            this@ComposeMigrationActivity,
                            MigrationViewModelFactory(application)
                        )[com.example.personallevelingsystem.viewmodel.TrainingViewModel::class.java]
                        
                        androidx.compose.runtime.LaunchedEffect(sessionId) {
                            if (sessionId != -1L) {
                                viewModel.startSession(sessionId)
                            }
                        }

                        com.example.personallevelingsystem.ui.compose.screens.TrainingSessionScreen(
                            viewModel = viewModel,
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                    composable("select_session") {
                        val viewModel = ViewModelProvider(
                            this@ComposeMigrationActivity,
                            MigrationViewModelFactory(application)
                        )[com.example.personallevelingsystem.viewmodel.TrainingViewModel::class.java]
                        
                        val sessions by viewModel.sessions.observeAsState(initial = emptyList())

                        androidx.compose.runtime.LaunchedEffect(Unit) {
                            viewModel.loadSessions()
                        }
                        
                        com.example.personallevelingsystem.ui.compose.screens.SelectSessionScreen(
                            sessions = sessions, 
                            onSessionClick = { sessionId -> 
                                navController.navigate("training_session/$sessionId") 
                            },
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                    composable("view_programs") {
                         val viewModel = ViewModelProvider(
                            this@ComposeMigrationActivity,
                            MigrationViewModelFactory(application)
                        )[com.example.personallevelingsystem.viewmodel.TrainingViewModel::class.java]

                        val programs by viewModel.programs.observeAsState(initial = emptyList())
                        
                        androidx.compose.runtime.LaunchedEffect(Unit) {
                            viewModel.loadPrograms()
                        }

                        com.example.personallevelingsystem.ui.compose.screens.ViewProgramsScreen(
                            programs = programs,
                            onDeleteProgram = { viewModel.deleteProgram(it.program) },
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                    composable("water") {
                        val viewModel = ViewModelProvider(
                            this@ComposeMigrationActivity,
                            MigrationViewModelFactory(application)
                        )[com.example.personallevelingsystem.viewmodel.HealthViewModel::class.java]
                        
                        com.example.personallevelingsystem.ui.compose.screens.WaterScreen(
                            viewModel = viewModel,
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                    composable("sleep") {
                         val viewModel = ViewModelProvider(
                            this@ComposeMigrationActivity,
                            MigrationViewModelFactory(application)
                        )[com.example.personallevelingsystem.viewmodel.HealthViewModel::class.java]
                        
                        com.example.personallevelingsystem.ui.compose.screens.SleepScreen(
                            viewModel = viewModel,
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                    composable("nutrition") {
                         val viewModel = ViewModelProvider(
                            this@ComposeMigrationActivity,
                            MigrationViewModelFactory(application)
                        )[com.example.personallevelingsystem.viewmodel.HealthViewModel::class.java]
                        
                        com.example.personallevelingsystem.ui.compose.screens.NutritionScreen(
                            viewModel = viewModel,
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                    composable("flexibility") {
                         val viewModel = ViewModelProvider(
                            this@ComposeMigrationActivity,
                            MigrationViewModelFactory(application)
                        )[com.example.personallevelingsystem.viewmodel.TrainingViewModel::class.java]
                        
                        com.example.personallevelingsystem.ui.compose.screens.FlexibilityScreen(
                            viewModel = viewModel,
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                    composable("endurance") {
                         val viewModel = ViewModelProvider(
                            this@ComposeMigrationActivity,
                            MigrationViewModelFactory(application)
                        )[com.example.personallevelingsystem.viewmodel.TrainingViewModel::class.java]
                        
                            
                        com.example.personallevelingsystem.ui.compose.screens.EnduranceScreen(
                            viewModel = viewModel,
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                    composable("create_program") {
                        val viewModel = ViewModelProvider(
                            this@ComposeMigrationActivity,
                            MigrationViewModelFactory(application)
                        )[com.example.personallevelingsystem.viewmodel.TrainingViewModel::class.java]
                        
                        com.example.personallevelingsystem.ui.compose.screens.CreateProgramScreen(
                            viewModel = viewModel,
                            onBackClick = { navController.popBackStack() },
                            onSaveSuccess = { 
                                navController.popBackStack() 
                            }
                        )
                    }
                    composable("settings") {
                        // Re-use Modify User for settings for now
                        val viewModel = ViewModelProvider(
                            this@ComposeMigrationActivity,
                            MigrationViewModelFactory(application)
                        )[UserViewModel::class.java]
                        
                        com.example.personallevelingsystem.ui.compose.screens.ModifyUserInfoScreen(
                            viewModel = viewModel,
                            onSaveClick = { navController.popBackStack() },
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                    composable("planning") {
                        val viewModel = ViewModelProvider(
                            this@ComposeMigrationActivity,
                            MigrationViewModelFactory(application)
                        )[MissionViewModel::class.java]

                        com.example.personallevelingsystem.ui.compose.screens.PlanningScreen(
                            missionViewModel = viewModel,
                            onBackClick = { navController.popBackStack() }
                        )
                    }
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
            modelClass.isAssignableFrom(com.example.personallevelingsystem.viewmodel.TrainingViewModel::class.java) -> {
                com.example.personallevelingsystem.viewmodel.TrainingViewModel(application) as T
            }
            modelClass.isAssignableFrom(com.example.personallevelingsystem.viewmodel.HealthViewModel::class.java) -> {
                com.example.personallevelingsystem.viewmodel.HealthViewModel(application) as T
            }
            modelClass.isAssignableFrom(com.example.personallevelingsystem.viewmodel.PerformanceViewModel::class.java) -> {
                com.example.personallevelingsystem.viewmodel.PerformanceViewModel(application) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
