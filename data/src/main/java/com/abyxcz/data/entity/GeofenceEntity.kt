package com.abyxcz.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "geofences")
data class GeofenceEntity(
    @PrimaryKey(autoGenerate = true)
    var pk: Long = 0,
    @SerializedName("_id")
    val id: String,
    @SerializedName("id")
    val geofenceId: String?,
    @SerializedName("createdAt")
    val createdAt: Long,
    @SerializedName("updatedAt")
    val updatedAt: Long,
    @SerializedName("provider")
    val provider: String?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("image")
    val image: String?,
    @SerializedName("key")
    val key: String,
    @SerializedName("radius")
    val radius: Float,
    @SerializedName("expiration")
    val expiration: Long,
    @SerializedName("lat")
    val latitude: Double,
    @SerializedName("lon")
    val longitude: Double,

)