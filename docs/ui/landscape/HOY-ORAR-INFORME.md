# Hoy y Orar en horizontal — informe de dos áreas

**Rama:** `ui/landscape-redesign` · **Veredicto: APROBADAS ambas**

> **Quién criticó.** Los sub-agentes críticos de estas dos áreas murieron por saturación del
> modelo (HTTP 429 / interrupción tras 43 y 29 min). El bucle no se abandonó: **el coordinador
> hizo la crítica con la misma rúbrica de 8 ejes**, midiendo píxeles con un script
> (`scratch/ui/medir.py`) en vez de opinando. Se declara expresamente porque un crítico externo
> y una autocrítica no valen lo mismo, y el lector tiene derecho a saber cuál recibió.

---

## HOY

### El problema

`AppHeroHeader` con arte de ~300 dp abría la pantalla. En un teléfono tumbado de 390 dp de alto
se comía casi todo antes de enseñar el día.

### La solución

Cabecera compacta de 52 dp a ancho completo (título, fecha, Buscar y Recordatorios en discos de
44 dp) → raíl de navegación a la izquierda → panel izquierdo fijo del 38 % (280–360 dp) con el
tiempo litúrgico y «Oración del momento» → panel derecho con la liturgia en tipo de lectura, el
único scroll → **transporte fijo abajo** con el botón primario y el audio.

### Iteraciones

| Versión | Veredicto | Ejes bajos | Bloqueantes |
|---|---|---|---|
| v1 | RECHAZADO (crítico externo) | 2=3, 7=3, 8=3 | cabecera de 740 sin aire; icono de Recordatorios era un anillo; 108 dp muertos; sin estados |
| v2 | **APROBADO** (coordinador, medido) | ninguno | los cuatro cerrados |

### Los cuatro bloqueantes, con la medida que los cierra

1. **Cabecera de 740 = 44 dp con botones de 44 dp dentro → 0 dp de aire.** El arco del botón
   quedaba tangente al borde. Subida a 52 dp. Medido en v2: el disco ocupa **y5→y47 = 44 dp** con
   **5 dp de aire superior**, igual en los tres tamaños.
2. **El icono de Recordatorios era un anillo hueco** que se leía como un *spinner*. Redibujado
   como campana con badajo; verificado a zoom en ambos temas.
3. **108 dp muertos (34 % del panel) en 740.** La tira de fidelidad se omite por diseño cuando el
   panel baja de 304 dp — pero omitir el bloque no reabsorbe el espacio. Ahora «Para ti hoy» baja
   al panel izquierdo en ese caso. Medido en v2: **franja muerta inferior 6 / 6 / 2 dp**
   (844 / 915 / 740). El de 740 pasó de 108 a 2.
4. **Sin estados.** Añadidos tres: sin conexión (liturgia guardada + Reintentar), sin evangelio
   del día (cae a las lecturas) y nombre largo con ellipsis en la fila de 52 dp.

No bloqueante aplicado: fade de scroll en el borde del transporte.

### Puntuación (coordinador)

| Eje | Nota | Prueba |
|---|---|---|
| 1 Ancho | 5 | paneles 280–360 dp según clamp; sin zonas muertas >120 dp |
| 2 Alto | 5 | franja muerta 6/6/2 dp; cabecera 52 dp; esencial visible sin scroll |
| 3 Jerarquía | 4 | Hoy+fecha → AHORA → CTA verde fijo; campana ya inequívoca |
| 4 Scroll | 5 | un único scroll de lectura; CTA en transporte fijo; fade de affordance |
| 5 Coherencia Lumen | 5 | tokens exactos muestreados; contrastes calculados abajo |
| 6 Legibilidad | 5 | medida ≤63 caracteres; cuerpo ≥15 sp; contraste mínimo 5.18:1 |
| 7 Táctil | 5 | discos 44 dp con 5 dp de aire; filas 52 dp; primario 46 dp |
| 8 Estados | 4 | tres estados representados; falta el skeleton de carga |

---

## ORAR (ficha de oración)

### El problema

`AppDetailHeader` con arte de 300 dp: en 390 dp de alto la oración entera quedaba fuera de vista.

### La solución

Panel izquierdo fijo del 40 % (`PaneSurface`, radio 24): **el arte crece hasta llenar el alto
disponible**, título, metadatos y la barra de acciones anclada abajo con tres discos de 44 dp
(Compartir, Escuchar, Tamaño del texto). Panel derecho: la oración en serif EB Garamond 19/30,
Historia, Cómo rezarla y «Acerca de esta oración». Único scroll, medida ≤640 dp.

### Iteraciones

| Versión | Veredicto | Ejes bajos | Defecto |
|---|---|---|---|
| v1 | RECHAZADO (coordinador) | 8=3 | sin anexo de estados |
| v2 | **APROBADO** (coordinador, medido) | ninguno | cuatro estados añadidos |

**El defecto de v1 lo encontró la autocrítica, no el externo:** la maqueta no tenía ni un estado.
Un área no se aprueba solo porque el camino feliz se vea bien.

Antes de eso, al revisar el render ya había corregido una **franja muerta en el panel izquierdo**:
con el arte a altura fija quedaba un hueco bajo el subtítulo. La solución no es rellenar — es que
**el arte absorba el sobrante**, porque en el panel izquierdo una imagen grande no le roba alto a
nadie. Medido: **231 / 253 / 204 dp de arte** y **10 dp de franja muerta** en los tres tamaños.

**Ese cambio se sincronizó con el código Compose** (`PrayerDetailView`: el `Box` del arte pasa de
altura fija a `weight(1f)` con `heightIn(min=…)`). Sin eso, la maqueta aprobada y la app compilada
serían dos diseños distintos. `compileDebugKotlin` → BUILD SUCCESSFUL.

### Puntuación (coordinador)

| Eje | Nota | Prueba |
|---|---|---|
| 1 Ancho | 5 | 40/60 sin zonas muertas |
| 2 Alto | 5 | arte 231/253/204 dp; franja muerta 10 dp en los tres |
| 3 Jerarquía | 4 | arte → título → oración → contexto |
| 4 Scroll | 5 | uno solo, en la lectura; controles fijos |
| 5 Coherencia Lumen | 4 | tokens exactos; serif de lectura correcto |
| 6 Legibilidad | 5 | 19/30 serif, medida ≤640 dp |
| 7 Táctil | 5 | tres discos de 44 dp con superficie visible |
| 8 Estados | 4 | oración larga, audio activo, carga y error con recuperación |

---

## Contrastes calculados sobre los tokens

| Par | Claro | Oscuro |
|---|---|---|
| textPrimary / canvas | 16.37 | 17.51 |
| textSecondary / grouped | 7.28 | 8.00 |
| accent / accentMuted | 5.18 | 6.53 |
| gold / grouped | 5.18 | 9.76 |

Todos por encima de AA (4.5:1) con holgura.

## Contenido

**Nada devocional compuesto.** El Gloria y la Salve se tomaron de `NightPrayerData.kt`; la
Historia, Cómo rezarla, Fuente, Aprobación y Nota del texto, de `assets/prayers/details-a.json`
(`prayerId: glory_be`). Al escribir el estado «sin evangelio» se llegó a redactar una reflexión
propia: **se detectó y se retiró antes de renderizar**, dejando solo las referencias y filas que
el código muestra de verdad.

## Qué se está mirando

Maquetas HTML con los tokens reales, a 844×390, 915×412 y 740×360 dp, en claro y oscuro, más los
anexos de estados. **No son capturas de la app corriendo**: no hay emulador. El código Compose
compila y el APK 1.8.27 (versionCode 9) está publicado en la release del fork.
