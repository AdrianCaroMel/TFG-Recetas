package es.tfg.recetas.core.data

import es.tfg.recetas.core.model.MealType
import es.tfg.recetas.core.model.MealType.BREAKFAST
import es.tfg.recetas.core.model.MealType.DINNER
import es.tfg.recetas.core.model.MealType.LUNCH
import es.tfg.recetas.core.model.Recipe
import es.tfg.recetas.core.model.RecipeIngredient

private fun recipe(
    id: String,
    name: String,
    description: String,
    meals: Set<MealType>,
    minutes: Int,
    ingredients: List<Pair<String, Number>>,
    steps: List<String>,
    servings: Int = 4,
) = Recipe(
    id = id,
    name = name,
    description = description,
    mealTypes = meals,
    servings = servings,
    prepMinutes = minutes,
    ingredients = ingredients.map { (ingredientId, qty) -> RecipeIngredient(ingredientId, qty.toDouble()) },
    steps = steps,
)

private val LUNCH_AND_DINNER = setOf(LUNCH, DINNER)

/** Recetas de ejemplo. Cantidades para 4 raciones salvo que se indique lo contrario. */
internal val sampleRecipes: List<Recipe> = listOf(
    // ---------- Comidas y cenas ----------
    recipe(
        "lentejas_verduras", "Lentejas estofadas con verduras",
        "Guiso de legumbre clásico, completo y muy económico.",
        setOf(LUNCH), 50,
        listOf("lentejas" to 300, "patata" to 300, "zanahoria" to 150, "cebolla" to 100, "pimiento_verde" to 80,
            "ajo" to 10, "tomate_triturado" to 100, "pimenton" to 5, "aceite_oliva" to 30, "sal" to 5),
        listOf(
            "Pica la cebolla, el pimiento y el ajo y sofríelos en el aceite 8 minutos.",
            "Añade el tomate y el pimentón y rehoga 2 minutos.",
            "Incorpora las lentejas lavadas, la zanahoria y la patata troceadas y cubre con agua.",
            "Cuece a fuego medio unos 40 minutos y ajusta de sal.",
        ),
    ),
    recipe(
        "lentejas_chorizo", "Lentejas con chorizo",
        "Las lentejas de toda la vida con el toque del chorizo.",
        setOf(LUNCH), 50,
        listOf("lentejas" to 300, "chorizo" to 120, "patata" to 300, "zanahoria" to 100, "cebolla" to 100,
            "ajo" to 10, "pimenton" to 5, "aceite_oliva" to 20, "sal" to 5),
        listOf(
            "Sofríe la cebolla y el ajo picados en el aceite.",
            "Añade el chorizo en rodajas y el pimentón y rehoga un minuto.",
            "Incorpora lentejas, patata y zanahoria, cubre con agua y cuece 40 minutos.",
        ),
    ),
    recipe(
        "garbanzos_espinacas", "Garbanzos con espinacas",
        "Potaje rápido de inspiración andaluza.",
        LUNCH_AND_DINNER, 25,
        listOf("garbanzos" to 800, "espinacas" to 400, "ajo" to 10, "pimenton" to 5, "comino" to 3,
            "aceite_oliva" to 40, "sal" to 5),
        listOf(
            "Dora el ajo laminado en el aceite.",
            "Añade las espinacas y cocina hasta que pierdan el agua.",
            "Agrega pimentón, comino y los garbanzos escurridos con un vaso de agua.",
            "Cocina 10 minutos y sirve.",
        ),
    ),
    recipe(
        "arroz_cubana", "Arroz a la cubana",
        "Arroz blanco con tomate frito, huevo y plátano.",
        LUNCH_AND_DINNER, 30,
        listOf("arroz" to 320, "tomate_frito" to 400, "huevos" to 4, "platano" to 400, "aceite_girasol" to 80, "sal" to 5),
        listOf(
            "Cuece el arroz en agua con sal 18 minutos y escúrrelo.",
            "Fríe los plátanos partidos a lo largo y después los huevos.",
            "Sirve el arroz con el tomate caliente, el huevo y el plátano.",
        ),
    ),
    recipe(
        "macarrones_atun", "Macarrones con tomate y atún",
        "Pasta sencilla que gusta a todo el mundo.",
        setOf(LUNCH), 20,
        listOf("macarrones" to 400, "tomate_frito" to 400, "atun" to 160, "cebolla" to 100, "queso_rallado" to 60, "aceite_oliva" to 20),
        listOf(
            "Cuece los macarrones según el paquete.",
            "Pocha la cebolla, añade el tomate y el atún escurrido.",
            "Mezcla con la pasta y sirve con queso rallado.",
        ),
    ),
    recipe(
        "macarrones_bolonesa", "Macarrones a la boloñesa",
        "Pasta con salsa de carne y tomate.",
        setOf(LUNCH), 35,
        listOf("macarrones" to 400, "carne_picada" to 400, "tomate_triturado" to 400, "cebolla" to 100, "zanahoria" to 80,
            "ajo" to 10, "aceite_oliva" to 30, "oregano" to 2),
        listOf(
            "Sofríe cebolla, zanahoria y ajo picados.",
            "Añade la carne y dórala; incorpora el tomate y el orégano y cuece 20 minutos.",
            "Mezcla con los macarrones cocidos.",
        ),
    ),
    recipe(
        "espaguetis_verduras", "Espaguetis con verduras",
        "Pasta vegana con verduras de temporada.",
        LUNCH_AND_DINNER, 25,
        listOf("espaguetis" to 400, "calabacin" to 300, "pimiento_rojo" to 150, "champinones" to 250, "ajo" to 10,
            "tomate_triturado" to 200, "aceite_oliva" to 40),
        listOf(
            "Saltea el ajo, el pimiento, el calabacín y los champiñones.",
            "Añade el tomate y cocina 10 minutos.",
            "Mezcla con los espaguetis cocidos.",
        ),
    ),
    recipe(
        "espaguetis_gambas", "Espaguetis con gambas al ajillo",
        "Pasta rápida con gambas, ajo y aceite.",
        LUNCH_AND_DINNER, 20,
        listOf("espaguetis" to 400, "gambas" to 300, "ajo" to 15, "aceite_oliva" to 50, "sal" to 5),
        listOf(
            "Cuece la pasta.",
            "Dora el ajo laminado en el aceite y saltea las gambas 2 minutos.",
            "Mezcla con la pasta escurrida.",
        ),
    ),
    recipe(
        "tortilla_patatas", "Tortilla de patatas",
        "El clásico de la cocina española.",
        LUNCH_AND_DINNER, 40,
        listOf("patata" to 800, "huevos" to 6, "cebolla" to 150, "aceite_girasol" to 200, "sal" to 5),
        listOf(
            "Pela y corta las patatas y la cebolla y póchalas a fuego suave en el aceite.",
            "Escúrrelas y mézclalas con los huevos batidos y la sal.",
            "Cuaja la tortilla por ambos lados en una sartén.",
        ),
    ),
    recipe(
        "pollo_horno", "Pollo al horno con patatas",
        "Contramuslos asados con patatas panaderas.",
        setOf(LUNCH), 60,
        listOf("pollo_muslos" to 1000, "patata" to 800, "cebolla" to 150, "limon" to 100, "ajo" to 15,
            "aceite_oliva" to 50, "oregano" to 2, "sal" to 8),
        listOf(
            "Corta las patatas y la cebolla en rodajas y colócalas en una bandeja.",
            "Pon encima el pollo salpimentado con ajo, limón y orégano.",
            "Hornea a 200 °C durante 50 minutos.",
        ),
    ),
    recipe(
        "pollo_ensalada", "Pechuga a la plancha con ensalada",
        "Cena ligera y rápida.",
        LUNCH_AND_DINNER, 15,
        listOf("pollo_pechuga" to 600, "lechuga" to 1, "tomate" to 300, "pepino" to 200, "aceite_oliva" to 40,
            "vinagre" to 15, "sal" to 5),
        listOf(
            "Haz las pechugas a la plancha con un poco de sal.",
            "Prepara una ensalada con lechuga, tomate y pepino y alíñala.",
        ),
    ),
    recipe(
        "merluza_horno", "Merluza al horno con verduras",
        "Pescado blanco asado sobre cama de patata y pimiento.",
        LUNCH_AND_DINNER, 40,
        listOf("merluza" to 600, "patata" to 600, "cebolla" to 150, "pimiento_rojo" to 150, "ajo" to 10, "aceite_oliva" to 50, "sal" to 5),
        listOf(
            "Hornea la patata, la cebolla y el pimiento en láminas 25 minutos a 200 °C.",
            "Coloca encima la merluza con ajo picado y aceite y hornea 12 minutos más.",
        ),
    ),
    recipe(
        "salmon_brocoli", "Salmón con brócoli",
        "Pescado azul con verdura al vapor.",
        setOf(DINNER), 20,
        listOf("salmon" to 500, "brocoli" to 500, "limon" to 100, "aceite_oliva" to 30, "sal" to 5),
        listOf(
            "Cuece el brócoli al vapor 6 minutos.",
            "Haz el salmón a la plancha y sírvelo con limón y el brócoli.",
        ),
    ),
    recipe(
        "crema_calabacin", "Crema de calabacín",
        "Crema suave y vegana.",
        setOf(DINNER), 30,
        listOf("calabacin" to 800, "patata" to 200, "cebolla" to 150, "caldo_verduras" to 500, "aceite_oliva" to 30, "sal" to 5),
        listOf(
            "Pocha la cebolla en el aceite.",
            "Añade calabacín y patata troceados y el caldo; cuece 20 minutos.",
            "Tritura hasta obtener una crema fina.",
        ),
    ),
    recipe(
        "crema_calabaza", "Crema de calabaza y zanahoria",
        "Crema de otoño dulce y reconfortante.",
        setOf(DINNER), 35,
        listOf("calabaza" to 800, "zanahoria" to 200, "cebolla" to 150, "caldo_verduras" to 500, "aceite_oliva" to 30, "sal" to 5),
        listOf(
            "Sofríe la cebolla.",
            "Añade calabaza y zanahoria troceadas y el caldo; cuece 25 minutos.",
            "Tritura y sirve.",
        ),
    ),
    recipe(
        "pisto_huevo", "Pisto con huevo",
        "Verduras guisadas con tomate y huevo a la plancha.",
        LUNCH_AND_DINNER, 45,
        listOf("calabacin" to 400, "berenjena" to 300, "pimiento_rojo" to 150, "pimiento_verde" to 150, "cebolla" to 200,
            "tomate_triturado" to 400, "huevos" to 4, "aceite_oliva" to 60, "sal" to 5),
        listOf(
            "Pica todas las verduras en dados.",
            "Pocha la cebolla y los pimientos, añade berenjena y calabacín y cocina 15 minutos.",
            "Agrega el tomate y cocina 15 minutos más.",
            "Sirve con un huevo a la plancha por persona.",
        ),
    ),
    recipe(
        "gazpacho_tostas", "Gazpacho con picatostes",
        "Sopa fría andaluza.",
        setOf(DINNER), 15,
        listOf("tomate" to 1000, "pepino" to 200, "pimiento_verde" to 100, "ajo" to 5, "pan_barra" to 100,
            "aceite_oliva" to 60, "vinagre" to 20, "sal" to 5),
        listOf(
            "Tritura tomate, pepino, pimiento, ajo y la mitad del pan con aceite, vinagre y sal.",
            "Enfría en la nevera y sirve con el resto del pan tostado en dados.",
        ),
    ),
    recipe(
        "ensalada_garbanzos", "Ensalada de garbanzos",
        "Ensalada completa de legumbre, fresca y vegana.",
        LUNCH_AND_DINNER, 10,
        listOf("garbanzos" to 800, "tomate" to 300, "pepino" to 200, "pimiento_rojo" to 150, "cebolla" to 80,
            "aceite_oliva" to 40, "vinagre" to 15, "sal" to 4),
        listOf(
            "Escurre y lava los garbanzos.",
            "Pica las verduras y mézclalas con los garbanzos.",
            "Aliña con aceite, vinagre y sal.",
        ),
    ),
    recipe(
        "arroz_pollo", "Arroz con pollo",
        "Arroz meloso de domingo.",
        setOf(LUNCH), 45,
        listOf("arroz" to 320, "pollo_muslos" to 600, "pimiento_rojo" to 150, "guisantes" to 150, "tomate_triturado" to 200,
            "caldo_pollo" to 900, "ajo" to 10, "pimenton" to 5, "aceite_oliva" to 40, "sal" to 5),
        listOf(
            "Dora el pollo troceado y resérvalo.",
            "Sofríe el pimiento y el ajo, añade el tomate y el pimentón.",
            "Agrega el arroz, el pollo, los guisantes y el caldo caliente y cuece 18 minutos.",
        ),
    ),
    recipe(
        "paella_verduras", "Paella de verduras",
        "Arroz vegano con judías, pimiento y tomate.",
        setOf(LUNCH), 45,
        listOf("arroz" to 320, "judias_verdes" to 200, "pimiento_rojo" to 150, "tomate_triturado" to 150,
            "caldo_verduras" to 900, "ajo" to 10, "pimenton" to 5, "aceite_oliva" to 50, "sal" to 5),
        listOf(
            "Sofríe las judías y el pimiento en la paella.",
            "Añade ajo, tomate y pimentón.",
            "Incorpora el arroz y el caldo caliente y cuece 18 minutos sin remover.",
        ),
    ),
    recipe(
        "arroz_gambas", "Arroz con gambas",
        "Arroz marinero sencillo.",
        setOf(LUNCH), 40,
        listOf("arroz" to 320, "gambas" to 300, "pimiento_rojo" to 100, "tomate_triturado" to 150,
            "caldo_verduras" to 900, "ajo" to 10, "pimenton" to 3, "aceite_oliva" to 40, "sal" to 5),
        listOf(
            "Saltea las gambas y resérvalas.",
            "Sofríe pimiento, ajo y tomate; añade el arroz y el caldo.",
            "A los 15 minutos incorpora las gambas y deja reposar.",
        ),
    ),
    recipe(
        "albondigas", "Albóndigas en salsa de tomate",
        "Albóndigas caseras guisadas.",
        setOf(LUNCH), 50,
        listOf("carne_picada" to 500, "huevos" to 1, "pan_rallado" to 50, "cebolla" to 150, "tomate_triturado" to 400,
            "ajo" to 10, "harina" to 30, "aceite_oliva" to 60, "sal" to 5),
        listOf(
            "Mezcla la carne con el huevo, el pan rallado, ajo picado y sal; forma bolas.",
            "Pásalas por harina y dóralas en el aceite.",
            "Haz una salsa con la cebolla y el tomate y guisa las albóndigas 20 minutos.",
        ),
    ),
    recipe(
        "lomo_pimientos", "Lomo con pimientos y patatas",
        "Filetes de lomo a la plancha con guarnición.",
        LUNCH_AND_DINNER, 35,
        listOf("lomo_cerdo" to 600, "pimiento_verde" to 300, "pimiento_rojo" to 200, "patata" to 400,
            "aceite_oliva" to 50, "ajo" to 10, "sal" to 5),
        listOf(
            "Fríe o asa las patatas en gajos.",
            "Confita los pimientos en tiras con el ajo.",
            "Haz el lomo a la plancha y sirve con la guarnición.",
        ),
    ),
    recipe(
        "tofu_salteado", "Tofu salteado con verduras y arroz",
        "Salteado vegano de inspiración asiática.",
        LUNCH_AND_DINNER, 30,
        listOf("tofu" to 400, "arroz" to 240, "brocoli" to 300, "zanahoria" to 150, "pimiento_rojo" to 150,
            "ajo" to 10, "aceite_oliva" to 30, "sal" to 4),
        listOf(
            "Cuece el arroz.",
            "Dora el tofu en dados y resérvalo.",
            "Saltea las verduras con el ajo, añade el tofu y sirve con el arroz.",
        ),
    ),
    recipe(
        "burritos_alubias", "Burritos de alubias",
        "Tortillas rellenas de alubias, maíz y verduras.",
        setOf(DINNER), 25,
        listOf("tortillas_trigo" to 8, "alubias" to 800, "maiz" to 280, "pimiento_rojo" to 150, "cebolla" to 100,
            "tomate" to 200, "comino" to 3, "aceite_oliva" to 20, "sal" to 4),
        listOf(
            "Sofríe la cebolla y el pimiento; añade alubias, maíz y comino.",
            "Rellena las tortillas con la mezcla y tomate picado.",
            "Enróllalas y dóralas en la sartén.",
        ),
    ),
    recipe(
        "fajitas_pollo", "Fajitas de pollo",
        "Tiras de pollo con pimientos en tortilla.",
        setOf(DINNER), 25,
        listOf("tortillas_trigo" to 8, "pollo_pechuga" to 500, "pimiento_rojo" to 150, "pimiento_verde" to 150,
            "cebolla" to 150, "aceite_oliva" to 30, "pimenton" to 3, "sal" to 4),
        listOf(
            "Corta el pollo y las verduras en tiras.",
            "Saltéalos con pimentón y sal.",
            "Sirve en las tortillas calientes.",
        ),
    ),
    recipe(
        "huevos_rotos", "Huevos rotos con jamón",
        "Patatas fritas con huevo y taquitos de jamón.",
        setOf(DINNER), 30,
        listOf("patata" to 800, "huevos" to 4, "jamon" to 150, "aceite_girasol" to 200, "sal" to 5),
        listOf(
            "Fríe las patatas cortadas finas.",
            "Fríe los huevos y colócalos sobre las patatas con el jamón.",
            "Rompe los huevos al servir.",
        ),
    ),
    recipe(
        "revuelto_champinones", "Revuelto de champiñones con tostada",
        "Cena rápida vegetariana.",
        setOf(DINNER), 15,
        listOf("huevos" to 6, "champinones" to 400, "ajo" to 10, "aceite_oliva" to 30, "pan_barra" to 150, "sal" to 4),
        listOf(
            "Saltea los champiñones con el ajo.",
            "Añade los huevos batidos y remueve hasta cuajar.",
            "Sirve con pan tostado.",
        ),
    ),
    recipe(
        "judias_patata", "Judías verdes con patata",
        "Verdura hervida con refrito de ajo y pimentón.",
        setOf(DINNER), 25,
        listOf("judias_verdes" to 600, "patata" to 500, "ajo" to 10, "pimenton" to 3, "aceite_oliva" to 40, "sal" to 5),
        listOf(
            "Cuece las judías y la patata troceada 15 minutos.",
            "Haz un refrito de ajo y pimentón y viértelo por encima.",
        ),
    ),
    recipe(
        "ensalada_pasta", "Ensalada de pasta",
        "Pasta fría con atún, huevo y maíz.",
        LUNCH_AND_DINNER, 20,
        listOf("macarrones" to 300, "atun" to 160, "huevos" to 3, "tomate" to 300, "maiz" to 140, "aceite_oliva" to 40, "sal" to 4),
        listOf(
            "Cuece la pasta y los huevos; enfría.",
            "Mezcla con el atún, el tomate picado y el maíz.",
            "Aliña con aceite y sal.",
        ),
    ),
    recipe(
        "cuscus_verduras", "Cuscús con verduras y garbanzos",
        "Plato único de inspiración magrebí.",
        LUNCH_AND_DINNER, 25,
        listOf("cuscus" to 300, "calabacin" to 200, "zanahoria" to 150, "pimiento_rojo" to 150, "garbanzos" to 400,
            "caldo_verduras" to 400, "comino" to 3, "aceite_oliva" to 40, "sal" to 4),
        listOf(
            "Saltea las verduras en dados con el comino.",
            "Añade los garbanzos y calienta.",
            "Hidrata el cuscús con el caldo caliente 5 minutos y mezcla.",
        ),
    ),
    recipe(
        "quinoa_salteada", "Quinoa salteada con verduras",
        "Plato vegano y sin gluten.",
        LUNCH_AND_DINNER, 25,
        listOf("quinoa" to 300, "brocoli" to 300, "pimiento_rojo" to 150, "cebolla" to 100, "champinones" to 250,
            "aceite_oliva" to 30, "sal" to 4),
        listOf(
            "Cuece la quinoa 15 minutos y escúrrela.",
            "Saltea las verduras troceadas.",
            "Mezcla todo y rectifica de sal.",
        ),
    ),
    recipe(
        "berenjenas_rellenas", "Berenjenas rellenas de carne",
        "Berenjenas gratinadas con relleno de carne y tomate.",
        setOf(LUNCH), 55,
        listOf("berenjena" to 800, "carne_picada" to 300, "cebolla" to 100, "tomate_triturado" to 200,
            "queso_rallado" to 80, "aceite_oliva" to 30, "sal" to 5),
        listOf(
            "Asa las berenjenas partidas por la mitad 20 minutos y vacíalas.",
            "Sofríe la cebolla, la carne, la pulpa de berenjena y el tomate.",
            "Rellena, cubre con queso y gratina 10 minutos.",
        ),
    ),
    recipe(
        "chili_sin_carne", "Chili sin carne con arroz",
        "Chili vegano con soja texturizada y alubias.",
        setOf(LUNCH), 40,
        listOf("soja_texturizada" to 150, "alubias" to 800, "tomate_triturado" to 400, "cebolla" to 150,
            "pimiento_rojo" to 150, "maiz" to 140, "comino" to 3, "pimenton" to 5, "aceite_oliva" to 30, "arroz" to 240, "sal" to 5),
        listOf(
            "Hidrata la soja en agua caliente 10 minutos.",
            "Sofríe cebolla y pimiento, añade la soja escurrida y las especias.",
            "Incorpora tomate, alubias y maíz y cuece 20 minutos. Sirve con arroz blanco.",
        ),
    ),

    // ---------- Desayunos ----------
    recipe(
        "tostadas_tomate", "Tostadas con tomate y aceite",
        "El desayuno mediterráneo por excelencia.",
        setOf(BREAKFAST), 5,
        listOf("pan_barra" to 250, "tomate" to 200, "aceite_oliva" to 30, "sal" to 2),
        listOf("Tuesta el pan.", "Úntalo con tomate rallado, aceite y una pizca de sal."),
    ),
    recipe(
        "gachas_avena", "Gachas de avena con plátano y miel",
        "Porridge caliente y saciante.",
        setOf(BREAKFAST), 10,
        listOf("avena" to 160, "leche" to 800, "platano" to 300, "miel" to 30),
        listOf("Cuece la avena en la leche 5 minutos removiendo.", "Sirve con plátano en rodajas y miel."),
    ),
    recipe(
        "porridge_soja", "Porridge vegano con nueces",
        "Avena con bebida de soja, plátano y nueces.",
        setOf(BREAKFAST), 10,
        listOf("avena" to 160, "bebida_soja" to 800, "platano" to 300, "nueces" to 40),
        listOf("Cuece la avena en la bebida de soja 5 minutos.", "Sirve con plátano y nueces troceadas."),
    ),
    recipe(
        "yogur_fruta", "Yogur con manzana y nueces",
        "Desayuno fresco sin gluten.",
        setOf(BREAKFAST), 5,
        listOf("yogur" to 500, "manzana" to 300, "nueces" to 60),
        listOf("Trocea la manzana.", "Sírvela sobre el yogur con las nueces."),
    ),
    recipe(
        "tostadas_cacahuete", "Tostadas con crema de cacahuete y plátano",
        "Energía para empezar el día.",
        setOf(BREAKFAST), 5,
        listOf("pan_molde" to 200, "crema_cacahuete" to 60, "platano" to 300),
        listOf("Tuesta el pan.", "Unta la crema de cacahuete y cubre con plátano en rodajas."),
    ),
    recipe(
        "huevos_revueltos_tostada", "Huevos revueltos con tostada",
        "Desayuno salado rico en proteína.",
        setOf(BREAKFAST), 10,
        listOf("huevos" to 6, "pan_molde" to 160, "aceite_oliva" to 15, "sal" to 2),
        listOf("Bate los huevos con sal y cuájalos en la sartén removiendo.", "Sirve con pan tostado."),
    ),
    recipe(
        "zumo_tostadas", "Zumo de naranja y tostadas",
        "Desayuno vegano de toda la vida.",
        setOf(BREAKFAST), 10,
        listOf("naranja" to 1000, "pan_barra" to 200, "aceite_oliva" to 30),
        listOf("Exprime las naranjas.", "Tuesta el pan y aliña con aceite."),
    ),
    recipe(
        "macedonia", "Macedonia de frutas",
        "Fruta fresca variada; apta para casi todas las dietas.",
        setOf(BREAKFAST), 10,
        listOf("platano" to 300, "manzana" to 300, "naranja" to 400, "limon" to 50),
        listOf("Pela y trocea la fruta.", "Mezcla con el zumo de limón para que no se oxide."),
    ),
)
