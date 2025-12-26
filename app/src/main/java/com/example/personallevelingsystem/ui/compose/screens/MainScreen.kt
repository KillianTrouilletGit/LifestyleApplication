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
import com.example.personallevelingsystem.R
import com.example.personallevelingsystem.ui.compose.components.JuicyCard
import com.example.personallevelingsystem.ui.compose.components.OperatorHeader
import com.example.personallevelingsystem.ui.compose.theme.PersonalLevelingSystemTheme
import com.example.personallevelingsystem.ui.compose.theme.ProtocolCyan

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
        DashboardItem("missions", "Missions", R.drawable.ic_missions),
        DashboardItem("training", "Training", R.drawable.ic_training),
        DashboardItem("nutrition", "Nutrition", R.drawable.ic_nutrition),
        DashboardItem("sleep", "Sleep", R.drawable.sleep_ic), // Note: xml says sleep_ic not ic_sleep
        DashboardItem("water", "Hydration", R.drawable.ic_water),
        DashboardItem("planning", "Planning", R.drawable.ic_planning),
        DashboardItem("profile", "Profile", R.drawable.ic_user),
        DashboardItem("settings", "Settings", R.drawable.ic_modify)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        OperatorHeader(
            subtitle = "Operator OS",
            title = "System Dashboard"
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        // ViewPager placeholder? The XML had a ViewPager.
        // I will omit it for now or put a placeholder as I don't see the adapter code.
        // Assuming it's a chart carousel based on file names `item_carousel_chart`.
        
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
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Using ColorFilter to tint the icons to reflect the theme if they are flats
            Image(
                painter = painterResource(id = item.iconRes),
                contentDescription = item.title,
                modifier = Modifier.size(64.dp),
                colorFilter = ColorFilter.tint(ProtocolCyan) 
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
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
