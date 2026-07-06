package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.AppConfig
import com.example.data.model.TripHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Query("SELECT * FROM trips ORDER BY timestamp DESC")
    fun getAllTrips(): Flow<List<TripHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripHistory)

    @Delete
    suspend fun deleteTrip(trip: TripHistory)

    @Query("DELETE FROM trips")
    suspend fun clearHistory()

    @Query("SELECT * FROM config WHERE id = 1")
    fun getConfigFlow(): Flow<AppConfig?>

    @Query("SELECT * FROM config WHERE id = 1")
    suspend fun getConfig(): AppConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: AppConfig)
}
