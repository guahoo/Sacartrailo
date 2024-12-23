package com.guahoo.presentation.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.guahoo.presentation.ui.trails.TrailsScreen
import com.guahoo.presentation.ui.trails.TrailsViewModel


@Composable
fun MainScreen(viewModel: TrailsViewModel) {
    val navController = rememberNavController()

    Scaffold() { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "forecast",
            modifier = Modifier.padding(innerPadding)
        ){
            composable("forecast") { TrailsScreen().InitTrackScreen(viewModel) }
        }
    }
}
