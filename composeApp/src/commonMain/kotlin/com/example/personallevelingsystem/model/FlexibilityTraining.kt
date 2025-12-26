package com.example.personallevelingsystem.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flexibility_training")
data class FlexibilityTraining(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long,
    val duration: Long
)
