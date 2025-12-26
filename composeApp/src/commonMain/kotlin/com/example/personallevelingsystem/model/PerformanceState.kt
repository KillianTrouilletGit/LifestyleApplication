package com.example.personallevelingsystem.model

data class PerformanceState(
    val level: Int = 1,
    val currentXp: Float = 0f,
    val requiredXp: Float = 1000f,
    val missionEfficiency: Float = 0f, // 0.0 to 1.0
    val dailyMissionsCompleted: Int = 0,
    val totalDailyMissions: Int = 0,
    val weeklyTrainingFrequency: List<Float> = List(7) { 0f }, // Last 7 days, 0.0 to 1.0 intensity
    val sleepHours: Float = 0f,
    val waterIntake: Float = 0f
)
