package com.example.personallevelingsystem.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "training_sessions")
data class TrainingSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val date: Long,
    val sessionId: Long,
    val startTime: Date,
    val endTime: Date?,
    val weekId: Long
){
    // Calcul de la durée en millisecondes
    val durationInMillis: Long
        get() = (endTime?.time ?: startTime.time) - startTime.time
}
