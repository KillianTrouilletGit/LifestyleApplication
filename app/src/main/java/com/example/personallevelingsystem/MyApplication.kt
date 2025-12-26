package com.example.personallevelingsystem

import android.app.Application
import com.example.personallevelingsystem.data.AppDatabase


class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        com.example.personallevelingsystem.util.NotificationUtils.createNotificationChannel(this)
        // WorkManagerScheduler.scheduleHourlySync(this) // Removed for GitHub cleanup
    }
}
