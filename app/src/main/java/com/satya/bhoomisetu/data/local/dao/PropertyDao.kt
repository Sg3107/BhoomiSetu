package com.satya.bhoomisetu.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.satya.bhoomisetu.data.local.entity.PropertyNoteEntity
import com.satya.bhoomisetu.data.local.entity.PropertyProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PropertyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProperty(property: PropertyProfileEntity)

    @Query("SELECT * FROM property_profiles WHERE ownerId = :ownerId ORDER BY createdAt DESC")
    fun getAllProperties(ownerId: String): Flow<List<PropertyProfileEntity>>

    @Query("SELECT * FROM property_profiles WHERE id = :id LIMIT 1")
    suspend fun getPropertyById(id: String): PropertyProfileEntity?

    @Query("DELETE FROM property_profiles WHERE id = :id")
    suspend fun deleteProperty(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: PropertyNoteEntity)

    @Query("SELECT * FROM property_notes WHERE propertyId = :propertyId ORDER BY createdAt DESC")
    fun getNotesForProperty(propertyId: String): Flow<List<PropertyNoteEntity>>

    @Delete
    suspend fun deleteNote(note: PropertyNoteEntity)

    @Query("DELETE FROM property_notes WHERE propertyId = :propertyId")
    suspend fun deleteNotesForProperty(propertyId: String)
}
