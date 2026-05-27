package com.example.personallevelingsystem.ui.compose.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.collectAsState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
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
    onNavigate: (String) -> Unit,
    performanceViewModel: com.example.personallevelingsystem.viewmodel.PerformanceViewModel,
    healthViewModel: com.example.personallevelingsystem.viewmodel.HealthViewModel
) {
    val performanceState by performanceViewModel.uiState.collectAsState()
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        performanceViewModel.loadPerformanceData()
        isVisible = true
    }

    val items = listOf(
        DashboardItem("missions", "Missions", R.drawable.ic_missions_v2),
        DashboardItem("training", "Training", R.drawable.ic_training_v2),
        DashboardItem("nutrition", "Nutrition", R.drawable.ic_nutrition_v2),
        DashboardItem("sleep", "Sleep", R.drawable.ic_sleep_v2),
        DashboardItem("water", "Hydration", R.drawable.ic_water_v2),
        DashboardItem("planning", "Planning", R.drawable.ic_planning_v2),
        DashboardItem("profile", "Profile", R.drawable.ic_profile_v2),
        DashboardItem("settings", "Settings", R.drawable.ic_settings_v2)
    )

    val lazyGridState = rememberLazyGridState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // SpaceBlack via Theme
            .padding(DesignSystem.Padding)
    ) {
        LazyVerticalGrid(
            state = lazyGridState,
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Header & Carousel Section
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                Column(modifier = Modifier.graphicsLayer {
                    if (lazyGridState.firstVisibleItemIndex == 0) {
                        translationY = lazyGridState.firstVisibleItemScrollOffset * 0.15f
                    }
                }) {
                    OperatorHeader(
                        subtitle = "Operator OS",
                        title = "System Dashboard"
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Performance Visualization
                    com.example.personallevelingsystem.ui.compose.components.PerformanceCarousel(state = performanceState)
        
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            itemsIndexed(items, span = { _, item ->
                // Make primary sections (Missions, Training) span the full width as "Hero" cards
                if (item.id == "missions" || item.id == "training") {
                    androidx.compose.foundation.lazy.grid.GridItemSpan(2)
                } else {
                    androidx.compose.foundation.lazy.grid.GridItemSpan(1)
                }
            }) { index, item ->
                val isHero = item.id == "missions" || item.id == "training"
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(animationSpec = tween(400, delayMillis = index * 50)) + 
                            slideInVertically(initialOffsetY = { 50 }, animationSpec = tween(400, delayMillis = index * 50))
                ) {
                    DashboardCard(item = item, isHero = isHero, extraText = null, onClick = { onNavigate(item.id) })
                }
            }
        }
    }
}

@Composable
fun DashboardCard(
    item: DashboardItem,
    isHero: Boolean = false,
    extraText: String? = null,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "iconBreathing")
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isHero) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconBreathingScale"
    )

    JuicyCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isHero) 160.dp else 140.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = item.iconRes),
                contentDescription = item.title,
                modifier = Modifier
                    .size(if (isHero) 56.dp else 40.dp)
                    .graphicsLayer { 
                        alpha = 0.99f // Required for BlendMode masking
                        scaleX = breathingScale
                        scaleY = breathingScale
                    }
                    .drawWithCache {
                        val brush = com.example.personallevelingsystem.ui.compose.theme.PrimaryGradient
                        onDrawWithContent {
                            drawContent()
                            drawRect(brush, blendMode = BlendMode.SrcIn)
                        }
                    }
            )
            Spacer(modifier = Modifier.height(if (isHero) 12.dp else 8.dp))
            Text(
                text = item.title.uppercase(),
                style = if (isHero) MaterialTheme.typography.titleMedium else MaterialTheme.typography.labelLarge,
                color = androidx.compose.ui.graphics.Color.White,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            if (extraText != null) {
                Spacer(modifier = Modifier.height(4.dp))
                 Text(
                    text = extraText,
                    style = MaterialTheme.typography.labelSmall,
                    color = com.example.personallevelingsystem.ui.compose.theme.AccentRed
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    PersonalLevelingSystemTheme {
         // Preview requires mock/fake VM
    }
}
