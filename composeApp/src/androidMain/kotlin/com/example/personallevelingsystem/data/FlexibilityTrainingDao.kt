package com.example.personallevelingsystem.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.personallevelingsystem.model.FlexibilityTraining
import com.example.personallevelingsystem.model.Water

@Dao
interface FlexibilityTrainingDao {
    @Insert
    suspend fun insert(flexibilityTraining: FlexibilityTraining)
    @Query("SELECT * FROM flexibility_training")
    suspend fun getAll(): List<FlexibilityTraining>
    @Query("DELETE FROM flexibility_training")
    fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(users: List<FlexibilityTraining>)

    @Query("SELECT * FROM flexibility_training ORDER BY date DESC")
    fun getAllFlexibilityTrainings(): LiveData<List<FlexibilityTraining>>

    @Query("SELECT * FROM flexibility_training WHERE date >= :startOfDay AND date <= :endOfDay")
    suspend fun getFlexForDay(startOfDay: Long, endOfDay: Long): List<FlexibilityTraining>
}
