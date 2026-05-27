package com.example.personallevelingsystem.repository

import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

import android.content.SharedPreferences
import android.content.Context
import com.example.personallevelingsystem.data.AppDatabase
import com.example.personallevelingsystem.model.MissionCategory
import com.example.personallevelingsystem.model.MissionDifficulty
import com.example.personallevelingsystem.model.MissionRequirement
import com.example.personallevelingsystem.model.MissionType
import com.example.personallevelingsystem.model.Mission
import kotlinx.coroutines.Dispatchers
import java.util.Calendar

class MissionRepository(private val context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("missions_prefs", Context.MODE_PRIVATE)

    companion object {
        private val _missionUpdates = kotlinx.coroutines.flow.MutableSharedFlow<Unit>(replay = 0)
        val missionUpdates = _missionUpdates.asSharedFlow()
    }

    private val dailyMissions = mutableListOf(
        Mission(
            id = "daily_flex",
            title = "Mobility Protocol",
            description = "Run a 15-minute flexibility session — open hips, spine, shoulders.",
            type = MissionType.DAILY,
            category = MissionCategory.BODY,
            isCompleted = getMissionCompletionStatus("daily_flex"),
            reward = 50,
            requirement = MissionRequirement.FlexibilityMinutes(15),
            deeplinkRoute = "flexibility",
            tip = "Hold each position 30s+. Breathe through tension, don't bounce."
        ),
        Mission(
            id = "daily_water",
            title = "Hydration Quota",
            description = "Drink your daily water target (≈ 35 ml per kg of body weight).",
            type = MissionType.DAILY,
            category = MissionCategory.NUTRITION,
            isCompleted = getMissionCompletionStatus("daily_water"),
            reward = 30,
            requirement = MissionRequirement.WaterDailyTarget,
            deeplinkRoute = "water",
            tip = "Sip every 30 min. A full glass on waking buys you 250 ml free."
        ),
        Mission(
            id = "daily_learn",
            title = "Knowledge Drip",
            description = "Deep-focus learning for 30 minutes — no phone, no tabs.",
            type = MissionType.DAILY,
            category = MissionCategory.MIND,
            isCompleted = getMissionCompletionStatus("daily_learn"),
            reward = 40,
            tip = "Single book or single tutorial. Notes by hand sticks better."
        ),
        Mission(
            id = "daily_meditate",
            title = "Mind Reset",
            description = "Meditate or breathwork for 10 minutes — eyes closed, no input.",
            type = MissionType.DAILY,
            category = MissionCategory.MIND,
            isCompleted = getMissionCompletionStatus("daily_meditate"),
            reward = 25,
            tip = "Box breathing 4-4-4-4 works if your mind is loud."
        ),
        Mission(
            id = "daily_hygiene",
            title = "Maintenance Pass",
            description = "Full hygiene cycle: shower, teeth, skin, nails, hair.",
            type = MissionType.DAILY,
            category = MissionCategory.RECOVERY,
            isCompleted = getMissionCompletionStatus("daily_hygiene"),
            reward = 20,
            tip = "Cold finish on the shower if you want a free recovery bonus."
        ),
        Mission(
            id = "daily_sleep",
            title = "Recovery Cycle",
            description = "Log 7+ hours of sleep — your stats regenerate while you're offline.",
            type = MissionType.DAILY,
            category = MissionCategory.RECOVERY,
            isCompleted = getMissionCompletionStatus("daily_sleep"),
            reward = 20,
            requirement = MissionRequirement.SleepHoursAtLeast(7f),
            deeplinkRoute = "sleep",
            tip = "Phone out of bed, room cold, last meal 3h+ before bed."
        ),
        Mission(
            id = "daily_nutrition",
            title = "Fuel Balance",
            description = "Hit a balanced macro split today — protein, complex carbs, healthy fats.",
            type = MissionType.DAILY,
            category = MissionCategory.NUTRITION,
            isCompleted = getMissionCompletionStatus("daily_nutrition"),
            reward = 20,
            requirement = MissionRequirement.NutritionBalance(0.7f),
            deeplinkRoute = "nutrition",
            tip = "Aim for a palm of protein and a fist of veg per meal."
        ),
        Mission(
            id = "daily_planning",
            title = "Execute the Plan",
            description = "Clear today's planning blocks — no skipped, no postponed.",
            type = MissionType.DAILY,
            category = MissionCategory.DISCIPLINE,
            isCompleted = getMissionCompletionStatus("daily_planning"),
            reward = 20,
            deeplinkRoute = "planning",
            tip = "Move on to the next block on the hour, not when it 'feels right'."
        ),
        Mission(
            id = "daily_planning_2",
            title = "Tomorrow's Briefing",
            description = "Lock in tomorrow's schedule before bed — 3 priorities minimum.",
            type = MissionType.DAILY,
            category = MissionCategory.DISCIPLINE,
            isCompleted = getMissionCompletionStatus("daily_planning_2"),
            reward = 20,
            deeplinkRoute = "planning",
            tip = "Write the priorities down. Phone notes don't count."
        ),
        Mission(
            id = "daily_appearance",
            title = "Operator Standard",
            description = "Groom and dress with intent — like you might meet anyone today.",
            type = MissionType.DAILY,
            category = MissionCategory.DISCIPLINE,
            isCompleted = getMissionCompletionStatus("daily_appearance"),
            reward = 20,
            tip = "Mirror check before you walk out. Two passes."
        )
    )

    private val weeklyMissions = mutableListOf(
        Mission(
            id = "weekly_workout",
            title = "Training Arc Complete",
            description = "Finish every session of this week's program — no missed sets.",
            type = MissionType.WEEKLY,
            category = MissionCategory.BODY,
            isCompleted = getMissionCompletionStatus("weekly_workout"),
            reward = 100,
            difficulty = MissionDifficulty.ELITE,
            deeplinkRoute = "training",
            tip = "Lock the schedule on Sunday. Missed sessions are skipped, not pushed."
        ),
        Mission(
            id = "weekly_planning",
            title = "Next-Week Strategy",
            description = "Build out next week's planning — workouts, meals, deep-work blocks.",
            type = MissionType.WEEKLY,
            category = MissionCategory.DISCIPLINE,
            isCompleted = getMissionCompletionStatus("weekly_planning"),
            reward = 80,
            difficulty = MissionDifficulty.HARD,
            deeplinkRoute = "planning",
            tip = "Block deep-work first, fit the rest around it."
        ),
        Mission(
            id = "weekly_endurance",
            title = "Distance Run",
            description = "Cover 10 km of endurance work — outdoor preferred.",
            type = MissionType.WEEKLY,
            category = MissionCategory.BODY,
            isCompleted = getMissionCompletionStatus("weekly_endurance"),
            reward = 60,
            difficulty = MissionDifficulty.HARD,
            requirement = MissionRequirement.EnduranceWeeklyKm(10f),
            deeplinkRoute = "endurance",
            tip = "Splits matter less than consistency. Even 3×3 km counts."
        ),
        Mission(
            id = "weekly_cook",
            title = "New Recipe Unlocked",
            description = "Cook a meal you've never made — expand the menu rotation.",
            type = MissionType.WEEKLY,
            category = MissionCategory.NUTRITION,
            isCompleted = getMissionCompletionStatus("weekly_cook"),
            reward = 50,
            deeplinkRoute = "nutrition",
            tip = "Pick the recipe before grocery day, not the morning of."
        ),
        Mission(
            id = "weekly_clean",
            title = "Base Reset",
            description = "Deep-clean your living space — surfaces, floors, laundry, dishes.",
            type = MissionType.WEEKLY,
            category = MissionCategory.DISCIPLINE,
            isCompleted = getMissionCompletionStatus("weekly_clean"),
            reward = 40,
            tip = "Cleaning playlist + 25-min timer. Anything not done after is for next week."
        ),
        Mission(
            id = "weekly_report",
            title = "Weekly Debrief",
            description = "Review your performance graphs — note wins, gaps, next focus.",
            type = MissionType.WEEKLY,
            category = MissionCategory.PROGRESS,
            isCompleted = getMissionCompletionStatus("weekly_report"),
            reward = 40,
            tip = "Three lines: what worked, what didn't, what's next."
        ),
        Mission(
            id = "weekly_weigh",
            title = "Body Stat Check",
            description = "Log this week's weight — same time, same conditions for accuracy.",
            type = MissionType.WEEKLY,
            category = MissionCategory.PROGRESS,
            isCompleted = getMissionCompletionStatus("weekly_weigh"),
            reward = 10,
            requirement = MissionRequirement.WeightLoggedThisWeek,
            deeplinkRoute = "modify_user",
            tip = "Morning, post-bathroom, pre-coffee — that's the honest number."
        )
    )

    fun getDailyMissions(): List<Mission> = dailyMissions

    fun getWeeklyMissions(): List<Mission> = weeklyMissions

    fun findById(id: String): Mission? =
        dailyMissions.firstOrNull { it.id == id } ?: weeklyMissions.firstOrNull { it.id == id }

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

    fun getIncompleteDailyMissions(): List<Mission> = dailyMissions.filter { !it.isCompleted }
    fun getIncompleteWeeklyMissions(): List<Mission> = weeklyMissions.filter { !it.isCompleted }
    fun getIncompleteDailyMissionsCount(): Int = getIncompleteDailyMissions().size
    fun getIncompleteWeeklyMissionsCount(): Int = getIncompleteWeeklyMissions().size

    fun updateMissionNotification() {
        // Persistent status notification stays disabled (Operator OS Streamlining).
        // Time-of-day reminders are handled by ReminderScheduler / reminder workers instead.
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
