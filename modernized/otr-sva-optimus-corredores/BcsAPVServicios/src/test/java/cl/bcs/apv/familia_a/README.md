# BcsAPVServicios - characterization tests, family A (contracts, recaudacion, consultas)

Behaviour ids are `APV-A-NNN`. Every test method is named `apvA<NNN>_<slug>` and carries a comment `// legacy File.java:line`
with the legacy code it pins. BUSINESS_RULES.md has no rule of this module: the oracle is the legacy code itself.
The only rule test is `Rule076SinCredencialesNiMainTest` (RULE-076).

## What a test does

legacy JSON -> adapter -> real domain use case on H2 -> adapter -> JSON, through
`ApvPipelineDeCaso.crear(...)` and `pipeline.invocar("cl.bcs.APV.<Clase>", json)`.
The expected value (`cases/<id>/expected.json`) was produced by EXECUTING the unmodified legacy class
(`legacy-oracle/record.sh`), never typed by hand. It holds the answer (or the exception), the tables listed in
`case.json:tablas`, the calls to collaborators outside APV, and the sequence values requested.

## Legacy class -> use case under test -> test class

| Legacy class | Use case (Step 2) | Test class | Cases |
|---|---|---|---|
| CrearContratoSAAPV | CrearContratoServicio | CrearContratoSAAPVCasosTest | 46 |
| GrabarContratoSAAPV | GrabarContratosServicio | GrabarContratoSAAPVCasosTest | 51 |
| ModificarContratoSAAPV | ModificarContratosServicio | ModificarContratoSAAPVCasosTest | 26 |
| CambiarEstadoContratosSAAPV | CambiarEstadoContratosServicio | CambiarEstadoContratosSAAPVCasosTest | 36 |
| CambiarEstadoEnvioNotificacionContratoSAAPV | CambiarEstadoNotificacionContratosServicio | CambiarEstadoEnvioNotificacionContratoSAAPVCasosTest | 7 |
| CambiarEstadoRevocacionSAAPV | RevocarContratosServicio | CambiarEstadoRevocacionSAAPVCasosTest | 11 |
| CambiarAPlanActivoContratoSAAPV | CambiarContratosAPlanActivoServicio | CambiarAPlanActivoContratoSAAPVCasosTest | 11 |
| CambiarAPlanActivoContratoSAAPVWrapper | SolicitarCambioAPlanActivoServicio | CambiarAPlanActivoContratoSAAPVWrapperCasosTest | 6 |
| ValidaContratoSAAPV | ValidarContratoServicio | ValidaContratoSAAPVCasosTest | 40 |
| ConsultarClienteContratoSAAPV | ConsultarClienteContratoServicio | ConsultarClienteContratoSAAPVCasosTest | 15 |
| ObtenerFolioSuscripcion | ObtenerFolioSuscripcionServicio | ObtenerFolioSuscripcionCasosTest | 6 |
| AsociarPerfilesAPV | AsociarPerfilesServicio | AsociarPerfilesAPVCasosTest | 13 |
| ModificarRelacionLaboral | ModificarRelacionLaboralServicio | ModificarRelacionLaboralCasosTest | 7 |
| BusquedaContratoSAAPVRecaudacion | BuscarContratosParaRecaudacionServicio | BusquedaContratoSAAPVRecaudacionCasosTest | 14 |
| AsignarContratoARecaudacion | AsignarContratoARecaudacionServicio | AsignarContratoARecaudacionCasosTest | 12 |
| ValidarYAsignarContratosARecaudaciones | ValidarYAsignarContratosARecaudacionesServicio | ValidarYAsignarContratosARecaudacionesCasosTest | 9 |
| CargaMasivaArchivo | CargarContratosMasivoServicio | CargaMasivaArchivoCasosTest | 6 |
| ConsultarCustodiaPortafolioAPV | ConsultarCustodiaPortafolioServicio | ConsultarCustodiaPortafolioAPVCasosTest | 13 |
| ConsultarPreferenciaPagoPortafolio | ConsultarPreferenciaPagoPortafolioServicio | ConsultarPreferenciaPagoPortafolioCasosTest | 5 |
| AutorizacionCambiarAPlanActivoContratoSAAPV (hook `BcsAutorizaciones`, finding H7; cases APV-A-526..539) | AutorizacionCambioPlanActivoServicio (`cl.bcs.apv.application.autorizaciones`) | AutorizacionCambiarAPlanActivoContratoSAAPVCasosTest | 14 |

The hook is not a JavaCall operation (no registry entry, no `pipeline.invocar`): its cases use `"metodo": "autorizarEvento" | "rechazarEvento"`, the oracle
runner calls the typed method with the legacy `EntradaAutorizacion` JSON (`p_Lista[Evento]`) and `support/AutorizacionDeCaso` runs the typed service on the
case's H2 (real `CambiarContratosAPlanActivo` of the composition root). Its REST entry point (`AutorizacionesController`) is tested in
`infrastructure/web/AutorizacionesControllerTest`. Legacy defects KEPT and pinned: the success test compares the client id of the event with the id of the
OK message of the plan change (the number of portfolios), so a real client id never gets `EXITO` and the messages of the plan change are returned instead
(APV-A-527), while client id = number of portfolios gets `EXITO` and hides the errors of the plan change (APV-A-531); two or more events make the detail query
a syntax error, swallowed, and the loop throws NullPointerException (APV-A-534); empty list, bad detail value (null / not numeric / fewer than 4 parts) throw
outside the protected block (APV-A-533, 535-537); the folio of the plan change is the contract id; the legacy `logica` leaves the counter at 0 when there are no
contracts although it answers an error (APV-A-529); `rechazarEvento` does nothing (APV-A-539). The schema gained `AUT.EventoDetalle` (inferred from the inline SQL of the hook).

Use cases live in `cl.bcs.apv.application.contratos`. Total 348 equivalence cases (334 + 14 of the hook), plus
`ApvAInventarioTest` (every case directory is run by exactly one generated test; each golden has provenance),
`HarnessSelfCheckTest` (the harness rejects a wrong answer, wrong exception, wrong trace; masks work; no golden holds the
day it was recorded) and `Rule076SinCredencialesNiMainTest`.

## State against the Step 2 stubs

All use cases throw `UnsupportedOperationException("TODO Step 2: <Clase>")`, so all 334 equivalence tests are RED by design:
that red list is the Step 2 fix list. Nothing is skipped or disabled. Each class prints
`equivalence cases executed (<Clase>): N` and asserts N > 0. A missing golden or case fails.

## Run

```
JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-25.0.4.101-hotspot"
mvn -o -f modernized/otr-sva-optimus-corredores/pom.xml -pl :bcs-apv-api,:bcs-apv-javacall-adapter,:apv-service test \
    -Dtest='cl.bcs.apv.familia_a.*Test' -Dsurefire.failIfNoSpecifiedTests=false
```

(the two sibling modules must be in the reactor when offline). The surefire fork runs in JVM zone UTC
(`-Duser.timezone=UTC -Dfile.encoding=UTF-8 -Duser.language=en -Duser.country=US`, pom.xml): goldens are recorded under the same zone.

## Re-record / add a case

Everything below is under `src/test/resources/apv-a/`.
1. Add a `caso(...)` call in `legacy-oracle/tools/casegen/c_<modulo>.py` (literal input, seed rows, scripted collaborators,
   tables to dump; `L(file, regex)` resolves the legacy line citation at generation time).
2. `python legacy-oracle/tools/mkcases.py <this resources dir>/apv-a`  (writes `cases/APV-A-NNN_slug/{case.json,seed.sql}`).
3. Record against the legacy: `ORACLE_WORK=<scratch> bash legacy-oracle/record.sh [id-prefix]`
   (env: `LEGACY_ROOT`, `ORACLE_SKIP_COMPILE=1` to reuse compiled classes, `LEGACY_OVERLAY` + `ORACLE_OUT` for mutation checks).
   Needs JDK 25 on the PATH.
4. Review `expected.json` (does it match what the legacy code says?), then
   `python legacy-oracle/tools/mktests.py <apv-a resources dir> <familia_a java dir>` to regenerate the test classes.
The JDK helpful NullPointerException text inside any string of the answer (e.g. `Cannot invoke "String.equals(Object)" because "<local33>.CodigoTipoSolicitud" is null`)
names bytecode locals of the legacy class and cannot be produced by the modern code: it is replaced by `<NPE-MSG>` in the golden "salida" and on both sides of the comparison
(`Canon`). Regex (`Dump.NPE_MSG`): `Cannot (?:invoke|read|load from|store to|assign field|throw exception|enter synchronized block|exit synchronized block)\b[^\n]*? is null`.
Exceptions are compared by class only, so their text is not masked. Self-checks: `elTextoUtilDeLaNpeDelJdkSeNormalizaEnAmbosLados`, `laMascaraDeLaNpeNoEscondeOtrasDiferenciasDeMensaje`.
Day-dependent columns/keys are masked (`enmascarar`): never record a golden containing the date of the recording.

## Assumptions (to be reviewed by a person)

- H2 schema (`schema-apv-a.sql`) is INFERRED from the legacy SQL text (54 tables, views as tables, 3 sequences); it was not
  taken from HANA. Column order of `PER.DireccionFisica` was forced to match an INSERT without column list.
- Clock fixed at 2025-04-01T10:15:30Z. Three legacy flows read the real wall clock (AsignarContratoARecaudacion,
  ValidarYAsignarContratosARecaudaciones, enrolment dates in Grabar): those cases set `relojReal` and mask the dates.
- Collaborators OUTSIDE APV (Tesoreria, FondoMutuo, CuentaInversion, Plataforma, SocioNegocio, Portafolio, Custodia) and
  stored procedures are scripted doubles through the `Colaboradores` port; the trace of calls is pinned. Collaborator contract
  name = simple class name of the legacy POJO call; stored procedures use the name `StoredProcedure` with entrada
  `{procedimiento, entrada}`. Only the Java side of the stored procedures is pinned.
- APV classes that call other APV classes run for real in-process on both sides.
- The H2 rewrite of the HANA `UPDATE ... FROM JOIN` of CambiarAPlanActivo is in `h2-dialect/`; the verbatim HANA text is kept in
  `h2-dialect/hana/` and checked by `cmp` in record.sh. The modern code must keep the SQL text of that statement so the
  override key matches.
- H2 vs HANA exception message texts are not pinned.

## Not covered (reason)

- Grabar: the HANA unique-violation path (error code 301 parsed from the SQLException) cannot be reproduced on H2.
- Stored procedure bodies and the services of other modules (not in this module).

## Legacy defects KEPT and pinned (for SME review)

- AsociarPerfilesAPV: `i` is never incremented, all rows share the same id.
- ValidaContratoSAAPV: `FolioContratoSAAPV != null || !equals("")` NPE on null folio; `PeriodoTermino.compareTo("NOK")` compares the
  period text; the size>=2 branch is unreachable (Asignado always OK); default `[null]` list NPE; the cumulative M batch re-applies
  the list; IdEmpleador/IdCliente/IdPortafolio are not echoed when informed.
- CambiarEstadoContratosSAAPV: `Collectors.toMap` IllegalStateException on duplicate mandates; NPE on unknown folio; the input is mutated
  (REV/VIG); ANU of a non-PDI contract silently skipped; `p_DesdePac` null NPE; early return after partial updates.
- CambiarEstadoRevocacionSAAPV: one InContrato object is shared and mutated for all contracts.
- GrabarContratoSAAPV: `break` after "No posee distribucion" ends the loop; CLIENTE_BLOQUEADO `continue` does not consume the id;
  NEW persona cargo `IdTipoCargoPersona != 0` NPE when null; ELIMINA messages are never added; unique-violation parsing is HANA specific.
- ModificarContratoSAAPV: the log builder throws IndexOutOfBounds (swallowed, no AgregarLog without DetalleOrigen rows); messages not added.
- CrearContratoSAAPV: only the first contract is processed (break); ERROR_REGIMEN_VACIO is unreachable (IndexOutOfBounds first);
  `PorcentajeParcial.equals(BigDecimal.ZERO)` is scale sensitive (0.0 passes); ERROR_CONTRATO_NO_VIGENTE is returned after Grabar already wrote.
- AsignarContratoARecaudacion: if there are updates the inserts are dropped (else-if); month comparison uses the real clock.
- ValidarYAsignarContratosARecaudaciones: DROP of the temp table per row (2+ rezagos -> SQLException); FolioContratoSAAPV never filled
  ("null" in the message); NPE when there are no REC rows.
- BusquedaContratoSAAPVRecaudacion: an ambiguous search answers 0.
- ConsultarCustodiaPortafolioAPV: a parallel stream adds messages non thread-safely (cases keep <= 1 zero-custody row); NPE when the portfolio is unknown.
- CambiarAPlanActivoContratoSAAPV: the global GBL table is never cleaned.

## Requests / seams

None outstanding: the factory `ApvPipelineDeCaso.crear` was enough for all cases.
