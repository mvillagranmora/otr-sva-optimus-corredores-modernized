# CRYSTAL SPIKE - can we prove equivalence by rendering legacy vs sidecar? (BcsCrystalReports)

Date: 2026-10-21. Read-only spike; everything ran in `C:\Users\m.villagran.mora\AppData\Local\Temp\mvscratch\crystal` (scratch).
Nothing was written under `legacy/` or `modernized/`; this file is the only repo write.

## 1. Verdict

**PARTLY YES, and the "partly" is itself a finding.**

- YES: the JRC 14.1 engine runs headless on Windows/JDK 11 with no native library, no AWT display and no license error.
  The legacy `ReportDocument` (compiled unmodified from `legacy/`) renders PDF, Excel, Word, RTF and TXT from an
  H2-backed `"BSCL.Report::Template"` table plus JSON data. Output is byte-reproducible except for a handful of
  maskable bytes (section 4). A characterization/equivalence harness is feasible and cheap (~300 lines of Java).
- NO (today): the sidecar `cl.bcs.reports.report.ReportDocument` **cannot render any template that has data**, because
  its `ListMapResultSet` is a stub whose `getMetaData()` throws `UnsupportedOperationException("getMetaData not supported in stub")`.
  JRC fails inside `setDataSource` with `ReportSDKException: Unexpected database connector error`; the exception is
  swallowed by `printStackTrace()`, `exportDocument` returns null and the service answers 500 "Export returned null".
  The legacy engine uses `cl.bcs.utils.ListMapResultSet extends com.sun.rowset.CachedRowSetImpl` (BcsUtils), not a stub.
- Equivalence of the ENGINE part is demonstrable: with the legacy `ListMapResultSet` transplanted into the sidecar package
  ("sidefix", scratch only) the sidecar `ReportDocument` produces PDF/XLS/DOC/RTF/TXT identical to legacy (section 4).
  So the rest of the adaptation (log4j->slf4j, POI 5.x, package) is behaviour-neutral for this template.
- The sidecar as packaged would also fail at runtime on missing jars and missing font dir (section 2). None of these are
  detectable by the existing 3 tests (health, 400 on `{}`, 1 `@Disabled`).

Not proven by this spike: Java 8 runtime of legacy (legacy Dockerfile runtime = `eclipse-temurin:8-jre-alpine`; only JDK 11/25
exist locally, so "legacy" here = legacy source on JDK 11), the real HANA SQL data, subreport/image/pdf417 paths, the
other 152 templates, Linux fonts. Only ONE template (`CuentaInversion/Templates/ComprobanteCuentaInversion.rpt`, 81 KB,
1 command table "Comando" with 33 fields, no subreports/params) was rendered with data.

## 2. Environment and build evidence

- JDK 11.0.32 (Temurin) is OK. `mvn -o -f modernized/.../BcsCrystalReports/pom.xml` FAILS offline as shipped:
  `~/.m2/repository/com/crystaldecisions/*/14.1` and `avalon-framework/4.2.0` contain only `*.lastUpdated` stubs
  ("Path ... NOT allowed from central"); the jars are NOT installed (Dockerfile does `install:install-file` from `libs/`).
  `libs/CrystalCommon2.jar` and `libs/CrystalReportsRuntime.jar` are md5-identical to `legacy/.../libs-private/`
  (8ea818ce..., 85411951...).
- Workaround (scratch copy, `sidecar/pom.xml`): crystal jars + avalon as `system` scope -> `systemPath ${project.basedir}/libs/*.jar`.
  `mvn -o -q test-compile` under JDK 11: BUILD SUCCESS (main + test classes).
- Tests: `mvn -o test` cannot run offline: surefire 2.22.2 is incomplete in ~/.m2 (`SurefireReportParameters` missing);
  surefire 3.5.6 needs `junit-platform-launcher:1.8.2` (only 1.12.2 and 6.0.3 are cached; 1.12.2 breaks Jupiter 5.8.2 with
  `NoSuchMethodError ReflectionUtils.returnsVoid`). So I could NOT obtain a fresh surefire count. Evidence available:
  `target/surefire-reports` (2026-10-07): `Tests run: 3, Failures: 0, Errors: 0, Skipped: 1`. Substitute run under JDK 11:
  `harness/SmokeApp.java` boots the Spring context (H2) and calls the endpoints: `GET /reports/health -> 200 "BcsCrystalReports UP"`,
  `POST /reports/generate {} -> 400 {"error":"templateId is required"}`, unknown templateId -> 400 `Template not found: NOPE`
  (the H2 "Schema BSCL not found" stack trace is swallowed by `readDocument`, which returns false).

### Classpath that JRC needs at runtime (found one failure at a time)
| Failure (verbatim, short) | Missing jar | Where it exists | In sidecar pom? |
|---|---|---|---|
| `NoClassDefFoundError com/businessobjects/foundation/logging/LoggerManager` | `logging.jar` | legacy/libs-private | NO |
| `NoClassDefFoundError org/apache/log4j/Logger` | log4j 1.x (`log4j.jar`) | legacy/libs-private | NO (only log4j-to-slf4j 2.x) |
| `NoClassDefFoundError com/crystaldecisions/common/keycode/KeycodeException` | `keycodeDecoder.jar` | legacy/libs-private | NO |
| `NoClassDefFoundError org/apache/commons/configuration/CompositeConfiguration` | commons-configuration 1.2 (+commons-lang 2.6, commons-collections 3.2.2, commons-logging 1.2) | gradle cache / legacy build.gradle | NO |
| `NoClassDefFoundError com/ibm/icu/util/Calendar` | icu4j 71.1 | gradle cache / legacy build.gradle:170 | NO |
| `ReportSDKException: java.lang.NullPointerException` at `OpenTypeFontManager.a(SourceFile:384)` during PDF export | directory `${java.home}/lib/fonts` must exist | JDK 8 had it; JDK 9+ does not | n/a (environment) |

Font NPE root cause (verified with javap + `FontProbe`): JRC's `opentype.b.if()` returns null when `${java.home}/lib/fonts`
does not exist and that null is added to the list of font maps; `Map.get` then NPEs. It scans `-DSystemFontPath`
(default `C:\Windows\Fonts`) plus `${java.home}/lib/fonts`. Workaround used: scratch copy of JDK 11 (`jdk11/`) with a
`lib/fonts` containing 13 TTFs copied from C:\Windows\Fonts. `-Djava.home=` override does not work (JVM needs `lib/modules`).
The sidecar Dockerfile (`eclipse-temurin:11-alpine`, no fonts, no `-Djava.awt.headless=true`, no `-Dfile.encoding`) would hit the same NPE.

## 3. Rendering evidence (template ComprobanteCuentaInversion.rpt, 1 row, headless)

Harness: `harness/Render.java` (reflection on the engine class), H2 2.1.214 in-memory:
`CREATE SCHEMA BSCL; CREATE TABLE BSCL."BSCL.Report::Template"("Id" VARCHAR, "ReportName", "MasterReportId", "ReportFile" BLOB,
"ReportVersion" INT, "CompanyId" BIGINT)` + one row (Id 'T1', bytes of the .rpt). `new ReportDocument("BSCL", ds)` ->
`readDocument("T1")` -> `setDocumentResultSet(Map schema, JsonArray)` -> `exportDocument(fmt)`; `-Djava.awt.headless=true`.
Data: `data/comprobante.json` (33 fields from the template, taken with `Probe.java` = ReportClientDocument table/field dump),
schema `{"IdCargoAbonoMovimiento":"number","Monto":"number", dates as "string"}`.

| Run | Result |
|---|---|
| legacy `ReportDocument` (unmodified, compiled in scratch, `--add-exports/--add-opens java.sql.rowset/com.sun.rowset`) | renders, PDF 62220 B, 1 page Letter 612x792 |
| sidecar `ReportDocument` as in repo | `readDocument=true`, then `UnsupportedOperationException: getMetaData not supported in stub` (ListMapResultSet.java:106) -> `Unexpected database connector error` -> `exportDocument returned null` |
| sidecar `ReportDocument` + legacy `ListMapResultSet` ("sidefix") | renders, PDF 62220 B, 1 page |
| no data set at all | JRC tries the DB saved in the .rpt: `ReportSDKLogonException ... Cannot connect to jdbc:sap://161.238.6.186:30015 ... semaphore timeout` (about 20 s). Templates embed a live HANA host/IP. |

Licensing/native/AWT: no keycode/license error, no native lib, no DISPLAY needed (PDF text extractable, no watermark seen via
OpenPDF `PdfTextExtractor`: "Comprobante Cuenta Inversion N 12345 ... 1,500,000.50"). Legal licence status not assessed.

## 4. Determinism, other formats, masks

PDF, same engine twice (legacy c1 vs c2, sidefix c1 vs c2) and legacy vs sidefix (`pdfdiff.py`, `streamdiff.py`):
same length, header `%PDF-1.7`, Producer "Powered By Business Objects", Creator "Crystal Reports", NO /CreationDate, /ModDate, /ID or XMP.
The ONLY raw byte differences: the `/Title (<uuid>.rpt)` in the Info dict (UUID of the temp file `tmp/<uuid>.rpt`, 29-30 bytes, offset ~58764,
same length). Legacy vs sidefix of the same minute: 0 differing decompressed streams (10 streams).
Second dynamic item: the report's PrintDate/PrintTime special fields live inside the Flate content stream as plain text
(`(21-10-2026)Tj ... (12:32PM)Tj`); they differ when runs cross a minute boundary (stream 1 differs only there).
Observed once a 62219-byte output from a first run (others 62220, 5 runs); unexplained, re-test with more runs.

Masks (PDF): (1) raw regex `/Title \([0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}\.rpt\)`; (2) inflate every `stream..endstream` (zlib) and
mask `\(\d{2}-\d{2}-\d{4}\)Tj` and `\(\d{1,2}:\d{2}[AP]M\)Tj`; better: set both runs in the same minute or normalise the clock.
Page count: OpenPDF 1.3.30 (`~/.m2/.../openpdf-1.3.30.jaspersoft.3.jar`) `PdfReader.getNumberOfPages()` works (harness/PdfInfo.java).

Other formats (one run each, legacy and sidefix, same data):
| format | works | size | legacy vs sidefix |
|---|---|---|---|
| pdf | yes | 62220 | identical modulo /Title (+ clock text) |
| excel (.xls, BIFF) | yes | 39424 | 1 range of 6 bytes at offset 36263 (an 8-byte double = print time-of-day) |
| word (.doc) | yes | 11859 | identical; NOTE `ReportExportFormat.MSWord` produces RTF text (`{\rtf1...`), byte-identical to the rtf export |
| rtf | yes | 11859 | identical; header has `{\creatim\yr2026\mo9\dy21\hr12\min33}` (minute granularity) -> mask `\\creatim[^}]*` |
| txt (Excel -> POI -> text) | yes | 1070 | identical (legacy POI 3.17 vs sidecar POI 5.2.5); encoding = JVM default charset (`getBytes()`); legacy runs `-Dfile.encoding=UTF-8` (setenv.sh), sidecar image sets nothing -> accents differ on Alpine/POSIX unless fixed |

Subreport template (APV/CONTRATO_SAAPV.rpt, subreport "Entidades"): `readDocument` calls `insertDocument` for each subreport =
`INSERT` into the template table + `SELECT CAST(SYSUUID AS VARCHAR) FROM DUMMY` (HANA-only). H2 needs `CREATE TABLE DUMMY(SYSUUID UUID
GENERATED ALWAYS AS (RANDOM_UUID()))` + one row, and the "Id" column must not be a PK. Side effect to note: every render of a template with
subreports WRITES rows to `BSCL.Report::Template` (never cleaned). With the shim it rendered (102672 B, no data set).

## 5. Inventory of the legacy entry points (read from code)

`CallReportServlet` (`@WebServlet("/report")`, `@MultipartConfig`, GET and POST identical; gateway path `/BSCL/Plataforma/GWJAVA/PLA/report`)
- Auth: header `X-SESSION-TOKEN` (missing -> 401 text "401 - NOT AUTHORIZED"); `Utils.getSessionData(conn, token)`; error count > 0 -> 401;
  derives companyId (IdEmpresa) and userId. `PLA.Ambiente` read. The sidecar has NO auth/session.
- Mode switch by query-parameter presence (checked in this order):
  1. `?update` (multipart): `reportId`, `reportName`, `reportVersion` (int), part `data` (.rpt). `readDocument(reportId)` else 404; `updateDocument(...)` UPDATE
     of ReportFile/ReportName/MasterReportId(null)/ReportVersion/CompanyId; answers `{"result":"OK"}` application/json.
  2. `?upload` (multipart): `reportName`, `reportVersion`, part `data`. `uploadDocument` writes tmp/<uuid>.rpt, opens it in JRC (validation), INSERTs template
     (+ one row per subreport with MasterReportId=uuid); answers `{"result":"OK","reportId":"<uuid>"}`.
  3. `?rpt`: JSON body `{reportId}`; `getDocument` streams ReportFile as attachment `<name>_<id>.rpt` (name/id are never set -> `_.rpt`); 404 if null.
  4. `?file`: no-op marker (used by UI `report?file`); falls through to render.
  5. (default) render, JSON body below. No `Content-Type` header is set on the render response; `Content-Disposition: attachment; filename="<documentName>_<documentId>.<EXT>"`
     (documentId is never assigned; documentName ends up as the last inserted subreport name or ""). Extensions PDF, XLS, DOC, RTF, TXT.
- Render body: `{ "reportId": uuid, "exportFormat": "pdf|excel|word|rtf|txt" (lower/trim; other value -> falls to 500),
  "debug": bool, "_serviceURL": string (debug only),
  "configuration": { "mainReport": { "reportName", "parameters": [ {"type":"image"|"pdf417","name","data"} ] | {} (object = TODO, ignored) , "data": {...}|[...] },
  "subReports": [ {"reportName": <subreport name>, "data": {...}|[...] } ] } }`
  - parameters: array required (absent or non-array/object -> 404); each item needs non-empty type, name, data else 404; `image` -> `setDocumentImage(base64[,dataURI], pictureObjectName)`
    (resize to picture height, `importPicture`, replaces the picture object); `pdf417` -> `setDocumentBarcodeImage` (barcode4j PDF417, only type "pdf417" accepted, else
    `ReportSDKException` swallowed). Unknown types are silently ignored. Note `setDocumentImage` writes `tmp/<uuid>.png` relative to the CWD.
  - data as OBJECT `{serviceId, serviceData:{...}, serviceResponse:"" | "json"}`: server-side `getServiceData` -> `StoredProcedure ...ACC.ValidaHashServicioUsuario(p_IdEmpresa,p_IdUsuario,p_Hash=serviceId)`
    returns type `p` (stored procedure: runs `p_out_Servicio` with serviceData, takes first result set; `serviceResponse=="json"` -> first column base64 JSON array) or `q`
    (OData proxy `server/BSCL/Plataforma/GWJAVA/PLA/call?t=<serviceId>` with `$select,$filter,$top,$skip,$orderby,$format,$inlinecount,$callback,$handler`, token forwarded;
    results = `d.results`). `serviceData.$schema` = `[{field,type: number|date|datetime|string|blob|other}]` drives conversions: `/Date(ms)/` -> `java.sql.Date/Timestamp.toString()`,
    blob = base64 -> byte[]. Result null -> 404. This is DATA ACCESS; it must stay outside the sidecar (caller resolves it).
  - data as ARRAY of flat objects: `new Gson().fromJson -> Map` rows -> `ListMapResultSet.populate` (no schema conversion in the servlet array path; if the array is EMPTY the main
    dataset is not set at all and JRC then reconnects to the DB saved in the rpt). Subreport arrays are populated even if empty (`list.get(0)` -> IndexOutOfBounds -> 500).
  - subReports: keyed by `reportName` = the subreport name inside the .rpt; `setSubdocumentsResultSet` binds `resultsetSubreport<i>Table`; reportName absent -> skipped.
  - Errors: 401/404 plain text, 500 "500 - INTERNAL ERROR" (+ stack trace when `debug:true`).
`TemplateServlet` (`/template`, GET/POST): FreeMarker HTML-template renderer (`template`, `context` params), no Crystal, no auth, no DB. Out of scope for the sidecar.
Other callers of the same engine (in-process, not through the servlet): 23 `exportDocument` + 6 `exportDocumentToFile` calls in Normativa (GenerarCertificado*Masivo, GenerarCartasCIR1985,
BcsJob*), Mesa (CrearInformesCoberturaPatrimonial), FondoMutuo, Facturacion, Custodia (GenerarReporteCuadratura) and `report/demo/Main.java`; they use
`new ReportDocument(Utils.getSchema(), dataSource)`, `setDocumentResultSet(schema, JsonArray)`, `setDocumentImage/BarcodeImage`, 1x `setSubdocumentsResultSet(FromQuery)`.
The quarantine must also redirect these, not only `/report`.

Sidecar contract today (`POST /reports/generate`, `GET /reports/health`): `templateId, format, beanData (Map, single row, every field forced to "string"), imageBase64/imageName,
barcodeData/barcodeName`. MISSING vs legacy: multi-row array data, per-field schema (number/date/datetime/blob), subreports (service never calls setSubdocumentsResultSet),
multiple image/pdf417 parameters, upload/update/rpt template management (endpoints and the `uploadDocument/updateDocument/getDocument` plumbing is in the class but unexposed),
auth/session, serviceId resolution (by design), Content-Disposition naming, 404 vs 400 mapping (sidecar gives 400 for unknown template, 500 for render failure),
HTTP error text. `format` default pdf when null; unknown format returns PDF content-type but `exportDocument` returns null -> 500.

## 6. Commands used (all in scratch; paths relative to `mvscratch/crystal`)

```
# build (sidecar copy with system-scope jars; JDK 11)
mvn -o -q test-compile                         # in sidecar/ -> BUILD SUCCESS ; mvn -o -X test-compile -> classpath saved in sidecar/cp.txt
# legacy engine, unmodified sources, JDK 11
javac --add-exports java.sql.rowset/com.sun.rowset=ALL-UNNAMED --add-exports java.sql.rowset/com.sun.rowset.internal=ALL-UNNAMED \
      -cp legacy/cp.txt -d legacy/out  ReportDocument SubreportDocument JsonResultSet (BcsPlataformaReporteApi) + ListMapResultSet (BcsUtils)
# render (run.sh <side|sidefix|legacy> <run> <rpt> <fmt> <ext> <data.json> <schema.json>)
java -Djava.awt.headless=true [--add-exports/--add-opens java.sql.rowset/com.sun.rowset=ALL-UNNAMED] -cp <harness;engine;libs;extra> Render <class> <rpt> <fmt> <data> <schema> <out>
py -3 -I pdfdiff.py a.pdf b.pdf ; py -3 -I streamdiff.py a.pdf b.pdf ; java PdfInfo x.pdf      # byte ranges, inflated streams, page count/text
```
Extra jars copied from `legacy/.../libs-private` and the gradle cache into `extra/`: logging.jar, log4j.jar (1.x), keycodeDecoder.jar,
commons-configuration-1.2, commons-lang-2.6, commons-logging-1.2, commons-collections-3.2.2, icu4j-71.1. Legacy classes compile against
log4j-api/core 2.17.1 (only for `Configurator.setLevel`), POI 3.17, sql2o 1.6.0, gson 2.10.1, barcode4j 2.1, avalon 4.2.0.

## 7. Risks

1. Sidecar data path is broken today (stub ListMapResultSet) - any "equivalence" claim for the sidecar made without rendering is unfounded; existing tests do not exercise Crystal at all.
2. Missing runtime jars (logging, log4j1, keycodeDecoder, commons-configuration, icu4j) and `${java.home}/lib/fonts`: the container would fail at first request. Fonts also change glyph metrics/line breaks:
   a Linux image with different fonts than the legacy JRE 8 host will not be byte- or even layout-identical; compare per-environment (same font set) or at text/structure level.
3. Legacy production runtime is JRE 8 (Alpine), sidecar JDK 11; JRC behaviour is expected to be the same but is unproven here (no JDK 8). Encoding: legacy `-Dfile.encoding=UTF-8`; sidecar sets none (TXT `getBytes()`).
4. `ListMapResultSet` (legacy) depends on JDK-internal `com.sun.rowset.CachedRowSetImpl/Row` and reflective write to private fields (`rvh`, `numRows`) -> needs `--add-exports/--add-opens` on JDK 11 and breaks on newer JDKs;
   column types are inferred from the FIRST row's Gson values only (String->VARCHAR, Number->DECIMAL, byte[]->BLOB, else JAVA_OBJECT; dates arrive as strings), so null in row 1 or numeric-as-string changes rendering.
5. Templates embed a production HANA host (161.238.6.186:30015); if the caller omits data the engine tries to connect (20 s timeout). The sidecar must always set a result set (even empty) and ideally run with no route to that host.
6. `readDocument` is not read-only (INSERT per subreport, HANA `DUMMY/SYSUUID`); tmp files are created relative to CWD (`tmp/`); JRC singletons/threading unverified under concurrent requests.
7. Errors are swallowed (`printStackTrace`) -> null stream -> generic 500; the REST contract needs explicit error mapping before it can be characterised.
8. Only 1 of 153 templates rendered; others use subreports (many), images, pdf417, formulas with date/locale (reportDoc locale forced `Locale.ENGLISH`), and may hit other JRC classpath gaps.

## 8. Minimal harness design (for the equivalence proof)

- Engines under test, same JVM flags and same font directory: (A) legacy `ReportDocument`+BcsUtils `ListMapResultSet` unmodified; (B) sidecar `ReportDocument` as built from `modernized/` (after the ListMapResultSet fix).
- H2 (2.1.x, in-memory): schema `BSCL`, table `"BSCL.Report::Template"("Id","ReportName","MasterReportId","ReportFile" BLOB,"ReportVersion","CompanyId")` loaded from `templates/**/Templates/*.rpt`
  (Id = relative path), plus `DUMMY(SYSUUID)` shim; "Id" not PK. Constructor `new ReportDocument("BSCL", ds)`.
- Datasets: one JSON per template derived from `Probe` (fields/types of each table and subreport) and from the sibling `Servicios/*.sql` output columns: (1) typical row, (2) NULL/empty strings and 0 amounts,
  (3) long text to force page breaks (multi-page), (4) accents/enyes/euro, (5) multi-row, (6) empty array (set empty rowset). Subreports via `setSubdocumentsResultSetFromJson`. Images/pdf417 for templates that have PictureObjects.
- Matrix: template x dataset x format (pdf, excel, word, rtf, txt) x engine, each executed twice per engine (self-determinism) in a fresh JVM, CWD = scratch (so `tmp/` is isolated).
- Compare: pdf -> mask `/Title (<uuid>.rpt)` + inflate streams + mask PrintDate/PrintTime text, then page count and byte equality; xls -> compare via POI cell values (skip the 8-byte print-time double);
  doc/rtf -> mask `\creatim{...}`; txt -> byte equality with fixed `-Dfile.encoding=UTF-8`. Failure classes: render error, null stream, size/page mismatch, content mismatch.
- Also run the REST layer (`POST /reports/generate`) once it accepts array data/subreports, comparing HTTP result vs direct engine output.

## 9. Recommended steps

1. Fix `ListMapResultSet` in the sidecar: either port the legacy class verbatim (needs `--add-exports/--add-opens` in the Dockerfile ENTRYPOINT) or reimplement on the public API
   (`RowSetProvider.newFactory().createCachedRowSet()` + public `javax.sql.rowset.RowSetMetaDataImpl`, `moveToInsertRow/updateObject/insertRow/moveToCurrentRow`) keeping the first-row type inference
   (the public-API variant is UNTESTED here). Add a test that renders a real template (ComprobanteCuentaInversion) and asserts page count/text; gate it on the jars being present.
2. Add the missing jars to `libs/` + pom/Dockerfile (logging, log4j 1.x, keycodeDecoder, commons-configuration 1.2, commons-lang 2.6, commons-collections 3.2.2, commons-logging, icu4j 71.1) and fix
   the build story offline (install-file step or system-scope profile); add fonts (`-DSystemFontPath`, and a `${java.home}/lib/fonts` directory, even empty/with the font set) , `-Djava.awt.headless=true`, `-Dfile.encoding=UTF-8`.
3. Move the harness from scratch into `modernized/.../BcsCrystalReports/src/test` (or a separate test module) using the H2 table above; keep byte masks in one utility; commit the masks as the oracle definition.
4. Generate the legacy goldens from legacy source (A) per template/dataset and store them (hash + extracted text), then run (B) against them; start with the 5-6 smallest templates (Mesa/FWDCONDICIONES*, MRF*, template_cartas_liquidacion), then subreport/image/pdf417 ones.
5. Extend the REST contract before characterising it: `data` array/object-resolved-by-caller, per-field `schema`, `subReports[]`, `parameters[]` (image/pdf417), `exportFormat`, upload/update/rpt template endpoints (or decide to keep template management in the monolith), error codes 401/404/500 and filename header.
6. Inventory the 29 in-process callers (Normativa/Mesa/FondoMutuo/Facturacion/Custodia jobs) and redirect them to the sidecar client.
7. If a Java 8 environment is obtainable (legacy runtime), repeat the same harness once under JRE 8 to close the JDK 8 vs 11 gap.
