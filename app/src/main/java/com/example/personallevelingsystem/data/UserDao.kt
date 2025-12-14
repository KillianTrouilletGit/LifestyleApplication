package com.example.personallevelingsystem.data

import androidx.compose.ui.Modifier
import androidx.room.*
import com.example.personallevelingsystem.model.User

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)

    @Query("DELETE FROM users")
    fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(users: List<User>)

    @Update
    suspend fun update(user: User)

    @Query("SELECT * FROM users")
    suspend fun getAll(): List<User>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Int): User?

    @Query("SELECT * FROM users WHERE name = :name")
    suspend fun getUserByName(name: String): User?

    @Query("SELECT * FROM users ORDER BY id DESC LIMIT 1")
    fun getLatestUser(): User?
}
