package es.tfg.recetas.core.planner

import es.tfg.recetas.core.model.Catalog
import es.tfg.recetas.core.model.Ingredient
import es.tfg.recetas.core.model.IngredientCategory
import es.tfg.recetas.core.model.MeasureUnit
import es.tfg.recetas.core.model.Product
import es.tfg.recetas.core.model.Supermarket
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PriceResolverTest {

    private val rice = Ingredient("arroz", "Arroz", MeasureUnit.GRAM, IngredientCategory.CEREALS_AND_PASTA)
    private val catalog = Catalog(
        ingredients = listOf(rice),
        products = listOf(
            Product("arroz", Supermarket.MERCADONA, "Arroz 1 kg", 1000.0, 130),
            Product("arroz", Supermarket.LIDL, "Arroz 500 g", 500.0, 60),
            Product("arroz", Supermarket.DIA, "Arroz 5 kg", 5000.0, 500),
        ),
        recipes = emptyList(),
    )

    @Test
    fun `only selected supermarkets are considered`() {
        val resolver = PriceResolver(catalog, setOf(Supermarket.MERCADONA))
        assertEquals(0.13, resolver.cheapestUnitPriceCents("arroz"), 1e-9)
        assertFalse(PriceResolver(catalog, setOf(Supermarket.ALDI)).isAvailable("arroz"))
    }

    @Test
    fun `best purchase rounds up to whole packages`() {
        val resolver = PriceResolver(catalog, setOf(Supermarket.MERCADONA, Supermarket.LIDL))
        // 1200 g: 2 × 1 kg = 260 cts, 3 × 500 g = 180 cts
        val purchase = resolver.bestPurchase("arroz", 1200.0)!!
        assertEquals(Supermarket.LIDL, purchase.product.supermarket)
        assertEquals(3, purchase.packs)
        assertEquals(180, purchase.costCents)
    }

    @Test
    fun `big package is not chosen when only a little is needed`() {
        val resolver = PriceResolver(catalog, setOf(Supermarket.LIDL, Supermarket.DIA))
        // 5 kg es más barato por gramo, pero para 300 g basta un paquete de 500 g
        assertEquals(60, resolver.bestPurchase("arroz", 300.0)!!.costCents)
        assertEquals(500, resolver.bestPurchase("arroz", 4800.0)!!.costCents)
    }

    @Test
    fun `nothing is bought for zero quantity`() {
        val resolver = PriceResolver(catalog, Supermarket.entries.toSet())
        assertNull(resolver.bestPurchase("arroz", 0.0))
        assertTrue(resolver.isAvailable("arroz"))
    }
}
