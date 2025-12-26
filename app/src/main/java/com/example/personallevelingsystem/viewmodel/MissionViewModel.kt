package com.example.personallevelingsystem.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.personallevelingsystem.model.Mission
import com.example.personallevelingsystem.model.MissionType
import com.example.personallevelingsystem.repository.MissionRepository
import com.example.personallevelingsystem.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MissionViewModel(
    application: Application,
    private val userRepository: UserRepository // Add userRepository as a parameter
) : AndroidViewModel(application) {

    private val repository = MissionRepository(application)

    private val _dailyMissions = MutableLiveData<List<Mission>>()
    val dailyMissions: LiveData<List<Mission>> = _dailyMissions

    private val _weeklyMissions = MutableLiveData<List<Mission>>()
    val weeklyMissions: LiveData<List<Mission>> = _weeklyMissions

    init {
        // Load missions when the ViewModel is created
        loadMissions()
    }

    private fun loadMissions() {
        viewModelScope.launch(Dispatchers.IO) {
            _dailyMissions.postValue(repository.getDailyMissions())
            _weeklyMissions.postValue(repository.getWeeklyMissions())
        }
    }

    fun completeMission(mission: Mission, userId: Int) { // Add userId parameter
        if (!mission.isCompleted) {
            // Optimistic Update: Update UI immediately
            val currentDaily = _dailyMissions.value.orEmpty().toMutableList()
            val indexDaily = currentDaily.indexOfFirst { it.id == mission.id }
            if (indexDaily != -1) {
                // Assuming Mission is mutable for now, or use copy if data class. 
                // Since it's passed as reference, modification should work but we need to post NEW list reference
                // for LiveData to trigger observers.
                // Creating a shallow copy of the list is enough if item was modified.
                currentDaily[indexDaily] = currentDaily[indexDaily].copy(isCompleted = true) 
                _dailyMissions.value = ArrayList(currentDaily) // Post new reference
            }

            val currentWeekly = _weeklyMissions.value.orEmpty().toMutableList()
            val indexWeekly = currentWeekly.indexOfFirst { it.id == mission.id }
            if (indexWeekly != -1) {
                currentWeekly[indexWeekly] = currentWeekly[indexWeekly].copy(isCompleted = true)
                _weeklyMissions.value = ArrayList(currentWeekly)
            }
            
            // Persist in background
            viewModelScope.launch(Dispatchers.IO) {
                repository.completeMission(mission)
                userRepository.addXp(userId, mission.reward)
                // We don't need to reloadMissions() immediately if we trust our optimistic update,
                // but it's safe to do so to ensure consistency later. 
                // loadMissions() 
                // Actually, let's NOT reload immediately to avoid overwriting the optimistic state 
                // if the DB write lags slightly.
            }
        }
    }

    fun completeMissionById(missionId: String, type: MissionType, userId: Int) {
        val missionList = if (type == MissionType.DAILY) _dailyMissions.value else _weeklyMissions.value
        missionList?.let {
            val mission = it.find { mission -> mission.id == missionId }
            mission?.let { completeMission(it, userId) }
        }
    }



    suspend fun getTotalCaloriesForCurrentDay(): Double {
        return repository.getTotalCaloriesForCurrentDay()
    }
}
