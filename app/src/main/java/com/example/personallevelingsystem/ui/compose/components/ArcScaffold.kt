package com.example.personallevelingsystem.ui.compose.components

import androidx.annotation.StringRes
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personallevelingsystem.R
import com.example.personallevelingsystem.ui.compose.theme.AccentViolet
import com.example.personallevelingsystem.ui.compose.theme.BorderSubtle
import com.example.personallevelingsystem.ui.compose.theme.CrimsonRed
import com.example.personallevelingsystem.ui.compose.theme.HologramText
import com.example.personallevelingsystem.ui.compose.theme.PrimaryGradient
import com.example.personallevelingsystem.ui.compose.theme.SurfaceElevated
import com.example.personallevelingsystem.ui.compose.theme.TextSecondary

enum class ArcTab(val route: String, @StringRes val labelRes: Int, val icon: ImageVector) {
    Home("main", R.string.tab_home, Icons.Rounded.Home),
    Missions("missions", R.string.tab_missions, Icons.Rounded.Flag),
    Training("training", R.string.tab_training, Icons.Rounded.FitnessCenter),
    Body("body", R.string.tab_body, Icons.Rounded.MonitorHeart)
}

data class ChromeAction(val icon: ImageVector, @StringRes val labelRes: Int, val route: String)

/** What the shell draws around a destination: title, tab membership, extra top-bar actions. */
data class ScreenChrome(
    @StringRes val titleRes: Int,
    @StringRes val overlineRes: Int? = null,
    val tab: ArcTab? = null,
    val showBars: Boolean = true,
    val actions: List<ChromeAction> = emptyList()
)

val ScreenChromes: Map<String, ScreenChrome> = mapOf(
    "splash" to ScreenChrome(titleRes = R.string.app_name, showBars = false),
    "main" to ScreenChrome(R.string.chrome_dashboard, R.string.chrome_dashboard_overline, ArcTab.Home),
    "missions" to ScreenChrome(
        R.string.tab_missions, R.string.chrome_missions_overline, ArcTab.Missions,
        actions = listOf(ChromeAction(Icons.Rounded.CalendarMonth, R.string.action_planning, "planning"))
    ),
    "training" to ScreenChrome(R.string.tab_training, R.string.chrome_training_overline, ArcTab.Training),
    "body" to ScreenChrome(R.string.tab_body, R.string.chrome_body_overline, ArcTab.Body),
    "profile" to ScreenChrome(R.string.chrome_profile, R.string.chrome_profile_overline),
    "modify_user" to ScreenChrome(R.string.chrome_modify_user, R.string.chrome_modify_user_overline),
    "settings" to ScreenChrome(R.string.chrome_settings, R.string.chrome_settings_overline),
    "style_lab" to ScreenChrome(R.string.chrome_style_lab, R.string.chrome_style_lab_overline),
    "planning" to ScreenChrome(R.string.chrome_planning, R.string.chrome_planning_overline),
    "select_session" to ScreenChrome(R.string.chrome_select_session, R.string.chrome_select_session_overline),
    "training_session/{sessionId}" to ScreenChrome(R.string.chrome_live_session, R.string.chrome_live_session_overline),
    "view_programs" to ScreenChrome(R.string.chrome_view_programs, R.string.chrome_view_programs_overline),
    "create_program" to ScreenChrome(R.string.chrome_create_program, R.string.chrome_create_program_overline),
    "flexibility" to ScreenChrome(R.string.chrome_flexibility, R.string.chrome_flexibility_overline),
    "endurance" to ScreenChrome(R.string.chrome_endurance, R.string.chrome_endurance_overline),
    "water" to ScreenChrome(R.string.chrome_water, R.string.chrome_water_overline),
    "sleep" to ScreenChrome(R.string.chrome_sleep, R.string.chrome_sleep_overline),
    "nutrition" to ScreenChrome(R.string.chrome_nutrition, R.string.chrome_nutrition_overline)
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
                if (chrome.overlineRes != null) {
                    Text(
                        text = stringResource(chrome.overlineRes).uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = AccentViolet,
                        letterSpacing = 1.5.sp
                    )
                }
                Text(
                    text = stringResource(chrome.titleRes),
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
                        contentDescription = stringResource(R.string.action_back),
                        tint = HologramText
                    )
                }
            }
        },
        actions = {
            chrome.actions.forEach { action ->
                IconButton(onClick = { onAction(action) }) {
                    Icon(action.icon, contentDescription = stringResource(action.labelRes), tint = HologramText)
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
                contentDescription = stringResource(R.string.menu_avatar_cd),
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.menu_profile)) },
                leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null) },
                onClick = { open = false; onProfile() }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.menu_settings)) },
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
                val label = stringResource(tab.labelRes)
                NavigationBarItem(
                    selected = tab == current,
                    onClick = { onSelect(tab) },
                    icon = { Icon(tab.icon, contentDescription = label) },
                    label = {
                        Text(
                            text = label,
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
