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

## Precios verificados (`precios_reales.csv`)

Desde octubre de 2026 los precios reales comprobados se guardan aparte, en
`app/src/main/assets/precios_reales.csv`, con la fecha y la fuente de cada uno. Al cargar la app
sustituyen al precio estimado. Van en euros por kilo, litro o unidad, así que sirven aunque cada
supermercado venda un envase de distinto tamaño. Se usa el precio normal, no el de oferta.

Comparando con capturas reales (folleto de Lidl del 19 al 25/10/2026 y web de Alcampo), el cálculo
con el índice de la OCU acertaba en algunos productos (chorizo en Alcampo: 8,85 frente a 8,89 €/kg)
y fallaba en otros (pechuga de pollo en Lidl: 6,50 frente a 8,89 €/kg). Por eso se prioriza ir
sustituyendo los estimados por reales: ver [06 - Precios por verificar](06-precios-por-verificar.md).

## Actualización del 22/10/2026: 114 precios reales

Con acceso a internet se obtuvieron precios del día directamente de las fuentes:

| Fuente | Cómo | Cadenas |
|---|---|---|
| API pública de **tienda.mercadona.es** | Categorías de la tienda online, precio por kg/l de cada producto | Mercadona |
| API de búsqueda de **dia.es** | Búsqueda de cada ingrediente; se usa el precio normal (tachado), no el de oferta | Dia |
| **RadarSuper** (`/precios/<producto>`) | Tabla «por supermercado (hoy)» con el más barato por €/kg de cada cadena | Alcampo, Carrefour, Lidl (y contraste de Mercadona y Dia) |
| Capturas de Adrián, FACUA, prensa | Manual | Lidl, Alcampo… |

Reglas aplicadas: para cada ingrediente se elige el producto equivalente más barato por kilo de cada
cadena (normalmente su marca blanca), se descartan platos preparados y productos que no son el
ingrediente, y en RadarSuper se descartan valores atípicos (menos del 60 % o más del 180 % de la
mediana del producto), que suelen ser otro producto mezclado. Carrefour bloquea el acceso
automático (403) y Aldi no publica catálogo con precios, por eso se cubren con RadarSuper o quedan
estimados.

Con estos datos el supermercado más barato por producto se reparte entre las cadenas (Alcampo 16,
Mercadona 13, Dia 6, Lidl 4, Carrefour 3), y el supermercado recomendado se elige comparando la misma
compra del mes en cada uno.

## Mejora futura

Actualizar los precios automáticamente desde un servicio (por ejemplo, con los datos que publican
comparadores como RadarSuper) en lugar de usar un CSV dentro de la app.

## Fuentes

- OCU, «Las cadenas de supermercados más baratas en 2026»: https://www.ocu.org/consumo-familia/supermercados/informe/cadenas-mas-baratas
- OCU, «Los supermercados más baratos 2025»: https://www.ocu.org/consumo-familia/supermercados/noticias/supermercados-mas-baratos-2025
- FACUA, estudios de precios de alimentos básicos (2026): https://facua.org/noticias/los-alimentos-basicos-se-encarecen-hasta-un-583-del-campo-al-supermercado/
- merca2.es, precio del aceite de oliva por litro (19/10/2026): https://www.merca2.es/2026/10/19/precio-aceite-oliva-supermercados-5-2476835/
