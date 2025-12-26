package com.example.personallevelingsystem.ui.compose.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.personallevelingsystem.model.Meal
import com.example.personallevelingsystem.ui.compose.components.JuicyButton
import com.example.personallevelingsystem.ui.compose.components.JuicyCard
import com.example.personallevelingsystem.ui.compose.components.JuicyInput
import com.example.personallevelingsystem.ui.compose.components.OperatorHeader
import com.example.personallevelingsystem.ui.compose.theme.DesignSystem
import com.example.personallevelingsystem.ui.compose.theme.PersonalLevelingSystemTheme
import com.example.personallevelingsystem.viewmodel.AnalysisState
import com.example.personallevelingsystem.viewmodel.FoodAnalysisViewModel
import com.example.personallevelingsystem.viewmodel.HealthViewModel
import java.io.File
import com.example.personallevelingsystem.util.createImageFile
import com.example.personallevelingsystem.util.uriToBitmap

@Composable
fun NutritionScreen(
    viewModel: HealthViewModel,
    foodAnalysisViewModel: FoodAnalysisViewModel,
    onBackClick: () -> Unit
) {
    var foodQuery by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    val loggedMeal by viewModel.nutritionResult.observeAsState()
    val analysisState by foodAnalysisViewModel.analysisState.collectAsState()
    
    val context = LocalContext.current
    var currentBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }

    // API Key Management
    val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    var savedApiKey by remember { mutableStateOf(prefs.getString("gemini_api_key", "") ?: "") }
    var showApiKeyDialog by remember { mutableStateOf(false) }

    if (showApiKeyDialog) {
        var tempKey by remember { mutableStateOf("") }
        androidx.compose.ui.window.Dialog(onDismissRequest = { showApiKeyDialog = false }) {
            JuicyCard(
                onClick = {},
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("CONFIGURE AI CORE", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    JuicyInput(
                        value = tempKey,
                        onValueChange = { tempKey = it },
                        placeholder = "PASTE API KEY"
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    JuicyButton(text = "SAVE & AUTHORIZE", onClick = {
                        prefs.edit().putString("gemini_api_key", tempKey).apply()
                        savedApiKey = tempKey
                        showApiKeyDialog = false
                    }, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && capturedImageUri != null) {
            val bitmap = uriToBitmap(context, capturedImageUri!!)
            if (bitmap != null) {
                currentBitmap = bitmap
                foodAnalysisViewModel.analyzeImage(bitmap, savedApiKey)
            } else {
                Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(DesignSystem.Padding)
            .verticalScroll(scrollState)
    ) {
        OperatorHeader(subtitle = "Fuel Management", title = "Nutrition Monitor")
        
        Spacer(modifier = Modifier.height(16.dp))

        // AI Camera Section
        JuicyCard(modifier = Modifier.fillMaxWidth(), onClick = {}) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("AI FOOD SCANNER", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                
                if (currentBitmap != null) {
                   Image(
                       bitmap = currentBitmap!!.asImageBitmap(),
                       contentDescription = "Food Image",
                       modifier = Modifier
                           .height(150.dp)
                           .fillMaxWidth(),
                       contentScale = ContentScale.Crop
                   )
                   Spacer(modifier = Modifier.height(8.dp))
                }

                when (val state = analysisState) {
                    is AnalysisState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                        Text("Analyzing...", style = MaterialTheme.typography.labelMedium)
                    }
                    is AnalysisState.Success -> {
                        val result = state.result
                        
                        // Local state for edits
                        var fiberInput by remember { mutableStateOf("0") }
                        
                        // Derived state for Balance Index
                        // Simplified heuristic: closer to 33/33/33 or 40/30/30 is better. 
                        // Let's use a standard deviation from a "balanced" calorie distribution (40% carbs, 30% protein, 30% fat)
                        // 1.0 is perfect, 0.0 is very unbalanced.
                        fun calculateBalance(p: Double, c: Double, f: Double): Double {
                            val totalCal = (p * 4) + (c * 4) + (f * 9)
                            if (totalCal == 0.0) return 0.5
                            
                            val pRatio = (p * 4) / totalCal
                            val cRatio = (c * 4) / totalCal
                            val fRatio = (f * 9) / totalCal
                            
                            // Target: P=0.3, C=0.4, F=0.3
                            val pDiff = kotlin.math.abs(pRatio - 0.3)
                            val cDiff = kotlin.math.abs(cRatio - 0.4)
                            val fDiff = kotlin.math.abs(fRatio - 0.3)
                            
                            val totalDiff = pDiff + cDiff + fDiff
                            // Max diff is roughly 2.0 (if all in one wrong bucket). 
                            // Map diff (0 to 1.4ish) to Score (1.0 to 0.0)
                            return (1.0 - (totalDiff / 1.5)).coerceIn(0.0, 1.0)
                        }

                        Text("DISH: ${result.dish_name.uppercase()}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        
                        // Summary
                        val totalCalories = result.ingredients.sumOf { it.calories }
                        val totalProtein = result.ingredients.sumOf { it.protein_g }
                        val totalCarbs = result.ingredients.sumOf { it.carbs_g }
                        val totalFat = result.ingredients.sumOf { it.fat_g }
                        // Fiber from AI (new field logic required in sumOf if it exists in Ingredient)
                        // Note: Ingredient class updated to have fiber_g, need to check if it's populated. 
                        // Assuming new API call returns it.
                        val totalFiber = result.ingredients.sumOf { it.fiber_g } 
                        
                        // Update default fiber input if AI found any
                        androidx.compose.runtime.LaunchedEffect(totalFiber) {
                            if (totalFiber > 0) fiberInput = totalFiber.toString()
                        }

                        val calculatedBalance = calculateBalance(totalProtein, totalCarbs, totalFat)

                        Text("DETECTED ${result.ingredients.size} COMPONENTS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Macro Table
                        JuicyCard(modifier = Modifier.fillMaxWidth().padding(4.dp), onClick = {}, shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Metric", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                                    Text("Value", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                
                                MacroRow("Calories", "%.0f kcal".format(totalCalories))
                                MacroRow("Protein (P)", "%.1f g".format(totalProtein))
                                MacroRow("Carbs (C)", "%.1f g".format(totalCarbs))
                                MacroRow("Fat (F)", "%.1f g".format(totalFat))
                                MacroRow("Fiber", "%.1f g".format(totalFiber))
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Balance Index", style = MaterialTheme.typography.labelMedium, color = com.example.personallevelingsystem.ui.compose.theme.CyberCyan)
                                    Text("%.2f".format(calculatedBalance), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = com.example.personallevelingsystem.ui.compose.theme.CyberCyan)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        

                        JuicyButton(text = "LOG THIS MEAL", onClick = {
                             val fiberVal = fiberInput.toDoubleOrNull() ?: totalFiber
                             val finalBalance = calculateBalance(totalProtein, totalCarbs, totalFat)

                             val meal = Meal(
                                date = System.currentTimeMillis(),
                                time = System.currentTimeMillis().toString(),
                                calories = totalCalories,
                                protein = totalProtein,
                                carbs = totalCarbs,
                                fat = totalFat,
                                fiber = fiberVal,
                                balanceIndex = finalBalance
                            )
                            viewModel.saveMeal(meal)
                        }, modifier = Modifier.fillMaxWidth())
                    }
                    is AnalysisState.Error -> {
                         Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                         Spacer(modifier = Modifier.height(8.dp))
                         JuicyButton(text = "TRY AGAIN / NEW PHOTO", onClick = {
                            if (savedApiKey.isBlank()) {
                                showApiKeyDialog = true
                            } else {
                                val uri = createImageFile(context)
                                capturedImageUri = uri
                                cameraLauncher.launch(uri)
                            }
                         }, modifier = Modifier.fillMaxWidth())
                    }
                    else -> {
                        JuicyButton(text = "TAKE PHOTO", onClick = {
                            if (savedApiKey.isBlank()) {
                                // Redirect to GitHub for instructions
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://github.com/KillianTrouilletGit/LifestyleApplication/"))
                                context.startActivity(intent)
                                showApiKeyDialog = true
                            } else {
                                val uri = createImageFile(context)
                                capturedImageUri = uri
                                cameraLauncher.launch(uri)
                            }
                        }, modifier = Modifier.fillMaxWidth())
                        
                        if (savedApiKey.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            androidx.compose.material3.TextButton(onClick = { showApiKeyDialog = true }) {
                                Text("UPDATE API KEY", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }
                }
                
                if (analysisState is AnalysisState.Success) {
                    Spacer(modifier = Modifier.height(8.dp))
                    JuicyButton(text = "RETAKE PHOTO", onClick = {
                         val uri = createImageFile(context)
                        capturedImageUri = uri
                        cameraLauncher.launch(uri)
                    }, modifier = Modifier.fillMaxWidth())
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        if (loggedMeal != null) {
            JuicyCard(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "LAST LOGGED MEAL", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Calories: %.1f".format(loggedMeal!!.calories), color = MaterialTheme.colorScheme.primary)
                    Text(text = "Protein: %.1f g".format(loggedMeal!!.protein), color = MaterialTheme.colorScheme.onSurface)
                    Text(text = "Carbs: %.1f g".format(loggedMeal!!.carbs), color = MaterialTheme.colorScheme.onSurface)
                    Text(text = "Fat: %.1f g".format(loggedMeal!!.fat), color = MaterialTheme.colorScheme.onSurface)
                    Text(text = "Fiber: %.1f g".format(loggedMeal!!.fiber), color = MaterialTheme.colorScheme.onSurface)
                    Text(text = "Balance Index: %.2f".format(loggedMeal!!.balanceIndex), color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        JuicyButton(
            text = "RETURN",
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun MacroRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}


