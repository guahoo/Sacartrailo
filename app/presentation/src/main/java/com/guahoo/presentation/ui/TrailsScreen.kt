package com.guahoo.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.guahoo.domain.commons.ResultState
import com.guahoo.domain.entity.Node
import com.guahoo.domain.entity.Track
import com.guahoo.presentation.ui.base.BaseScreen.Companion.ErrorScreen
import com.guahoo.presentation.ui.base.BaseScreen.Companion.InitialScreen
import com.guahoo.presentation.ui.base.BaseScreen.Companion.LoadingScreen
import com.guahoo.presentation.ui.base.BaseScreen.Companion.TrackMapView

@Composable
fun WeatherForecastScreen(viewModel: TrailsViewModel) {

    val trackState by viewModel.tracksState.collectAsState()


    val systemUiController = rememberSystemUiController()
    val statusBarColor = Color(0xFF156603)

    LaunchedEffect(systemUiController) {
        viewModel.fetchTracks()
        systemUiController.setStatusBarColor(
            color = statusBarColor,
            darkIcons = false
        )
    }

    val nodeTracks = remember { mutableListOf<Track>() }

    if (trackState is ResultState.Success) {
        val successData = (trackState as ResultState.Success<List<Track>>).data
        nodeTracks.addAll(successData)
    }


    when (trackState) {
        is ResultState.PreAction -> InitialScreen()
        is ResultState.Loading -> LoadingScreen()
        is ResultState.Success -> SuccessScreen(nodeTracks)
        is ResultState.Error -> ErrorScreen((trackState as ResultState.Error).message)
    }
}

@Composable
fun SuccessScreen(nodeTracks: MutableList<Track>) {
    Box(modifier = Modifier.fillMaxSize()){
        nodeTracks.add(
            Track(
            nodes = listOf(
                Node(
                id = 78969696996969,
                lat = 42.44672,
                lon = 43.07219
            ),

                Node(
                    id = 78969696996980,
                    lat = 42.44672,
                    lon = 43.07219
                ),

                Node(
                    id = 78969696996971,
                    lat = 42.46102,
                    lon = 43.12787
                ),
                Node(
                    id = 78969696996970,
                    lat = 42.42355,
                    lon = 43.16510
                ),



                ),
                groupId = 89162543084
        )
        )
        TrackMapView(modifier = Modifier.fillMaxSize(), tracks = nodeTracks)
    }

}




