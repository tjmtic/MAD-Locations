package com.abyxcz.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.abyxcz.data.entity.GeofenceEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the geofences table.
 */
@Dao
interface GeofenceDao {

    /**
     * Observes list of geofences.
     *
     * @return all geofences.
     */
    @Query("SELECT * FROM Geofences")
    fun observeGeofences(): Flow<List<GeofenceEntity>>

    /**
     * Observes a single geofence.
     *
     * @param geofenceId the geofence id.
     * @return the geofence with geofenceId.
     */
    @Query("SELECT * FROM Geofences WHERE id = :geofenceId")
    fun observeGeofenceById(geofenceId: String): Flow<GeofenceEntity>

    /**
     * Select all geofences from the geofences table.
     *
     * @return all geofences.
     */
    @Query("SELECT * FROM Geofences")
    suspend fun getGeofences(): List<GeofenceEntity>

    /**
     * Select a geofence by id.
     *
     * @param geofenceId the geofence id.
     * @return the geofence with geofenceId.
     */
    @Query("SELECT * FROM Geofences WHERE id = :geofenceId")
    suspend fun getGeofenceById(geofenceId: String): GeofenceEntity?

    /**
     * Insert a geofence in the database. If the geofence already exists, replace it.
     *
     * @param geofenceEntity the geofence to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGeofence(geofenceEntity: GeofenceEntity)

    /**
     * Insert a list of geofences in the database. If the geofence already exists, replace it.
     *
     * @param geofenceEntity the geofence to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGeofences(geofenceEntities: List<GeofenceEntity>)

    /**
     * Update a geofence.
     *
     * @param geofenceEntity geofence to be updated
     * @return the number of geofences updated. This should always be 1.
     */
    @Update
    suspend fun updateGeofence(geofenceEntity: GeofenceEntity): Int

    /**
     * Update a list of geofences.
     *
     * @param geofenceEntity geofence to be updated
     * @return the number of geofences updated. This should always be 1.
     */
    @Update
    suspend fun updateGeofences(geofenceEntities: List<GeofenceEntity>): Int

    /**
     * Delete a geofence by id.
     *
     * @return the number of geofences deleted. This should always be 1.
     */
    @Query("DELETE FROM Geofences WHERE id = :geofenceId")
    suspend fun deleteGeofenceById(geofenceId: String): Int

    /**
     * Delete all geofences.
     */
    @Query("DELETE FROM Geofences")
    suspend fun deleteGeofences()

}