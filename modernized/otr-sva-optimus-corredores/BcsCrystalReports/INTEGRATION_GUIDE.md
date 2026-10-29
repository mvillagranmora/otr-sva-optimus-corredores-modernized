# BcsCrystalReports — Guía de integración para otros servicios

## Propósito

Servicio de cuarentena para el motor Crystal Reports. Corre en **Java 11** (el SDK de
Crystal Reports usa JNI nativo incompatible con Java 25). Todos los demás servicios Java 25
deben llamar a este servicio vía REST para generar reportes.

**Puerto:** `8100`  
**Stack:** Spring Boot 2.7.18 / Java 11  
**Imagen Docker:** `bcs-crystal-reports:latest`  
**Nombre en docker-compose/red:** `bcs-crystal-reports`

## Endpoints disponibles

### `POST /reports/generate`

Genera un reporte en el formato solicitado.

**Request body (JSON):**
```json
{
  "templateId": "uuid-del-template-en-hana",
  "format": "pdf",
  "beanData": {
    "campo1": "valor1",
    "campo2": "valor2"
  },
  "imageBase64": "base64-opcional",
  "imageName": "nombre-campo-imagen-en-template",
  "barcodeData": "datos-barcode-opcional",
  "barcodeName": "nombre-campo-barcode"
}
```

**Campos:**
- `templateId` (requerido): UUID del template `.rpt` almacenado como BLOB en `"BSCL.Report::Template"`
- `format`: `pdf` (default) | `excel` | `word` | `rtf` | `txt`
- `beanData` (opcional): mapa clave→valor con los campos del reporte (equivalente a `DatosComprobanteCrystal`)
- `imageBase64` / `imageName` (opcionales): imagen base64 para reemplazar un `PictureObject` en el template
- `barcodeData` / `barcodeName` (opcionales): datos para generar código de barras PDF417

**Response:** bytes del documento con el Content-Type apropiado (`application/pdf`, etc.)

### `GET /reports/health`

Health check del servicio. Devuelve `200 OK` con cuerpo `BcsCrystalReports UP`.

## Cómo integrar desde un servicio Java 25

Agrega la variable de entorno `CRYSTAL_REPORTS_URL` (configurada en docker-compose).

**Ejemplo con Spring RestClient (Java 25 / Spring Boot 4.1.1):**
```java
@Service
public class CrystalReportsClient {

    private final RestClient restClient;

    public CrystalReportsClient(@Value("${crystal.reports.url:http://localhost:8100}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public byte[] generateReport(String templateId, String format, Map<String, Object> data) {
        return restClient.post()
            .uri("/reports/generate")
            .contentType(MediaType.APPLICATION_JSON)
            .body(Map.of("templateId", templateId, "format", format, "beanData", data))
            .retrieve()
            .body(byte[].class);
    }
}
```

**application.yml del servicio cliente:**
```yaml
crystal:
  reports:
    url: ${CRYSTAL_REPORTS_URL:http://localhost:8100}
```

## Templates

Los templates `.rpt` están almacenados como BLOBs en la tabla HANA:
```
"BSCL.Report::Template" (Id VARCHAR, ReportName VARCHAR, ReportFile BLOB, ...)
```

Los templates **no están en el filesystem** — el servicio los lee de HANA por UUID.

## Dominios que usan reportes Crystal (36 callers, 160 templates)

| Dominio | Templates | Servicio modernizado |
|---|---|---|
| Mesa | 55 | BcsMesaServicios (8085) |
| Normativa | 35 | — |
| FondoMutuo | 18 | — |
| Custodia | 13 | BcsCustodiaServicios (8097) |
| Orden | 9 | BcsOrdenServicios (8086) |
| Tesorería | 7 | BcsTesoreriaServicios (8073) |
| Contabilidad | 7 | BcsContabilidadServicios (8081) |
| Facturación | 5 | BcsFacturacionServicios (8099) |
| APV | 3 | BcsAPVServicios (8083) |
| CuentaInversión | 2 | BcsCuentaInversionServicios (8080) |

## Migración futura a JasperReports

La migración real a JasperReports 7.x queda **diferida** a una fase posterior.
Este servicio de cuarentena es la solución provisional mientras se analiza la factibilidad.
