package es.tfg.recetas.core.model

/** Supermercados soportados por la aplicación. */
enum class Supermarket(val displayName: String) {
    MERCADONA("Mercadona"),
    DIA("Dia"),
    LIDL("Lidl"),
    ALDI("Aldi"),
    ALCAMPO("Alcampo"),
    CARREFOUR("Carrefour"),
    EROSKI("Eroski"),
    CONSUM("Consum"),
}

/**
 * Los 14 alérgenos de declaración obligatoria según el Reglamento (UE) 1169/2011.
 */
enum class Allergen(val displayName: String) {
    GLUTEN("Gluten"),
    CRUSTACEANS("Crustáceos"),
    EGGS("Huevo"),
    FISH("Pescado"),
    PEANUTS("Cacahuete"),
    SOY("Soja"),
    MILK("Leche"),
    TREE_NUTS("Frutos de cáscara"),
    CELERY("Apio"),
    MUSTARD("Mostaza"),
    SESAME("Sésamo"),
    SULPHITES("Sulfitos"),
    LUPIN("Altramuces"),
    MOLLUSCS("Moluscos"),
}

/** Características de un ingrediente relevantes para dietas y otras restricciones. */
enum class FoodTrait {
    MEAT,
    PORK,
    FISH,
    SEAFOOD,
    DAIRY,
    LACTOSE,
    EGG,
    HONEY,
}

/** Tipo de dieta que sigue el usuario. Cada dieta prohíbe un conjunto de características. */
enum class Diet(val displayName: String, val forbiddenTraits: Set<FoodTrait>) {
    OMNIVORE("Omnívora", emptySet()),
    PESCATARIAN("Pescetariana", setOf(FoodTrait.MEAT, FoodTrait.PORK)),
    VEGETARIAN("Vegetariana", setOf(FoodTrait.MEAT, FoodTrait.PORK, FoodTrait.FISH, FoodTrait.SEAFOOD)),
    VEGAN(
        "Vegana",
        setOf(
            FoodTrait.MEAT, FoodTrait.PORK, FoodTrait.FISH, FoodTrait.SEAFOOD,
            FoodTrait.DAIRY, FoodTrait.LACTOSE, FoodTrait.EGG, FoodTrait.HONEY,
        ),
    ),
}

enum class MealType(val displayName: String) {
    BREAKFAST("Desayuno"),
    LUNCH("Comida"),
    DINNER("Cena"),
}

enum class MeasureUnit(val symbol: String) {
    GRAM("g"),
    MILLILITER("ml"),
    UNIT("ud"),
}

enum class IngredientCategory(val displayName: String) {
    CEREALS_AND_PASTA("Cereales, pan y pasta"),
    LEGUMES("Legumbres"),
    VEGETABLES("Verduras y hortalizas"),
    FRUIT("Fruta"),
    MEAT("Carne"),
    FISH("Pescado y marisco"),
    EGGS_AND_DAIRY("Huevos y lácteos"),
    PLANT_PROTEIN("Proteína vegetal"),
    PANTRY("Despensa"),
}
