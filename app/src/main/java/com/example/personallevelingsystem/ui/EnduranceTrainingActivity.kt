package com.example.personallevelingsystem.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.personallevelingsystem.MainActivity
import com.example.personallevelingsystem.R
import com.example.personallevelingsystem.data.AppDatabase
import com.example.personallevelingsystem.model.EnduranceTraining
import com.example.personallevelingsystem.model.MissionType
import com.example.personallevelingsystem.repository.UserRepository
import com.example.personallevelingsystem.viewmodel.MissionViewModel
import com.example.personallevelingsystem.viewmodel.MissionViewModelFactory
import com.example.personallevelingsystem.viewmodel.UserViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class EnduranceTrainingActivity : AppCompatActivity() {

    private lateinit var tvTimer: TextView
    private lateinit var etDistance: EditText
    private lateinit var btnStart: Button
    private lateinit var btnPause: Button
    private lateinit var btnSave: Button
    private lateinit var viewModel: MissionViewModel
    private var startTime: Long = 0
    private var elapsedTime: Long = 0
    private var running: Boolean = false
    private var timerHandler = Handler(Looper.getMainLooper())
    private lateinit var db: AppDatabase
    private lateinit var userRepository: UserRepository

    private val timerRunnable = object : Runnable {
        override fun run() {
            val millis = System.currentTimeMillis() - startTime + elapsedTime
            val seconds = (millis / 1000).toInt()
            val minutes = seconds / 60
            val hours = minutes / 60

            tvTimer.text = String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60)
            timerHandler.postDelayed(this, 500)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_endurance_training)
        supportActionBar?.title = ""

        tvTimer = findViewById(R.id.tvTimer)
        etDistance = findViewById(R.id.etDistance)
        btnStart = findViewById(R.id.btnStart)
        btnPause = findViewById(R.id.btnPause)
        btnSave = findViewById(R.id.btnSave)

        // Initialize the database
        db = AppDatabase.getDatabase(this)
        userRepository = UserRepository(db.userDao(),this)
        val factory = MissionViewModelFactory(application, userRepository) // Use the custom factory
        viewModel = ViewModelProvider(this, factory).get(MissionViewModel::class.java)

        if (savedInstanceState != null) {
            startTime = savedInstanceState.getLong("startTime")
            elapsedTime = savedInstanceState.getLong("elapsedTime")
            running = savedInstanceState.getBoolean("running")

            if (running) {
                timerHandler.post(timerRunnable)
                btnStart.text = "Resume"
            } else {
                 val millis = elapsedTime
                val seconds = (millis / 1000).toInt()
                val minutes = seconds / 60
                val hours = minutes / 60
                tvTimer.text = String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60)
            }
        }

        btnStart.setOnClickListener {
            if (!running) {
                startTime = System.currentTimeMillis() - elapsedTime
                timerHandler.post(timerRunnable)
                running = true
                btnStart.text = "Resume"
            }
        }

        btnPause.setOnClickListener {

            if (running) {
                timerHandler.removeCallbacks(timerRunnable)
                elapsedTime = System.currentTimeMillis() - startTime
                running = false
            }
        }

        btnSave.setOnClickListener {
            if (!running) {
                val duration = elapsedTime
                val distance = etDistance.text.toString().toFloatOrNull() ?: 0f
                saveEnduranceTraining(duration, distance)
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong("startTime", startTime)
        if (running) {
             outState.putLong("elapsedTime", System.currentTimeMillis() - startTime)
        } else {
             outState.putLong("elapsedTime", elapsedTime)
        }
        outState.putBoolean("running", running)
    }

    private fun saveEnduranceTraining(duration: Long, distance: Float) {

        val enduranceTraining = EnduranceTraining(
            date = System.currentTimeMillis(),
            duration = duration,
            distance = distance
        )

        CoroutineScope(Dispatchers.IO).launch {
            db.enduranceTrainingDao().insert(enduranceTraining)
            checkAndCompleteWeeklyRunMission()
        }
    }
    private suspend fun getTotalDistanceForCurrentWeek(): Float {
        return withContext(Dispatchers.IO) {
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

            Log.d("MissionRepository", "Start of week: $startOfWeek, End of week: $endOfWeek")

            val totalDistance = db.enduranceTrainingDao().getTotalDistanceForWeek(startOfWeek, endOfWeek) ?: 0f
            Log.d("MissionRepository", "Total distance for current week: $totalDistance km")
            return@withContext totalDistance
        }
    }
    private suspend fun checkAndCompleteWeeklyRunMission() {
        val totalDistance = getTotalDistanceForCurrentWeek()
        CoroutineScope(Dispatchers.IO).launch {
            val userId = db.userDao().getLatestUser()?.id
            Log.d("MissionRepository", "Checking total distance: $totalDistance m")
            if (totalDistance > 10000) {
                if (userId != null) {
                    viewModel.completeMissionById("weekly_endurance", MissionType.WEEKLY, userId)
                }
                Log.d("MissionRepository", "Weekly endurance mission completed")
            } else {
                Log.d("MissionRepository", "Weekly endurance mission not completed")
            }
        }
    }
}
