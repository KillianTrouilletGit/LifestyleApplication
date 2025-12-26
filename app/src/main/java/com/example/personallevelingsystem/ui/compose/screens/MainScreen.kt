package com.example.personallevelingsystem.ui.compose.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import com.example.personallevelingsystem.R
import com.example.personallevelingsystem.ui.compose.components.JuicyCard
import com.example.personallevelingsystem.ui.compose.components.OperatorHeader
import com.example.personallevelingsystem.ui.compose.theme.DesignSystem
import com.example.personallevelingsystem.ui.compose.theme.PersonalLevelingSystemTheme

data class DashboardItem(
    val id: String,
    val title: String,
    val iconRes: Int
)

@Composable
fun MainScreen(
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        DashboardItem("missions", "Missions", R.drawable.ic_missions_neon),
        DashboardItem("training", "Training", R.drawable.ic_training_neon),
        DashboardItem("nutrition", "Nutrition", R.drawable.ic_nutrition_neon),
        DashboardItem("sleep", "Sleep", R.drawable.ic_sleep_neon),
        DashboardItem("water", "Hydration", R.drawable.ic_water_neon),
        DashboardItem("planning", "Planning", R.drawable.ic_planning_neon),
        DashboardItem("profile", "Profile", R.drawable.ic_profile_neon),
        DashboardItem("settings", "Settings", R.drawable.ic_settings_neon)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // SpaceBlack via Theme
            .padding(DesignSystem.Padding)
    ) {
        OperatorHeader(
            subtitle = "Operator OS",
            title = "System Dashboard"
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(items) { item ->
                DashboardCard(item = item, onClick = { onNavigate(item.id) })
            }
        }
    }
}

@Composable
fun DashboardCard(
    item: DashboardItem,
    onClick: () -> Unit
) {
    JuicyCard(
        onClick = onClick,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = item.iconRes),
                contentDescription = item.title,
                modifier = Modifier
                    .size(48.dp)
                    .drawWithCache {
                        val brush = com.example.personallevelingsystem.ui.compose.theme.PrimaryGradient
                        onDrawWithContent {
                            drawContent()
                            drawRect(brush, blendMode = BlendMode.SrcIn)
                        }
                    }
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = item.title.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    PersonalLevelingSystemTheme {
        MainScreen(onNavigate = {})
    }
}
