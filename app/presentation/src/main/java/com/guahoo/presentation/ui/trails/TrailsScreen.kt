package com.guahoo.presentation.ui.trails

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.guahoo.app.presentation.BuildConfig
import com.guahoo.data.network.L
import com.guahoo.domain.commons.ResultState
import com.guahoo.domain.entity.Track
import com.guahoo.presentation.ui.base.BaseScreen
import com.guahoo.presentation.ui.elements.DownloadingTracksButton
import com.guahoo.presentation.ui.elements.TrackMapView



class TrailsScreen: BaseScreen() {
    @Composable
    fun InitTrackScreen(viewModel: TrailsViewModel) {
        val trackState by viewModel.state.collectAsState()

        val systemUiController = rememberSystemUiController()
        val statusBarColor = Color(0xFF6C873D)
        val nodeTracks = remember { mutableListOf<Track>() }

        LaunchedEffect(systemUiController) {
            viewModel.processIntent(TrailsViewModel.Intent.FetchTracks)

            systemUiController.setStatusBarColor(
                color = statusBarColor,
                darkIcons = false
            )
        }


        if (trackState.tracks.isNotEmpty()) {
            val successData = (trackState as ResultState.Success<List<Track>>).data
            nodeTracks.clear()
            nodeTracks.addAll(successData)
        }

        if (trackState.isLoading) {
            LoadingScreen("Loading")
        }

        if (trackState.errorMessage != null) {
            ErrorScreen(trackState.errorMessage ?: "")
        }

    }

    @Composable
    fun SuccessScreen(nodeTracks: MutableList<Track>, viewModel: TrailsViewModel) {
        Box(modifier = Modifier.fillMaxSize()) {
            L.d("getTracksFromDBcount ${nodeTracks.size}")
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "App Version: ${BuildConfig.VERSION_NAME}",
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp) // Adjust padding as needed
                )
            }
            TrackMapView(modifier = Modifier.fillMaxSize(), tracks = nodeTracks)

            DownloadingTracksButton(
                onClick = {
                    viewModel.processIntent(TrailsViewModel.Intent.ResetPreferences)
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            )
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun SuccessScreenPreview() {

        val mockTracks = listOf<Track>()


        TrailsScreen().SuccessScreen(
            nodeTracks = mockTracks.toMutableList(),
            viewModel = viewModel()
        )
    }
}




