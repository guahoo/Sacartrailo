package com.guahoo.domain.usecase

import com.guahoo.domain.commons.ResultState
import com.guahoo.domain.entity.Track
import com.guahoo.domain.repository.TracksRepository
import kotlinx.coroutines.flow.Flow

class GetTracksUseCase(private val tracksRepository: TracksRepository) {

    operator fun invoke(): Flow<ResultState<List<Track>>>{
         return tracksRepository.fetchTracks()
    }
}