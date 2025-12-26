package com.example.personallevelingsystem.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.personallevelingsystem.model.Exercise
import com.example.personallevelingsystem.model.Session

@Dao
interface SessionDao {
    @Insert
    suspend fun insert(session: Session)
    @Query("DELETE FROM sessions")
    fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(users: List<Session>)


    @Query("SELECT * FROM sessions")
    suspend fun getAll(): List<Session>
}