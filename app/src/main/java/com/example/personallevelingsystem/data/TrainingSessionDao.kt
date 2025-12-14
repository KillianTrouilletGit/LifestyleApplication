package com.example.personallevelingsystem.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.personallevelingsystem.model.Exercise
import com.example.personallevelingsystem.model.TrainingSession
import com.example.personallevelingsystem.model.TrainingSet
import java.util.Date

@Dao
interface TrainingSessionDao {
    @Insert
    fun insertTrainingSession(trainingSession: TrainingSession): Long
    @Query("DELETE FROM training_sessions")
    fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(users: List<TrainingSession>)

    @Update
    fun updateTrainingSession(trainingSession: TrainingSession)

    @Insert
    fun insertTrainingSet(trainingSet: TrainingSet)

    @Query("SELECT * FROM training_sessions")
    suspend fun getAll(): List<TrainingSession>

    @Query("SELECT * FROM training_sessions WHERE id = :trainingSessionId")
    fun getTrainingSessionById(trainingSessionId: Long): TrainingSession

    @Query("""
    SELECT ts.* FROM training_sessions ts
    JOIN sessions s ON ts.sessionId = s.id
    WHERE s.programId = :programId AND ts.date BETWEEN :startOfWeek AND :endOfWeek
    """)
    suspend fun getCompletedSessionsForProgramInWeek(programId: Long, startOfWeek: Long, endOfWeek: Long): List<TrainingSession>

    @Query("SELECT * FROM training_sets WHERE exerciseId = :exerciseId AND trainingSessionId < :currentSessionId ORDER BY trainingSessionId DESC LIMIT :limit")
    fun getPreviousTrainingSets(exerciseId: Long, currentSessionId: Long, limit: Int = 1): List<TrainingSet>

    @Query("SELECT * FROM training_sessions WHERE date >= :startOfWeek AND date <= :endOfWeek")
    suspend fun getTrainingSessionsForWeek(startOfWeek: Long, endOfWeek: Long): List<TrainingSession>

}
