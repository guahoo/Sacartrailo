package com.guahoo.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.guahoo.data.db.model.NodeEntity
import com.guahoo.data.db.model.TrackEntity
import com.guahoo.data.db.model.WayEntity

@Dao
interface TrackDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: TrackEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<TrackEntity>)

    @Query("SELECT * FROM tracks WHERE id = :trackId")
    suspend fun getTrackById(trackId: Long): TrackEntity?

    @Update
    suspend fun updateTrack(track: TrackEntity)

    @Query("SELECT * FROM tracks")
    fun getAllTracks(): List<TrackEntity>
}

@Dao
interface WayDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserrtWay(way: WayEntity)

    @Query("SELECT * FROM ways WHERE id = :wayId")
    suspend fun getWayById(wayId: Long): WayEntity?
}

@Dao
interface NodeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNode(node: NodeEntity)

    @Query("SELECT * FROM nodes WHERE id = :nodeId")
    suspend fun getNodeById(nodeId: Long): NodeEntity?
}
