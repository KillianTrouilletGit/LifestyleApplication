package com.example.personallevelingsystem.service

import android.content.Context
import com.example.personallevelingsystem.data.AppDatabase
import com.example.personallevelingsystem.model.Mission
import com.example.personallevelingsystem.model.MissionRequirement
import com.example.personallevelingsystem.model.MissionType
import com.example.personallevelingsystem.repository.MissionRepository
import com.example.personallevelingsystem.repository.UserRepository
import com.example.personallevelingsystem.util.MissionPrefs
import com.example.personallevelingsystem.util.NotificationUtils
import com.example.personallevelingsystem.util.todayDayKey
import com.example.personallevelingsystem.util.thisWeekKey
import java.util.Calendar

/**
 * Snapshot of the current value vs. target for a data-driven mission.
 * Used both to surface inline progress in the UI and to auto-complete
 * once `current >= target`.
 */
data class MissionProgress(
    val current: Float,
    val target: Float,
    val unit: String
) {
    val ratio: Float get() = if (target <= 0f) 0f else (current / target).coerceIn(0f, 1f)
    val isMet: Boolean get() = target > 0f && current >= target
}

/**
 * Computes progress and auto-completes data-driven missions.
 *
 * Designed to be called whenever underlying data changes (water logged, sleep
 * logged, etc.) and at periodic worker ticks. Idempotent: only triggers the
 * completion side effects (XP add, streak bump, achievement check) once per day
 * per mission.
 */
class MissionAutoCompleter(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val missionRepository = MissionRepository(context)
    private val userRepository = UserRepository(db.userDao(), context)
    private val prefs = MissionPrefs.get(context)

    suspend fun computeProgress(mission: Mission): MissionProgress? {
        return when (val req = mission.requirement) {
            MissionRequirement.Manual -> null
            MissionRequirement.WaterDailyTarget -> {
                val (start, end) = todayBounds()
                val ml = db.WaterDao().getWaterForDay(start, end).sumOf { it.amount.toDouble() }.toFloat()
                MissionProgress(current = ml, target = waterTargetMl(), unit = "ml")
            }
            is MissionRequirement.SleepHoursAtLeast -> {
                val (start, end) = todayBounds()
                val hours = db.SleepTimeDao().getSleepForDay(start, end)
                    .sumOf { it.duration.replace("h", "").trim().toDoubleOrNull() ?: 0.0 }
                    .toFloat()
                MissionProgress(current = hours, target = req.hours, unit = "h")
            }
            is MissionRequirement.FlexibilityMinutes -> {
                val (start, end) = todayBounds()
                val minutes = db.flexibilityTrainingDao().getFlexForDay(start, end)
                    .sumOf { it.duration }
                    .toFloat() / 60_000f // duration is millis
                MissionProgress(current = minutes, target = req.minutes.toFloat(), unit = "min")
            }
            is MissionRequirement.NutritionBalance -> {
                val (start, end) = todayBounds()
                val meals = db.mealDao().getMealForDay(start, end)
                val balance = if (meals.isEmpty()) 0f
                    else meals.map { it.balanceIndex }.average().toFloat()
                MissionProgress(current = balance, target = req.threshold, unit = "")
            }
            is MissionRequirement.EnduranceWeeklyKm -> {
                val (start, end) = thisWeekBounds()
                val km = db.enduranceTrainingDao().getTotalDistanceForWeek(start, end) ?: 0f
                MissionProgress(current = km, target = req.km, unit = "km")
            }
            MissionRequirement.TrainingSessionLoggedToday -> {
                val (start, end) = todayBounds()
                // Only sessions with an endTime count — abandoned/started ones don't
                val count = db.trainingSessionDao().countCompletedSessionsForDay(start, end).toFloat()
                MissionProgress(current = count, target = 1f, unit = "session")
            }
            MissionRequirement.WeightLoggedThisWeek -> {
                val ts = prefs.getWeightLoggedAt()
                val (start, end) = thisWeekBounds()
                val ok = ts in start..end
                MissionProgress(current = if (ok) 1f else 0f, target = 1f, unit = "")
            }
        }
    }

    /**
     * Runs through all missions, computes progress, and ticks anything
     * whose underlying data has crossed the target. Returns the missions
     * that were freshly completed during this sweep (for caller-side UX).
     */
    suspend fun sweep(): List<Mission> {
        val freshly = mutableListOf<Mission>()
        val all = missionRepository.getDailyMissions() + missionRepository.getWeeklyMissions()
        for (mission in all) {
            if (mission.isCompleted) continue
            val progress = computeProgress(mission) ?: continue
            if (progress.isMet) {
                missionRepository.completeMission(mission)
                userRepository.addXp(DEFAULT_USER_ID, awardWithStreak(mission))
                freshly.add(mission)
            }
        }
        return freshly
    }

    /**
     * Award XP for a mission, bump its streak, and update the per-category XP.
     * Returns the actual XP awarded (base * streak multiplier).
     * Also fires achievement notifications on streak milestones and on
     * "first time clearing all daily missions".
     */
    suspend fun awardWithStreak(mission: Mission): Int {
        val today = todayDayKey()
        val last = prefs.getLastCompletedDay(mission.id)
        val currentStreak = prefs.getStreak(mission.id)

        // Up to STREAK_GRACE_DAYS idle days are forgiven before a streak breaks
        // (gap of 1 = consecutive days = 0 idle days).
        val idleDays = com.example.personallevelingsystem.util.daysBetweenDayKeys(last, today) - 1
        val newStreak = when {
            last == today -> currentStreak // already counted today, no double-bump
            last == 0 && currentStreak == 0 -> currentStreak + 1 // first ever completion
            mission.type == MissionType.WEEKLY -> currentStreak + 1
            idleDays <= STREAK_GRACE_DAYS -> currentStreak + 1
            else -> 1 // streak broken
        }

        if (last != today) {
            prefs.setStreak(mission.id, newStreak)
            prefs.setLastCompletedDay(mission.id, today)
            checkStreakAchievements(mission, newStreak)
        }

        val multiplier = streakMultiplier(newStreak)
        val awarded = (mission.reward * multiplier).toInt().coerceAtLeast(mission.reward)
        prefs.addCategoryXp(mission.category, awarded)

        checkAllDailyClearedAchievement()
        return awarded
    }

    private fun checkStreakAchievements(mission: Mission, streak: Int) {
        val milestones = listOf(7, 30, 100)
        if (streak !in milestones) return
        val achId = "streak::${mission.id}::$streak"
        if (prefs.hasAchievement(achId)) return
        prefs.markAchievement(achId)
        NotificationUtils.showAchievement(
            context,
            id = achId,
            title = "${streak}-Day Streak Locked",
            body = "${mission.title} held for $streak days. Streak multiplier: +${(streakMultiplier(streak) - 1f) * 100f}% XP."
        )
    }

    private fun checkAllDailyClearedAchievement() {
        val daily = missionRepository.getDailyMissions()
        if (daily.isEmpty() || daily.any { !it.isCompleted }) return
        val achId = "first_full_clear_${todayDayKey()}"
        val universal = "first_ever_full_clear"
        if (prefs.hasAchievement(universal)) {
            // Subsequent clears get a lighter "perfect day" ping, once per day.
            if (prefs.hasAchievement(achId)) return
            prefs.markAchievement(achId)
            NotificationUtils.showAchievement(
                context,
                id = achId,
                title = "Perfect Day",
                body = "All daily ops cleared. Keep the chain."
            )
            return
        }
        prefs.markAchievement(universal)
        prefs.markAchievement(achId)
        NotificationUtils.showAchievement(
            context,
            id = universal,
            title = "First Full Clear Unlocked",
            body = "You cleared every daily mission for the first time. The system remembers."
        )
    }

    private fun streakMultiplier(streak: Int): Float {
        // +10% per 7-day tier, capped at +50%
        val tier = (streak / 7).coerceAtMost(5)
        return 1f + tier * 0.10f
    }

    suspend fun waterTargetMl(): Float {
        val user = db.userDao().getAll().firstOrNull()
        val weight = user?.weight ?: 0f
        return if (weight > 0f) weight * 35f else 2500f
    }

    private fun todayBounds(): Pair<Long, Long> {
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0)
        val start = c.timeInMillis
        c.set(Calendar.HOUR_OF_DAY, 23); c.set(Calendar.MINUTE, 59); c.set(Calendar.SECOND, 59); c.set(Calendar.MILLISECOND, 999)
        val end = c.timeInMillis
        return start to end
    }

    private fun thisWeekBounds(): Pair<Long, Long> {
        val c = Calendar.getInstance()
        c.set(Calendar.DAY_OF_WEEK, c.firstDayOfWeek)
        c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0)
        val start = c.timeInMillis
        c.add(Calendar.DAY_OF_MONTH, 7)
        c.add(Calendar.MILLISECOND, -1)
        return start to c.timeInMillis
    }

    companion object {
        const val DEFAULT_USER_ID = 1

        /** Days of inactivity tolerated before a streak resets. */
        const val STREAK_GRACE_DAYS = 3
    }
}
