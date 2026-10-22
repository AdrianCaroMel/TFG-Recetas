package es.tfg.recetas.core.planner

import es.tfg.recetas.core.model.Catalog
import es.tfg.recetas.core.model.Product
import es.tfg.recetas.core.model.Recipe
import es.tfg.recetas.core.model.Supermarket
import kotlin.math.ceil

/** Compra de [packs] envases de un producto. */
data class Purchase(val product: Product, val packs: Int) {
    val costCents: Int get() = packs * product.priceCents
    val purchasedQuantity: Double get() = packs * product.packageSize
}

/**
 * Resuelve precios teniendo en cuenta solo los supermercados elegidos por el usuario.
 */
class PriceResolver(private val catalog: Catalog, val supermarkets: Set<Supermarket>) {

    private val options: Map<String, List<Product>> =
        catalog.ingredients.keys.associateWith { catalog.productsFor(it, supermarkets) }

    fun isAvailable(ingredientId: String): Boolean = options[ingredientId].orEmpty().isNotEmpty()

    fun isAvailable(recipe: Recipe): Boolean = recipe.ingredients.all { isAvailable(it.ingredientId) }

    /** Precio por unidad (g, ml o ud) más barato entre los supermercados elegidos, en céntimos. */
    fun cheapestUnitPriceCents(ingredientId: String): Double =
        options[ingredientId].orEmpty().minOfOrNull { it.pricePerUnitCents }
            ?: throw IllegalArgumentException("Ingrediente $ingredientId no disponible en $supermarkets")

    /**
     * La forma más barata de comprar al menos [quantity] de un ingrediente con un único tipo de envase.
     * A igualdad de precio se prefiere la opción que deja menos sobrante.
     */
    fun bestPurchase(ingredientId: String, quantity: Double): Purchase? {
        if (quantity <= EPSILON) return null
        return options[ingredientId].orEmpty()
            .map { Purchase(it, ceil(quantity / it.packageSize - EPSILON).toInt().coerceAtLeast(1)) }
            .minWithOrNull(compareBy<Purchase> { it.costCents }.thenBy { it.purchasedQuantity })
    }

    fun purchaseCostCents(ingredientId: String, quantity: Double): Int =
        bestPurchase(ingredientId, quantity)?.costCents ?: 0

    /**
     * Coste estimado de cocinar una receta para [people] personas sin tener en cuenta los envases
     * (cantidad usada × precio unitario más barato). Útil para mostrar el "coste por plato".
     */
    fun estimatedRecipeCostCents(recipe: Recipe, people: Int): Double {
        val factor = people.toDouble() / recipe.servings
        return recipe.ingredients.sumOf { it.quantity * factor * cheapestUnitPriceCents(it.ingredientId) }
    }

    companion object {
        const val EPSILON = 1e-9
    }
}
