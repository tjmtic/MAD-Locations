package com.abyxcz.data.dataSource

import com.abyxcz.data.db.GeofenceDao
import com.abyxcz.data.entity.GeofenceEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext


/**
 * Concrete implementation of a data source as a db.
 */
class GeofenceDataSource internal constructor(
    private val geofencesDao: GeofenceDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : DefaultGeofenceDataSource {

    override fun observeGeofences(): Flow<Result<List<GeofenceEntity>>> {
        return geofencesDao.observeGeofences().map {
            Result.Success(it)
        }
    }

    override fun observeGeofence(GeofenceId: String): Flow<Result<GeofenceEntity>> {
        return geofencesDao.observeGeofenceById(GeofenceId).map {
            Result.Success(it)
        }
    }

    override suspend fun refreshGeofence(GeofenceId: String) {
        //NO-OP
    }

    override suspend fun refreshGeofences() {
        //NO-OP
    }

    override suspend fun getGeofences(): Result<List<GeofenceEntity>> = withContext(ioDispatcher) {
        return@withContext try {
            Result.Success(geofencesDao.getGeofences())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getGeofence(geofenceId: String): Result<GeofenceEntity> = withContext(ioDispatcher) {
        try {
            val geofence = geofencesDao.getGeofenceById(geofenceId)
            if (geofence != null) {
                return@withContext Result.Success(geofence)
            } else {
                return@withContext Result.Error(Exception("Geofence not found!"))
            }
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    override suspend fun saveGeofence(geofence: GeofenceEntity) = withContext(ioDispatcher) {
        geofencesDao.insertGeofence(geofence)
    }

    override suspend fun saveGeofences(geofences: List<GeofenceEntity>) = withContext(ioDispatcher) {
        geofencesDao.insertGeofences(geofences)
    }

    override suspend fun updateGeofence(geofence: GeofenceEntity) = withContext(ioDispatcher) {
        geofencesDao.updateGeofence(geofence)
    }

    override suspend fun updateGeofences(geofences: List<GeofenceEntity>) = withContext(ioDispatcher) {
        geofencesDao.updateGeofences(geofences)
    }

    override suspend fun clearAllGeofences() = withContext(ioDispatcher) {
        geofencesDao.deleteGeofences()
    }

    override suspend fun clearGeofence(geofenceId: String) = withContext<Unit>(ioDispatcher) {
        geofencesDao.deleteGeofenceById(geofenceId)
    }
}