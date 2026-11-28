# T-065 · Informe (entrega parcial A)

## Parte A · Días sugeridos de las coronillas

Se revisaron las 55 fichas y sus 55 archivos de evidencia en `docs/content/coronillas/sources/`. Solo se han cambiado `weekdays` cuando la fuente de la coronilla o una fuente católica reconocida da un día concreto; no se han inferido días por tradición oral ni por el texto genérico del sitio sobre los misterios del Rosario.

| Coronilla | Día | ISO | Justificación verificada |
|---|---|---:|---|
| `corona-san-miguel-raccolta` | martes | 2 | «...on Tuesdays, the day traditionally dedicated to the Holy Angels.»[1] |
| `san-antonio-padua-coronilla` | martes | 2 | «Tuesday was chosen as the day for this devotion because Saint Anthony entered into eternal life on a Tuesday.»[2] |
| `coronilla-san-jose-hozana` | miércoles | 3 | «The Church has traditionally assigned Wednesday as a special day of remembrance of St. Joseph.»[3] |
| `rosario-espiritu-santo` | domingo | 7 | Ya tenía weekdays = 7; la fuente IVE dice «Se sugiere realizarlo en día domingo en que recordamos la Pascua del Señor y el envío de su Espíritu».[4] |
| `santa-rita-coronilla` | miércoles | 3 | Ya tenía weekdays = 3; la fuente del Santuário dice «O terço de Santa Rita de Cássia é rezado todas as quartas-feiras as 18h30».[5] |

No se han tocado los otros 50 `weekdays`. En particular:

- Las páginas de Hozana repiten un texto general sobre los misterios del Rosario según el día (martes/viernes, jueves, lunes/sábado, miércoles/domingo); no es una recomendación para cada coronilla y por eso no se ha usado.
- El rosario de difuntos de Hozana menciona noviembre como mes de sufragio, pero el contrato solo admite `lent`, `advent` y `easter` en `season`; no se ha forzado ese dato en un campo que no existe para él.
- No se ha añadido `season` a ninguna ficha: ninguna fuente de las 55 recomienda explícitamente uno de los tres tiempos permitidos para su coronilla.

**Validador:** `python docs/tasks/tools/validate_chaplets.py --images app/src/main/assets/chaplets/*.json` → **55 files · 0 errors · 1 warnings** (la advertencia conocida de `coronilla-difuntos-45`, explicada en sus `notes`).

## Parte B

Pendiente: buscar las versiones solo símbolo de los cuatro emblemas y actualizar `orders/credits.json`.

## Sources

[1] https://covenantcatholic.org/prayers-devotions/the-chaplet-of-st-michael — Covenant Catholic · Chaplet of St Michael
[2] https://seashorestanthony.org/tuesday.html — Seashore St Anthony Shrine · St Anthony Tuesdays
[3] https://www.piercedhearts.org/treasures/devotions/to_saints/prayers_saintjoseph.htm — Pierced Hearts · St Joseph devotion
[4] https://iveinarabic.org/es/rosarios-y-coronas — IVE · Rosarios y coronas
[5] https://santaritalondrina.com.br/p/terco-de-santa-rita-de-cassia — Santuário Santa Rita · Terço
