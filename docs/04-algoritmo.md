# Algoritmo de planificación

Implementación: `core/src/main/kotlin/es/tfg/recetas/core/planner/MealPlanner.kt`.

## 1. Formulación del problema

Datos:

- Un conjunto de **huecos** $H = \{(d, m)\}$: cada día $d \in \{1..D\}$ ($D = 30$) y cada comida $m$
  elegida por el usuario (desayuno, comida, cena).
- Un conjunto de **recetas aptas** $R_m$ para cada tipo de comida: las que cumplen las restricciones
  alimentarias y cuyos ingredientes se venden en algún supermercado elegido.
- Para cada ingrediente $i$, un conjunto de **productos** $p$ con tamaño de envase $s_p$ y precio $c_p$.
- Presupuesto $B$ y número de personas $n$.

Se busca asignar una receta $x_h \in R_m$ a cada hueco $h$ tal que:

$$
\text{Coste} = \sum_{i} \min_{p \in P_i} \left\lceil \frac{Q_i}{s_p} \right\rceil c_p \;\le\; B,
\qquad Q_i = \sum_{h} q_{x_h, i} \cdot \frac{n}{\text{raciones}(x_h)}
$$

y que el menú sea lo más **variado** posible (no repetir recetas en días cercanos).

Observaciones:

- El coste **no es lineal**: se compran envases completos, así que dos recetas que comparten
  ingrediente pueden costar juntas menos que por separado (se aprovecha el sobrante del envase).
- Es una variante de problemas de mochila / asignación con costes cóncavos a trozos, NP-difícil en
  general. Con ~40 recetas y 90 huecos el espacio de soluciones es enorme ($40^{90}$), así que se
  opta por una **heurística** rápida y suficientemente buena para un móvil.

## 2. Heurística: voraz con anticipación + reparación

### Paso 1 — Filtrado
Se descartan recetas con alérgenos marcados, rasgos prohibidos por la dieta, intolerancias,
ingredientes excluidos o algún ingrediente no disponible en los supermercados elegidos
(`RecipeFilter`, `PriceResolver.isAvailable`).

### Paso 2 — Cota inferior de lo que queda por gastar
Para cada tipo de comida se calcula el plato más barato (coste lineal = cantidad × precio unitario
más barato). Sumando hacia atrás se obtiene `lowerBoundFrom[i]`: lo mínimo que costará rellenar los
huecos `i..n-1`.

### Paso 3 — Asignación voraz
Se recorren los huecos en orden cronológico. Para cada uno:

1. **Candidatas por variedad**: recetas que no se han usado en los últimos `minDaysBetweenRepeats`
   días (4 por defecto, o menos si hay pocas recetas). Nunca se repite plato el mismo día salvo que
   no exista alternativa.
2. **Coste marginal** de cada candidata sobre la cesta acumulada (`Basket`): cuánto sube el total
   de la compra al añadirla. Si sobra arroz de un paquete ya comprado, el arroz le sale gratis.
3. **Viabilidad**: solo se aceptan candidatas tales que
   `costeActual + marginal + lowerBoundFrom[i+1] ≤ B`, es decir, que aún permitan acabar el mes.
4. **Puntuación** entre las viables:
   `novedad + ruido·0,6 − 0,5·exceso`, donde
   - *novedad* ∈ [0, 1] crece con los días desde la última vez que se comió esa receta;
   - *ruido* aleatorio (con semilla) da menús distintos al pulsar «regenerar»;
   - *exceso* penaliza superar el gasto objetivo por hueco, `(B − gastado) / huecosRestantes`.
5. Si ninguna es viable se toma la de menor coste marginal.

### Paso 4 — Reparación
Por el redondeo a envases la cota del paso 2 no es exacta, así que el total puede pasarse un poco.
Mientras `Coste > B`, se busca el cambio (hueco, receta alternativa) que más ahorra y se aplica.
Si no hay mejora posible, se devuelve `OverBudget` con el plan más barato encontrado, cuyo coste se
muestra al usuario como «lo mínimo que necesitarías».

### Estimación del mínimo
`estimateMinimumCostCents` ejecuta el paso 3 con presupuesto 0 (elige siempre lo más barato
manteniendo la variedad) y se muestra en la pantalla del presupuesto.

## 3. Complejidad

Con $H$ huecos, $K$ recetas candidatas e $I$ ingredientes por receta:

- Asignación voraz: $O(H \cdot K \cdot I)$ → 90 × 40 × 10 ≈ 36 000 operaciones.
- Cada iteración de reparación: $O(H \cdot K \cdot I)$, con un máximo de 500 iteraciones (en la
  práctica, unas pocas).

Se ejecuta en milisegundos.

## 4. Resultados con el catálogo de ejemplo

Mercadona + Lidl + Dia, presupuesto = 120 % del mínimo estimado:

| Perfil | Mínimo estimado | Coste del plan | Recetas distintas |
|---|---|---|---|
| 1 persona, comida + cena, omnívora | 60,45 € | 71,78 € | 18 |
| 1 persona, 3 comidas, vegana | 76,76 € | 91,44 € | 18 |
| 4 personas, 3 comidas, omnívora | 229,35 € | 273,86 € | 34 |

Con un presupuesto holgado (1 000 €) el algoritmo prioriza la variedad: 1 persona omnívora llega a
34 recetas distintas con un gasto de 122 €.

## 5. Limitaciones y mejoras previstas

- Supone que toda la compra se hace a principio de mes: no modela la caducidad de los frescos.
  Mejora: dividir la lista en compras semanales.
- No aprovecha **sobras** (cocinar 4 raciones para 2 personas y comer el resto otro día).
- No equilibra nutrientes. Se podría añadir a la puntuación un término de equilibrio
  (legumbre, pescado, verdura… por semana).
- Alternativa exacta para comparar en la memoria: modelar el problema como **programación lineal
  entera** (variables binarias receta-hueco y enteras de envases) y resolverlo con un *solver*
  (p. ej. OR-Tools) para instancias pequeñas, midiendo cuánto se aleja la heurística del óptimo.
