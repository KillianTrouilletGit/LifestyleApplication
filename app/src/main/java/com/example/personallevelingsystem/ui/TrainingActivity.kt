package com.example.personallevelingsystem.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.personallevelingsystem.MainActivity
import com.example.personallevelingsystem.R


class TrainingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_training)
        supportActionBar?.title = ""
        findViewById<View>(R.id.button_create_program).setOnClickListener {
            // Launch EnterSessionCountActivity to get the number of sessions
            startActivity(Intent(this, EnterSessionCountActivity::class.java))
        }

        findViewById<View>(R.id.button_view_programs).setOnClickListener {
            // Launch ViewProgramsActivity to view existing programs
            startActivity(Intent(this, ViewProgramsActivity::class.java))
        }

        findViewById<View>(R.id.button_start_training).setOnClickListener {
            // Launch SelectSessionActivity to select a session for training
            startActivity(Intent(this, SelectSessionActivity::class.java))
        }
        findViewById<View>(R.id.button_start_endurance).setOnClickListener {
            // Launch SelectSessionActivity to select a session for training
            startActivity(Intent(this, EnduranceTrainingActivity::class.java))
        }
        findViewById<View>(R.id.button_start_flexibility).setOnClickListener {
            // Launch SelectSessionActivity to select a session for training
            startActivity(Intent(this, FlexibilityTrainingActivity::class.java))
        }
        findViewById<View>(R.id.button_main).setOnClickListener {
            // Launch SelectSessionActivity to select a session for training
            startActivity(Intent(this, MainActivity::class.java))
        }


    }
}
