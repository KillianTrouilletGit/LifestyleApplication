package com.example.personallevelingsystem.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.personallevelingsystem.model.Exercise

@Dao
interface ExerciseDao {
    @Insert
    suspend fun insert(exercise: Exercise)

    @Query("DELETE FROM exercises")
    fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(users: List<Exercise>)

    @Query("SELECT * FROM exercises")
    suspend fun getAll(): List<Exercise>
}