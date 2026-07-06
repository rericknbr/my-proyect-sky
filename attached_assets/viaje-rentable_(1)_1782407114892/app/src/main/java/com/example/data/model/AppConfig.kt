package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "config")
data class AppConfig(
    @PrimaryKey val id: Int = 1,
    val limitGreen: Double = 8.0,
    val limitYellow: Double = 6.0
)
