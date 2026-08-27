package com.ecoroute

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.ecoroute.navigation.AppNavigation
import com.ecoroute.ui.theme.EcoRouteTheme
import com.google.android.libraries.places.api.Places

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, BuildConfig.MAPS_API_KEY)
        }

        setContent {
            EcoRouteTheme {
                AppNavigation()
            }
        }
    }
}
