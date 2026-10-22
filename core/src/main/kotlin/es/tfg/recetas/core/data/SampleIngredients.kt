package es.tfg.recetas.core.data

import es.tfg.recetas.core.model.Allergen
import es.tfg.recetas.core.model.Allergen.CELERY
import es.tfg.recetas.core.model.Allergen.CRUSTACEANS
import es.tfg.recetas.core.model.Allergen.EGGS
import es.tfg.recetas.core.model.Allergen.FISH
import es.tfg.recetas.core.model.Allergen.GLUTEN
import es.tfg.recetas.core.model.Allergen.MILK
import es.tfg.recetas.core.model.Allergen.PEANUTS
import es.tfg.recetas.core.model.Allergen.SOY
import es.tfg.recetas.core.model.Allergen.SULPHITES
import es.tfg.recetas.core.model.Allergen.TREE_NUTS
import es.tfg.recetas.core.model.FoodTrait
import es.tfg.recetas.core.model.FoodTrait.DAIRY
import es.tfg.recetas.core.model.FoodTrait.EGG
import es.tfg.recetas.core.model.FoodTrait.HONEY
import es.tfg.recetas.core.model.FoodTrait.LACTOSE
import es.tfg.recetas.core.model.FoodTrait.MEAT
import es.tfg.recetas.core.model.FoodTrait.PORK
import es.tfg.recetas.core.model.FoodTrait.SEAFOOD
import es.tfg.recetas.core.model.Ingredient
import es.tfg.recetas.core.model.IngredientCategory
import es.tfg.recetas.core.model.IngredientCategory.CEREALS_AND_PASTA
import es.tfg.recetas.core.model.IngredientCategory.EGGS_AND_DAIRY
import es.tfg.recetas.core.model.IngredientCategory.FRUIT
import es.tfg.recetas.core.model.IngredientCategory.LEGUMES
import es.tfg.recetas.core.model.IngredientCategory.PANTRY
import es.tfg.recetas.core.model.IngredientCategory.PLANT_PROTEIN
import es.tfg.recetas.core.model.IngredientCategory.VEGETABLES
import es.tfg.recetas.core.model.MeasureUnit
import es.tfg.recetas.core.model.MeasureUnit.GRAM
import es.tfg.recetas.core.model.MeasureUnit.MILLILITER
import es.tfg.recetas.core.model.MeasureUnit.UNIT

/**
 * Ingrediente de ejemplo con un envase y precio de referencia (aprox. Mercadona, 2025).
 * Los precios del resto de supermercados se derivan de este en [SampleCatalog].
 */
internal data class SeedIngredient(
    val ingredient: Ingredient,
    val packageName: String,
    val packageSize: Double,
    val referencePriceCents: Int,
)

private fun seed(
    id: String,
    name: String,
    unit: MeasureUnit,
    category: IngredientCategory,
    packageName: String,
    packageSize: Number,
    priceCents: Int,
    allergens: Set<Allergen> = emptySet(),
    traits: Set<FoodTrait> = emptySet(),
) = SeedIngredient(Ingredient(id, name, unit, category, allergens, traits), packageName, packageSize.toDouble(), priceCents)

internal val sampleIngredients: List<SeedIngredient> = listOf(
    // Cereales, pan y pasta
    seed("arroz", "Arroz redondo", GRAM, CEREALS_AND_PASTA, "Arroz redondo 1 kg", 1000, 129),
    seed("macarrones", "Macarrones", GRAM, CEREALS_AND_PASTA, "Macarrones 1 kg", 1000, 120, setOf(GLUTEN)),
    seed("espaguetis", "Espaguetis", GRAM, CEREALS_AND_PASTA, "Espaguetis 1 kg", 1000, 120, setOf(GLUTEN)),
    seed("cuscus", "Cuscús", GRAM, CEREALS_AND_PASTA, "Cuscús 500 g", 500, 130, setOf(GLUTEN)),
    seed("quinoa", "Quinoa", GRAM, CEREALS_AND_PASTA, "Quinoa 500 g", 500, 260),
    seed("pan_barra", "Pan de barra", GRAM, CEREALS_AND_PASTA, "Barra de pan 250 g", 250, 55, setOf(GLUTEN)),
    seed("pan_molde", "Pan de molde", GRAM, CEREALS_AND_PASTA, "Pan de molde 450 g", 450, 125, setOf(GLUTEN)),
    seed("pan_rallado", "Pan rallado", GRAM, CEREALS_AND_PASTA, "Pan rallado 500 g", 500, 85, setOf(GLUTEN)),
    seed("harina", "Harina de trigo", GRAM, CEREALS_AND_PASTA, "Harina de trigo 1 kg", 1000, 75, setOf(GLUTEN)),
    seed("avena", "Copos de avena", GRAM, CEREALS_AND_PASTA, "Copos de avena 500 g", 500, 115, setOf(GLUTEN)),
    seed("tortillas_trigo", "Tortillas de trigo", UNIT, CEREALS_AND_PASTA, "Tortillas de trigo 8 uds", 8, 125, setOf(GLUTEN)),

    // Legumbres
    seed("lentejas", "Lentejas pardinas", GRAM, LEGUMES, "Lentejas pardinas 1 kg", 1000, 185),
    seed("garbanzos", "Garbanzos cocidos", GRAM, LEGUMES, "Garbanzos cocidos bote 400 g", 400, 85),
    seed("alubias", "Alubias cocidas", GRAM, LEGUMES, "Alubias cocidas bote 400 g", 400, 85),

    // Verduras y hortalizas
    seed("patata", "Patata", GRAM, VEGETABLES, "Patatas malla 3 kg", 3000, 320),
    seed("cebolla", "Cebolla", GRAM, VEGETABLES, "Cebollas malla 2 kg", 2000, 210),
    seed("ajo", "Ajo", GRAM, VEGETABLES, "Ajos malla 250 g", 250, 120),
    seed("tomate", "Tomate", GRAM, VEGETABLES, "Tomate 1 kg", 1000, 220),
    seed("tomate_triturado", "Tomate triturado", GRAM, VEGETABLES, "Tomate triturado 800 g", 800, 95),
    seed("tomate_frito", "Tomate frito", GRAM, VEGETABLES, "Tomate frito 400 g", 400, 75),
    seed("pimiento_rojo", "Pimiento rojo", GRAM, VEGETABLES, "Pimiento rojo 1 kg", 1000, 290),
    seed("pimiento_verde", "Pimiento verde", GRAM, VEGETABLES, "Pimiento verde 1 kg", 1000, 240),
    seed("calabacin", "Calabacín", GRAM, VEGETABLES, "Calabacín 1 kg", 1000, 180),
    seed("berenjena", "Berenjena", GRAM, VEGETABLES, "Berenjena 1 kg", 1000, 200),
    seed("zanahoria", "Zanahoria", GRAM, VEGETABLES, "Zanahorias bolsa 1 kg", 1000, 110),
    seed("espinacas", "Espinacas congeladas", GRAM, VEGETABLES, "Espinacas congeladas 1 kg", 1000, 165),
    seed("brocoli", "Brócoli", GRAM, VEGETABLES, "Brócoli 500 g", 500, 160),
    seed("lechuga", "Lechuga", UNIT, VEGETABLES, "Lechuga romana 1 ud", 1, 90),
    seed("pepino", "Pepino", GRAM, VEGETABLES, "Pepino 1 kg", 1000, 160),
    seed("calabaza", "Calabaza", GRAM, VEGETABLES, "Calabaza 1 kg", 1000, 150),
    seed("champinones", "Champiñones", GRAM, VEGETABLES, "Champiñón laminado 250 g", 250, 115),
    seed("judias_verdes", "Judías verdes congeladas", GRAM, VEGETABLES, "Judías verdes congeladas 1 kg", 1000, 185),
    seed("guisantes", "Guisantes congelados", GRAM, VEGETABLES, "Guisantes congelados 1 kg", 1000, 170),
    seed("maiz", "Maíz dulce", GRAM, VEGETABLES, "Maíz dulce 3 latas (420 g)", 420, 140),

    // Fruta
    seed("platano", "Plátano", GRAM, FRUIT, "Plátanos 1 kg", 1000, 190),
    seed("manzana", "Manzana", GRAM, FRUIT, "Manzanas 1 kg", 1000, 180),
    seed("naranja", "Naranja", GRAM, FRUIT, "Naranjas malla 2 kg", 2000, 230),
    seed("limon", "Limón", GRAM, FRUIT, "Limones malla 500 g", 500, 110),

    // Carne
    seed("pollo_pechuga", "Pechuga de pollo", GRAM, IngredientCategory.MEAT, "Pechuga de pollo fileteada 1 kg", 1000, 650, traits = setOf(MEAT)),
    seed("pollo_muslos", "Contramuslos de pollo", GRAM, IngredientCategory.MEAT, "Contramuslos de pollo 1 kg", 1000, 350, traits = setOf(MEAT)),
    seed("carne_picada", "Carne picada mixta", GRAM, IngredientCategory.MEAT, "Carne picada vacuno y cerdo 500 g", 500, 360, traits = setOf(MEAT, PORK)),
    seed("lomo_cerdo", "Lomo de cerdo", GRAM, IngredientCategory.MEAT, "Lomo de cerdo fileteado 1 kg", 1000, 590, traits = setOf(MEAT, PORK)),
    seed("chorizo", "Chorizo", GRAM, IngredientCategory.MEAT, "Chorizo sarta 200 g", 200, 160, traits = setOf(MEAT, PORK)),
    seed("jamon", "Taquitos de jamón serrano", GRAM, IngredientCategory.MEAT, "Taquitos de jamón 200 g", 200, 210, traits = setOf(MEAT, PORK)),

    // Pescado y marisco
    seed("merluza", "Merluza congelada", GRAM, IngredientCategory.FISH, "Filetes de merluza congelada 1 kg", 1000, 620, setOf(FISH), setOf(FoodTrait.FISH)),
    seed("salmon", "Salmón", GRAM, IngredientCategory.FISH, "Lomos de salmón congelado 500 g", 500, 550, setOf(FISH), setOf(FoodTrait.FISH)),
    seed("atun", "Atún en aceite", GRAM, IngredientCategory.FISH, "Atún en aceite 3 latas (240 g)", 240, 310, setOf(FISH), setOf(FoodTrait.FISH)),
    seed("gambas", "Gambas peladas congeladas", GRAM, IngredientCategory.FISH, "Gambas peladas 400 g", 400, 450, setOf(CRUSTACEANS), setOf(SEAFOOD)),

    // Huevos y lácteos
    seed("huevos", "Huevos", UNIT, EGGS_AND_DAIRY, "Huevos L docena", 12, 240, setOf(EGGS), setOf(EGG)),
    seed("leche", "Leche entera", MILLILITER, EGGS_AND_DAIRY, "Leche entera pack 6 L", 6000, 540, setOf(MILK), setOf(DAIRY, LACTOSE)),
    seed("yogur", "Yogur natural", GRAM, EGGS_AND_DAIRY, "Yogur natural 4 × 125 g", 500, 90, setOf(MILK), setOf(DAIRY, LACTOSE)),
    seed("queso_rallado", "Queso rallado", GRAM, EGGS_AND_DAIRY, "Queso rallado 200 g", 200, 185, setOf(MILK), setOf(DAIRY, LACTOSE)),

    // Proteína vegetal y bebidas vegetales
    seed("tofu", "Tofu", GRAM, PLANT_PROTEIN, "Tofu firme 400 g", 400, 220, setOf(SOY)),
    seed("soja_texturizada", "Soja texturizada", GRAM, PLANT_PROTEIN, "Soja texturizada 200 g", 200, 180, setOf(SOY)),
    seed("bebida_soja", "Bebida de soja", MILLILITER, PLANT_PROTEIN, "Bebida de soja 1 L", 1000, 110, setOf(SOY)),

    // Despensa
    seed("aceite_oliva", "Aceite de oliva", MILLILITER, PANTRY, "Aceite de oliva 1 L", 1000, 650),
    seed("aceite_girasol", "Aceite de girasol", MILLILITER, PANTRY, "Aceite de girasol 1 L", 1000, 170),
    seed("sal", "Sal", GRAM, PANTRY, "Sal fina 1 kg", 1000, 35),
    seed("pimenton", "Pimentón dulce", GRAM, PANTRY, "Pimentón dulce 75 g", 75, 110),
    seed("comino", "Comino molido", GRAM, PANTRY, "Comino molido 50 g", 50, 100),
    seed("oregano", "Orégano", GRAM, PANTRY, "Orégano 15 g", 15, 95),
    seed("vinagre", "Vinagre de vino", MILLILITER, PANTRY, "Vinagre de vino 1 L", 1000, 70, setOf(SULPHITES)),
    seed("caldo_verduras", "Caldo de verduras", MILLILITER, PANTRY, "Caldo de verduras 1 L", 1000, 110, setOf(CELERY)),
    seed("caldo_pollo", "Caldo de pollo", MILLILITER, PANTRY, "Caldo de pollo 1 L", 1000, 120, setOf(CELERY), setOf(MEAT)),
    seed("miel", "Miel", GRAM, PANTRY, "Miel 500 g", 500, 350, traits = setOf(HONEY)),
    seed("crema_cacahuete", "Crema de cacahuete", GRAM, PANTRY, "Crema de cacahuete 350 g", 350, 230, setOf(PEANUTS)),
    seed("nueces", "Nueces peladas", GRAM, PANTRY, "Nueces peladas 200 g", 200, 250, setOf(TREE_NUTS)),
)
