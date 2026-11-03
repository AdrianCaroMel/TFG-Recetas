# CLAUDE.md · TFG-Recetas (PickEats)

App Android en Java (Activities + layouts XML). TFG de DAM de Adrián Caro Melgosa. La documentación está en `docs/`.

## Diseño de la interfaz

Antes de tocar layouts, colores, textos o iconos, lee [docs/08-referencias-diseno-ui.md](docs/08-referencias-diseno-ui.md). Ahí están las señales de "app hecha con IA" que hay que evitar y las referencias de diseño.

- Mantener la paleta propia verde/naranja de `res/values/colors.xml` y `values-night`. No usar degradados morado/azul ni el azul típico de las IAs.
- Nada de emojis en títulos, glassmorphism, tarjetas con borde de color ni textos genéricos de marketing.
- Márgenes en múltiplos de 4/8 dp y contraste suficiente en modo oscuro.

## Skills instaladas (`.claude/skills`)

Diseño:
- `frontend-design`: dirección visual que no parezca plantilla. Usar al rediseñar pantallas.
- `web-design-guidelines`: revisión de UI con las guías de Vercel. Son reglas web; en los layouts Android aplicar solo las de accesibilidad, textos y estados.
- `design-mobile-apps`: diseño de pantallas con Sleek (necesita `SLEEK_API_KEY`; implementa en React Native/HTML, así que en esta app sirve como referencia visual).

Código y pruebas:
- `tdd`: escribir primero el test JUnit y luego el código (rojo-verde-refactor).
- `diagnosing-bugs`: bucle para encontrar fallos difíciles antes de tocar código.
- `improve-codebase-architecture`, `codebase-design`, `domain-modeling`: revisar la estructura, diseñar módulos con interfaz pequeña y mantener un glosario del dominio (recetas, cesta, plan).
- `git-guardrails-claude-code`: instala un hook que bloquea comandos git peligrosos. Ojo: también bloquea `git push`.

Planificación y estudio:
- `grill-me` / `grilling`: entrevista para afinar una idea antes de programarla (se lanza con `/grill-me`).
- `to-spec` / `to-tickets`: convertir una idea en especificación y en tareas (issues). Útil para la memoria y la planificación del TFG.
- `teach`: aprender un concepto paso a paso con ejercicios.
- `find-skills`: buscar más skills con `npx skills find`.

Vídeo de presentación del TFG:
- `hyperframes` (incluye el modo explainer) y `media-use` (música, voz, subtítulos). Necesitan `npx hyperframes`.

## Código

- Java sin lambdas (clases anónimas), métodos pocos y claros, comentarios de una línea que digan qué hace cada parte.
- La lógica (`logica/`, `modelo/`) no debe depender de Android: se prueba con JUnit en `app/src/test`.
