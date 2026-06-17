package com.civictracker.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.civictracker.app.ui.navigation.NavGraph
import com.civictracker.app.ui.theme.CivicTrackerTheme
import dagger.hilt.android.AndroidEntryPoint
import org.osmdroid.config.Configuration
import androidx.preference.PreferenceManager

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize OSMDroid Configuration using the correct PreferenceManager
        Configuration.getInstance().load(
            applicationContext,
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )
        // User agent is required by OSMDroid to identify the app to tile servers
        Configuration.getInstance().userAgentValue = packageName

        enableEdgeToEdge()
        setContent {
            CivicTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(navController = navController)
                }
            }
        }
    }
}
