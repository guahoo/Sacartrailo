package com.guahoo.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.guahoo.data.BuildConfig
import com.guahoo.presentation.ui.MainScreen
import com.guahoo.presentation.ui.TrailsViewModel
import com.guahoo.presentation.ui.theme.ComposeApplicationTheme
import dagger.hilt.android.AndroidEntryPoint
import org.osmdroid.config.Configuration

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val weatherViewModel: TrailsViewModel by viewModels()
        Configuration.getInstance().userAgentValue = BuildConfig.LIBRARY_PACKAGE_NAME + "/" + BuildConfig.VERSION_NAME
        setContent {
            ComposeApplicationTheme {
                MainScreen(weatherViewModel)
            }
        }
    }
}