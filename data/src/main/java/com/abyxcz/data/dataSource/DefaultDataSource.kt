package com.abyxcz.data.dataSource

import com.abyxcz.data.entity.LocationEntity
import kotlinx.coroutines.flow.Flow

/**
 * Main entry point for accessing items data.
 */
interface DefaultDataSource {


    fun observeLocations(): Flow<Result<List<LocationEntity>>>

    suspend fun getLocations(): Result<List<LocationEntity>>

    suspend fun refreshLocations()

    fun observeLocation(itemId: String): Flow<Result<LocationEntity>>

    suspend fun getLocation(itemId: String): Result<LocationEntity>

    suspend fun refreshLocation(itemId: String)

    suspend fun saveLocation(item: LocationEntity)

    suspend fun saveLocations(items: List<LocationEntity>)

    suspend fun updateLocation(item: LocationEntity) : Int

    suspend fun updateLocations(items: List<LocationEntity>) : Int

    suspend fun clearAllLocations()

    suspend fun clearLocation(itemId: String)
}