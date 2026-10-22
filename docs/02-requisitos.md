# 02 · Requisitos

## Requisitos funcionales

| ID | Requisito | Estado |
|---|---|---|
| RF1 | El usuario elige uno o varios supermercados donde comprar. | Hecho |
| RF2 | El usuario marca sus alergias, intolerancias y dietas (puede no marcar ninguna). | Hecho |
| RF3 | El usuario indica presupuesto mensual (€), número de personas y si planifica comida, cena o ambas. | Hecho |
| RF4 | El usuario puede indicar que ya tiene básicos (aceite, sal, pimentón) para no contarlos. | Hecho |
| RF5 | La app guarda los datos y no los vuelve a pedir al abrirla. | Hecho |
| RF6 | La app genera un plan de 30 días cuyo coste no supera el presupuesto. | Hecho |
| RF7 | Ninguna receta del plan contiene ingredientes prohibidos para el usuario. | Hecho |
| RF8 | Solo se usan productos que vende al menos uno de sus supermercados. | Hecho |
| RF9 | Cada producto se asigna al supermercado más barato de los elegidos. | Hecho |
| RF10 | Si el presupuesto no llega, se muestra un aviso con el dinero mínimo necesario. | Hecho |
| RF11 | El usuario puede pedir otro plan distinto con los mismos datos. | Hecho |
| RF12 | Al pulsar una receta se ve su detalle con cantidades para el número de personas. | Hecho |
| RF13 | La lista de la compra se muestra agrupada por supermercado con subtotales. | Hecho |
| RF14 | El usuario puede cambiar sus datos en cualquier momento. | Hecho |
| RF15 | Marcar recetas como favoritas o excluir las que no le gustan. | Pendiente |
| RF16 | Cambiar una receta concreta del plan por otra. | Pendiente |

## Requisitos no funcionales

| ID | Requisito |
|---|---|
| RNF1 | Funciona sin conexión a internet (todos los datos van dentro de la app). |
| RNF2 | Android 8.0 (API 26) o superior. |
| RNF3 | El plan se genera en menos de un segundo en un móvil normal. |
| RNF4 | Interfaz en español, pensada para usarse con una mano y con textos claros. |
| RNF5 | La lógica (filtros, costes, planificador) no depende de Android y tiene tests unitarios. |
| RNF6 | Añadir productos o recetas solo requiere editar los CSV, sin tocar código. |

## Casos de uso principales

**CU1 · Configurar perfil.** El usuario abre la app por primera vez → pulsa *Empezar* → elige supermercados → marca restricciones → escribe presupuesto y personas → pulsa *Generar mi plan*.
*Alternativa:* si no marca ningún supermercado o el presupuesto es 0, se le avisa y no avanza.

**CU2 · Consultar el plan.** El usuario abre la app → pulsa *Ver mi plan* → ve el gasto, lo que le sobra y los 30 días.
*Alternativa:* si el presupuesto no llega, ve un aviso en rojo y el plan hasta donde da el dinero.

**CU3 · Ver una receta.** Desde el plan pulsa una comida o cena → ve ingredientes y pasos.

**CU4 · Ir a la compra.** Desde el plan pulsa *Lista de la compra* → ve qué comprar en cada supermercado.

**CU5 · Pedir otro plan.** Desde el plan pulsa *Otro plan* → se genera una combinación distinta respetando el presupuesto.
