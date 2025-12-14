package com.example.personallevelingsystem.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.personallevelingsystem.R
import com.example.personallevelingsystem.adapter.SessionsAdapter
import com.example.personallevelingsystem.data.AppDatabase
import com.example.personallevelingsystem.model.Session
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SelectSessionActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var sessionsAdapter: SessionsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_session)
        supportActionBar?.title = ""
        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "personal_levelingsystem.db").build()

        val sessionsRecyclerView: RecyclerView = findViewById(R.id.sessions_recycler_view)
        sessionsRecyclerView.layoutManager = LinearLayoutManager(this)
        sessionsAdapter = SessionsAdapter { session ->
            startTraining(session.id)
        }
        sessionsRecyclerView.adapter = sessionsAdapter

        loadSessions()
    }

    private fun loadSessions() {
        CoroutineScope(Dispatchers.IO).launch {
            val sessions = db.programDao().getAllSessions()
            withContext(Dispatchers.Main) {
                sessionsAdapter.setSessions(sessions)
            }
        }
    }

    private fun startTraining(sessionId: Long) {
        val intent = Intent(this, TrainingSessionActivity::class.java)
        intent.putExtra("SESSION_ID", sessionId)
        startActivity(intent)
        finish()
    }
}
