package com.example.personallevelingsystem.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.personallevelingsystem.model.Program
import com.example.personallevelingsystem.model.ProgramWithSessions
import com.example.personallevelingsystem.model.Session
import com.example.personallevelingsystem.model.Exercise

@Dao
interface ProgramDao {

    @Insert
    fun insertProgram(program: Program): Long
    @Query("DELETE FROM programs")
    fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(users: List<Program>)

    @Insert
    fun insertSession(session: Session): Long

    @Insert
    fun insertExercise(exercise: Exercise)

    @Query("SELECT * FROM programs")
    @Transaction
    fun getAllProgramsWithSessionsAndExercises(): List<ProgramWithSessions>

    @Query("SELECT * FROM programs")
    suspend fun getAll(): List<Program>

    @Query("SELECT * FROM sessions")
    fun getAllSessions(): List<Session>

    @Query("SELECT * FROM exercises WHERE sessionId = :sessionId")
    fun getExercisesBySessionId(sessionId: Long): List<Exercise>

    @Query("SELECT programId FROM sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getProgramIdBySessionId(sessionId: Long): Long

    @Delete
    fun deleteProgram(program: Program)

    @Transaction
    fun insertProgramWithSessionsAndExercises(program: Program, sessions: List<Session>, exercises: List<List<Exercise>>) {
        val programId = insertProgram(program)
        sessions.forEachIndexed { index, session ->
            val sessionId = insertSession(session.copy(programId = programId))
            exercises[index].forEach { exercise ->
                insertExercise(exercise.copy(sessionId = sessionId))
            }
        }
    }

    @Query("SELECT * FROM sessions WHERE id = :sessionId LIMIT 1")
    fun getSessionById(sessionId: Long): Session?

    @Query("SELECT * FROM exercises WHERE id = :exerciseId LIMIT 1")
    fun getExerciseById(exerciseId: Long): Exercise?
}
