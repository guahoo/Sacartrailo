package com.guahoo.presentation.ui.elements

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.guahoo.app.presentation.R

@Composable
fun DownloadingTracksButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        shape = CircleShape,
        containerColor = Color(0xFFFFEA00),
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.download_tracks_back),
            contentDescription = "Add",
            tint = Color.Unspecified,
        )
    }
}
