package com.example.personallevelingsystem.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sessions",
    foreignKeys = [ForeignKey(entity = Program::class, parentColumns = ["id"], childColumns = ["programId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["programId"])]
)
data class Session(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val programId: Long,
    val name: String
)
