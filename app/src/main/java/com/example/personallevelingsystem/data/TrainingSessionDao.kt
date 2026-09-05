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

    /**
     * Sets logged for this exercise in the most recent earlier session, in the
     * order they were entered (set 1 first). TrainingSet has no set index, so
     * insertion order (id) is the only thing that identifies "set N".
     */
    @Query("""
        SELECT * FROM training_sets
        WHERE exerciseId = :exerciseId
          AND trainingSessionId = (
              SELECT MAX(trainingSessionId) FROM training_sets
              WHERE exerciseId = :exerciseId AND trainingSessionId < :currentSessionId
          )
        ORDER BY id ASC
    """)
    fun getPreviousTrainingSets(exerciseId: Long, currentSessionId: Long): List<TrainingSet>

    @Query("SELECT * FROM training_sessions WHERE date >= :startOfWeek AND date <= :endOfWeek")
    suspend fun getTrainingSessionsForWeek(startOfWeek: Long, endOfWeek: Long): List<TrainingSession>

    @Query("SELECT COUNT(*) FROM training_sessions WHERE date >= :startOfDay AND date <= :endOfDay AND endTime IS NOT NULL")
    suspend fun countCompletedSessionsForDay(startOfDay: Long, endOfDay: Long): Int

    @Query("""
        SELECT s.date AS date, MAX(t.weight) AS topWeight
        FROM training_sets t
        JOIN training_sessions s ON t.trainingSessionId = s.id
        WHERE t.exerciseId = :exerciseId AND t.weight > 0
        GROUP BY t.trainingSessionId
        ORDER BY s.date ASC
    """)
    suspend fun getExerciseWeightHistory(exerciseId: Long): List<ExerciseHistoryPoint>

}

/** One training day's best weight for an exercise — feeds the evolution chart. */
data class ExerciseHistoryPoint(
    val date: Long,
    val topWeight: Float
)
