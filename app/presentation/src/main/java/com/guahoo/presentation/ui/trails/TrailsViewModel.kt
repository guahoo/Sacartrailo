package com.guahoo.presentation.ui.trails

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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrailsViewModel @Inject constructor(
    private val getTracksUseCase: GetTracksUseCase,
    private val preferencesService: PreferencesService
) : ViewModel() {

    sealed class Intent {
        data object FetchTracks : Intent()
        data object ResetPreferences : Intent()
    }

    data class State(
        val tracks: List<Track> = emptyList(),
        val isLoading: Boolean = false,
        val errorMessage: String? = null
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        println("Exception caught: $exception ${exception.stackTraceToString()} ${exception.message} ${exception.cause}")
        _state.update { currentState ->
            currentState.copy(
                isLoading = false,
                errorMessage = exception.message
            )
        }
    }

    private val intentChannel = Channel<Intent>(Channel.UNLIMITED)

    private val trackScope = CoroutineScope(Dispatchers.IO + SupervisorJob() + exceptionHandler)

    init {
        handleIntents()
    }

    private fun handleIntents() {
        trackScope.launch {
            intentChannel.consumeAsFlow().collect { intent ->
                when (intent) {
                    is Intent.FetchTracks -> fetchTracks()
                    is Intent.ResetPreferences -> resetPreferencesAndFetchTracks()
                }
            }
        }
    }

    private fun fetchTracks() {
        trackScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            getTracksUseCase.invoke().collect { tracksData ->
                when (tracksData) {
                    is ResultState.Success -> _state.update { it.copy(isLoading = false, tracks = tracksData.data) }
                    is ResultState.Error -> _state.update { it.copy(isLoading = false, errorMessage = tracksData.message) }
                    else -> _state.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    private fun resetPreferencesAndFetchTracks() {
        trackScope.launch {
            preferencesService.trackIsDownloaded = null
            fetchTracks()
        }
    }

    // Exposed function for UI to send intents
    fun processIntent(intent: Intent) {
        trackScope.launch { intentChannel.send(intent) }
    }
}
