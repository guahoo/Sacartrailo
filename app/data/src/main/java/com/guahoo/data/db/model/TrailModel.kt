package com.guahoo.data.db.model

import com.guahoo.domain.R
import com.guahoo.domain.entity.NODE
import com.guahoo.domain.entity.WAY
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "tracks")

data class TrackEntity(
    val id: Long = 0L,
    @PrimaryKey(autoGenerate = true)
    val dbid: Long=0L,
    val groupId: Long,
    @TypeConverters(NodeConverter::class) val nodes: List<NodeEntity>? = null,
    @TypeConverters(TagConverter::class) val tags: Map<String,String>? = mapOf(),
    val color: Int? = R.color.black,
)

@Entity(tableName = "ways")
data class WayEntity(
    @PrimaryKey val id: Long = 0L,
    val type: String = WAY,
    @TypeConverters(LongListConverter::class) val nodes: List<Long>?,
    @TypeConverters(TagConverter::class) val tags: Map<String, String>?
)

@Entity(tableName = "nodes")
data class NodeEntity(
    val id: Long,
    @PrimaryKey(autoGenerate = true)
    val dbid: Long=0L,
    val type: String = NODE,
    val lat: Double,
    val lon: Double
)
