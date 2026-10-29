# Phase 3 Trading — Fix Designs

> Pre-requisite hardening fixes for BcsJobOrders / BcsOrdenServicios / BcsMesaServicios.
> All fixes apply to the **modernized** Spring Boot target, not to legacy files.

---

## RULE-057 — Race condition in job-lock selection (BcsJobManager / TransactionManager)

### Legacy symptom

`TransactionManager.readProgramm()` (line 48) selects candidate programs with a plain `SELECT` then immediately issues a separate `UPDATE ... SET "Bloqueo"='S'` to mark them taken. The `FOR UPDATE` clause that would lock the rows between select and update was commented out with the note `//ESTA GENERANDO LOG TRAN`. This creates a TOCTOU window: two concurrent job-manager nodes can SELECT the same program before either UPDATE completes, resulting in double-execution.

```java
// Legacy line 48 — problematic:
+ " "; //+ " LIMIT 1 FOR UPDATE OF P.\"Bloqueo\" "; //ESTA GENERANDO LOG TRAN
```

### Root cause

`FOR UPDATE` on HANA generates transaction log entries that caused performance concerns in the legacy environment. The team disabled it rather than tuning the transaction log. Without it, concurrent nodes race on the same lock record.

### Modernized fix design

**Option chosen: optimistic lock + single atomic UPDATE-then-SELECT**

Replace the two-step read-then-lock with a single atomic claim:

```java
// 1. Atomically claim exactly one PEN program for this node/taskType:
String claimSql = """
    UPDATE "BSCL.Plataforma.BD::BAT.Programa"
       SET "Bloqueo" = 'S', "IdEstado" = 'EJE'
     WHERE "IdPrograma" = (
           SELECT "IdPrograma"
             FROM "BSCL.Plataforma.BD::BAT.Programa"
            WHERE "IdEstado" = 'PEN'
              AND "TipoTarea" = :taskType
              AND "Nodo" = :node
              AND "Bloqueo" = 'N'
            LIMIT 1
           )
       AND "Bloqueo" = 'N'   -- optimistic guard
""";
int claimed = jdbcTemplate.update(claimSql, Map.of("taskType", taskType, "node", node));

// 2. Only if claimed==1, SELECT to get the full record:
if (claimed == 1) {
    String fetchSql = """
        SELECT * FROM "BSCL.Plataforma.BD::BAT.Programa"
         WHERE "IdEstado" = 'EJE'
           AND "TipoTarea" = :taskType
           AND "Nodo" = :node
           AND "Bloqueo" = 'S'
         LIMIT 1
    """;
    return jdbcTemplate.query(fetchSql, Map.of("taskType", taskType, "node", node), programaRowMapper);
}
return List.of(); // another node won the race — skip
```

**Why this is safe:** The `AND "Bloqueo" = 'N'` guard in the outer UPDATE acts as an optimistic lock. Only one concurrent UPDATE can win (HANA row-level lock during UPDATE is brief and deterministic); all others return `claimed=0` and skip gracefully.

**Port interface addition:**

```java
// JobLockPort.java
int claimPrograma(String taskType, String node);
List<Programa> fetchClaimedPrograma(String taskType, String node);
```

**Test oracle:**
- Concurrent calls with same taskType/node → exactly one returns a non-empty list.
- `claimPrograma` returns 0 when no PEN programs exist.

---

## RULE-059 — Failed jobs never retry (BcsJobManager)

### Legacy symptom

`BcsJobManager.poolDiary()` line 105 has the retry-reset call commented out:

```java
// Legacy line 105 — dead call:
// transacction.updateTryProgramm(connection);
```

`updateTryProgramm()` itself (`TransactionManager.java:259-279`) is correctly implemented: it resets programs in `ERR` state with `Intento < MaximoIntentos` back to `PEN`, incrementing `Intento`. Without the call, ERR programs are permanent — they never re-enter the execution queue regardless of `MaximoIntentos`.

### Modernized fix design

Uncomment and wire the call in the modernized `JobManagerService.pollDiary()`:

```java
// In JobManagerService.pollDiary() — Step 1 before claiming new work:
int reset = jobLockPort.resetRetryablePrograms(taskType, node);
if (reset > 0) {
    log.info("Reset {} ERR programs to PEN for retry", reset);
}
```

**Port method:**

```java
// JobLockPort.java addition:
int resetRetryablePrograms(String taskType, String node);
```

**Repository SQL (parameterized — matches legacy `updateTryProgramm` logic):**

```java
String sql = """
    UPDATE "BSCL.Plataforma.BD::BAT.Programa"
       SET "Bloqueo" = 'N', "IdEstado" = 'PEN', "Intento" = "Intento" + 1
     WHERE "IdEstado" = 'ERR'
       AND "TipoTarea" = :taskType
       AND "Nodo" = :node
       AND "Intento" < "MaximoIntentos"
       AND "Bloqueo" = 'N'
""";
return jdbcTemplate.update(sql, Map.of("taskType", taskType, "node", node));
```

**Call order in `pollDiary()`:**
1. `resetRetryablePrograms()` — promote ERR→PEN where retries remain
2. `claimPrograma()` — atomically claim one PEN (RULE-057 fix)
3. Execute the claimed program
4. On success → mark `EJE`→`FIN`; on failure → mark `EJE`→`ERR`, increment `Intento`

**Test oracles:**
- Program with `IdEstado='ERR'`, `Intento=1`, `MaximoIntentos=3` → after `resetRetryablePrograms`, state is `PEN`, `Intento=2`.
- Program with `IdEstado='ERR'`, `Intento=3`, `MaximoIntentos=3` → state unchanged (at max).

---

## RULE-068 — SQL injection in `updateIntent()` (TransactionManager)

### Legacy symptom

`TransactionManager.updateIntent()` lines 282-283 concatenates integers into SQL:

```java
// Legacy lines 282-283 — concatenation:
String query = "update \"BSCL.Plataforma.BD::BAT.Programa\" set \"Intento\" = "
        + intento + " where \"IdPrograma\" = " + idPrograma + "";
```

`intento` is `int` and `idPrograma` is `long` — Java primitives cannot carry SQL metacharacters so this is not exploitable in practice. However, it violates the parameterized-query rule and would become a real injection vector if types ever widen to `String`.

> **Note:** `executionCount()` (lines 522-546) cited in the brief is already correctly parameterized — it is NOT a vector.

### Modernized fix design

Replace with `NamedParameterJdbcTemplate` parameterized update:

```java
// In JobLockRepository.java:
public int updateIntent(long idPrograma, int intento) {
    String sql = """
        UPDATE "BSCL.Plataforma.BD::BAT.Programa"
           SET "Intento" = :intento
         WHERE "IdPrograma" = :idPrograma
    """;
    return jdbcTemplate.update(sql, Map.of("intento", intento, "idPrograma", idPrograma));
}
```

**Port method:**

```java
// JobLockPort.java addition:
int updateIntent(long idPrograma, int intento);
```

**Test oracle:**
- `updateIntent(42L, 3)` → SQL contains `?` placeholders, not raw values (verified via `NamedParameterJdbcTemplate` argument capture in test).

---

## Thread-Safety — `ObtenerFolioOrden.ultimosFolios` static HashMap

### Legacy symptom

`ObtenerFolioOrden.java:18`:

```java
public static HashMap<String,FolioDia> ultimosFolios = new HashMap<>();
```

`actualizarFolios()` is `synchronized`, but the field is `public static` — external callers can read and write it directly without synchronization. Under concurrent order processing (multiple threads calling into `BcsOrdenServicios`), this HashMap is a data race.

### Modernized fix design

Replace with `ConcurrentHashMap`, scoped private, accessed only through controlled accessors:

```java
// In ObtenerFolioOrdenService.java (modernized):
private final ConcurrentHashMap<String, FolioDia> ultimosFolios = new ConcurrentHashMap<>();

public FolioDia getOrActualizarFolios(Connection conn, Long idEmpresa, String fechaOrden) throws Exception {
    return ultimosFolios.computeIfAbsent(fechaOrden,
            fecha -> actualizarFoliosInternal(conn, idEmpresa, fecha));
}
```

`computeIfAbsent` is atomic in `ConcurrentHashMap` — only one thread computes for a given `fechaOrden`; subsequent callers return the cached value without re-entering the database.

**Additional constraint:** `FolioDia` counters (`FormadorMercado`, `OrdenEspecial`, etc.) are mutated after insertion. Change to `AtomicLong` fields or an immutable record:

```java
record FolioDia(
    AtomicLong formadorMercado,
    AtomicLong ordenEspecial,
    AtomicLong ordenMila,
    AtomicLong ordenRuteo1,
    AtomicLong ordenOrdinaria
) {
    static FolioDia initial() {
        return new FolioDia(
            new AtomicLong(600001), new AtomicLong(500001),
            new AtomicLong(400001), new AtomicLong(200001),
            new AtomicLong(100001)
        );
    }
    long nextFormador() { return formadorMercado.getAndIncrement(); }
    // ... etc.
}
```

**Test oracle:**
- 20 concurrent threads call `getOrActualizarFolios` with same `fechaOrden` → exactly 1 DB call (verified by Mockito `verify(times(1))`).
- Folio counters incremented under contention → no duplicate folios (use `CountDownLatch` in test).

---

## Hardcoded Credentials — `ConnectorsOrders.sapHanaConnection()` (RULE-076/077)

### Legacy symptom

`ConnectorsOrders.java:77-80`:

```java
public static Connection sapHanaConnection() throws SQLException {
    return DriverManager.getConnection("jdbc:sap://161.238.34.39:30215/?autocommit=false",
            "JOBMAN", "${HANA_PASSWORD}");
}
```

Hard-coded HANA IP, username `JOBMAN`, and password `${HANA_PASSWORD}` in source. IP `161.238.34.39` is a live production address. This method bypasses the application's DataSource entirely.

### Modernized fix design

Remove `sapHanaConnection()` entirely. The modernized `BcsJobOrders` module uses the standard Spring `DataSource` configured via environment variables — the same `HANA_URL` / `HANA_USER` / `HANA_PASSWORD` pattern used across all other modules:

```yaml
# application.yml — BcsJobOrders:
spring:
  datasource:
    url: ${HANA_URL}
    username: ${HANA_USER}
    password: ${HANA_PASSWORD}
    driver-class-name: com.sap.db.jdbc.Driver
```

All repository methods receive `NamedParameterJdbcTemplate` by constructor injection — no raw `DriverManager` calls anywhere in the modernized code.

**Credential rotation:** The hardcoded `JOBMAN`/`${HANA_PASSWORD}` credentials are flagged for rotation by the ops team (DEFERRED per user — rotation requires coordinating with other services that use the same account). Until rotated, they must not appear in source.

---

## Summary table

| ID | File (legacy) | Line | Severity | Fix type |
|---|---|---|---|---|
| RULE-057 | TransactionManager.java | 48 | HIGH | Atomic UPDATE-then-SELECT replaces SELECT + separate UPDATE |
| RULE-059 | BcsJobManager.java | 105 | HIGH | Uncomment `updateTryProgramm` — wire to `resetRetryablePrograms()` port |
| RULE-068 | TransactionManager.java | 282-283 | MEDIUM | Parameterized query via `NamedParameterJdbcTemplate` |
| Thread-safety | ObtenerFolioOrden.java | 18 | HIGH | `ConcurrentHashMap` + `computeIfAbsent` + `AtomicLong` folios |
| RULE-076/077 | ConnectorsOrders.java | 77-80 | CRITICAL | Remove `sapHanaConnection()` — all connections via env-var DataSource |
