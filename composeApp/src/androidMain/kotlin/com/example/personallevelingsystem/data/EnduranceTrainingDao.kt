package com.example.personallevelingsystem.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.personallevelingsystem.model.EnduranceTraining
import com.example.personallevelingsystem.model.Water

@Dao
interface EnduranceTrainingDao {
    @Insert
    suspend fun insert(enduranceTraining: EnduranceTraining)
    @Query("DELETE FROM endurance_training")
    fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(users: List<EnduranceTraining>)

    @Query("SELECT * FROM endurance_training")
    suspend fun getAll(): List<EnduranceTraining>

    @Query("SELECT * FROM endurance_training ORDER BY date DESC")
    fun getAllEnduranceTrainings(): LiveData<List<EnduranceTraining>>

    @Query("SELECT SUM(distance) FROM endurance_training WHERE date >= :startOfWeek AND date <= :endOfWeek")
    suspend fun getTotalDistanceForWeek(startOfWeek: Long, endOfWeek: Long): Float?




    @Query("SELECT * FROM endurance_training WHERE date >= :startOfWeek AND date <= :endOfWeek")
    suspend fun getEnduranceForWeek(startOfWeek: Long, endOfWeek: Long): List<EnduranceTraining>
}
