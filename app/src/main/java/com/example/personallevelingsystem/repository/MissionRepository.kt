package com.example.personallevelingsystem.repository

import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

import android.content.SharedPreferences
import android.content.Context
import android.util.Log
import com.example.personallevelingsystem.data.AppDatabase
import com.example.personallevelingsystem.model.MissionType
import com.example.personallevelingsystem.model.Mission
import com.example.personallevelingsystem.util.NotificationUtils
import com.example.personallevelingsystem.viewmodel.UserViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

class MissionRepository(private val context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val sharedPreferences: SharedPreferences =context.getSharedPreferences("missions_prefs", Context.MODE_PRIVATE)

    companion object {
        private val _missionUpdates = kotlinx.coroutines.flow.MutableSharedFlow<Unit>(replay = 0)
        val missionUpdates = _missionUpdates.asSharedFlow()
    }

    private val dailyMissions = mutableListOf(
        Mission("daily_flex", "Complete a 15-minutes flexibility training", MissionType.DAILY, getMissionCompletionStatus("daily_flex"), 50),
        Mission("daily_water", "Drink the correct amount of water", MissionType.DAILY, getMissionCompletionStatus("daily_water"), 30),
        Mission("daily_learn", "Practice micro learning for 30 minutes", MissionType.DAILY, getMissionCompletionStatus("daily_learn"), 40),
        Mission("daily_meditate", "Meditate for 10 minutes", MissionType.DAILY, getMissionCompletionStatus("daily_meditate"), 25),
        Mission("daily_hygiene", "Make sure to have proper hygiene", MissionType.DAILY, getMissionCompletionStatus("daily_hygiene"), 20),
        Mission("daily_sleep", "Sleep over 7 hours", MissionType.DAILY, getMissionCompletionStatus("daily_sleep"), 20),
        Mission("daily_nutrition", "Be sure to have a proper nutrition", MissionType.DAILY, getMissionCompletionStatus("daily_nutrition"), 20),
        Mission("daily_planning", "Respect today's planning", MissionType.DAILY, getMissionCompletionStatus("daily_planning"), 20),
        Mission("daily_planning_2", "Fine tune tomorrow's planning", MissionType.DAILY, getMissionCompletionStatus("daily_planning_2"), 20),
        Mission("daily_appearance", "Work on external appearance", MissionType.DAILY, getMissionCompletionStatus("daily_appearance"), 20)

    )

    private val weeklyMissions = mutableListOf(
        Mission("weekly_workout", "Complete the workout program", MissionType.WEEKLY, getMissionCompletionStatus("weekly_workout"), 100),
        Mission("weekly_planning", "Create next week planning", MissionType.WEEKLY, getMissionCompletionStatus("weekly_planning"), 80),
        Mission("weekly_endurance", "Run 10 kilometers", MissionType.WEEKLY, getMissionCompletionStatus("weekly_endurance"), 60),
        Mission("weekly_cook", "Cook a new recipe", MissionType.WEEKLY, getMissionCompletionStatus("weekly_cook"), 50),
        Mission("weekly_clean", "Clean your living space", MissionType.WEEKLY, getMissionCompletionStatus("weekly_clean"), 40),
        Mission("weekly_report", "Check the progress of the week", MissionType.WEEKLY, getMissionCompletionStatus("weekly_report"), 40),
        Mission("weekly_weigh", "Register new weight", MissionType.WEEKLY, getMissionCompletionStatus("weekly_weigh"), 10)
    )

    fun getDailyMissions(): List<Mission> = dailyMissions

    fun getWeeklyMissions(): List<Mission> = weeklyMissions

    fun resetDailyMissions() {
        dailyMissions.replaceAll { it.copy(isCompleted = false) }
        dailyMissions.forEach {
            saveMissionCompletionStatus(it.id, false)
        }
        updateMissionNotification()
    }

    fun resetWeeklyMissions() {
        weeklyMissions.replaceAll { it.copy(isCompleted = false) }
        weeklyMissions.forEach {
            saveMissionCompletionStatus(it.id, false)
        }
        updateMissionNotification()
    }

    fun completeMission(mission: Mission) {
        val dailyIndex = dailyMissions.indexOfFirst { it.id == mission.id }
        if (dailyIndex != -1) {
            dailyMissions[dailyIndex] = dailyMissions[dailyIndex].copy(isCompleted = true)
        }

        val weeklyIndex = weeklyMissions.indexOfFirst { it.id == mission.id }
        if (weeklyIndex != -1) {
            weeklyMissions[weeklyIndex] = weeklyMissions[weeklyIndex].copy(isCompleted = true)
        }

        saveMissionCompletionStatus(mission.id, true)
        updateMissionNotification()
        
        // Signal update to any listeners (like PerformanceViewModel)
        kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
            _missionUpdates.emit(Unit)
        }
    }

    private fun getMissionCompletionStatus(missionId: String): Boolean {
        return sharedPreferences.getBoolean(missionId, false)
    }

    private fun saveMissionCompletionStatus(missionId: String, isCompleted: Boolean) {
        sharedPreferences.edit().putBoolean(missionId, isCompleted).apply()
    }

    private fun getIncompleteDailyMissionsCount(): Int {
        return dailyMissions.count { !it.isCompleted }
    }

    private fun getIncompleteWeeklyMissionsCount(): Int {
        return weeklyMissions.count { !it.isCompleted }
    }

    fun updateMissionNotification() {
        val dailyMissionsLeft = getIncompleteDailyMissionsCount()
        val weeklyMissionsLeft = getIncompleteWeeklyMissionsCount()
        NotificationUtils.updatePermanentMissionNotification(context, dailyMissionsLeft, weeklyMissionsLeft)
    }

    suspend fun getTotalCaloriesForCurrentDay(): Double {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val startOfNextDay = calendar.timeInMillis
        return db.mealDao().getTotalCaloriesForCurrentDay(startOfDay, startOfNextDay) ?: 0.0
    }

}


