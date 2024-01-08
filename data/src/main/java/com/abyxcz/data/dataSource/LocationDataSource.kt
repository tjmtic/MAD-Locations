package com.abyxcz.data.dataSource

import com.abyxcz.data.db.LocationDao
import com.abyxcz.data.entity.LocationEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext


/**
 * Concrete implementation of a data source as a db.
 */
class LocationDataSource internal constructor(
    private val locationsDao: LocationDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : DefaultDataSource {

    override fun observeLocations(): Flow<Result<List<LocationEntity>>> {
        return locationsDao.observeLocations().map {
            Result.Success(it)
        }
    }

    override fun observeLocation(LocationId: String): Flow<Result<LocationEntity>> {
        return locationsDao.observeLocationById(LocationId).map {
            Result.Success(it)
        }
    }

    override suspend fun refreshLocation(LocationId: String) {
        //NO-OP
    }

    override suspend fun refreshLocations() {
        //NO-OP
    }

    override suspend fun getLocations(): Result<List<LocationEntity>> = withContext(ioDispatcher) {
        return@withContext try {
            Result.Success(locationsDao.getLocations())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getLocation(locationId: String): Result<LocationEntity> = withContext(ioDispatcher) {
        try {
            val location = locationsDao.getLocationById(locationId)
            if (location != null) {
                return@withContext Result.Success(location)
            } else {
                return@withContext Result.Error(Exception("Location not found!"))
            }
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    override suspend fun saveLocation(location: LocationEntity) = withContext(ioDispatcher) {
        locationsDao.insertLocation(location)
    }

    override suspend fun saveLocations(locations: List<LocationEntity>) = withContext(ioDispatcher) {
        locationsDao.insertLocations(locations)
    }

    override suspend fun updateLocation(location: LocationEntity) = withContext(ioDispatcher) {
        locationsDao.updateLocation(location)
    }

    override suspend fun updateLocations(locations: List<LocationEntity>) = withContext(ioDispatcher) {
        locationsDao.updateLocations(locations)
    }

    override suspend fun clearAllLocations() = withContext(ioDispatcher) {
        locationsDao.deleteLocations()
    }

    override suspend fun clearLocation(locationId: String) = withContext<Unit>(ioDispatcher) {
        locationsDao.deleteLocationById(locationId)
    }
}