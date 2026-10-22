package es.tfg.recetas.ui.plan

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import es.tfg.recetas.core.model.MealType
import es.tfg.recetas.core.planner.MealPlan
import es.tfg.recetas.core.planner.PlanResult
import es.tfg.recetas.core.planner.PlannedMeal
import es.tfg.recetas.core.planner.ShoppingItem
import es.tfg.recetas.ui.PlanUiState
import es.tfg.recetas.ui.common.formatEuros
import es.tfg.recetas.ui.common.formatQuantity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanScreen(
    state: PlanUiState,
    boughtIngredients: Set<String>,
    onRegenerate: () -> Unit,
    onEditPreferences: () -> Unit,
    onOpenRecipe: (String) -> Unit,
    onToggleBought: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tu menú del mes") },
                actions = {
                    IconButton(onClick = onRegenerate, enabled = state is PlanUiState.Ready) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Generar otro menú")
                    }
                    IconButton(onClick = onEditPreferences) {
                        Icon(Icons.Filled.Settings, contentDescription = "Cambiar preferencias")
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            when (state) {
                PlanUiState.Empty, PlanUiState.Generating -> Loading()
                is PlanUiState.Ready -> when (val result = state.result) {
                    is PlanResult.Success -> PlanContent(result.plan, null, boughtIngredients, onOpenRecipe, onToggleBought)
                    is PlanResult.OverBudget -> PlanContent(
                        plan = result.cheapestPlan,
                        warning = "Con tu presupuesto no llegamos a cubrir el mes. Lo más ajustado que hemos " +
                            "encontrado cuesta ${formatEuros(result.minimumCostCents)}. Prueba a subir el " +
                            "presupuesto, elegir más supermercados o planificar menos comidas.",
                        boughtIngredients = boughtIngredients,
                        onOpenRecipe = onOpenRecipe,
                        onToggleBought = onToggleBought,
                    )
                    is PlanResult.NoRecipes -> NoRecipes(result.mealTypes, onEditPreferences)
                }
            }
        }
    }
}

@Composable
private fun Loading() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(16.dp))
        Text("Preparando tu menú…")
    }
}

@Composable
private fun NoRecipes(mealTypes: Set<MealType>, onEditPreferences: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(16.dp))
        Text(
            "No hemos encontrado recetas compatibles para: " +
                mealTypes.joinToString { it.displayName.lowercase() } + ".",
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onEditPreferences) { Text("Cambiar preferencias") }
    }
}

@Composable
private fun PlanContent(
    plan: MealPlan,
    warning: String?,
    boughtIngredients: Set<String>,
    onOpenRecipe: (String) -> Unit,
    onToggleBought: (String) -> Unit,
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    Column(Modifier.fillMaxSize()) {
        BudgetSummary(plan, warning)
        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Menú") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Lista de la compra") })
        }
        when (tab) {
            0 -> MenuList(plan, onOpenRecipe)
            else -> ShoppingList(plan, boughtIngredients, onToggleBought)
        }
    }
}

@Composable
private fun BudgetSummary(plan: MealPlan, warning: String?) {
    val budget = plan.profile.monthlyBudgetCents
    val colors = if (warning != null) {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    } else {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    }
    Card(colors = colors, modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(formatEuros(plan.totalCostCents), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(" de ${formatEuros(budget)}", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { if (budget == 0) 1f else (plan.totalCostCents.toFloat() / budget).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            val people = plan.profile.people
            Text(
                "${plan.profile.days} días · ${plan.meals.size} platos · ${plan.distinctRecipes} recetas distintas · " +
                    "$people ${if (people == 1) "persona" else "personas"}",
                style = MaterialTheme.typography.bodySmall,
            )
            if (warning != null) {
                Spacer(Modifier.height(8.dp))
                Text(warning, style = MaterialTheme.typography.bodyMedium)
            } else if (plan.remainingBudgetCents > 0) {
                Text("Te sobran ${formatEuros(plan.remainingBudgetCents)} este mes.", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun MenuList(plan: MealPlan, onOpenRecipe: (String) -> Unit) {
    val days = plan.days.entries.toList()
    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
        days.forEach { (day, meals) ->
            if (day % 7 == 0) {
                item(key = "week-$day") {
                    Text(
                        "Semana ${day / 7 + 1}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                    )
                }
            }
            item(key = "day-$day") {
                DayCard(day, meals, onOpenRecipe)
            }
        }
    }
}

@Composable
private fun DayCard(day: Int, meals: List<PlannedMeal>, onOpenRecipe: (String) -> Unit) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 6.dp)) {
        Column(Modifier.padding(vertical = 8.dp)) {
            Text("Día ${day + 1}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp))
            meals.forEach { meal ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenRecipe(meal.recipe.id) }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(meal.mealType.displayName, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(meal.recipe.name, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Text(formatEuros(meal.estimatedCostCents), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun ShoppingList(plan: MealPlan, bought: Set<String>, onToggleBought: (String) -> Unit) {
    val groups = plan.shoppingList.bySupermarket
    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            Text(
                "Cantidades para todo el mes. Los productos frescos conviene comprarlos semana a semana.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
        groups.forEach { (market, items) ->
            item(key = "market-${market.name}") {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
                    ) {
                        Text(market.displayName, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                        Text(formatEuros(items.sumOf { it.purchase.costCents }), style = MaterialTheme.typography.titleMedium)
                    }
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                }
            }
            items(items, key = { "item-${it.ingredient.id}" }) { item ->
                ShoppingRow(item, item.ingredient.id in bought) { onToggleBought(item.ingredient.id) }
            }
        }
    }
}

@Composable
private fun ShoppingRow(item: ShoppingItem, checked: Boolean, onToggle: () -> Unit) {
    val alpha = if (checked) 0.5f else 1f
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Checkbox(checked = checked, onCheckedChange = { onToggle() })
        Column(Modifier.weight(1f)) {
            Text(
                item.ingredient.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
            )
            Text(
                "${item.purchase.packs} × ${item.purchase.product.name} · necesitas ${formatQuantity(item.neededQuantity, item.ingredient.unit)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
            )
        }
        Text(
            formatEuros(item.purchase.costCents),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 8.dp, end = 8.dp),
        )
    }
}
