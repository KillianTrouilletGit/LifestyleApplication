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
import com.example.personallevelingsystem.ui.MissionsListActivity

object NotificationUtils {

    private const val CHANNEL_ID = "level_up_channel"
    private const val PERMANENT_NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Level Up"
            val descriptionText = "Notifications for level up"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showLevelUpNotification(context: Context, level: Int) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_user)
            .setContentTitle("Level Up!")
            .setContentText("Congratulations! You've reached level $level.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setColor(ContextCompat.getColor(context, R.color.black))
        with(NotificationManagerCompat.from(context)) {
            notify(level, builder.build())
        }
    }

    private fun showPermanentMissionNotification(context: Context, dailyMissionsLeft: Int, weeklyMissionsLeft: Int) {
        val intent = Intent(context, MissionsListActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val notificationManager = NotificationManagerCompat.from(context)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_missions)
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
}
