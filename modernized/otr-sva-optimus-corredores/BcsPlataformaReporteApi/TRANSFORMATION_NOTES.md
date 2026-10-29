# BcsPlataformaReporteApi — Transformation Notes

## Status: Temporary compatibility shim — DECOMMISSION after TomEE shutdown

## What this module is

`BcsPlataformaReporteApi` was the legacy Crystal Reports API library distributed as a
TomEE JAR.  It provided the `cl.bcs.plataforma.report.api.ReportDocument` class, which
callers used to generate PDF/Excel/Word/RTF/TXT reports from Crystal Reports `.rpt`
templates stored in SAP HANA.

This modernized module is a **thin HTTP shim** that keeps the old REST surface alive
(`POST /reporte-api/generate`, `GET /reporte-api/health`) during the canary transition
window.  It has **zero Crystal Reports SDK dependencies** and does not process reports
itself — it proxies every request to `BcsCrystalReports` on port 8100.

## Phase 5 decommission plan

| Step | Action |
|------|--------|
| 1 | Verify all callers use `CrystalReportsPort` (bcs-utils) — not this shim's endpoints |
| 2 | Confirm no traffic hits `POST /reporte-api/generate` in production logs |
| 3 | Shut down TomEE instances |
| 4 | Remove `<module>BcsPlataformaReporteApi</module>` from parent `pom.xml` |
| 5 | Delete this directory |

## Callers already migrated

All modules that previously instantiated `ReportDocument` directly have been migrated
to inject `CrystalReportsPort` from `bcs-utils` and call `generateReport(CrystalReportRequest)`.
`CrystalReportsRestClient` (the default implementation) calls
`POST http://bcs-crystal-reports:8100/reports/generate`.

## What the legacy module did

`ReportDocument` (legacy) provided:

- `uploadDocument(name, stream, version, companyId)` — stored `.rpt` files in SAP HANA
  (`BSCL.Report::Template` table) via BLOB insert
- `readDocument(id)` — retrieved a stored template and opened it via Crystal Reports SDK
- `setDocumentResultSet(...)` — bound SQL or JSON data sources to the report
- `exportDocument(format)` — exported the report as PDF/Excel/Word/RTF/TXT using the
  Crystal Reports Java Runtime API (`com.crystaldecisions.sdk.*`)
- `setDocumentImage(base64, name)` — embedded images in report templates
- `setDocumentBarcodeImage(type, data, name)` — embedded PDF417 barcodes

All of this logic now lives in `BcsCrystalReports` (Java 11, Spring Boot 2.7.18,
standalone service on port 8100), which is not a child of the Java 25 parent pom
because the Crystal Reports SDK is incompatible with Java 25.

## Port assignment

| Service | Port |
|---------|------|
| BcsPlataformaReporteApi (this shim) | 8115 (healthcheck only) |
| BcsCrystalReports (real implementation) | 8100 |

## Legacy interface summary (for reference)

The legacy service exposed via HTTP:

```
POST http://[host]:[port]/[path]/report?file
Content-Type: application/json

{
  "reportId"     : "<UUID>",
  "exportFormat" : "pdf | txt | word | excel | rtf",
  "configuration": {
    "mainReport" : { "reportName": "...", "parameters": [...], "data": "..." },
    "subReports" : [{ "reportName": "...", "data": "..." }]
  }
}
```

The modernized endpoint is simpler:

```
POST /reporte-api/generate
Content-Type: application/json

{
  "templateId"  : "<UUID>",
  "format"      : "pdf | excel | word | rtf | txt",
  "beanData"    : { ... },
  "imageBase64" : "...",
  "imageName"   : "...",
  "barcodeData" : "...",
  "barcodeName" : "..."
}
```

This shim accepts the modernized format and proxies it directly to BcsCrystalReports.
