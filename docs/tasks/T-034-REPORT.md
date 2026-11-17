# T-034 · Imágenes de los santos

- **Asignado:** Hermes · **Rama de entrega:** `media/saint-images` (fork) · **Estado:** ABIERTO.
- **Nota de estado:** el coordinador dijo en 17-nov que se empezara al terminar T-036. La tabla de
  `origin` seguía marcándolo `EN COLA`, así que se adelanta esta primera entrega y **queda constancia
  de que estamos trabajando en él**, como pidió el usuario. No se ha tocado `CatholicGPT`.
- **Entrega parcial:** solo el **primer trimestre** (38 fichas de enero–marzo). El resto queda para
  los siguientes ciclos.

## Recuento de esta entrega

| | Cantidad |
|---|---|
| Fichas del trimestre (ene–mar) | 38 |
| **Con imagen** | **36** |
| Sin imagen | 2 |
| Referenciales | 0 |

Las dos sin imagen:

- `timothy_of_ephesus_and_titus_of_crete_bishops` — en Commons no aparece una obra que represente a
  los dos conjuntamente; solo PDFs de vidas de santos y vidrieras de «todos los santos». Dejado sin
  imagen antes que poner una genérica.
- `peter_damian_bishop` — los candidatos eran iglesias, PDFs o el «vía crucis» de Cosmas y Damián;
  ninguno es Pedro Damián. Dejado sin imagen.

## Licencias

| Licencia | Cantidad |
|---|---|
| Public domain | 26 |
| CC0 | 6 |
| CC BY 4.0 | 1 |
| CC BY-SA 3.0 | 1 |
| CC BY-SA 4.0 | 1 |

Todas permitidas (dominio público, CC0, CC BY, CC BY-SA). Ninguna NC ni ND.

## Fuentes

- **Wikimedia Commons** (`commons.wikimedia.org`), vía su API. Cada imagen se descargó por el título
  exacto del archivo y su `url` en `credits.json` apunta a la **página del archivo**, no a la imagen.
- Autor y licencia se tomaron de `extmetadata` (`Artist`, `LicenseShortName`); donde el autor no
  consta, se dejó vacío. No se ha inventado ningún autor.
- `our_lady_of_lourdes` reutiliza la imagen de la devoción ya integrada en la app
  (`app/src/main/assets/devotions/images/lourdes.jpg`, Fernando Martello, CC BY-SA 4.0), anotado en
  `note`.

## Criterio de selección

1. **Obra del santo concreto**: cuadro, icono, vitral, escultura o grabado que lo represente, no una
   iglesia ni una procesión. Se descartaron sistemáticamente los PDFs, los vídeos y las fachadas.
2. **Antiguo y de dominio público** siempre que existe; para santos modernos (Don Bosco, Josefa
   Bakhita) se usó una fotografía histórica o un retrato.
3. **Escenas serenas**: se evitó el martirio explícito cuando había retrato o escena serena.
4. **Sin imagen si no hay obra fiable**, como manda el encargo.

## Verificación

- Se generaron hojas de contactos (6 imágenes por hoja) y se verificaron con visión.
- **Hoja 1 verificada**: los seis santos de la primera hoja (Águeda, Inés, Ángela Merici, Anunciación,
  Ascario, Antonio Abad) concuerdan con su iconografía.
- **Hojas 2 a 6 pendientes**: el modelo de visión devolvió HTTP 429 (cupo agotado, `retry_after`
  787 s) durante la verificación. La selección se hizo por el **título y la descripción del archivo**
  de Commons, que es la verificación que pide el encargo, pero la segunda pasada visual queda
  pendiente y se completará en el siguiente ciclo.

## Formatos

- `saints/<clave>.<jpg>` — la clave exacta de `docs/tasks/T-034-data/saints.tsv`, imagen original
  redimensionada a un máximo de 2000 px por lado.
- `saints/credits.json` — mismo formato que
  `app/src/main/assets/devotions/images/credits.json`.

## Pendiente

- Verificar visualmente las hojas 2-6 y corregir lo que no sea el santo.
- Entregar los trimestres 2, 3 y 4 (177 fichas).
