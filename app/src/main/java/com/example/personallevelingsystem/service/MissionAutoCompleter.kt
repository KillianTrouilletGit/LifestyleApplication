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
import com.example.personallevelingsystem.util.daysBetweenDayKeys
import com.example.personallevelingsystem.util.parseSleepHours
import com.example.personallevelingsystem.util.thisWeekBounds
import com.example.personallevelingsystem.util.todayBounds
import com.example.personallevelingsystem.util.todayDayKey
import com.example.personallevelingsystem.util.weeksBetweenDayKeys
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

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
 * Computes progress and completes missions — the *only* path that awards XP.
 *
 * [complete] and [sweep] share a process-wide mutex and re-check the persisted
 * completion state inside it, so a manual tick racing an auto-sweep (water
 * logged → sweep, list refreshed → sweep) can't pay the same mission twice.
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
                    .sumOf { parseSleepHours(it.duration).toDouble() }
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
     * Marks [mission] complete for the current period and pays XP + streak.
     * Returns the XP awarded, or null if it was already completed (no-op).
     */
    suspend fun complete(mission: Mission): Int? = completionLock.withLock { completeLocked(mission) }

    /**
     * Runs through all missions, computes progress, and ticks anything
     * whose underlying data has crossed the target. Returns the missions
     * that were freshly completed during this sweep (for caller-side UX).
     */
    suspend fun sweep(): List<Mission> = completionLock.withLock {
        val freshly = mutableListOf<Mission>()
        val all = missionRepository.getDailyMissions() + missionRepository.getWeeklyMissions()
        for (mission in all) {
            if (mission.isCompleted) continue
            val progress = computeProgress(mission) ?: continue
            if (progress.isMet && completeLocked(mission) != null) {
                freshly.add(mission)
            }
        }
        freshly
    }

    private suspend fun completeLocked(mission: Mission): Int? {
        if (missionRepository.isMissionCompleted(mission.id, mission.type)) return null
        missionRepository.completeMission(mission)
        val awarded = awardWithStreak(mission)
        userRepository.addXp(UserRepository.DEFAULT_USER_ID, awarded)
        return awarded
    }

    /**
     * Streak as it should be displayed right now: the stored counter while the
     * chain is still alive, 0 once it has lapsed (more than [STREAK_GRACE_DAYS]
     * idle days for a daily mission, a whole skipped week for a weekly one).
     */
    fun effectiveStreak(mission: Mission): Int {
        val last = prefs.getLastCompletedDay(mission.id)
        if (last == 0) return 0
        val today = todayDayKey()
        val alive = when (mission.type) {
            MissionType.DAILY -> daysBetweenDayKeys(last, today) - 1 <= STREAK_GRACE_DAYS
            MissionType.WEEKLY -> weeksBetweenDayKeys(last, today) <= 1
        }
        return if (alive) prefs.getStreak(mission.id) else 0
    }

    /**
     * Bump the streak, update per-category XP and return the XP to award
     * (base * streak multiplier). Fires achievement notifications on streak
     * milestones and on "first time clearing all daily missions".
     */
    private fun awardWithStreak(mission: Mission): Int {
        val today = todayDayKey()
        val last = prefs.getLastCompletedDay(mission.id)
        val stored = prefs.getStreak(mission.id)

        val newStreak = when {
            last == today -> stored                      // already counted today
            last == 0 -> 1                               // first ever completion
            mission.type == MissionType.WEEKLY -> when (weeksBetweenDayKeys(last, today)) {
                0 -> stored                              // same week (shouldn't happen, be safe)
                1 -> stored + 1                          // consecutive weeks
                else -> 1                                // a whole week skipped
            }
            // Up to STREAK_GRACE_DAYS idle days are forgiven before a daily streak breaks
            // (gap of 1 = consecutive days = 0 idle days).
            daysBetweenDayKeys(last, today) - 1 <= STREAK_GRACE_DAYS -> stored + 1
            else -> 1                                    // streak broken
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
            body = "${mission.title} held for $streak days. Streak multiplier: +${((streakMultiplier(streak) - 1f) * 100f).toInt()}% XP."
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

    suspend fun waterTargetMl(): Float {
        val weight = db.userDao().getUserById(UserRepository.DEFAULT_USER_ID)?.weight ?: 0f
        return if (weight > 0f) weight * 35f else 2500f
    }

    companion object {
        const val DEFAULT_USER_ID = UserRepository.DEFAULT_USER_ID

        /** Days of inactivity tolerated before a streak resets. */
        const val STREAK_GRACE_DAYS = 3

        /** +10% per 7-day tier, capped at +50%. Shared with the list UI so the badge matches the payout. */
        fun streakMultiplier(streak: Int): Float {
            val tier = (streak / 7).coerceAtMost(5)
            return 1f + tier * 0.10f
        }

        private val completionLock = Mutex()
    }
}
