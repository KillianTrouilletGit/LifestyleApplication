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


object NotificationUtils {

    private const val CHANNEL_ID = "level_up_channel"
    private const val TRAINING_CHANNEL_ID = "training_timer_channel"
    private const val PERMANENT_NOTIFICATION_ID = 1001
    const val TRAINING_NOTIFICATION_ID = 2001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Level Up Channel
            val name = "Level Up"
            val descriptionText = "Notifications for level up"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)

            // Training Timer Channel
            val trainingName = "Training Timer"
            val trainingDesc = "Persistent timer for active training sessions"
            val trainingImportance = NotificationManager.IMPORTANCE_DEFAULT 
            val trainingChannel = NotificationChannel(TRAINING_CHANNEL_ID, trainingName, trainingImportance).apply {
                description = trainingDesc
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(trainingChannel)
        }
    }

    fun showLevelUpNotification(context: Context, level: Int) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_profile_v2)
            .setContentTitle("Level Up!")
            .setContentText("Congratulations! You've reached level $level.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setColor(ContextCompat.getColor(context, R.color.black))
        with(NotificationManagerCompat.from(context)) {
            notify(level, builder.build())
        }
    }

    private fun showPermanentMissionNotification(context: Context, dailyMissionsLeft: Int, weeklyMissionsLeft: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val notificationManager = NotificationManagerCompat.from(context)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_missions_v2)
            .setContentTitle("Mission Status")
            .setContentText("Daily missions left: $dailyMissionsLeft, Weekly missions left: $weeklyMissionsLeft")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true) // Makes the notification permanent
            .setColor(ContextCompat.getColor(context, R.color.black))
            .setContentIntent(pendingIntent)

        notificationManager.notify(PERMANENT_NOTIFICATION_ID, builder.build())
    }

    fun updatePermanentMissionNotification(context: Context, dailyMissionsLeft: Int, weeklyMissionsLeft: Int) {
        showPermanentMissionNotification(context, dailyMissionsLeft, weeklyMissionsLeft)
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
            .setStyle(NotificationCompat.DecoratedCustomViewStyle()) // Allows system decorations if needed, or remove for raw custom view
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(true) 
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(pendingIntent)
            .build()
    }
}
