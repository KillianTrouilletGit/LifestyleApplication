package com.example.personallevelingsystem.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.personallevelingsystem.data.UserDao
import com.example.personallevelingsystem.model.User
import com.example.personallevelingsystem.util.MissionPrefs
import com.example.personallevelingsystem.util.NotificationUtils
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class UserRepository(private val userDao: UserDao, private val context: Context) {

    companion object {
        /** The app is single-user: every XP / profile path targets this row. */
        const val DEFAULT_USER_ID = 1
    }

    /**
     * Returns the operator row, creating it if the table is still empty.
     * Called at app start and defensively from [addXp] so XP is never dropped
     * on a fresh install before the profile has been filled in.
     */
    suspend fun ensureDefaultUser(): User {
        userDao.getUserById(DEFAULT_USER_ID)?.let { return it }
        val user = User(id = DEFAULT_USER_ID, name = "Operator", weight = 0f, height = 0f, dateOfBirth = "")
        userDao.insert(user)
        return user
    }

    suspend fun addXp(userId: Int, xpToAdd: Int) {
        val user = userDao.getUserById(userId)
            ?: if (userId == DEFAULT_USER_ID) ensureDefaultUser() else return
        val oldLevel = user.level
        user.xp += xpToAdd
        var xpForNextLevel = calculateXpForNextLevel(user.level)
        while (user.xp >= xpForNextLevel) {
            user.xp -= xpForNextLevel
            user.level += 1
            xpForNextLevel = calculateXpForNextLevel(user.level)
        }
        userDao.update(user)
        checkLevelUp(user, oldLevel)
    }

    suspend fun insertUser(user: User) {
        userDao.insert(user)
        if (user.weight > 0f) {
            MissionPrefs.get(context).setWeightLoggedAt(System.currentTimeMillis())
        }
    }

    suspend fun updateUser(user: User) {
        val previous = userDao.getUserById(user.id)
        userDao.update(user)
        // Stamp weight-logged-at whenever weight actually changes, for the
        // weekly "Body Stat Check" mission and the Sunday weigh-in reminder.
        if (previous == null || previous.weight != user.weight) {
            MissionPrefs.get(context).setWeightLoggedAt(System.currentTimeMillis())
        }
    }

    suspend fun getUserById(userId: Int): User? {
        return userDao.getUserById(userId)
    }


    suspend fun getUserByName(name: String): User? {
        return userDao.getUserByName(name)
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
        val user = userDao.getUserById(userId) ?: return null
        val age = calculateAge(user.dateOfBirth) ?: return null
        val bmr = 88.362 + (13.397 * user.weight) + (4.799 * user.height) - (5.677 * age)
        val activityFactor = 1.55 // Moderate activity level
        return bmr * activityFactor
    }
    /** Null when the stored date of birth is empty or not yyyy-MM-dd. */
    private fun calculateAge(dateOfBirth: String): Int? {
        if (dateOfBirth.isBlank()) return null
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dob = try { sdf.parse(dateOfBirth) } catch (e: ParseException) { null } ?: return null
        val today = Calendar.getInstance()
        val birthDate = Calendar.getInstance().apply { time = dob }

        var age = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR)
        if (today.get(Calendar.DAY_OF_YEAR) < birthDate.get(Calendar.DAY_OF_YEAR)) {
            age--
        }
        return age
    }


}
