package com.example.personallevelingsystem.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.personallevelingsystem.util.NotificationUtils

class TrainingTimerService : Service() {

    override fun onCreate() {
        super.onCreate()
        NotificationUtils.createNotificationChannel(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val exerciseName = intent?.getStringExtra(EXTRA_EXERCISE_NAME) ?: "Training"
        val startTime = intent?.getLongExtra(EXTRA_START_TIME, System.currentTimeMillis()) ?: System.currentTimeMillis()

        when (action) {
            ACTION_START, ACTION_UPDATE -> {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    try {
                        startForeground(
                            NotificationUtils.TRAINING_NOTIFICATION_ID,
                            NotificationUtils.buildTimerNotification(this, exerciseName, startTime),
                            android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        startForeground(
                            NotificationUtils.TRAINING_NOTIFICATION_ID,
                            NotificationUtils.buildTimerNotification(this, exerciseName, startTime)
                        )
                    }
                } else {
                    startForeground(
                        NotificationUtils.TRAINING_NOTIFICATION_ID,
                        NotificationUtils.buildTimerNotification(this, exerciseName, startTime)
                    )
                }
            }
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "com.example.personallevelingsystem.START_TIMER"
        const val ACTION_UPDATE = "com.example.personallevelingsystem.UPDATE_TIMER"
        const val ACTION_STOP = "com.example.personallevelingsystem.STOP_TIMER"
        
        const val EXTRA_EXERCISE_NAME = "extra_exercise_name"
        const val EXTRA_START_TIME = "extra_start_time"
    }
}
