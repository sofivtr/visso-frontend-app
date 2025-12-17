package cl.duoc.visso

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import cl.duoc.visso.ui.navigation.NavGraph
import cl.duoc.visso.ui.navigation.Screen
import cl.duoc.visso.ui.theme.VissoAppTheme
import cl.duoc.visso.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            VissoAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val scope = rememberCoroutineScope()

                    var startDestination by remember { mutableStateOf<String?>(null) }

                    LaunchedEffect(Unit) {
                        scope.launch {
                            val isLoggedIn = sessionManager.isLoggedIn.first()
                            val userRole = sessionManager.userRole.first()

                            startDestination = if (isLoggedIn) {
                                // Redirigir según el rol del usuario
                                if (userRole == "ADMIN") {
                                    Screen.AdminHome.route
                                } else {
                                    Screen.Home.route
                                }
                            } else {
                                Screen.Login.route
                            }
                        }
                    }

                    startDestination?.let { destination ->
                        NavGraph(
                            navController = navController,
                            startDestination = destination
                        )
                    }
                }
            }
        }
    }
}