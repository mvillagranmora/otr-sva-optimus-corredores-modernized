# BcsCrystalReports — Transformation Notes

## Purpose
Quarantine microservice that wraps the SAP Crystal Reports Java SDK (JRC) as an isolated REST service.
The Crystal Reports SDK requires Java 11; the rest of the modernized system runs Java 25.
This service runs as a sidecar to the main modernized services.

## Port
`8100`

## Java Version
`11` — mandatory for Crystal Reports SDK compatibility. Do NOT upgrade.

## Crystal Reports JARs
The following proprietary JARs are installed to the local Maven repository (`~/.m2`) and are NOT committed to Git:

| JAR | GroupId | ArtifactId | Version |
|-----|---------|------------|---------|
| CrystalReportsRuntime.jar | com.crystaldecisions | CrystalReportsRuntime | 14.1 |
| CrystalCommon2.jar | com.crystaldecisions | CrystalCommon2 | 14.1 |
| webreporting.jar | com.crystaldecisions | webreporting | 14.1 |
| JDBInterface.jar | com.crystaldecisions | JDBInterface | 14.1 |
| DatabaseConnectors.jar | com.crystaldecisions | DatabaseConnectors | 14.1 |
| pfjgraphics.jar | com.crystaldecisions | pfjgraphics | 14.1 |
| cvom.jar | com.crystaldecisions | cvom | 14.1 |
| barcode4j.jar | net.sf.barcode4j | barcode4j | 2.1 |
| avalon-framework-4.2.0.jar | avalon-framework | avalon-framework | 4.2.0 |
| logging.jar | com.crystaldecisions | logging | 14.1 |
| keycodeDecoder.jar | com.crystaldecisions | keycodeDecoder | 14.1 |
| log4j.jar (log4j 1.2.6, as shipped in legacy) | com.crystaldecisions | log4j | 1.2.6 |

The last three were MISSING from the first version of this module (found by the spike as `NoClassDefFoundError`:
JRC needs `com.businessobjects.foundation.logging.LoggerManager`, `org.apache.log4j.Logger`, `...keycode.KeycodeException`).
Also added as normal dependencies: commons-configuration 1.2, commons-lang 2.6, commons-collections 3.2.2,
commons-logging 1.2 and icu4j 71.1 (same versions as the legacy build.gradle).

Source JARs are located at:
`legacy/otr-sva-optimus-corredores/libs-private/` (copy them into `BcsCrystalReports/libs/`, which is gitignored).

### Offline build (one-time local setup)
`~/.m2` only had `*.lastUpdated` stubs for the Crystal jars and avalon, so `mvn -o` failed. Run once (JDK 11):
```bash
JAVA_HOME=<jdk11> bash modernized/otr-sva-optimus-corredores/BcsCrystalReports/install-libs.sh
```
It installs every `libs/*.jar` with the coordinates above (same as the Dockerfile), icu4j and commons-configuration
from the gradle cache, and `junit-platform-launcher:1.8.2` (see Tests). Idempotent. It uses
`maven-install-plugin:3.1.4` because the 2.5.2 pinned by Spring Boot is incomplete offline. With network access the
script is not needed except for the proprietary jars.
Then: `JAVA_HOME=<jdk11> mvn -o -f modernized/otr-sva-optimus-corredores/BcsCrystalReports/pom.xml test`.
`mvn -o package`/`clean` do not work offline here (maven-jar/clean plugins are incomplete in ~/.m2); not related to this module.
If an IDE (Java language server/Eclipse compiler) auto-builds into `target/classes`, it overwrites the classes with
ECJ output that lacks the `com.sun.rowset` exports ("Unresolved compilation problems"); move `target/classes` and
`target/test-classes` away and re-run Maven.

## API Contract

### POST /reports/generate
Generates a Crystal Report and returns the binary content.

**Request body (JSON):**
```json
{
  "templateId": "<UUID from BSCL.Report::Template>",
  "format": "pdf | excel | word | rtf | txt",
  "beanData": { "fieldName": "fieldValue", ... },
  "imageBase64": "<base64-encoded image>",
  "imageName": "<image field name in template>",
  "barcodeData": "<barcode data string>",
  "barcodeName": "<barcode field name in template>"
}
```

**Response:** Binary report file with appropriate Content-Type header.

**Error response (400 / 500):**
```json
{ "error": "<message>" }
```

### GET /reports/health
Returns `200 OK` with body `BcsCrystalReports UP`.

### /report (GET or POST): faithful contract of the legacy `CallReportServlet`
Quarantine = isolation only: `cl.bcs.reports.rest.*` reproduces what the legacy servlet
(`BcsPlataformaWeb/.../services/CallReportServlet.java`, `@WebServlet("/report")`) does around the unchanged engine
(`ReportDocument`), with the same modes, order, JSON names, defaults, status codes and text bodies. Classes:
`CallReportController` (mode dispatch, 500 handling), `BodyReader`, `RenderOrchestrator`, `ParametersApplier`,
`DataApplier`, `RowsBuilder` (Gson typing + `ListMapResultSet.populate`), `TemplateModes` (update/upload/rpt), `Reply`/`Stop`
(what the servlet wrote), `InternalTokenFilter`.

Auth: header `X-Internal-Token` (env `INTERNAL_TOKEN` -> `services.internal-token`). Allow-list filter, fails closed
(empty secret = every protected call rejected), constant-time compare, 401 body `401 - NOT AUTHORIZED` (the servlet's text).
Public: `/reports/health`, `/actuator/health`, `/actuator/info` and, TEMPORARILY, `/reports/generate`. Everything else is protected.

| Mode (servlet order) | Request | Response | Servlet lines |
|---|---|---|---|
| `?update` (param present, any value) | multipart: params `reportId`, `reportName`, `reportVersion` (int), `companyId` (long, NEW, replaces the session's company); part `data` (.rpt; empty = keep the stored file) | unknown reportId `404 - NOT FOUND`; else `200 application/json {"result":"OK"}` | 188-218 |
| `?upload` | multipart: `reportName`, `reportVersion`, `companyId`; part `data` | `200 application/json {"result":"OK","reportId":"<uuid>"}` (`reportId:""` if JRC cannot open the file: the servlet answers OK anyway) | 220-244 |
| (body read) | JSON body, lines joined without separator, empty = `{ }`; `debug` (bool), `reportId` (default `""`) | non-JSON-object body: `500` | 246-265 |
| `?rpt` | body `{"reportId":...}` | `200` .rpt bytes, `Content-Disposition: attachment; filename="_.rpt"` (name/id are never filled by `getDocument`); unknown/missing id `404` | 273-291 |
| `?file` | | empty branch in the servlet: falls through to the render | 293-295 |
| render (default) | `exportFormat` (pdf/excel/word/rtf/txt, trimmed, case-insensitive), `configuration.mainReport.{parameters,data,schema?}`, `configuration.subReports[{reportName,data,schema?}]` (key mandatory, may be `[]`) | `200` bytes + `Content-Disposition: attachment; filename="<name>_<id>.<PDF|XLS|DOC|RTF|TXT>"`, NO Content-Type (as the servlet; `name` is the last subreport name for templates with subreports, id is always empty) | 302-516 |

Render details: `parameters` array of `{type: image|pdf417, name, data}` (`setDocumentImage` / `setDocumentBarcodeImage`; missing
type/name/data = 404; unknown type ignored; `parameters` as object = no-op as in the servlet; missing/other = 404); `data`
array of rows -> Gson `Map<String,Object>` -> `ListMapResultSet.populate` (empty main array: no result set set); subreports via
`setSubdocumentsResultSet`; same call order as the servlet (readDocument, parameters, main data, subreports, export).
Extension (not in the servlet's array form): optional `schema: [{field,type}]` next to `data` (same shape as the servlet's
`serviceData.$schema`) applies the type fixes the servlet did only for server-resolved data (lines 755-795): `date`/`datetime`
`/Date(ms)/` -> text, `blob` base64 -> `byte[]`. Needed because JSON cannot carry the BLOB logos; without `schema` the behaviour is the servlet's.

Error mapping (as the servlet; plain-text bodies):
| Situation | Status / body |
|---|---|
| no / wrong `X-Internal-Token`, or no secret configured | 401 `401 - NOT AUTHORIZED` (servlet: missing/invalid X-SESSION-TOKEN) |
| unknown or missing `reportId` (update, rpt, render) | 404 `404 - NOT FOUND` |
| `mainReport.parameters` missing/wrong type, parameter without type/name/data, `mainReport.data` missing/primitive, subreport data primitive | 404 `404 - NOT FOUND` |
| `data` (main or sub) in OBJECT form `{serviceId,serviceData,serviceResponse}` | 404 `404 - NOT FOUND` + header `X-Crystal-Reason` (sidecar-only, diagnostic) |
| missing `exportFormat`/`configuration`/`mainReport`/`subReports`, subreport without `data`, unknown subreport name, bad int, missing part `data`, malformed JSON, any exception | 500 `500 - INTERNAL ERROR` (with `debug:true`: `500 - INTERNAL ERROR \n\n<stack trace>`, as the servlet) |
| format not in the list, or the engine returns no stream | 500 `500 - INTERNAL ERROR` (after the whole setup, as the servlet) |
| other HTTP methods | 500 JSON via the pre-existing catch-all `GlobalExceptionHandler` (servlet: 405) |

What the sidecar deliberately does NOT do: (1) server-side data access: the object form of `data` (`getServiceData`:
`ValidaHashServicioUsuario`, stored procedure, OData proxy `/call?t=`, `debug`/`_serviceURL` redirect) is the PLATFORM's job; it
resolves the rows and sends the array form. (2) User session: `X-SESSION-TOKEN`, `Utils.getSessionData`, company/user from the
session; replaced by the internal token plus the `companyId` parameter in update/upload. (3) No engine change (only closes the
`ReportDocument` at the end of each request, which the servlet leaked). Also: the servlet used schema `null` (session's
default schema); the sidecar uses `app.hana.schema` like `/reports/generate`. `?file` stays a no-op.

Open follow-ups: (a) migrate callers: `BcsPlataformaWeb` `CallReportServlet` must forward to the sidecar with the platform-resolved
rows (and token); (b) ~29 in-process callers of `ReportDocument` (Normativa, Mesa, FondoMutuo, Facturacion, Custodia jobs) must go
through REST; (c) `/reports/generate` still unauthenticated: its consumers (BcsUtils `CrystalReportsRestClient`) must send
`X-Internal-Token`, then drop it from `InternalTokenFilter.PUBLICAS`; (d) the container still runs as root (non-root user pending);
(e) `/reports/generate` with an empty body answers 500 (400 for a body without `templateId`): align or leave; (f) `ListMapResultSet`
keeps a `static` metadata field (legacy), so concurrent renders are not proven thread-safe: serialize or scale by replicas;
(g) `debug:true` returns stack traces (kept for fidelity; the endpoint is internal only).

## Integration Pattern
The modernized Java 25 services call this service via HTTP (RestClient/WebClient):
```java
// Example from a Java 25 service
byte[] pdf = restClient.post()
    .uri("http://bcs-crystal-reports:8100/reports/generate")
    .body(new GenerateReportRequest(templateId, "pdf", beanData))
    .retrieve()
    .body(byte[].class);
```

## Source Adaptation
`ReportDocument.java` is adapted from:
`legacy/otr-sva-optimus-corredores/BSCL/Plataforma/JAVA/BcsPlataformaReporteApi/src/cl/bcs/plataforma/report/api/ReportDocument.java`

Changes made:
1. Package changed: `cl.bcs.plataforma.report.api` → `cl.bcs.reports.report`
2. `import cl.bcs.utils.ListMapResultSet` replaced with local `cl.bcs.reports.report.ListMapResultSet`. NOTE (correction): it was
   first a stub whose `getMetaData()` threw `UnsupportedOperationException`, so the sidecar could NOT render any template
   with data (JRC failed with "Unexpected database connector error", swallowed, 500 "Export returned null"). It is now a
   faithful port of the legacy class (`extends com.sun.rowset.CachedRowSetImpl`, only the package changed).
3. `import org.apache.logging.log4j.*` removed; replaced with SLF4J logger
4. (reverted) The first adaptation moved to POI 5.2.5 (`Cell.CELL_TYPE_NUMERIC` -> `CellType.NUMERIC`). That was NOT behavior-neutral,
   see "POI version deviation" below. The sidecar now uses POI 3.17 exactly like the legacy and keeps `Cell.CELL_TYPE_NUMERIC`.
5. `System.out.println` calls replaced with `log.info` / `log.debug`

## POI version deviation (found by the fresh-input verification, FIXED)
- Deviation: the TXT export (`DataFormatter(en_US).formatCellValue` over the Crystal xls) differs between POI 3.x and 5.x for
  the xls number format `[$$-0409]* #,##0;-[$$-0409]* #,##0;-` (Facturacion/Templates/InformeGananciaCapital.rpt). POI 3.17 cannot
  parse it (logs `Invalid format`) and writes the raw number (`-98765.43`, `1234.5`, `0,01`, `0`, `100000000000`); POI 5.2.5 applies
  it (`-$   98,765`). Fresh case F035 (txt) differed; the 20 development templates never use such a format, so the earlier statement
  that POI 5.x was behavior-neutral ("XLS/TXT through POI 3.17 vs 5.2.5 render byte-identically") was only true for those 20.
- Cause: sidecar pom had `org.apache.poi:poi:5.2.5`; legacy runs POI 3.x (the repo's gradle/ant files declare 3.14; the legacy
  recorder, and therefore every golden, ran with poi-3.17.jar from ~/.m2; the probe shows 3.14 and 3.17 give identical output on
  this xls). Rule: legacy behavior is always replicated.
- Fix: pom pins `poi` 3.17 (only `poi` is imported by ReportDocument; its log4j 1.2.17 runtime dependency is excluded because the
  JRC log4j 1.2.6 is the one on the legacy classpath; commons-collections4 4.1 / commons-codec come transitively, all resolvable
  offline); ReportDocument and the test `Facet` use `Cell.CELL_TYPE_*` again. No other adaptation changed.
- Evidence: `analysis/.../equivalence/BcsCrystalReports/fresh/poi-probe/` (3.17 vs 5.2.5 per-cell output),
  `FRESH_EQUIVALENCE.before-poi-fix.json` (79 same / 1 differs) vs `FRESH_EQUIVALENCE.json` (80/80 same); the template is now the
  permanent regression case `equiv/Facturacion_InformeGananciaCapital` (21 cases): with POI 5.2.5 restored in a scratch copy its txt
  facet fails in EquivalenceTest and EquivalenceRestTest (`fresh/poi-probe/regression-guard-poi-5.2.5.log`).
- Upgrading POI is a behavior change: re-run both equivalence suites first.

## JVM flags (pom property `crystal.jvm.args`, Dockerfile ENTRYPOINT, surefire argLine, spring-boot:run)
```
--add-exports java.sql.rowset/com.sun.rowset=ALL-UNNAMED
--add-exports java.sql.rowset/com.sun.rowset.internal=ALL-UNNAMED
--add-opens   java.sql.rowset/com.sun.rowset=ALL-UNNAMED
-Djava.awt.headless=true  -Dfile.encoding=UTF-8
```
`ListMapResultSet` extends a JDK-internal class and writes its private fields reflectively (breaks on newer JDKs, hence
Java 11 stays). Compilation also needs the two `--add-exports`: the main code is compiled without `--release`
(compiler plugin execution `default-compile`), tests keep release 11. `file.encoding=UTF-8` because legacy ran with it
(TXT export uses the default charset). Tests run with assertions disabled (`enableAssertions=false`): JRC's
`ReportDocument.<clinit>` asserts that the optional `com.azalea.ufl.BarcodeLibrary` loads (it is absent), production has no `-ea`.

## Fonts
JDK 9+ has no `${java.home}/lib/fonts`; JRC's `OpenTypeFontManager` NPEs on every PDF export without it (and also with
an empty one: it needs TTFs). Dockerfile (Alpine): `apk add fontconfig ttf-dejavu`, create `$JAVA_HOME/lib/fonts` and copy
the DejaVu TTFs, `-DSystemFontPath=/usr/share/fonts`. Verified with podman on 2026-10-21 (build, start, real PDF render: `analysis/otr-sva-optimus-corredores/CRYSTAL_CONTAINER_CHECK.md`);
the image fonts are DejaVu, NOT the Windows fonts the equivalence goldens were recorded with, so pixel equivalence inside the image is not proven. Locally the installed JDK is
not modified: `CrystalTestEnvironment` creates `target/test-java-home/lib/{fonts,fontconfig.*,psfont*}` (TTFs copied from
`C:/Windows/Fonts` or `/usr/share/fonts`) and points the `java.home` property to it before JRC loads; it FAILS (not skips)
if no TTFs exist. Rendering depends on the fonts, so compare outputs only with the same font set.

## Tests (`mvn -o test`, JDK 11: 7 tests, 0 failures, 0 skipped)
| Test | Status |
|------|--------|
| `ReportControllerTest.health()` — GET /reports/health returns 200 | Active |
| `ReportControllerTest.generateMissingTemplateId()` — POST without templateId returns 400 | Active |
| `generatePdfFullIntegration()` (@Disabled, HANA) | REPLACED by `CrystalRenderTest` |
| `CrystalRenderTest.pdf/excel/word/rtf/txt` — real render via controller->service->JRC | Active |

`CrystalRenderTest` loads `templates/CuentaInversion/Templates/ComprobanteCuentaInversion.rpt` into an H2 table
`BSCL."BSCL.Report::Template"` and posts `src/test/resources/crystal/comprobante.json` as `beanData` (single row, current contract).
PDF: 1 page, text contains client name and amount, two renders equal after the masks in `PdfProbe` (`/Title (<uuid>.rpt)`,
print date `(dd-MM-yyyy)Tj` and time `(h:mmAM)Tj`). Excel: OLE2/BIFF opens with POI. Word: RTF text (legacy quirk).
RTF/TXT contain the data. It fails if the Crystal SDK/jars, JVM flags or fonts are missing.
Surefire 3.5.6 (the one complete in ~/.m2) needs `junit-platform-launcher` = the Jupiter platform version (1.8.2 with Boot 2.7.18),
which was not cached: `install-libs.sh` installs the unmodified 1.8.2 jar shipped in the Gradle 8.12 distribution.

## Deferred
- Not proven: Java 8 runtime of legacy, real HANA data, subreports/images/pdf417, the other 152 templates, the Docker image against real HANA and with the production fonts (the image itself builds and renders: see Fonts).
- `/reports/generate` keeps the single-row/all-"string" contract; the full contract (multi-row, subreports, template modes, auth) is `/report`, see above.
- REST tests (`cl.bcs.reports.rest.*`, `equiv/EquivalenceRestTest`): 100 case-formats through POST /report vs the legacy goldens (same masks),
  `ReportRestRenderTest` (17: each servlet branch), `ReportRestModesTest` (7: rpt/upload/update), `ReportRestAuthTest` (7), `ReportRestFailClosedTest` (2).
  Full suite 263 tests (130 before). `compare.py` on `analysis/.../equivalence/BcsCrystalReports/cases-rest.json` (out-rest/): 100 same, 0 differs
  (result copy: `EQUIVALENCE-rest.json`; `EQUIVALENCE.json` is the run of `cases.json`). Negative check: with the `image` branch of
  `ParametersApplier` commented out, `EquivalenceRestTest` fails 9 of 100 (APV_CONTRATO_SAAPV, CuentaInversion_ComprobanteCuentaInversion, ...); restored.
- Templates embed a HANA host; if no data set is given JRC tries to connect to it (~20 s). Do not render without data.

## Canary
Break the health endpoint → `health()` test fails → restore endpoint to validate CI pipeline coverage.

## Environment Variables Required
| Variable | Description |
|----------|-------------|
| `HANA_URL` | JDBC URL for SAP HANA (e.g. `jdbc:sap://host:39015`) |
| `HANA_USER` | HANA database user |
| `HANA_PASSWORD` | HANA database password |
| `HANA_SCHEMA` | HANA schema (default: `BSCL`) |
| `INTERNAL_TOKEN` | Shared secret callers send in `X-Internal-Token` for `/report`; empty = every `/report` call is 401 and an ERROR is logged at startup. Required by both compose files |


## Architecture review (step 5)
Review findings (H = high, M = medium, L = low). Only the fixes that do not change any behavior covered by the goldens, the
status/body mapping of `/report`, nor the contract of `GET /reports/health` / `POST /reports/generate` (which stays public) were applied.

### APPLIED
- **H1** both compose entries (`modernized/docker-compose.yml`, `modernized/otr-sva-optimus-corredores/docker-compose.yml`) pass
  `INTERNAL_TOKEN: ${INTERNAL_TOKEN:?set INTERNAL_TOKEN in .env}`; **M6** `HANA_URL/HANA_USER/HANA_PASSWORD` guarded with `${...:?}`. NOTE: `compose up/config`
  now fails until `.env` defines `INTERNAL_TOKEN` and the HANA variables (neither `.env` has `INTERNAL_TOKEN` today). The sidecar logs an ERROR at startup
  (`StartupChecks`) when the token is empty (not under the `test` profile) and still starts (fail closed: `/report` = 401).
- **H2** `otr-sva-optimus-corredores/docker-compose.yml`: `ports: 8100:8100` replaced by `expose: ["8100"]`; the healthcheck runs inside the container (`wget localhost`) so it is unaffected.
- **H3 (container controls only)** both compose entries: `read_only: true`, `tmpfs` `/app/tmp` (512m) and `/tmp` (256m), `cap_drop: [ALL]`, `security_opt: [no-new-privileges:true]`.
  Verified with podman: the image renders a PDF under read-only rootfs + those two tmpfs + cap-drop ALL + no-new-privileges + non-root.
- **H4** temp files: `TempFiles` is the single place for the (now absolute) temp dir (`-Dbcs.reports.tmpdir`, default `<user.dir>/tmp`); `ReportDocument` registers the tmp .rpt
  before `open()` and `close()` always deletes it (separate try/finally); streams closed in `finally`; picture PNGs deleted in `finally`; startup sweep of `*.rpt`/`*.png` older than 1 h.
  Tests: `TempFilesLeakTest` (garbage upload, garbage blob, bad base64 / non-image / unknown picture / barcode, close idempotent, sweep).
- **H5** `RenderLock` (one fair `ReentrantLock`) around `RenderOrchestrator.render`, `CrystalReportService.generateReport` and the template upload/update paths; `ListMapResultSet` untouched.
  Tests: `ReportRenderConcurrencyTest` (two row shapes concurrently = their sequential outputs; and a deterministic test that `/report`, `/reports/generate` and upload block while the lock is held).
  Negative check: with the lock calls commented out the deterministic test FAILS ("/report finished while the render lock was held"); the row-shape equality test did NOT fail without the lock
  (the race window inside `populate` is microseconds, not reproducible even with 2100-row bodies), so it is a regression guard, not proof of the race. Lock restored.
- **H6 (non-behavioral)** Dockerfile ENTRYPOINT `-XX:MaxRAMPercentage=70 -XX:+ExitOnOutOfMemoryError`; compose `mem_limit: 2g`, `restart: unless-stopped`.
- **M5 (container)** non-root user `app`, `/app` and `/app/tmp` writable, fonts still copied as root at build time; Dockerfile `HEALTHCHECK` (wget on `/reports/health`; podman builds OCI
  images that drop HEALTHCHECK, use `--format docker` to keep it); `BcsCrystalReports/Dockerfile.dockerignore` (context = `modernized/otr-sva-optimus-corredores`, shared by both composes; the context-root
  `.dockerignore` is replaced for this Dockerfile by a whitelist: libs, pom.xml, src; no `.env`, `target`, `tmp`, `.git`); stale "UNVERIFIED / no Docker" comments fixed (Dockerfile, Fonts section).
- **M8** `Reply.safeFileName`: control characters removed, `"` and `\` become `_`; any other name is byte-identical. Test `ReplyFileNameTest`.
- **M9** `management.endpoints.web.exposure.include: health`; `/actuator/info` removed from the public list; rejected requests logged at WARN (method, normalized path, remote address, "no token"/"invalid token"; never the token).
  `ReportRestAuthTest` +2 tests: `/report/`, `/report;x=1`, `//report`, `/reports/%67enerate` (raw URI is not the exact public path: 401), `/reports//generate/`, `/actuator/health/` (401: only the exact path is public),
  `/actuator/info` (401 without token, 404 with it).
- **M4 (partial)** the file-path log lines of `ReportDocument` are DEBUG now.

### OPEN (not applied)
- H3: dedicated low-privilege HANA user for the sidecar, egress restriction to HANA only, restricting upload/update at the platform (infrastructure).
- H6: multipart limit (100MB today), request-body size guard, image pixel guard (they would reject inputs the legacy accepted).
- M1 real readiness check (health never touches DB/JRC); M2 per-request timeouts / bounded executor (20 s hang without data); M3 `debug:true` gating and `printStackTrace` to a logger (behavior);
  M4 PII in logs beyond the paths; M5: pin image digests and the Maven version, SBOM/CVE scan, font set equal to production (the goldens used Windows fonts, DejaVu in the image: pixel equivalence inside the image NOT proven);
  M7 audit log and a separate upload/update token (dedicated SIDECAR token).
- L1..L8 (low): Tomcat bump within Boot 2.7 (2.7.18 is EOL: compensating controls), double `ReportClientDocument` creation, `ReportClientDocument` close, RowsBuilder `X-Crystal-Reason`,
  smoke script portability / SHA-256 of the libs jars, error include flags, retirement date for `/reports/generate`. Also: `ListMapResultSet` static metadata (behavior) and refusing to render without data.
- Repo hygiene found while verifying: `core.autocrlf=true` rewrote the recorded goldens `src/test/resources/crystal/equiv/*/legacy/txt.txt` (the committed blobs are LF-normalized: 1002 bytes vs the recorded 1030 for `Custodia_Anexo2`),
  which made `EquivalenceTest` fail on the txt facet of 10 cases. The 10 files were restored in the working tree to their recorded bytes and `.gitattributes` now has `src/test/resources/crystal/equiv/**/legacy/* -text`;
  `git add --renormalize` / re-adding those 10 files is needed in the next commit, otherwise a fresh clone has the normalized goldens and the txt facet fails.

### Verification of step 5
- `mvn -o test` (JDK 11): 275 tests, 0 failures, 0 errors, 0 skipped (263 before + 12 new: TempFilesLeakTest 6, ReplyFileNameTest 2, ReportRenderConcurrencyTest 2, ReportRestAuthTest +2).
- Re-dump with `-Dequivalence.out` into a scratch folder: `out/` and `out-rest/` byte-identical to the stored ones; `compare.py` cases.json and cases-rest.json: same 100 / differs 0 each.
- Container: see `analysis/otr-sva-optimus-corredores/CRYSTAL_CONTAINER_CHECK.md` (addendum, image `bcs-crystal-reports:verify2`).
