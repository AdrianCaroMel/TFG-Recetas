# Requisitos

## Requisitos funcionales

| ID | Requisito | Estado |
|---|---|---|
| RF-01 | El usuario puede seleccionar uno o varios supermercados donde comprar. | ✅ |
| RF-02 | El usuario puede indicar su dieta: omnívora, pescetariana, vegetariana o vegana. | ✅ |
| RF-03 | El usuario puede marcar cualquiera de los 14 alérgenos de declaración obligatoria en la UE. | ✅ |
| RF-04 | El usuario puede indicar intolerancia a la lactosa y evitar el cerdo. | ✅ |
| RF-05 | El usuario puede excluir ingredientes concretos que no le gustan. | 🟡 soportado en la lógica, falta pantalla |
| RF-06 | El usuario introduce su presupuesto mensual y el número de personas. | ✅ |
| RF-07 | El usuario elige qué comidas planificar (desayuno, comida, cena). | ✅ |
| RF-08 | El sistema muestra una estimación del gasto mínimo antes de generar el menú. | ✅ |
| RF-09 | El sistema genera un menú de 30 días que respeta restricciones y presupuesto. | ✅ |
| RF-10 | Si el presupuesto no alcanza, el sistema lo indica y muestra el coste mínimo encontrado. | ✅ |
| RF-11 | El usuario puede generar un menú alternativo con las mismas preferencias. | ✅ |
| RF-12 | El sistema genera la lista de la compra agrupada por supermercado con precios. | ✅ |
| RF-13 | El usuario puede marcar productos de la lista como comprados. | ✅ (no persistente) |
| RF-14 | El usuario puede ver el detalle de una receta con cantidades ajustadas a sus personas. | ✅ |
| RF-15 | Las preferencias y el menú se conservan al cerrar la aplicación. | ✅ |
| RF-16 | El usuario puede cambiar un plato concreto del menú por otro. | ⏳ |

## Requisitos no funcionales

| ID | Requisito |
|---|---|
| RNF-01 | Plataforma Android 8.0 (API 26) o superior. |
| RNF-02 | Generar el menú en menos de 2 segundos en un móvil de gama media. |
| RNF-03 | Funcionar sin conexión con el catálogo local. |
| RNF-04 | La lógica de negocio debe ser independiente de Android y estar cubierta por tests. |
| RNF-05 | Interfaz en español, siguiendo las pautas de Material Design 3 y con soporte de modo oscuro. |
| RNF-06 | Los datos del usuario se guardan solo en el dispositivo. |

## Casos de uso principales

### CU-01 Configurar perfil (primer uso)
1. El usuario abre la app y pulsa «Empezar».
2. Selecciona sus supermercados.
3. Indica dieta, alergias y otras restricciones.
4. Introduce presupuesto, personas y comidas; ve el gasto mínimo estimado.
5. Pulsa «Generar mi menú» y el sistema muestra el menú del mes.

### CU-02 Consultar menú y receta
1. El usuario ve el menú agrupado por semanas y días con el coste de cada plato.
2. Pulsa un plato y ve ingredientes (ajustados a sus personas), alérgenos y pasos.

### CU-03 Hacer la compra
1. El usuario abre la pestaña «Lista de la compra».
2. Ve los productos agrupados por supermercado con nº de envases, cantidad necesaria y precio.
3. Marca los productos según los va comprando.

### CU-04 Cambiar preferencias o regenerar
1. Desde el menú, el usuario pulsa ⚙ para modificar sus preferencias, o ⟳ para obtener otro menú.
2. El sistema recalcula el menú y la lista de la compra.
