package com.example.personallevelingsystem.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.personallevelingsystem.R
import com.example.personallevelingsystem.data.AppDatabase
import com.example.personallevelingsystem.model.Meal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ManualEntryActivity : AppCompatActivity() {

    private lateinit var caloriesEditText: EditText
    private lateinit var proteinEditText: EditText
    private lateinit var fatEditText: EditText
    private lateinit var carbsEditText: EditText
    private lateinit var fiberEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual_entry)
        supportActionBar?.title = ""
        // Récupérer les champs du layout
        caloriesEditText = findViewById(R.id.calories_edit_text)
        proteinEditText = findViewById(R.id.protein_edit_text)
        fatEditText = findViewById(R.id.fat_edit_text)
        carbsEditText = findViewById(R.id.carbs_edit_text)
        fiberEditText = findViewById(R.id.fiber_edit_text)
        saveButton = findViewById(R.id.save_button)

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "personal_levelingsystem.db"
        ).build()
        // Définir l'action du bouton sauvegarder
        saveButton.setOnClickListener {
            val calories = caloriesEditText.text.toString().toDoubleOrNull()
            val protein = proteinEditText.text.toString().toDoubleOrNull()
            val fat = fatEditText.text.toString().toDoubleOrNull()
            val carbs = carbsEditText.text.toString().toDoubleOrNull()
            val fiber = fiberEditText.text.toString().toDoubleOrNull()

            if (calories != null && protein != null && fat != null && carbs != null && fiber != null) {
                saveMealToDatabase(calories, protein, fat, carbs, fiber)
            } else {
                Toast.makeText(this, "Please enter valid values for all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun saveMealToDatabase(calories: Double, protein: Double, fat: Double, carbs: Double, fiber: Double) {
        lifecycleScope.launch(Dispatchers.IO) {
            // Calculer l'index de balance
            val balanceIndex = calculateBalanceIndex(calories, protein, fat, carbs, fiber)

            // Créer un objet Meal
            val date = System.currentTimeMillis()
            val time = System.currentTimeMillis()
            val meal = Meal(
                date = date,
                time = time.toString(),
                calories = calories,
                protein = protein,
                fat = fat,
                carbs = carbs,
                fiber = fiber,
                balanceIndex = balanceIndex
            )

            // Insérer le repas dans la base de données
            db.mealDao().insert(meal)

            withContext(Dispatchers.Main) {
                Toast.makeText(this@ManualEntryActivity, "Data saved successfully", Toast.LENGTH_SHORT).show()
                finish() // Fermer l'activité après la sauvegarde
            }
        }
    }
    private fun calculateBalanceIndex(calories: Double, protein: Double, fat: Double, carbs: Double, fiber: Double): Double {
        val proteinCalories = protein * 4
        val fatCalories = fat * 9
        val carbCalories = carbs * 4
        val fiberCalories = fiber * 2
        val totalMacronutrientCalories = proteinCalories + fatCalories + carbCalories + fiberCalories

        val proteinRatio = proteinCalories / totalMacronutrientCalories
        val fatRatio = fatCalories / totalMacronutrientCalories
        val carbRatio = carbCalories / totalMacronutrientCalories
        val fiberRatio = fiberCalories / totalMacronutrientCalories

        return 1.0 - (Math.abs(proteinRatio - 0.3) + Math.abs(fatRatio - 0.3) + Math.abs(carbRatio - 0.3) + Math.abs(fiberRatio - 0.1))
    }
}
