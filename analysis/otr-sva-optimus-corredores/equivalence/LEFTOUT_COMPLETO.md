# leftOut completo (2026-10-31a)

La lista `leftOut` de `equivalence/test-runs.json` tenia 85 lineas y el pack (`proof_pack.py`) lee solo las primeras 20 (y recorta cada una a 200 caracteres). El 2026-10-31a se consolido en 17 entradas, una por modulo o grupo, con un resumen de hasta 200 caracteres.

Este archivo conserva TODAS las lineas originales, LITERALES, agrupadas por modulo, con su numero de linea original `[n]` (la lista original completa esta en `verify-backups/test-runs.previous-20261031a-leftout.json`). Las lineas [1] (BcsReplica y BcsCanalAgenteServicios) y [62] (BcsJobManager, BcsPlataformaWeb, BcsAPVApi, BcsApvJavaCallAdapter) nombran varios modulos y se repiten en cada grupo; las lineas [0], [1] y [2] son del estado anterior a las correcciones de 2026-10-31a (ver la entrada nueva de cada modulo en `test-runs.json`).

Resumen que quedo en `test-runs.json` (una linea por grupo, <= 200 caracteres):

- BcsCanalAgenteServicios 20261031a: Parameters text fixed; 88/88 dev, 39/39 fresh same. Not covered: HANA, real GLOBAL TEMP table, row order w/o ORDER BY. Detail: LEFTOUT_COMPLETO.md
- BcsReplica 20261031a: 104 fresh inputs (45 own), none refused; malformed tried. Not covered: HANA triggers/views/DDL commit, endless day loops. HTML-escape gap now pinned. LEFTOUT_COMPLETO.md
- BcsAPVApi, BcsApvJavaCallAdapter: no INTENT.md; wrong-type inputs tried (VC-C04..C07); broken JSON not tried; logica needs HANA; no legacy oracle for typed-API libs. Detail: LEFTOUT_COMPLETO.md
- BcsCustodiaServicios: legacy QA refused (401), early fresh set 15 empty files proves nothing, HANA NEMO check, verify4 no fresh, verify5 F06 unstable (D-CUS-06), 14 same. LEFTOUT_COMPLETO.md
- BcsContabilidadServicios: legacy via shims on H2 (not TomEE/HANA); fresh F01-F16 only EnvioContabilidad/ReversaPreComprobante; 1 malformed (F16); secuencias/log not compared. LEFTOUT_COMPLETO.md
- BcsContabilidadJobs: fresh JF01-JF12 only the InterfazContable shell; other jobs need TomEE/HANA/SFTP; malformed not compared (CONT-J-62/63/64); RutaSalida not compared. LEFTOUT_COMPLETO.md
- BcsUtils: fresh F01-F14 pure API only; Timer, clave, sesion, HybridPasswordEncoder, Crystal client and approved deviations not compared; legacy gson 2.8.2/JDK 11 stand-ins. LEFTOUT_COMPLETO.md
- BcsCuentaInversionServicios: 23 fresh inputs, F03/F16/F20/F26 left out; many services not covered; H2 only (no HANA), PDF text only; 1 canary survived (dead code). LEFTOUT_COMPLETO.md
- BcsTesoreriaServicios: H2 only, schema inferred; Cloper, DistribucionPac, SOAP only static, collaborators scripted; 2 canaries survived (libreta:254, deposit branch). LEFTOUT_COMPLETO.md
- BcsAPVServicios: H2 only; collaborators scripted; REST by wiring tests; 20 fresh cover 11 of 64 operations; defects kept bug-for-bug; SME sign-off external; malformed not tried. LEFTOUT_COMPLETO.md
- BcsTesoreriaJobs: H2 only; opencsv 3.7 shim not the real jar; collaborators scripted; Quartz/files not proven; H2-H4 findings open; PG-BX01 replaced; 19 defects kept. LEFTOUT_COMPLETO.md
- BcsAPVJobs: H2 only; collaborators scripted; Quartz not proven; canary rv-tope-custodia (<= 0) survived; OBF-006/CMP-025/RVB-039 mask tablas (user a); OBF-101/102 marginal. LEFTOUT_COMPLETO.md
- BcsCuentaInversionJobs: H2 only; opencsv shim; Servicio-name validation deviation (user A, GA-29 test-only ctor); Step 1 support changes accepted; PLA config read assumed. LEFTOUT_COMPLETO.md
- BcsCrystalReports: 21 of 153 templates + 16 fresh rendered; fake inputs; JDK 11 not production runtime; masks documented; REST changes; open review findings; xls canary weak. LEFTOUT_COMPLETO.md
- BcsFondoMutuoServicios: H2 only, scripted collaborators; F3 W1/W2/FFMM/Normal verifies; deviations NOT approved: DESVIO-KIT-1, VDO-05/06, VF-NOR-11/36 (a person decides). LEFTOUT_COMPLETO.md
- BcsGateway: no legacy counterpart (nginx), own tests only. BcsPlataformaWeb login MVP: legacy QA over HTTP; ActualizarClave success, other channels, SSO, /call, menus not compared. LEFTOUT_COMPLETO.md
- General: legacy cannot run for some modules (TomEE 8/Java 8), no INTENT.md, trace-based; no dev cases generated (cases.json empty); pack reads 20 leftOut, 50 canaries. LEFTOUT_COMPLETO.md

## BcsCanalAgenteServicios

[0] 2026-10-30g BcsCanalAgenteServicios: 39 own fresh inputs, none refused by legacy; 2 differ (Parameters text of failed INSERT). Not covered: HANA, real GLOBAL TEMP table, row order w/o ORDER BY

[1] 2026-10-30g canaries SURVIVED (not in canaries): BcsReplica Sql.json HTML escape; BcsCanalAgenteServicios NombreTipoOperacion<->Movimiento swap (equal values in dev data). Evidence canary/*-20261030g

## BcsReplica

[2] 2026-10-30g BcsReplica: 104 fresh inputs (45 own), none refused by legacy; malformed tried (no INTENT.md). Not covered: HANA triggers/views/DDL commit, endless day loops. 1 own canary survived

[1] 2026-10-30g canaries SURVIVED (not in canaries): BcsReplica Sql.json HTML escape; BcsCanalAgenteServicios NombreTipoOperacion<->Movimiento swap (equal values in dev data). Evidence canary/*-20261030g

## BcsAPVApi / BcsApvJavaCallAdapter

[4] BcsAPVApi, BcsApvJavaCallAdapter: no INTENT.md; wrong-type inputs tried (VC-C04..C07, refused by both sides); broken JSON text not tried; logica(...) needs HANA, out of scope.

[62] BcsJobManager, BcsPlataformaWeb, BcsAPVApi, BcsApvJavaCallAdapter: platform/routing or typed-API libraries with no legacy oracle here; no fresh inputs.

## BcsCustodiaServicios

[5] 2026-10-10 BcsCustodiaServicios: QA legacy comparison attempted with the user's authorization (read-only). QA endpoints reachable; HANA QA reads succeeded. The legacy GWJAVA application refused the service call (HTTP 401 Invalid Session), so the legacy side produced no output to compare and no fresh-input case could be completed.

[6] 2026-10-10 BcsCustodiaServicios: the earlier fresh set at equivalence/fresh/BcsCustodiaServicios-verify2/ is placeholder-only — all 15 legacy/*.txt files are 0 bytes, so its 15-of-15 'differs' are artifacts of empty files, not behavioral differences. Not promoted to the canonical cases path; it proves nothing either way.

[7] 2026-10-10: a read-only diagnostic SELECT against ACC.Sesion (to explain the 401) was blocked by this environment's production-read guard and was not run.

[9] HANA verify 2026-10-08: BSCL_VIRTUAL.CUSTODIA_OPTIMUS queried (5454 rows, 172 distinct NEMOs). No NEMOs with transitional markers found — determinarNuevoNemo identity path verified on 20 real codes. CONFIGURACION_T and POSICION_CUSTODIA_T do not exist in HANA — adapter plain-SQL uses H2 placeholder table names; schema alignment required before HANA connection. Results in analysis/otr-sva-optimus-corredores/equivalence/fresh/BcsCustodiaServicios/nemo-fresh-check.txt.

[67] 2026-10-26 BcsCustodiaServicios verify4: no fresh inputs run. The ConsultarFlujoCustodia slice has no implementation to run, and the legacy GWJAVA QA refused sessions earlier (HTTP 401); the live QA re-run was not repeated here because QA is not a local instance.

[68] 2026-10-26 BcsCustodiaServicios verify5: fresh inputs F01..F15 (15 invented, valid records; INTENT.md does not exist so malformed records were allowed but none was tried: the slice reads typed database rows, not text records). F06 left out: the unmodified legacy is UNSTABLE on it (3 runs differ, D-CUS-06). 14 fresh inputs compared, all same.

## BcsContabilidadServicios

[10] 2026-10-15 BcsContabilidadServicios: the legacy ran through shims for its runtime (SQL/JavaCall/config/OACT) on H2, not on TomEE/HANA; HANA-specific SQL behavior beyond H2 MODE=LEGACY is not covered.

[11] 2026-10-15 BcsContabilidadServicios: comparison is of salida + table dumps through the module's JsonCanon (null keys dropped, numbers by value, unordered SQL arrays sorted); secuenciasSolicitadas and log text were not compared.

[12] 2026-10-15 verify5 BcsContabilidadServicios: fresh inputs F01-F16 cover only EnvioContabilidad and ReversaPreComprobante (the slice the legacy oracle can run); Centralizacion, devengo, interfaz, informe, GenerarJson and InterfazContable have no fresh legacy comparison here (trace-based tests only).

[13] 2026-10-15 verify5 BcsContabilidadServicios: one malformed record tried (F16, letters in SecuenciaComprobante): both sides throw JsonSyntaxException, only the class is compared; other malformed shapes were not tried.

## BcsContabilidadJobs

[14] 2026-10-16 verify2 BcsContabilidadJobs: fresh inputs JF01-JF12 cover only the InterfazContable job shell (the only job the legacy oracle can run here); CentralizaMultiSeleccionJob, CreaInterfazDiaria/Demanda and GenerarInformeClienteRelacionadoBatch need the TomEE Job Manager and HANA/SFTP, so they rest on trace-based tests.

[15] 2026-10-16 verify2 BcsContabilidadJobs: malformed records (non-numeric SecCmbte ids, bad base64) were not compared against the legacy: the OracleRunner records an exception without the job-shell outputs; unit tests CONT-J-62/63/64 pin them.

[16] 2026-10-16 verify2 BcsContabilidadJobs: RutaSalida is not compared (legacy wall-clock timestamp vs fixed clock); CONT-J-68 pins its format.

## BcsUtils

[17] 2026-10-19 verify3 BcsUtils: fresh inputs F01-F14 cover the pure API only (RutUtil, StringUtils, Utils pure subset, DateUtil, Fecha, copyPojo/List2Hash/getColumn, FileUtils/ZipUtil, Gson deserializers); no INTENT.md exists, so malformed records were tried (F01, F03, F04, F07, F08, F13).

[18] 2026-10-19 verify3 BcsUtils: not compared against the legacy: Timer (API changed to createTimer(LongSupplier), SLF4J), clave/PoliticaClave, sesion/* and security/HybridPasswordEncoder (new or DB-bound classes; RULE-021/024/025/031-034/071 rest on golden-driven tests), CrystalReports client, and the approved deviations RULE-025, RULE-032, RULE-031 (128+ chars), UTIL-56 (ZIP-027).

[19] 2026-10-19 verify3 BcsUtils: legacy gson 2.8.2 / commons 2.5 / JDK 11 stand in for the production pins (gson 2.6.2, commons-io 2.4, Java 8 JRE); BcsUtils/src/test/resources/golden/PROVENANCE.md records that json and collections re-recorded with gson 2.11.0 are identical.

## BcsCuentaInversionServicios

[20] 2026-10-19 verify1 BcsCuentaInversionServicios fresh inputs: 23 inputs (12 transaccional, 11 reportes), 40 output files. Left out: F03 (malformed empty date: both sides throw DateTimeParseException, no output), F20 (lower-case status: status-only output), F26 (index first value 0: both ArithmeticException), F16 (all outputs identical to a development output). Malformed records tried only in F03 and F20 because there is no INTENT.md.

[21] 2026-10-19 verify2 BcsCuentaInversionServicios: the verify1 difference F24-pdfTextos came from the modern test double recording blank PDF cells that the legacy never draws (GenerarArchivoSaldoCuentas.java:745: an empty text gives no line, so no showText). The double now skips blank cells and the SaldoCuentas tests compare pdfTextos (3 dev cases with 84/75/42 texts); the fresh modern side was re-run: 40 of 40 same. The comparison is of recorded PDF cell texts, not of a real PDF.

[22] 2026-10-19 verify1 BcsCuentaInversionServicios: not covered by fresh inputs: Anexo16, ClientesPorPagarYCobrar, InformeAttribution, GenerarReporteAcreedores, GuardarPatrimonioCaja, Eliminar*/Crear*OpcionesCobro, CargaMasiva, GenerarArchivoCargoAbono, ConsultaComparacionSaldo, ConsultarCartolaDeMovimiento, ConsultarMovimientosWS, SaldoCuentaInversion, ActualizarSaldoCargoAbono directly.

[23] 2026-10-19 verify1 BcsCuentaInversionServicios: HANA is not exercised. Everything runs on H2 with a modelled schema shared by oracle and modern code (DECIMAL(28,16), views as tables, PLA.Identidad 6x6): a schema-model error would not show. The SQLScript of the CrearCargoAbono stored procedure is not in the repo (only its Java side is pinned). Collaborator numerics are scripted. PDF layout is not compared (iText/PDFBox shims record text only); no production PdfGenerator adapter exists. ICU4J 4.6 and POI 3.17 stand in for 71.1 and the production version.

[24] 2026-10-19 verify1 BcsCuentaInversionServicios: a first canary on ActualizarSaldoCargoAbonoService.acumularSaldoEfectivo (add to subtract) SURVIVED: 0 tests failed, because those two helpers are dead code in production (kept only for the old ServiceFormulasTest; the live accumulation is SQL). Evidence: equivalence/canary/BcsCuentaInversionServicios-verify1/saldo-signo-codigo-muerto. It is not listed in canaries.

[25] 2026-10-19 verify1 BcsCuentaInversionServicios: the only P0 rule the module answers for is RULE-076 (main() only in the Spring bootstrap class). BUSINESS_RULES.md has no rule for CuentaInversion, so the oracle is the legacy code itself.

## BcsTesoreriaServicios

[26] 2026-10-20 verify1 BcsTesoreriaServicios: HANA is not exercised. Everything runs on H2 with a schema inferred from the legacy SQL (106 tables, HANA views as seeded tables), shared by oracle and modern code: a schema-model error would not show. Stored procedures (EdicionMultiple, CrearComprobante with USARCREARCOMPROBANTEJAVA=N, the LiquidacionMasiva call) are pinned only on their Java side; the ProcedimientoAlmacenado adapter, SecuenciasJdbc and temp-table behavior are untested on HANA.

[27] 2026-10-20 verify1 BcsTesoreriaServicios: outside the proof: Cloper and CloperWsBSC (real HTTPS), DistribucionPac (dead, does not compile in the legacy), cases TES-A-345/355 (H2 MERGE emulation) and TES-A-429/443 (H2 conversion text), the stock-order path of ProcesarDistribucion (scripted collaborators whose contracts are hand-written shims), queries without ORDER BY whose order HANA decides, branches of ProcesarDistribucion unreachable through the legacy SQL.

[28] 2026-10-20 verify1 BcsTesoreriaServicios: cross-module collaborators (CrearCargoAbono, CalcularParidad, CrearOrdenes, ...) are proven only against scripted doubles; the routing of every collaborator name is tested (ColaboradoresJavaCallEnrutamientoTest) but nothing was exchanged with the real services.

[29] 2026-10-20 verify1 BcsTesoreriaServicios: SOAP (SvcRecaudaInqV1/TrnV1) is covered only by a STATIC contract (SoapContratoEstaticoTest): no real WSDL, no runtime, no wire XML, no consumer validation; the modern endpoints are still a skeleton that does not reproduce the legacy port implementations.

[30] 2026-10-20 verify1 BcsTesoreriaServicios: the only P0 rule the module answers for is RULE-076; BUSINESS_RULES.md has no Tesoreria rule (the session and provisional-password rules were reassigned to BcsUtils and BcsPlataformaWeb by decision of the user), so the oracle is the legacy code itself.

[31] 2026-10-20 verify1 BcsTesoreriaServicios: two canaries SURVIVED and are not listed in canaries: ConsultarLibretaBancoService:254 rounding HALF_UP to DOWN (no case holds a half-cent value on that line; evidence in canary/BcsTesoreriaServicios-verify1/libreta-redondeo-linea254-sobrevive) and, from the transform step, CrearTraspasoService:168 (Math.abs) and GestionDeDepositosService:171 (branch unreachable because of a legacy defect).

## BcsAPVServicios

[32] 2026-10-20 verify1 BcsAPVServicios: HANA is not exercised. Everything runs on H2 with a schema inferred from the legacy SQL (HANA views as seeded tables), shared by oracle and modern code: a schema-model error would not show. Stored procedures, SecuenciasJdbc (NEXTVAL FROM DUMMY), the three seeded recaudacion views and the HANA unique-violation path (code 301) of GrabarContratoSAAPV are pinned only on their Java side.

[33] 2026-10-20 verify1 BcsAPVServicios: cross-module collaborators (Tesoreria, CuentaInversion, SocioNegocio, Ordenes, ...) are proven only against scripted doubles; the routing of the collaborator names is tested but nothing was exchanged with the real services, and they now run over HTTP outside the APV transaction (review finding H4, open).

[34] 2026-10-20 verify1 BcsAPVServicios: REST and the legacy JSON adapter are proven by wiring and mapping tests (RestEntradaConDefectosLegacyTest 69, RestRegistroConsistenciaTest, ApvCableadoRealTest, AutorizacionesControllerTest), not by the dual-execution cases; no real-proxy test exists for the services with mid-flow commit/rollback (CargaMasivaPAC, CargaMasivaBanco, CargaBonificacionTGR, DevolucionBF, EjecutarBonificacion, ProcesarDistribucion, EliminarRegistros).

[35] 2026-10-20 verify1 BcsAPVServicios: not covered by cases: CargaBonificacionTGR batches over 1000 rows, ReplicaPortafolioAPV with data, ProcesarCargaMasivaRecaudacion with more than 2 deposits; ValidarBancoPrevired and ValidarMandatosPAC return null in the legacy and stay null adapters. The 20 fresh inputs cover 11 operations, not all 64.

[36] 2026-10-20 verify1 BcsAPVServicios: the oracle is the legacy code itself; BUSINESS_RULES.md rules are not tied to this module by name (the only P0 rule the module answers for is RULE-076). Legacy defects are kept bug-for-bug and listed in TRANSFORMATION_NOTES.md for the domain expert; SME sign-off on APV regulatory correctness is an external blocker and malformed records were not tried (INTENT.md asks for equivalence on valid-format inputs).

## BcsTesoreriaJobs

[37] 2026-10-21 verify1 BcsTesoreriaJobs: HANA is not exercised. Everything runs on H2 with a schema inferred from the legacy SQL (shared by oracle and modern code): a schema-model error would not show. Stored procedures (the HANA CALLs of the file jobs) are scripted on their Java side only; ProcedimientoArchivoJdbc is tested with mocks.

[38] 2026-10-21 verify1 BcsTesoreriaJobs: opencsv 3.7 (the legacy runtime library, libs/opencsv-3.7.jar) is not available offline: the legacy oracle runs a shim of the 3.x rules and the modern code a reproduction (CsvLegacyWriter), so both are the same reading of 3.7, never checked against the real jar. Production OS (LF line endings, Linux) is an assumption.

[39] 2026-10-21 verify1 BcsTesoreriaJobs: the collaborators (ValidarMandatosPAC, ValidaEjecucionProcesoRecPAC, ConsultarEstadoModulo, ObtenerDiaAnterior, AgregarLog and the Tesoreria services) are proven only against scripted doubles; nothing was exchanged with the real services or the platform router.

[40] 2026-10-21 verify1 BcsTesoreriaJobs: not proven: Quartz / Job Manager scheduling (the jobs are called in process through POST /internal/job-call; the legacy ran under BcsJob + Quartz), real file delivery (volume, FTP, bank consumers), and the web entry point against the real platform. Open review findings H2 (ephemeral output files, no volume, healthcheck, non-root), H3 (no bound on a run) and H4 (parameters and idPrograma chosen by the caller, in-flight guard per JVM only) need decisions or deployment work.

[41] 2026-10-21 verify1 BcsTesoreriaJobs: no P0 rule of BUSINESS_RULES.md is tied to this module by name except RULE-061, RULE-076 and RULE-077 (tested by Rule061/076/077 classes); the oracle is the legacy code itself. 19 legacy defects (TES-J-DEF-01..19) are kept bug-for-bug and listed for the domain expert. Malformed records were not tried (INTENT.md asks for exact behavior on valid-format inputs only).

[42] 2026-10-21 verify1 BcsTesoreriaJobs: fresh input PG-BX01 first try (amount 0.00000001) made the legacy fail with StringIndexOutOfBoundsException (defect family TES-J-DEF-12); it was replaced by 0.00000100 and is not counted. The 46 fresh inputs cover the 6 jobs but not every branch.

## BcsAPVJobs

[43] 2026-10-21 verify1 BcsAPVJobs: HANA is not exercised: everything runs on H2 with a schema inferred from the legacy SQL (shared by oracle and modern code); stored procedures only on their Java side. Collaborators outside the module are proven only against scripted doubles. The jobs run in process through POST /internal/job-call; Quartz / Job Manager scheduling is not proven. Open architecture-review findings (H-1 collaborators commit outside the job transaction over HTTP, H2/H3/H4 job pattern, shared output volume) need decisions or deployment work. Production is Linux (LF). Malformed records were not tried (INTENT.md asks for exact behavior on valid-format inputs only). The oracle is the legacy code itself; only RULE-061, RULE-076 and RULE-077 tie to the module.

[44] 2026-10-21 verify1 BcsAPVJobs: canary job/IngresarOrdenRVBonificacionFiscal.java:147 custody cap compareTo(cantidadBF) < 0 changed to <= 0 SURVIVED (IngresarOrdenRVBonificacionFiscalCasosTest 50/50 green): no case holds a custody quantity exactly equal to the bonus quantity; evidence in canary/BcsAPVJobs-verify1/rv-tope-custodia (not listed in canaries).

[45] 2026-10-21 verify1 BcsAPVJobs: the OBF-006, CMP-025 and RVB-039 Error cases mask the `tablas` comparison point (a harness artifact: the legacy dumped the tables on a connection still open after a java.lang.Error; HANA rolls back on close); decided by the user (option a).

[46] 2026-10-21 verify1 BcsAPVJobs: the 4 jobs collaborators were not exchanged with the real services; the router names are covered by ColaboradoresJobsEnrutamientoTest.

[47] 2026-10-21 verify1 BcsAPVJobs: fresh inputs OBF-101 and OBF-102 are marginal: the job sends a hard-coded order and ignores the scripted answer, so only the tablas facet (new IdPrograma) differs from OBF-001; the OBF job has no real input surface. MonedaFormato static cache is cleared per case by the oracle.

## BcsCuentaInversionJobs

[48] 2026-10-21 verify1 BcsCuentaInversionJobs: HANA is not exercised: everything runs on H2 with a schema inferred from the legacy SQL (shared by oracle and modern code); stored procedures only on their Java side. Collaborators outside the module are proven only against scripted doubles. The jobs run in process through POST /internal/job-call; Quartz / Job Manager scheduling is not proven. Open architecture-review findings (H-1 collaborators commit outside the job transaction over HTTP, H2/H3/H4 job pattern, shared output volume) need decisions or deployment work. Production is Linux (LF). Malformed records were not tried (INTENT.md asks for exact behavior on valid-format inputs only). The oracle is the legacy code itself; only RULE-061, RULE-076 and RULE-077 tie to the module.

[49] 2026-10-21 verify1 BcsCuentaInversionJobs: opencsv 3.7 is reproduced (shim in the oracle, own writer in the module), never run against the real jar.

[50] 2026-10-21 verify1 BcsCuentaInversionJobs: the Servicio-name validation (^[A-Za-z0-9_.:]+$) in GenerarArchivo is a deliberate hardening deviation decided by the user (option A); golden GA-29 (Servicio SP"X) runs only through the test-only 4-argument constructor with validation off, so that golden does not exercise the production constructor.

[51] 2026-10-21 verify1 BcsCuentaInversionJobs: Step 1 test-support changes accepted by the user (Observacion, ColaboradoresDeCaso, Equivalencia with ${RUTA_BASE} masked on both sides in 24 IP/RC cases); goldens untouched.

[52] 2026-10-21 verify1 BcsCuentaInversionJobs: PLA.RutaBaseArchivos and PLA.FechaSistema are read from PLA.Configuracion (legacy used a HANA procedure): an assumption not proven on HANA. The PAUTA steps have no entry point yet (plain beans with ProcesoPauta).

## BcsCrystalReports

[53] 2026-10-21 verify1 BcsCrystalReports: only 21 of the 153 repository templates (2 per domain plus one regression case) and 16 fresh ones were rendered: about 116 templates were never rendered by either engine; the 15 templates exported from HANA QA and the 21 HANA-only rows were not rendered either; SocioNegocio has no .rpt (3 queries only) and its templates, if they exist, are not in the repository nor in the HANA QA listing.

[54] 2026-10-21 verify1 BcsCrystalReports: inputs are fake rows deduced from the templates and .sql files, not real HANA data or types: typed dates arrive as text through the JSON contract (two templates, Normativa CI63Transacciones and Mesa MRF_QUINCENAL_DE_PACTOS, could not be rendered even by the legacy engine and were replaced); procedure-driven templates and real data volumes are not covered.

[55] 2026-10-21 verify1 BcsCrystalReports: the legacy production runtime (JRE 8, Alpine, TomEE) was not available: engine equality is shown on JDK 11 only; the container image (Alpine, DejaVu fonts, non-root) renders a 1-page PDF with the expected text but the goldens use the Windows font set of this machine: pixel/page equivalence inside the image is not proven; POI is pinned to 3.17 while the legacy build files declare 3.14 (the 3.14 jar probe gave the same output on the one affected xls).

[56] 2026-10-21 verify1 BcsCrystalReports: masks (PDF /Title temp name, print date/time text and the font subset objects that change with the minute, 6 Excel print-time bytes, RTF \creatim, TXT time) are documented in equiv/README.md; text, positions, images, font names and object order are compared exactly; Word is RTF in both engines; TXT mojibake exists in both engines.

[57] 2026-10-21 verify1 BcsCrystalReports: REST contract: the legacy object-form data (serviceId/serviceData: server-side data access) and the user session token (X-SESSION-TOKEN) are deliberately outside the sidecar (the platform must resolve data and call with the internal token); an optional schema field and a companyId parameter were added; other HTTP methods answer 500 instead of 405; BcsPlataformaWeb CallReportServlet and about 29 in-process ReportDocument callers (Normativa, Mesa, FondoMutuo, Facturacion, Custodia) have not been redirected to the sidecar.

[58] 2026-10-21 verify1 BcsCrystalReports: open review findings: /reports/generate stays public until its consumers send the token (retirement date not set), caller-controlled debug:true returns stack traces, no per-request timeout (a render without data waits about 20 s for the HANA host embedded in the template), ListMapResultSet static metadata field (renders are serialized with a lock instead), 100 MB upload limit and no body/pixel guards, no audit log of template upload/update, the uploaded .rpt is opened by the engine in a process that holds HANA credentials (needs a dedicated HANA user, egress limited to HANA, upload/update restricted at the platform), mem_limit 2g not exercised (the podman machine has no memory cgroup), Spring Boot 2.7.18 end of life (compensating controls: no published port, network isolation).

[59] 2026-10-21 verify1 BcsCrystalReports: canary extension-xls (RenderOrchestrator.java:53 XLS->XLSX) is pinned by one test only (122 run, 1 failed): the file-name extension of the Excel reply is weakly pinned.

[60] 2026-10-21 verify1 BcsCrystalReports: malformed requests were not tried (INTENT.md asks for exact behavior on valid-format inputs only); data with a backslash was not run (a bug of the test tooling Facet.rtfText, not of the engine).

## BcsFondoMutuoServicios

[61] BcsFondoMutuoServicios: no fresh inputs this run; its F1 oracles (equivalence/BcsFondoMutuoServicios) are not in the compare.py cases format, and the module is still mostly stubs (F1 slice only), so fresh-input work is deferred to the full-module verify.

[63] 2026-10-25 BcsFondoMutuoServicios: dev cases.json holds the 135 FMM-NOR and 73 FMM-RRE goldens only; the FMM-MAN Manejador goldens and the 3A/3B pure-closure goldens are left out (their module tests compare a subset, not a full dump, so no byte comparison exists).

[64] 2026-10-25 BcsFondoMutuoServicios: verifier fresh inputs cover RescateReinversion only (the code changed on 2026-10-25); Normal and the in-process Tesoreria (changed 2026-10-23/24) got no verifier fresh inputs this run.

[65] 2026-10-25 BcsFondoMutuoServicios: malformed records tried: VF-RRE-21 (letters in NumeroCuotas) and VF-RRE-22 (blank IdInterno): both sides JsonSyntaxException; no other malformed shape tried.

[66] 2026-10-25 BcsFondoMutuoServicios: H2 only, scripted collaborators; HANA, the autonomous lock procedures' real behaviour and the router answers are not exercised.

[71] 2026-10-26 BcsFondoMutuoServicios: no new fresh inputs this round (the change touches only the failure path of the reinversion lock and a start-up guard); the 22 fresh inputs of 2026-10-25 were re-run on the current code against the same legacy dumps. FMM-RRE-10 and FMM-RRE-11 differ from the legacy golden only in the session field tablasLocalesSinDrop (DEV-6-H1 extended to RescateReinversion; the user approved the deviation in the session on 2026-10-26, the per-case approval is for a person to record).

[72] F3 W1 reads, verify 2026-10-27: no fresh input was refused by the legacy (leftOut empty for the 135); DESVIO-KIT-1 (empresa nula/sin configuracion: legacy NullPointerException, new ParametroNoEncontradoException of the kit) and VDO FMM-VDO-05/06 (legacy plain Exception, new ParametroNoEncontradoException, same message) are NOT approved here: 15 development cases and 1 fresh case differ only by exception class, for a person to decide.

[73] F3 W1 reads: malformed records were not tried beyond the nulls, wrong JSON types and unparseable dates that the legacy itself accepts or rejects in the fronts' own cases; INTENT.md asks for the same behaviour including quirks, and the fresh inputs of each front already include them.

[74] F3 W1 reads: HANA-only behaviour is not covered (column types, MAP, row order without ORDER BY, the driver's getColumnName with an alias); the proof is on H2 with the unmodified legacy classes. Read-only SELECTs on HANA QA settled Q-F3A-9 (IdEmpresa 0 filters IdEmpresa=0), D-F3A-28 (getColumnName returns the underlying name), MAP(NULL,...)=0, IngresoAutomatico without NULL in ING/PEN and a null Long bound as NULL; readOnly transaction and statement timeout are tested on H2 only.

[75] F3 W2 CVC, verify 2026-10-27: VF-CVC-15 (ISIN mode with repeated ISINs) left out because its dump is byte-identical to a development output (the dumped tables do not show the extra series); VF-CVC-33 replaces it. The legacy refused no input.

[76] F3 W2 CVC: not approved here (a person decides): cvc-FMM-CVC-193 (DESVIO-KIT-1, NullPointerException vs ParametroNoEncontradoException) and VF-CVC-29 / VF-CVC-30 (an injected write failure in a batch of 12: the legacy SQL.batch on H2 keeps executing the rows after the failing one, the new SqlLote stops at the first; the state after the host's rollback is identical, only the in-session state before the rollback differs; HANA's executeBatch behaviour on a partial failure cannot be checked with a SELECT).

[77] F3 FFMM and chain, verify 2026-10-28: no fresh input was refused by the legacy; a first run refused an input with IdRegimenTributario NULL because FMM.SolicitudAporteRescate.IdRegimenTributario is NOT NULL in the schema (not reachable), replaced by an empty string in VF-FFMM-10b and VF-CHAIN-11.

[78] F3 FFMM, verify 2026-10-28: not approved here (a person decides): ffmm-FM-CPFF-C04d/C04e/C04f (DESVIO-KIT-1: null, absent or unconfigured company; legacy NullPointerException, new ParametroNoEncontradoException of the kit; everything else identical).

[79] F3 FFMM chain, verify 2026-10-28: CadenaEquivalenciaTest.conIdsCanonicos requires the request ids to be 2..n+1 without gaps; VF-CHAIN-05 and VF-CHAIN-17 mix amount and quota requests (ids [2,4] and [3]); both sides are identical and the verifier renamed by first appearance; a case like that added to the module suite would fail its own assertion.

[80] Re-run of the new side 2026-10-28: VF-CVC-29 and VF-CVC-30, approved by the user on 2026-10-27 as a batch difference, are now identical to the legacy (real executeBatch); their approvals are no longer needed.

[81] F3 Normal fresh, verify 2026-10-29: no input was refused by the legacy; 6 of the 42 do not count (VF-NOR-27..32: early-error outputs equal to an existing one: user without profile, p_IdUsuario 0 and 2147483647, p_IdEmpresa 0 and -1, a batch of 1 with IdInterno 0).

[82] Normal, verify 2026-10-29: not approved here (a person decides): VF-NOR-11 (USD contribution with Spot: legacy ROLLBACK_HTTP500 SQLException because the oracle H2 has no MES.Mesa; new ROLLBACK_503 ServicioNoServidoException, gate G2 closed: the same behaviour as the approved development case FMM-NOR-07, DEV-F4) and VF-NOR-36 (batch of 5 with a duplicated lock key: only the session differs: the legacy leaves the local table #p_BloqueoSolicitudAporteRescate undropped, the new code drops it: the same behaviour as the approved development case FMM-NOR-37, DEV-6-H1).

[83] Normal custody switches, verify 2026-10-29: the CasoNormal harness builds Compuertas with the 6-argument constructor (Normal follows G-CUS), so it does not exercise normal-habilitada separately; a verifier copy (V29Gates) ran the 42 inputs with each combination: with G-CUS and normal-habilitada open, with or without comisiones-en-proceso, identical to the harness; with normal-habilitada=false 21 inputs (the ones that query the custody flow) answer 503 G-CUS by design; with habilitada=false they also answer 503 because ConsultarFlujoCustodiaFondoMutuoService also evaluates gCustodia().

## BcsJobManager / BcsPlataformaWeb / BcsGateway

[62] BcsJobManager, BcsPlataformaWeb, BcsAPVApi, BcsApvJavaCallAdapter: platform/routing or typed-API libraries with no legacy oracle here; no fresh inputs.

[69] 2026-10-26 BcsGateway: no legacy counterpart (the legacy had nginx in front): judged by its own tests only.

[70] 2026-10-26 BcsPlataformaWeb login MVP: legacy oracle = the legacy OPTIMUS web on QA (qa.optimuscb.cl:9045) over HTTP and a browser capture, with the QA test users; ActualizarClave needs a session in the new code (owner decision, the legacy asks none): its refusals were compared with the legacy anonymous answers, and the 400 / 403 rules have no legacy counterpart (tests only). Not compared: a successful ActualizarClave (it changes the password), channels other than COR, SSO, /call, menus and windows (UI); malformed records not tried (the contract, not broken input, must match).

## Generales (sin modulo)

[3] Fresh-input check: legacy cannot run in this environment (TomEE 8 / Java 8 required). Malformed records not attempted; there is no INTENT.md in this workspace, so the earlier claim of 'INTENT.md guidance' for that choice was unsupported. Fresh-input verification is trace-based only.

[8] cases.json: no development equivalence cases were generated; no legacy runner available to generate or re-run them.

[84] canaries: the pack reads only the first 50 entries, so the list keeps the 2 most recent per module (2026-10-30); the evidence of every earlier canary stays in equivalence/canary/
