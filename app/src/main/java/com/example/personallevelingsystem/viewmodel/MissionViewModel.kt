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
            viewModelScope.launch(Dispatchers.IO) {
                repository.completeMission(mission)
                userRepository.addXp(userId, mission.reward)
                mission.isCompleted = true
                // Reload missions after completion
                loadMissions()
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
