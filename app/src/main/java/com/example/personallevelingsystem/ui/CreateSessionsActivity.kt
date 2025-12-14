package com.example.personallevelingsystem.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.example.personallevelingsystem.R
import com.example.personallevelingsystem.data.AppDatabase
import com.example.personallevelingsystem.model.Program
import com.example.personallevelingsystem.model.Session
import com.example.personallevelingsystem.model.Exercise
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CreateSessionsActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_sessions)
        supportActionBar?.title = ""
        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "personal_levelingsystem.db").build()

        val sessionCount = intent.getIntExtra("SESSION_COUNT", 0)
        val sessionsContainer: LinearLayout = findViewById(R.id.sessions_container)
        val programNameEditText: EditText = findViewById(R.id.program_name_edit_text)

        val sessionViews = mutableListOf<View>()

        for (i in 1..sessionCount) {
            val sessionView = LayoutInflater.from(this).inflate(R.layout.session_item, sessionsContainer, false)
            val sessionNameEditText = sessionView.findViewById<EditText>(R.id.session_name_edit_text)
            sessionNameEditText.hint = "Session $i Name"

            val exercisesContainer: LinearLayout = sessionView.findViewById(R.id.exercises_container)
            val addExerciseButton: Button = sessionView.findViewById(R.id.add_exercise_button)

            addExerciseButton.setOnClickListener {
                val exerciseView = LayoutInflater.from(this).inflate(R.layout.exercise_item, exercisesContainer, false)
                val removeExerciseButton: Button = exerciseView.findViewById(R.id.remove_exercise_button)

                removeExerciseButton.setOnClickListener {
                    exercisesContainer.removeView(exerciseView)
                }

                exercisesContainer.addView(exerciseView)
            }

            sessionsContainer.addView(sessionView)
            sessionViews.add(sessionView)
        }
        val backButton: View = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            startActivity(Intent(this, EnterSessionCountActivity::class.java))
            finish() // Close this activity
        }
        val saveButton: Button = findViewById(R.id.save_button)
        saveButton.setOnClickListener {
            val programName = programNameEditText.text.toString()
            if (programName.isBlank()) {
                programNameEditText.error = "Program name is required"
                return@setOnClickListener
            }

            var sessions = mutableListOf<Session>()
            val exercisesList = mutableListOf<Pair<Session, List<Exercise>>>()

            for (sessionView in sessionViews) {
                val sessionNameEditText = sessionView.findViewById<EditText>(R.id.session_name_edit_text)
                val sessionName = sessionNameEditText.text.toString()
                val exercisesContainer: LinearLayout = sessionView.findViewById(R.id.exercises_container)

                val exercises = mutableListOf<Exercise>()
                for (i in 0 until exercisesContainer.childCount) {
                    val exerciseView = exercisesContainer.getChildAt(i)
                    val exerciseNameEditText = exerciseView.findViewById<EditText>(R.id.exercise_name_edit_text)
                    val exerciseSetsEditText = exerciseView.findViewById<EditText>(R.id.exercise_sets_edit_text)
                    val exerciseName = exerciseNameEditText.text.toString()
                    val exerciseSets = exerciseSetsEditText.text.toString().toIntOrNull() ?: 0
                    exercises.add(Exercise(sessionId = 0, name = exerciseName, sets = exerciseSets)) // sessionId will be set later
                }

                val session = Session(programId = 0, name = sessionName) // programId will be set later
                sessions.add(session)
                exercisesList.add(Pair(session, exercises))
            }

            val program = Program(name = programName)

            CoroutineScope(Dispatchers.IO).launch {
                val programId = db.programDao().insertProgram(program)
                sessions = sessions.map { it.copy(programId = programId) }.toMutableList()

                exercisesList.forEach { (session, exercises) ->
                    val sessionId = db.programDao().insertSession(session.copy(programId = programId))
                    exercises.forEach { exercise ->
                        db.programDao().insertExercise(exercise.copy(sessionId = sessionId))
                    }
                }

                val intent = Intent(this@CreateSessionsActivity, ViewProgramsActivity::class.java)
                startActivity(intent)
                finish() // Close this activity
            }
        }
    }
}
