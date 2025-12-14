package com.example.personallevelingsystem.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.personallevelingsystem.data.UserDao
import com.example.personallevelingsystem.model.User
import com.example.personallevelingsystem.util.NotificationUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class UserRepository(private val userDao: UserDao, private val context: Context) {

    suspend fun addXp(userId: Int, xpToAdd: Int) {
        val user = userDao.getUserById(userId)
        user?.let {
            val oldLevel = it.level
            it.xp += xpToAdd
            var xpForNextLevel = calculateXpForNextLevel(it.level)
            while (it.xp >= xpForNextLevel) {
                it.xp -= xpForNextLevel
                it.level += 1
                xpForNextLevel = calculateXpForNextLevel(it.level)
            }
            userDao.update(it)
            checkLevelUp(it, oldLevel)
        }
    }

    suspend fun insertUser(user: User) {
        userDao.insert(user)
    }

    suspend fun updateUser(user: User) {
        userDao.update(user)
    }

    suspend fun getUserById(userId: Int): User? {
        return userDao.getUserById(userId)
    }


    suspend fun getUserByName(name: String): User? {
        return userDao.getUserByName(name)
    }

    suspend fun createDefaultUser() {
        val user = userDao.getUserByName("User")
        if (user == null) {
            val defaultUser = User(name = "User", weight = 0f, height = 0f, dateOfBirth = "NONE")
            userDao.insert(defaultUser)
        }
    }

    fun calculateXpForNextLevel(level: Int): Int {
        return 100 * level * level
    }

    private fun checkLevelUp(user: User, oldLevel: Int) {
        if (user.level > oldLevel) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED) {
                NotificationUtils.showLevelUpNotification(context, user.level)
            } else {
                // Gérez le cas où la permission n'est pas accordée
                // Vous pouvez enregistrer l'événement et réessayer plus tard
            }
        }
    }
    suspend fun calculateDailyRequiredKcal(userId: Int): Double? {
        val user = userDao.getUserById(userId)
        return user?.let {
            val age = calculateAge(it.dateOfBirth)
            val bmr = 88.362 + (13.397 * it.weight) + (4.799 * it.height) - (5.677 * age)
            val activityFactor = 1.55 // Moderate activity level
            bmr * activityFactor
        }
    }
    private fun calculateAge(dateOfBirth: String): Int {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dob = sdf.parse(dateOfBirth)
        val today = Calendar.getInstance()
        val birthDate = Calendar.getInstance()
        if (dob != null) {
            birthDate.time = dob
        }

        var age = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR)
        if (today.get(Calendar.DAY_OF_YEAR) < birthDate.get(Calendar.DAY_OF_YEAR)) {
            age--
        }
        return age
    }


}
