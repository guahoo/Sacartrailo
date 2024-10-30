package com.guahoo.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.guahoo.app.presentation.R
import com.guahoo.domain.commons.ResultState
import com.guahoo.domain.entity.Track
import com.guahoo.presentation.ui.base.BaseScreen.Companion.ErrorScreen
import com.guahoo.presentation.ui.base.BaseScreen.Companion.InitialScreen
import com.guahoo.presentation.ui.base.BaseScreen.Companion.LoadingScreen
import com.guahoo.presentation.ui.base.BaseScreen.Companion.TrackMapView

@Composable
fun InitTrackScreen(viewModel: TrailsViewModel) {

    val trackState by viewModel.tracksState.collectAsState()

    val systemUiController = rememberSystemUiController()
    val statusBarColor = Color(0xFF156603)
    val nodeTracks = remember { mutableListOf<Track>() }

    LaunchedEffect(systemUiController) {
        viewModel.fetchTracks()

        systemUiController.setStatusBarColor(
            color = statusBarColor,
            darkIcons = false
        )
    }



    if (trackState is ResultState.Success) {
        val successData = (trackState as ResultState.Success<List<Track>>).data
        nodeTracks.addAll(successData)
    }


    when (trackState) {
        is ResultState.PreAction -> InitialScreen()
        is ResultState.Loading -> LoadingScreen((trackState as ResultState.Loading).message)
        is ResultState.Success -> SuccessScreen(nodeTracks, viewModel)
        is ResultState.Error -> ErrorScreen((trackState as ResultState.Error).message)
    }
}

@Composable
fun SuccessScreen(nodeTracks: MutableList<Track>, viewModel: TrailsViewModel) {
    Box(modifier = Modifier.fillMaxSize()) {
        TrackMapView(modifier = Modifier.fillMaxSize(), tracks = nodeTracks)

        DownloadingTracksButton(
            onClick = { viewModel.fetchTracks(resetPrefs = true) },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        )

    }


}

@Composable
fun DownloadingTracksButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        shape = CircleShape,
        containerColor = Color(0xFF69A655),
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.download_tracks_back),
            contentDescription = "Add",
            tint = Color.Unspecified,
        )
    }
}




