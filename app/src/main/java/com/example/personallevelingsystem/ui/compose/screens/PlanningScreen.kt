package com.example.personallevelingsystem.ui.compose.screens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.personallevelingsystem.R
import com.example.personallevelingsystem.model.MissionType
import com.example.personallevelingsystem.ui.compose.components.JuicyButton
import com.example.personallevelingsystem.ui.compose.components.JuicyCard
import com.example.personallevelingsystem.ui.compose.components.OperatorHeader
import com.example.personallevelingsystem.ui.compose.theme.DesignSystem
import com.example.personallevelingsystem.ui.compose.theme.PlacementSpring
import com.example.personallevelingsystem.ui.compose.theme.PrimaryAccent
import com.example.personallevelingsystem.ui.compose.theme.SpaceBlack
import com.example.personallevelingsystem.viewmodel.MissionViewModel
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun PlanningScreen(
    missionViewModel: MissionViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // State
    var events by remember { mutableStateOf<List<Event>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isAuthorized by remember { mutableStateOf(false) }

    // Google Credential Setup
    val credential = remember {
        GoogleAccountCredential.usingOAuth2(
            context, listOf(CalendarScopes.CALENDAR)
        )
    }

    // Launchers
    val accountPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null && result.data?.extras != null) {
            val accountName = result.data!!.getStringExtra(android.accounts.AccountManager.KEY_ACCOUNT_NAME)
            if (accountName != null) {
                credential.selectedAccountName = accountName
                isAuthorized = true
            }
        } else {
            errorMessage = "Account selection failed."
        }
    }

    val authLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
             // Authorization granted, retry fetch handled by specific function if needed or manual refresh
             isAuthorized = true
        }
    }

    // API Logic
    fun fetchEvents() {
        if (credential.selectedAccountName == null) {
            accountPickerLauncher.launch(credential.newChooseAccountIntent())
            return
        }

        isLoading = true
        errorMessage = null
        
        scope.launch(Dispatchers.IO) {
            try {
                val transport = NetHttpTransport()
                val jsonFactory = GsonFactory.getDefaultInstance()
                val service = com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential
                )
                .setApplicationName("Operator OS")
                .build()

                val now = java.util.Calendar.getInstance()
                // Set to start of this week (Monday)
                now.firstDayOfWeek = java.util.Calendar.MONDAY
                now.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY)
                now.set(java.util.Calendar.HOUR_OF_DAY, 0)
                now.set(java.util.Calendar.MINUTE, 0)
                now.set(java.util.Calendar.SECOND, 0)
                val startOfWeek = DateTime(now.time)
                
                now.add(java.util.Calendar.DAY_OF_YEAR, 6)
                now.set(java.util.Calendar.HOUR_OF_DAY, 23)
                now.set(java.util.Calendar.MINUTE, 59)
                val endOfWeek = DateTime(now.time)

                val items = service.events().list("primary")
                    .setTimeMin(startOfWeek)
                    .setTimeMax(endOfWeek)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute()
                    .items

                withContext(Dispatchers.Main) {
                    events = items ?: emptyList()
                    isLoading = false
                    if (events.isNotEmpty()) {
                         // Auto-complete mission if we successfully fetched and viewed events
                         // Logic from legacy: checking off events. Here we simplify to "Viewed Planning" for now
                         // or require user interaction. The legacy demanded verifying/checking all.
                         // For now, let's keep it simple: Fetching successfully is step 1.
                    }
                }
            } catch (e: UserRecoverableAuthIOException) {
                authLauncher.launch(e.intent)
                withContext(Dispatchers.Main) { isLoading = false }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { 
                    errorMessage = "Error: ${e.message}" 
                    isLoading = false
                }
            }
        }
    }

    // Initial Fetch attempt
    LaunchedEffect(isAuthorized) {
        if (isAuthorized) {
            fetchEvents()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(DesignSystem.Padding)
    ) {
        OperatorHeader(subtitle = "Google Link", title = "Weekly Protocol")

        if (errorMessage != null) {
            Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (!isAuthorized && credential.selectedAccountName == null) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                JuicyButton(
                    text = "LINK GOOGLE ACCOUNT",
                    onClick = { accountPickerLauncher.launch(credential.newChooseAccountIntent()) }
                )
            }
        } else if (isLoading) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryAccent)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(events) { event ->
                    EventItem(event)
                }
                
                if (events.isEmpty()) {
                    item {
                        Text(
                            "No events scheduled for this week.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        JuicyButton(
            text = "REFRESH DATA",
            onClick = { fetchEvents() },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )

        JuicyButton(
            text = "RETURN TO DASHBOARD",
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun EventItem(event: Event) {
    val dateString = event.start.dateTime?.toString() ?: event.start.date?.toString() ?: ""
    // formatting logic could be better, simplified for now
    
    JuicyCard(
        onClick = {}, // Could toggle "checked" status here for mission logic
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
             Text(
                 text = event.summary ?: "No Title",
                 style = MaterialTheme.typography.titleMedium,
                 color = PrimaryAccent
             )
             Spacer(modifier = Modifier.height(4.dp))
             Text(
                 text = "TIME: $dateString",
                 style = MaterialTheme.typography.bodySmall,
                 color = MaterialTheme.colorScheme.onSurfaceVariant
             )
        }
    }
}
