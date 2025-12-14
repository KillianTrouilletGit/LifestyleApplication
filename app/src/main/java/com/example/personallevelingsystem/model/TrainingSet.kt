package com.example.personallevelingsystem.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "training_sets",
    foreignKeys = [
        ForeignKey(entity = Exercise::class, parentColumns = ["id"], childColumns = ["exerciseId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = TrainingSession::class, parentColumns = ["id"], childColumns = ["trainingSessionId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index(value = ["exerciseId"]), Index(value = ["trainingSessionId"])]
)
data class TrainingSet(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val trainingSessionId: Long,
    val exerciseId: Long,
    var reps: Int,
    var weight: Float
)
