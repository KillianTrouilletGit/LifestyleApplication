package com.example.personallevelingsystem.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.personallevelingsystem.worker.DailyMissionResetWorker

class DailyMissionResetReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("DailyMissionResetWorker", "Weekly mission reset worker executed")
        val workRequest = OneTimeWorkRequestBuilder<DailyMissionResetWorker>().build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }
}
