package com.example.personallevelingsystem.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.personallevelingsystem.data.AppDatabase
import com.example.personallevelingsystem.repository.MissionRepository
import com.example.personallevelingsystem.service.MissionAutoCompleter
import com.example.personallevelingsystem.util.MissionPrefs
import com.example.personallevelingsystem.util.NotificationUtils
import java.util.Calendar

/**
 * Mid-day / evening / last-call reminders for incomplete daily and weekly missions.
 *
 * Picks the right slot from input data, runs an auto-complete sweep first
 * (so anything the user has logged but not ticked auto-resolves), then
 * consults `MissionPrefs.isBehindOnlyMode()` and snooze state via
 * `NotificationUtils.shouldSuppress(...)` to decide whether to actually post.
 *
 * If multiple things are pending and we're at evening / last-call, we use the
 * consolidated digest path instead of a per-slot notification.
 */
class MissionReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val slot = inputData.getString(KEY_SLOT) ?: SLOT_MIDDAY
        val repo = MissionRepository(applicationContext)
        val prefs = MissionPrefs.get(applicationContext)

        // Run a sweep so anything already logged ticks off before we count.
        MissionAutoCompleter(applicationContext).sweep()

        val incompleteDaily = repo.getIncompleteDailyMissions()
        val incompleteWeekly = repo.getIncompleteWeeklyMissions()

        when (slot) {
            SLOT_MIDDAY -> {
                if (incompleteDaily.isEmpty()) {
                    NotificationUtils.cancelReminder(applicationContext, NotificationUtils.MIDDAY_MISSION_ID)
                    return Result.success()
                }
                if (prefs.isBehindOnlyMode() && incompleteDaily.size < 3) return Result.success()
                NotificationUtils.showMissionReminder(
                    context = applicationContext,
                    id = NotificationUtils.MIDDAY_MISSION_ID,
                    slot = slot,
                    title = "Mid-Day Check-In",
                    body = "${incompleteDaily.size} daily ops still pending. Keep momentum.",
                    incompleteMissions = incompleteDaily,
                    urgent = false
                )
            }
            SLOT_EVENING -> {
                if (incompleteDaily.isEmpty()) {
                    NotificationUtils.cancelReminder(applicationContext, NotificationUtils.EVENING_MISSION_ID)
                    return Result.success()
                }
                val missingLogs = computeMissingLogs(applicationContext)
                NotificationUtils.showDailyDigest(
                    context = applicationContext,
                    incompleteMissions = incompleteDaily,
                    missingLogs = missingLogs,
                    urgent = false
                )
            }
            SLOT_LAST_CALL_DAILY -> {
                if (incompleteDaily.isEmpty()) {
                    NotificationUtils.cancelReminder(applicationContext, NotificationUtils.LAST_CALL_DAILY_ID)
                    return Result.success()
                }
                val totalXp = incompleteDaily.sumOf { it.reward }
                NotificationUtils.showMissionReminder(
                    context = applicationContext,
                    id = NotificationUtils.LAST_CALL_DAILY_ID,
                    slot = slot,
                    title = "LAST CALL — Daily Reset Incoming",
                    body = "$totalXp XP at stake. ${incompleteDaily.size} ops still unlocked.",
                    incompleteMissions = incompleteDaily,
                    urgent = true
                )
            }
            SLOT_LAST_CALL_WEEKLY -> {
                if (incompleteWeekly.isEmpty()) {
                    NotificationUtils.cancelReminder(applicationContext, NotificationUtils.LAST_CALL_WEEKLY_ID)
                    return Result.success()
                }
                val totalXp = incompleteWeekly.sumOf { it.reward }
                NotificationUtils.showMissionReminder(
                    context = applicationContext,
                    id = NotificationUtils.LAST_CALL_WEEKLY_ID,
                    slot = slot,
                    title = "WEEKLY RESET TONIGHT",
                    body = "$totalXp XP burning. ${incompleteWeekly.size} weekly ops open.",
                    incompleteMissions = incompleteWeekly,
                    urgent = true
                )
            }
        }

        return Result.success()
    }

    private suspend fun computeMissingLogs(context: Context): List<String> {
        val db = AppDatabase.getDatabase(context)
        val (start, end) = todayBounds()
        val missing = mutableListOf<String>()
        if (db.WaterDao().getWaterForDay(start, end).isEmpty()) missing += "water"
        if (db.SleepTimeDao().getSleepForDay(start, end).isEmpty()) missing += "sleep"
        if (db.mealDao().getMealForDay(start, end).isEmpty()) missing += "meals"
        return missing
    }

    private fun todayBounds(): Pair<Long, Long> {
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0)
        val start = c.timeInMillis
        c.set(Calendar.HOUR_OF_DAY, 23); c.set(Calendar.MINUTE, 59); c.set(Calendar.SECOND, 59); c.set(Calendar.MILLISECOND, 999)
        return start to c.timeInMillis
    }

    companion object {
        const val KEY_SLOT = "slot"
        const val SLOT_MIDDAY = "midday"
        const val SLOT_EVENING = "evening"
        const val SLOT_LAST_CALL_DAILY = "last_call_daily"
        const val SLOT_LAST_CALL_WEEKLY = "last_call_weekly"
    }
}
