# Render equivalence harness: legacy engine vs sidecar (BcsCrystalReports)

Isolation only: nothing of the engine was modernized or fixed. The harness renders the SAME template bytes with the SAME
fake rows through (A) the unmodified legacy classes (`legacy/.../BcsPlataformaReporteApi/.../report/api/*.java` +
BcsUtils `ListMapResultSet`, compiled from `legacy/` into `target/legacy-recorder`, legacy is never written) and (B) the
sidecar (`cl.bcs.reports.report.ReportDocument`), in 5 formats (pdf, excel, word, rtf, txt), and compares after masks.
Both run Crystal JRC 14.1 on JDK 11, headless, `-Dfile.encoding=UTF-8`, same font set (see "Not proven").

## Layout
- `src/test/resources/crystal/equiv/<Dominio>_<Plantilla>/` : `spec.json` (template, formats, image/pdf417 params, note),
  `data.json` (schema + rows + optional subreports), `legacy/<format>.<ext>` (goldens) and `legacy/meta.json`
  (recording day, xls print date/time windows).
- `src/test/legacy-recorder/` : `record.sh` (re-records the goldens), `probe.sh` + `ProbeTemplates.java` (Crystal API dump
  of tables/fields/subreports/pictures/formulas), `gen_cases.py` + `cases.py` + `gen_sql.py` + `gen_values.py` (data
  generator), `make_cases.py` (cases.json for compare.py), `RecordLegacy/LegacyRenderer/FontHome.java`, `env.sh`.
- `src/test/java/cl/bcs/reports/equiv/` : `EquivalenceTest` (JUnit, one dynamic test per case and format plus a data
  guard), `SidecarRenderer`, `Masks`, `PdfStructure`, `XlsWindows`, `Facet`, `EquivCase`, `H2Shim`, `MasksSelfTest`.

## Run
```
JAVA_HOME=<jdk11> mvn -o -f modernized/otr-sva-optimus-corredores/BcsCrystalReports/pom.xml test
   -Dequivalence.out=<dir>/out        # also writes out/{legacy,new}/<case>/<format>.txt (facets) for compare.py
   -Dequivalence.cases=<dir>          # another set of cases (negative proof); -Dequivalence.raw=<dir> raw sidecar bytes
py -3 src/test/legacy-recorder/make_cases.py analysis/otr-sva-optimus-corredores/equivalence/BcsCrystalReports
py -3 <plugin>/scripts/compare.py analysis/otr-sva-optimus-corredores/equivalence/BcsCrystalReports/cases.json
```
The test FAILS (never skips) on any masked difference, render error, swallowed engine exception (stderr with "Exception"),
missing SDK/JVM flags/fonts, or a missing golden.

## Re-record the goldens (legacy engine)
```
JAVA_HOME=<jdk11> bash src/test/legacy-recorder/record.sh [case-substring ...]     # needs libs/ and ~/.m2 (offline)
```
Each case is rendered in two passes >= 65 s apart (`RECORD_GAP_MS` overrides): the legacy engine must be self-deterministic
modulo the masks, otherwise nothing is written for that case. Regenerate data: `probe.sh target/probe_all.json
templates/**/*.rpt` then `py -3 gen_cases.py` (edit `cases.py` to change the selection). Goldens are tied to the machine's
font set: re-record when the fonts change.

## Chosen templates (2 per domain, 20 cases, 100 case-formats) + 1 regression case (21 cases, 105 case-formats)
Rule: HANA QA listing preferred (`CRYSTAL_REPO_ONLY_TEMPLATES.csv`), small, a `.sql` beside, no subreport except one.
Fields come from the .rpt (Crystal API); types from the Crystal field type; lengths/scales refined from the `.sql` named by
the template's CALL (column declarations and `AS "Alias"` expressions). Rows are fake and deterministic. Formula-driven
constraints are read from the .rpt formulas (e.g. `Split({FechaComprobante}, "-")` needs `d-M-yyyy`: without it JRC fails
with "Failed to regenerate records", found on Facturacion).
| Dominio | Plantilla | in HANA QA | rows x fields | params | subreport | PDF pages | .sql typing |
|---|---|---|---|---|---|---|---|
| APV | CONTRATO_SAAPV | yes | 1x80 | image Picture1 | Entidades | 3 | 42/80 |
| APV | NOTIFICACIONCLIENTESAAPV | yes | 1x18 | - | - | 1 | 6/18 |
| Contabilidad | INFORME_CARTERA_LEASING | no (all 7 are repo-only) | 3x53 | - | - | 1 | 52/53 |
| Contabilidad | VencimientosAnticiposPactos | no | 3x25 | - | - | 1 | 8/25 |
| CuentaInversion | ComprobanteCuentaInversion | yes | 1x33 | image Picture6 | - | 1 | 7/33 |
| CuentaInversion | Anexo16CuentasPorCobrar | yes | 3x19 | - | - | 1 | 12/19 |
| Custodia | Anexo2 | yes | 3x24 | - | - | 1 | 6/24 |
| Custodia | comprobante_garantia_ingreso_retiro | yes | 1x34 | pdf417 | - | 1 | 4/34 |
| Facturacion | BoletaFacturacion | yes | 1x95 | - | - | 1 | 32/95 |
| Facturacion | ComprobanteFacturacion | yes | 1x114 | - | - | 1 | 38/114 |
| FondoMutuo | template_cartas_liquidacion | yes | 1x11 | - | - | 2 | 0/11 (rpt only) |
| FondoMutuo | template_informe_liquidacion | yes | 3x19 | - | - | 1 | 0/19 (rpt only) |
| Mesa | MRFVentaInstrumentos | yes | 1x51 | - | - | 1 | 27/51 |
| Mesa | Articulo84 | yes | 3x13 | - | - | 1 | 1/13 |
| Normativa | UAF48PEPINFO | yes | 3x9 | - | - | 1 | 0/9 (rpt only) |
| Normativa | Certificado30 | yes | 1x22 | - | - | 1 | 0/22 (rpt only) |
| Orden | COMPROBANTE_ORDEN | yes | 1x69 | pdf417 | - | 1 | 29/69 |
| Orden | COMPROBANTE_ORDEN_CREDICORP_PERU | no | 1x64 | pdf417 | - | 1 | 33/64 |
| Tesoreria | CartaInstruccionPagoBcoChile | yes | 1x16 | - | - | 1 | 1/16 |
| Tesoreria | DetalleComprobanteTesoreria | yes | 3x50 | - | - | 1 | 8/50 |
| Facturacion | InformeGananciaCapital (regression, from fresh case F035) | n/a | 6x28 | - | - | 2 | ConsultarGananciaCapital.sql |

InformeGananciaCapital is the regression case for the POI version: its xls has the number format `[$$-0409]* #,##0;-[$$-0409]* #,##0;-`
that POI 3.17 (legacy) cannot parse (TXT writes the raw number) and POI 5.x applies (`-$   98,765`). Legacy goldens are the ones
recorded from the unmodified legacy engine (poi-3.17) for the fresh run, copied byte for byte. With POI 5.2.5 its txt facet FAILS.

Sidecar path: 4 cases go through the real REST/service layer (`POST /reports/generate`: ComprobanteCuentaInversion,
template_cartas_liquidacion, MRFVentaInstrumentos, Certificado30; see the `EQUIV <case> [fmt] mode=` lines of the test
output); the other 16 drive `ReportDocument` directly as the legacy servlet does because the REST contract cannot carry
them yet (rows > 1, typed `blob` fields such as LogoCorredora, subreports). Pdf417 and image work through the engine
(`setDocumentBarcodeImage`, `setDocumentImage`); a subreport rendered (data verified present with marker values).

## Masks (all in code: Masks, PdfStructure, XlsWindows; proven not to hide data by MasksSelfTest + the negative run)
- PDF: `/Title (<uuid>.rpt)`; in the inflated content streams `(dd-MM-yyyy)Tj` and `(h:mmAM)Tj`; font program streams
  (`/Length1`), ToUnicode CMaps, standalone numeric arrays (/Widths), every `/Length` and the xref/trailer. Reason (found by
  the 2-pass recording): the PDF embeds a font SUBSET, so the minute digits of the print time change the embedded glyph set,
  and with it lengths and offsets (one case differed by 520 bytes between two legacy runs). Text, positions, images,
  page structure, font names and object order stay exact. Also compared: page count and extracted text (facet).
- Excel: the 8-byte doubles of the print date/time NUMBER records recorded in `legacy/meta.json` (`offset:8`; only
  ComprobanteCuentaInversion has them: 2 windows, 6 of the 8 bytes of the time double differ); each window must still hold a
  temporal value in both files. Other 19 xls were byte-identical raw.
- RTF/Word: `\creatim \revtim \printim \buptim` groups (the Word export IS RTF, byte-identical to the rtf export).
- Text (pdf text, rtf, txt): time `h:mm AM/PM` (also POI's `1\:47 PM`) and the literal dates of the recording day and of
  today in 34 formats. The data guard test asserts the fake data never matches these masks.
- TXT: no other mask; 19 of 20 are byte-identical raw.

## Result (this machine, 2026-10-21)
21 cases x 5 formats = 105 facets: legacy == sidecar after masks in all (compare.py dev and rest: `same 105, differs 0`; the first 20
cases: 130 tests when recorded). Negative proof: the same suite pointed at 2 cases whose rows differ by ONE character
(`PENA`->`PENX`) fails 10/10 renders. Raw (unmasked) identity: xls 19/20, txt 19/20, pdf 3/20, rtf/word 0/20 (the
`\creatim` minute digits change the length by 1); every raw difference is explained by the masks above.

## Deviations legacy vs sidecar
Engine behavior: one found by the fresh-input verification and FIXED: POI 5.2.5 changed the TXT export for number formats such as
`[$$-0409]* #,##0`; the sidecar now pins POI 3.17 like the legacy (case InformeGananciaCapital guards it). The other adaptations
(package, log4j->slf4j, ported `ListMapResultSet`) render byte-identically. Contract/environment
differences observed (not engine changes, nothing fixed):
1. REST contract: one flat row, all fields as string, 1 image + 1 pdf417, no subreports/typed fields: 16 of 20 cases
   cannot be sent through it (16 of 20). The service also maps unknown template to 400 and render failure to 500.
2. Both engines swallow SDK errors (`printStackTrace`) and return null/500; the test therefore also fails on any
   "Exception" in stderr during a render.
3. TXT shows mojibake in both engines (`Antiguef¿dad` in Anexo16): equal, a legacy quirk to keep.
4. Templates with formulas that parse data (Facturacion `FechaString`) fail in the legacy engine with "Failed to regenerate
   records" when the data format does not fit (the sidecar was not run with the bad data): the caller's data contract
   matters, not the engine.

## NOT proven
The other 133 templates of the 153 (formulas, subreports with several levels, pictures by name, Mesa/Normativa bulk); real HANA data
and column types (dates/decimals arrive typed from SQL; here dates are strings, ListMapResultSet infers VARCHAR/DECIMAL from
row 1); stored-procedure-driven templates (`Pro` tables, e.g. Contabilidad INFORME_OPERACIONES_PACTO_DETALLE); the Java 8 (legacy
runtime, Alpine) behavior (only JDK 11 here); Linux/Alpine fonts (goldens are bound to the Windows font set copied into a
throw-away `${java.home}/lib/fonts`) and `-Dfile.encoding`/locale of the real image; concurrency; the pdf417/image outputs
under other barcode data; upload/update/rpt template management; auth/session.

## REST path (`EquivalenceRestTest`, `RestRenderer`)
All 21 cases x 5 formats again, but through `POST /report` (the legacy servlet's JSON body, built from spec.json/data.json,
`X-Internal-Token`), so the 16 cases the old `/reports/generate` could not carry (multi-row, typed fields, subreports, several
images) now go over HTTP too. Same goldens and masks. `-Dtest=EquivalenceRestTest -Dequivalence.out=<dir>/out-rest` and
`py -3 make_cases.py <dir> out-rest cases-rest.json rest` build `cases-rest.json` for compare.py.
