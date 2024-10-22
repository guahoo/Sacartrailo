package com.guahoo.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.guahoo.data.db.dao.NodeDao
import com.guahoo.data.db.dao.TrackDao
import com.guahoo.data.db.dao.WayDao
import com.guahoo.data.db.model.LongListConverter
import com.guahoo.data.db.model.NodeConverter
import com.guahoo.data.db.model.NodeEntity
import com.guahoo.data.db.model.TagConverter
import com.guahoo.data.db.model.TrackEntity
import com.guahoo.data.db.model.WayEntity

@Database(
    entities = [TrackEntity::class, WayEntity::class, NodeEntity::class],
    version = 1,
    exportSchema = false,
)


@TypeConverters(NodeConverter::class, TagConverter::class, LongListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun wayDao(): WayDao
    abstract fun nodeDao(): NodeDao
}