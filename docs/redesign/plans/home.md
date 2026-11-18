# Hoy (HomeScreen): rama landscape — diagnóstico y layout

Owner: área Hoy. Fichero: `ui/screens/HomeScreen.kt`. Estado: IMPLEMENTADO.
Formato objetivo: teléfono tumbado 844×390, 915×412, 740×360 dp (alto útil ~330–360 dp con el rail lateral del armazón).

## a. Diagnóstico del vertical en horizontal (líneas del fichero original)

- L373–382: toda la pantalla es un único `LazyColumn` (`home_screen`) pensado como columna vertical; en horizontal sigue siendo una sola columna con la lectura limitada a 640 dp (L484, L542, L644), desperdiciando el ancho sobrante.
- L385–407: `AppHeroHeader` → `AppHeaderArtHeight = 236.dp` (LumenComponents L329) + barra de estado + bloque eyebrow/título. En 390 dp de alto el héroe se come ~260 dp (~75 % del alto útil): la primera pantalla solo muestra arte y parte de la tarjeta de tiempo litúrgico.
- L429–436: «Oración del momento» (la acción más urgente del día) queda a ~380 dp del inicio: invisible sin scroll.
- L438–441 + L973–1020: `FidelityCard` mide ~292 dp (anillo 76 + cápsula 52 + fila 52 + cabecera + paddings): más que todo el alto útil horizontal.
- L445–606: la tarjeta de liturgia mide ~424 dp y la acción primaria `read_mass_full_button` (L578–583) vive al final de la tarjeta, a ~800 dp del inicio de la lista. El test `HomePrayerPrioritiesTest` L127–129 tiene que hacer `performScrollToNode` para alcanzarla.
- L614–814: versículo, santo, pregunta y los 6 tiles de «Explorar la fe» (2 filas × 104 dp) quedan aún más abajo, tras el toggle «Más para hoy».
- L379: el dock inferior flotante resta ~82 dp adicionales (`insets.calculateBottomPadding()`). (El armazón ya lo convirtió en rail lateral: commit 7755715.)

Resultado: en horizontal nada esencial cabe en pantalla; el ancho sobra y el alto se gasta en arte y en una columna única.

## b. Layout propuesto (zonas y dp)

Condición de forma (misma ventana que el precedente del Rosario):
`form.useLandscapeLayout && maxHeight < 480.dp && maxHeight >= 340.dp` dentro de `BoxWithConstraints`. Vertical y tablet (alto ≥ 480) conservan el `LazyColumn` original.

```
Column (padding top/bottom = insets del armazón)
├─ CompactHeader «Hoy» + fecha (44/52 dp, form.headerHeight)
│    acciones: global_search_button, home_reminders_button (vidrio 48 dp)
└─ Row (weight 1)
   ├─ Índice izquierdo FIJO, sin scroll: (maxWidth*0.38f).coerceIn(280,360) dp
   │    · HomeCompactSeason (liturgical_calendar_widget, ~58 dp)
   │    · rótulo «Oración del momento» (home_prayer_moment_heading, ~35 dp)
   │    · AppListGroup: Ahora + Después (DailyPrayerRow, tags intactos, ~131 dp)
   │    · Spacer(weight) + FidelityStrip (~56 dp) si el alto lo permite
   ├─ PaneDivider (1 dp, separator Lumen)
   └─ Panel derecho weight(1): SOLO lectura
        · LazyColumn (home_screen) — único scroll de la pantalla:
            tarjeta de liturgia (daily_mass_card): Evangelio en tipo reading,
            primera lectura, meditación desplegable, seguir leyendo
            «Para ti hoy», fidelity completa si no cupo en el índice,
            toggle today_more_toggle y, expandido: versículo, invitación,
            continuar oración, santo, pregunta, tiles Explorar
        · Transporte FIJO abajo: read_mass_full_button (weight 1) +
          home_gospel_listen; fondo canvasBottom en identidades opacas,
          hairline superior en Sanctuary (mismos condicionales que el Rosario)
```

Presupuesto de alto del panel izquierdo (adaptativo, sin scroll nunca):
`paneHeight = maxHeight − insets − form.headerHeight`.
- ≥ 304 dp: tiempo litúrgico + oración + FidelityStrip (anillo 40, cuenta, «Mis prácticas», objetivo táctil 44 dp para tentaciones).
- 244–304 dp: tiempo litúrgico + oración; `FidelityCard` completa pasa al scroll derecho.
- < 244 dp (dock aún abajo en builds sin rail): solo oración; tiempo litúrgico y fidelity al scroll derecho.
Todos los testTags existen en las tres configuraciones.

Cromo del armazón: el velo/degradado superior del scaffold sigue al scroll acumulado; con cabecera e índice FIJOS se resetea el acumulador en cada cambio de scroll (`NestedScrollDispatcher.dispatchPostScroll(+1e6)`), patrón ya usado en `CatechismScreen` L214–224. Sin esto, la cabecera fija se desvanecería al hacer scroll en el panel de lectura.

## c. Implementación

- Rama landscape detrás de la condición de forma; el `LazyColumn` vertical no se redefine (solo se extraen 6 items a composables privados compartidos, render idéntico): `VerseOfTheDayCard`, `TodayInvitationCard`, `ContinuePrayerRow`, `SaintOfTheDayCard`, `DailyQuestionCard`, `ExploreFaithGates`.
- Piezas nuevas solo-landscape: `HomeCompactSeason` (misma piel que `AppSeasonCard`, padding vertical 8 en vez de 14) y `FidelityStrip` (anillo + cuenta + acciones con los 6 tags de fidelity).
- Infraestructura usada: `rememberLandscapeForm`, `CompactHeader`, `PaneDivider`, `LandscapeMetrics.touchTarget`. El ancho del índice es fijo `coerceIn(280,360)` como el precedente del Rosario (LandscapeTwoPane no expresa ese coerce).
- Contenido devocional: el mismo que ya trae la app (mismos `strings(es,en,pt)`, mismas fuentes Vatican News/bundled); nada inventado.
- Estados compartidos entre ramas: `listState`, `meditationExpanded`, `showMoreToday`, `listeningToGospel` (+ `DisposableEffect` que corta el TTS al salir).

## d. Riesgos para el vertical

1. La extracción de los 6 items es movimiento verbatim con los mismos `remember`/tags/keys; el render vertical es idéntico. Verificado con `HomePrayerPrioritiesTest` (orden tiempo→oración→prácticas, contador «3», scroll a la acción, toggle→versículo→lector).
2. `BoxWithConstraints` envuelve también el vertical: mismas constraints, sin cambio de medida.
3. `rememberLandscapeForm()` se evalúa siempre pero solo lee `LocalConfiguration` (estable, sin bucles).
4. Texto muy grande (fontScale ≥ 1.3): las filas del índice crecen y el panel izquierdo fijo puede desbordar en los altos más bajos; el vertical no se afecta. Mitigación futura: raíl con scroll propio solo en ese caso extremo (hoy prohibido por la regla 1).
5. El reset del cromo solo se compone en la rama landscape; el vertical conserva el comportamiento scroll-edge original.