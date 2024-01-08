package com.abyxcz.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.abyxcz.data.entity.LocationEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the locations table.
 */
@Dao
interface LocationDao {

    /**
     * Observes list of locations.
     *
     * @return all locations.
     */
    @Query("SELECT * FROM Locations")
    fun observeLocations(): Flow<List<LocationEntity>>

    /**
     * Observes a single location.
     *
     * @param locationId the location id.
     * @return the location with locationId.
     */
    @Query("SELECT * FROM Locations WHERE id = :locationId")
    fun observeLocationById(locationId: String): Flow<LocationEntity>

    /**
     * Select all locations from the locations table.
     *
     * @return all locations.
     */
    @Query("SELECT * FROM Locations")
    suspend fun getLocations(): List<LocationEntity>

    /**
     * Select a location by id.
     *
     * @param locationId the location id.
     * @return the location with locationId.
     */
    @Query("SELECT * FROM Locations WHERE id = :locationId")
    suspend fun getLocationById(locationId: String): LocationEntity?

    /**
     * Insert a location in the database. If the location already exists, replace it.
     *
     * @param locationEntity the location to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(locationEntity: LocationEntity)

    /**
     * Insert a list of locations in the database. If the location already exists, replace it.
     *
     * @param locationEntity the location to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocations(locationEntities: List<LocationEntity>)

    /**
     * Update a location.
     *
     * @param locationEntity location to be updated
     * @return the number of locations updated. This should always be 1.
     */
    @Update
    suspend fun updateLocation(locationEntity: LocationEntity): Int

    /**
     * Update a list of locations.
     *
     * @param locationEntity location to be updated
     * @return the number of locations updated. This should always be 1.
     */
    @Update
    suspend fun updateLocations(locationEntities: List<LocationEntity>): Int

    /**
     * Delete a location by id.
     *
     * @return the number of locations deleted. This should always be 1.
     */
    @Query("DELETE FROM Locations WHERE id = :locationId")
    suspend fun deleteLocationById(locationId: String): Int

    /**
     * Delete all locations.
     */
    @Query("DELETE FROM Locations")
    suspend fun deleteLocations()

}