package com.example.personallevelingsystem

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import androidx.work.*
import com.example.personallevelingsystem.adapter.CarouselAdapter
import com.example.personallevelingsystem.data.AppDatabase
import com.example.personallevelingsystem.repository.MissionRepository
import com.example.personallevelingsystem.scheduler.MissionScheduler
import com.example.personallevelingsystem.ui.*
import com.example.personallevelingsystem.util.NotificationUtils
import com.example.personallevelingsystem.worker.DailyNotificationWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: CarouselAdapter
    private lateinit var missionRepository: MissionRepository

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            adapter.notifyDataSetChanged()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        NotificationUtils.createNotificationChannel(this)
        supportActionBar?.hide() // Hide action bar for full dashboard look
        
        checkAndRequestNotificationPermission()

        val scheduler = MissionScheduler(this)
        scheduler.scheduleDailyMissionReset()
        scheduler.scheduleWeeklyMissionReset()

        // Initialize repository
        missionRepository = MissionRepository(this)

        // Setup Carousel
        setupCarousel()

        // Setup Dashboard Clicks
        setupDashboard()

        // Schedule Workers
        scheduleDailyNotificationWorker()
        
        // Datastore managers (Sync logic removed)
        // setupDatastoreManagers()
    }

    private fun setupCarousel() {
        viewPager = findViewById(R.id.viewPager)
        val db = AppDatabase.getDatabase(this)
        adapter = CarouselAdapter(
            db.WaterDao(),
            db.SleepTimeDao(),
            db.enduranceTrainingDao(),
            db.trainingSessionDao(),
            db.mealDao(),
            db.flexibilityTrainingDao()
        )
        viewPager.adapter = adapter
        viewPager.setCurrentItem(3 * 1000, false) // Infinite scroll effect
    }

    private fun setupDashboard() {
        findViewById<LinearLayout>(R.id.cardMissions).setOnClickListener {
            startActivity(Intent(this, MissionsListActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.cardTraining).setOnClickListener {
            startActivity(Intent(this, TrainingActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.cardNutrition).setOnClickListener {
            startActivity(Intent(this, MenuActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.cardSleep).setOnClickListener {
            startActivity(Intent(this, SleepMonitoringActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.cardWater).setOnClickListener {
            startActivity(Intent(this, WaterActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.cardPlanning).setOnClickListener {
            startActivity(Intent(this, GoogleCalendarActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.cardProfile).setOnClickListener {
            startActivity(Intent(this, UserProfileActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.cardModify).setOnClickListener {
            startActivity(Intent(this, ModifyUserInfoActivity::class.java))
        }
    }

    // Datastore managers removed for GitHub cleanup
    // setupDatastoreManagers()

    /*
    private fun setupDatastoreManagers() {
        val db = AppDatabase.getDatabase(this)
        val uploader = DatastoreUploader(this,
            db.userDao(), db.WaterDao(), db.SleepTimeDao(), db.flexibilityTrainingDao(),
            db.enduranceTrainingDao(), db.mealDao(), db.programDao(), db.sessionDao(),
            db.exerciseDao(), db.trainingSessionDao(), db.trainingSetDao()
        )
    }
    */

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("MainActivity", "Notification permission granted")
        } else {
            Log.d("MainActivity", "Notification permission denied")
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkAndRequestNotificationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.d("MainActivity", "Notification permission already granted")
            }
            shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun scheduleDailyNotificationWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(true)
            .setRequiresCharging(false)
            .setRequiresDeviceIdle(false)
            .build()

        val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyNotificationWorker>(1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DailyNotificationWorker",
            ExistingPeriodicWorkPolicy.UPDATE,
            dailyWorkRequest
        )
    }

    private fun calculateInitialDelay(): Long {
        val now = Calendar.getInstance()
        val nextRun = Calendar.getInstance()
        nextRun.set(Calendar.HOUR_OF_DAY, 0)
        nextRun.set(Calendar.MINUTE, 0)
        nextRun.set(Calendar.SECOND, 1)
        nextRun.set(Calendar.MILLISECOND, 0)

        if (now.after(nextRun)) {
            nextRun.add(Calendar.DAY_OF_MONTH, 1)
        }

        return nextRun.timeInMillis - now.timeInMillis
    }
}