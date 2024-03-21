package com.abyxcz.mad_locations.di

import android.content.Context
import com.abyxcz.mad_locations.DefaultLocationClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object FrameworkModule {
    @Provides
    fun provideDefaultLocationClient(@ApplicationContext context: Context): DefaultLocationClient {
        return DefaultLocationClient(context, LocationServices.getFusedLocationProviderClient(context))
    }
}