package com.example.personallevelingsystem.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Chronometer
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.personallevelingsystem.R
import com.example.personallevelingsystem.data.AppDatabase
import com.example.personallevelingsystem.model.Exercise
import com.example.personallevelingsystem.model.MissionType
import com.example.personallevelingsystem.model.TrainingSession
import com.example.personallevelingsystem.model.TrainingSet
import com.example.personallevelingsystem.repository.UserRepository
import com.example.personallevelingsystem.viewmodel.MissionViewModel
import com.example.personallevelingsystem.viewmodel.MissionViewModelFactory
import com.example.personallevelingsystem.viewmodel.UserViewModel
import com.example.personallevelingsystem.viewmodel.UserViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date

class TrainingSessionActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var exercises: List<Exercise>
    private var currentExerciseIndex: Int = 0
    private lateinit var chronometer: Chronometer
    private lateinit var exerciseContainer: LinearLayout
    private var trainingSessionId: Long = 0
    private lateinit var viewModel: MissionViewModel
    private lateinit var userRepository: UserRepository
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_training_session)
        supportActionBar?.title = ""
        db = AppDatabase.getDatabase(this)
        userRepository = UserRepository(db.userDao(),this)
        val factory = MissionViewModelFactory(application, userRepository) // Use the custom factory
        viewModel = ViewModelProvider(this, factory).get(MissionViewModel::class.java)
        val userFactory = UserViewModelFactory(userRepository) // Assuming you have a UserViewModelFactory
        userViewModel = ViewModelProvider(this, userFactory).get(UserViewModel::class.java)
        chronometer = findViewById(R.id.chronometer)
        exerciseContainer = findViewById(R.id.exercise_container)

        val sessionId = intent.getLongExtra("SESSION_ID", -1)
        val sessionName = intent.getStringExtra("SESSION_NAME") ?: "Default Session Name"

        if (savedInstanceState != null) {
            // Restore state
            currentExerciseIndex = savedInstanceState.getInt("KEY_EXERCISE_INDEX", 0)
            trainingSessionId = savedInstanceState.getLong("KEY_SESSION_ID", 0)
            val chronometerBase = savedInstanceState.getLong("KEY_CHRONOMETER_BASE", 0)
            chronometer.base = chronometerBase
            startChronometer()

            // Reload exercises and show current one
            if (sessionId != -1L) {
                loadExercises(sessionId)
            }
        } else {
            // New Session
            if (sessionId != -1L) {
                startTrainingSession(sessionId, sessionName)
            }
            // Start chronometer fresh (set base to now)
            chronometer.base = android.os.SystemClock.elapsedRealtime()
            startChronometer()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("KEY_EXERCISE_INDEX", currentExerciseIndex)
        outState.putLong("KEY_SESSION_ID", trainingSessionId)
        outState.putLong("KEY_CHRONOMETER_BASE", chronometer.base)
    }

    private fun startTrainingSession(sessionId: Long, sessionName: String) {
        val startTime = Date()
        val date = System.currentTimeMillis()
        CoroutineScope(Dispatchers.IO).launch {
            // Ensure sessionId is valid
            val sessionExists = db.programDao().getSessionById(sessionId) != null
            if (sessionExists) {
                val trainingSession = TrainingSession(sessionId = sessionId, name = sessionName, startTime = startTime, endTime = null, weekId = 0L, date = date)
                trainingSessionId = db.trainingSessionDao().insertTrainingSession(trainingSession)
                loadExercises(sessionId)
            } else {
                Log.e("TrainingSessionActivity", "Session ID $sessionId does not exist")
            }
        }
    }


    private fun loadExercises(sessionId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            exercises = db.programDao().getExercisesBySessionId(sessionId)
            withContext(Dispatchers.Main) {
                if (exercises.isNotEmpty()) {
                    showExercise()
                } else {
                    Log.e("TrainingSessionActivity", "No exercises found for Session ID $sessionId")
                    // Handle empty exercises, possibly notify the user
                }
            }
        }
    }


    @SuppressLint("SetTextI1n")
    private fun showExercise() {
        if (currentExerciseIndex < exercises.size) {
            val exercise = exercises[currentExerciseIndex]
            exerciseContainer.removeAllViews()

            val exerciseNameTextView = TextView(this)
            exerciseNameTextView.text = exercise.name
            exerciseContainer.addView(exerciseNameTextView)

            CoroutineScope(Dispatchers.IO).launch {
                val previousTrainingSets = db.trainingSessionDao().getPreviousTrainingSets(exercise.id, trainingSessionId,exercise.sets)
                val userId = db.userDao().getLatestUser()?.id
                withContext(Dispatchers.Main) {
                    val currentSets = mutableListOf<TrainingSet>()
                    
                    for (i in 0 until exercise.sets) {
                        // Inflate the new item layout
                        val itemView = layoutInflater.inflate(R.layout.item_training_set, exerciseContainer, false)
                        
                        val tvSetLabel = itemView.findViewById<TextView>(R.id.tvSetLabel)
                        val tvPreviousStats = itemView.findViewById<TextView>(R.id.tvPreviousStats)
                        val etReps = itemView.findViewById<EditText>(R.id.etReps)
                        val etWeight = itemView.findViewById<EditText>(R.id.etWeight)

                        tvSetLabel.text = "SET ${i + 1}"

                        val trainingSet = TrainingSet(
                            trainingSessionId = trainingSessionId,
                            exerciseId = exercise.id,
                            reps = 0,
                            weight = 0f
                        )

                        // Display previous training sets
                        if (i < previousTrainingSets.size) {
                            val previousSet = previousTrainingSets[i]
                            
                            tvPreviousStats.visibility = View.VISIBLE
                            tvPreviousStats.text = "PREV: ${previousSet.reps}x${previousSet.weight}kg"
                            
                            // Pre-fill with previous values
                             etReps.setText(previousSet.reps.toString())
                             etWeight.setText(previousSet.weight.toString())
                             
                             // Update the trainingSet object immediately with pre-filled values
                             trainingSet.reps = previousSet.reps
                             trainingSet.weight = previousSet.weight
                        } else {
                            tvPreviousStats.visibility = View.GONE
                        }

                        exerciseContainer.addView(itemView)

                        etReps.addTextChangedListener {
                            val reps = it.toString().toIntOrNull() ?: 0
                            trainingSet.reps = reps
                        }

                        etWeight.addTextChangedListener {
                            val weight = it.toString().toFloatOrNull() ?: 0f
                            trainingSet.weight = weight
                        }
                        currentSets.add(trainingSet)
                    }

                    // Style the Next button using Protocol style programmatically or inflate a button layout
                    // Ideally we should have a button in the layout or add it here styled.
                    // For now, simple Button but let's try to set background if possible, or just standard.
                    val nextButton = Button(this@TrainingSessionActivity)
                    nextButton.text = "NEXT EXERCISE"
                    // Apply style programmatically is hard, usually we just set background tint
                    nextButton.setBackgroundColor(getColor(R.color.buttonColor))
                    nextButton.setTextColor(getColor(R.color.buttonTextColor))
                    
                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.topMargin = 32
                    nextButton.layoutParams = params

                    nextButton.setOnClickListener {
                        saveCurrentSets(currentSets, previousTrainingSets, userId)
                        currentExerciseIndex++
                        showExercise()
                    }
                    exerciseContainer.addView(nextButton)
                }
            }
        } else {
            endTrainingSession()
        }
    }

    private fun saveCurrentSets(currentSets: List<TrainingSet>, previousSets: List<TrainingSet>, userId: Int?) {
        CoroutineScope(Dispatchers.IO).launch {
           // Save to DB
           currentSets.forEach { set ->
               db.trainingSessionDao().insertTrainingSet(set)
           }
           // Calculate XP
           if (userId != null) {
               calculateAndAddXp(currentSets, previousSets, userId)
           }
        }
    }


    private fun endTrainingSession() {
        chronometer.stop()
        val endTime = Date()

        CoroutineScope(Dispatchers.IO).launch {
            val userId = db.userDao().getLatestUser()?.id
            val trainingSession = db.trainingSessionDao().getTrainingSessionById(trainingSessionId).copy(endTime = endTime)
            db.trainingSessionDao().updateTrainingSession(trainingSession)

            if (userId != null) {
                checkAndCompleteProgramMission(trainingSession.sessionId, userId)
            }
            withContext(Dispatchers.Main) {
                finish() // End of session
            }
        }
    }



    private fun startChronometer() {
        chronometer.start()
    }

    companion object {
        private const val TAG = "TrainingSessionActivity"
    }
    private suspend fun checkAndCompleteProgramMission(sessionId: Long, userId : Int) {
        val programId = db.programDao().getProgramIdBySessionId(sessionId)

        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfWeek = calendar.timeInMillis

        calendar.add(Calendar.WEEK_OF_YEAR, 1)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfWeek = calendar.timeInMillis

        val programWithSessions = db.programDao().getAllProgramsWithSessionsAndExercises().find { it.program.id == programId }
        val completedSessions = db.trainingSessionDao().getCompletedSessionsForProgramInWeek(programId, startOfWeek, endOfWeek)

        if (programWithSessions != null && completedSessions.size == programWithSessions.sessions.size) {
            viewModel.completeMissionById("weekly_workout", MissionType.WEEKLY, userId)
        }
    }
    private fun calculateAndAddXp(currentSets: List<TrainingSet>, previousTrainingSets: List<TrainingSet>, userId: Int) {
        var totalXp = 0
        for (i in currentSets.indices) {
            val currentSet = currentSets[i]
            val previousSet = if (i < previousTrainingSets.size) previousTrainingSets[i] else null

            if (previousSet != null) {
                totalXp += calculateXpForSetChange(
                    currentReps = currentSet.reps,
                    currentWeight = currentSet.weight,
                    previousReps = previousSet.reps,
                    previousWeight = previousSet.weight
                )
            }
        }
        userViewModel.addXpToUser(userId, totalXp)
    }
    private fun calculateXpForSetChange(currentReps: Int, currentWeight: Float, previousReps: Int, previousWeight: Float): Int {
        var xp = 0
        if (currentReps > previousReps) {
            xp += (currentReps - previousReps) * 5 // 10 XP per additional rep
        }
        if (currentWeight > previousWeight) {
            xp += ((currentWeight - previousWeight) * 5).toInt() // 10 XP per additional kg
        }
        return xp
    }

}
