# BcsApvJavaCallAdapter: evidencia de equivalencia propia

## Evidencia propia del adaptador (2026-10-30)

Esta carpeta da al adaptador su propia evidencia de equivalencia, en el formato de `compare.py`. No hay un oraculo nuevo: el adaptador (mapeadores JSON de las operaciones JavaCall) es exactamente la pieza que produce las salidas `new/` de BcsAPVApi, de modo que el oraculo dual de `../BcsAPVApi/` ES el oraculo del adaptador. Se le da carpeta propia porque la verificacion busca `equivalence/<modulo>/cases.json` y `fresh-cases.json`.

### Que comparte con BcsAPVApi y por que

- Mismos casos, mismas salidas: `legacy/` y `new/` (128 casos por defecto), `fresh/legacy`, `fresh/new`, `fresh/input` (384 entradas frescas) y `fresh/*-verify-b` (15 entradas del verificador). Son copias byte a byte de `../BcsAPVApi/legacy`, `new`, `fresh/legacy`, `fresh/new`, `fresh/input`, `fresh/legacy-verify-b`, `fresh/new-verify-b` y `fresh/input-verify-b`; el hash SHA-256 de cada directorio (lista ordenada de archivos con su hash) coincide con el original: 129, 129, 385, 385, 385, 16, 16 y 16 archivos.
- Por que copias y no rutas `../BcsAPVApi/...`: `compare.py` rechaza rutas que salen de la carpeta del `cases.json` salvo con `--allow-outside`, y el paquete de verificacion juzga los archivos de casos sin ese indicador (todos los casos saldrian `missing`). Se probo: con `../` y sin el indicador, 128 de 128 `missing`.
- `cases.json` y `fresh-cases.json` son los de BcsAPVApi con dos cambios: `system` = `BcsApvJavaCallAdapter` y la etiqueta `new`; las rutas siguen siendo relativas a esta carpeta (sin el prefijo `../BcsAPVApi/`). Ids, titulos, notas, `leftOut`, `discarded` y `approvedInputs` (incluida la aprobacion de VB-V09, con su texto) se copiaron tal cual.
- No se tocaron los archivos de `../BcsAPVApi/`. Si alli se regeneran los goldens, esta copia debe regenerarse tambien (la prueba `ApvEquivalenciaGoldensTest` del adaptador sigue leyendo `../BcsAPVApi/`, o `APV_EQUIVALENCE_DIR`).

### Resultados de compare.py (sin `--allow-outside`)

```
py -3 -I compare.py cases.json       --out EQUIVALENCE.json
py -3 -I compare.py fresh-cases.json --out FRESH_EQUIVALENCE.json
```

- `EQUIVALENCE.json`: 128 casos, 128 ejecutados, 128 iguales, 0 difieren, 0 faltantes; autoverificacion superada.
- `FRESH_EQUIVALENCE.json`: 399 casos, 399 ejecutados, 398 iguales, 0 difieren, 1 diferencia aprobada (VB-V09, aprobada por el usuario el 2026-10-30), 0 faltantes; autoverificacion superada.

### Prueba transversal propia

`src/test/java/cl/bcs/apv/adapter/javacall/Rule076CredencialesHardcodeadasTest.java` (con `Rule076Escaner.java`): RULE-076 escanea el propio modulo (main y test): sin `main`, sin literal JDBC con usuario y clave ni el host:puerto de la regla, sin clave asignada a un literal no vacio; 2 metodos prueban el propio escaner. Canario: `../canary/BcsApvJavaCallAdapter-20261030e/` (literal JDBC falso en `src/main`: la prueba falla).
