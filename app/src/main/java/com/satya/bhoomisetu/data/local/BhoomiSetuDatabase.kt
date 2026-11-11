package com.satya.bhoomisetu.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.satya.bhoomisetu.data.local.dao.PropertyDao
import com.satya.bhoomisetu.data.local.dao.UserDao
import com.satya.bhoomisetu.data.local.entity.PropertyNoteEntity
import com.satya.bhoomisetu.data.local.entity.PropertyProfileEntity
import com.satya.bhoomisetu.data.local.entity.SurveyorProfileEntity
import com.satya.bhoomisetu.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        SurveyorProfileEntity::class,
        PropertyProfileEntity::class,
        PropertyNoteEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class BhoomiSetuDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun propertyDao(): PropertyDao
}
