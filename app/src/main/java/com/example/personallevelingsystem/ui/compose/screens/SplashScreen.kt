package com.example.personallevelingsystem.ui.compose.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personallevelingsystem.ui.compose.theme.PrimaryAccent
import com.example.personallevelingsystem.ui.compose.theme.NeonCyan
import com.example.personallevelingsystem.ui.compose.theme.NeonMagenta
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onAnimationFinished: () -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    
    val logs = listOf(
        "> INITIALIZING BOOT SEQUENCE...",
        "> [OK] MOUNTING CORE MODULES",
        "> [OK] NEURAL INTERFACE SYNCED",
        "> [OK] CALIBRATING BIOMETRICS",
        "> SYSTEM ONLINE. WELCOME OPERATOR."
    )

    LaunchedEffect(Unit) {
        for (i in logs.indices) {
            delay(500)
            step = i + 1
        }
        delay(800)
        onAnimationFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "OPERATOR OS",
                style = MaterialTheme.typography.displayMedium,
                color = NeonCyan,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Box(
                modifier = Modifier
                    .height(2.dp)
                    .fillMaxWidth(0.6f)
                    .background(NeonMagenta.copy(alpha = 0.5f))
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Log sequence
            Column(
                modifier = Modifier.fillMaxWidth(0.8f),
                horizontalAlignment = Alignment.Start
            ) {
                logs.take(step).forEachIndexed { index, log ->
                    Text(
                        text = log,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (index == step - 1) PrimaryAccent else Color.Gray,
                        modifier = Modifier.padding(vertical = 2.dp),
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            }
        }
    }
}
