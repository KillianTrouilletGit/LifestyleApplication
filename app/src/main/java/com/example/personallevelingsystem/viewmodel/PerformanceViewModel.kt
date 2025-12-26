package com.example.personallevelingsystem.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.personallevelingsystem.data.AppDatabase
import com.example.personallevelingsystem.repository.MissionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import java.util.Calendar

data class PerformanceState(
    val level: Int = 1,
    val currentXp: Float = 0f,
    val requiredXp: Float = 1000f,
    val missionEfficiency: Float = 0f, // 0.0 to 1.0
    val dailyMissionsCompleted: Int = 0,
    val totalDailyMissions: Int = 0,
    val weeklyTrainingFrequency: List<Float> = List(7) { 0f }, // Last 7 days, 0.0 to 1.0 intensity
    val sleepHours: Float = 0f,
    val waterIntake: Float = 0f
)

class PerformanceViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val missionRepository = MissionRepository(application)
    private val userDao = db.userDao()
    private val trainingSessionDao = db.trainingSessionDao()
    private val sleepDao = db.SleepTimeDao()
    private val waterDao = db.WaterDao()

    private val _uiState = MutableStateFlow(PerformanceState())
    val uiState: StateFlow<PerformanceState> = _uiState.asStateFlow()

    init {
        loadPerformanceData()
    }

    fun loadPerformanceData() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                // 1. User Level & XP
                val user = userDao.getLatestUser()
                val level = user?.level ?: 1
                val currentXp = user?.xp?.toFloat() ?: 0f
                // Formula from UserRepository: 100 * level * level
                val requiredXp = (100 * level * level).toFloat()

                // 2. Mission Stats
                val dailyMissions = missionRepository.getDailyMissions()
                val completedCount = dailyMissions.count { it.isCompleted }
                val totalCount = dailyMissions.size
                val efficiency = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

                // 3. Training Frequency (Last 7 Days)
                val calendar = Calendar.getInstance()
                // Reset to end of today
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                val endOfWeek = calendar.timeInMillis
                // Go back 7 days
                calendar.add(Calendar.DAY_OF_YEAR, -6)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                val startOfWeek = calendar.timeInMillis
                
                val sessions = trainingSessionDao.getTrainingSessionsForWeek(startOfWeek, endOfWeek)
                
                // Map sessions to days of the week (0=Mon, 6=Sun or whatever the chart expects)
                // Chart expects M, T, W, T, F, S, S (7 days)
                // We need to map session.startTime to these bins relative to startOfWeek?
                // Actually, let's map them to the last 7 days dynamically or fixed days?
                // The label in Carousel is "M, T, W...".
                // Let's assume we want to show the specific volume for each of the last 7 days relative to today.
                // OR we want to show Monday-Sunday of the current week?
                // "Weekly Volume" usually implies the current week or rolling week.
                // Let's use rolling 7 days ending today for "Performance".
                // So index 6 is Today, index 5 is Yesterday, etc.
                // Wait, the labels in Carousel are fixed "M, T, W, T, F, S, S". Using rolling window with fixed labels is confusing.
                // Let's align to Monday-Sunday of the CURRENT week for clarity, or map correctly to the labels.
                // If I assume the labels are static M-S, I should map sessions to DayOfWeek.
                
                val frequency = MutableList(7) { 0f }
                
                // Group by Day of Week (Monday=1 -> index 0)
                sessions.forEach { session ->
                    val c = Calendar.getInstance()
                    c.time = session.startTime
                    // Calendar.DAY_OF_WEEK: Sun=1, Mon=2, ..., Sat=7
                    var dayIndex = c.get(Calendar.DAY_OF_WEEK) - 2 // Mon=0, Sun=-1
                    if (dayIndex < 0) dayIndex = 6 // Make Sun=6
                    
                    if (dayIndex in 0..6) {
                        // Add duration in hours
                        val hours = session.durationInMillis / (1000f * 60f * 60f)
                        frequency[dayIndex] += hours
                    }
                }

                // 4. Health Data (Today)
                val todayStart = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                val todayEnd = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }.timeInMillis
                
                val sleepRecords = sleepDao.getSleepForDay(todayStart, todayEnd)
                val waterRecords = waterDao.getWaterForDay(todayStart, todayEnd)
                
                // Sum up values
                val sleep = sleepRecords.sumOf { 
                    it.duration.replace("h", "").trim().toDoubleOrNull() ?: 0.0 
                }.toFloat() 
                
                val water = waterRecords.sumOf { it.amount.toDouble() }.toFloat() / 1000f // Convert ml to L

                _uiState.value = PerformanceState(
                    level = level,
                    currentXp = currentXp,
                    requiredXp = requiredXp,
                    missionEfficiency = efficiency,
                    dailyMissionsCompleted = completedCount,
                    totalDailyMissions = totalCount,
                    weeklyTrainingFrequency = frequency,
                    sleepHours = sleep,
                    waterIntake = water
                )
            }
        }
    }
}
