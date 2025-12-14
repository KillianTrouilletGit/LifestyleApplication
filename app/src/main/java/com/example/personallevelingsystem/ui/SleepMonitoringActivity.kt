package com.example.personallevelingsystem.ui

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModelProvider
import com.example.personallevelingsystem.MainActivity
import com.example.personallevelingsystem.R
import com.example.personallevelingsystem.data.AppDatabase
import com.example.personallevelingsystem.model.MissionType
import com.example.personallevelingsystem.model.Sleep
import com.example.personallevelingsystem.repository.UserRepository
import com.example.personallevelingsystem.viewmodel.MissionViewModel
import com.example.personallevelingsystem.viewmodel.MissionViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SleepMonitoringActivity : AppCompatActivity() {

    private lateinit var sleepTimeInput: EditText
    private lateinit var saveButton: Button
    private lateinit var backButton: Button
    private lateinit var db: AppDatabase
    private lateinit var viewModel: MissionViewModel
    private lateinit var userRepository: UserRepository


    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sleep_monitoring)
        supportActionBar?.title = ""
        db = AppDatabase.getDatabase(this)
        userRepository = UserRepository(db.userDao(),this)
        val factory = MissionViewModelFactory(application, userRepository) // Use the custom factory
        viewModel = ViewModelProvider(this, factory)[MissionViewModel::class.java]
        sleepTimeInput = findViewById(R.id.sleep_time_input)
        saveButton = findViewById(R.id.save_time_button)
        backButton = findViewById(R.id.back_button)

        sleepTimeInput.setOnClickListener {
            val calendar = java.util.Calendar.getInstance()
            val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
            val minute = calendar.get(java.util.Calendar.MINUTE)

            val timePickerDialog = android.app.TimePickerDialog(
                this,
                { _, selectedHour, selectedMinute ->
                    val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                    sleepTimeInput.setText(formattedTime)
                },
                hour,
                minute,
                true // 24 hour view
            )

            timePickerDialog.show()
            timePickerDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(R.color.buttonColor)
            timePickerDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(R.color.buttonColor)
        }

        saveButton.setOnClickListener {
            val sleepTime = sleepTimeInput.text.toString()
            if (sleepTime.isNotEmpty()) {
                saveSleepTime(sleepTime)
                if (convertTimeToMinutes(sleepTime)>=420){
                    completeMission()
                }
                Toast.makeText(this, "Sleep time saved: $sleepTime hours", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please enter your sleeping time", Toast.LENGTH_SHORT).show()
            }
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        backButton.setOnClickListener{
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun saveSleepTime(duration: String) {
        val sleep = Sleep(
            date = System.currentTimeMillis(),
            duration = duration
        )

        CoroutineScope(Dispatchers.IO).launch {
            db.SleepTimeDao().insert(sleep)
        }
    }
    private fun convertTimeToMinutes(time: String): Int {
        val parts = time.split(":")
        val hours = parts[0].toInt()
        val minutes = parts[1].toInt()
        return hours * 60 + minutes
    }

    private fun completeMission() {
        CoroutineScope(Dispatchers.IO).launch {
            val missionId = "daily_sleep" // Use the actual mission ID
            val missionType = MissionType.DAILY
            val userId = getCurrentUserId()

            if (userId != null) {
                withContext(Dispatchers.Main) {
                    viewModel.completeMissionById(missionId, missionType, userId)
                }
            }
        }
    }

    private suspend fun getCurrentUserId(): Int? {
        return withContext(Dispatchers.IO) {
            val latestUser = db.userDao().getLatestUser()
            latestUser?.id
        }
    }
}
