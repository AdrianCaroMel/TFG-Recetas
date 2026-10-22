package es.tfg.recetas.core.model

/**
 * Conjunto de datos con el que trabaja el planificador: ingredientes, productos a la venta y recetas.
 */
class Catalog(
    ingredients: List<Ingredient>,
    val products: List<Product>,
    val recipes: List<Recipe>,
) {
    val ingredients: Map<String, Ingredient> = ingredients.associateBy { it.id }

    private val productsByIngredient: Map<String, List<Product>> = products.groupBy { it.ingredientId }

    init {
        require(this.ingredients.size == ingredients.size) { "Hay ingredientes con id duplicado" }
        products.forEach { p ->
            require(p.ingredientId in this.ingredients) { "Producto '${p.name}' con ingrediente desconocido ${p.ingredientId}" }
            require(p.packageSize > 0 && p.priceCents > 0) { "Producto '${p.name}' con tamaño o precio no válido" }
        }
        require(recipes.map { it.id }.toSet().size == recipes.size) { "Hay recetas con id duplicado" }
        recipes.forEach { r ->
            require(r.servings > 0) { "La receta ${r.id} debe tener al menos una ración" }
            r.ingredients.forEach { ri ->
                require(ri.ingredientId in this.ingredients) { "La receta ${r.id} usa el ingrediente desconocido ${ri.ingredientId}" }
            }
        }
    }

    fun ingredient(id: String): Ingredient = ingredients.getValue(id)

    fun productsFor(ingredientId: String, supermarkets: Set<Supermarket>): List<Product> =
        productsByIngredient[ingredientId].orEmpty().filter { it.supermarket in supermarkets }

    fun allergensOf(recipe: Recipe): Set<Allergen> =
        recipe.ingredients.flatMapTo(mutableSetOf()) { ingredient(it.ingredientId).allergens }

    fun traitsOf(recipe: Recipe): Set<FoodTrait> =
        recipe.ingredients.flatMapTo(mutableSetOf()) { ingredient(it.ingredientId).traits }

    fun recipe(id: String): Recipe? = recipes.firstOrNull { it.id == id }
}
