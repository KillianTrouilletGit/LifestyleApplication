package com.example.personallevelingsystem.ui.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MonitorHeart
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personallevelingsystem.ui.compose.theme.AccentViolet
import com.example.personallevelingsystem.ui.compose.theme.BorderSubtle
import com.example.personallevelingsystem.ui.compose.theme.CrimsonRed
import com.example.personallevelingsystem.ui.compose.theme.HologramText
import com.example.personallevelingsystem.ui.compose.theme.PrimaryGradient
import com.example.personallevelingsystem.ui.compose.theme.SurfaceElevated
import com.example.personallevelingsystem.ui.compose.theme.TextSecondary

enum class ArcTab(val route: String, val label: String, val icon: ImageVector) {
    Home("main", "Home", Icons.Rounded.Home),
    Missions("missions", "Missions", Icons.Rounded.Flag),
    Training("training", "Training", Icons.Rounded.FitnessCenter),
    Body("body", "Body", Icons.Rounded.MonitorHeart)
}

data class ChromeAction(val icon: ImageVector, val label: String, val route: String)

/** What the shell draws around a destination: title, tab membership, extra top-bar actions. */
data class ScreenChrome(
    val title: String,
    val overline: String? = null,
    val tab: ArcTab? = null,
    val showBars: Boolean = true,
    val actions: List<ChromeAction> = emptyList()
)

val ScreenChromes: Map<String, ScreenChrome> = mapOf(
    "splash" to ScreenChrome(title = "", showBars = false),
    "main" to ScreenChrome("Dashboard", "Overview", ArcTab.Home),
    "missions" to ScreenChrome(
        "Missions", "Objectives", ArcTab.Missions,
        actions = listOf(ChromeAction(Icons.Rounded.CalendarMonth, "Planning", "planning"))
    ),
    "training" to ScreenChrome("Training", "Protocol", ArcTab.Training),
    "body" to ScreenChrome("Body", "Recovery & fuel", ArcTab.Body),
    "profile" to ScreenChrome("Profile", "Identity"),
    "modify_user" to ScreenChrome("Update info", "Credentials"),
    "settings" to ScreenChrome("Settings", "Reminders"),
    "style_lab" to ScreenChrome("Style Lab", "Design system"),
    "planning" to ScreenChrome("Planning", "Google Calendar"),
    "select_session" to ScreenChrome("Select a session", "Programs"),
    "training_session/{sessionId}" to ScreenChrome("Live session", "Training"),
    "view_programs" to ScreenChrome("Program archive", "Database"),
    "create_program" to ScreenChrome("Create program", "Architect"),
    "flexibility" to ScreenChrome("Flexibility", "Mobility"),
    "endurance" to ScreenChrome("Endurance", "Stamina"),
    "water" to ScreenChrome("Hydration", "H2O levels"),
    "sleep" to ScreenChrome("Sleep", "Recovery"),
    "nutrition" to ScreenChrome("Nutrition", "Fuel")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArcTopBar(
    chrome: ScreenChrome,
    onBack: (() -> Unit)?,
    onAction: (ChromeAction) -> Unit,
    onProfile: () -> Unit,
    onSettings: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                if (chrome.overline != null) {
                    Text(
                        text = chrome.overline.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = AccentViolet,
                        letterSpacing = 1.5.sp
                    )
                }
                Text(
                    text = chrome.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = HologramText
                )
            }
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = HologramText
                    )
                }
            }
        },
        actions = {
            chrome.actions.forEach { action ->
                IconButton(onClick = { onAction(action) }) {
                    Icon(action.icon, contentDescription = action.label, tint = HologramText)
                }
            }
            if (chrome.tab != null) {
                AvatarMenu(onProfile = onProfile, onSettings = onSettings)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent
        )
    )
}

@Composable
private fun AvatarMenu(onProfile: () -> Unit, onSettings: () -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box(modifier = Modifier.padding(end = 8.dp)) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(PrimaryGradient)
                .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                .clickable { open = true },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Person,
                contentDescription = "Profile & settings",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            DropdownMenuItem(
                text = { Text("Profile") },
                leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null) },
                onClick = { open = false; onProfile() }
            )
            DropdownMenuItem(
                text = { Text("Settings") },
                leadingIcon = { Icon(Icons.Rounded.Settings, contentDescription = null) },
                onClick = { open = false; onSettings() }
            )
        }
    }
}

@Composable
fun ArcBottomBar(current: ArcTab?, onSelect: (ArcTab) -> Unit) {
    Column {
        HorizontalDivider(color = BorderSubtle, thickness = 1.dp)
        NavigationBar(
            containerColor = SurfaceElevated.copy(alpha = 0.96f),
            tonalElevation = 0.dp
        ) {
            ArcTab.entries.forEach { tab ->
                NavigationBarItem(
                    selected = tab == current,
                    onClick = { onSelect(tab) },
                    icon = { Icon(tab.icon, contentDescription = tab.label) },
                    label = {
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelMedium,
                            letterSpacing = 0.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = HologramText,
                        selectedTextColor = HologramText,
                        indicatorColor = CrimsonRed.copy(alpha = 0.24f),
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
            }
        }
    }
}
