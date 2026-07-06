package com.example.data.repository

import com.example.data.dao.TripDao
import com.example.data.model.AppConfig
import com.example.data.model.TripHistory
import kotlinx.coroutines.flow.Flow

class TripRepository(private val tripDao: TripDao) {
    val allTrips: Flow<List<TripHistory>> = tripDao.getAllTrips()
    val config: Flow<AppConfig?> = tripDao.getConfigFlow()

    suspend fun insertTrip(trip: TripHistory) {
         tripDao.insertTrip(trip)
    }

    suspend fun deleteTrip(trip: TripHistory) {
         tripDao.deleteTrip(trip)
    }

    suspend fun clearHistory() {
         tripDao.clearHistory()
    }

    suspend fun getConfig(): AppConfig {
         return tripDao.getConfig() ?: AppConfig()
    }

    suspend fun saveConfig(config: AppConfig) {
         tripDao.insertConfig(config)
    }
}
