# BcsReplica — Transformation Notes

Phase 4 modernization: otr-sva-optimus-corredores  
Pattern: strangler-fig, hexagonal architecture  
Source: Java EE 8 / TomEE  
Target: Spring Boot 4.1.1 / Java 25 / Port 8093

---

## Behavior Mapping

| Rule ID  | Legacy file / method                          | Modern implementation                                     | Notes                                                        |
|----------|-----------------------------------------------|-----------------------------------------------------------|--------------------------------------------------------------|
| B-REP-01 | `IniciarReplicaFull.java` — SQL Parameters string | `ReplicaService.buildParametersString(queryName, fecha)` | Format: `"BCS_query:<queryName>;Fecha:<fecha>"`              |
| B-REP-02 | `IniciarReplicaFull.java` vs `IniciarReplicaDelta.java` | `/api/replica/full` uses "QueryReplica"; `/api/replica/delta` uses "QueryReplicaDelta" | Both use Action="F" in WORKSTACK_T (legacy had Action="F" for both) |
| B-REP-03 | `ReplicaUtils.java` — `PLA.RespaldoReplicaTablasSebra == "S"` | `ReplicaService.esRespaldoActivo(configValue)` — `"S".equalsIgnoreCase(configValue)` | HANA gate: `ejecutarRespaldoSebra` always throws UOE in H2 env |
| B-REP-04 | `ReplicaUtils.java` — `fechaSistema.substring(0,7)` | `ReplicaService.extractCodPer(fechaSistema)` | Extracts "YYYY-MM" for COD_PER SQL param                     |

---

## Legacy File Disposition

| Legacy file              | Lines  | Disposition                                                               |
|--------------------------|--------|---------------------------------------------------------------------------|
| `IniciarReplicaFull.java`  | ~80    | Replaced by `ReplicaController.iniciarFull()` + `ReplicaService.buildParametersString()` |
| `IniciarReplicaDelta.java` | ~80    | Replaced by `ReplicaController.iniciarDelta()` + `ReplicaService.buildParametersString()` |
| `ReplicaUtils.java`        | ~200   | `esRespaldoActivo()` and `extractCodPer()` extracted; `ejecutarRespaldoSebra()` stubbed as UOE |
| `EnviarTablaReplica.java`  | ~24000 | @Disabled — pure HANA SQL, no portable logic; all 13-table DELETE+INSERT paths stubbed |

---

## Canary Result

**Canary test: REP-SVC-01**
```
Input:  buildParametersString("QueryReplica", "2024-01-15")
Output: "BCS_query:QueryReplica;Fecha:2024-01-15"
Result: PASS
```

---

## Test Execution Summary

| Test class                    | Enabled | Skipped | Result |
|-------------------------------|---------|---------|--------|
| `ReplicaServiceTest`          | 9       | 0       | PASS   |
| `ReplicaIntegrationTest`      | 4       | 1       | PASS   |
| **Total**                     | **13**  | **1**   | **BUILD SUCCESS** |

Skipped test: `REP-INT-05 ejecutarRespaldoSebra_hanaOnly` — `@Disabled("requires HANA: BSCL.Replica tables DELETE+INSERT across 13 BSCL tables")`

Maven output: `Tests run: 14, Failures: 0, Errors: 0, Skipped: 1`

---

## Architecture

```
BcsReplicaApplication (main only)
│
├── controller/
│   ├── ReplicaController       @RestController — 4 endpoints
│   └── GlobalExceptionHandler  @RestControllerAdvice — NPE/IAE/NFE→400, UOE→503
│
├── service/
│   └── ReplicaService          @Service — 3 public static pure-logic methods
│
├── domain/
│   ├── EntradaIniciarReplica   record(Long idEmpresa, Long idUsuario)
│   ├── SalidaIniciarReplica    mutable(Long erroresContador, String mensaje)
│   └── Workstack               record(idStack, key, table, action, status, stamp, parameters)
│
├── port/out/
│   └── ReplicaDataPort         interface — insertarWorkstack, obtenerFechaSistema,
│                               obtenerParametroRespaldoSebra, ejecutarRespaldoSebra
│
└── adapter/out/
    └── ReplicaRepositoryAdapter @Repository — NamedParameterJdbcTemplate
                                 ejecutarRespaldoSebra → UnsupportedOperationException
```

---

## HANA-Specific Paths (Stubbed)

- `EnviarTablaReplica.java` (~24K LOC): DELETE+INSERT across 13 BSCL tables  
  (TBCTCIRV, TBCTCURV, and 11 others) — no pure logic, all HANA SQL
- `ejecutarRespaldoSebra(codPer)` always throws `UnsupportedOperationException("requires HANA: BSCL.Replica tables DELETE+INSERT")`
- Controller returns HTTP 503 via `GlobalExceptionHandler` when this path is reached

---

## Non-Negotiable Rules Compliance

- [x] No hardcoded credentials — all via `${HANA_URL}`, `${HANA_USER}`, `${HANA_PASSWORD}`
- [x] `main()` only in `BcsReplicaApplication.java`
- [x] No SSL trust-all pattern
- [x] `Objects.requireNonNull` before first port call in all controller methods
- [x] `mvn test` → BUILD SUCCESS (14 run, 0 failures, 1 skipped)
