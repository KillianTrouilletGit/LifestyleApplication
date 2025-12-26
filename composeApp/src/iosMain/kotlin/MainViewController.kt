package com.example.personallevelingsystem

import androidx.compose.ui.window.ComposeUIViewController
import com.example.personallevelingsystem.ui.compose.screens.MainScreen
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController {
    // Need to provide ViewModel or Mock
    // For now, passing empty lambda for navigation and mock/null for ViewModel?
    // MainScreen uses PerformanceViewModel. Ideally we use Koin to inject it.
    // Assuming refactor to Koin or hoisting state.
    // For scaffolding:
    /*
    MainScreen(
        onNavigate = {},
        performanceViewModel = koinInject() // or similar
    )
    */
    // Placeholder to make it compile:
    // TODO: Setup Koin or Dependency Injection properly
}
