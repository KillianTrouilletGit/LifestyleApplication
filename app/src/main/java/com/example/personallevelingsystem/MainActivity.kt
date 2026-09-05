package com.example.personallevelingsystem

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.personallevelingsystem.data.AppDatabase
import com.example.personallevelingsystem.repository.UserRepository
import com.example.personallevelingsystem.ui.compose.components.AmbientBackground
import com.example.personallevelingsystem.ui.compose.components.ArcBottomBar
import com.example.personallevelingsystem.ui.compose.components.ArcTab
import com.example.personallevelingsystem.ui.compose.components.ArcTopBar
import com.example.personallevelingsystem.ui.compose.components.ScreenChromes
import com.example.personallevelingsystem.ui.compose.screens.BodyScreen
import com.example.personallevelingsystem.ui.compose.screens.CreateProgramScreen
import com.example.personallevelingsystem.ui.compose.screens.EnduranceScreen
import com.example.personallevelingsystem.ui.compose.screens.FlexibilityScreen
import com.example.personallevelingsystem.ui.compose.screens.MainScreen
import com.example.personallevelingsystem.ui.compose.screens.MissionsListScreen
import com.example.personallevelingsystem.ui.compose.screens.ModifyUserInfoScreen
import com.example.personallevelingsystem.ui.compose.screens.NutritionScreen
import com.example.personallevelingsystem.ui.compose.screens.PlanningScreen
import com.example.personallevelingsystem.ui.compose.screens.ReminderSettingsScreen
import com.example.personallevelingsystem.ui.compose.screens.SelectSessionScreen
import com.example.personallevelingsystem.ui.compose.screens.SleepScreen
import com.example.personallevelingsystem.ui.compose.screens.SplashScreen
import com.example.personallevelingsystem.ui.compose.screens.StyleLabScreen
import com.example.personallevelingsystem.ui.compose.screens.TrainingScreen
import com.example.personallevelingsystem.ui.compose.screens.TrainingSessionScreen
import com.example.personallevelingsystem.ui.compose.screens.UserProfileScreen
import com.example.personallevelingsystem.ui.compose.screens.ViewProgramsScreen
import com.example.personallevelingsystem.ui.compose.screens.WaterScreen
import com.example.personallevelingsystem.ui.compose.theme.ArcStyle
import com.example.personallevelingsystem.ui.compose.theme.Motion
import com.example.personallevelingsystem.ui.compose.theme.PersonalLevelingSystemTheme
import com.example.personallevelingsystem.viewmodel.FoodAnalysisViewModel
import com.example.personallevelingsystem.viewmodel.HealthViewModel
import com.example.personallevelingsystem.viewmodel.MissionViewModel
import com.example.personallevelingsystem.viewmodel.PerformanceViewModel
import com.example.personallevelingsystem.viewmodel.TrainingViewModel
import com.example.personallevelingsystem.viewmodel.UserViewModel

private val TabRoutes = ArcTab.entries.map { it.route }.toSet()

class MainActivity : ComponentActivity() {
    @SuppressLint("ComposableDestinationInComposeScope")
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)
        ArcStyle.load(this)

        setContent {
            PersonalLevelingSystemTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val chrome = ScreenChromes[currentRoute]
                val showBars = chrome?.showBars ?: true
                val currentTab = chrome?.tab

                fun popBackStackSafe() {
                    if (navBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                        navController.popBackStack()
                    }
                }

                fun openTab(tab: ArcTab) {
                    navController.navigate(tab.route) {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(ArcTab.Home.route) { saveState = true }
                    }
                }

                fun viewModels() = MigrationViewModelFactory(application)

                Box(modifier = Modifier.fillMaxSize()) {
                    if (currentRoute != "splash") {
                        AmbientBackground()
                    }

                    Scaffold(
                        containerColor = Color.Transparent,
                        contentWindowInsets = if (showBars) ScaffoldDefaults.contentWindowInsets else WindowInsets(0, 0, 0, 0),
                        topBar = {
                            if (showBars && chrome != null) {
                                ArcTopBar(
                                    chrome = chrome,
                                    onBack = if (currentTab == null) ({ popBackStackSafe() }) else null,
                                    onAction = { navController.navigate(it.route) },
                                    onProfile = { navController.navigate("profile") },
                                    onSettings = { navController.navigate("settings") }
                                )
                            }
                        },
                        bottomBar = {
                            if (showBars && currentTab != null) {
                                ArcBottomBar(current = currentTab, onSelect = { openTab(it) })
                            }
                        }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = "splash",
                            modifier = Modifier
                                .padding(innerPadding)
                                .consumeWindowInsets(innerPadding)
                                .imePadding(),
                            enterTransition = {
                                val tabToTab = initialState.destination.route in TabRoutes &&
                                    targetState.destination.route in TabRoutes
                                if (tabToTab) fadeIn(tween(Motion.Standard))
                                else fadeIn(tween(Motion.Standard)) + slideInHorizontally(tween(Motion.Standard)) { it / 12 }
                            },
                            exitTransition = { fadeOut(tween(Motion.Quick)) },
                            popEnterTransition = {
                                fadeIn(tween(Motion.Standard)) + slideInHorizontally(tween(Motion.Standard)) { -it / 12 }
                            },
                            popExitTransition = {
                                fadeOut(tween(Motion.Quick)) + slideOutHorizontally(tween(Motion.Standard)) { it / 12 }
                            }
                        ) {
                            composable("splash") {
                                SplashScreen(
                                    onAnimationFinished = {
                                        navController.navigate("main") {
                                            popUpTo("splash") { inclusive = true }
                                        }
                                        // Honor deeplink_route from notification taps
                                        intent?.getStringExtra("deeplink_route")?.takeIf { it.isNotBlank() }?.let { route ->
                                            navController.navigate(route)
                                            intent.removeExtra("deeplink_route")
                                        }
                                    }
                                )
                            }
                            composable("main") {
                                // Android 13+ only shows notifications once the user has
                                // granted POST_NOTIFICATIONS at runtime — ask on first landing.
                                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                                    ActivityResultContracts.RequestPermission()
                                ) { /* granted or not, reminders check the permission themselves */ }
                                LaunchedEffect(Unit) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                        ContextCompat.checkSelfPermission(
                                            this@MainActivity, Manifest.permission.POST_NOTIFICATIONS
                                        ) != PackageManager.PERMISSION_GRANTED
                                    ) {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                }

                                val performanceViewModel = ViewModelProvider(this@MainActivity, viewModels())[PerformanceViewModel::class.java]
                                val healthViewModel = ViewModelProvider(this@MainActivity, viewModels())[HealthViewModel::class.java]
                                val missionViewModel = ViewModelProvider(this@MainActivity, viewModels())[MissionViewModel::class.java]

                                MainScreen(
                                    onNavigate = { destination -> navController.navigate(destination) },
                                    performanceViewModel = performanceViewModel,
                                    healthViewModel = healthViewModel,
                                    missionViewModel = missionViewModel
                                )
                            }
                            composable("missions") {
                                val viewModel = ViewModelProvider(this@MainActivity, viewModels())[MissionViewModel::class.java]
                                MissionsListScreen(
                                    viewModel = viewModel,
                                    onDeeplink = { route -> navController.navigate(route) }
                                )
                            }
                            composable("training") {
                                TrainingScreen(
                                    onCreateProgramClick = { navController.navigate("create_program") },
                                    onViewProgramsClick = { navController.navigate("view_programs") },
                                    onStartProgramClick = { navController.navigate("select_session") },
                                    onStartFlexibilityClick = { navController.navigate("flexibility") },
                                    onStartEnduranceClick = { navController.navigate("endurance") }
                                )
                            }
                            composable("body") {
                                BodyScreen(onNavigate = { navController.navigate(it) })
                            }
                            composable("profile") {
                                val viewModel = ViewModelProvider(this@MainActivity, viewModels())[UserViewModel::class.java]
                                UserProfileScreen(
                                    viewModel = viewModel,
                                    onModifyClick = { navController.navigate("modify_user") }
                                )
                            }
                            composable("modify_user") {
                                val viewModel = ViewModelProvider(this@MainActivity, viewModels())[UserViewModel::class.java]
                                ModifyUserInfoScreen(
                                    viewModel = viewModel,
                                    onSaveClick = { navController.popBackStack() }
                                )
                            }
                            composable("settings") {
                                ReminderSettingsScreen(onOpenStyleLab = { navController.navigate("style_lab") })
                            }
                            composable("style_lab") {
                                StyleLabScreen()
                            }
                            composable("planning") {
                                val viewModel = ViewModelProvider(this@MainActivity, viewModels())[MissionViewModel::class.java]
                                PlanningScreen(missionViewModel = viewModel)
                            }
                            composable(
                                "training_session/{sessionId}",
                                arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
                            ) { backStackEntry ->
                                val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: -1L
                                val viewModel = ViewModelProvider(this@MainActivity, viewModels())[TrainingViewModel::class.java]

                                LaunchedEffect(sessionId) {
                                    if (sessionId != -1L) {
                                        viewModel.startSession(sessionId)
                                    }
                                }

                                TrainingSessionScreen(
                                    viewModel = viewModel,
                                    onBackClick = { popBackStackSafe() }
                                )
                            }
                            composable("select_session") {
                                val viewModel = ViewModelProvider(this@MainActivity, viewModels())[TrainingViewModel::class.java]
                                val programs by viewModel.programs.observeAsState(initial = emptyList())

                                LaunchedEffect(Unit) {
                                    viewModel.loadPrograms()
                                }

                                SelectSessionScreen(
                                    programs = programs,
                                    onSessionClick = { sessionId ->
                                        navController.navigate("training_session/$sessionId")
                                    }
                                )
                            }
                            composable("view_programs") {
                                val viewModel = ViewModelProvider(this@MainActivity, viewModels())[TrainingViewModel::class.java]
                                val programs by viewModel.programs.observeAsState(initial = emptyList())
                                val exerciseHistory by viewModel.exerciseHistory.observeAsState(initial = emptyList())

                                LaunchedEffect(Unit) {
                                    viewModel.loadPrograms()
                                }

                                ViewProgramsScreen(
                                    programs = programs,
                                    exerciseHistory = exerciseHistory,
                                    onExerciseClick = { exercise -> viewModel.loadExerciseHistory(exercise.id) },
                                    onDeleteProgram = { viewModel.deleteProgram(it.program) }
                                )
                            }
                            composable("water") {
                                val viewModel = ViewModelProvider(this@MainActivity, viewModels())[HealthViewModel::class.java]
                                WaterScreen(viewModel = viewModel)
                            }
                            composable("sleep") {
                                val viewModel = ViewModelProvider(this@MainActivity, viewModels())[HealthViewModel::class.java]
                                SleepScreen(
                                    viewModel = viewModel,
                                    onBackClick = { popBackStackSafe() }
                                )
                            }
                            composable("nutrition") {
                                val healthViewModel = ViewModelProvider(this@MainActivity, viewModels())[HealthViewModel::class.java]
                                val foodAnalysisViewModel = ViewModelProvider(this@MainActivity)[FoodAnalysisViewModel::class.java]
                                NutritionScreen(
                                    viewModel = healthViewModel,
                                    foodAnalysisViewModel = foodAnalysisViewModel
                                )
                            }
                            composable("flexibility") {
                                val viewModel = ViewModelProvider(this@MainActivity, viewModels())[TrainingViewModel::class.java]
                                FlexibilityScreen(
                                    viewModel = viewModel,
                                    onBackClick = { popBackStackSafe() }
                                )
                            }
                            composable("endurance") {
                                val viewModel = ViewModelProvider(this@MainActivity, viewModels())[TrainingViewModel::class.java]
                                EnduranceScreen(
                                    viewModel = viewModel,
                                    onBackClick = { popBackStackSafe() }
                                )
                            }
                            composable("create_program") {
                                val viewModel = ViewModelProvider(this@MainActivity, viewModels())[TrainingViewModel::class.java]
                                CreateProgramScreen(
                                    viewModel = viewModel,
                                    onSaveSuccess = { popBackStackSafe() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        try {
            val viewModel = ViewModelProvider(
                this,
                MigrationViewModelFactory(application)
            )[TrainingViewModel::class.java]
            viewModel.ensureTimerNotification()
        } catch (e: Exception) {
            e.printStackTrace()
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
                MissionViewModel(application) as T
            }
            modelClass.isAssignableFrom(TrainingViewModel::class.java) -> {
                TrainingViewModel(application) as T
            }
            modelClass.isAssignableFrom(HealthViewModel::class.java) -> {
                HealthViewModel(application) as T
            }
            modelClass.isAssignableFrom(PerformanceViewModel::class.java) -> {
                PerformanceViewModel(application) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
