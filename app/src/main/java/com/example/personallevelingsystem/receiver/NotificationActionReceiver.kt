package com.example.personallevelingsystem.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.example.personallevelingsystem.data.AppDatabase
import com.example.personallevelingsystem.model.Sleep
import com.example.personallevelingsystem.model.Water
import com.example.personallevelingsystem.repository.MissionRepository
import com.example.personallevelingsystem.service.MissionAutoCompleter
import com.example.personallevelingsystem.util.MissionPrefs
import com.example.personallevelingsystem.util.formatSleepDuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Handles inline action buttons posted on logging / mission reminders.
 *
 * Actions:
 *  - ACTION_LOG_WATER     extra: amount_ml (Float)
 *  - ACTION_LOG_SLEEP     extra: hours (Float)
 *  - ACTION_COMPLETE_MISSION  extra: mission_id (String)
 *  - ACTION_SNOOZE        extra: slot (String), notification_id (Int)
 */
class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val ctx = context.applicationContext
        val action = intent.action ?: return
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)

        when (action) {
            ACTION_LOG_WATER -> {
                val ml = intent.getFloatExtra(EXTRA_AMOUNT_ML, 0f)
                if (ml > 0f) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val db = AppDatabase.getDatabase(ctx)
                        db.WaterDao().insert(Water(date = System.currentTimeMillis(), amount = ml))
                        MissionAutoCompleter(ctx).sweep()
                    }
                }
                dismiss(ctx, notificationId)
            }

            ACTION_LOG_SLEEP -> {
                val hours = intent.getFloatExtra(EXTRA_HOURS, 0f)
                if (hours > 0f) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val db = AppDatabase.getDatabase(ctx)
                        // Same "HH:MM" shape the sleep screen writes
                        db.SleepTimeDao().insert(Sleep(date = System.currentTimeMillis(), duration = formatSleepDuration(hours)))
                        MissionAutoCompleter(ctx).sweep()
                    }
                }
                dismiss(ctx, notificationId)
            }

            ACTION_COMPLETE_MISSION -> {
                val missionId = intent.getStringExtra(EXTRA_MISSION_ID) ?: return
                CoroutineScope(Dispatchers.IO).launch {
                    val mission = MissionRepository(ctx).findById(missionId) ?: return@launch
                    // Locked path: no-op if the app or a sweep already completed it
                    MissionAutoCompleter(ctx).complete(mission)
                }
                dismiss(ctx, notificationId)
            }

            ACTION_SNOOZE -> {
                val slot = intent.getStringExtra(EXTRA_SLOT) ?: return
                MissionPrefs.get(ctx).snoozeSlot(slot, System.currentTimeMillis() + 60 * 60_000L)
                dismiss(ctx, notificationId)
            }
        }
    }

    private fun dismiss(ctx: Context, id: Int) {
        if (id != -1) NotificationManagerCompat.from(ctx).cancel(id)
    }

    companion object {
        const val ACTION_LOG_WATER = "com.example.personallevelingsystem.LOG_WATER"
        const val ACTION_LOG_SLEEP = "com.example.personallevelingsystem.LOG_SLEEP"
        const val ACTION_COMPLETE_MISSION = "com.example.personallevelingsystem.COMPLETE_MISSION"
        const val ACTION_SNOOZE = "com.example.personallevelingsystem.SNOOZE"

        const val EXTRA_AMOUNT_ML = "amount_ml"
        const val EXTRA_HOURS = "hours"
        const val EXTRA_MISSION_ID = "mission_id"
        const val EXTRA_SLOT = "slot"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
    }
}
