package es.tfg.recetas.core.data

import es.tfg.recetas.core.model.Catalog
import es.tfg.recetas.core.model.Product
import es.tfg.recetas.core.model.Supermarket
import kotlin.math.roundToInt

/**
 * Catálogo de ejemplo para el prototipo.
 *
 * IMPORTANTE: los precios son **aproximados e ilustrativos**. Se parte de un precio de referencia
 * por producto y se aplica a cada supermercado un factor medio de precio más una pequeña variación
 * determinista por producto, para simular que no todos son más baratos en todo. En una versión
 * real estos datos vendrían de un servicio que consulte los precios de cada cadena.
 */
object SampleCatalog {

    /** Nivel de precios medio de cada cadena respecto a la referencia (1.0). */
    private val priceLevel = mapOf(
        Supermarket.MERCADONA to 1.00,
        Supermarket.DIA to 0.99,
        Supermarket.LIDL to 0.95,
        Supermarket.ALDI to 0.94,
        Supermarket.ALCAMPO to 0.97,
        Supermarket.CARREFOUR to 1.03,
        Supermarket.EROSKI to 1.08,
        Supermarket.CONSUM to 1.04,
    )

    /** Productos que (en este ejemplo) no se venden en algunas cadenas. */
    private val notSoldAt = mapOf(
        "tofu" to setOf(Supermarket.DIA, Supermarket.ALDI),
        "soja_texturizada" to setOf(Supermarket.DIA, Supermarket.ALDI, Supermarket.CONSUM),
        "quinoa" to setOf(Supermarket.DIA),
        "crema_cacahuete" to setOf(Supermarket.CONSUM),
        "cuscus" to setOf(Supermarket.ALDI),
    )

    val catalog: Catalog by lazy {
        Catalog(
            ingredients = sampleIngredients.map { it.ingredient },
            products = sampleIngredients.flatMap { seed ->
                Supermarket.entries
                    .filter { it !in notSoldAt[seed.ingredient.id].orEmpty() }
                    .map { market ->
                        Product(
                            ingredientId = seed.ingredient.id,
                            supermarket = market,
                            name = seed.packageName,
                            packageSize = seed.packageSize,
                            priceCents = priceFor(seed, market),
                        )
                    }
            },
            recipes = sampleRecipes,
        )
    }

    private fun priceFor(seed: SeedIngredient, market: Supermarket): Int {
        // Variación determinista entre -8 % y +8 % según el par (producto, supermercado).
        val hash = (seed.ingredient.id + market.name).hashCode()
        val variation = 1.0 + (Math.floorMod(hash, 17) - 8) / 100.0
        val level = priceLevel.getValue(market)
        return (seed.referencePriceCents * level * variation).roundToInt().coerceAtLeast(1)
    }
}
