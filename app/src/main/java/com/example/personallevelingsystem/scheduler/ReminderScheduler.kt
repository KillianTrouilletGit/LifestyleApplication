package com.example.personallevelingsystem.scheduler

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.personallevelingsystem.worker.BriefingWorker
import com.example.personallevelingsystem.worker.LoggingReminderWorker
import com.example.personallevelingsystem.worker.MissionReminderWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Wires up all time-of-day reminder workers. Each runs once every 24h
 * (or 7d for weekly slots), with the initial delay computed to land at the
 * intended wall-clock time.
 *
 * Workers themselves are responsible for checking state, quiet hours, snooze,
 * and posting (or skipping) the notification — the scheduler just makes sure
 * they fire.
 */
class ReminderScheduler(private val context: Context) {

    fun scheduleAll() {
        // Mission slot reminders
        scheduleDaily<MissionReminderWorker>("mission_reminder_${MissionReminderWorker.SLOT_MIDDAY}",
            MissionReminderWorker.KEY_SLOT, MissionReminderWorker.SLOT_MIDDAY,
            hour = 12, minute = 30, tag = TAG_MISSION_REMINDER)
        scheduleDaily<MissionReminderWorker>("mission_reminder_${MissionReminderWorker.SLOT_EVENING}",
            MissionReminderWorker.KEY_SLOT, MissionReminderWorker.SLOT_EVENING,
            hour = 18, minute = 0, tag = TAG_MISSION_REMINDER)
        scheduleDaily<MissionReminderWorker>("mission_reminder_${MissionReminderWorker.SLOT_LAST_CALL_DAILY}",
            MissionReminderWorker.KEY_SLOT, MissionReminderWorker.SLOT_LAST_CALL_DAILY,
            hour = 21, minute = 30, tag = TAG_MISSION_REMINDER)
        scheduleWeekly<MissionReminderWorker>("mission_reminder_${MissionReminderWorker.SLOT_LAST_CALL_WEEKLY}",
            MissionReminderWorker.KEY_SLOT, MissionReminderWorker.SLOT_LAST_CALL_WEEKLY,
            dayOfWeek = Calendar.SUNDAY, hour = 19, minute = 0, tag = TAG_MISSION_REMINDER)

        // Logging slot reminders
        scheduleDaily<LoggingReminderWorker>("logging_reminder_${LoggingReminderWorker.SLOT_WATER_MORNING}",
            LoggingReminderWorker.KEY_SLOT, LoggingReminderWorker.SLOT_WATER_MORNING,
            hour = 10, minute = 0, tag = TAG_LOGGING_REMINDER)
        scheduleDaily<LoggingReminderWorker>("logging_reminder_${LoggingReminderWorker.SLOT_WATER_AFTERNOON}",
            LoggingReminderWorker.KEY_SLOT, LoggingReminderWorker.SLOT_WATER_AFTERNOON,
            hour = 15, minute = 0, tag = TAG_LOGGING_REMINDER)
        scheduleDaily<LoggingReminderWorker>("logging_reminder_${LoggingReminderWorker.SLOT_SLEEP_MORNING}",
            LoggingReminderWorker.KEY_SLOT, LoggingReminderWorker.SLOT_SLEEP_MORNING,
            hour = 9, minute = 0, tag = TAG_LOGGING_REMINDER)
        scheduleDaily<LoggingReminderWorker>("logging_reminder_${LoggingReminderWorker.SLOT_NUTRITION_LUNCH}",
            LoggingReminderWorker.KEY_SLOT, LoggingReminderWorker.SLOT_NUTRITION_LUNCH,
            hour = 13, minute = 30, tag = TAG_LOGGING_REMINDER)
        scheduleDaily<LoggingReminderWorker>("logging_reminder_${LoggingReminderWorker.SLOT_NUTRITION_DINNER}",
            LoggingReminderWorker.KEY_SLOT, LoggingReminderWorker.SLOT_NUTRITION_DINNER,
            hour = 20, minute = 30, tag = TAG_LOGGING_REMINDER)
        scheduleDaily<LoggingReminderWorker>("logging_reminder_${LoggingReminderWorker.SLOT_PLANNING_EVENING}",
            LoggingReminderWorker.KEY_SLOT, LoggingReminderWorker.SLOT_PLANNING_EVENING,
            hour = 21, minute = 0, tag = TAG_LOGGING_REMINDER)
        scheduleWeekly<LoggingReminderWorker>("logging_reminder_${LoggingReminderWorker.SLOT_WEIGHT_SUNDAY}",
            LoggingReminderWorker.KEY_SLOT, LoggingReminderWorker.SLOT_WEIGHT_SUNDAY,
            dayOfWeek = Calendar.SUNDAY, hour = 10, minute = 0, tag = TAG_LOGGING_REMINDER)

        // Briefings
        scheduleDaily<BriefingWorker>("briefing_${BriefingWorker.SLOT_MORNING}",
            BriefingWorker.KEY_SLOT, BriefingWorker.SLOT_MORNING,
            hour = 7, minute = 30, tag = TAG_BRIEFING)
        scheduleWeekly<BriefingWorker>("briefing_${BriefingWorker.SLOT_SUNDAY}",
            BriefingWorker.KEY_SLOT, BriefingWorker.SLOT_SUNDAY,
            dayOfWeek = Calendar.SUNDAY, hour = 20, minute = 0, tag = TAG_BRIEFING)
    }

    private inline fun <reified W : androidx.work.ListenableWorker> scheduleDaily(
        uniqueName: String,
        slotKey: String,
        slotValue: String,
        hour: Int,
        minute: Int,
        tag: String
    ) {
        val request = PeriodicWorkRequestBuilder<W>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelayMinutesToTimeOfDay(hour, minute), TimeUnit.MINUTES)
            .setInputData(Data.Builder().putString(slotKey, slotValue).build())
            .addTag(tag)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            uniqueName, ExistingPeriodicWorkPolicy.UPDATE, request
        )
    }

    private inline fun <reified W : androidx.work.ListenableWorker> scheduleWeekly(
        uniqueName: String,
        slotKey: String,
        slotValue: String,
        dayOfWeek: Int,
        hour: Int,
        minute: Int,
        tag: String
    ) {
        val request = PeriodicWorkRequestBuilder<W>(7, TimeUnit.DAYS)
            .setInitialDelay(initialDelayMinutesToDayAndTime(dayOfWeek, hour, minute), TimeUnit.MINUTES)
            .setInputData(Data.Builder().putString(slotKey, slotValue).build())
            .addTag(tag)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            uniqueName, ExistingPeriodicWorkPolicy.UPDATE, request
        )
    }

    private fun initialDelayMinutesToTimeOfDay(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (!target.after(now)) target.add(Calendar.DAY_OF_MONTH, 1)
        return (target.timeInMillis - now.timeInMillis) / 60_000L
    }

    private fun initialDelayMinutesToDayAndTime(dayOfWeek: Int, hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, dayOfWeek)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (!target.after(now)) target.add(Calendar.WEEK_OF_YEAR, 1)
        return (target.timeInMillis - now.timeInMillis) / 60_000L
    }

    companion object {
        const val TAG_MISSION_REMINDER = "mission_reminder"
        const val TAG_LOGGING_REMINDER = "logging_reminder"
        const val TAG_BRIEFING = "briefing"
    }
}
