package com.guahoo.presentation.ui

import androidx.lifecycle.ViewModel
import com.guahoo.data.preferenses.PreferencesService
import com.guahoo.domain.commons.ResultState
import com.guahoo.domain.entity.Track
import com.guahoo.domain.usecase.GetTracksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
open class TrailsViewModel @Inject constructor(
    private val getTracksUseCase: GetTracksUseCase,
    private val preferencesService: PreferencesService
) : ViewModel() {

    private val _tracksState = MutableStateFlow<ResultState<List<Track>>>(ResultState.PreAction)
    val tracksState: StateFlow<ResultState<List<Track>>> = _tracksState


    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        println("Exception caught: $exception ${exception.stackTraceToString()} ${exception.message} ${exception.cause}")
    }

    private val trackScope = CoroutineScope(Dispatchers.IO + SupervisorJob() + exceptionHandler)



    fun fetchTracks(resetPrefs: Boolean = false){
        trackScope.launch {
            if (resetPrefs) preferencesService.trackIsDownloaded = null
            getTracksUseCase.invoke().collect { tracksData ->
                    _tracksState.emit(tracksData)
            }
        }
    }
}