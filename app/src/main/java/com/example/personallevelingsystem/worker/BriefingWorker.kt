package com.example.personallevelingsystem.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.personallevelingsystem.data.AppDatabase
import com.example.personallevelingsystem.model.MissionCategory
import com.example.personallevelingsystem.repository.MissionRepository
import com.example.personallevelingsystem.service.MissionAutoCompleter
import com.example.personallevelingsystem.util.MissionPrefs
import com.example.personallevelingsystem.util.NotificationUtils
import java.util.Calendar

/**
 * Morning Briefing (daily, ~07:30) and Weekly Debrief (Sunday ~20:00).
 *
 * Morning: short rich card summarising recovery state, today's mission count,
 * and the first planning block of the day.
 * Sunday: weekly debrief with category XP highlights and streaks held.
 */
class BriefingWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val slot = inputData.getString(KEY_SLOT) ?: SLOT_MORNING
        when (slot) {
            SLOT_MORNING -> postMorning()
            SLOT_SUNDAY -> postSundayDebrief()
        }
        return Result.success()
    }

    private suspend fun postMorning() {
        // Sweep first so anything logged overnight ticks.
        MissionAutoCompleter(applicationContext).sweep()

        val db = AppDatabase.getDatabase(applicationContext)
        val repo = MissionRepository(applicationContext)
        val (yStart, yEnd) = lastNightBounds()

        val sleepHours = db.SleepTimeDao().getSleepForDay(yStart, yEnd)
            .sumOf { it.duration.replace("h", "").trim().toDoubleOrNull() ?: 0.0 }
            .toFloat()

        val incomplete = repo.getIncompleteDailyMissions()

        // We don't have planning Room storage, so the "first planning item"
        // is left null for now; the briefing handles that gracefully.
        NotificationUtils.showMorningBriefing(
            context = applicationContext,
            sleepHours = sleepHours,
            missionsToday = incomplete.size,
            firstPlanningItem = null
        )
    }

    private fun postSundayDebrief() {
        val prefs = MissionPrefs.get(applicationContext)
        val xpMap = prefs.allCategoryXp()
        if (xpMap.values.sum() == 0) return // nothing earned, skip the post

        val ranked = xpMap.entries.sortedByDescending { it.value }
        val top = ranked.firstOrNull()?.key ?: MissionCategory.BODY
        val weakest = ranked.lastOrNull()?.key ?: MissionCategory.PROGRESS
        val totalXp = xpMap.values.sum()

        // Count any mission with streak >= 7 as "held"
        val streaksHeld = MissionRepository(applicationContext).getDailyMissions()
            .count { prefs.getStreak(it.id) >= 7 }

        // Level is not strictly stored on prefs, but we can read user once.
        val db = AppDatabase.getDatabase(applicationContext)
        val level = kotlinx.coroutines.runBlocking { db.userDao().getAll().firstOrNull()?.level ?: 1 }

        NotificationUtils.showWeeklyDebrief(
            context = applicationContext,
            topCategory = top.name,
            weakestCategory = weakest.name,
            xpEarned = totalXp,
            currentLevel = level,
            streakHeld = streaksHeld
        )
    }

    private fun lastNightBounds(): Pair<Long, Long> {
        // Treat "last night" as yesterday 18:00 → today 11:00 to catch most logs
        val end = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 11); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val start = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, -1)
            set(Calendar.HOUR_OF_DAY, 18); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return start to end
    }

    companion object {
        const val KEY_SLOT = "slot"
        const val SLOT_MORNING = "morning"
        const val SLOT_SUNDAY = "sunday"
    }
}
