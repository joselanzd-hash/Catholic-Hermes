# Santos en horizontal — informe del área

**Rama:** `ui/landscape-redesign` · **Veredicto del crítico: APROBADO** (v2, pulida en v3)

## El problema

- **L150-163:** la lista era un único `LazyColumn` con `AppHeroHeader` como primer item: en
  horizontal el arte más los filtros empujaban el calendario fuera de pantalla.
- **L164-194:** búsqueda y filtros eran *items del scroll*, así que desaparecían al bajar.
- **L260-270:** `SaintDetail` usaba `AppDetailHeader` sin `imageHeight` explícito → el arte por
  defecto de ~300 dp. En un teléfono de 390 dp de alto se comía la pantalla entera antes de la
  biografía.
- **L281-296:** «Todos los santos» y «Escuchar» viajaban dentro del scroll.
- **L114-120:** lista y ficha eran pantallas excluyentes: el ancho sobrante se desperdiciaba.

## La solución

`LandscapeTwoPane(leftWidthFraction = 0.40f)`, condición `form.isLandscape && form.heightDp < 500.dp`:

- **Izquierda, fijo:** cabecera de 52 dp (44 en 740) + buscador de 48 dp + chips de 44 dp, siempre
  visibles. Debajo, el calendario por meses con filas de 52 dp y **el santo abierto resaltado en
  `accentMuted`**, con el índice desplazado hasta la selección.
- **Derecha:** fila fija arriba con «‹ Todos los santos» y «Escuchar» (cápsulas de 44 dp). Debajo,
  miniatura de **128×88 dp** (104×76 en 740) junto al título, fecha, crédito de imagen, biografía,
  patronazgos y fuentes. Es el único scroll de lectura, con medida ≤640 dp.

Dos scrolls, ambos legítimos: el del índice y el de lectura. Ningún control esencial requiere
desplazar. Los 15 `testTag` conservados.

## El bucle con el crítico

| Versión | Veredicto | Ejes bajos | Bloqueantes |
|---|---|---|---|
| v1 | RECHAZADO | 7=3, 8=3 | chips inactivos de 37 dp; el santo abierto no se veía en el índice; faltaban los estados |
| v2 | **APROBADO** | ninguno | los tres cerrados y medidos |
| v3 | pulido | — | lupa, wrap ejercitado, viuda del crédito, placeholder plano |

**Puntuación final (v2):** ancho 5 · alto 5 · jerarquía 5 · scroll 5 · coherencia Lumen 4 ·
legibilidad 4 · táctil 5 · estados 4.

## Lo que el crítico encontró

1. **Los chips inactivos medían 37 dp, el activo 44.** Yo había puesto `min-height: 44px` y la hoja
   base lo sobrescribía: la métrica se cumplía en un control de tres. Ahora `height` explícito, y el
   crítico midió **44 dp exactos en las nueve cápsulas** de las tres maquetas.
2. **El índice no conectaba con la ficha.** Las maquetas enseñaban enero mientras la ficha mostraba
   un santo de febrero: dos columnas sin vínculo visual. El índice ahora arranca en la selección.
3. **Un glifo ambiguo.** El icono de búsqueda era un círculo con asta vertical: se leía como un
   pin, no como una lupa. El texto lo desambiguaba, pero el icono no comunicaba por sí solo.
   Redibujado con mango diagonal.
4. **Una prueba que no ejercitaba su rama.** La caja rotulada «wrap del título» mostraba un nombre
   que cabía en una línea: la regla se cumplía pero no se demostraba. Caja a 740 dp y nombre más
   largo; ahora parte en dos líneas.

## Dos falsos positivos que se descartaron midiendo

- La primera lectura de visión dijo que las maquetas estaban **recortadas por abajo**. Falso: lo
  que se corta es la lista del índice, que tiene scroll propio por diseño. Verificado recortando
  los últimos 420 px y comprobando los bordes redondeados.
- El crítico midió y **descartó** cuatro supuestos defectos: el gap «desigual» de los chips (1 px
  de antialias), el ellipsis «pegado» al chevron (19–25 px de holgura), el crédito «de bajo
  contraste» (es el token exacto, ~7:1) y la proporción del placeholder (según spec).

Ambas cosas importan: un crítico que inventa defectos gasta iteraciones en arreglar lo que no está
roto. Por eso al de v2 se le dieron las aclaraciones explícitas y se le pidió medir.

## Qué se está mirando

Maquetas HTML con los tokens reales de Lumen, a 844×390, 915×412 y 740×360 dp, en claro y oscuro,
más tres estados (sin resultados, sin santo elegido, nombre largo). **No son capturas de la app
corriendo**: no hay emulador en el equipo. El código Compose está en `SaintsScreen.kt` y compila
(`assembleDebug`, APK 1.8.27 / versionCode 9).

Maquetas: `docs/ui/landscape/shots/santos-v1.png`, `santos-v2.png`, `santos-v3-aprobado.png`.
