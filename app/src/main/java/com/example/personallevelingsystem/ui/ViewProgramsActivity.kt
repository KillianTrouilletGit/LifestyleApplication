package com.example.personallevelingsystem.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import android.view.View
import android.content.Intent
import com.example.personallevelingsystem.R
import com.example.personallevelingsystem.data.AppDatabase
import com.example.personallevelingsystem.MainActivity
import com.example.personallevelingsystem.adapter.ProgramsAdapter
import com.example.personallevelingsystem.model.ProgramWithSessions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ViewProgramsActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var programsAdapter: ProgramsAdapter

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_programs)
        supportActionBar?.title = ""
        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "personal_levelingsystem.db").build()

        val programsRecyclerView: RecyclerView = findViewById(R.id.programs_recycler_view)
        programsRecyclerView.layoutManager = LinearLayoutManager(this)
        programsAdapter = ProgramsAdapter { program ->
            deleteProgram(program)
        }
        programsRecyclerView.adapter = programsAdapter

        val returnToMainButton: View = findViewById(R.id.button_return_to_main)
        returnToMainButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish() // Close this activity
        }
        val backButton: View = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            startActivity(Intent(this, TrainingActivity::class.java))
            finish() // Close this activity
        }

        loadPrograms()
    }

    private fun loadPrograms() {
        CoroutineScope(Dispatchers.IO).launch {
            val programs = db.programDao().getAllProgramsWithSessionsAndExercises()
            withContext(Dispatchers.Main) {
                programsAdapter.setPrograms(programs)
            }
        }
    }

    private fun deleteProgram(program: ProgramWithSessions) {
        CoroutineScope(Dispatchers.IO).launch {
            db.programDao().deleteProgram(program.program)
            loadPrograms() // Reload the programs after deletion
        }
    }
}
