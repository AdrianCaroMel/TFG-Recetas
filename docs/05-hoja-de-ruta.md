# Hoja de ruta

## Hecho (iteración 1–2)

- [x] Estructura del proyecto Android (Gradle, módulos `core` y `app`, CI en GitHub Actions)
- [x] Modelo de dominio y catálogo de ejemplo
- [x] Filtro por restricciones y planificador con presupuesto
- [x] Tests unitarios de la lógica
- [x] Pantallas: bienvenida, supermercados, restricciones, presupuesto, menú, lista de la compra, receta
- [x] Guardado local del perfil

## Siguientes pasos

### Funcionalidad
- [ ] Pantalla para excluir ingredientes que no gustan (la lógica ya lo soporta).
- [ ] Cambiar un plato concreto del menú por otro (manteniendo el presupuesto).
- [ ] Guardar las casillas de «comprado» de la lista de la compra.
- [ ] Dividir la lista de la compra por semanas (frescos) y despensa (una vez al mes).
- [ ] Aprovechamiento de sobras: cocinar más raciones y repartirlas en varios huecos.
- [ ] Indicar ingredientes que el usuario ya tiene en casa (despensa).
- [ ] Información nutricional básica y equilibrio semanal (legumbres, pescado, verdura).

### Datos
- [ ] Ampliar el catálogo de recetas (objetivo: 100+), incluyendo más opciones sin gluten y veganas.
- [ ] **Precios reales**. Opciones a estudiar:
  - APIs públicas/no oficiales de las tiendas online de las cadenas.
  - *Web scraping* periódico desde un pequeño backend que publique un JSON de precios
    (revisar condiciones de uso de cada web).
  - Introducción manual o colaborativa de precios por los usuarios.
- [ ] Guardar el catálogo en **Room** y actualizarlo en segundo plano (WorkManager).
- [ ] Ubicación: mostrar solo supermercados cercanos (permiso de localización + mapa).

### Calidad y memoria
- [ ] Tests de interfaz con Compose Testing.
- [ ] Comparar la heurística con una solución óptima (programación lineal entera) en casos pequeños.
- [ ] Pruebas con usuarios reales y encuesta de usabilidad (SUS).
- [ ] Capturas de pantalla y vídeo de demostración para la defensa.
