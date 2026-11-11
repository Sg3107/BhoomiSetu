package com.satya.bhoomisetu.di

import android.content.Context
import androidx.room.Room
import com.satya.bhoomisetu.data.local.BhoomiSetuDatabase
import com.satya.bhoomisetu.data.local.dao.PropertyDao
import com.satya.bhoomisetu.data.local.dao.UserDao
import com.satya.bhoomisetu.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BhoomiSetuDatabase {
        return Room.databaseBuilder(
            context,
            BhoomiSetuDatabase::class.java,
            Constants.DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideUserDao(db: BhoomiSetuDatabase): UserDao {
        return db.userDao()
    }

    @Provides
    fun providePropertyDao(db: BhoomiSetuDatabase): PropertyDao {
        return db.propertyDao()
    }
}
