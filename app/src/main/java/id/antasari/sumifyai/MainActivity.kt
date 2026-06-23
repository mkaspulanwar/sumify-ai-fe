package id.antasari.sumifyai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import id.antasari.sumifyai.ui.navigation.AppNavigation
import id.antasari.sumifyai.ui.navigation.Routes
import id.antasari.sumifyai.ui.theme.SumifyaiTheme
import id.antasari.sumifyai.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SumifyaiTheme {
                // Surface wraps the app content to prevent transition flashes (spalsh glitches)
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val hasSeenWelcome by mainViewModel.hasSeenWelcome.collectAsState()

                    if (hasSeenWelcome == null) {
                        Box(modifier = Modifier.fillMaxSize())
                    } else {
                        AppNavigation(
                            navController = navController,
                            viewModel = mainViewModel,
                            startDestination = if (hasSeenWelcome == true) {
                                Routes.DASHBOARD
                            } else {
                                Routes.WELCOME
                            }
                        )
                    }
                }
            }
        }
    }
}
