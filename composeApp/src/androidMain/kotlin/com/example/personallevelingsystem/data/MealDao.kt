package com.example.personallevelingsystem.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.personallevelingsystem.model.Meal
import com.example.personallevelingsystem.model.User
import com.example.personallevelingsystem.model.Water
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Dao
interface MealDao {
    @Insert
    suspend fun insert(meal: Meal)
    @Query("SELECT * FROM meals")
    suspend fun getAll(): List<Meal>
    @Query("DELETE FROM meals")
    fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(users: List<Meal>)

    @Query("SELECT SUM(calories) FROM meals WHERE date >= :startOfDay AND date < :startOfNextDay")
    suspend fun getTotalCaloriesForCurrentDay(startOfDay: Long, startOfNextDay: Long): Double?
    @Query("SELECT * FROM meals WHERE date >= :startOfDay AND date <= :endOfDay")
    suspend fun getMealForDay(startOfDay: Long, endOfDay: Long): List<Meal>
}
