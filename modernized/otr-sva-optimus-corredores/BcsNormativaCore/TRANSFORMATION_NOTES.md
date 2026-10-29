# BcsNormativaCore — Transformation Notes

**Source:** Java EE 8 / TomEE — `legacy/otr-sva-optimus-corredores/BSCL/Normativa/Java/BcsNormativaCore/`
**Target:** Spring Boot 4.1.1 / Java 25 — `modernized/otr-sva-optimus-corredores/BcsNormativaCore/`
**Port:** 8109
**Build:** `mvn test` — 52 tests passed, 1 skipped (`@Disabled("requires HANA")`), 0 failures

---

## Architecture

Hexagonal (ports and adapters):

```
cl.bcs.normativa
├── domain/          — POJOs: Campo, Esquema, Documento, Validacion, Clasificador, Cuadratura,
│                       Empresa, ResumenCarga, ResumenGeneracion, RangoFechas, EsquemaException, ...
├── port/
│   ├── in/          — Use-case interfaces (FindDocumentoUseCase, FindEsquemaUseCase, ...)
│   └── out/         — Repository interfaces (DocumentoPort, EsquemaPort, CampoPort, ...)
├── service/         — Use-case implementations (@Service); pure-logic methods as public static
├── adapter/
│   ├── in/          — REST controllers (@RestController)
│   └── out/         — JDBC adapters (@Repository, NamedParameterJdbcTemplate)
└── config/          — GlobalExceptionHandler
```

---

## Key Decisions

### 1. Separate JDBC Adapters (not monolithic)
The legacy code had a single DAO layer without separation. A naive single-class adapter
implementing all ports failed to compile because:
- `DocumentoPort.buscarPorId(long)` returns `Optional<Documento>`
- `EmpresaPort.buscarPorId(long)` returns `Optional<Empresa>`
- `ClasificadorPort.buscarPorClave(String, String, String)` returns `Optional<Clasificador>`
- `CuadraturaPort.buscarPorClave(String, String, String)` returns `Optional<Cuadratura>`

Java does not allow a class to implement two interfaces with the same method signature but
different return types. Solution: one adapter class per domain group.

| Adapter | Implements |
|---|---|
| `DocumentoJdbcAdapter` | `DocumentoPort` |
| `EsquemaJdbcAdapter` | `EsquemaPort`, `CampoPort` |
| `ValidacionJdbcAdapter` | `ValidacionPort` |
| `ClasificadorJdbcAdapter` | `ClasificadorPort` |
| `CuadraturaJdbcAdapter` | `CuadraturaPort` |
| `EmpresaJdbcAdapter` | `EmpresaPort` |
| `ConfiguracionJdbcAdapter` | `ConfiguracionPort`, `ParametroPort` |
| `ResumenJdbcAdapter` | `ResumenCargaPort`, `ResumenGeneracionPort` |

### 2. HANA Table Names → H2 Aliases
HANA uses dot-qualified names like `"BSCL.Normativa.BD::DOC.Campo"`. H2 cannot handle
these names. Each adapter uses short aliases in SQL; the mapping is documented in
`src/test/resources/schema-normativa.sql` comments and in each adapter's Javadoc.

### 3. DataParser as @Service with public static helpers
`DataParserService` is a Spring `@Service` to allow injection, but all pure parsing
logic (`normalizeNumber`, `discardDecimals`) is declared `public static` so callers
can invoke them without a bean reference, matching the mandate.

### 4. Batch (HANA Stored Procedures)
The legacy `Batch.java` scheduled processes via SAP HANA stored procedure
`BAT.AgregarPrograma` and used temporary table creation. This cannot run on H2.
Test `scheduleProcess_requiresHana` is annotated `@Disabled("requires HANA")`.

### 5. SecuenciadorRegistro pattern
Legacy used `MAX(ID)+1` for ID generation (no sequences, no auto-increment). Preserved
in `ResumenJdbcAdapter.nextId(tabla)`.

### 6. Jackson serialization property
Spring Boot 4.x uses Jackson 3.x (`tools.jackson`). The property
`spring.jackson.serialization.write-dates-as-timestamps` from Jackson 2.x is not a
valid enum constant in Jackson 3.x and causes context load failure. The property was
removed; the default behavior (ISO date strings) is acceptable for this service.

### 7. Credential externalization
`application.properties` uses environment variables with empty defaults:
```properties
spring.datasource.url=${NORMATIVA_DB_URL:jdbc:sap://localhost:39013}
spring.datasource.username=${NORMATIVA_DB_USER:}
spring.datasource.password=${NORMATIVA_DB_PASSWORD:}
```
No credentials are hardcoded.

---

## REST API (port 8109)

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/normativa/documentos/{id}` | Busca documento por id |
| `PATCH` | `/api/normativa/documentos/{id}/en-proceso` | Actualiza indicador EnProceso |
| `GET` | `/api/normativa/esquemas/{nombre}/{version}` | Busca esquema con campos |
| `GET` | `/api/normativa/esquemas/{nombre}/{version}/validaciones` | Lista validaciones activas |
| `GET` | `/api/normativa/empresas/{idEmpresa}` | Busca empresa |
| `GET` | `/api/normativa/empresas/{idEmpresa}/corredora?idBolsa=` | Codigo corredora |
| `GET` | `/api/normativa/empresas/{idEmpresa}/configuracion/{codigo}` | Valor de configuracion |
| `GET` | `/api/normativa/esquemas/{nombre}/{version}/parametros/{codigo}` | Valor de parametro |

### Error Mapping (GlobalExceptionHandler)
| Exception | HTTP Status |
|---|---|
| `NullPointerException` | 400 Bad Request |
| `IllegalArgumentException` | 400 Bad Request |
| `NumberFormatException` | 400 Bad Request |
| `UnsupportedOperationException` | 503 Service Unavailable |

---

## Test Coverage

| Suite | Tests | Scope |
|---|---|---|
| `DataParserServiceTest` | 20 | Unit — B-NOR-DP-01..06 |
| `EsquemaTest` | 12 | Unit — B-NOR-ESQ-01..08 |
| `NormativaCoreIntegrationTest` | 20 (1 skipped) | H2 integration — B-NOR-INT-01..13 |

Total: **52 tests**, 0 failures, 1 skipped.

---

## Legacy Files (not modified)

All legacy files remain untouched under:
`legacy/otr-sva-optimus-corredores/BSCL/Normativa/Java/BcsNormativaCore/`
