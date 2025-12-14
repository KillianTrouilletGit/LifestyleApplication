package com.example.personallevelingsystem.receiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.personallevelingsystem.worker.WeeklyMissionResetWorker

class WeeklyMissionResetReceiver : BroadcastReceiver() {
    @SuppressLint("LongLogTag")
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("WeeklyMissionResetWorker", "Daily mission reset worker executed")
        val workRequest = OneTimeWorkRequestBuilder<WeeklyMissionResetWorker>().build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }
}
