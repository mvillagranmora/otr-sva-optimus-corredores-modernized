# BcsTributarioJobs — Transformation Notes

## Overview

Modernized from Java EE 8 / TomEE to Spring Boot 4.1.1 / Java 25.

| Attribute            | Legacy                                       | Modern                              |
|----------------------|----------------------------------------------|-------------------------------------|
| Framework            | TomEE (Java EE 8)                            | Spring Boot 4.1.1                   |
| Java version         | Java 8                                       | Java 25                             |
| Architecture         | Flat procedural (`BcsJob` subclass)          | Hexagonal (port/adapter)            |
| Logging              | `System.out.println`                         | SLF4J / Logback                     |
| JDBC access          | Raw `Connection` / `CallableStatement`       | `NamedParameterJdbcTemplate` (main) |
| Error handling       | Bare `Throwable`, `return false`             | `RuntimeException`, `ProblemDetail` |
| Port                 | N/A                                          | 8101                                |

---

## Legacy Source

Single file: `src/cl/bcs/tributario/cargaipc/BcsJobCargaIPC.java`

Job type identifier: `CargaIPC` / module: `TRI`

---

## Business Rules Extracted

| ID       | Description                                                                       |
|----------|-----------------------------------------------------------------------------------|
| B-TRI-01 | `idModulo`, `simularOperacion`, `usaReglasNegocio` must be non-null.              |
| B-TRI-02 | If `erroresContador > 0` (OUT param 7 of the SP) → rollback and throw.            |
| B-TRI-03 | If `erroresContador == 0` → commit and return success.                            |
| B-TRI-04 | Legacy defaults: `idEmpresa = 1`, `idUsuario = 3` when params are absent.         |

---

## Bug Fixed

The legacy code checked `sp.getObject(4).toString()` to determine the error count.
Parameter index 4 is the `SimularOperacion` IN parameter — a String, not the error
counter. The correct OUT parameter for `erroresContador` is index 7.

**Legacy (incorrect):**
```java
if (Integer.parseInt(sp.getObject(4).toString()) > 0) { // bug: index 4 = SimularOperacion
```

**Modernized (correct):**
```java
if (!resultado.exitoso()) {  // checks erroresContador (OUT param 7) from CargaIpcResultado
```

In the legacy system the bug was likely latent — `SimularOperacion` was always `""` or
`"0"`, so `Integer.parseInt("0") == 0` and the rollback path was never triggered.

---

## HANA Stored Procedure

**Name:** `BSCL.Normativa.BD.Servicios::TRI.CargarValorFactorIpc`

**Signature (10 placeholders per legacy call string):**

| Position | Direction | Type    | Name               |
|----------|-----------|---------|--------------------|
| 1        | IN        | BIGINT  | idEmpresa          |
| 2        | IN        | BIGINT  | idUsuario          |
| 3        | IN        | VARCHAR | IdModulo           |
| 4        | IN        | VARCHAR | SimularOperacion   |
| 5        | IN        | VARCHAR | UsaReglasNegocio   |
| 6        | OUT       | INTEGER | advertenciasContador |
| 7        | OUT       | INTEGER | erroresContador    |
| 8–10     | ?         | ?       | reserved / not set |

The SP also returns three result sets: `p_out_Resultado`, advertencias list, errores list.

---

## Architecture (Hexagonal)

```
adapter/in/CargaIpcController          — REST POST /api/tributario/jobs/carga-ipc
service/CargaIpcJobService             — business logic, static helper methods
port/out/CargaIpcHanaPort              — output port (interface)
adapter/out/CargaIpcHanaAdapter        — HANA SP adapter (throws UOE without HANA)
domain/CargaIpcResultado               — immutable record (advertencias, errores)
infrastructure/GlobalExceptionHandler  — NPE→400, IAE→400, NFE→400, UOE→503
```

---

## Test Coverage

| Test class                 | Type        | Count | Notes                               |
|----------------------------|-------------|-------|-------------------------------------|
| `CargaIpcJobServiceTest`   | Unit        | 14    | Pure logic, no Spring context       |
| `CargaIpcIntegrationTest`  | Integration | 3 (1 skipped) | @SpringBootTest, H2, MockMvc |

**Canary (B-TRI-07):** Asserts `CargaIpcHanaAdapter.SP_NOMBRE` equals the exact
HANA SP name. If the constant is altered, this test fails immediately.

**Disabled tests (require live HANA):**
- `IT-TRI-03`: actual SP execution returning HTTP 200.

---

## MockMvc Configuration

`@AutoConfigureMockMvc` was removed in Spring Boot 4.1.1. Integration tests use:

```java
@BeforeEach
void setUpMockMvc() {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
}
```

---

## Environment Variables

| Variable      | Purpose                                  | Default (dev)              |
|---------------|------------------------------------------|----------------------------|
| `HANA_URL`    | SAP HANA JDBC URL                        | `jdbc:sap://localhost:30015` |
| `HANA_USER`   | HANA username                            | (required)                 |
| `HANA_PASSWORD` | HANA password                          | (required)                 |
| `SERVER_PORT` | HTTP listen port                         | `8101`                     |
