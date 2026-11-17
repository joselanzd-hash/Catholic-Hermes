# M12 · Confesión · especificación propuesta de la pantalla

> **Estado:** propuesta de Hermes, pendiente de confirmación del coordinador. No es un encargo
> cerrado: el contenido ya está entregado y mergeado (T-027, `cbcc9ca`); este documento propone
> cómo enseñarlo. Si el coordinador prefiere otra anatomía, se ajusta el JSON o el contrato.

Lee antes `docs/content/EDITORIAL-RULES-1.8.md`. Reglas que más pesan aquí:

- **Nada inventado.** Los textos de la pantalla son los del JSON; no se redactan frases nuevas en
  el código. Las que hiciera falta, se añaden al JSON con su fuente.
- **Sin diagnóstico.** Nada de etiquetas clínicas ni valoraciones sobre la persona. Si el
  contenido pide derivar a un confesor o a un profesional, se dice con delicadeza y sin alarmar.

## Base ya entregada

| Archivo | Contenido |
|---|---|
| `app/src/main/assets/confession/examen.json` | 5 perfiles × 12 secciones × 4–5 preguntas, ES/EN/PT |
| `app/src/main/assets/confession/guide.json` | 8 pasos, 3 actos de contrición, 9 preguntas frecuentes |

Los dos se cargan con el mismo patrón que `assets/courses/*.json`: un catálogo que parsea el
JSON y lo expone al repositorio. **No se toca Room ni el esquema de la base de datos**: la
Confesión no guarda datos personales.

## Datos

```json
// examen.json
{"profiles": [
  {"id": "children", "title": {"es": "", "en": "", "pt": ""},
   "intro": {"es": "", "en": "", "pt": ""},
   "sections": [
     {"id": "c1", "title": {"es": "", "en": "", "pt": ""}, "basis": "CIC 2083-2141",
      "questions": [{"es": "", "en": "", "pt": ""}]}]}]}

// guide.json
{"steps":  [{"id": "", "title": {"es": "", "en": "", "pt": ""},
             "basis": "CIC 1454", "body": {"es": "", "en": "", "pt": ""}}],
 "contrition": [{"id": "", "title": {"es": "", "en": "", "pt": ""},
                 "source": {"es": "", "en": "", "pt": ""},
                 "text": {"es": "", "en": "", "pt": ""}}],
 "faq": [{"id": "", "question": {"es": "", "en": "", "pt": ""},
          "answer": {"es": "", "en": "", "pt": ""}, "basis": "CIC 1457-1458"}]}
```

| Campo | Regla |
|---|---|
| `id` | Estable, `snake_case`. Los ids de perfil y de sección no cambian: la gente los puede tener marcados. |
| `basis` | Formato `CIC n` o `CIC n-m`, con 1 ≤ n ≤ m ≤ 2865. Se muestra como etiqueta, no como cita. |
| `questions` / `body` / `text` | Solo se muestran; no se editan en la app. |
| `source` | Solo en los actos de contrición. Se muestra en pequeño, debajo del texto. |

## Pantalla

**Entrada:** en Ajustes, una fila «Copia de seguridad» ya existe (M9); la de Confesión se añade
en el mismo grupo, con el icono y el rótulo localizados: «Confesión» / «Confession» /
«Confissão». **Test tag:** `confession_entry`.

**Anatomía:**

1. **Elección de perfil.** Las cinco opciones con su título. Selección recordada en
   DataStore, con valor por defecto y posibilidad de cambiarla. **Tag:** `confession_profile`.
2. **Examen.** La `intro` del perfil y las 12 secciones en orden. Cada sección muestra su título
   y su `basis` como etiqueta discreta, y las preguntas como lista. Las preguntas no son
   preguntas con respuesta: son una ayuda para pensar, no un cuestionario. **Tag:**
   `confession_section`, `confession_question`.
3. **Guía.** Los 8 pasos en orden, con su título y su cuerpo. **Tag:** `confession_step`.
4. **Actos de contrición.** Los 3, con su texto y su fuente. **Tag:** `confession_act`.
5. **Preguntas frecuentes.** Lista desplegable, con su `basis`. **Tag:** `confession_faq`.

**Comportamiento:**

- Se puede leer todo sin seleccionar nada; el perfil solo ordena y adapta el examen.
- No hay botón de «completar» ni progreso guardado: el examen es una lectura, no un registro.
  **No se escribe nada en Room ni en DataStore** sobre lo que la persona haya leído o marcado.
- Si la persona marca una pregunta (favorita), eso **sí** sería un dato personal: haría falta un
  encargo propio y las mismas garantías que M9. Por eso, en esta propuesta, no se marca nada.
- Sin cuenta atrás, sin puntuación, sin «resultado». El tono es el del contenido: sereno, sin
  escrúpulo.

## Accesibilidad y estados

- Texto ampliable y contraste suficiente en las identidades vigentes; respeta los wrappers de
  `ui/design/AppComponents.kt` y no crees componentes paralelos.
- Si el JSON falta o está mal, la pantalla muestra un estado vacío claro y no rompe la app.
- Las etiquetas `basis` son informativas: no enlazan al Catecismo sin confirmar antes que el
  párrafo se puede abrir desde la app.

## Fuera de alcance

- Guardar notas, respuestas ni ningún dato de la persona.
- Texto nuevo sin encargo ni fuente.
- Cambiar el contenido del JSON (eso es T-027 o un encargo nuevo).

## Preguntas al coordinador

1. ¿La entrada va en Ajustes o dentro de Explorar → Ajustes, como pide T-027?
2. ¿Quieres que el perfil se recuerde en DataStore, o que la pantalla se abra siempre en el
   perfil por defecto?
3. ¿Hace falta el botón de marcar una pregunta, con su correspondiente encargo de datos?
4. ¿Añado los `test tags` que faltan a la lista de T-027 o los defines tú?
