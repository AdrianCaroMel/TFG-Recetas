# TFG-Recetas

Trabajo de Fin de Grado de **Adrián Caro Melgosa** — Grado Superior en Desarrollo de Aplicaciones Multiplataforma (DAM), 2025-2027.

**PickEats** es una aplicación Android que, a partir de los **supermercados** donde compras, tus **restricciones alimenticias** y tu **presupuesto mensual**, te genera un **plan de comidas y cenas para todo el mes** con su **lista de la compra**, sin pasarse nunca del dinero que tienes.

## Cómo funciona

1. **Supermercados**: el usuario marca dónde puede comprar (Mercadona, Dia, Lidl, Aldi, Alcampo, Carrefour).
2. **Restricciones**: alergias e intolerancias (gluten, lactosa, huevo, frutos secos, pescado, marisco, soja) y dietas (vegetariana, vegana).
3. **Presupuesto**: euros al mes, número de personas, si quiere planificar comida, cena o ambas y si ya tiene en casa aceite, sal y pimentón.
4. La app calcula un **plan de 30 días** y una **lista de la compra agrupada por supermercado**, comprando cada producto donde sale más barato.

Si el presupuesto no da para el mes entero, la app lo avisa y dice cuánto haría falta.

## Estado actual (v0.1)

- [x] Flujo completo de pantallas (inicio → supermercados → restricciones → presupuesto → plan → receta / lista de la compra)
- [x] 42 productos con precio por supermercado y 35 recetas en CSV (`app/src/main/assets`)
- [x] Algoritmo de planificación mensual con control de presupuesto y variedad
- [x] Tests unitarios de la lógica (`app/src/test`)
- [ ] Probar en emulador / móvil real y pulir la interfaz
- [ ] Más recetas y precios reales actualizados
- [ ] Ver [docs/04-planificacion.md](docs/04-planificacion.md) para lo que falta

## Estructura

```
app/src/main/
├── assets/                 productos.csv, recetas.csv, ingredientes.csv
├── java/com/adriancaro/tfgrecetas/
│   ├── modelo/             Producto, Receta, PerfilUsuario, PlanMensual...
│   ├── datos/              Carga de CSV y guardado del perfil (SharedPreferences)
│   ├── logica/             FiltroRecetas, CestaCompra, PlanificadorMensual
│   └── ui/                 Activities y adaptador de la lista
└── res/                    layouts, textos, colores, icono
docs/                       Documentación del TFG
```

## Abrir y ejecutar

1. Android Studio → **File > Open** → carpeta del repositorio.
2. Esperar a que Gradle sincronice (si Android Studio propone actualizar el plugin de Android, se puede aceptar).
3. Ejecutar en un emulador o móvil con Android 8.0 (API 26) o superior.
4. Tests de la lógica: clic derecho en `app/src/test` → **Run tests**.

## Documentación

- [01 - Anteproyecto](docs/01-anteproyecto.md)
- [02 - Requisitos](docs/02-requisitos.md)
- [03 - Diseño y algoritmo](docs/03-diseno.md)
- [04 - Planificación](docs/04-planificacion.md)
- [05 - Fuentes y fiabilidad de los precios](docs/05-fuentes-precios.md)
- [06 - Precios por verificar](docs/06-precios-por-verificar.md)
- [07 - Estado del arte: apps parecidas](docs/07-estado-del-arte.md)

> Los precios son **orientativos**: combinan el índice de precios real de cada cadena (estudio OCU 2026) con algunos precios verificados en octubre de 2026. Ver [docs/05-fuentes-precios.md](docs/05-fuentes-precios.md).
