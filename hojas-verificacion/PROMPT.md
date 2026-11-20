# Verificación visual de las imágenes de T-044

Verifica cada imagen contra la devoción que le corresponde. **No des por hecho que un nombre de
fichero es correcto**: el nombre lo eligió un script y la descarga la confirmó la licencia, pero
nadie ha mirado la imagen.

## Reglas

- **SÍ** — es una imagen de la Virgen de esa advocación: talla, icono, cuadro, mosaico, vitral o
  estatua que la representa (con el Niño o según el tipo de la advocación). Para las apariciones
  sin imagen antigua (Knock, Banneux, Beauraing, Pontmain) vale la **imagen oficial del
  santuario** que representa la aparición: mosaico, vitral o estatua.
- **NO** — es una iglesia, fachada, plaza, calle, paisaje, procesión en la que no se distingue la
  imagen, un libro, un documento, o **otra devoción o santo distinto**. También **NO** si es una
  Virgen genérica que no se corresponde con la advocación (una Visitación para una Asunción).
- **DUDOSO** — no se distingue con claridad; describe lo que ves.

## Hojas

`h1.png` a `h3.png`, 6 imágenes por hoja (la última con las que queden). El número que aparece
bajo cada imagen es el orden dentro de la hoja y el id es el que va en el fichero.

| # | id | Devoción | Lugar |
|---|---|---|---|
| 1 | `nuestra_senora_knock` | Nuestra Señora de Knock | Knock, Irlanda |
| 2 | `nuestra_senora_vailankanni` | Ntra. Sra. de la Salud de Vailankanni | Tamil Nadu, India |
| 3 | `virgen_banneux` | Ntra. Sra. de Banneux: Virgen de los Pobres | Bélgica |
| 4 | `virgen_covadonga` | Ntra. Sra. de Covadonga | Asturias, España |
| 5 | `virgen_czestochowa` | Ntra. Sra. de Częstochowa | Jasna Góra, Polonia |
| 6 | `virgen_desamparados_valencia` | Ntra. Sra. de los Desamparados | Valencia, España |
| 7 | `virgen_itati` | Ntra. Sra. de Itatí | Corrientes, Argentina |
| 8 | `virgen_la_salette` | Ntra. Sra. de La Salette | Isère, Francia |
| 9 | `virgen_rocio` | Ntra. Sra. del Rocío | Almonte, Huelva |
| 10 | `virgen_socavon` | Ntra. Sra. del Socavón | Oruro, Bolivia |
| 11 | `virgen_suyapa` | Ntra. Sra. de Suyapa | Tegucigalpa, Honduras |
| 12 | `virgen_urkupina` | Ntra. Sra. de Urkupiña | Quillacollo, Bolivia |
| 13 | `virgen_zapopan` | Ntra. Sra. de Zapopan | Jalisco, México |

## Formato de respuesta

Una tabla por hoja:

| # | id | Veredicto | Lo que se ve (prueba) |
|---|---|---|---|
| 1 | `nuestra_senora_knock` | SÍ / NO / DUDOSO | … |

Y al final: **retirar** (los que fallen) y **sustitución propuesta** (si la hay, con el título
exacto del archivo de Commons).

## Contexto que ya está verificado (no lo repitas)

- La **licencia** de cada fichero se comprobó contra la API de Commons: solo dominio público,
  CC0, CC BY y CC BY-SA. Nada de NC/ND.
- El **título, la descripción y las categorías** de cada fichero se consultaron en Commons antes
  de elegirlo, y están en `devotions/credits.json`. El nombre solo no basta, pero aquí ya está
  contrastado.
- Las devociones **sin imagen** (de momento) son: `virgen_beauraing`, `nossa_senhora_nazare_portugal`,
  `maria_auxiliadora`, `virgen_paz_el_salvador`, `nossa_senhora_nazare_belem`,
  `madre_buen_consejo_genazzano`, `maria_desatadora_nudos`, `nuestra_senora_pontmain`,
  `virgen_montserrat`, `virgen_loreto`, `virgen_del_quinche`, `virgen_walsingham`.
