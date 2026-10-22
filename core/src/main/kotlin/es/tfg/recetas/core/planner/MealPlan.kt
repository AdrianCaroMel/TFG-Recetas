package es.tfg.recetas.core.planner

import es.tfg.recetas.core.model.Ingredient
import es.tfg.recetas.core.model.MealType
import es.tfg.recetas.core.model.Recipe
import es.tfg.recetas.core.model.Supermarket
import es.tfg.recetas.core.model.UserProfile

data class PlannedMeal(
    /** Día del plan, empezando en 0. */
    val day: Int,
    val mealType: MealType,
    val recipe: Recipe,
    /** Coste estimado del plato para todas las personas (sin redondear a envases). */
    val estimatedCostCents: Double,
)

data class ShoppingItem(
    val ingredient: Ingredient,
    /** Cantidad que realmente se necesita durante el mes. */
    val neededQuantity: Double,
    val purchase: Purchase,
) {
    val supermarket: Supermarket get() = purchase.product.supermarket
    val leftoverQuantity: Double get() = purchase.purchasedQuantity - neededQuantity
}

data class ShoppingList(val items: List<ShoppingItem>) {
    val totalCents: Int get() = items.sumOf { it.purchase.costCents }

    val bySupermarket: Map<Supermarket, List<ShoppingItem>>
        get() = items.groupBy { it.supermarket }
            .mapValues { (_, list) -> list.sortedWith(compareBy({ it.ingredient.category }, { it.ingredient.name })) }
            .toSortedMap()
}

data class MealPlan(
    val profile: UserProfile,
    val meals: List<PlannedMeal>,
    val shoppingList: ShoppingList,
) {
    val totalCostCents: Int get() = shoppingList.totalCents
    val withinBudget: Boolean get() = totalCostCents <= profile.monthlyBudgetCents
    val remainingBudgetCents: Int get() = profile.monthlyBudgetCents - totalCostCents
    val days: Map<Int, List<PlannedMeal>> get() = meals.groupBy { it.day }.toSortedMap()
    val distinctRecipes: Int get() = meals.map { it.recipe.id }.toSet().size
}

sealed interface PlanResult {
    /** Plan generado que respeta el presupuesto. */
    data class Success(val plan: MealPlan) : PlanResult

    /**
     * No se ha encontrado un plan dentro del presupuesto. [cheapestPlan] es el plan más barato
     * encontrado, y su coste sirve de referencia para indicar al usuario cuánto necesitaría.
     */
    data class OverBudget(val cheapestPlan: MealPlan) : PlanResult {
        val minimumCostCents: Int get() = cheapestPlan.totalCostCents
    }

    /** No hay recetas que cumplan las restricciones para algún tipo de comida. */
    data class NoRecipes(val mealTypes: Set<MealType>) : PlanResult
}
