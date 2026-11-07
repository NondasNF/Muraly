package br.com.mural.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import br.com.mural.feature.calendar.CalendarScreen
import br.com.mural.feature.calendar.CalendarViewModel
import br.com.mural.feature.day.DayScreen
import br.com.mural.feature.day.DayViewModel

@Composable
fun MuralNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.CALENDAR
    ) {
        composable(Routes.CALENDAR) {
            val viewModel: CalendarViewModel = hiltViewModel()
            CalendarScreen(
                viewModel = viewModel,
                onDayClick = { dateKey ->
                    navController.navigate(Routes.day(dateKey))
                }
            )
        }

        composable(
            route = Routes.DAY_PATTERN,
            arguments = listOf(
                navArgument("dateKey") { type = NavType.StringType }
            )
        ) {
            val viewModel: DayViewModel = hiltViewModel()
            DayScreen(
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
