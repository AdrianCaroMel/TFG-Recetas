package es.tfg.recetas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import es.tfg.recetas.ui.AppNavHost
import es.tfg.recetas.ui.AppViewModel
import es.tfg.recetas.ui.theme.RecetasTheme

class MainActivity : ComponentActivity() {

    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RecetasTheme {
                AppNavHost(viewModel)
            }
        }
    }
}
