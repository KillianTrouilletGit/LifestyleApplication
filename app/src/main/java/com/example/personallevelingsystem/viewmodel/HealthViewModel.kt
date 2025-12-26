package com.example.personallevelingsystem.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.personallevelingsystem.data.AppDatabase
import com.example.personallevelingsystem.model.Meal
import com.example.personallevelingsystem.model.Sleep
import com.example.personallevelingsystem.model.Water
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.util.Calendar

class HealthViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val waterDao = db.WaterDao()
    private val sleepDao = db.SleepTimeDao()
    private val mealDao = db.mealDao()
    private val client = OkHttpClient()

    // LiveData Declarations (Must be before init)
    private val _totalWaterToday = MutableLiveData<Float>()
    val totalWaterToday: LiveData<Float> = _totalWaterToday

    private val _foodSuggestions = MutableLiveData<List<String>>()
    val foodSuggestions: LiveData<List<String>> = _foodSuggestions

    private val _nutritionResult = MutableLiveData<Meal?>()
    val nutritionResult: LiveData<Meal?> = _nutritionResult

    private val _dailyBalanceIndex = MutableLiveData<Double>(0.0)
    val dailyBalanceIndex: LiveData<Double> = _dailyBalanceIndex

    init {
        calculateTotalWaterForToday()
        calculateDailyBalanceIndex()
    }

    // Water Logic
    fun saveWater(amount: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            val water = Water(date = System.currentTimeMillis(), amount = amount)
            waterDao.insert(water)
            calculateTotalWaterForToday()
        }
    }

    fun calculateTotalWaterForToday() {
        viewModelScope.launch(Dispatchers.IO) {
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

            val waterEntries = waterDao.getWaterForDay(startOfDay, endOfDay)
            val total = waterEntries.sumOf { it.amount.toDouble() }.toFloat()
            _totalWaterToday.postValue(total)
        }
    }

    // Sleep Logic
    fun saveSleep(duration: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val sleep = Sleep(date = System.currentTimeMillis(), duration = duration)
            sleepDao.insert(sleep)
            // TODO: Trigger mission completion logic here or observe in View
        }
    }

    // Nutrition Logic
    fun fetchFoodSuggestions(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val apiKey = com.example.personallevelingsystem.BuildConfig.USDA_API_KEY
            val url = "https://api.nal.usda.gov/fdc/v1/foods/search?api_key=$apiKey&query=$query"
            val request = Request.Builder().url(url).build()

            try {
                val response = client.newCall(request).execute()
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
                        _foodSuggestions.postValue(suggestions)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    // Simplified nutrition fetch for single item (can be expanded to list)
    fun fetchAndSaveMeal(foodName: String, quantity: Double) {
         viewModelScope.launch(Dispatchers.IO) {
            val apiKey = com.example.personallevelingsystem.BuildConfig.USDA_API_KEY
            val url = "https://api.nal.usda.gov/fdc/v1/foods/search?api_key=$apiKey&query=$foodName"
            val request = Request.Builder().url(url).build()
            
             try {
                val response = client.newCall(request).execute()
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
                            
                            val factor = quantity / 100
                            
                            // Calculate Balance (Duplicate logic from UI for now, ideally shared util)
                            val totalCal = (calories * factor)
                            val p = protein * factor
                            val c = carbs * factor
                            val f = fat * factor
                            
                            var balance = 0.5
                            if (totalCal > 0) {
                                val pRatio = (p * 4) / totalCal
                                val cRatio = (c * 4) / totalCal
                                val fRatio = (f * 9) / totalCal
                                val diff = kotlin.math.abs(pRatio - 0.3) + kotlin.math.abs(cRatio - 0.4) + kotlin.math.abs(fRatio - 0.3)
                                balance = (1.0 - (diff / 1.5)).coerceIn(0.0, 1.0)
                            }

                            val meal = Meal(
                                date = System.currentTimeMillis(),
                                time = System.currentTimeMillis().toString(),
                                calories = totalCal,
                                protein = p,
                                fat = f,
                                carbs = c,
                                fiber = fiber * factor,
                                balanceIndex = balance
                            )
                            mealDao.insert(meal)
                            _nutritionResult.postValue(meal)
                            calculateDailyBalanceIndex()
                        }
                    }
                }
             } catch (e: Exception) {
                 e.printStackTrace()
             }
         }
    }
    // Daily Balance Logic
    fun calculateDailyBalanceIndex() {
        viewModelScope.launch(Dispatchers.IO) {
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
            
            val meals = mealDao.getMealForDay(startOfDay, endOfDay)
            if (meals.isNotEmpty()) {
                val averageBalance = meals.map { it.balanceIndex }.average()
                _dailyBalanceIndex.postValue(averageBalance)
            } else {
                _dailyBalanceIndex.postValue(0.0)
            }
        }
    }

    fun saveMeal(meal: Meal) {
        viewModelScope.launch(Dispatchers.IO) {
            mealDao.insert(meal)
            _nutritionResult.postValue(meal)
            calculateDailyBalanceIndex()
        }
    }
}
