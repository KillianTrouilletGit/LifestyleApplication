package com.example.personallevelingsystem.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.personallevelingsystem.data.AppDatabase
import com.example.personallevelingsystem.model.Program
import com.example.personallevelingsystem.model.ProgramWithSessions
import com.example.personallevelingsystem.model.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TrainingViewModel(application: Application) : AndroidViewModel(application) {

    private val programDao = AppDatabase.getDatabase(application).programDao()
    private val trainingSessionDao = AppDatabase.getDatabase(application).trainingSessionDao()
    private val exerciseDao = AppDatabase.getDatabase(application).exerciseDao()

    private val _programs = MutableLiveData<List<ProgramWithSessions>>()
    val programs: LiveData<List<ProgramWithSessions>> = _programs

    private val _sessions = MutableLiveData<List<Session>>()
    val sessions: LiveData<List<Session>> = _sessions

    // Active Training Session State
    private var currentSessionId: Long = 0
    private val _currentExercises = MutableLiveData<List<com.example.personallevelingsystem.model.Exercise>>()
    val currentExercises: LiveData<List<com.example.personallevelingsystem.model.Exercise>> = _currentExercises

    private val _currentExerciseIndex = MutableLiveData(0)
    val currentExerciseIndex: LiveData<Int> = _currentExerciseIndex

    // Holds sets for the *current* exercise, including history
    private val _currentSets = MutableLiveData<List<TrainingSetState>>()
    val currentSets: LiveData<List<TrainingSetState>> = _currentSets
    
    // Notify UI when session is finished
    private val _sessionFinished = MutableLiveData<Boolean>(false)
    val sessionFinished: LiveData<Boolean> = _sessionFinished
    
    // UI State for the Screen
    data class TrainingSetState(
        var reps: String = "",
        var weight: String = "",
        val previousReps: Int = 0,
        val previousWeight: Float = 0f,
        val exerciseId: Long = 0,
        val setNumber: Int = 0
    )

    fun loadPrograms() {
        viewModelScope.launch(Dispatchers.IO) {
            _programs.postValue(programDao.getAllProgramsWithSessionsAndExercises())
        }
    }

    fun loadSessions() {
        viewModelScope.launch(Dispatchers.IO) {
            _sessions.postValue(programDao.getAllSessions())
        }
    }

    private val flexibilityDao = AppDatabase.getDatabase(application).flexibilityTrainingDao()
    private val enduranceDao = AppDatabase.getDatabase(application).enduranceTrainingDao()
    private val userDao = AppDatabase.getDatabase(application).userDao()

    fun deleteProgram(program: Program) {
        viewModelScope.launch(Dispatchers.IO) {
            programDao.deleteProgram(program)
            loadPrograms()
        }
    }

    fun saveFlexibilityTraining(duration: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val training = com.example.personallevelingsystem.model.FlexibilityTraining(
                date = System.currentTimeMillis(),
                duration = duration
            )
            flexibilityDao.insert(training)
        }
    }

    fun saveEnduranceTraining(duration: Long, distance: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            val training = com.example.personallevelingsystem.model.EnduranceTraining(
                date = System.currentTimeMillis(),
                duration = duration,
                distance = distance
            )
            enduranceDao.insert(training)
        }
    }

    data class NewExercise(var name: String = "", var sets: String = "")
    data class NewSession(var name: String = "", var exercises: MutableList<NewExercise> = mutableListOf())

    fun saveProgram(name: String, newSessions: List<NewSession>) {
        viewModelScope.launch(Dispatchers.IO) {
            val programId = programDao.insertProgram(Program(name = name))
            
            newSessions.forEach { newSession ->
                val sessionId = programDao.insertSession(Session(programId = programId, name = newSession.name))
                newSession.exercises.forEach { newExercise ->
                     val sets = newExercise.sets.toIntOrNull() ?: 1
                     programDao.insertExercise(com.example.personallevelingsystem.model.Exercise(
                         sessionId = sessionId,
                         name = newExercise.name,
                         sets = sets
                     ))
                }
            }
            loadPrograms() // Refresh list
        }
    }

    // --- Active Session Logic ---

    fun startSession(sessionId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            // 0. Create new TrainingSession record
            val startTime = java.util.Date()
            val trainingSession = com.example.personallevelingsystem.model.TrainingSession(
                sessionId = sessionId, // references the blueprint session ID
                name = "Session", // Ideally fetch name
                startTime = startTime,
                endTime = null,
                weekId = 0L,
                date = System.currentTimeMillis()
            )
            currentSessionId = trainingSessionDao.insertTrainingSession(trainingSession)

            // 1. Fetch Exercises
            val exercises = programDao.getExercisesBySessionId(sessionId)
            _currentExercises.postValue(exercises)
            _currentExerciseIndex.postValue(0)
            
            // 2. Load first exercise sets
            if (exercises.isNotEmpty()) {
                val firstExercise = exercises[0]
                loadSetsForExercise(firstExercise)
                
                // 3. Start Timer Service
                startTimerService(firstExercise.name)
            }
        }
    }

    private var sessionStartTime: Long = 0

    // Track if timer service *should* be running
    var isTimerRunning: Boolean = false
    private var lastExerciseName: String = "Training"

    fun startTimerService(exerciseName: String) {
        val app = getApplication<Application>()
        // Use elapsedRealtime for Chronometer compatibility
        sessionStartTime = android.os.SystemClock.elapsedRealtime()
        lastExerciseName = exerciseName
        isTimerRunning = true
        
        val intent = Intent(app, com.example.personallevelingsystem.service.TrainingTimerService::class.java).apply {
            action = com.example.personallevelingsystem.service.TrainingTimerService.ACTION_START
            putExtra(com.example.personallevelingsystem.service.TrainingTimerService.EXTRA_EXERCISE_NAME, exerciseName)
            putExtra(com.example.personallevelingsystem.service.TrainingTimerService.EXTRA_START_TIME, sessionStartTime)
        }
        androidx.core.content.ContextCompat.startForegroundService(app, intent)
    }

    private fun updateTimerService(exerciseName: String) {
        val app = getApplication<Application>()
        lastExerciseName = exerciseName
        // isTimerRunning remains true
        
        val intent = Intent(app, com.example.personallevelingsystem.service.TrainingTimerService::class.java).apply {
            action = com.example.personallevelingsystem.service.TrainingTimerService.ACTION_UPDATE
            putExtra(com.example.personallevelingsystem.service.TrainingTimerService.EXTRA_EXERCISE_NAME, exerciseName)
            putExtra(com.example.personallevelingsystem.service.TrainingTimerService.EXTRA_START_TIME, sessionStartTime)
        }
        androidx.core.content.ContextCompat.startForegroundService(app, intent)
    }

    fun stopTimerService() {
        isTimerRunning = false
        val app = getApplication<Application>()
        val intent = Intent(app, com.example.personallevelingsystem.service.TrainingTimerService::class.java).apply {
            action = com.example.personallevelingsystem.service.TrainingTimerService.ACTION_STOP
        }
        app.startService(intent) // stop command doesn't need foreground promotion
    }

    /**
     * Called by Activity lifecycle (onStop) to ensure notification is visible
     * if the timer is supposed to be running.
     */
    fun ensureTimerNotification() {
        if (isTimerRunning) {
            updateTimerService(lastExerciseName)
        }
    }

    private fun loadSetsForExercise(exercise: com.example.personallevelingsystem.model.Exercise) {
         viewModelScope.launch(Dispatchers.IO) {
             // Fetch previous sets using the REAL currentSessionId
             val previousSets = trainingSessionDao.getPreviousTrainingSets(exercise.id, currentSessionId, exercise.sets)
             
             val newSets = mutableListOf<TrainingSetState>()
             for (i in 0 until exercise.sets) {
                 val prev = previousSets.getOrNull(i)
                 newSets.add(TrainingSetState(
                     reps = prev?.reps?.toString() ?: "",
                     weight = prev?.weight?.toString() ?: "",
                     previousReps = prev?.reps ?: 0,
                     previousWeight = prev?.weight ?: 0f,
                     exerciseId = exercise.id,
                     setNumber = i
                 ))
             }
             _currentSets.postValue(newSets)
         }
    }

    fun saveCurrentSetState(sets: List<TrainingSetState>) {
        // In a real app, save to DB here. For now just updating livedata if needed
        _currentSets.value = sets
    }
    
    fun nextExercise() {
        val exercises = _currentExercises.value ?: return
        val setsToSave = _currentSets.value ?: return
        val currentIndex = _currentExerciseIndex.value ?: 0
        
        viewModelScope.launch(Dispatchers.IO) {
            // Save current sets before moving
            setsToSave.forEach { set ->
                val trainingSet = com.example.personallevelingsystem.model.TrainingSet(
                    trainingSessionId = currentSessionId,
                    exerciseId = set.exerciseId,
                    reps = set.reps.toIntOrNull() ?: 0,
                    weight = set.weight.toFloatOrNull() ?: 0f
                )
                trainingSessionDao.insertTrainingSet(trainingSet)
            }

            if (currentIndex < exercises.size - 1) {
                val nextIndex = currentIndex + 1
                _currentExerciseIndex.postValue(nextIndex)
                val nextExercise = exercises[nextIndex]
                loadSetsForExercise(nextExercise)
                
                // Update Timer Notification
                updateTimerService(nextExercise.name)
            } else {
                // Session Complete
                stopTimerService()
                val session = trainingSessionDao.getTrainingSessionById(currentSessionId)
                val updatedSession = session.copy(endTime = java.util.Date())
                trainingSessionDao.updateTrainingSession(updatedSession)
                _sessionFinished.postValue(true)
            }
        }
    }
}
