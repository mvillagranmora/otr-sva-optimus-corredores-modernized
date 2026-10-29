# Crystal Reports Integration Guide

> **Audiences:** Agents modernizing domain modules. Read before implementing any report-generation endpoint.

## Architecture

Crystal Reports runs isolated in `BcsCrystalReports` (Java 11 / Spring Boot 2.7.18, port 8100).
All Java 25 services call it via REST. The Crystal SDK is NOT available on Java 25.

```
[Java 25 Service]
    └──▶ CrystalReportsPort (interface in BcsUtils)
            └──▶ CrystalReportsRestClient (Spring @Component, HTTP)
                    └──▶ POST http://bcs-crystal-reports:8100/reports/generate
                                └──▶ [BcsCrystalReports — Java 11]
                                        └──▶ Crystal SDK → HANA BLOB → .rpt → PDF/Excel/Word/RTF/TXT
```

Templates are stored as BLOBs in HANA table `"BSCL.Report::Template"` (column `Id` = UUID, `ReportFile` = BLOB).
Templates are NOT filesystem files — never hardcode a `.rpt` path.

## Template inventory by domain

| Dominio | Templates | Prioridad |
|---|---|---|
| Mesa | 55 | Alta |
| Normativa | 35 | Alta |
| FondoMutuo | 18 | Media |
| Custodia | 13 | Media |
| Orden | 9 | Media |
| Tesoreria | 7 | Media |
| Contabilidad | 7 | Media |
| Facturacion | 5 | Media |
| APV | 3 | Baja |
| CuentaInversion | 2 | Baja |
| **Total** | **160** | |

Template UUIDs are stored in HANA — query `"BSCL.Report::Template"` WHERE `ReportName` matches the legacy report name.

## How to integrate in a new Java 25 service

### 1 — Add BcsUtils dependency (if not already present)

In the module's `pom.xml` (version comes from parent pom — never hardcode):
```xml
<dependency>
    <groupId>cl.bcs</groupId>
    <artifactId>bcs-utils</artifactId>
</dependency>
```

### 2 — Inject CrystalReportsPort

```java
import cl.bcs.utils.report.CrystalReportRequest;
import cl.bcs.utils.report.CrystalReportsPort;

@Service
public class MiDominioService {

    private final CrystalReportsPort crystalReports;

    public MiDominioService(CrystalReportsPort crystalReports, /* other deps */) {
        this.crystalReports = crystalReports;
    }

    public byte[] generarInformePdf(String templateId, Map<String, Object> datos) {
        CrystalReportRequest req = CrystalReportRequest.pdf(templateId);
        req.beanData = datos;   // campos del DatosComprobanteCrystal correspondiente
        return crystalReports.generateReport(req);
    }

    public byte[] generarInformeExcel(String templateId) {
        return crystalReports.generateReport(CrystalReportRequest.excel(templateId));
    }
}
```

### 3 — Environment variable in application.yml

```yaml
crystal:
  reports:
    url: ${CRYSTAL_REPORTS_URL:http://localhost:8100}
```

In docker-compose: `CRYSTAL_REPORTS_URL=http://bcs-crystal-reports:8100`

### 4 — Mock in tests

```java
import cl.bcs.utils.report.CrystalReportsPort;
import cl.bcs.utils.report.CrystalReportRequest;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class MiDominioServiceTest {

    @MockBean
    CrystalReportsPort crystalReports;

    @Test
    void generarInformePdf_returnsBytes() {
        byte[] fakePdf = {0x25, 0x50, 0x44, 0x46}; // %PDF magic bytes
        when(crystalReports.generateReport(any())).thenReturn(fakePdf);

        // call your service method...
    }
}
```

No `@Disabled` needed for Crystal Reports tests — mock the port, no HANA required.

## CrystalReportRequest fields

| Campo | Tipo | Descripción |
|---|---|---|
| `templateId` | String (UUID) | UUID del template en `"BSCL.Report::Template"` |
| `format` | String | `"pdf"` \| `"excel"` \| `"word"` \| `"rtf"` \| `"txt"` |
| `beanData` | `Map<String, Object>` | Campos del comprobante (equivalente a DatosComprobanteCrystal) |
| `imageBase64` | String | Imagen logo en base64 (opcional) |
| `imageName` | String | Nombre del campo imagen en el template (opcional) |
| `barcodeData` | String | Datos para código de barras PDF417 (opcional) |
| `barcodeName` | String | Nombre del campo barcode en el template (opcional) |

## BcsCrystalReports API endpoints

| Endpoint | Body | Response | Notas |
|---|---|---|---|
| `POST /reports/generate` | `CrystalReportRequest` JSON | `byte[]` (PDF/Excel/etc.) | Content-Type en respuesta según `format` |
| `GET /reports/health` | — | `"BcsCrystalReports UP"` | Health check |

## Response Content-Type by format

| format | Content-Type | Extension |
|---|---|---|
| pdf | application/pdf | .pdf |
| excel | application/vnd.ms-excel | .xls |
| word | application/msword | .doc |
| rtf | application/rtf | .rtf |
| txt | text/plain | .txt |

## DatosComprobanteCrystal mapping

The legacy `DatosComprobanteCrystal` class has ~107 public fields. When migrating a domain
that uses it, map the relevant fields to `beanData` as a `Map<String, Object>`. Only include
fields that the specific template actually uses — Crystal ignores extra fields.

Example for a Facturacion template:
```java
Map<String, Object> datos = new HashMap<>();
datos.put("RutEmisor", factura.getRutEmisor());
datos.put("FolioDocumento", factura.getFolio());
datos.put("FechaEmision", factura.getFechaEmision().toString());
datos.put("MontoTotal", factura.getMontoTotal());
// ... only fields the template uses
CrystalReportRequest req = CrystalReportRequest.pdf(templateId);
req.beanData = datos;
```

## Classes in BcsUtils (cl.bcs.utils.report)

| Clase | Rol |
|---|---|
| `CrystalReportsPort` | Interface — inject this, never the impl |
| `CrystalReportsRestClient` | Spring `@Component` implementation — auto-registered via component scan |
| `CrystalReportRequest` | Request DTO with factory methods `pdf()`, `excel()`, `word()` |

## Deployment notes

- `BcsCrystalReports` is a **standalone** service — NOT in the Java 25 parent pom.xml
- It requires `JAVA_HOME` pointing to Java 11 to run
- Its `Dockerfile` uses `eclipse-temurin:11-alpine`
- It must start before any domain service that calls it (docker-compose `depends_on`)
- Crystal Reports JARs are installed to local Maven repo (`~/.m2`); they are NOT in the source tree (gitignored via `BcsCrystalReports/.gitignore`)

## Deferred

Full migration of 160 templates from Crystal Reports to JasperReports 7.x is deferred to
a dedicated migration phase. See `CRYSTAL_REPORTS_MIGRATION.md` for the full analysis.
