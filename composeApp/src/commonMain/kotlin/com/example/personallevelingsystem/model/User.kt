package com.example.personallevelingsystem.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var name: String,
    var xp: Int = 0,
    var level: Int = 1,
    var weight: Float = 0f,
    var height: Float = 0f,
    var dateOfBirth: String = ""


)

