package com.example.personallevelingsystem.model


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "water")
data class Water(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long,
    val amount: Float
)