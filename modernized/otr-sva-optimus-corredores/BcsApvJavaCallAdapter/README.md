# BcsApvJavaCallAdapter

Registro JavaCall y mapeadores JSON de las operaciones del servicio APV, sobre los records tipados de `BcsAPVApi`.

## Evidencia propia del adaptador (2026-10-30)

- Evidencia de equivalencia: `analysis/otr-sva-optimus-corredores/equivalence/BcsApvJavaCallAdapter/` (`cases.json` 128 casos, `fresh-cases.json` 399 casos, `EQUIVALENCE.json`, `FRESH_EQUIVALENCE.json`, `ORACLE_NOTES.md`). Los goldens son copias verificadas con hash de los de `equivalence/BcsAPVApi/`; ver `ORACLE_NOTES.md`.
- Prueba transversal propia: `Rule076CredencialesHardcodeadasTest` (RULE-076, con `Rule076Escaner`) en `src/test/java/cl/bcs/apv/adapter/javacall/`. Canario: `analysis/otr-sva-optimus-corredores/equivalence/canary/BcsApvJavaCallAdapter-20261030e/`.
- Pruebas: `mvn -o clean test -pl BcsAPVApi,BcsApvJavaCallAdapter` desde `modernized/otr-sva-optimus-corredores`.
