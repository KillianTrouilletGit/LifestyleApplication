package com.example.personallevelingsystem.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.personallevelingsystem.R

class EnterSessionCountActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter_session_count)
        supportActionBar?.title = ""

        val sessionCountEditText: EditText = findViewById(R.id.session_count_edit_text)
        val nextButton: Button = findViewById(R.id.next_button)

        nextButton.setOnClickListener {
            val sessionCount = sessionCountEditText.text.toString().toIntOrNull()
            if (sessionCount != null && sessionCount > 0) {
                val intent = Intent(this, CreateSessionsActivity::class.java)
                intent.putExtra("SESSION_COUNT", sessionCount)
                startActivity(intent)
            } else {
                sessionCountEditText.error = "Please enter a valid number"
            }
        }
        val backButton: View = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            startActivity(Intent(this, TrainingActivity::class.java))
            finish() // Close this activity
        }
    }
}
