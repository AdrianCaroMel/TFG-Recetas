package es.tfg.recetas.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import es.tfg.recetas.ui.onboarding.BudgetScreen
import es.tfg.recetas.ui.onboarding.RestrictionsScreen
import es.tfg.recetas.ui.onboarding.SupermarketsScreen
import es.tfg.recetas.ui.onboarding.WelcomeScreen
import es.tfg.recetas.ui.plan.PlanScreen
import es.tfg.recetas.ui.recipe.RecipeDetailScreen

private object Routes {
    const val WELCOME = "bienvenida"
    const val SUPERMARKETS = "supermercados"
    const val RESTRICTIONS = "restricciones"
    const val BUDGET = "presupuesto"
    const val PLAN = "menu"
    const val RECIPE = "receta/{id}"
    fun recipe(id: String) = "receta/$id"
}

@Composable
fun AppNavHost(viewModel: AppViewModel) {
    val startState by viewModel.startState.collectAsStateWithLifecycle()
    when (startState) {
        StartState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        StartState.NeedsOnboarding -> AppNavigation(viewModel, startDestination = Routes.WELCOME)
        StartState.HasPlan -> AppNavigation(viewModel, startDestination = Routes.PLAN)
    }
}

@Composable
private fun AppNavigation(viewModel: AppViewModel, startDestination: String) {
    val navController = rememberNavController()
    val draft by viewModel.draft.collectAsStateWithLifecycle()

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.WELCOME) {
            WelcomeScreen(onStart = { navController.navigate(Routes.SUPERMARKETS) })
        }
        composable(Routes.SUPERMARKETS) {
            SupermarketsScreen(
                draft = draft,
                onToggle = { market ->
                    viewModel.updateDraft { d ->
                        d.copy(supermarkets = if (market in d.supermarkets) d.supermarkets - market else d.supermarkets + market)
                    }
                },
                onNext = { navController.navigate(Routes.RESTRICTIONS) },
                onBack = navController.backAction(),
            )
        }
        composable(Routes.RESTRICTIONS) {
            RestrictionsScreen(
                draft = draft,
                onUpdate = viewModel::updateDraft,
                onNext = { navController.navigate(Routes.BUDGET) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.BUDGET) {
            val estimate by viewModel.minimumEstimate.collectAsStateWithLifecycle()
            BudgetScreen(
                draft = draft,
                minimumEstimate = estimate,
                onUpdate = viewModel::updateDraft,
                onFinish = {
                    if (viewModel.confirmProfile()) {
                        navController.navigate(Routes.PLAN) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.PLAN) {
            val planState by viewModel.planState.collectAsStateWithLifecycle()
            val bought by viewModel.boughtIngredients.collectAsStateWithLifecycle()
            PlanScreen(
                state = planState,
                boughtIngredients = bought,
                onRegenerate = viewModel::regenerate,
                onEditPreferences = { navController.navigate(Routes.SUPERMARKETS) },
                onOpenRecipe = { id -> navController.navigate(Routes.recipe(id)) },
                onToggleBought = viewModel::toggleBought,
            )
        }
        composable(Routes.RECIPE) { entry ->
            val recipe = entry.arguments?.getString("id")?.let { viewModel.catalog.recipe(it) }
            if (recipe == null) {
                LaunchedEffect(Unit) { navController.popBackStack() }
            } else {
                RecipeDetailScreen(
                    recipe = recipe,
                    catalog = viewModel.catalog,
                    profile = viewModel.currentProfile(),
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}

private fun NavHostController.backAction(): (() -> Unit)? =
    if (previousBackStackEntry != null) ({ popBackStack() }) else null
