package com.example.personallevelingsystem.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.personallevelingsystem.data.AppDatabase
import com.example.personallevelingsystem.model.Mission
import com.example.personallevelingsystem.model.MissionCategory
import com.example.personallevelingsystem.model.MissionDifficulty
import com.example.personallevelingsystem.model.MissionRequirement
import com.example.personallevelingsystem.model.MissionType
import com.example.personallevelingsystem.util.thisWeekKey
import com.example.personallevelingsystem.util.todayDayKey
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.Calendar

/**
 * Mission catalogue + completion state.
 *
 * The catalogue is static. Completion is *not* kept in memory: every read goes
 * to SharedPreferences, so the many short-lived instances of this class
 * (view-models, auto-completer, workers, notification receiver) always agree.
 *
 * A completion is stored as the period it was earned in — the day key for
 * daily missions, the Monday key for weekly ones. A mission is "completed"
 * only while that stored period is the current one, which means daily and
 * weekly resets happen naturally at midnight / Monday without any alarm.
 */
class MissionRepository(private val context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("missions_prefs", Context.MODE_PRIVATE)

    companion object {
        private val _missionUpdates = MutableSharedFlow<Unit>(
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
        val missionUpdates = _missionUpdates.asSharedFlow()

        private fun periodKey(missionId: String) = "$missionId::period"
    }

    private val dailyTemplates = listOf(
        Mission(
            id = "daily_flex",
            title = "Mobility Protocol",
            description = "Run a 15-minute flexibility session — open hips, spine, shoulders.",
            type = MissionType.DAILY,
            category = MissionCategory.BODY,
            isCompleted = false,
            reward = 50,
            requirement = MissionRequirement.FlexibilityMinutes(15),
            deeplinkRoute = "flexibility",
            tip = "Hold each position 30s+. Breathe through tension, don't bounce."
        ),
        Mission(
            id = "daily_strength",
            title = "Iron Session",
            description = "Complete a full strength session — every exercise closed out.",
            type = MissionType.DAILY,
            category = MissionCategory.BODY,
            isCompleted = false,
            reward = 50,
            requirement = MissionRequirement.TrainingSessionLoggedToday,
            deeplinkRoute = "training",
            tip = "Only finished sessions count — close the last exercise to bank the XP."
        ),
        Mission(
            id = "daily_water",
            title = "Hydration Quota",
            description = "Drink your daily water target (≈ 35 ml per kg of body weight).",
            type = MissionType.DAILY,
            category = MissionCategory.NUTRITION,
            isCompleted = false,
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
            isCompleted = false,
            reward = 40,
            tip = "Single book or single tutorial. Notes by hand sticks better."
        ),
        Mission(
            id = "daily_meditate",
            title = "Mind Reset",
            description = "Meditate or breathwork for 10 minutes — eyes closed, no input.",
            type = MissionType.DAILY,
            category = MissionCategory.MIND,
            isCompleted = false,
            reward = 25,
            tip = "Box breathing 4-4-4-4 works if your mind is loud."
        ),
        Mission(
            id = "daily_hygiene",
            title = "Maintenance Pass",
            description = "Full hygiene cycle: shower, teeth, skin, nails, hair.",
            type = MissionType.DAILY,
            category = MissionCategory.RECOVERY,
            isCompleted = false,
            reward = 20,
            tip = "Cold finish on the shower if you want a free recovery bonus."
        ),
        Mission(
            id = "daily_sleep",
            title = "Recovery Cycle",
            description = "Log 7+ hours of sleep — your stats regenerate while you're offline.",
            type = MissionType.DAILY,
            category = MissionCategory.RECOVERY,
            isCompleted = false,
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
            isCompleted = false,
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
            isCompleted = false,
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
            isCompleted = false,
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
            isCompleted = false,
            reward = 20,
            tip = "Mirror check before you walk out. Two passes."
        )
    )

    private val weeklyTemplates = listOf(
        Mission(
            id = "weekly_workout",
            title = "Training Arc Complete",
            description = "Finish every session of this week's program — no missed sets.",
            type = MissionType.WEEKLY,
            category = MissionCategory.BODY,
            isCompleted = false,
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
            isCompleted = false,
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
            isCompleted = false,
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
            isCompleted = false,
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
            isCompleted = false,
            reward = 40,
            tip = "Cleaning playlist + 25-min timer. Anything not done after is for next week."
        ),
        Mission(
            id = "weekly_report",
            title = "Weekly Debrief",
            description = "Review your performance graphs — note wins, gaps, next focus.",
            type = MissionType.WEEKLY,
            category = MissionCategory.PROGRESS,
            isCompleted = false,
            reward = 40,
            tip = "Three lines: what worked, what didn't, what's next."
        ),
        Mission(
            id = "weekly_weigh",
            title = "Body Stat Check",
            description = "Log this week's weight — same time, same conditions for accuracy.",
            type = MissionType.WEEKLY,
            category = MissionCategory.PROGRESS,
            isCompleted = false,
            reward = 10,
            requirement = MissionRequirement.WeightLoggedThisWeek,
            deeplinkRoute = "modify_user",
            tip = "Morning, post-bathroom, pre-coffee — that's the honest number."
        )
    )

    fun getDailyMissions(): List<Mission> = dailyTemplates.map { it.withCurrentState() }

    fun getWeeklyMissions(): List<Mission> = weeklyTemplates.map { it.withCurrentState() }

    fun findById(id: String): Mission? =
        (dailyTemplates.firstOrNull { it.id == id } ?: weeklyTemplates.firstOrNull { it.id == id })
            ?.withCurrentState()

    /** True while the stored completion period matches the current day / week. */
    fun isMissionCompleted(missionId: String, type: MissionType): Boolean =
        sharedPreferences.getInt(periodKey(missionId), 0) == currentPeriodKey(type)

    fun completeMission(mission: Mission) {
        sharedPreferences.edit()
            .putInt(periodKey(mission.id), currentPeriodKey(mission.type))
            .apply()
        // Signal update to any listeners (like PerformanceViewModel)
        _missionUpdates.tryEmit(Unit)
    }

    fun getIncompleteDailyMissions(): List<Mission> = getDailyMissions().filter { !it.isCompleted }
    fun getIncompleteWeeklyMissions(): List<Mission> = getWeeklyMissions().filter { !it.isCompleted }
    fun getIncompleteDailyMissionsCount(): Int = getIncompleteDailyMissions().size
    fun getIncompleteWeeklyMissionsCount(): Int = getIncompleteWeeklyMissions().size

    fun updateMissionNotification() {
        // Persistent status notification stays disabled (Operator OS Streamlining).
        // Time-of-day reminders are handled by ReminderScheduler / reminder workers instead.
    }

    private fun Mission.withCurrentState(): Mission = copy(isCompleted = isMissionCompleted(id, type))

    private fun currentPeriodKey(type: MissionType): Int = when (type) {
        MissionType.DAILY -> todayDayKey()
        MissionType.WEEKLY -> thisWeekKey()
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
