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

## Parte B · Cuatro emblemas solo símbolo

Se sustituyeron los cuatro PNG por versiones sin texto legible a 44 dp. Los cuatro miden 256×256 px y conservan transparencia.

| Emblema | Solución | Origen | Derechos |
|---|---|---|---|
| `hermanitas-pobres-psdp` | Recorte del símbolo naranja del logotipo oficial (260×290 px antes de reducir) | Web oficial de las Petites Sœurs des Pauvres[6] | Derechos: Petites Sœurs des Pauvres |
| `hijas-san-pablo-fsp` | Recorte del libro con órbita del logotipo oficial (400×360 px antes de reducir) | Web oficial de las Figlie di San Paolo[7] | Derechos: Figlie di San Paolo |
| `rogacionistas-rcj` | Archivo `Logo_Rogazionista2023_Senza_nome.png` del paquete oficial de logo RCJ | Web oficial de los Rogacionistas[8] | Derechos: Congregazione dei Padri Rogazionisti del Cuore di Gesù |
| `san-jose-cluny-sjc` | Corazón vectorial oficial rasterizado, sin las letras SJC | Web oficial de las Sœurs de Saint Joseph de Cluny[9] | Derechos: Sœurs de Saint Joseph de Cluny |

La lámina de verificación quedó en `docs/tasks/T-065-data/emblemas-lamina.png`. Se actualizaron las cuatro entradas de `app/src/main/assets/orders/credits.json` con `source`, `url`, `license` y la nota exacta de recorte o rasterizado.

No se encontró una versión «solo símbolo» publicada para las Hermanitas de los Pobres ni para las Hijas de San Pablo, así que se aplicó la regla del encargo: recortar el símbolo del logotipo oficial con más de 200 px por lado. Los Rogacionistas sí publican una variante `Senza nome` en su paquete oficial; las Cluny publican el corazón como SVG independiente.

No se han tocado Kotlin, `ui/`, versiones, firma, Room, contenido ni `dist/`.

## Sources

[1] https://covenantcatholic.org/prayers-devotions/the-chaplet-of-st-michael — Covenant Catholic · Chaplet of St Michael
[2] https://seashorestanthony.org/tuesday.html — Seashore St Anthony Shrine · St Anthony Tuesdays
[3] https://www.piercedhearts.org/treasures/devotions/to_saints/prayers_saintjoseph.htm — Pierced Hearts · St Joseph devotion
[4] https://iveinarabic.org/es/rosarios-y-coronas — IVE · Rosarios y coronas
[5] https://santaritalondrina.com.br/p/terco-de-santa-rita-de-cassia — Santuário Santa Rita · Terço
[6] https://petitessoeursdespauvres.org/wp-content/uploads/2025/11/pspd-logo-updated.webp — Petites Sœurs des Pauvres · logo oficial
[7] https://www.paoline.org/wp-content/uploads/2024/03/EN-Logo-2.png — Daughters of St Paul · logo oficial
[8] https://www.rcj.org/download/baixar/logo-rcj/spagnolo — Rogacionistas RCJ · descarga oficial del logo
[9] https://www.sj-cluny.org/wp-content/uploads/2025/05/coeur.svg — Sisters of St Joseph of Cluny · corazón oficial
