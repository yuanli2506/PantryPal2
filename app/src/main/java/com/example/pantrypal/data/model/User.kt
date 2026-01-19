package com.example.pantrypal. data.model

import androidx.room.ColumnInfo
import androidx. room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "email")
    val email: String,

    @ColumnInfo(name = "password")
    val password: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "profile_picture")
    val profilePicture: String?  = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)