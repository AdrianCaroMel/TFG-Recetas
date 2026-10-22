# Precios por verificar

Checklist para ir sustituyendo los precios **estimados** por precios **reales**.
Verificados: **114 de 250** combinaciones producto-supermercado.

✅ precio real verificado · ⬜ precio estimado (pendiente) · — ese supermercado no lo vende

## Cómo conseguir precios rápido

1. Busca el producto en la web del supermercado (o en su folleto si no tiene tienda online):
   - Mercadona: tienda.mercadona.es (pide código postal)
   - Alcampo: compraonline.alcampo.es · Carrefour: carrefour.es · Dia: dia.es
   - Lidl y Aldi: folleto semanal en su web o app
2. Haz una **captura de la página de resultados** donde se vea el precio por kilo/litro.
   Una sola captura sirve para muchos productos.
3. Pásasela a Claude: añade una línea por precio en `app/src/main/assets/precios_reales.csv`
   (precio NORMAL, no el de oferta) y actualiza esta tabla.

## Estado

| id | Producto | Precio en | Mercadona | Dia | Lidl | Aldi | Alcampo | Carrefour |
|---|---|---|---|---|---|---|---|---|
| 1 | Arroz redondo | €/kg | ✅ | ✅ | ⬜ | ⬜ | ✅ | ✅ |
| 2 | Macarrones | €/kg | ✅ | ✅ | ✅ | ⬜ | ✅ | ✅ |
| 3 | Espaguetis | €/kg | ✅ | ✅ | ⬜ | ⬜ | ✅ | ✅ |
| 4 | Pasta sin gluten | €/kg | ✅ | ✅ | ⬜ | — | ⬜ | ⬜ |
| 5 | Cuscús | €/kg | ✅ | ✅ | ⬜ | ⬜ | ✅ | ⬜ |
| 6 | Quinoa | €/kg | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ |
| 7 | Tortillas de trigo | €/ud | ✅ | ✅ | ⬜ | ⬜ | ⬜ | ⬜ |
| 8 | Pan rallado | €/kg | ✅ | ✅ | ⬜ | ⬜ | ⬜ | ⬜ |
| 9 | Lentejas pardinas | €/kg | ✅ | ✅ | ✅ | ⬜ | ✅ | ✅ |
| 10 | Garbanzos cocidos | €/kg | ✅ | ✅ | ✅ | ⬜ | ✅ | ✅ |
| 11 | Alubias cocidas | €/kg | ✅ | ✅ | ⬜ | ⬜ | ✅ | ✅ |
| 12 | Patatas | €/kg | ✅ | ✅ | ✅ | ⬜ | ✅ | ✅ |
| 13 | Cebolla | €/kg | ✅ | ✅ | ⬜ | ⬜ | ⬜ | ⬜ |
| 14 | Ajos (malla de 3) | €/ud | ✅ | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ |
| 15 | Tomate | €/kg | ✅ | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ |
| 16 | Pimiento rojo | €/kg | ✅ | ✅ | ⬜ | ⬜ | ⬜ | ⬜ |
| 17 | Calabacín | €/kg | ✅ | ✅ | ⬜ | ⬜ | ⬜ | ⬜ |
| 18 | Zanahoria | €/kg | ✅ | ✅ | ⬜ | ⬜ | ⬜ | ⬜ |
| 19 | Espinacas frescas | €/kg | ✅ | ✅ | ⬜ | ⬜ | ⬜ | ⬜ |
| 20 | Brócoli | €/kg | ✅ | ✅ | ⬜ | ⬜ | ⬜ | ⬜ |
| 21 | Lechuga | €/ud | ✅ | ✅ | ⬜ | ⬜ | ⬜ | ⬜ |
| 22 | Champiñones | €/kg | ✅ | ✅ | ⬜ | ⬜ | ⬜ | ⬜ |
| 23 | Berenjena | €/kg | ✅ | ✅ | ⬜ | ⬜ | ⬜ | ⬜ |
| 24 | Guisantes congelados | €/kg | ✅ | ✅ | ⬜ | ⬜ | ⬜ | ⬜ |
| 25 | Tomate triturado | €/kg | ✅ | ✅ | ⬜ | ⬜ | ⬜ | ⬜ |
| 26 | Maíz dulce | €/kg | ✅ | ✅ | ⬜ | ⬜ | ⬜ | ⬜ |
| 27 | Caldo de verduras | €/l | ✅ | ✅ | ⬜ | ⬜ | ⬜ | ⬜ |
| 28 | Atún en aceite (pack 3) | €/kg | ✅ | ✅ | ✅ | ⬜ | ⬜ | ⬜ |
| 29 | Huevos | €/ud | ✅ | ✅ | ⬜ | ⬜ | ⬜ | ✅ |
| 30 | Queso rallado | €/kg | ✅ | ✅ | ⬜ | ⬜ | ⬜ | ⬜ |
| 31 | Pesto | €/kg | ✅ | ✅ | ⬜ | ⬜ | ⬜ | ⬜ |
| 32 | Salsa de soja | €/l | ✅ | ✅ | ⬜ | ⬜ | ⬜ | ⬜ |
| 33 | Tofu firme | €/kg | ⬜ | — | ⬜ | ⬜ | ⬜ | ⬜ |
| 34 | Pechuga de pollo | €/kg | ✅ | ✅ | ✅ | ⬜ | ⬜ | ✅ |
| 35 | Carne picada mixta | €/kg | ✅ | ✅ | ⬜ | ⬜ | ✅ | ✅ |
| 36 | Lomo de cerdo | €/kg | ✅ | ✅ | ✅ | ⬜ | ⬜ | ⬜ |
| 37 | Chorizo | €/kg | ✅ | ✅ | ✅ | ⬜ | ✅ | ✅ |
| 38 | Merluza congelada | €/kg | ✅ | ✅ | ✅ | ⬜ | ✅ | ✅ |
| 39 | Gambas congeladas | €/kg | ✅ | ✅ | ⬜ | ⬜ | ⬜ | ⬜ |
| 40 | Aceite de oliva virgen extra | €/l | ✅ | ✅ | ✅ | ⬜ | ✅ | ✅ |
| 41 | Sal | €/kg | ✅ | ✅ | ⬜ | ⬜ | ⬜ | ✅ |
| 42 | Pimentón | €/kg | ✅ | ✅ | ⬜ | ⬜ | ⬜ | ⬜ |
