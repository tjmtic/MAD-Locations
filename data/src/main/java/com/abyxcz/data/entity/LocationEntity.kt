package com.abyxcz.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true)
    var pk: Long = 0,
    @SerializedName("_id")
    val id: String,
    @SerializedName("id")
    val locationId: String?,
    @SerializedName("provider")
    val provider: String,
    @SerializedName("lat")
    val latitude: Double,
    @SerializedName("lon")
    val longitude: Double,
    @SerializedName("geolocationId")
    val geolocationId: String?,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String?,
    @SerializedName("image")
    val image: String?

)