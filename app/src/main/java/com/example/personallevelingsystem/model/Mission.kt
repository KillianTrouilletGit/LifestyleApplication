package com.example.personallevelingsystem.model

data class Mission(
    val id: String,
    val title: String,
    val description: String,
    val type: MissionType,
    val category: MissionCategory,
    val isCompleted: Boolean,
    val reward: Int,
    val difficulty: MissionDifficulty = MissionDifficulty.NORMAL,
    val requirement: MissionRequirement = MissionRequirement.Manual,
    val deeplinkRoute: String? = null,
    val tip: String? = null
)

enum class MissionType {
    DAILY,
    WEEKLY
}

enum class MissionCategory {
    BODY,        // training, flexibility, endurance
    MIND,        // learning, meditation
    NUTRITION,   // water, food
    RECOVERY,    // sleep, hygiene
    DISCIPLINE,  // planning, appearance, environment
    PROGRESS     // tracking, reporting, weighing
}

enum class MissionDifficulty {
    NORMAL,
    HARD,
    ELITE
}

/**
 * Describes how a mission can be auto-completed from tracked data.
 *
 * Manual: only the checkbox can flip it.
 * Anything else carries a target so the UI can show inline progress
 * ("1850 / 2500 ml") and the MissionAutoCompleter can tick it off.
 */
sealed class MissionRequirement {
    object Manual : MissionRequirement()

    /** Total water intake today in ml. Target uses 35 ml/kg if user.weight > 0, else 2500. */
    object WaterDailyTarget : MissionRequirement()

    /** Last-night sleep >= hours. */
    data class SleepHoursAtLeast(val hours: Float) : MissionRequirement()

    /** At least one flexibility entry today with duration >= minutes. */
    data class FlexibilityMinutes(val minutes: Int) : MissionRequirement()

    /** Today's average meal balance index >= threshold (0..1). */
    data class NutritionBalance(val threshold: Float) : MissionRequirement()

    /** Cumulative endurance distance this week >= km. */
    data class EnduranceWeeklyKm(val km: Float) : MissionRequirement()

    /** Any training session logged today (counts as workout discipline). */
    object TrainingSessionLoggedToday : MissionRequirement()

    /** User weight last updated within the current ISO week. */
    object WeightLoggedThisWeek : MissionRequirement()
}
