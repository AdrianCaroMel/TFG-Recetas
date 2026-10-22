package es.tfg.recetas.ui.onboarding

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import es.tfg.recetas.core.model.Supermarket
import es.tfg.recetas.ui.ProfileDraft

@Composable
fun SupermarketsScreen(
    draft: ProfileDraft,
    onToggle: (Supermarket) -> Unit,
    onNext: () -> Unit,
    onBack: (() -> Unit)?,
) {
    OnboardingScaffold(
        step = 1,
        totalSteps = 3,
        title = "¿Dónde puedes comprar?",
        subtitle = "Elige los supermercados que tienes cerca o en los que estás dispuesto a comprar. " +
            "Cuantos más elijas, más barato podremos ajustar tu menú.",
        nextLabel = "Siguiente",
        nextEnabled = draft.supermarkets.isNotEmpty(),
        onNext = onNext,
        onBack = onBack,
    ) {
        Supermarket.entries.forEach { market ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle(market) }
                    .padding(vertical = 4.dp),
            ) {
                Checkbox(checked = market in draft.supermarkets, onCheckedChange = { onToggle(market) })
                Text(market.displayName, style = MaterialTheme.typography.bodyLarge)
            }
            HorizontalDivider()
        }
    }
}
