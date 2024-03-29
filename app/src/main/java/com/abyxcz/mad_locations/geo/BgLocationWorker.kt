package com.abyxcz.mad_locations.geo

/*
 * Copyright 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

class BgLocationWorker(context: Context, param: WorkerParameters) :
    CoroutineWorker(context, param) {
    companion object {
        // unique name for the work
        val workName = "BgLocationWorker"
        private const val TAG = "BackgroundLocationWork"
    }

    private val locationClient = LocationServices.getFusedLocationProviderClient(context)

    override suspend fun doWork(): Result {
        Log.d(TAG,"DOIGN WORK TO FIND CURRENT LOCATION 1")
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG,"DOIGN WORK TO FIND CURRENT LOCATION 2")

            return Result.failure()
        }
        locationClient.getCurrentLocation(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY, CancellationTokenSource().token,
        ).addOnSuccessListener { location ->
            Log.d(TAG,"DOIGN WORK TO FIND CURRENT LOCATION 3")

            location?.let {
                Log.d(TAG,"DOIGN WORK TO FIND CURRENT LOCATION 4")

                Log.d(
                    TAG,
                    "Current Location = [lat : ${location.latitude}, lng : ${location.longitude}]",
                )
            }
        }
        Log.d(TAG,"DOIGN WORK TO FIND CURRENT LOCATION 5")

        return Result.success()
    }
}