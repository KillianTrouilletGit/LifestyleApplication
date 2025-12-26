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

    // Water Logic
    private val _totalWaterToday = MutableLiveData<Float>()
    val totalWaterToday: LiveData<Float> = _totalWaterToday

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
    private val _foodSuggestions = MutableLiveData<List<String>>()
    val foodSuggestions: LiveData<List<String>> = _foodSuggestions

    private val _nutritionResult = MutableLiveData<Meal?>()
    val nutritionResult: LiveData<Meal?> = _nutritionResult

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
                            val meal = Meal(
                                date = System.currentTimeMillis(),
                                time = System.currentTimeMillis().toString(),
                                calories = calories * factor,
                                protein = protein * factor,
                                fat = fat * factor,
                                carbs = carbs * factor,
                                fiber = fiber * factor,
                                balanceIndex = 0.0 // Simplified for now
                            )
                            mealDao.insert(meal)
                            _nutritionResult.postValue(meal)
                        }
                    }
                }
             } catch (e: Exception) {
                 e.printStackTrace()
             }
         }
    }
}
