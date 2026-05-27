package com.example.personallevelingsystem.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.personallevelingsystem.MainActivity
import com.example.personallevelingsystem.R
import com.example.personallevelingsystem.model.Mission
import com.example.personallevelingsystem.receiver.NotificationActionReceiver
import java.util.Calendar


object NotificationUtils {

    private const val CHANNEL_ID = "level_up_channel"
    private const val TRAINING_CHANNEL_ID = "training_timer_channel"
    const val MISSION_REMINDER_CHANNEL_ID = "mission_reminder_channel"
    const val LOGGING_REMINDER_CHANNEL_ID = "logging_reminder_channel"
    const val LAST_CALL_CHANNEL_ID = "last_call_channel"
    const val BRIEFING_CHANNEL_ID = "briefing_channel"
    const val ACHIEVEMENT_CHANNEL_ID = "achievement_channel"

    private const val PERMANENT_NOTIFICATION_ID = 1001
    const val TRAINING_NOTIFICATION_ID = 2001

    // Reminder IDs
    const val DAILY_DIGEST_ID = 3000
    const val MIDDAY_MISSION_ID = 3001
    const val EVENING_MISSION_ID = 3002
    const val LAST_CALL_DAILY_ID = 3003
    const val LAST_CALL_WEEKLY_ID = 3004
    const val WATER_LOG_ID = 4001
    const val NUTRITION_LOG_ID = 4002
    const val SLEEP_LOG_ID = 4003
    const val WEIGHT_LOG_ID = 4004
    const val PLANNING_LOG_ID = 4005
    const val MORNING_BRIEFING_ID = 5001
    const val WEEKLY_DEBRIEF_ID = 5002
    const val ACHIEVEMENT_BASE_ID = 6000

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            nm.createNotificationChannel(NotificationChannel(
                CHANNEL_ID, "Level Up", NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Notifications for level up"; setShowBadge(false) })

            nm.createNotificationChannel(NotificationChannel(
                TRAINING_CHANNEL_ID, "Training Timer", NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Persistent timer for active training sessions"; setShowBadge(false) })

            nm.createNotificationChannel(NotificationChannel(
                MISSION_REMINDER_CHANNEL_ID, "Mission Reminders", NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Periodic reminders for incomplete daily and weekly missions"; setShowBadge(true) })

            nm.createNotificationChannel(NotificationChannel(
                LOGGING_REMINDER_CHANNEL_ID, "Logging Reminders", NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Reminders to log missing data (water, sleep, nutrition, weight, planning)"; setShowBadge(true) })

            nm.createNotificationChannel(NotificationChannel(
                LAST_CALL_CHANNEL_ID, "Last Call", NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Urgent reminders just before daily / weekly mission reset"; setShowBadge(true) })

            nm.createNotificationChannel(NotificationChannel(
                BRIEFING_CHANNEL_ID, "Daily Briefings", NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Morning briefing and Sunday weekly debrief"; setShowBadge(false) })

            nm.createNotificationChannel(NotificationChannel(
                ACHIEVEMENT_CHANNEL_ID, "Achievements", NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Streak milestones, level-ups, unlocks"; setShowBadge(true) })
        }
    }

    // --- Gatekeeper ------------------------------------------------------------

    /**
     * Returns true if the caller should suppress this notification because
     * the user has notifications off, slot disabled, slot snoozed, or we're
     * inside quiet hours (urgent reminders bypass quiet hours).
     */
    fun shouldSuppress(context: Context, slot: String? = null, urgent: Boolean = false): Boolean {
        val prefs = MissionPrefs.get(context)
        if (!prefs.areNotificationsEnabled()) return true
        if (slot != null) {
            if (!prefs.isSlotEnabled(slot)) return true
            if (System.currentTimeMillis() < prefs.getSnoozedUntil(slot)) return true
        }
        if (urgent) return false
        if (!prefs.areQuietHoursEnabled()) return false
        return inQuietHours(prefs.getQuietStartHour(), prefs.getQuietEndHour())
    }

    private fun inQuietHours(startHour: Int, endHour: Int): Boolean {
        val now = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        // Window may wrap midnight (e.g., 22 -> 7)
        return if (startHour <= endHour) now in startHour until endHour
        else now >= startHour || now < endHour
    }

    fun showLevelUpNotification(context: Context, level: Int) {
        if (shouldSuppress(context)) return
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_profile_v2)
            .setContentTitle("Level Up!")
            .setContentText("Congratulations! You've reached level $level.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setColor(ContextCompat.getColor(context, R.color.black))
        NotificationManagerCompat.from(context).notify(level, builder.build())
    }

    private fun showPermanentMissionNotification(context: Context, dailyMissionsLeft: Int, weeklyMissionsLeft: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_missions_v2)
            .setContentTitle("Mission Status")
            .setContentText("Daily missions left: $dailyMissionsLeft, Weekly missions left: $weeklyMissionsLeft")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setColor(ContextCompat.getColor(context, R.color.black))
            .setContentIntent(pendingIntent)

        NotificationManagerCompat.from(context).notify(PERMANENT_NOTIFICATION_ID, builder.build())
    }

    fun updatePermanentMissionNotification(context: Context, dailyMissionsLeft: Int, weeklyMissionsLeft: Int) {
        showPermanentMissionNotification(context, dailyMissionsLeft, weeklyMissionsLeft)
    }

    // --- Reminder builders -----------------------------------------------------

    fun showMissionReminder(
        context: Context,
        id: Int,
        slot: String,
        title: String,
        body: String,
        incompleteMissions: List<Mission>,
        urgent: Boolean = false
    ) {
        if (shouldSuppress(context, slot = slot, urgent = urgent)) return

        val channel = if (urgent) LAST_CALL_CHANNEL_ID else MISSION_REMINDER_CHANNEL_ID

        val pendingIntent = mainActivityPending(context, id)
        val expanded = buildMissionList(incompleteMissions)

        val builder = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_missions_v2)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$body\n\n$expanded"))
            .setPriority(if (urgent) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setColor(ContextCompat.getColor(context, R.color.black))
            .setContentIntent(pendingIntent)

        // Quick action: one-tap-complete the top-most easy mission, plus snooze.
        val topMission = incompleteMissions.firstOrNull()
        if (topMission != null) {
            builder.addAction(
                0,
                "DONE: ${topMission.title.take(20)}",
                missionCompletePending(context, id, topMission.id)
            )
        }
        builder.addAction(0, "SNOOZE 1H", snoozePending(context, id, slot))

        NotificationManagerCompat.from(context).notify(id, builder.build())
    }

    fun showLoggingReminder(
        context: Context,
        id: Int,
        slot: String,
        title: String,
        body: String,
        deeplinkRoute: String? = null,
        iconRes: Int = R.drawable.ic_missions_v2,
        waterActions: Boolean = false,
        sleepActions: Boolean = false
    ) {
        if (shouldSuppress(context, slot = slot)) return

        val pendingIntent = mainActivityPending(context, id, deeplinkRoute)

        val builder = NotificationCompat.Builder(context, LOGGING_REMINDER_CHANNEL_ID)
            .setSmallIcon(iconRes)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setColor(ContextCompat.getColor(context, R.color.black))
            .setContentIntent(pendingIntent)

        if (waterActions) {
            builder.addAction(0, "+250 ML", waterLogPending(context, id, 250f))
            builder.addAction(0, "+500 ML", waterLogPending(context, id, 500f))
            builder.addAction(0, "+750 ML", waterLogPending(context, id, 750f))
        }
        if (sleepActions) {
            builder.addAction(0, "7H", sleepLogPending(context, id, 7f))
            builder.addAction(0, "8H", sleepLogPending(context, id, 8f))
            builder.addAction(0, "9H", sleepLogPending(context, id, 9f))
        }
        if (!waterActions && !sleepActions) {
            builder.addAction(0, "SNOOZE 1H", snoozePending(context, id, slot))
        }

        NotificationManagerCompat.from(context).notify(id, builder.build())
    }

    /**
     * Single consolidated "Daily Status" card that lists every kind of pending
     * thing (missions + missing logs) at once. Used by the smart-consolidation
     * path when multiple reminder slots would otherwise fire near the same time.
     */
    fun showDailyDigest(
        context: Context,
        incompleteMissions: List<Mission>,
        missingLogs: List<String>,
        urgent: Boolean = false
    ) {
        if (shouldSuppress(context, urgent = urgent)) return

        val title = if (urgent) "Daily Status — Final Window" else "Daily Status"
        val missionLine = if (incompleteMissions.isEmpty()) "All missions cleared. Solid."
            else "${incompleteMissions.size} missions open"
        val logLine = if (missingLogs.isEmpty()) "All logs entered."
            else "Missing: ${missingLogs.joinToString(", ")}"

        val expandedBody = buildString {
            append(missionLine).append('\n').append(logLine)
            if (incompleteMissions.isNotEmpty()) {
                append("\n\n").append(buildMissionList(incompleteMissions))
            }
        }

        val channel = if (urgent) LAST_CALL_CHANNEL_ID else MISSION_REMINDER_CHANNEL_ID
        val builder = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_missions_v2)
            .setContentTitle(title)
            .setContentText("$missionLine · $logLine")
            .setStyle(NotificationCompat.BigTextStyle().bigText(expandedBody))
            .setPriority(if (urgent) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setColor(ContextCompat.getColor(context, R.color.black))
            .setContentIntent(mainActivityPending(context, DAILY_DIGEST_ID))

        NotificationManagerCompat.from(context).notify(DAILY_DIGEST_ID, builder.build())
    }

    fun showMorningBriefing(
        context: Context,
        sleepHours: Float,
        missionsToday: Int,
        firstPlanningItem: String?
    ) {
        if (shouldSuppress(context, slot = "morning_briefing")) return

        val sleepLine = when {
            sleepHours <= 0f -> "Recovery: unlogged. Tap to enter last night."
            sleepHours >= 7.5f -> "Recovery: ${"%.1f".format(sleepHours)}h — solid."
            sleepHours >= 6.5f -> "Recovery: ${"%.1f".format(sleepHours)}h — passable."
            else -> "Recovery: ${"%.1f".format(sleepHours)}h — drag day, manage load."
        }
        val planLine = firstPlanningItem?.let { "First block: $it" } ?: "No planning blocks loaded yet."
        val missionsLine = "$missionsToday daily ops on deck."

        val builder = NotificationCompat.Builder(context, BRIEFING_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_profile_v2)
            .setContentTitle("Morning Briefing")
            .setContentText(missionsLine)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$sleepLine\n$missionsLine\n$planLine"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setColor(ContextCompat.getColor(context, R.color.black))
            .setContentIntent(mainActivityPending(context, MORNING_BRIEFING_ID))

        NotificationManagerCompat.from(context).notify(MORNING_BRIEFING_ID, builder.build())
    }

    fun showWeeklyDebrief(
        context: Context,
        topCategory: String,
        weakestCategory: String,
        xpEarned: Int,
        currentLevel: Int,
        streakHeld: Int
    ) {
        if (shouldSuppress(context, slot = "weekly_debrief")) return

        val body = """
            +$xpEarned XP this week · Level $currentLevel
            Top focus: $topCategory
            Underweighted: $weakestCategory
            Streaks held: $streakHeld
        """.trimIndent()

        val builder = NotificationCompat.Builder(context, BRIEFING_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_missions_v2)
            .setContentTitle("Weekly Debrief")
            .setContentText("+$xpEarned XP · L$currentLevel · $streakHeld streaks")
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setColor(ContextCompat.getColor(context, R.color.black))
            .setContentIntent(mainActivityPending(context, WEEKLY_DEBRIEF_ID))

        NotificationManagerCompat.from(context).notify(WEEKLY_DEBRIEF_ID, builder.build())
    }

    fun showAchievement(
        context: Context,
        id: String,
        title: String,
        body: String
    ) {
        if (shouldSuppress(context)) return

        val builder = NotificationCompat.Builder(context, ACHIEVEMENT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_profile_v2)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setColor(ContextCompat.getColor(context, R.color.black))
            .setContentIntent(mainActivityPending(context, ACHIEVEMENT_BASE_ID + id.hashCode()))

        NotificationManagerCompat.from(context).notify(ACHIEVEMENT_BASE_ID + id.hashCode(), builder.build())
    }

    fun cancelReminder(context: Context, id: Int) {
        NotificationManagerCompat.from(context).cancel(id)
    }

    private fun buildMissionList(missions: List<Mission>): String {
        if (missions.isEmpty()) return ""
        val shown = missions.take(5)
        val extra = missions.size - shown.size
        val lines = shown.joinToString("\n") { "• ${it.title} (+${it.reward} XP)" }
        return if (extra > 0) "$lines\n+ $extra more" else lines
    }

    // --- PendingIntents --------------------------------------------------------

    private fun mainActivityPending(context: Context, requestCode: Int, route: String? = null): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            if (route != null) putExtra("deeplink_route", route)
        }
        return PendingIntent.getActivity(
            context, requestCode, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun waterLogPending(context: Context, notificationId: Int, ml: Float): PendingIntent {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_LOG_WATER
            putExtra(NotificationActionReceiver.EXTRA_AMOUNT_ML, ml)
            putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
        }
        return PendingIntent.getBroadcast(
            context, notificationId * 10 + ml.toInt(), intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun sleepLogPending(context: Context, notificationId: Int, hours: Float): PendingIntent {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_LOG_SLEEP
            putExtra(NotificationActionReceiver.EXTRA_HOURS, hours)
            putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
        }
        return PendingIntent.getBroadcast(
            context, notificationId * 10 + hours.toInt(), intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun missionCompletePending(context: Context, notificationId: Int, missionId: String): PendingIntent {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_COMPLETE_MISSION
            putExtra(NotificationActionReceiver.EXTRA_MISSION_ID, missionId)
            putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
        }
        return PendingIntent.getBroadcast(
            context, notificationId * 100 + missionId.hashCode(), intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun snoozePending(context: Context, notificationId: Int, slot: String): PendingIntent {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_SNOOZE
            putExtra(NotificationActionReceiver.EXTRA_SLOT, slot)
            putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
        }
        return PendingIntent.getBroadcast(
            context, notificationId * 1000 + slot.hashCode(), intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    fun buildTimerNotification(context: Context, exerciseName: String, startTimeMillis: Long): android.app.Notification {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val remoteViews = android.widget.RemoteViews(context.packageName, R.layout.notification_timer)
        remoteViews.setTextViewText(R.id.notification_exercise_name, exerciseName)
        remoteViews.setChronometer(R.id.notification_timer, startTimeMillis, null, true)

        return NotificationCompat.Builder(context, TRAINING_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_training_v2)
            .setCustomContentView(remoteViews)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(true)
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(pendingIntent)
            .build()
    }
}
