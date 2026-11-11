package com.satya.bhoomisetu.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.satya.bhoomisetu.domain.model.User
import com.satya.bhoomisetu.domain.model.UserRole

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String,
    val email: String,
    val role: String,
    val createdAt: Long
) {
    fun toDomainModel(): User {
        return User(
            id = id,
            name = name,
            phone = phone,
            email = email,
            role = try { UserRole.valueOf(role) } catch (e: Exception) { UserRole.USER },
            createdAt = createdAt
        )
    }

    companion object {
        fun fromDomainModel(user: User): UserEntity {
            return UserEntity(
                id = user.id,
                name = user.name,
                phone = user.phone,
                email = user.email,
                role = user.role.name,
                createdAt = user.createdAt
            )
        }
    }
}
