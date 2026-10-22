# 04 · Planificación

Planificación orientativa (ajustar con las fechas de entrega que dé el centro).

| Fase | Fechas aprox. | Tareas | Estado |
|---|---|---|---|
| 1. Idea y análisis | oct 2026 | Definir la idea, objetivos, requisitos y modelo de datos | Hecho |
| 2. Prototipo funcional | oct-nov 2026 | Lógica del planificador, CSV de datos, pantallas básicas, tests | Hecho (v0.1) |
| 3. Probar en Android Studio | nov 2026 | Compilar, ejecutar en emulador y móvil, corregir fallos de interfaz | Pendiente |
| 4. Datos reales | nov-dic 2026 | Revisar precios en las webs de los supermercados, ampliar a 50+ recetas | Pendiente |
| 5. Mejoras de la app | ene-mar 2027 | Cambiar una receta del plan, favoritas/excluidas, compartir lista de la compra, mejor diseño (RecyclerView, tarjetas) | Pendiente |
| 6. Opcional | mar-abr 2027 | Base de datos SQLite/Room, información nutricional, desayunos, ubicación | Pendiente |
| 7. Memoria y defensa | abr-jun 2027 | Redactar la memoria, manual de usuario, capturas, presentación | Pendiente |

## Próximos pasos inmediatos

1. ~~Abrir el proyecto en Android Studio y ejecutarlo en el emulador.~~ Hecho.
2. ~~Pasar los tests (`app/src/test`).~~ Hecho (13 tests, también en GitHub Actions).
3. **Pendiente (pedido por Adrián): mostrar dónde comprar cada ingrediente.**
   En el detalle de la receta (y/o en otro apartado), junto a cada ingrediente indicar
   en qué supermercado conviene comprarlo y a qué precio, para comprar con la mejor
   eficiencia. Idea: reutilizar `CestaCompra.mejorSupermercado(producto)`, que ya elige
   el más barato entre los supermercados del usuario, y mostrarlo como etiqueta
   («Lidl · 1,15 €») en cada fila de `item_ingrediente.xml`.
4. Revisar los precios de los CSV con las webs de los supermercados.
5. Decidir qué otras mejoras de la fase 5 entran en el TFG (favoritas, excluir recetas...).
