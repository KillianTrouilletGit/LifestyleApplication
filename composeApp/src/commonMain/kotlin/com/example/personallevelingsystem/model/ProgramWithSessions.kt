package com.example.personallevelingsystem.model

import androidx.room.Embedded
import androidx.room.Relation

data class ProgramWithSessions(
    @Embedded val program: Program,
    @Relation(
        entity = Session::class,
        parentColumn = "id",
        entityColumn = "programId"
    )
    val sessions: List<SessionWithExercises>
)

data class SessionWithExercises(
    @Embedded val session: Session,
    @Relation(
        entity = Exercise::class,
        parentColumn = "id",
        entityColumn = "sessionId"
    )
    val exercises: List<Exercise>
)
