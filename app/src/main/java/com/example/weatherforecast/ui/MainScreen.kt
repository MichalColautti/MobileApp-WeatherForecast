package com.example.weatherforecast.ui

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import com.example.weatherforecast.layout.PhoneScreen
import com.example.weatherforecast.layout.TabletLayout

@Composable
fun MainScreen() {
    val isTablet = isTablet()

    if (isTablet) {
        TabletLayout()
    } else {
        PhoneScreen()
    }
}


@Composable
fun isTablet(): Boolean {
    val configuration = LocalConfiguration.current
    return if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        configuration.screenWidthDp > 840 && configuration.screenHeightDp > 600
    } else {
        configuration.screenWidthDp > 600 && configuration.screenHeightDp > 600
    }
}