package es.tfg.recetas.core.model

data class Ingredient(
    val id: String,
    val name: String,
    val unit: MeasureUnit,
    val category: IngredientCategory,
    val allergens: Set<Allergen> = emptySet(),
    val traits: Set<FoodTrait> = emptySet(),
)

/**
 * Un producto concreto a la venta en un supermercado.
 *
 * @param packageSize cantidad del envase, expresada en la unidad del ingrediente.
 * @param priceCents precio del envase en céntimos de euro.
 */
data class Product(
    val ingredientId: String,
    val supermarket: Supermarket,
    val name: String,
    val packageSize: Double,
    val priceCents: Int,
) {
    val pricePerUnitCents: Double get() = priceCents / packageSize
}

data class RecipeIngredient(
    val ingredientId: String,
    /** Cantidad para las raciones indicadas en [Recipe.servings], en la unidad del ingrediente. */
    val quantity: Double,
)

data class Recipe(
    val id: String,
    val name: String,
    val description: String,
    val mealTypes: Set<MealType>,
    val servings: Int,
    val prepMinutes: Int,
    val ingredients: List<RecipeIngredient>,
    val steps: List<String>,
)

/** Restricciones alimentarias del usuario. */
data class DietaryProfile(
    val diet: Diet = Diet.OMNIVORE,
    val allergens: Set<Allergen> = emptySet(),
    val lactoseIntolerant: Boolean = false,
    val avoidPork: Boolean = false,
    val excludedIngredientIds: Set<String> = emptySet(),
) {
    val forbiddenTraits: Set<FoodTrait>
        get() = buildSet {
            addAll(diet.forbiddenTraits)
            if (lactoseIntolerant) add(FoodTrait.LACTOSE)
            if (avoidPork) add(FoodTrait.PORK)
        }
}

/** Toda la información que el usuario nos da sobre su situación. */
data class UserProfile(
    val supermarkets: Set<Supermarket>,
    val dietary: DietaryProfile,
    val monthlyBudgetCents: Int,
    val people: Int = 1,
    val meals: Set<MealType> = setOf(MealType.LUNCH, MealType.DINNER),
    val days: Int = 30,
) {
    init {
        require(people >= 1) { "Debe haber al menos una persona" }
        require(days >= 1) { "Debe planificarse al menos un día" }
        require(monthlyBudgetCents >= 0) { "El presupuesto no puede ser negativo" }
    }
}
