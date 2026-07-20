package com.example.personallevelingsystem.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.personallevelingsystem.model.Mission
import com.example.personallevelingsystem.model.MissionCategory
import com.example.personallevelingsystem.model.MissionType
import com.example.personallevelingsystem.repository.MissionRepository
import com.example.personallevelingsystem.repository.UserRepository
import com.example.personallevelingsystem.service.MissionAutoCompleter
import com.example.personallevelingsystem.service.MissionProgress
import com.example.personallevelingsystem.util.MissionPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MissionViewModel(
    application: Application,
    private val userRepository: UserRepository
) : AndroidViewModel(application) {

    private val repository = MissionRepository(application)
    private val autoCompleter = MissionAutoCompleter(application)
    private val prefs = MissionPrefs.get(application)

    private val _dailyMissions = MutableLiveData<List<Mission>>()
    val dailyMissions: LiveData<List<Mission>> = _dailyMissions

    private val _weeklyMissions = MutableLiveData<List<Mission>>()
    val weeklyMissions: LiveData<List<Mission>> = _weeklyMissions

    /** Progress snapshots keyed by mission id. Only present for data-driven missions. */
    private val _missionProgress = MutableLiveData<Map<String, MissionProgress>>(emptyMap())
    val missionProgress: LiveData<Map<String, MissionProgress>> = _missionProgress

    /** Streak counters keyed by mission id. */
    private val _streaks = MutableLiveData<Map<String, Int>>(emptyMap())
    val streaks: LiveData<Map<String, Int>> = _streaks

    /** XP totals keyed by category — for the specialization summary card. */
    private val _categoryXp = MutableLiveData<Map<MissionCategory, Int>>(emptyMap())
    val categoryXp: LiveData<Map<MissionCategory, Int>> = _categoryXp

    /** Streak value to celebrate (Duolingo-style). Null when nothing to show. */
    private val _streakCelebration = MutableLiveData<Int?>(null)
    val streakCelebration: LiveData<Int?> = _streakCelebration

    fun clearStreakCelebration() {
        _streakCelebration.postValue(null)
    }

    init {
        refresh()
    }

    /**
     * Reload missions from repository, then run an auto-complete sweep,
     * then re-read state to surface freshly-ticked missions.
     */
    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            // First sweep against current data — may auto-complete some
            val freshly = autoCompleter.sweep()
            // Auto-completed missions with a continuing streak also earn the celebration
            val bestFreshStreak = freshly.maxOfOrNull { prefs.getStreak(it.id) } ?: 0
            if (bestFreshStreak >= 2) _streakCelebration.postValue(bestFreshStreak)

            val daily = repository.getDailyMissions()
            val weekly = repository.getWeeklyMissions()
            _dailyMissions.postValue(daily)
            _weeklyMissions.postValue(weekly)

            val progressMap = mutableMapOf<String, MissionProgress>()
            for (m in daily + weekly) {
                val p = autoCompleter.computeProgress(m) ?: continue
                progressMap[m.id] = p
            }
            _missionProgress.postValue(progressMap)

            _streaks.postValue((daily + weekly).associate { it.id to prefs.getStreak(it.id) })
            _categoryXp.postValue(prefs.allCategoryXp())
        }
    }

    fun completeMission(mission: Mission, userId: Int) {
        if (!mission.isCompleted) {
            // Optimistic UI: tick it locally
            val currentDaily = _dailyMissions.value.orEmpty().toMutableList()
            val indexDaily = currentDaily.indexOfFirst { it.id == mission.id }
            if (indexDaily != -1) {
                currentDaily[indexDaily] = currentDaily[indexDaily].copy(isCompleted = true)
                _dailyMissions.value = ArrayList(currentDaily)
            }

            val currentWeekly = _weeklyMissions.value.orEmpty().toMutableList()
            val indexWeekly = currentWeekly.indexOfFirst { it.id == mission.id }
            if (indexWeekly != -1) {
                currentWeekly[indexWeekly] = currentWeekly[indexWeekly].copy(isCompleted = true)
                _weeklyMissions.value = ArrayList(currentWeekly)
            }

            viewModelScope.launch(Dispatchers.IO) {
                repository.completeMission(mission)
                val awarded = autoCompleter.awardWithStreak(mission)
                userRepository.addXp(userId, awarded)
                val newStreak = prefs.getStreak(mission.id)
                if (newStreak >= 2) _streakCelebration.postValue(newStreak)
                refresh()
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

    suspend fun getTotalCaloriesForCurrentDay(): Double = withContext(Dispatchers.IO) {
        repository.getTotalCaloriesForCurrentDay()
    }
}
