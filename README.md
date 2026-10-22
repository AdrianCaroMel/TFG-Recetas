# TFG-Recetas · *Cesta Justa*

Repositorio del Trabajo de Fin de Grado: una **aplicación Android** que, a partir de

1. los **supermercados** en los que el usuario puede o quiere comprar (Mercadona, Dia, Lidl, Aldi, Alcampo, Carrefour, Eroski, Consum…),
2. sus **restricciones alimentarias** (alergias, intolerancias, dieta vegana/vegetariana/pescetariana, sin cerdo…),
3. su **presupuesto mensual** para comida, el **número de personas** y las **comidas** que quiere planificar (desayuno, comida, cena),

genera un **menú para todo el mes** con recetas que puede cocinar **sin pasarse del presupuesto**, junto con la **lista de la compra** agrupada por supermercado.

> «Cesta Justa» es un nombre provisional; se cambia en `app/src/main/res/values/strings.xml`.

## Estado actual (v0.1.0)

| Parte | Estado |
|---|---|
| Modelo de dominio (supermercados, alérgenos UE, dietas, ingredientes, productos, recetas) | ✅ |
| Catálogo de ejemplo: 67 ingredientes × 8 supermercados, 42 recetas españolas | ✅ (precios **ilustrativos**) |
| Filtro de recetas por restricciones | ✅ |
| Planificador mensual con control de presupuesto (envases completos, variedad, reparación) | ✅ |
| Tests unitarios de la lógica (18 tests) | ✅ |
| App Android: asistente inicial (3 pasos), menú mensual, lista de la compra, detalle de receta | ✅ |
| Persistencia local del perfil (DataStore) | ✅ |
| Precios reales de los supermercados | ⏳ pendiente (ver hoja de ruta) |

## Estructura del proyecto

```
TFG-Recetas/
├── core/   → Lógica de negocio en Kotlin puro (sin Android). Se puede probar con tests JUnit.
│   ├── model/    Modelos: Supermarket, Allergen, Diet, Ingredient, Product, Recipe, UserProfile, Catalog
│   ├── data/     Catálogo de ejemplo (ingredientes, precios, recetas)
│   └── planner/  PriceResolver, RecipeFilter, MealPlanner (algoritmo), MealPlan/ShoppingList
├── app/    → Aplicación Android (Kotlin + Jetpack Compose + Material 3)
│   ├── data/     ProfileRepository (DataStore)
│   └── ui/       ViewModel, navegación y pantallas (onboarding, plan, receta)
└── docs/   → Documentación para la memoria del TFG
```

## Cómo abrirlo en Android Studio

1. *File → Open…* y seleccionar la carpeta del repositorio.
2. Android Studio descargará Gradle y las dependencias automáticamente.
3. Ejecutar la configuración **app** en un emulador o móvil (Android 8.0 / API 26 o superior).

Desde terminal:

```bash
./gradlew :core:test          # tests de la lógica
./gradlew :app:assembleDebug  # genera app/build/outputs/apk/debug/app-debug.apk
```

Cada *push* a GitHub ejecuta además el workflow **Android CI** (`.github/workflows/android.yml`), que pasa los tests y compila el APK. El APK se puede descargar desde la pestaña *Actions* del repositorio → la ejecución correspondiente → *Artifacts*.

## Tecnologías

- Kotlin 2.1, Android Gradle Plugin 8.9, minSdk 26 / targetSdk 35
- Jetpack Compose + Material 3, Navigation Compose, ViewModel + StateFlow
- Jetpack DataStore para guardar las preferencias
- JUnit / kotlin-test para los tests

## Documentación

- [Anteproyecto](docs/01-anteproyecto.md): problema, objetivos, alcance y metodología
- [Requisitos](docs/02-requisitos.md): requisitos funcionales, no funcionales y casos de uso
- [Arquitectura](docs/03-arquitectura.md): módulos, capas y modelo de datos
- [Algoritmo de planificación](docs/04-algoritmo.md): formulación del problema y solución
- [Hoja de ruta](docs/05-hoja-de-ruta.md): siguientes pasos y trabajo futuro
