package com.abyxcz.data.di

import android.app.Application
import androidx.room.Room
import com.abyxcz.data.dataSource.LocationDataSource
import com.abyxcz.data.db.LocationDB
import com.abyxcz.data.db.LocationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DataSourceModule {
    @Provides
    fun provideDatabase(app: Application): LocationDB =
        Room.databaseBuilder(app, LocationDB::class.java, "location_db").fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideLocationDao(locationDB: LocationDB) : LocationDao = locationDB.LocationDao()

    @Provides
    fun providesLocationDataSource(locationDao: LocationDao) : LocationDataSource = LocationDataSource(locationDao)

}