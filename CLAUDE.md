# CLAUDE.md · TFG-Recetas (PickEats)

App Android en Java (Activities + layouts XML). TFG de DAM de Adrián Caro Melgosa. La documentación está en `docs/`.

## Diseño de la interfaz

Antes de tocar layouts, colores, textos o iconos, lee [docs/08-referencias-diseno-ui.md](docs/08-referencias-diseno-ui.md). Ahí están las señales de "app hecha con IA" que hay que evitar y las referencias de diseño.

- Mantener la paleta propia verde/naranja de `res/values/colors.xml` y `values-night`. No usar degradados morado/azul ni el azul típico de las IAs.
- Nada de emojis en títulos, glassmorphism, tarjetas con borde de color ni textos genéricos de marketing.
- Márgenes en múltiplos de 4/8 dp y contraste suficiente en modo oscuro.

## Skills instaladas (`.claude/skills`)

- `frontend-design`: dirección visual que no parezca plantilla. Usar al rediseñar pantallas.
- `grill-me` / `grilling`: entrevista para afinar una idea antes de programarla (se lanza con `/grill-me`).
- `find-skills`: buscar más skills con `npx skills find`.
- `design-mobile-apps`: diseño de pantallas con Sleek (necesita `SLEEK_API_KEY`; implementa en React Native/HTML, así que en esta app sirve como referencia visual).
- `agent-browser`: automatizar el navegador (necesita `npm i -g agent-browser`).
- `media-use`: medios para vídeos HyperFrames (necesita `npx hyperframes`); útil para el vídeo de presentación del TFG.

## Código

- Java sin lambdas (clases anónimas), métodos pocos y claros, comentarios de una línea que digan qué hace cada parte.
- La lógica (`logica/`, `modelo/`) no debe depender de Android: se prueba con JUnit en `app/src/test`.
