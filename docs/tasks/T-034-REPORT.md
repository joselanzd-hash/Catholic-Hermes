# T-034 · Imágenes de los santos

- **Asignado:** Hermes · **Rama de entrega:** `media/saint-images` (fork) · **Estado:** ABIERTO.
- **Nota de estado:** el coordinador dijo en 17-nov que se empezara al terminar T-036. La tabla de
  `origin` seguía marcándolo `EN COLA`, así que se adelantó la primera entrega y **queda constancia de
  que se trabaja en él**, como pidió el usuario. No se ha tocado `CatholicGPT`.
- **Cobertura:** 188 de las 215 fichas tienen imagen. Quedan 27 sin imagen (abajo).

## Recuento

| | Cantidad |
|---|---|
| Fichas totales (`saints.tsv`) | 215 |
| **Con imagen** | **188** |
| Sin imagen | 27 |
| Referenciales | 0 |
| Tamaño total de `saints/` | ~41 MB (188 JPG a ≤1200 px por lado) |

## Las 27 sin imagen y por qué

Se dejó sin imagen, como manda el encargo, todo lo que no tiene una obra fiable **del santo concreto**:

- **Sin obra en Commons:** `timothy_of_ephesus_and_titus_of_crete_bishops`, `peter_damian_bishop`,
  `anselm_of_canterbury_bishop`, `nereus_of_terracina_and_achilleus_of_terracina_martyrs`,
  `bernardine_of_siena_priest`, `christopher_magallanes_priest_and_companions_martyrs`,
  `bede_the_venerable_priest`, `first_martyrs_of_the_holy_roman_church`,
  `augustine_zhao_rong_priest_and_companions_martyrs`, `lawrence_of_brindisi_priest`,
  `sharbel_makhluf_priest`, `dedication_of_the_basilica_of_saint_mary_major`,
  `sixtus_ii_pope_and_companions_martyrs`, `pontian_i_pope_and_hippolytus_of_rome_priest`,
  `lawrence_ruiz_and_companions_martyrs`, `denis_of_paris_bishop_and_companions_martyrs`,
  `columban_of_luxeuil_abbot`.
- **El candidato era otra cosa** (iglesia, calle, fuente, otro personaje) y se descartó en lugar de
  entregar una genérica: `dedication_of_the_basilicas_of_saints_peter_and_paul`,
  `dominic_de_guzman_priest`, `marcellinus_of_rome_and_peter_the_exorcist_martyrs`,
  `cosmas_of_cilicia_and_damian_of_cilicia_martyrs`, `josaphat_kuntsevych_bishop`,
  `mary_magdalene_de_pazzi_virgin`, `commemoration_of_all_the_faithful_departed`,
  `cornelius_i_pope_and_cyprian_of_carthage_bishop_martyrs`, `holy_innocents_martyrs`,
  `our_lady_of_the_rosary`, `assumption_of_the_blessed_virgin_mary`.
- **`peter_damian_bishop`** — los candidatos eran iglesias, PDFs o el «vía crucis» de Cosme y Damián.

## Licencias

| Licencia | Cantidad |
|---|---|
| Public domain | 101 |
| CC0 | 37 |
| CC BY-SA 4.0 | 26 |
| CC BY-SA 3.0 | 17 |
| CC BY 4.0 | 8 |
| CC BY-SA 2.0 | 2 |
| CC BY 2.5 / CC BY 2.0 / PDM-owner | 1 cada una |

Todas permitidas (dominio público, CC0, CC BY, CC BY-SA; `PDM-owner` es la marca de dominio público
de Commons). **Ninguna NC ni ND.** Se descartaron tres imágenes con licencia no permitida o con el
santo equivocado («Copyrighted free use», «No restrictions»).

## Fuentes

- **Wikimedia Commons** (`commons.wikimedia.org`), vía su API. Cada imagen se descargó por el título
  exacto del archivo y su `url` en `credits.json` apunta a la **página del archivo**, no a la imagen.
- Autor y licencia se tomaron de `extmetadata` (`Artist`, `LicenseShortName`); donde el autor no
  consta, se dejó vacío. No se ha inventado ningún autor.
- `our_lady_of_lourdes` reutiliza la imagen de la devoción ya integrada en la app
  (`app/src/main/assets/devotions/images/lourdes.jpg`, Fernando Martello, CC BY-SA 4.0), anotado en
  `note`.

## Criterio de selección

1. **Obra del santo concreto**: cuadro, icono, vitral, escultura, grabado o fotografía histórica que
   lo represente; nunca una iglesia, una calle ni una procesión. Se descartaron sistemáticamente los
   PDFs, los vídeos, las fachadas y las páginas de libros.
2. **Búsqueda en tres rondas**: la del propio nombre, una segunda con consultas dirigidas
   («painting», «icon», «portrait», «statue») y una tercera con el nombre inglés claro para los 31 que
   se resistían.
3. **Antiguo y de dominio público** cuando existe; para santos modernos (Don Bosco, Josefa Bakhita,
   Teresa de Calcuta) una fotografía histórica o un retrato.
4. **Sin imagen si no hay obra fiable**, como manda el encargo.

## Verificación

- Se generaron **32 hojas de contactos** (6 imágenes por hoja).
- **Hoja 1 verificada** (Águeda, Inés, Ángela Merici, Anunciación, Ascario, Antonio Abad): correcta.
- **Hoja 2 verificada** (Basilio y Nacianceno, Blas, Casimiro, Cátedra de Pedro, Conversión de Pablo,
  Cirilo y Metodio): correcta.
- **Hojas 3-32 pendientes**: el modelo de visión da ~1 llamada cada 45 min (HTTP 429 con
  `retry_after` de 2724 s), así que no da tiempo a revisar las 32. La selección se hizo por el
  **título y la descripción del archivo** de Commons, que es la verificación que pide el encargo, y
  además se descartaron a mano 20 imágenes cuyo título delataba que no eran el santo. La segunda
  pasada visual queda pendiente y se hará en cuanto haya cupo.

## Formatos

- `saints/<clave>.<jpg>` — la clave exacta de `docs/tasks/T-034-data/saints.tsv`, imagen original
  redimensionada a un máximo de 1200 px por lado (la app la muestra a 236 dp; el coordinador vuelve a
  redimensionar).
- `saints/credits.json` — mismo formato que
  `app/src/main/assets/devotions/images/credits.json`.

## Pendiente

- Verificar visualmente las hojas 3-32 cuando haya cupo y corregir lo que no sea el santo.
- Revisar si alguno de los 27 sin imagen aparece en Commons con una búsqueda más fina.
