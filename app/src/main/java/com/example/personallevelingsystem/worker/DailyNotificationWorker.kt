package com.example.personallevelingsystem.worker


import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.personallevelingsystem.repository.MissionRepository

class DailyNotificationWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val missionRepository = MissionRepository(applicationContext)
        missionRepository.updateMissionNotification()
        return Result.success()
    }
}
