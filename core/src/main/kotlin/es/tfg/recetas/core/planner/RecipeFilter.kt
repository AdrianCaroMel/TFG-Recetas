package es.tfg.recetas.core.planner

import es.tfg.recetas.core.model.Catalog
import es.tfg.recetas.core.model.DietaryProfile
import es.tfg.recetas.core.model.Recipe

/** Decide si una receta es apta para las restricciones alimentarias de un usuario. */
object RecipeFilter {

    fun isCompatible(recipe: Recipe, catalog: Catalog, dietary: DietaryProfile): Boolean =
        incompatibilityReasons(recipe, catalog, dietary).isEmpty()

    /** Motivos (en texto legible) por los que la receta no es apta. Vacío si es apta. */
    fun incompatibilityReasons(recipe: Recipe, catalog: Catalog, dietary: DietaryProfile): List<String> {
        val reasons = mutableListOf<String>()
        val allergens = catalog.allergensOf(recipe) intersect dietary.allergens
        allergens.forEach { reasons += "Contiene ${it.displayName.lowercase()}" }

        val traits = catalog.traitsOf(recipe) intersect dietary.forbiddenTraits
        if (traits.isNotEmpty()) {
            val offending = recipe.ingredients
                .map { catalog.ingredient(it.ingredientId) }
                .filter { it.traits.any { t -> t in traits } }
                .joinToString { it.name.lowercase() }
            reasons += "No apto para tus restricciones ($offending)"
        }

        recipe.ingredients
            .filter { it.ingredientId in dietary.excludedIngredientIds }
            .forEach { reasons += "Lleva ${catalog.ingredient(it.ingredientId).name.lowercase()}, que has excluido" }
        return reasons
    }
}
