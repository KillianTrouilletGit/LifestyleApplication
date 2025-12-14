package com.example.personallevelingsystem.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.personallevelingsystem.MainActivity
import com.example.personallevelingsystem.R
import com.example.personallevelingsystem.data.AppDatabase
import com.example.personallevelingsystem.model.Meal
import com.example.personallevelingsystem.model.MissionType
import com.example.personallevelingsystem.repository.UserRepository
import com.example.personallevelingsystem.viewmodel.MissionViewModel
import com.example.personallevelingsystem.viewmodel.MissionViewModelFactory
import com.example.personallevelingsystem.viewmodel.UserViewModel
import com.example.personallevelingsystem.viewmodel.UserViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class NutritionActivity : AppCompatActivity() {

    private lateinit var resultTextView: TextView
    private lateinit var numberOfItemsEditText: EditText
    private lateinit var itemInputsLayout: LinearLayout
    private lateinit var nextButton: Button
    private lateinit var calculateButton: Button
    private lateinit var backButton: Button
    private lateinit var totalCaloriesTextView: TextView
    private lateinit var dailyRequiredKcalTextView: TextView
    private lateinit var comparisonTextView: TextView

    private val client = OkHttpClient()

    private var numberOfItems = 0
    private val foodItems = mutableListOf<FoodItem>()
    private lateinit var db: AppDatabase
    private lateinit var userRepository: UserRepository
    private lateinit var viewModel: MissionViewModel
    private lateinit var userViewModel: UserViewModel

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nutrition)
        supportActionBar?.title = ""

        numberOfItemsEditText = findViewById(R.id.number_of_items)
        itemInputsLayout = findViewById(R.id.item_inputs)
        nextButton = findViewById(R.id.next_button)
        calculateButton = findViewById(R.id.calculate_button)
        backButton = findViewById(R.id.back_button)
        resultTextView = findViewById(R.id.result_text)
        totalCaloriesTextView = findViewById(R.id.total_calories_text)
        dailyRequiredKcalTextView = findViewById(R.id.daily_required_kcal_text)
        comparisonTextView = findViewById(R.id.comparison_text)

        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "personal_levelingsystem.db").build()
        userRepository = UserRepository(db.userDao(), this)
        val factory = MissionViewModelFactory(application, userRepository)

        viewModel = ViewModelProvider(this, factory).get(MissionViewModel::class.java)
        val userFactory = UserViewModelFactory(userRepository)
        userViewModel = ViewModelProvider(this, userFactory).get(UserViewModel::class.java)

        nextButton.setOnClickListener {
            numberOfItems = numberOfItemsEditText.text.toString().toIntOrNull() ?: 0
            if (numberOfItems > 0) {
                setupItemInputs()
            } else {
                Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show()
            }
        }

        calculateButton.setOnClickListener {
            fetchNutritionDataForAllItems()
        }
        backButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun setupItemInputs() {
        itemInputsLayout.removeAllViews()
        foodItems.clear() // Clear the list to avoid duplication

        for (i in 1..numberOfItems) {
            val foodNameEditText = AutoCompleteTextView(this)
            foodNameEditText.id = View.generateViewId()
            foodNameEditText.hint = "Enter food name"
            foodNameEditText.inputType = android.text.InputType.TYPE_CLASS_TEXT

            val foodQuantityEditText = EditText(this)
            foodQuantityEditText.id = View.generateViewId()
            foodQuantityEditText.hint = "Enter quantity in grams"
            foodQuantityEditText.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL

            val foodItem = FoodItem(foodNameEditText, foodQuantityEditText)
            foodNameEditText.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {}
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s != null && s.length >= 2 && !foodItem.suggestionsShown) {
                        fetchFoodSuggestions(s.toString(), foodNameEditText, foodItem)
                    }
                }
            })

            itemInputsLayout.addView(foodNameEditText)
            itemInputsLayout.addView(foodQuantityEditText)
            foodItems.add(foodItem)
        }
        itemInputsLayout.visibility = View.VISIBLE
        calculateButton.visibility = View.VISIBLE
    }

    private fun fetchFoodSuggestions(query: String, textView: AutoCompleteTextView, foodItem: FoodItem) {
        val apiKey = com.example.personallevelingsystem.BuildConfig.USDA_API_KEY
        val url = "https://api.nal.usda.gov/fdc/v1/foods/search?api_key=$apiKey&query=$query"
        val request = Request.Builder().url(url).build()
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                runOnUiThread { resultTextView.text = "Failed to fetch data." }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    if (responseData != null) {
                        val json = JSONObject(responseData)
                        val foods = json.getJSONArray("foods")
                        val suggestions = mutableListOf<String>()
                        for (i in 0 until foods.length()) {
                            val food = foods.getJSONObject(i)
                            suggestions.add(food.getString("description"))
                        }
                        runOnUiThread {
                            val adapter = ArrayAdapter(this@NutritionActivity, android.R.layout.simple_dropdown_item_1line, suggestions)
                            textView.setAdapter(adapter)
                            textView.showDropDown()

                        }
                    }
                }
            }
        })
    }
    private fun fetchNutritionDataForAllItems() {
        lifecycleScope.launch(Dispatchers.IO) {
            var totalCalories = 0.0
            var totalProtein = 0.0
            var totalFat = 0.0
            var totalCarbs = 0.0
            var totalFiber = 0.0
            var completedRequests = 0

            foodItems.forEach { item ->
                val foodName = item.foodNameEditText.text.toString()
                val foodQuantity = item.foodQuantityEditText.text.toString().toDoubleOrNull() ?: 0.0
                fetchNutritionData(foodName, foodQuantity) { calories, protein, fat, carbs, fiber ->
                    totalCalories += calories
                    totalProtein += protein
                    totalFat += fat
                    totalCarbs += carbs
                    totalFiber += fiber
                    completedRequests++
                    if (completedRequests == foodItems.size) {
                        val balanceIndex = calculateBalanceIndex(totalCalories, totalProtein, totalFat, totalCarbs, totalFiber)
                        saveMeal(totalCalories, totalProtein, totalFat, totalCarbs, totalFiber, balanceIndex)
                        fetchAndCompareCalories(calories)
                    }
                }
            }
        }
    }

    private fun fetchNutritionData(foodName: String, foodQuantity: Double, callback: (Double, Double, Double, Double, Double) -> Unit) {
        val apiKey = com.example.personallevelingsystem.BuildConfig.USDA_API_KEY
        val url = "https://api.nal.usda.gov/fdc/v1/foods/search?api_key=$apiKey&query=$foodName"
        val request = Request.Builder().url(url).build()
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                runOnUiThread { resultTextView.text = "Failed to fetch data." }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    if (responseData != null) {
                        val json = JSONObject(responseData)
                        val foods = json.getJSONArray("foods")
                        if (foods.length() > 0) {
                            val food = foods.getJSONObject(0)
                            val nutrients = food.getJSONArray("foodNutrients")
                            var calories = 0.0
                            var protein = 0.0
                            var fat = 0.0
                            var carbs = 0.0
                            var fiber = 0.0
                            for (i in 0 until nutrients.length()) {
                                val nutrient = nutrients.getJSONObject(i)
                                when (nutrient.getInt("nutrientId")) {
                                    1008 -> calories = nutrient.getDouble("value")
                                    1003 -> protein = nutrient.getDouble("value")
                                    1004 -> fat = nutrient.getDouble("value")
                                    1005 -> carbs = nutrient.getDouble("value")
                                    1079 -> fiber = nutrient.getDouble("value")
                                }
                            }
                            val factor = foodQuantity / 100
                            callback(calories * factor, protein * factor, fat * factor, carbs * factor, fiber * factor)
                        }
                    }
                }
            }
        })
    }

    private fun calculateBalanceIndex(calories: Double, protein: Double, fat: Double, carbs: Double, fiber: Double): Double {
        // Simple balance index calculation
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

    @SuppressLint("SetTextI18n")
    private fun saveMeal(calories: Double, protein: Double, fat: Double, carbs: Double, fiber: Double, balanceIndex: Double) {
        lifecycleScope.launch(Dispatchers.IO) {
            val date = System.currentTimeMillis()
            val time = System.currentTimeMillis()
            val meal = Meal(date = date, time = time.toString(), calories = calories, protein = protein, fat = fat, carbs = carbs, fiber = fiber, balanceIndex = balanceIndex)

            db.mealDao().insert(meal)
            withContext(Dispatchers.Main) {
                resultTextView.text = """
                    Calories: $calories
                    Protein: $protein g
                    Fat: $fat g
                    Carbohydrates: $carbs g
                    Fiber: $fiber g
                    Balance Index: $balanceIndex
                """.trimIndent()
                resultTextView.visibility = View.VISIBLE
            }
        }
    }

    private fun fetchAndCompareCalories(calories: Double) {
        lifecycleScope.launch {
            val totalCaloriesDeferred = async(Dispatchers.IO) {
                viewModel.getTotalCaloriesForCurrentDay()
            }
            val dailyKcalDeferred = async(Dispatchers.IO) {
                val userId = db.userDao().getLatestUser()?.id
                userId?.let { userViewModel.calculateDailyRequiredKcal(it) }
            }

            val totalCalories = totalCaloriesDeferred.await()
            val dailyKcal = dailyKcalDeferred.await()

            withContext(Dispatchers.Main) {
                totalCaloriesTextView.text = "Total calories consumed today: ${totalCalories + calories ?: "N/A"} kcal"
                totalCaloriesTextView.visibility = View.VISIBLE

                dailyRequiredKcalTextView.text = "Daily required kcal: ${dailyKcal ?: "N/A"} kcal"
                dailyRequiredKcalTextView.visibility = View.VISIBLE

                comparisonTextView.text = if (totalCalories != null && dailyKcal != null) {
                    if (totalCalories > dailyKcal) {
                        lifecycleScope.launch(Dispatchers.IO) {
                            db.userDao().getLatestUser()?.id?.let {
                                viewModel.completeMissionById("daily_nutrition", MissionType.DAILY, it)
                            }
                        }
                        "You have consumed more than the required calories for today."
                    } else {
                        "You have consumed less than the required calories for today."
                    }
                } else {
                    "Unable to compare calories."
                }

                comparisonTextView.visibility = View.VISIBLE
            }
        }
    }

    data class FoodItem(val foodNameEditText: AutoCompleteTextView, val foodQuantityEditText: EditText, var suggestionsShown: Boolean = false )
}
