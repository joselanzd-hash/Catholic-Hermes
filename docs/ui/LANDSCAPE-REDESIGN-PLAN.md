# Rediseño landscape · Plan de acción (coordinador: Hermes)

- **Rama:** `ui/landscape-redesign` (fork). No se toca `CatholicGPT`.
- **Objetivo:** que **toda** la app luzca y funcione en horizontal con la misma calidad que en
  vertical: espacio aprovechado, jerarquía clara, sin scroll innecesario, y en armonía con el
  lenguaje visual Lumen.
- **Referencia de tamaño:** teléfono en horizontal = **844×390 dp** (iPhone-like), **915×412 dp**
  (Android grande) y **740×360 dp** (Android pequeño). El alto útil, descontando barra de estado y
  gestos, queda en **~330–360 dp**. Ese es el recurso escaso; el ancho sobra.

## 1. Principio rector

En vertical la app es **una columna que se recorre**. En horizontal **no** debe ser la misma columna
centrada con márgenes enormes: debe convertirse en **dos o tres zonas simultáneas**, donde lo que
antes estaba «más abajo» ahora está «al lado».

Reglas duras que se aplican a todas las áreas:

1. **Nada de scroll para lo esencial.** Si el contenido principal no cabe en 330 dp de alto, se
   parte en paneles, no se alarga.
2. **El índice/navegación a la izquierda, el contenido a la derecha.** Es el patrón maestro–detalle
   y es el que resuelve el ejemplo del Rosario que dio el usuario.
3. **El alto manda.** Cabeceras ilustradas, tarjetas grandes y espacios verticales generosos del
   modo vertical se **comprimen** en horizontal (cabecera de 1 línea, no de 3).
4. **Un solo nivel de scroll por panel**, y solo en el panel de lectura.
5. **Misma piel.** Mismos radios, glass, tipografía y color que Lumen; nada de un «modo tablet»
   con otro aspecto.
6. **El vertical no se rompe.** Todo cambio va detrás de una condición de forma; el vertical
   sigue exactamente igual.

## 2. Infraestructura común (se hace primero, la usan todas las áreas)

| Pieza | Qué hace |
|---|---|
| `LandscapeForm` | Estado derivado de `LocalConfiguration`: `isLandscape`, `widthDp`, `heightDp`, `isShortLandscape` (alto < 500 dp). Un único punto de verdad. |
| `LandscapeScaffold` | Armazón de dos/tres zonas con raíl izquierdo fijo, panel central y panel derecho opcional. Gestiona insets y el alto útil. |
| `LandscapeRail` | Raíl de navegación izquierdo (sustituye a la barra inferior en horizontal). Iconos + etiqueta corta, scroll propio si no cabe. |
| `CompactHeader` | Versión de `IllustratedScreenHeader` para horizontal: 44–56 dp de alto, título + acción, sin ilustración grande. |
| `PaneDivider` | Separador vertical sutil coherente con el glass de Lumen. |
| `LandscapeMetrics` | Tokens de espaciado horizontal (gutter 12 dp, panel mínimo 260 dp, lectura máx. 68 ch). |

## 3. Áreas y reparto (un sub-agente por área)

| # | Área | Pantallas | Problema esperado en horizontal |
|---|---|---|---|
| A | **Armazón y navegación** | `MainApp`, barra inferior, `MoreScreen` | La barra inferior roba alto; el menú «Explorar» es una lista larga |
| B | **Hoy** | `HomeScreen` | Tarjetas apiladas; mucho ancho vacío |
| C | **Rosario** | `RosaryScreen`, `PrayerBeadCounter` | Dos columnas, pero las cuentas obligan a scroll para leer |
| D | **Orar** | `PrayersAndNovenasScreen`, `NightPrayersScreen` | Lista + detalle en la misma columna |
| E | **Leer (Biblia)** | `BibleScreen`, `ScriptureStudyScreen` | Ya tiene algo de landscape; falta maestro–detalle real |
| F | **Catecismo** | `CatechismScreen` | Índice y texto compitiendo por el alto |
| G | **Mi camino** | `MyPathScreen`, `HabitsAndVirtuesScreen`, `SpiritualLifeScreen`, `PilgrimageAndHabitsScreen` | Paneles de hábitos y diario apilados |
| H | **Doctrina y estudio** | `DoctrineAndHistoryScreen`, preguntas, María y Jesús, Historia, Órdenes | Listas largas a una columna |
| I | **Santos** | `SaintsScreen` | Ficha con imagen grande + texto largo |
| J | **Confesión** | `ConfessionScreen`, `ui/confession/*` | Examen por secciones, mucho scroll |
| K | **Planes** | `ReadingPlansScreen` | Calendario + lista |
| L | **Buscar** | `SearchScreen` | Resultados a una columna |
| M | **Recordatorios y avisos** | `RemindersScreen` | Formularios altos |
| N | **Primeros pasos** | `BeginnerJourneyScreen` | Onboarding de tarjetas |
| O | **Parroquias y noticias** | `ParishFinderScreen`, noticias | Mapa/lista |
| P | **Diálogos y hojas** | `ui/components/*Dialog`, `*Sheet`, `GuiCustomizerModal` | Hojas que ocupan todo el alto |

## 4. Proceso por área (bucle con crítico)

Para cada área, **dos sub-agentes distintos**:

1. **Diseñador/implementador.** Lee la pantalla y el sistema Lumen, propone el layout landscape,
   lo implementa en Compose detrás de `LandscapeForm`, y genera una **maqueta HTML** a 844×390,
   915×412 y 740×360 con la piel real de Lumen (colores, radios, tipografía).
2. **Crítico visual (severo).** Recibe las maquetas renderizadas y las juzga con una rúbrica dura.
   No aprueba por defecto. Si algo no es AAA, devuelve la lista de defectos y el área **vuelve al
   paso 1**. Se repite hasta que el crítico no encuentre nada.

### Rúbrica del crítico (0–5 en cada eje; se exige ≥4 en todos y ningún 0)

- **Aprovechamiento del ancho:** ¿hay zonas muertas > 120 dp? ¿columnas centradas con márgenes absurdos?
- **Uso del alto:** ¿cabe lo esencial sin scroll? ¿la cabecera se come más de 60 dp?
- **Jerarquía:** ¿se entiende en 2 segundos qué es lo principal?
- **Scroll:** ¿hay doble scroll, scroll horizontal, o scroll para alcanzar el control principal?
- **Coherencia Lumen:** ¿radios, glass, tipografía y color son los mismos que en vertical?
- **Legibilidad:** ¿medida de línea ≤ 68 caracteres? ¿cuerpo ≥ 14 sp? ¿contraste suficiente?
- **Táctil:** ¿objetivos ≥ 44 dp? ¿algo pegado al borde o bajo el notch?
- **Estados:** vacío, carga, error y texto largo, ¿resuelven igual de bien?

## 5. Verificación y entrega

- Maquetas renderizadas a PNG por área y por tamaño, guardadas en `docs/ui/landscape/shots/`.
- Informe por área con el veredicto del crítico y las iteraciones.
- **Compilación:** `./gradlew :app:assembleDebug` con JDK 21 + SDK de Android. La versión se sube al
  número siguiente a la última release publicada.
- Push a `ui/landscape-redesign` en el fork.

## 6. Riesgos conocidos

- **No hay JDK ni SDK en el equipo al empezar.** Se instalan (JDK 21 por winget, SDK por
  cmdline-tools). Si la instalación falla, se entrega el código y se avisa: no se inventa una
  compilación.
- **No hay emulador.** La verificación visual se hace sobre maquetas HTML fieles a Lumen, no sobre
  la app corriendo. Se declara expresamente en el informe; no se presenta como captura de la app.
- **El modelo de los sub-agentes se satura (HTTP 429).** Si un sub-agente falla, el coordinador
  hace el trabajo a mano y lo anota.

## 7. Estado verificado

Compilación real: `./gradlew :app:assembleDebug` → **BUILD SUCCESSFUL**. APK verificado con
`aapt2 dump badging`: `versionCode='9'`, `versionName='1.8.27'`. El `debug.keystore` se genera
localmente con `keytool` y sigue ignorado por git.

| Área | Rama landscape | Compila |
|---|---|---|
| Armazón / navegación (`LumenKit`) | dock → raíl lateral fijo | sí |
| Rosario (`RosaryScreen`) | cuentas fijas izquierda, lectura derecha | sí · **crítico APROBADO** |
| Hoy (`HomeScreen`) | implementada por sub-agente | sí |
| Biblia (`BibleScreen`) | maestro-detalle | sí |
| Estudio bíblico (`ScriptureStudyScreen`) | maestro-detalle | sí |
| Oraciones de la noche (`NightPrayersScreen`) | implementada por sub-agente | sí |
| Orar · ficha de oración (`PrayerDetailView`) | arte+controles fijos izquierda, texto derecha | sí |
| Planes de lectura (`ReadingPlansScreen`) | estado del plan fijo, lecturas y navegación anclada | sí |
| Santos (`SaintsScreen`) | índice y filtros fijos, ficha con miniatura | sí · crítico en 2ª vuelta |
| Más (`MoreScreen`) | héroe 232→44 dp, secciones en rejilla 2-3 col | sí |
| Buscar (`SearchScreen`) | campo fijo que sobrevive al teclado, `imePadding` | sí |
| Catecismo (`CatechismScreen`) | índice fijo, artículo a la derecha | sí |
| Mi camino (`MyPathScreen`) | resumen fijo, historial a la derecha | sí |
| Vida espiritual (`SpiritualLifeScreen`) | resumen fijo, detalle a la derecha | sí |
| Hábitos y virtudes (`HabitsAndVirtuesScreen`) | anillo 216→96-112 dp, checklist a la derecha | sí |
| Recordatorios (`RemindersScreen`) | formulario y Guardar sobre el teclado, rueda 200→120 dp | sí |
| Avisos (`NoticeSettings`) | rejilla 2 col, editores en panel | sí |
| Devociones (`DevotionsExplorerScreen`) | rejilla 2-3 col; detalle en dos paneles | sí |
| Doctrina e historia (`DoctrineAndHistoryScreen`) | cabecera compacta (beneficia a 6 roots) | sí |
| Parroquias (`ParishFinderScreen`) | dos paneles sin scroll | sí |
| Guía del principiante (`BeginnerJourneyScreen`) | índice de pasos fijo, lectura y acción anclada | sí |
| Peregrinación (`PilgrimageAndHabitsScreen`) | calendario/tabs fijos, rejilla; santuario en dos paneles | sí |

**20 pantallas** con rama landscape. `assembleDebug` → **BUILD SUCCESSFUL**; APK verificado con
`aapt2 dump badging`: `versionCode='9'`, `versionName='1.8.27'`.

### El defecto sistemático

Casi todas las fichas de detalle usaban `AppDetailHeader` / `AppHeroHeader` con arte de
**200–300 dp**. En vertical es elegante; en un teléfono tumbado de 390 dp de alto **se come la
pantalla entera** antes de mostrar el contenido. La corrección es la misma en todas: arte a
78–128 dp en un panel izquierdo fijo, contenido a la derecha con el único scroll, y los controles
anclados abajo. El barrido se hizo con `grep` sobre los ficheros, no pantalla a pantalla.

### Lecciones del bucle con el crítico

- **Un objetivo táctil invisible no es un objetivo táctil.** El área de 44 dp existía con
  `background: transparent`; el crítico la rechazó porque no se puede apuntar a lo que no se ve.
  Los iconos llevan ahora superficie visible.
- **Añadir márgenes fijos por tamaño empeora el reparto del alto.** La v4 dejó hasta el 25 % del
  alto muerto. Se resuelve con columna flex y `space-between`, no con márgenes por resolución.
- **La semántica del color importa:** el verde es éxito/acento; un icono de error en verde miente.
  El gold es el token de advertencia.
- **Un sub-agente puede escribir el cableado y no la implementación.** Uno llamó a composables que
  nunca definió y dejó 30 errores que bloquearon el trabajo de los demás. Regla añadida al contexto
  de todos: no llames a nada que no hayas escrito, y si no puedes terminar, revierte tu fichero.
- **El coordinador verifica la compilación antes de integrar.** Sin build real, tres cuartas partes
  del trabajo habrían parecido buenas sin serlo.
