package es.tfg.recetas.ui.onboarding

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import es.tfg.recetas.core.model.Allergen
import es.tfg.recetas.core.model.Diet
import es.tfg.recetas.ui.ProfileDraft

private fun Diet.explanation(): String = when (this) {
    Diet.OMNIVORE -> "Como de todo"
    Diet.PESCATARIAN -> "Sin carne, pero sí pescado"
    Diet.VEGETARIAN -> "Sin carne ni pescado"
    Diet.VEGAN -> "Ningún producto de origen animal"
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RestrictionsScreen(
    draft: ProfileDraft,
    onUpdate: ((ProfileDraft) -> ProfileDraft) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
) {
    OnboardingScaffold(
        step = 2,
        totalSteps = 3,
        title = "Tus restricciones alimentarias",
        subtitle = "Solo te propondremos recetas que puedas comer.",
        nextLabel = "Siguiente",
        nextEnabled = true,
        onNext = onNext,
        onBack = onBack,
    ) {
        SectionTitle("Dieta")
        Diet.entries.forEach { diet ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onUpdate { it.copy(diet = diet) } },
            ) {
                RadioButton(selected = draft.diet == diet, onClick = { onUpdate { it.copy(diet = diet) } })
                Column {
                    Text(diet.displayName, style = MaterialTheme.typography.bodyLarge)
                    Text(diet.explanation(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        SectionTitle("Alergias")
        Text(
            "Marca los alérgenos que debes evitar (los 14 de declaración obligatoria en la UE).",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Allergen.entries.forEach { allergen ->
                val selected = allergen in draft.allergens
                FilterChip(
                    selected = selected,
                    onClick = {
                        onUpdate { d -> d.copy(allergens = if (selected) d.allergens - allergen else d.allergens + allergen) }
                    },
                    label = { Text(allergen.displayName) },
                    leadingIcon = if (selected) {
                        { Icon(Icons.Filled.Check, contentDescription = null) }
                    } else {
                        null
                    },
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        SectionTitle("Otras restricciones")
        SwitchRow("Intolerancia a la lactosa", draft.lactoseIntolerant) { v -> onUpdate { it.copy(lactoseIntolerant = v) } }
        SwitchRow("Sin cerdo", draft.avoidPork) { v -> onUpdate { it.copy(avoidPork = v) } }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 4.dp))
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onChange(!checked) }
            .padding(vertical = 4.dp),
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
