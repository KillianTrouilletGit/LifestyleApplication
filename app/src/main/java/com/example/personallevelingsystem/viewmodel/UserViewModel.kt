package com.example.personallevelingsystem.viewmodel

import androidx.lifecycle.*
import com.example.personallevelingsystem.data.AppDatabase
import com.example.personallevelingsystem.data.UserDao
import com.example.personallevelingsystem.model.User
import com.example.personallevelingsystem.repository.UserRepository
import kotlinx.coroutines.launch

class UserViewModel(private val repository: UserRepository) : ViewModel() {

    private val _user = MutableLiveData<User?>()
    private lateinit var db: AppDatabase
    val user: LiveData<User?> get() = _user

    fun getUserById(userId: Int) {
        viewModelScope.launch {
            val fetchedUser = repository.getUserById(userId)
            _user.postValue(fetchedUser)
        }
    }

    fun addXpToUser(userId: Int, xpToAdd: Int) {
        viewModelScope.launch {
            repository.addXp(userId, xpToAdd)
            val updatedUser = repository.getUserById(userId)
            _user.postValue(updatedUser)
        }
    }

    fun insertUser(user: User) {
        viewModelScope.launch {
            repository.insertUser(user)
            _user.postValue(user)
        }
    }

    fun updateUser(user: User) {
        viewModelScope.launch {
            repository.updateUser(user)
            _user.postValue(user)
        }
    }



    fun calculateXpForNextLevel(level: Int): Int {
        return repository.calculateXpForNextLevel(level)
    }

    suspend fun calculateDailyRequiredKcal(userId: Int): Double? {
        return repository.calculateDailyRequiredKcal(userId)
    }




}
