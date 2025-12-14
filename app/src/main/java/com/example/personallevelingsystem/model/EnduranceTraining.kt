package com.example.personallevelingsystem.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "endurance_training")
data class EnduranceTraining(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long,
    val duration: Long,
    val distance: Float
)