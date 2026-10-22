package es.tfg.recetas.core.planner

import es.tfg.recetas.core.data.SampleCatalog
import es.tfg.recetas.core.model.Allergen
import es.tfg.recetas.core.model.Diet
import es.tfg.recetas.core.model.DietaryProfile
import es.tfg.recetas.core.model.FoodTrait
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RecipeFilterTest {

    private val catalog = SampleCatalog.catalog
    private fun recipe(id: String) = catalog.recipe(id)!!

    @Test
    fun `vegan diet excludes any animal product`() {
        val vegan = DietaryProfile(diet = Diet.VEGAN)
        assertTrue(RecipeFilter.isCompatible(recipe("lentejas_verduras"), catalog, vegan))
        assertFalse(RecipeFilter.isCompatible(recipe("tortilla_patatas"), catalog, vegan))
        assertFalse(RecipeFilter.isCompatible(recipe("gachas_avena"), catalog, vegan))
        assertFalse(RecipeFilter.isCompatible(recipe("lentejas_chorizo"), catalog, vegan))

        val allowed = catalog.recipes.filter { RecipeFilter.isCompatible(it, catalog, vegan) }
        val animalTraits = Diet.VEGAN.forbiddenTraits
        assertTrue(allowed.isNotEmpty())
        assertTrue(allowed.none { catalog.traitsOf(it).any { t -> t in animalTraits } })
    }

    @Test
    fun `vegetarian allows egg and dairy but not fish`() {
        val vegetarian = DietaryProfile(diet = Diet.VEGETARIAN)
        assertTrue(RecipeFilter.isCompatible(recipe("tortilla_patatas"), catalog, vegetarian))
        assertFalse(RecipeFilter.isCompatible(recipe("merluza_horno"), catalog, vegetarian))
    }

    @Test
    fun `allergies exclude recipes containing the allergen`() {
        val celiac = DietaryProfile(allergens = setOf(Allergen.GLUTEN))
        assertFalse(RecipeFilter.isCompatible(recipe("macarrones_atun"), catalog, celiac))
        assertTrue(RecipeFilter.isCompatible(recipe("arroz_cubana"), catalog, celiac))
        assertTrue(RecipeFilter.incompatibilityReasons(recipe("macarrones_atun"), catalog, celiac).isNotEmpty())
    }

    @Test
    fun `lactose intolerance and pork avoidance`() {
        val profile = DietaryProfile(lactoseIntolerant = true, avoidPork = true)
        assertTrue(FoodTrait.LACTOSE in profile.forbiddenTraits)
        assertFalse(RecipeFilter.isCompatible(recipe("yogur_fruta"), catalog, profile))
        assertFalse(RecipeFilter.isCompatible(recipe("huevos_rotos"), catalog, profile))
        assertTrue(RecipeFilter.isCompatible(recipe("pollo_horno"), catalog, profile))
    }

    @Test
    fun `excluded ingredients are respected`() {
        val noMushrooms = DietaryProfile(excludedIngredientIds = setOf("champinones"))
        assertFalse(RecipeFilter.isCompatible(recipe("revuelto_champinones"), catalog, noMushrooms))
    }
}
