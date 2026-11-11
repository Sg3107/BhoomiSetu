package com.satya.bhoomisetu.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.satya.bhoomisetu.data.local.entity.SurveyorProfileEntity
import com.satya.bhoomisetu.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSurveyorProfile(profile: SurveyorProfileEntity): Long

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId")
    fun observeUserById(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM surveyor_profiles WHERE userId = :userId")
    suspend fun getSurveyorProfileByUserId(userId: String): SurveyorProfileEntity?

    @Query("DELETE FROM users")
    suspend fun clearUsers(): Int

    @Query("DELETE FROM surveyor_profiles")
    suspend fun clearSurveyorProfiles(): Int
}
