package com.abyxcz.data.dataSource

import com.abyxcz.data.entity.GeofenceEntity
import kotlinx.coroutines.flow.Flow

/**
 * Main entry point for accessing items data.
 */
interface DefaultGeofenceDataSource {


    fun observeGeofences(): Flow<Result<List<GeofenceEntity>>>

    suspend fun getGeofences(): Result<List<GeofenceEntity>>

    suspend fun refreshGeofences()

    fun observeGeofence(itemId: String): Flow<Result<GeofenceEntity>>

    suspend fun getGeofence(itemId: String): Result<GeofenceEntity>

    suspend fun refreshGeofence(itemId: String)

    suspend fun saveGeofence(item: GeofenceEntity)

    suspend fun saveGeofences(items: List<GeofenceEntity>)

    suspend fun updateGeofence(item: GeofenceEntity) : Int

    suspend fun updateGeofences(items: List<GeofenceEntity>) : Int

    suspend fun clearAllGeofences()

    suspend fun clearGeofence(itemId: String)
}