package es.tfg.recetas.core.planner

import es.tfg.recetas.core.data.SampleCatalog
import es.tfg.recetas.core.model.Allergen
import es.tfg.recetas.core.model.Diet
import es.tfg.recetas.core.model.DietaryProfile
import es.tfg.recetas.core.model.MealType
import es.tfg.recetas.core.model.Supermarket
import es.tfg.recetas.core.model.UserProfile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MealPlannerTest {

    private val catalog = SampleCatalog.catalog
    private val planner = MealPlanner(catalog)

    private fun profile(
        budgetEuros: Int = 200,
        people: Int = 1,
        diet: Diet = Diet.OMNIVORE,
        allergens: Set<Allergen> = emptySet(),
        markets: Set<Supermarket> = setOf(Supermarket.MERCADONA, Supermarket.LIDL),
        meals: Set<MealType> = setOf(MealType.LUNCH, MealType.DINNER),
    ) = UserProfile(
        supermarkets = markets,
        dietary = DietaryProfile(diet = diet, allergens = allergens),
        monthlyBudgetCents = budgetEuros * 100,
        people = people,
        meals = meals,
    )

    @Test
    fun `generates a full month within budget`() {
        val p = profile(budgetEuros = 200)
        val result = planner.plan(p, seed = 1)
        val plan = assertIs<PlanResult.Success>(result).plan
        assertEquals(30 * 2, plan.meals.size)
        assertTrue(plan.totalCostCents <= p.monthlyBudgetCents)
        assertTrue(plan.distinctRecipes >= 10, "El menú debería ser variado (${plan.distinctRecipes} recetas)")
    }

    @Test
    fun `the same recipe is never served twice on the same day`() {
        val plan = assertIs<PlanResult.Success>(planner.plan(profile(), seed = 7)).plan
        plan.days.values.forEach { meals ->
            assertEquals(meals.size, meals.map { it.recipe.id }.toSet().size)
        }
    }

    @Test
    fun `restrictions are respected in every planned meal`() {
        val p = profile(budgetEuros = 250, diet = Diet.VEGAN, allergens = setOf(Allergen.GLUTEN),
            meals = setOf(MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER))
        val plan = assertIs<PlanResult.Success>(planner.plan(p, seed = 3)).plan
        plan.meals.forEach {
            assertTrue(RecipeFilter.isCompatible(it.recipe, catalog, p.dietary), "${it.recipe.name} no es apta")
        }
    }

    @Test
    fun `shopping list only uses selected supermarkets and covers needs`() {
        val p = profile(markets = setOf(Supermarket.ALDI))
        val plan = when (val r = planner.plan(p, seed = 2)) {
            is PlanResult.Success -> r.plan
            is PlanResult.OverBudget -> r.cheapestPlan
            is PlanResult.NoRecipes -> error("Debería haber recetas")
        }
        plan.shoppingList.items.forEach {
            assertEquals(Supermarket.ALDI, it.supermarket)
            assertTrue(it.purchase.purchasedQuantity >= it.neededQuantity - 1e-6)
        }
        assertEquals(plan.shoppingList.items.sumOf { it.purchase.costCents }, plan.totalCostCents)
    }

    @Test
    fun `budget that is too low reports the minimum needed`() {
        val result = planner.plan(profile(budgetEuros = 20), seed = 1)
        val over = assertIs<PlanResult.OverBudget>(result)
        assertTrue(over.minimumCostCents > 20 * 100)
    }

    @Test
    fun `more people need more money`() {
        val one = assertNotNull(planner.estimateMinimumCostCents(profile(people = 1)))
        val four = assertNotNull(planner.estimateMinimumCostCents(profile(people = 4)))
        assertTrue(four > one * 2, "4 personas ($four) deberían costar bastante más que 1 ($one)")
    }

    @Test
    fun `a tighter budget produces a cheaper plan`() {
        val generous = assertIs<PlanResult.Success>(planner.plan(profile(budgetEuros = 400), seed = 5)).plan
        val minimum = assertNotNull(planner.estimateMinimumCostCents(profile()))
        val tightBudget = (minimum * 1.15).toInt() / 100 + 1
        val tight = assertIs<PlanResult.Success>(planner.plan(profile(budgetEuros = tightBudget), seed = 5)).plan
        assertTrue(tight.totalCostCents <= tightBudget * 100)
        assertTrue(tight.totalCostCents <= generous.totalCostCents)
    }

    @Test
    fun `plans are reproducible with the same seed and vary with another`() {
        val p = profile(budgetEuros = 300)
        val a = assertIs<PlanResult.Success>(planner.plan(p, seed = 42)).plan
        val b = assertIs<PlanResult.Success>(planner.plan(p, seed = 42)).plan
        val c = assertIs<PlanResult.Success>(planner.plan(p, seed = 43)).plan
        assertEquals(a.meals.map { it.recipe.id }, b.meals.map { it.recipe.id })
        assertNotEquals(a.meals.map { it.recipe.id }, c.meals.map { it.recipe.id })
    }

    @Test
    fun `no recipes when restrictions leave a meal type empty`() {
        // Sin gluten, sin frutos secos, vegano y sin soja: no quedan desayunos salvo la macedonia;
        // excluyendo además la naranja no queda ninguno.
        val p = UserProfile(
            supermarkets = setOf(Supermarket.MERCADONA),
            dietary = DietaryProfile(
                diet = Diet.VEGAN,
                allergens = setOf(Allergen.GLUTEN, Allergen.TREE_NUTS, Allergen.SOY),
                excludedIngredientIds = setOf("naranja"),
            ),
            monthlyBudgetCents = 30000,
            meals = setOf(MealType.BREAKFAST, MealType.LUNCH),
        )
        val result = assertIs<PlanResult.NoRecipes>(planner.plan(p))
        assertEquals(setOf(MealType.BREAKFAST), result.mealTypes)
    }
}
