# Recomendaciones de skills para Codex

Fecha: 19 de noviembre de 2026. Fase `find-skill`, variante aprobada `recommend`.

## Resultado

Se recomienda reutilizar las seis skills de la tabla que ya están instaladas. No hace falta una nueva instalación para continuar la fundación. `webapp-testing` queda como candidata opcional para cuando exista una interfaz ejecutable; no está seleccionada ni instalada. El MCP Playwright disponible y una skill de pruebas son capacidades distintas: esta fase no instala navegadores ni paquetes del producto.

## Comparación

Instalaciones consultadas en skills.sh; estrellas contrastadas en GitHub. `K` indica miles. Son indicadores redondeados y cambiantes, no garantía de calidad. Se inspeccionaron fuentes, contenido y correspondencia con las necesidades del proyecto, además de las métricas. La copia local puede diferir de la versión remota vigente; no se actualizó ninguna.

| Skill y ficha | Origen | Instalaciones | Estrellas del repo | Acción | Aplicación al proyecto |
| --- | --- | --- | --- | --- | --- |
| [java-springboot](https://skills.sh/github/awesome-copilot/java-springboot) | [github/awesome-copilot](https://github.com/github/awesome-copilot) | 22.1K | 41.3K | Reutilizar | Java/Spring Boot: configuración, validación, transacciones y pruebas de backend; no fija versiones ni arquitectura. |
| [supabase-postgres-best-practices](https://skills.sh/supabase/agent-skills/supabase-postgres-best-practices) | [supabase/agent-skills](https://github.com/supabase/agent-skills) | 490.7K | 3.1K | Reutilizar | Diseño PostgreSQL, consultas y concurrencia para profesional/sillón/bloqueos. No obliga a usar hosting Supabase. |
| [api-and-interface-design](https://skills.sh/addyosmani/agent-skills/api-and-interface-design) | [addyosmani/agent-skills](https://github.com/addyosmani/agent-skills) | 78.6K | 119.0K | Reutilizar | Contratos entre frontend vanilla y backend, validaciones y errores estables sin elegir endpoints por anticipado. |
| [test-driven-development](https://skills.sh/obra/superpowers/test-driven-development) | [obra/superpowers](https://github.com/obra/superpowers) | 284.9K | 321.4K | Reutilizar | Escenarios de solapamiento, plazos 48/24, permisos y lista de espera; no selecciona framework de pruebas. |
| [security-audit](https://skills.sh/cloudflare/security-audit-skill/security-audit) | [cloudflare/security-audit-skill](https://github.com/cloudflare/security-audit-skill) | 116.1K | 75.4K | Reutilizar | Revisión enfocada de permisos clínicos, cuenta propia y enlaces. No inicia una auditoría completa. |
| [web-design-guidelines](https://skills.sh/vercel-labs/agent-skills/web-design-guidelines) | [vercel-labs/agent-skills](https://github.com/vercel-labs/agent-skills) | 879.9K | 34.5K | Reutilizar | Revisión de accesibilidad, formularios y experiencia de interfaz; compatible con HTML/CSS/TypeScript vanilla. |
| [webapp-testing](https://skills.sh/anthropics/skills/webapp-testing) | [anthropics/skills](https://github.com/anthropics/skills) | 208.9K | 197.2K | Opcional, no instalada | Pruebas de aplicaciones web locales con Playwright/Python y gestión de servidores. Evaluar cuando exista una UI ejecutable. |

## Criterio de selección y límites

- Java/Spring Boot, PostgreSQL y TypeScript vanilla proceden del stack aprobado; ninguna skill autoriza cambiarlo.
- La prevención de solapamientos y la disponibilidad concurrente necesitan pruebas contra persistencia cuando corresponda; TDD no convierte pruebas aisladas en evidencia de PostgreSQL.
- La cuenta, el enlace de cancelación y la separación clínica requieren revisión de autorización. Recomendar `security-audit` no ejecuta su modo de auditoría completa.
- Email real gratuito sigue sin proveedor elegido. No se recomienda una skill atada a un proveedor ni se decide SMTP, API, cola o scheduler. Se usará documentación primaria del proveedor al definir el change.
- La skill de PostgreSQL aplica a PostgreSQL fuera de Supabase; no se elige su plataforma por el nombre.
- Se conserva `design-system`, ya disponible desde Active Stack, para resolver la política visual diferida cuando corresponda. No se crea un sistema visual en esta fase ni se inventan cifras públicas para esta instalación local.
- Se conservan las seis skills OpenSpec locales ya instaladas por el CLI. No se reinstalan ni se crean changes.
- React, Next.js y shadcn no corresponden al frontend vanilla aprobado. No se incorporan por popularidad del catálogo.
- No se ejecutó `npx skills`: las páginas de catálogo cubrieron la búsqueda y se evitó descargar un CLI para una fase de recomendación.

## Opción futura de instalación

Solo si el usuario la selecciona expresamente, el comando de la candidata es:

```sh
npx skills add https://github.com/anthropics/skills --skill webapp-testing
```

No fue ejecutado. La candidata contiene un flujo Playwright/Python; evaluar esa dependencia antes de adoptarla y reutilizar el navegador/MCP disponible cuando sea suficiente. Su [SKILL.md original](https://github.com/anthropics/skills/blob/main/skills/webapp-testing/SKILL.md) fue inspeccionado. Esta recomendación no selecciona el framework de pruebas del frontend ni del backend.

## Evidencia local y siguiente fase

Los seis archivos instalados y sus orígenes se contrastaron con `/Users/giovita/.agents/.skill-lock.json`. Sus hashes y rutas, las métricas y las recomendaciones están en [la evidencia](evidencias/recomendacion-skills-verificacion.json) y en `skills.recommended` del estado. Se preservaron instalaciones previas.

La siguiente fase aprobada es `registry`: crear el registro del proyecto con las skills realmente disponibles. Aún no se ejecuta en esta fase. No se modifican KB, roadmap, código, configuración global ni instalaciones.
