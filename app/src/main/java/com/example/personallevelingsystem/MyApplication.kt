package com.example.personallevelingsystem

import android.app.Application
import com.example.personallevelingsystem.data.AppDatabase
import com.example.personallevelingsystem.repository.UserRepository
import com.example.personallevelingsystem.scheduler.ReminderScheduler
import com.example.personallevelingsystem.util.MissionPrefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch


class MyApplication : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        com.example.personallevelingsystem.util.NotificationUtils.createNotificationChannel(this)
        ReminderScheduler(this).scheduleAll()

        appScope.launch {
            val db = AppDatabase.getDatabase(this@MyApplication)
            // XP is credited to user #1 from day one — make sure the row exists.
            UserRepository(db.userDao(), this@MyApplication).ensureDefaultUser()

            // One-shot: the water screen used to accept litres while every
            // consumer (targets, notifications, bio-metrics) expects ml.
            val prefs = MissionPrefs.get(this@MyApplication)
            if (!prefs.isWaterNormalizedToMl()) {
                db.WaterDao().normalizeLitresToMl()
                prefs.markWaterNormalizedToMl()
            }
        }
    }
}
