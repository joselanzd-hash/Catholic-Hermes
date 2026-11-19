# Rosario en horizontal — informe del área

**Rama:** `ui/landscape-redesign` · **Veredicto del crítico: APROBADO** (v5, pulida en v6)

## El problema

El Rosario ya tenía dos columnas en horizontal, pero el usuario señaló el defecto exacto: había
que hacer scroll para leer la oración mientras las cuentas quedaban arriba. La causa, medida en
el código:

- `wide = maxWidth >= 600.dp` (línea ~1157) solo miraba el **ancho**, así que un teléfono tumbado
  entraba por la misma rama que una tablet en vertical.
- El split era 45/55 y **el progreso (52 dp), las cuentas (70 dp) y la rúbrica viajaban dentro del
  mismo scroll que la oración**. De ~240 dp útiles, ~122 dp eran chrome.
- Un `LaunchedEffect` reseteaba el scroll en cada cuenta: unas 59 veces por rosario.

## La solución

Rama nueva condicionada por el **alto** (`maxHeight < 480.dp`, suelo `>= 340.dp`), sin tocar
`wide` para no romper las tablets en vertical:

- **Raíl izquierdo fijo** (38 % del ancho, limitado 280–360 dp): escenario, título, progreso y las
  cinco decenas completas. No se desplaza nunca.
- **Panel derecho** (~524 dp en un teléfono de 840): solo la lectura, con el único scroll. Una
  Avemaría cabe entera sin desplazar.
- **Transporte fijo** en la zona del pulgar.

`testTag` conservados: `prayer_content_scroll`, `prayer_bead_counter`, `prayer_transport`,
`next_bead_button` (dependencia de `RosaryInteractionTest`).

## El bucle con el crítico

Cinco iteraciones. Cada rechazo vino con defectos medidos en píxeles, no con impresiones.

| Versión | Veredicto | Ejes bajos | Defectos |
|---|---|---|---|
| v1 | RECHAZADO | — | decena 5 truncada, wrap 9+1, panel derecho medio vacío |
| v2 | RECHAZADO | 5,6,7,8 | 8 defectos: objetivos táctiles de 20 px, medida de 73 caracteres, cuentas sin contraste… |
| v3 | RECHAZADO | 7=2, 8=2 | el objetivo de 44 dp existía **invisible**; anexo recortado; error sin recuperación |
| v4 | RECHAZADO | 2=2, 8=3 | el reparto del alto **empeoró**: 25 % de franja muerta; error en verde; play mientras «leyendo» |
| v5 | **APROBADO** | ninguno | todos los ejes ≥4 |
| v6 | pulido | — | los 4 refinamientos no bloqueantes del crítico |

**Puntuación final (v5):** ancho 5 · alto 4 · jerarquía 5 · scroll 5 · coherencia Lumen 4 ·
legibilidad 5 · táctil 5 · estados 4.

## Lo que el crítico encontró y una autoevaluación no habría visto

1. **Un objetivo táctil invisible no es un objetivo.** El área de 44 dp existía con
   `background: transparent`. El crítico midió el glifo (19×10 px), no el área declarada. Y señaló
   la ironía: el arreglo sí estaba en el anexo de estados, pero no se había propagado a las
   pantallas principales.
2. **Añadir márgenes fijos por tamaño empeora el reparto del alto.** La v4 dejó 91 dp muertos
   (25 % del alto) en la pantalla pequeña. Se resuelve con columna flex y `space-between`, con un
   único padding inferior de 16 dp para los tres tamaños — verificado por el crítico midiendo el
   borde del botón al borde de pantalla en las tres maquetas.
3. **La semántica del color importa.** Un icono de error en verde miente: el verde es éxito. El
   token de advertencia es el gold (`#8A6418` / `#E3C27A`).
4. **El estado tiene que decir la verdad.** Una barra de audio con icono de play mientras dice
   «leyendo en voz alta» es una contradicción; un vacío importado de una búsqueda no es el vacío
   del Rosario.

## Pulido aplicado en v6

- La columna derecha también ancla su último bloque abajo: holgura inferior unificada a 16 dp
  (era 19/41/11 según tamaño).
- El «Aa» de la caja de texto largo vuelve a `selectionTint` + secundario, y recupera el disco de
  play en su cabecera.
- El bloque skeleton de Carga recibe los 16 dp de padding inferior del panel real.
- El anillo del icono inactivo se refuerza por encima de 1.27:1.

## Qué se está mirando

Maquetas HTML renderizadas a PNG con los tokens reales de Lumen, a 844×390, 915×412 y 740×360 dp,
en claro y oscuro, más cinco estados (carga, error, texto largo, audio activo, vacío). **No son
capturas de la app corriendo**: no hay emulador en el equipo. El código Compose correspondiente
está implementado en `RosaryScreen.kt` y compila (`assembleDebug`, APK 1.8.27 / versionCode 9).

Maquetas: `docs/ui/landscape/shots/rosary-v1.png` … `rosary-v6-aprobado.png`.
