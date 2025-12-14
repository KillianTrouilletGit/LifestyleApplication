package com.example.personallevelingsystem.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.personallevelingsystem.MainActivity
import com.example.personallevelingsystem.R
import com.example.personallevelingsystem.data.AppDatabase
import com.example.personallevelingsystem.model.MissionType
import com.example.personallevelingsystem.model.Sleep
import com.example.personallevelingsystem.model.User
import com.example.personallevelingsystem.model.Water
import com.example.personallevelingsystem.repository.UserRepository
import com.example.personallevelingsystem.viewmodel.MissionViewModel
import com.example.personallevelingsystem.viewmodel.MissionViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class WaterActivity : AppCompatActivity() {

    private lateinit var waterAmountInput: EditText
    private lateinit var saveButton: Button
    private lateinit var backButton: Button
    private lateinit var db: AppDatabase
    private lateinit var dailyRequirementTextView: TextView
    private lateinit var totalWaterTextView: TextView
    private lateinit var comparisonResultTextView: TextView
    private lateinit var viewModel: MissionViewModel
    private lateinit var userRepository: UserRepository


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_water)
        supportActionBar?.title = ""
        db = AppDatabase.getDatabase(this)
        userRepository = UserRepository(db.userDao(),this)
        val factory = MissionViewModelFactory(application, userRepository) // Use the custom factory
        viewModel = ViewModelProvider(this, factory).get(MissionViewModel::class.java)
        waterAmountInput = findViewById(R.id.water_amount_input)
        saveButton = findViewById(R.id.save_amount_button)
        backButton = findViewById(R.id.back_button)
        dailyRequirementTextView = findViewById(R.id.daily_requirement_text_view)
        totalWaterTextView = findViewById(R.id.total_water_text_view)
        comparisonResultTextView = findViewById(R.id.comparison_result_text_view)
        backButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        saveButton.setOnClickListener {

            val waterAmount = waterAmountInput.text.toString()

            if (waterAmount.isNotEmpty()) {
                saveWaterAmount(waterAmount)
                Toast.makeText(this, "Water amount saved: $waterAmount Liters", Toast.LENGTH_SHORT).show()
                compareWaterIntake()
            } else {
                Toast.makeText(this, "Please enter the total water amount", Toast.LENGTH_SHORT).show()
            }
            compareWaterIntake()

        }

        
        // Initial data load
        compareWaterIntake()
    }


    fun calculateDailyWaterRequirement(): Float {
        val latestUser = db.userDao().getLatestUser()
        return latestUser?.let {
            calculateWaterRequirement(it.weight, 20) // Assuming 30 minutes of exercise
        } ?: 0f
    }

    private fun calculateWaterRequirement(weight:Float, minutesOfExercise: Int=30): Float {
        val basicWaterRequirement = weight * 0.035f
        val activityWaterRequirement = (minutesOfExercise / 30) * 0.35f
        return basicWaterRequirement + activityWaterRequirement
    }

    private fun saveWaterAmount(amount: String) {
        val water = Water(
            date = System.currentTimeMillis(),
            amount = amount.toFloat()
        )

        CoroutineScope(Dispatchers.IO).launch {
            db.WaterDao().insert(water)
        }
    }

    private suspend fun calculateTotalWaterForToday(): Float {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfDay = calendar.timeInMillis

        val waterEntries = db.WaterDao().getWaterForDay(startOfDay, endOfDay)
        return waterEntries.sumOf { it.amount.toDouble() }.toFloat()
    }

    @SuppressLint("SetTextI18n")
    private fun compareWaterIntake() {
        CoroutineScope(Dispatchers.IO).launch {
            val dailyRequirement = calculateDailyWaterRequirement()

            val totalWaterConsumed = calculateTotalWaterForToday()

            val hasMetRequirement = totalWaterConsumed >= dailyRequirement

            withContext(Dispatchers.Main) {
                dailyRequirementTextView.text = "Daily Water Requirement: $dailyRequirement Liters"
                totalWaterTextView.text = "Total Water Consumed: $totalWaterConsumed Liters"
                comparisonResultTextView.text = if (hasMetRequirement) {
                    "You have met your daily water intake!"
                } else {
                    "You need to drink more water."
                }

                // Use the boolean value for any further logic
                if (hasMetRequirement) {
                    completeMission()
                }
            }
        }
    }
    private fun completeMission() {
        CoroutineScope(Dispatchers.IO).launch {
            val missionId = "daily_water" // Use the actual mission ID
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
