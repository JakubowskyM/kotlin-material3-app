package pl.stud.ur.edu.mj131446.spotly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import pl.stud.ur.edu.mj131446.spotly.ui.theme.SpotlyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SpotlyTheme {
                val repo = PlaceRepository(applicationContext)
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "list"
                ) {
                    // Lista miejsc
                    composable("list") {
                        PlacesListScreen(
                            repo = repo,
                            onPlaceClicked = { place ->
                                navController.navigate("detail/${place.id}")
                            },
                            onAddClicked = { navController.navigate("add") },
                            onInfoClicked = { navController.navigate("about") }
                        )
                    }

                    // Dodawanie miejsca
                    composable("add") {
                        AddPlaceScreen(
                            repo = repo,
                            onSaved = { navController.popBackStack() },
                            onCancel = { navController.popBackStack() }
                        )
                    }

                    // Szczegóły miejsca
                    composable(
                        "detail/{placeId}",
                        arguments = listOf(navArgument("placeId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val placeId = backStackEntry.arguments?.getString("placeId") ?: ""
                        val place = repo.getPlace(placeId) ?: Place.default()

                        PlaceDetailScreen(
                            place = place,
                            repo = repo,
                            onBack = { navController.popBackStack() },
                            context = applicationContext
                        )
                    }

                    // Ekran informacji / About
                    composable("about") {
                        AboutScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
