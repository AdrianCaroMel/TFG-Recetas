# Anteproyecto

## 1. Contexto y problema

Planificar qué comer durante un mes ajustándose a un presupuesto es una tarea pesada: hay que
elegir recetas, calcular cantidades, comparar precios entre supermercados y, además, respetar
las restricciones alimentarias de quien va a comer (alergias, intolerancias o dietas como la
vegana o la vegetariana). En la práctica, mucha gente acaba repitiendo siempre los mismos platos,
gastando más de lo previsto o desperdiciando comida.

Las aplicaciones existentes suelen cubrir **una parte** del problema: recetarios, planificadores
de menús semanales, listas de la compra o comparadores de precios de supermercados. Este proyecto
propone **unir las tres piezas** —restricciones, precios por supermercado y presupuesto— en una
única aplicación móvil.

## 2. Objetivo general

Desarrollar una aplicación Android que, a partir de la situación del usuario (supermercados
disponibles, restricciones alimentarias, presupuesto mensual y número de personas), le proponga
un **menú mensual variado** y la **lista de la compra** correspondiente, garantizando que el coste
total **no supera el presupuesto**.

## 3. Objetivos específicos

1. Modelar el dominio: supermercados, productos con precio y formato de envase, ingredientes con
   alérgenos (los 14 del Reglamento UE 1169/2011) y rasgos dietéticos, y recetas.
2. Implementar un filtro de recetas compatible con dietas, alergias, intolerancias y exclusiones.
3. Diseñar e implementar un **algoritmo de planificación** que reparta recetas en los días del mes,
   maximice la variedad y respete el presupuesto teniendo en cuenta que se compran envases completos.
4. Generar una lista de la compra agrupada por supermercado eligiendo la opción más barata.
5. Construir una interfaz móvil sencilla (asistente inicial, menú, lista de la compra, detalle de receta).
6. Validar la lógica mediante pruebas automáticas.

## 4. Alcance

**Incluido en la primera versión**

- Selección de supermercados, restricciones, presupuesto, personas y comidas a planificar.
- Menú de 30 días con desayuno, comida y/o cena.
- Coste estimado por plato y coste total, con aviso si el presupuesto no es suficiente.
- Lista de la compra por supermercado con casillas para marcar lo comprado.
- Catálogo de ejemplo con precios aproximados.

**Fuera de alcance (trabajo futuro)**

- Obtención automática de precios reales y actualizados de cada cadena.
- Geolocalización de supermercados cercanos.
- Información nutricional y objetivos calóricos.
- Gestión de despensa (lo que el usuario ya tiene en casa) y caducidad de productos frescos.
- Cuentas de usuario y sincronización en la nube.

## 5. Metodología

Desarrollo iterativo e incremental (inspirado en Scrum, adaptado a una sola persona) con
iteraciones cortas. Cada iteración entrega una versión funcional:

| Iteración | Contenido |
|---|---|
| 1 | Modelo de dominio, catálogo de ejemplo, filtro y planificador con tests |
| 2 | Interfaz Android: asistente inicial, menú y lista de la compra |
| 3 | Mejoras del algoritmo (sobras, compras semanales), ampliación del catálogo |
| 4 | Fuente de precios reales, pruebas con usuarios y memoria |

## 6. Herramientas

- **Android Studio**, Kotlin, Jetpack Compose, Gradle.
- **Git y GitHub** para control de versiones; **GitHub Actions** para integración continua.
- JUnit para pruebas unitarias.
