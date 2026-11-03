# 08 · Referencias de diseño de la interfaz (reels guardados)

Notas sacadas de varios reels de Instagram que guardé sobre cómo hacer con IA interfaces que **no parezcan hechas con IA** ("vibecoded"). Sirven para la tarea pendiente del README: *probar en emulador / móvil real y pulir la interfaz*.

> Los reels eran web, no Android. Aquí está todo traducido a lo que aplica en PickEats (Activities + layouts XML + Material Components).

## 1. Las 20 señales de que una app "parece hecha con IA"

Reel de **millee.md** ("20 reasons why your app looks vibecoded"). Captura: [20-senales-vibecoded.jpg](img/referencias-reels/20-senales-vibecoded.jpg)

| # | Señal | ¿Aplica a PickEats? |
|---|---|---|
| 1 | Degradado morado → azul | Evitar. La app ya usa verde/naranja propio. |
| 2 | Texto del titular con degradado | Evitar en la pantalla de inicio. |
| 3 | Emojis en los títulos | **Revisar** textos de `strings.xml` y cabeceras. |
| 4 | La fuente Inter en todo | En Android sería usar solo Roboto por defecto. Valorar una fuente con personalidad para los títulos. |
| 5 | Tarjetas con borde de color | Revisar `item_*` con borde verde. |
| 6 | Tarjetas de cristal (glassmorphism) | No usar. |
| 7 | Modo oscuro con poco contraste | **Revisar `values-night`**: texto secundario sobre fondo oscuro. |
| 8 | 3 cajas de icono en fila | Típico en la pantalla de inicio: evitar los "3 beneficios con icono". |
| 9 | Etiqueta (badge) encima del titular | Evitar. |
| 10 | Iconos Lucide en todo | En Android: no mezclar estilos de icono; usar un solo set (Material Symbols). |
| 11 | Componentes de librería sin tocar (shadcn) | En Android: Material Components con el estilo por defecto. Personalizar forma, esquinas y colores. |
| 12 | Aparecer al hacer scroll (fade-in) | No animar cada elemento de la lista. |
| 13 | Brillo que sigue al cursor | No aplica en móvil. |
| 14 | Botones que se desvanecen al pulsar | Usar el ripple estándar. |
| 15 | Espaciado inconsistente | **Revisar**: usar siempre múltiplos de 4/8 dp, mejor con `dimens.xml`. |
| 16 | Rayas largas (—) en todos los textos | Revisar textos. |
| 17 | Textos genéricos de marketing ("potencia tu…") | Textos concretos: "Plan de 30 días con 150 €". |
| 18 | Acentos en cursiva con serifa | Evitar. |
| 19 | Combinación Space Grotesk + Instrument Serif | Evitar esa pareja concreta. |
| 20 | (no se llegó a ver en el reel) | — |

## 2. Skills y herramientas de diseño para Claude

Reel de **petergriffin.ai** ("3 Claude skills that make your websites look…"). Captura: [skills-diseno-claude.jpg](img/referencias-reels/skills-diseno-claude.jpg)

- **Emil Kowalski's design skills**: detalles pequeños y animaciones suaves (estilo Vercel/Linear).
- **Impeccable**: vocabulario de diseño real (layout, tipografía, color, pulido).
- **Taste**: basado en 247 webs reales estudiadas; elige una dirección artística.
- **Figma MCP**: conectar Claude con Figma para pasar de diseño a código.
- **Playwright**: que la IA abra la app y pruebe que funciona. En Android el equivalente es **Espresso** o capturas del emulador.
- Idea del reel: con esas skills, una landing de cafetería se hace en menos de 1 hora.

## 3. Tendencias de diseño

Reel de **ivanvibecodes** ("10 nuevas tendencias de diseño para pedirle a la IA"). Captura: [tendencias-diseno.jpg](img/referencias-reels/tendencias-diseno.jpg)

1. Glassmorfismo
2. Neumorfismo (sombras suaves)
3. Brutalismo / neo-brutalismo (alto contraste, colores fuertes, formas gruesas)
4. Minimalismo
5. (el resto no se vio en la grabación; en el reel también se enseña **manus.im** para publicar webs con dominio)

Además, **marcelodesignxh** ("Stop designing boring hero sections"): portadas con una imagen grande protagonista y tipografía potente en vez del titular centrado y genérico.

Para PickEats encaja mejor **minimalismo** con algo de personalidad (foto o ilustración de comida grande en inicio y en receta) que glass o neumorfismo.

## 4. Cómo usar esto con Claude Code

Al pedir cambios de interfaz:

1. Pasar la tabla del punto 1 y pedir que revise los layouts (`app/src/main/res/layout`) y `strings.xml` buscando esas señales.
2. Pedir que unifique márgenes en `dimens.xml` y revise el contraste de `values-night/colors.xml`.
3. Comprobar en el emulador con capturas antes de dar el cambio por bueno.

## Otros reels del mismo lote (no son del TFG)

Trading con IA, Shorts, *clipping*, libros para colorear en Amazon y JARVIS con Claude Code. Eran ideas para ganar dinero y no tienen que ver con la app.
