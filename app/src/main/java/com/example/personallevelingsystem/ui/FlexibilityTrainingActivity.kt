package com.example.personallevelingsystem.ui

import android.content.Intent
import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity

import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.personallevelingsystem.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.personallevelingsystem.data.AppDatabase
import com.example.personallevelingsystem.model.FlexibilityTraining
import com.example.personallevelingsystem.R
import com.example.personallevelingsystem.model.MissionType
import com.example.personallevelingsystem.repository.UserRepository
import com.example.personallevelingsystem.viewmodel.MissionViewModel
import com.example.personallevelingsystem.viewmodel.MissionViewModelFactory
import kotlinx.coroutines.withContext

class FlexibilityTrainingActivity : AppCompatActivity() {

    private lateinit var tvTimer: TextView
    private lateinit var btnStart: Button
    private lateinit var btnPause: Button
    private lateinit var btnSave: Button
    private var startTime: Long = 0
    private var elapsedTime: Long = 0
    private var running: Boolean = false
    private var timerHandler = Handler(Looper.getMainLooper())
    private lateinit var db: AppDatabase
    private lateinit var viewModel: MissionViewModel
    private lateinit var userRepository: UserRepository

    private val timerRunnable = object : Runnable {
        override fun run() {
            val millis = System.currentTimeMillis() - startTime + elapsedTime
            val seconds = (millis / 1000).toInt()
            val minutes = seconds / 60
            val hours = minutes / 60
            tvTimer.text = String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60)
            if (minutes >= 15) {
                CoroutineScope(Dispatchers.Main).launch {
                    completeMission()
                }
            }
            timerHandler.postDelayed(this, 500)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flexibility_training)
        supportActionBar?.title = ""
        tvTimer = findViewById(R.id.tvTimer)
        btnStart = findViewById(R.id.btnStart)
        btnPause = findViewById(R.id.btnPause)
        btnSave = findViewById(R.id.btnSave)
        db = AppDatabase.getDatabase(applicationContext)
        userRepository = UserRepository(db.userDao(), this)
        val factory = MissionViewModelFactory(application, userRepository) // Use the custom factory
        viewModel = ViewModelProvider(this, factory)[MissionViewModel::class.java]

        if (savedInstanceState != null) {
            startTime = savedInstanceState.getLong("startTime")
            elapsedTime = savedInstanceState.getLong("elapsedTime")
            running = savedInstanceState.getBoolean("running")

            if (running) {
                timerHandler.post(timerRunnable)
                btnStart.text = "Resume"
            } else {
                // If paused, update text one last time
                val millis = elapsedTime
                val seconds = (millis / 1000).toInt()
                val minutes = seconds / 60
                val hours = minutes / 60
                tvTimer.text = String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60)
            }
        }

        btnStart.setOnClickListener {
            if (!running) {
                // If resuming from pause (where elapsedTime has value), adjust startTime
                // If fresh start, elapsedTime is 0
                startTime = System.currentTimeMillis() - elapsedTime
                timerHandler.post(timerRunnable)
                running = true
                btnStart.text = "Resume"
            }
        }

        btnPause.setOnClickListener {
            if (running) {
                timerHandler.removeCallbacks(timerRunnable)
                // elapsedTime is updated continuously in runnable formula, but for pause storage:
                elapsedTime = System.currentTimeMillis() - startTime
                running = false
            }
        }

        btnSave.setOnClickListener {
            if (!running) {
                val duration = elapsedTime // When paused, this is accurate
                saveFlexibilityTraining(duration)
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong("startTime", startTime)
        // If currently running, `elapsedTime` isn't fully updated in variable until pause.
        // But for calculation `System.currentTimeMillis() - startTime` works. 
        // We'll store current `elapsedTime` as the accumulation basis.
        // Actually best generic way:
        if (running) {
            // We need to shift 'startTime' so that when we restore, the calc holds.
            // On rotation, activity destroys. 
            // We can save current `elapsedTime` effectively.
             outState.putLong("elapsedTime", System.currentTimeMillis() - startTime)
        } else {
             outState.putLong("elapsedTime", elapsedTime)
        }
        outState.putBoolean("running", running)
    }

    private suspend fun completeMission() {
        val missionId = "daily_flex" // Utilisez l'ID de mission réel
        val missionType = MissionType.DAILY
        val userId = getCurrentUserId()
        if (userId != null) {
            withContext(Dispatchers.Main) {
                viewModel.completeMissionById(missionId, missionType, userId)
                // Afficher un toast
                Toast.makeText(this@FlexibilityTrainingActivity, "Mission Completed!", Toast.LENGTH_SHORT).show()
            }
        }
        // Le chronomètre continue de fonctionner sans interruption
    }


    private fun saveFlexibilityTraining(duration: Long) {
        val flexibilityTraining = FlexibilityTraining(
            date = System.currentTimeMillis(),
            duration = duration
        )

        CoroutineScope(Dispatchers.IO).launch {
            db.flexibilityTrainingDao().insert(flexibilityTraining)
        }
    }


    private suspend fun getCurrentUserId(): Int? {
        return withContext(Dispatchers.IO) {
            val latestUser = db.userDao().getLatestUser()
            latestUser?.id
        }
    }
}