package com.example.personallevelingsystem.model

data class Mission(
    val id: String,
    val description: String,
    val type: MissionType,
    val isCompleted: Boolean,
    val reward: Int // Reward points or other types of rewards
)

enum class MissionType {
    DAILY,
    WEEKLY
}
