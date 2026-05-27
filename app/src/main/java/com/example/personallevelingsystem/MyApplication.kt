package com.example.personallevelingsystem

import android.app.Application
import com.example.personallevelingsystem.data.AppDatabase
import com.example.personallevelingsystem.scheduler.ReminderScheduler


class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        com.example.personallevelingsystem.util.NotificationUtils.createNotificationChannel(this)
        ReminderScheduler(this).scheduleAll()
    }
}
