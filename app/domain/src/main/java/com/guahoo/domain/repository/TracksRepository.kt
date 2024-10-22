package com.guahoo.domain.repository

import com.guahoo.domain.commons.ResultState
import com.guahoo.domain.entity.Track
import kotlinx.coroutines.flow.Flow

interface TracksRepository {
    fun fetchTracks(): Flow<ResultState<List<Track>>>
    fun getAllTracks(): List<Track>
    suspend fun insertTrackOrUpdateTrack(track: Track)
}