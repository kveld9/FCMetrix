package com.kveld9.fcmetrix.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kveld9.fcmetrix.data.local.converter.LineupConverters
import com.kveld9.fcmetrix.data.local.dao.LineupDao
import com.kveld9.fcmetrix.data.local.entity.TeamEntity

@Database(entities = [TeamEntity::class], version = 1, exportSchema = false)
@TypeConverters(LineupConverters::class)
abstract class LineupDatabase : RoomDatabase() {
    abstract fun lineupDao(): LineupDao

    companion object {
        @Volatile
        private var INSTANCE: LineupDatabase? = null

        fun getDatabase(context: Context): LineupDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LineupDatabase::class.java,
                    "lineup_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
