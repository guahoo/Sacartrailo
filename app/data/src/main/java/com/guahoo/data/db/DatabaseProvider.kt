package com.guahoo.data.db

import android.content.Context
import androidx.room.Room
import com.guahoo.data.db.dao.NodeDao
import com.guahoo.data.db.dao.TrackDao
import com.guahoo.data.db.dao.WayDao

object DatabaseProvider {
    private var instance: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return instance ?: synchronized(this) {
            val tempInstance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "tracks_database"
            ).build()
            instance = tempInstance
            tempInstance
        }
    }

    // Provide the TrackDao from the AppDatabase instance
    fun getTrackDao(context: Context): TrackDao {
        return getDatabase(context).trackDao()
    }

    // Similarly, provide other DAOs if needed
    fun getWayDao(context: Context): WayDao {
        return getDatabase(context).wayDao()
    }

    fun getNodeDao(context: Context): NodeDao {
        return getDatabase(context).nodeDao()
    }
}
