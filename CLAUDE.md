# CLAUDE.md · TFG-Recetas (PickEats)

App Android en Java (Activities + layouts XML). TFG de DAM de Adrián Caro Melgosa. La documentación está en `docs/`.

## Diseño de la interfaz

Antes de tocar layouts, colores, textos o iconos, lee [docs/08-referencias-diseno-ui.md](docs/08-referencias-diseno-ui.md). Ahí están las señales de "app hecha con IA" que hay que evitar y las referencias de diseño.

- Mantener la paleta propia de `res/values/colors.xml` y `values-night` (en `Max`: papel, verde albahaca y azafrán, igual que `web/`). No usar degradados morado/azul ni el azul típico de las IAs.
- Tipografías en `res/font` (Bricolage Grotesque, Instrument Sans y JetBrains Mono): usar siempre la familia (`@font/bricolage`) con `textStyle`, no el fichero suelto, y aplicarlas con `textAppearance`.
- Nada de emojis en títulos, tarjetas con borde de color ni textos genéricos de marketing.
- Glassmorphism permitido solo en la rama `Max` (lo pidió Adrián): vidrio en superficies, nunca bajo texto sin contraste AA, con alternativa sólida si no hay `backdrop-filter` o si el usuario reduce transparencias.
- Márgenes en múltiplos de 4/8 dp y contraste suficiente en modo oscuro.

## Código

- Java sin lambdas (clases anónimas), métodos pocos y claros, comentarios de una línea que digan qué hace cada parte.
- La lógica (`logica/`, `modelo/`) no debe depender de Android: se prueba con JUnit en `app/src/test`.
