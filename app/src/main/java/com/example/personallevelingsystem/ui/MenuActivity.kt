package com.example.personallevelingsystem.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.personallevelingsystem.R

class MenuActivity : AppCompatActivity() {

    private lateinit var apiButton: Button
    private lateinit var manualButton: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        supportActionBar?.title = ""
        // Récupérer les boutons du layout
        apiButton = findViewById(R.id.api_button)
        manualButton = findViewById(R.id.manual_button)

        // Définir les actions des boutons
        apiButton.setOnClickListener {
            // Lancer l'activité NutritionActivity pour entrer les données via l'API
            startActivity(Intent(this, NutritionActivity::class.java))
        }

        manualButton.setOnClickListener {
            // Lancer une nouvelle activité pour entrer les données manuellement
            startActivity(Intent(this, ManualEntryActivity::class.java))
        }
    }
}
