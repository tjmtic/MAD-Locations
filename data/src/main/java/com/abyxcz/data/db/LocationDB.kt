package com.abyxcz.data.db

import com.abyxcz.data.entity.LocationEntity
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.abyxcz.data.entity.Converters
import com.abyxcz.data.entity.ListTypeConverter
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory


@Database(
    entities = [LocationEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class, ListTypeConverter::class)
abstract class ItemDB : RoomDatabase() {
    abstract fun LocationDao(): LocationDao

    companion object {
        @Volatile
        private var INSTANCE: ItemDB? = null
        fun getInstance(context: Context, password: String): ItemDB {
            val path = context.getDatabasePath("locations").absolutePath
            return INSTANCE ?: synchronized(this) {
                val supportFactory = SupportFactory(SQLiteDatabase.getBytes(password.toCharArray()))
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ItemDB::class.java,
                    path,
                )
                    .openHelperFactory(supportFactory)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}