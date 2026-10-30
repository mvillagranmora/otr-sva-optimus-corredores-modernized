# BcsReplica: por que el pack le exige 60 reglas P0 (investigacion, 2026-10-30)

Solo lectura: no se edito el plugin, ni `BUSINESS_RULES.md`, ni `extract-rules.modules.json`. Scripts del pack en
`C:/Users/m.villagran.mora/.claude/plugins/cache/claude-plugins-official/code-modernization/1.0.0/scripts/`.

## Mecanismo exacto

1. `proof_pack.py:494` llama a `trace_rules.module_view(tr, subject["rel"])` y `proof_pack.py:495-496` toma las filas P0 de esa vista;
   `missing` = las que no estan `tested`.
2. `trace_rules.py:430-445` (`module_view`) decide que reglas "responde" un modulo. Una regla entra solo si, en el modulo, se cumple alguna de
   `slot["main"]` (codigo de `src/main` nombra `RULE-nnn`), `slot["tests"]` (un archivo de prueba lo nombra), `slot["claimed"]` (fila de la tabla de
   `TRANSFORMATION_NOTES.md` que nombra la regla Y un archivo existente del modulo, `trace_rules.py:294-313`), `slot["named"]` (las notas nombran el archivo
   fuente de la regla, p. ej. `Sesion.java`, `trace_rules.py:388`) o `slot["by"]` (una prueba que corrio se llama `...Rule076...`, `trace_rules.py:382`).
3. Si NINGUNA regla queda atada (`rows` vacio), `trace_rules.py:441-444` pone `tied = False` y devuelve TODAS las reglas (78; 60 P0). El comentario
   (`:433-434`) dice que es deliberado: "an empty list must never pass". `proof_pack.py:502-503` solo agrega la nota "Nothing ties a rule to this module by
   name, so every rule in BUSINESS_RULES.md was counted" y `:515` produce el texto "60 of 60 P0 rule(s) are not named by any test: RULE-001, RULE-002, ...".
4. No existe mapeo regla -> modulo en el pack. `extract-rules.modules.json` NO lo lee `trace_rules.py` ni `proof_pack.py`: solo lo ESCRIBE `make_shards.py:228`
   (lista de carpetas fuente para repartir `/modernize-extract-rules`) y lo consume esa extraccion. Por lo tanto editarlo no cambia nada en el pack.
   Tampoco se usan las columnas `Source` del archivo del sistema para atar un modulo: la vinculacion es solo por mencion del id en el codigo/pruebas/notas
   del modulo nuevo (o por nombre de archivo fuente en las notas).

## Causa en BcsReplica (datos)

- `analysis/otr-sva-optimus-corredores/equivalence/BcsReplica-verify-20261030.trace-rules.output.txt`: "Counted for this module: 78 rule(s) (nothing ties a rule
  to it by name, so every rule counts)"; P0 60 con estado `none`, P1 14, P2 4. `VERIFICATION.json` (BcsReplica): `tied False, counted 78, outOfScope 0, p0NoTests 60`.
- Las 78 reglas salen solo de 5 proyectos legacy (columna `Source` de `BUSINESS_RULES.md`): BcsPlataformaWeb (30 P0 + 6), BcsFilters (8 P0), BcsJobManager (10 P0 + 5),
  BcsUtils (8 P0 + 5: `Sesion*.java`, `SesionUtils.java`...), BcsPlataformaServicios (4 P0 + 2). Ninguna cita un archivo de `Replica/` (`grep -i replica BUSINESS_RULES.md` no da nada).
  `extract-rules.modules.json` (123 entradas) cubre solo BcsFilters, BcsJobManager, BcsPlataformaServicios y BcsPlataformaWeb: el modulo Replica nunca se extrajo, asi que
  no tiene reglas `RULE-nnn` propias. Sus reglas son B-REP-nn / D-REP-nn (46 ids, `reglas_rep.py`, `TRANSFORMATION_NOTES.md`), fuera del vocabulario `RULE-nnn` que busca el pack.
- El modulo nuevo no nombra ningun `RULE-nnn` en codigo ni pruebas. `TRANSFORMATION_NOTES.md:114` cita RULE-076 en una fila de "Que no se porta", pero la fila no nombra un archivo
  existente (`EnviarTablaReplica` sin extension no casa el patron `PATH`), asi que no cuenta como `claimed`.
- Conclusion: no es una entrada faltante ni un mapeo roto; es el fallback "ninguna regla atada = todas cuentan". Es el mismo caso de BcsCanalAgenteServicios.

## Comparacion con otros modulos (`VERIFICATION.json`, 2026-10-30)

| Modulo | tied | reglas contadas | P0 pedidas | Por que |
|---|---|---|---|---|
| BcsAPVServicios, BcsCrystalReports, BcsFondoMutuoServicios, BcsCuentaInversionServicios, BcsTesoreriaServicios, BcsPlataformaServicios (BcsCustodiaServicios: RULE-076 y RULE-077) | si | 1 (2) | RULE-076 | una prueba `Rule076...Test` (o un archivo de prueba que cita RULE-076) ata el modulo a esa sola regla; estaba `tested` |
| BcsAPVApi | si | 1 | RULE-076 (estado `code only`) | solo `src/main` la nombraba en la corrida de ese verify; por eso pedia solo RULE-076 |
| BcsUtils | si | 15 | 10 P0 (RULE-023..027, 031, 032, 076...) | sus reglas citan archivos de BcsUtils: las atan `main`/`tests`; todas `tested` |
| BcsPlataformaWeb (23), BcsJobManager (6), BcsFilters (11), BcsPortafolioServicios (2), BcsTesoreriaJobs (16) | si | varias | las suyas | reglas con `Source` en el modulo o con pruebas que las nombran |
| BcsCanalAgenteServicios, BcsReplica y otros 24 (26 en total sin atar: BcsBECB, BcsCartolas*, BcsGateway, BcsMesa*, BcsNormativa*, BcsOrden*, BcsSURA, BcsValo2, BcsApvJavaCallAdapter...) | no | 78 | las 60 P0 (RULE-001..) | ningun id atado; `TRANSFORMATION_NOTES.md:82` de CanalAgente dice textualmente que ningun RULE-nnn describe el modulo |

BcsCanalAgenteServicios: se le piden exactamente las mismas 60 P0 (RULE-001..RULE-078 menos las P1/P2), no una lista propia.

## Que reglas P0 se le exigirian a BcsReplica

- Con el criterio del pack tal como esta: las 60 P0 de otros modulos, ninguna relacionada con Replica (login, sesion, ServiceFilter, SSO, JobManager...). No tiene sentido de negocio.
- Con un mapeo por archivo fuente (`Source` en `Replica/`): 0 reglas. El comportamiento correcto para un modulo sin reglas P0 propias es no exigir ninguna, pero el pack hoy
  no lo permite (por diseno, para que "una lista vacia nunca apruebe").
- Regla transversal que otros modulos ya usaron para salir del fallback: RULE-076 (P0, "credenciales HANA en `main()`", `BcsUtils/Sesion.java:304-319`). El legacy de Replica SI tiene tres `main()`
  de prueba manual con conexion real (`EnviarTablaReplica.java:532`, `IniciarReplicaFull.java:62`, `IniciarReplicaDelta.java:62`; usan `connections.properties`, sin credenciales literales),
  y el modulo nuevo no tiene `main()` ni credenciales. Seria la unica P0 defendible: `RULE-076`.

## Opciones de arreglo, de menor a mayor costo

1. (Barata, sin tocar reglas, del lado del modulo) Anadir a BcsReplica una prueba `Rule076SinMainNiCredencialesTest` (escaneo de `src/main` sin `main(`, `jdbc:sap`, `password=`), como hicieron
   BcsAPVServicios/BcsCrystalReports/BcsFondoMutuoServicios. El pack contaria 1 regla (RULE-076). Es lo mismo que ya aceptaron esos modulos; no cubre ninguna regla propia de Replica,
   asi que se debe declarar como lo que es (regla transversal), no como cobertura del dominio.
2. (Media, decision del usuario) Extraer las reglas B-REP a `BUSINESS_RULES.md` con ids propios `RULE-079...` (prioridad y `Source: Replica/JAVA/BcsReplica/src/cl/bcs/replica/...`), via
   `/modernize-extract-rules` sobre Replica, y nombrar cada `RULE-0nn` en las pruebas de BcsReplica (los casos ya llevan B-REP/D-REP en su nombre: bastaria anadir el id RULE en `reglas_rep.py`/DisplayName).
   Con eso el modulo queda atado solo a sus reglas; si ninguna se califica P0 el pack dice "No rule this module answers for is rated P0" y pasa. No inventadas aqui: se proponen, no se agregaron.
3. (No sirve) Anadir una entrada de Replica en `extract-rules.modules.json`: el pack no lo lee.
4. (Fuera de nuestro alcance, cambio del pack, no editado) Que `module_view` use un mapeo explicito modulo -> reglas (p. ej. un `analysis/<system>/rule-modules.json`) o que un modulo
   cuyas reglas `Source` no estan en su carpeta cuente 0 reglas con una advertencia en lugar de las 78; o que `tied == False` sea un caveat y no un `gap` automatico.
   Afectaria a los 26 modulos que hoy estan en el mismo caso.
