package com.example.personallevelingsystem.data


import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.personallevelingsystem.model.Water

@Dao
interface WaterDao {
    @Insert
    suspend fun insert(water: Water)
    @Query("DELETE FROM water")
    fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(users: List<Water>)


    @Query("SELECT * FROM water")
    suspend fun getAll(): List<Water>

    @Query("SELECT * FROM water ORDER BY date DESC")
    fun getAllWaterAmounts(): LiveData<List<Water>>

    @Query("SELECT * FROM water WHERE date >= :startOfDay AND date <= :endOfDay")
    suspend fun getWaterForDay(startOfDay: Long, endOfDay: Long): List<Water>
}