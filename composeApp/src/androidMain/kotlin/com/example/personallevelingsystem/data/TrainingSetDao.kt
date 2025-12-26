package com.example.personallevelingsystem.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.personallevelingsystem.model.Exercise
import com.example.personallevelingsystem.model.Session
import com.example.personallevelingsystem.model.TrainingSet

@Dao
interface TrainingSetDao {
    @Insert
    suspend fun insert(trainingSet: TrainingSet)
    @Query("DELETE FROM training_sets")
    fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(users: List<TrainingSet>)


    @Query("SELECT * FROM training_sets")
    suspend fun getAll(): List<TrainingSet>
}