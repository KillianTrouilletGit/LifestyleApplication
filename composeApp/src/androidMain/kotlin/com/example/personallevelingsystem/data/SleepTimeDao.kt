package com.example.personallevelingsystem.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.personallevelingsystem.model.Sleep
import com.example.personallevelingsystem.model.Water

@Dao
interface SleepTimeDao {
    @Insert
    suspend fun insert(sleep: Sleep)
    @Query("DELETE FROM sleep")
    fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(users: List<Sleep>)

    @Query("SELECT * FROM sleep")
    suspend fun getAll(): List<Sleep>

    @Query("SELECT * FROM sleep ORDER BY date DESC")
    fun getAllSleepingTimes(): LiveData<List<Sleep>>
    @Query("SELECT * FROM sleep WHERE date >= :startOfDay AND date <= :endOfDay")
    suspend fun getSleepForDay(startOfDay: Long, endOfDay: Long): List<Sleep>
}