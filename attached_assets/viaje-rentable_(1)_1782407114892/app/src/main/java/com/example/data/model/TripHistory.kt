package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class TripHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val platform: String, // "Uber", "DiDi", "Otro"
    val earnings: Double,
    val pickupDistance: Double,
    val pickupUnit: String, // "km", "m"
    val tripDistance: Double,
    val totalDistance: Double,
    val earningsPerKmReal: Double,
    val earningsPerKmTrip: Double,
    val status: String // "EXCELENTE", "ACEPTABLE", "MALO"
)
