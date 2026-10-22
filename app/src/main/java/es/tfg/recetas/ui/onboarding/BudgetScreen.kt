package es.tfg.recetas.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import es.tfg.recetas.core.model.MealType
import es.tfg.recetas.ui.MinimumEstimate
import es.tfg.recetas.ui.ProfileDraft
import es.tfg.recetas.ui.common.formatEuros

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BudgetScreen(
    draft: ProfileDraft,
    minimumEstimate: MinimumEstimate,
    onUpdate: ((ProfileDraft) -> ProfileDraft) -> Unit,
    onFinish: () -> Unit,
    onBack: () -> Unit,
) {
    val budgetCents = draft.budgetCents
    OnboardingScaffold(
        step = 3,
        totalSteps = 3,
        title = "Tu presupuesto",
        subtitle = "¿Cuánto puedes gastar al mes en comida y para cuántas personas cocinas?",
        nextLabel = "Generar mi menú",
        nextEnabled = budgetCents != null && draft.meals.isNotEmpty(),
        onNext = onFinish,
        onBack = onBack,
    ) {
        OutlinedTextField(
            value = draft.budgetText,
            onValueChange = { text ->
                val clean = text.filter { it.isDigit() || it == ',' || it == '.' }.take(8)
                onUpdate { it.copy(budgetText = clean) }
            },
            label = { Text("Presupuesto mensual") },
            suffix = { Text("€") },
            singleLine = true,
            isError = draft.budgetText.isNotEmpty() && budgetCents == null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(24.dp))
        Text("Personas", style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            FilledTonalButton(
                onClick = { onUpdate { it.copy(people = (it.people - 1).coerceAtLeast(1)) } },
                enabled = draft.people > 1,
            ) { Text("−") }
            Text(
                "${draft.people}",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
            FilledTonalButton(
                onClick = { onUpdate { it.copy(people = (it.people + 1).coerceAtMost(12)) } },
                enabled = draft.people < 12,
            ) { Text("+") }
        }

        Spacer(Modifier.height(24.dp))
        Text("¿Qué comidas quieres planificar?", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MealType.entries.forEach { meal ->
                val selected = meal in draft.meals
                FilterChip(
                    selected = selected,
                    onClick = { onUpdate { d -> d.copy(meals = if (selected) d.meals - meal else d.meals + meal) } },
                    label = { Text(meal.displayName) },
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        EstimateCard(minimumEstimate, budgetCents)
    }
}

@Composable
private fun EstimateCard(estimate: MinimumEstimate, budgetCents: Int?) {
    val minimum = (estimate as? MinimumEstimate.Value)?.cents
    val warning = estimate is MinimumEstimate.NoRecipes ||
        (minimum != null && budgetCents != null && budgetCents < minimum)
    val colors = CardDefaults.cardColors(
        containerColor = if (warning) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer,
    )
    val message = when {
        estimate is MinimumEstimate.Calculating -> "Calculando el gasto mínimo para tus preferencias…"
        estimate is MinimumEstimate.NoRecipes ->
            "No tenemos recetas para todas las comidas elegidas con tus restricciones. " +
                "Prueba a quitar alguna comida o restricción."
        minimum == null -> ""
        budgetCents == null ->
            "Con tus preferencias, un mes de menú variado cuesta desde unos ${formatEuros(minimum)}."
        budgetCents >= minimum ->
            "¡Bien! Tu presupuesto cubre el mínimo estimado (${formatEuros(minimum)})."
        else ->
            "Ojo: con tus preferencias el mínimo estimado es ${formatEuros(minimum)}. " +
                "Intentaremos ajustarnos, pero puede que no sea posible."
    }
    Card(colors = colors, modifier = Modifier.fillMaxWidth()) {
        Text(message, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(16.dp))
    }
}
