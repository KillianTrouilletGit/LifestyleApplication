package com.example.personallevelingsystem.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.personallevelingsystem.MainActivity
import com.example.personallevelingsystem.R
import com.example.personallevelingsystem.adapter.MissionAdapter
import com.example.personallevelingsystem.data.AppDatabase
import com.example.personallevelingsystem.repository.UserRepository
import com.example.personallevelingsystem.viewmodel.MissionViewModel
import com.example.personallevelingsystem.viewmodel.MissionViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MissionsListActivity : AppCompatActivity() {

    private lateinit var viewModel: MissionViewModel
    private lateinit var adapter: MissionAdapter
    private lateinit var userRepository: UserRepository
    private lateinit var db: AppDatabase

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_missions_list)
        supportActionBar?.title = ""

        // Initialize the database
        db = AppDatabase.getDatabase(this)
        userRepository = UserRepository(db.userDao(), this)
        val factory = MissionViewModelFactory(application, userRepository)

        viewModel = ViewModelProvider(this, factory)[MissionViewModel::class.java]

        CoroutineScope(Dispatchers.Main).launch {
            val userId = getCurrentUserId()
            if (userId != null) {
                setupRecyclerView(userId)
                observeMissions()

            }
        }
        val backButton: View = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish() // Close this activity
        }
    }

    private fun setupRecyclerView(userId: Int) {
        adapter = MissionAdapter(this, emptyList(), userId,viewModel)


        val recyclerView = findViewById<RecyclerView>(R.id.missionRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this@MissionsListActivity)
        recyclerView.adapter = adapter
    }

    private fun observeMissions() {
        viewModel.dailyMissions.observe(this, Observer { dailyMissions ->
            viewModel.weeklyMissions.observe(this, Observer { weeklyMissions ->
                adapter.updateMissions(dailyMissions, weeklyMissions)
            })
        })
    }

    private suspend fun getCurrentUserId(): Int? {
        return withContext(Dispatchers.IO) {
            val latestUser = db.userDao().getLatestUser()
            latestUser?.id
        }
    }

}
