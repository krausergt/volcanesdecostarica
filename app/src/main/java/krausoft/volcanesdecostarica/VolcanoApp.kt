package krausoft.volcanesdecostarica

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import krausoft.volcanesdecostarica.ui.about.AboutScreen
import krausoft.volcanesdecostarica.ui.camera.CameraScreen
import krausoft.volcanesdecostarica.ui.home.HomeScreen

/**
 * Raíz de la navegación de la app: define el grafo con la pantalla principal
 * (lista de cámaras) y el visor de una cámara concreta.
 */
@Composable
fun VolcanoApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onCameraClick = { id -> navController.navigate(Routes.camera(id)) },
                onAboutClick = { navController.navigate(Routes.ABOUT) },
            )
        }
        composable(Routes.ABOUT) {
            AboutScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Routes.CAMERA,
            arguments = listOf(navArgument(Routes.ARG_CAMERA_ID) { type = NavType.StringType }),
        ) { entry ->
            val id = entry.arguments?.getString(Routes.ARG_CAMERA_ID).orEmpty()
            CameraScreen(cameraId = id, onBack = { navController.popBackStack() })
        }
    }
}

/** Rutas de navegación y helpers para construir rutas parametrizadas. */
private object Routes {
    const val HOME = "home"
    const val ABOUT = "about"
    const val ARG_CAMERA_ID = "cameraId"
    const val CAMERA = "camera/{$ARG_CAMERA_ID}"

    fun camera(id: String): String = "camera/$id"
}
