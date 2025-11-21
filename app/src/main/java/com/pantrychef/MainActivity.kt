package com.pantrychef

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import com.pantrychef.front.navigation.NavGraph
import com.pantrychef.front.theme.PantryChefTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PantryChefTheme {
                Surface {
                    val navController = rememberNavController()
                    NavGraph(
                        navController = navController,
                        startDestination = com.pantrychef.front.navigation.Routes.LOGIN
                    )
                }
            }
        }
    }
}