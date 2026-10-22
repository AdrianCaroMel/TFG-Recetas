package es.tfg.recetas.ui.recipe

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import es.tfg.recetas.core.model.Catalog
import es.tfg.recetas.core.model.Recipe
import es.tfg.recetas.core.model.UserProfile
import es.tfg.recetas.core.planner.PriceResolver
import es.tfg.recetas.ui.common.formatEuros
import es.tfg.recetas.ui.common.formatQuantity

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RecipeDetailScreen(
    recipe: Recipe,
    catalog: Catalog,
    profile: UserProfile?,
    onBack: () -> Unit,
) {
    val people = profile?.people ?: recipe.servings
    val factor = people.toDouble() / recipe.servings
    val estimatedCost = profile?.let {
        val resolver = PriceResolver(catalog, it.supermarkets)
        if (resolver.isAvailable(recipe)) resolver.estimatedRecipeCostCents(recipe, people) else null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(recipe.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Text(recipe.description, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(12.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, label = { Text("${recipe.prepMinutes} min") })
                AssistChip(onClick = {}, label = { Text("$people ${if (people == 1) "ración" else "raciones"}") })
                if (estimatedCost != null) {
                    AssistChip(onClick = {}, label = { Text("≈ ${formatEuros(estimatedCost / people)} / ración") })
                }
            }
            val allergens = catalog.allergensOf(recipe)
            if (allergens.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Alérgenos: " + allergens.sortedBy { it.ordinal }.joinToString { it.displayName.lowercase() },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(24.dp))
            Text("Ingredientes", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            recipe.ingredients.forEach { ri ->
                val ingredient = catalog.ingredient(ri.ingredientId)
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text("•", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    Text(ingredient.name, modifier = Modifier.weight(1f))
                    Text(formatQuantity(ri.quantity * factor, ingredient.unit), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("Preparación", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            recipe.steps.forEachIndexed { index, step ->
                Row(Modifier.padding(vertical = 6.dp)) {
                    Text("${index + 1}.", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text(step, style = MaterialTheme.typography.bodyLarge)
                }
                if (index < recipe.steps.lastIndex) HorizontalDivider()
            }
        }
    }
}
