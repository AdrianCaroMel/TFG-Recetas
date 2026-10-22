# 05 · Fuentes y fiabilidad de los precios

Revisión: octubre de 2026.

## Qué es real y qué es aproximado

Los supermercados no publican una API de precios y no existe un estudio público con el precio de
cada producto en cada cadena. Por eso la app combina **datos reales** con un **cálculo documentado**:

| Dato | Origen | Fiabilidad |
|---|---|---|
| Nivel de precios de cada cadena | Estudio anual de supermercados de la **OCU 2026** (101.520 precios, 690 tiendas) | Real |
| Huevos Mercadona (24 uds a 5,25 €) y Carrefour (12 L a 3,05 €) | FACUA, 13/10/2026 | Real |
| Patatas Lidl (malla 5 kg a 5,45 €) | FACUA, septiembre 2026 | Real |
| Aceite de oliva virgen extra Mercadona (4,35 €/l) | merca2.es, 19/10/2026 | Real |
| Precio de referencia del resto de productos | Precios habituales de marca blanca | Aproximado |
| Precio en cada cadena | Referencia × índice OCU de la cadena / índice OCU de Mercadona | Calculado |

Índices OCU 2026 de las cadenas nacionales (cadena más barata = 100):
Alcampo 103 · Lidl 105 · Mercadona 105 · Aldi 107 · Carrefour 109.
Dia no aparece en los datos consultados; se estima en 108 a partir del informe de 2025.

## Problema detectado y solución

En la primera versión de los datos, **Lidl era el más barato en los 42 productos** (empataba con Aldi
en 38 y ganaba por ir antes en la lista). Elegir supermercados no servía para nada.

1. Los precios se recalcularon con los índices reales de la OCU, de modo que el más barato depende de
   los supermercados que elija el usuario (con todos, Alcampo; con Mercadona, Lidl y Dia, Lidl).
2. Se añadió la opción **«Hacer toda la compra en un solo supermercado»** (por defecto). La app calcula
   el mes en cada supermercado del usuario, elige el más barato y muestra la comparación en la lista de
   la compra. Es más realista: nadie va a cuatro tiendas para ahorrar unos céntimos.

## Mejora futura

Actualizar los precios automáticamente desde un servicio (por ejemplo, con los datos que publican
comparadores como RadarSuper) en lugar de usar un CSV dentro de la app.

## Fuentes

- OCU, «Las cadenas de supermercados más baratas en 2026»: https://www.ocu.org/consumo-familia/supermercados/informe/cadenas-mas-baratas
- OCU, «Los supermercados más baratos 2025»: https://www.ocu.org/consumo-familia/supermercados/noticias/supermercados-mas-baratos-2025
- FACUA, estudios de precios de alimentos básicos (2026): https://facua.org/noticias/los-alimentos-basicos-se-encarecen-hasta-un-583-del-campo-al-supermercado/
- merca2.es, precio del aceite de oliva por litro (19/10/2026): https://www.merca2.es/2026/10/19/precio-aceite-oliva-supermercados-5-2476835/
