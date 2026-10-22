package es.tfg.recetas.core.planner

import es.tfg.recetas.core.model.Catalog
import es.tfg.recetas.core.model.MealType
import es.tfg.recetas.core.model.Recipe
import es.tfg.recetas.core.model.UserProfile
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

data class PlannerConfig(
    /** Días mínimos que deben pasar antes de repetir una receta (si hay recetas suficientes). */
    val minDaysBetweenRepeats: Int = 4,
    /** Peso del componente aleatorio en la puntuación; da variedad entre planes distintos. */
    val randomness: Double = 0.6,
    /** Penalización por superar el gasto "objetivo" por comida. */
    val overspendPenalty: Double = 0.5,
    /** Iteraciones máximas de la fase de reparación cuando el plan se pasa de presupuesto. */
    val maxRepairIterations: Int = 500,
)

/**
 * Genera un menú mensual que respeta restricciones alimentarias y presupuesto.
 *
 * El algoritmo es voraz con anticipación (look-ahead) y una fase de reparación:
 *
 * 1. **Filtrado**: se descartan las recetas no aptas (alérgenos, dieta, ingredientes excluidos)
 *    y las que tienen algún ingrediente que no se vende en los supermercados elegidos.
 * 2. **Cota inferior**: para cada tipo de comida se calcula el coste mínimo de un plato. Sumándolas
 *    se obtiene, para cada hueco del calendario, lo mínimo que costará rellenar los huecos restantes.
 * 3. **Asignación voraz**: se recorren los huecos (día × comida) en orden. Para cada uno, se evalúan
 *    las recetas candidatas calculando su coste *marginal* sobre la cesta de la compra acumulada
 *    (aprovechando envases ya comprados) y se descartan las que harían imposible terminar el mes
 *    dentro del presupuesto. Entre las viables se elige la de mayor puntuación, que premia la
 *    variedad y penaliza pasarse del gasto objetivo por comida.
 * 4. **Reparación**: si al redondear a envases el total supera el presupuesto, se sustituyen
 *    platos por alternativas más baratas hasta cumplirlo o no poder mejorar más.
 */
class MealPlanner(
    private val catalog: Catalog,
    private val config: PlannerConfig = PlannerConfig(),
) {

    private data class Slot(val day: Int, val mealType: MealType)

    /** Recetas aptas y disponibles para el perfil, agrupadas por tipo de comida. */
    fun candidates(profile: UserProfile): Map<MealType, List<Recipe>> {
        val resolver = PriceResolver(catalog, profile.supermarkets)
        val valid = catalog.recipes.filter {
            RecipeFilter.isCompatible(it, catalog, profile.dietary) && resolver.isAvailable(it)
        }
        return profile.meals.associateWith { meal -> valid.filter { meal in it.mealTypes } }
    }

    fun plan(profile: UserProfile, seed: Long = 0L): PlanResult {
        val byMeal = candidates(profile)
        val missing = byMeal.filterValues { it.isEmpty() }.keys
        if (missing.isNotEmpty() || byMeal.isEmpty()) return PlanResult.NoRecipes(missing)

        val resolver = PriceResolver(catalog, profile.supermarkets)
        val budget = profile.monthlyBudgetCents
        val assignment = greedyAssignment(profile, byMeal, resolver, budget.toDouble(), Random(seed))

        val basket = Basket(resolver)
        assignment.forEach { (_, recipe) -> basket.add(scaled(recipe, profile.people)) }
        if (basket.costCents > budget) repair(profile, byMeal, assignment, basket, budget)

        val plan = buildPlan(profile, assignment, basket, resolver)
        return if (plan.withinBudget) PlanResult.Success(plan) else PlanResult.OverBudget(plan)
    }

    /**
     * Estimación del gasto mínimo mensual manteniendo la variedad. Se usa para orientar al usuario
     * al introducir su presupuesto.
     */
    fun estimateMinimumCostCents(profile: UserProfile): Int? {
        val byMeal = candidates(profile)
        if (byMeal.isEmpty() || byMeal.values.any { it.isEmpty() }) return null
        val resolver = PriceResolver(catalog, profile.supermarkets)
        val assignment = greedyAssignment(profile, byMeal, resolver, budget = 0.0, random = Random(0))
        val basket = Basket(resolver)
        assignment.forEach { (_, recipe) -> basket.add(scaled(recipe, profile.people)) }
        return basket.costCents
    }

    private fun greedyAssignment(
        profile: UserProfile,
        byMeal: Map<MealType, List<Recipe>>,
        resolver: PriceResolver,
        budget: Double,
        random: Random,
    ): MutableList<Pair<Slot, Recipe>> {
        val slots = (0 until profile.days).flatMap { day ->
            MealType.entries.filter { it in byMeal }.map { Slot(day, it) }
        }

        val linearCost = byMeal.values.flatten().distinct()
            .associateWith { resolver.estimatedRecipeCostCents(it, profile.people) }
        val minCostPerMeal = byMeal.mapValues { (_, recipes) -> recipes.minOf { linearCost.getValue(it) } }

        // lowerBoundFrom[i] = coste mínimo para rellenar los huecos i, i+1, ..., n-1
        val lowerBoundFrom = DoubleArray(slots.size + 1)
        for (i in slots.indices.reversed()) {
            lowerBoundFrom[i] = lowerBoundFrom[i + 1] + minCostPerMeal.getValue(slots[i].mealType)
        }

        val basket = Basket(resolver)
        val lastDayUsed = mutableMapOf<String, Int>()
        val result = ArrayList<Pair<Slot, Recipe>>(slots.size)

        for ((i, slot) in slots.withIndex()) {
            val candidates = byMeal.getValue(slot.mealType)
            val gap = min(config.minDaysBetweenRepeats, candidates.size - 1).coerceAtLeast(0)
            fun daysSince(r: Recipe): Int = lastDayUsed[r.id]?.let { slot.day - it } ?: Int.MAX_VALUE
            // Nunca repetir el mismo día salvo que no quede otra opción.
            val pool = candidates.filter { daysSince(it) >= max(gap, 1) }
                .ifEmpty { candidates.filter { daysSince(it) >= 1 } }
                .ifEmpty { candidates }

            val remainingSlots = slots.size - i
            val targetPerSlot = max(0.0, (budget - basket.costCents) / remainingSlots)

            data class Option(val recipe: Recipe, val marginal: Int, val score: Double)
            val options = pool.map { recipe ->
                val marginal = basket.marginalCostCents(scaled(recipe, profile.people))
                val novelty = min(daysSince(recipe), NOVELTY_HORIZON_DAYS).toDouble() / NOVELTY_HORIZON_DAYS
                val overspend = if (targetPerSlot > 0) max(0.0, (marginal - targetPerSlot) / targetPerSlot) else 0.0
                val score = novelty + random.nextDouble() * config.randomness - config.overspendPenalty * overspend
                Option(recipe, marginal, score)
            }
            val feasible = options.filter { basket.costCents + it.marginal + lowerBoundFrom[i + 1] <= budget }
            val chosen = if (feasible.isNotEmpty()) {
                feasible.maxBy { it.score }
            } else {
                options.minWith(compareBy<Option> { it.marginal }.thenByDescending { it.score })
            }

            basket.add(scaled(chosen.recipe, profile.people))
            lastDayUsed[chosen.recipe.id] = slot.day
            result += slot to chosen.recipe
        }
        return result
    }

    /** Sustituye platos por otros más baratos mientras el total supere el presupuesto. */
    private fun repair(
        profile: UserProfile,
        byMeal: Map<MealType, List<Recipe>>,
        assignment: MutableList<Pair<Slot, Recipe>>,
        basket: Basket,
        budget: Int,
    ) {
        repeat(config.maxRepairIterations) {
            if (basket.costCents <= budget) return
            var bestSaving = 0
            var bestIndex = -1
            var bestRecipe: Recipe? = null
            for ((index, entry) in assignment.withIndex()) {
                val (slot, current) = entry
                val currentScaled = scaled(current, profile.people)
                val sameDay = assignment.filter { it.first.day == slot.day && it.first != slot }.map { it.second.id }
                basket.remove(currentScaled)
                val currentMarginal = basket.marginalCostCents(currentScaled)
                for (alternative in byMeal.getValue(slot.mealType)) {
                    if (alternative.id == current.id || alternative.id in sameDay) continue
                    val saving = currentMarginal - basket.marginalCostCents(scaled(alternative, profile.people))
                    if (saving > bestSaving) {
                        bestSaving = saving
                        bestIndex = index
                        bestRecipe = alternative
                    }
                }
                basket.add(currentScaled)
            }
            val replacement = bestRecipe ?: return
            val (slot, old) = assignment[bestIndex]
            basket.remove(scaled(old, profile.people))
            basket.add(scaled(replacement, profile.people))
            assignment[bestIndex] = slot to replacement
        }
    }

    private fun buildPlan(
        profile: UserProfile,
        assignment: List<Pair<Slot, Recipe>>,
        basket: Basket,
        resolver: PriceResolver,
    ): MealPlan {
        val meals = assignment.map { (slot, recipe) ->
            PlannedMeal(slot.day, slot.mealType, recipe, resolver.estimatedRecipeCostCents(recipe, profile.people))
        }
        val items = basket.quantities.mapNotNull { (ingredientId, quantity) ->
            resolver.bestPurchase(ingredientId, quantity)?.let { ShoppingItem(catalog.ingredient(ingredientId), quantity, it) }
        }
        return MealPlan(profile, meals, ShoppingList(items))
    }

    private val scaledCache = ConcurrentHashMap<Pair<String, Int>, Map<String, Double>>()

    /** Cantidades de ingredientes de una receta ajustadas al número de personas. */
    private fun scaled(recipe: Recipe, people: Int): Map<String, Double> =
        scaledCache.getOrPut(recipe.id to people) {
            val factor = people.toDouble() / recipe.servings
            recipe.ingredients.groupBy { it.ingredientId }
                .mapValues { (_, list) -> list.sumOf { it.quantity } * factor }
        }

    private companion object {
        const val NOVELTY_HORIZON_DAYS = 14
    }
}

/**
 * Cesta de la compra acumulada. Mantiene la cantidad total de cada ingrediente y el coste de
 * comprarla en envases completos, de forma que el coste marginal de añadir una receta tiene en
 * cuenta lo que sobra de envases ya comprados.
 */
internal class Basket(private val resolver: PriceResolver) {
    private val _quantities = HashMap<String, Double>()
    private val costs = HashMap<String, Int>()

    val quantities: Map<String, Double> get() = _quantities

    var costCents: Int = 0
        private set

    fun marginalCostCents(ingredients: Map<String, Double>): Int =
        ingredients.entries.sumOf { (id, qty) ->
            resolver.purchaseCostCents(id, (_quantities[id] ?: 0.0) + qty) - (costs[id] ?: 0)
        }

    fun add(ingredients: Map<String, Double>) = update(ingredients, sign = 1.0)

    fun remove(ingredients: Map<String, Double>) = update(ingredients, sign = -1.0)

    private fun update(ingredients: Map<String, Double>, sign: Double) {
        for ((id, qty) in ingredients) {
            val newQty = (_quantities[id] ?: 0.0) + sign * qty
            val newCost: Int
            if (newQty <= PriceResolver.EPSILON) {
                _quantities.remove(id)
                newCost = 0
            } else {
                _quantities[id] = newQty
                newCost = resolver.purchaseCostCents(id, newQty)
            }
            costCents += newCost - (costs[id] ?: 0)
            if (newCost == 0) costs.remove(id) else costs[id] = newCost
        }
    }
}
