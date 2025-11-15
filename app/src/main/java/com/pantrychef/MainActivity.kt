package com.pantrychef

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pantrychef.front.theme.PantryChefTheme
import dagger.hilt.android.AndroidEntryPoint // <-- 1. Import Hilt

@AndroidEntryPoint // <-- 2. Añade la anotación
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge() // Esto está bien, lo mantenemos

        setContent {
            PantryChefTheme {
                // Aquí es donde irá tu NavGraph (Paso 7/8)
                // Por ahora lo dejamos vacío, listo para la app.
            }
        }
    }
}


