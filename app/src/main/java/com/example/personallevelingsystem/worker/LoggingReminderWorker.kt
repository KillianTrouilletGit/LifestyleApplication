package com.example.personallevelingsystem.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.personallevelingsystem.R
import com.example.personallevelingsystem.data.AppDatabase
import com.example.personallevelingsystem.service.MissionAutoCompleter
import com.example.personallevelingsystem.util.NotificationUtils
import java.util.Calendar

/**
 * Logging reminders — fire when the user hasn't entered expected info.
 *
 * Slots:
 *  - water_morning        (10:00) — no water entry yet today
 *  - water_afternoon      (15:00) — under target by mid-afternoon
 *  - sleep_morning        (09:00) — no sleep entry for last night
 *  - nutrition_lunch      (13:30) — no meal logged since breakfast window
 *  - nutrition_dinner     (20:30) — no dinner logged
 *  - weight_sunday        (Sun 10:00) — weekly weigh-in
 *  - planning_evening     (21:00) — set tomorrow's plan
 *
 * Each slot is silent when the underlying data is already present.
 * Each carries inline action buttons where it makes sense (water, sleep).
 */
class LoggingReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val slot = inputData.getString(KEY_SLOT) ?: return Result.success()
        val db = AppDatabase.getDatabase(applicationContext)
        val (startOfDay, endOfDay) = todayBounds()
        val autoCompleter = MissionAutoCompleter(applicationContext)
        autoCompleter.sweep()

        when (slot) {
            SLOT_WATER_MORNING, SLOT_WATER_AFTERNOON -> {
                val entries = db.WaterDao().getWaterForDay(startOfDay, endOfDay)
                val totalMl = entries.sumOf { it.amount.toDouble() }
                val target = autoCompleter.waterTargetMl().toDouble()

                if (slot == SLOT_WATER_MORNING && entries.isEmpty()) {
                    NotificationUtils.showLoggingReminder(
                        context = applicationContext,
                        id = NotificationUtils.WATER_LOG_ID,
                        slot = slot,
                        title = "Hydration — Zero logged",
                        body = "Open the water tracker and log your first intake. Target ≈ ${target.toInt()} ml today.",
                        deeplinkRoute = "water",
                        iconRes = R.drawable.ic_water_v2,
                        waterActions = true
                    )
                } else if (slot == SLOT_WATER_AFTERNOON && totalMl < target * 0.5) {
                    val left = (target - totalMl).coerceAtLeast(0.0).toInt()
                    NotificationUtils.showLoggingReminder(
                        context = applicationContext,
                        id = NotificationUtils.WATER_LOG_ID,
                        slot = slot,
                        title = "Hydration — Behind pace",
                        body = "You're at ${totalMl.toInt()} / ${target.toInt()} ml. About $left ml left to clear the quota.",
                        deeplinkRoute = "water",
                        iconRes = R.drawable.ic_water_v2,
                        waterActions = true
                    )
                }
            }

            SLOT_SLEEP_MORNING -> {
                val sleepEntries = db.SleepTimeDao().getSleepForDay(startOfDay, endOfDay)
                if (sleepEntries.isEmpty()) {
                    NotificationUtils.showLoggingReminder(
                        context = applicationContext,
                        id = NotificationUtils.SLEEP_LOG_ID,
                        slot = slot,
                        title = "Log Last Night's Recovery",
                        body = "How many hours did you actually sleep? Tap a button below or open the sleep tracker.",
                        deeplinkRoute = "sleep",
                        iconRes = R.drawable.ic_sleep_v2,
                        sleepActions = true
                    )
                }
            }

            SLOT_NUTRITION_LUNCH -> {
                val meals = db.mealDao().getMealForDay(startOfDay, endOfDay)
                if (meals.isEmpty()) {
                    NotificationUtils.showLoggingReminder(
                        context = applicationContext,
                        id = NotificationUtils.NUTRITION_LOG_ID,
                        slot = slot,
                        title = "No meals logged",
                        body = "Already past lunch and the nutrition log is empty. Snap a meal to keep your fuel stats accurate.",
                        deeplinkRoute = "nutrition",
                        iconRes = R.drawable.ic_nutrition_v2
                    )
                }
            }

            SLOT_NUTRITION_DINNER -> {
                val meals = db.mealDao().getMealForDay(startOfDay, endOfDay)
                if (meals.size < 2) {
                    NotificationUtils.showLoggingReminder(
                        context = applicationContext,
                        id = NotificationUtils.NUTRITION_LOG_ID,
                        slot = slot,
                        title = "Dinner check-in",
                        body = "Log tonight's meal before the day closes — balance index needs the data.",
                        deeplinkRoute = "nutrition",
                        iconRes = R.drawable.ic_nutrition_v2
                    )
                }
            }

            SLOT_WEIGHT_SUNDAY -> {
                NotificationUtils.showLoggingReminder(
                    context = applicationContext,
                    id = NotificationUtils.WEIGHT_LOG_ID,
                    slot = slot,
                    title = "Weekly Weigh-In",
                    body = "Same time, same conditions. Update your weight in the profile to track the trend.",
                    deeplinkRoute = "profile",
                    iconRes = R.drawable.ic_profile_v2
                )
            }

            SLOT_PLANNING_EVENING -> {
                NotificationUtils.showLoggingReminder(
                    context = applicationContext,
                    id = NotificationUtils.PLANNING_LOG_ID,
                    slot = slot,
                    title = "Tomorrow's Briefing",
                    body = "Set 3 priorities and lock the schedule. Tomorrow-you will thank you.",
                    deeplinkRoute = "planning",
                    iconRes = R.drawable.ic_planning_v2
                )
            }
        }

        return Result.success()
    }

    private fun todayBounds(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val end = calendar.timeInMillis
        return start to end
    }

    companion object {
        const val KEY_SLOT = "slot"
        const val SLOT_WATER_MORNING = "water_morning"
        const val SLOT_WATER_AFTERNOON = "water_afternoon"
        const val SLOT_SLEEP_MORNING = "sleep_morning"
        const val SLOT_NUTRITION_LUNCH = "nutrition_lunch"
        const val SLOT_NUTRITION_DINNER = "nutrition_dinner"
        const val SLOT_WEIGHT_SUNDAY = "weight_sunday"
        const val SLOT_PLANNING_EVENING = "planning_evening"
    }
}
