# 01 · Anteproyecto

**Título provisional:** Recetas con presupuesto — planificador mensual de comidas según supermercado, restricciones y dinero disponible.

**Autor:** Adrián Caro Melgosa · DAM 2025-2027 · Madrid

## 1. Idea

Mucha gente tiene claro cuánto puede gastar al mes en comida, pero no qué cocinar para que ese dinero llegue. Además, quien tiene una alergia, una intolerancia o sigue una dieta vegetariana o vegana tiene que descartar a mano muchas recetas, y los precios cambian bastante de un supermercado a otro.

La app junta esas tres cosas: el usuario dice **dónde compra**, **qué no puede comer** y **cuánto puede gastar**, y recibe un **plan de comidas para el mes** con la **lista de la compra**, sin pasarse del presupuesto.

## 2. Objetivos

**Objetivo general:** desarrollar una aplicación Android que genere un plan mensual de recetas ajustado al presupuesto, a las restricciones alimenticias y a los supermercados del usuario.

**Objetivos específicos:**

1. Recoger los datos del usuario en pocos pasos y guardarlos en el móvil.
2. Filtrar las recetas según alergias, intolerancias y dietas.
3. Calcular el coste real del mes teniendo en cuenta que los productos se compran por envases (un paquete de arroz da para varias recetas).
4. Elegir en cada producto el supermercado más barato de los que usa el usuario.
5. Repartir las recetas por los días del mes con variedad y sin superar el presupuesto.
6. Avisar cuando el presupuesto no llegue y decir cuánto haría falta.
7. Mostrar el detalle de cada receta y la lista de la compra agrupada por supermercado.

## 3. Alcance

**Entra en el proyecto:**

- App Android nativa en Java (Android Studio).
- 6 supermercados: Mercadona, DIA, Lidl, Aldi, Alcampo y Carrefour.
- 7 alergias/intolerancias + dieta vegetariana y vegana.
- Planificación de comida y/o cena durante 30 días, para 1 a 10 personas.
- Base de datos local de productos, precios y recetas (ficheros CSV en la app).

**No entra (posibles mejoras futuras):**

- Precios en tiempo real (las webs de los supermercados no ofrecen una API pública).
- Desayunos, meriendas y fruta (se deja como margen del presupuesto).
- Información nutricional y calorías.
- Cuentas de usuario y sincronización en la nube.
- Ubicación del usuario para saber qué supermercados tiene cerca.

## 4. Tecnologías

| Parte | Tecnología |
|---|---|
| Lenguaje | Java 11 |
| Entorno | Android Studio, Gradle |
| Interfaz | Layouts XML, Material Components |
| Datos | CSV en `assets` (lectura con `BufferedReader`) |
| Persistencia | `SharedPreferences` |
| Pruebas | JUnit 4 |
| Control de versiones | Git + GitHub |

## 5. Público objetivo

Estudiantes, personas que viven solas, familias con presupuesto ajustado y cualquiera con alergias o dietas que quiera organizar la compra del mes sin pensar cada día qué cocinar.
