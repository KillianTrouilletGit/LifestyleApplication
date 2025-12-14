package com.example.personallevelingsystem.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.personallevelingsystem.repository.MissionRepository

class DailyMissionResetWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val repository = MissionRepository(applicationContext)
        repository.resetDailyMissions()
        return Result.success()
    }
}
