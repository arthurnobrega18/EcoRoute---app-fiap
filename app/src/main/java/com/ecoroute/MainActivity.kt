package com.ecoroute

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.ecoroute.navigation.AppNavigation
import com.ecoroute.ui.theme.EcoRouteTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            EcoRouteTheme {
                AppNavigation()
            }
        }
    }
}
