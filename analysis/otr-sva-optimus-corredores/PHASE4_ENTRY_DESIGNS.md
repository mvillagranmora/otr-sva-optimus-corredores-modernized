# Phase 4 Core Business — God-Object Decomposition Plan & Fix Designs

> Written 2026-10-10 to satisfy three unmet Phase 4 entry criteria in `MODERNIZATION_BRIEF.md:230-236`:
> god-object decomposition plan (SME review pending), RULE-018 fix design, RULE-052 fix design.
> All designs target the **modernized** Spring Boot 4.1.1 / Java 25 code, never legacy files.
> **No module code has been written.** This document is for SME review first.

---

## 0. Why this document exists before any code

`/code-modernization:modernize-transform otr-sva-optimus-corredores BcsCustodiaServicios` was requested. It was not run, because
Phase 4's entry criteria are unmet and the brief is binding. The decomposition plan is the first
of them, and the brief is explicit about ordering: god-objects are to be decomposed
*"before writing characterization tests, not after"* (`MODERNIZATION_BRIEF.md:228`).

### What the measurement found first

Phase 4's modules are far less complete than a `TRANSFORMATION_NOTES.md` file implies. Counted
2026-10-10:

| Module | Legacy `.java` | Modernized `.java` | Of which deferred 503 stubs | Real coverage |
|---|---:|---:|---:|---|
| BcsSocioNegocioServicios | 558 | 10 | 0 | **~2%** |
| BcsCustodiaServicios | 286 | 190 | **180** | **~2%** |
| BcsCustodiaJobs | 37 | 9 | 0 | ~24% |
| BcsFacturacionServicios | 104 | 60 | 0 | ~58% |
| BcsFacturacionJobs | 14 | 9 | 0 | ~64% |
| BcsSocioNegocioJobs | 13 | 9 | 0 | ~69% |
| BcsReplica | 5 | 9 | 0 | complete |

In `BcsCustodiaServicios`, 97 of 99 service classes throw `HTTP 503` with a `DEFERRED:` javadoc.
Only `CustodiaServiciosService` (five utility helpers) and the `JavaCallService` interface are real.
Its 37 passing tests exercise those helpers and two reflection-based security assertions — which is
why `VERIFICATION.md` records zero equivalence cases for it.

**The deferral reason is now stale.** Every cross-service dependency the stubs cite already exists:

| Cited by the stubs | Now implemented at |
|---|---|
| `cl.bcs.plataforma.mer.CalcularParidad` | `BcsPlataformaServicios/.../service/mer/CalcularParidadService.java` |
| `cl.bcs.plataforma.tas.EntregarPreciosTasas` | `BcsPlataformaServicios/.../service/tas/EntregarPreciosTasasService.java` |
| `cl.bcs.plataforma.pla.ObtenerDiasHabilesAnteriores` | `BcsPlataformaServicios/.../service/pla/ObtenerDiasHabilesAnterioresService.java` |
| `cl.bcs.fondomutuo.CalcularMontoNumeroCuota` | `BcsFondoMutuoServicios/.../domain/CalcularMontoNumeroCuotaEntrada.java` |

So the wall that caused the stubbing is gone, and Phase 4 is genuinely actionable — at XL scale,
as the brief says.

---

# Part A — God-object decomposition plan

## A.1 The shared anti-pattern

All five god-objects are the same shape: a `JavaCall` implementation whose entire behaviour sits in
one `logica(Connection, Entrada_X)` method, with request/response DTOs as `public static class`
inner types, SQL already externalised to `.sql` resources via `loadResourceToString`, and a
`public static void main` left in for developer testing.

Measured 2026-10-10 (`logica()` is the single largest method in four of the five):

| God-object | Module | Lines | Inner classes | Methods | Largest method |
|---|---|---:|---:|---:|---|
| `CrearComprobante` | Facturacion | 5,175 | 36 | 28 | **`logica()` 3,695 L** (L978-4673) |
| `ProcesarFacturacionCustodia` | Facturacion | 5,385 | 20 | 17 | `logica()` 662 L (L397-1059) |
| `ConsultarCustodiaPortafolio` | Custodia | 4,002 | 23 | 10 | **`logica()` 2,362 L** (L1295-3657) |
| `CrearClienteFullWS` | SocioNegocio | 3,558 | 26 | 78 | `logica()` 331 L (L675-1006) |
| `ModificarSocioNegocio` | SocioNegocio | 3,123 | 26 | 8 | **`logica()` 2,366 L** (L519-2885) |

Two consequences for sequencing:

- `CrearClienteFullWS` (78 methods, largest 331 L) is **already decomposed** by legacy standards. It
  is the cheapest of the five and the right place to prove the recipe.
- `CrearComprobante` (3,695 L in one method, 36 inner classes) is the most dangerous and should go
  last, after the recipe is proven twice.

`ProcesarFacturacionCustodia` is a different shape: the work is spread across eight `Procesar*`
methods of 400–660 L each (`ProcesarAnticipoSimultaneaCompra` 658 L, `ProcesarVentasConCustodia`
543 L, `ProcesarVentaSimultaneas` 447 L, `ProcesarCompraSimTransito` 405 L,
`ProcesarCompraSimultaneas` 403 L). These are natural per-operation seams — it decomposes more
readily than its line count suggests.

> **Naming hazard:** there are two `CrearComprobante.java` files. The Phase 4 god-object is
> `BSCL/Facturacion/.../facturacion/CrearComprobante.java` (5,175 L). A second, unrelated
> `BSCL/Tesoreria/.../tesoreria/CrearComprobante.java` also exists and is **not** in scope here.

## A.2 Target structure

Keep the hexagonal layout the modernized modules already use
(`adapter/out`, `controller`, `domain`, `port/in`, `port/out`, `service`), and split each god-object
along four seams:

```
domain/            the inner-class DTOs, as Java records — immutable, no JDBC, no Gson
port/in/           one use-case interface per business operation
service/           orchestration: one class per operation, no SQL, no Connection
port/out/ + adapter/out/   every SQL resource behind a repository interface
controller/        the JavaCall/REST adapter — parses and validates only
```

Four rules for every split, each chosen so a reviewer can check it mechanically:

1. **Pure calculation first.** Arithmetic that takes values and returns values moves to the domain
   as static methods on records, with no `Connection` parameter. These are the only pieces testable
   without a database, so they are written first and pinned by characterization tests.
2. **One `Connection` owner.** Only `adapter/out` touches JDBC. A service that needs data declares a
   port method. This is what makes the rest unit-testable.
3. **Parameter objects, not parameter lists.** `CalculoDiferenciaMercado(BigDecimal, BigDecimal,
   BigDecimal, BigDecimal, ...)` becomes a record. Positional `BigDecimal` arguments are the single
   likeliest source of a silent transposition defect in this migration.
4. **`main()` is deleted, never ported.** See A.4.

## A.3 Worked seam: `ConsultarCustodiaPortafolio`

The one in `BcsCustodiaServicios`, the module that triggered this plan. Its 2,362-line `logica()`
has identifiable phases; the other methods are already clean seams.

| Legacy | Lines | Target | Notes |
|---|---|---|---|
| 23 inner classes | L178-1294 | `domain/` records | `Tabla_Salida` (L240-747) and `ResultadoConsulta` (L761-944) are the large ones; both have `copyValuesFrom` mutators that records remove by construction |
| `logica()` phase 1 — config & date defaults | L1295-1320 | `service/ConsultaCustodiaContext` | Reads `PLA.FechaSistema`; the `0001-01-01` / `2300-12-31` sentinel dates are behaviour, not bugs — pin them |
| `logica()` phase 2 — ~60 SQL queries | L1320-3390 | `port/out/CustodiaPortafolioRepository` + adapter | SQL already externalised; each `*_SELECT` field becomes one port method. The `HIS` / non-`HIS` pairs are a current-vs-historical switch, not duplication to collapse |
| `logica()` phase 3 — valuation | L3390-3657 | `service/ValorizacionService` | Guarded by the comment `/** Se Usa el Precio Actual Mercado Solo para RV **/` (L3390) — an RV-only branch that must survive |
| `CalculoDiferenciaMercado` | L3658-3678 | `domain/DiferenciaMercado` | Pure; 5 positional `BigDecimal` args → record. **Write tests here first** |
| `CalculoDiferenciaCustodiaMercado` | L3679-3685 | `domain/DiferenciaMercado` | Pure |
| `ValidaMoneda` | L3686-3741 | `domain/MonedaPolicy` | Pure; currency-consistency rule across six result tables |
| `TransformarAHash` | L3742-3774 | delete | Hand-rolled `List`→`HashMap` indexing; `Collectors.toMap` with an explicit merge function. **Pick the merge deliberately** — the legacy silently keeps the last duplicate |
| `procesaTablaResultado` | L3775-3881 | `service/` | The result-set merge orchestrator |
| `ObtenerUltimoDiaHabil` | L3882-3917 | **call `BcsPlataformaServicios`** | Business-calendar logic duplicated locally; the modernized `ObtenerDiasHabilesAnterioresService` now owns it. Do not re-implement |
| `calcularCantidadMontoPrecioPromedio` | L3918-3938 | `domain/PrecioPromedio` | Pure; weighted-average — a rounding-sensitive site, give it explicit `MathContext` |
| `call(Connection, JsonObject)` | L3939-3950 | `controller/` | Thin adapter |
| `main(String[])` | L3951-4001 | **delete** | RULE-076 vector — see A.4 |

Apply the same four rules to the other four god-objects; `ProcesarFacturacionCustodia` splits one
service per `Procesar*` method, and `CrearClienteFullWS` mostly needs its 78 methods grouped into
services rather than split further.

## A.4 `main()` removal — ties to RULE-076

All five god-objects carry a `public static void main`. Three obtain a database connection from
`connections.properties`, the file that holds the committed HANA credentials:

| God-object | `main()` at | Connection literal |
|---|---|---|
| `ConsultarCustodiaPortafolio` | L3951 | `getPropertiesConnection("master02", "connections.properties")` |
| `CrearComprobante` (Facturacion) | L5138 | `getPropertiesConnection("desa2", "connections.properties")` |
| `ModificarSocioNegocio` | L3076 | `getPropertiesConnection("desa01", "connections.properties")` |
| `ProcesarFacturacionCustodia` | L5348 | — |
| `CrearClienteFullWS` | L3389 | — |

`RULE_REVIEWS.json` marks **RULE-076 and RULE-077 both `wrong`**, so neither is pinned as an
equivalence oracle. The reviewer's notes are the specification instead:

- RULE-076 — *"Credentials must be rotated immediately (treat as compromised) and the `main()` method
  removed from source."*
- RULE-077 — *"production credentials must not use Static3DES. Rotate and replace with AES-256 or a
  managed-key scheme."*

So: no `main()` is ported, and no credential becomes a test fixture. Config comes from the
environment, as `BcsCustodiaServicios/src/main/resources/application.yml` already does
(`${HANA_URL}`, `${HANA_USER}`, `${HANA_PASSWORD}`). Credential rotation itself is a day-zero human
action per `MODERNIZATION_BRIEF.md:452` and is not closed by this document.

## A.5 Risks

1. **Positional `BigDecimal` transposition.** The pure calculators take up to 5 same-typed
   positional arguments. Records plus characterization tests written *before* the move are the
   mitigation; this is the main reason calculators go first.
2. **Rounding drift.** Weighted averages and market-difference maths are rounding-sensitive. Pin
   `MathContext`/scale explicitly and give equivalence cases a `tolerance` with a stated `why`
   rather than a mask.
3. **`HIS` pairs are not duplication.** Roughly half the SQL resources exist in current and
   historical variants. Collapsing them would silently change which table is read.
4. **Sentinel dates.** `0001-01-01` and `2300-12-31` carry meaning. Normalising them to `null` or
   `Optional` changes behaviour.
5. **`TransformarAHash` duplicate-key semantics.** Legacy keeps the last duplicate. A `Collectors.toMap`
   without a merge function throws instead — a behavioural change disguised as a cleanup.
6. **DCV Settlement and SII DTE remain untestable.** Both sandboxes are still unconfirmed
   (`MODERNIZATION_BRIEF.md:232-233`), so the custody-settlement and DTE paths cannot be proven in
   this phase regardless of how well the decomposition goes.

## A.6 Questions for the SME

These need answers before implementation, not after:

1. `ObtenerUltimoDiaHabil` is duplicated in `ConsultarCustodiaPortafolio`. Is the local copy
   intentionally different from the platform business calendar, or is consolidating onto
   `ObtenerDiasHabilesAnterioresService` safe?
2. `TransformarAHash` silently keeps the last duplicate key. Intended, or latent?
3. Are the `0001-01-01` / `2300-12-31` sentinels contractual for any external consumer?
4. For the RV-only branch at L3390, should non-RV instruments keep using the stored price in all cases?
5. `BcsSocioNegocioServicios` is 558 legacy files against 10 modernized. Is the intended Phase 4
   scope the whole module, or a named subset?

---

# Part B — RULE-018 fix design

**CLI entity with `BLO` status can log in.** P0. Source:
`BcsPlataformaWeb/src/cl/bcs/plataforma/services/CrearSesion.java:337`.
Reviewer verdict: **`wrong`** — *"This is a bug — BLO customers should not be able to log in. The
condition should be `!IdEstado.equals("ACT")` like the EMP and PER paths."*

> **Citation correction.** `BUSINESS_RULES.md` cites L331 for the defect and L308/L318 for the EMP
> and PER paths. Verified 2026-10-10, the actual lines are **L337** (CLI defect), **L314** (EMP) and
> **L324** (PER) — the rule card is off by roughly six lines. L331 is the CLI entity-type dispatch,
> not the status check. Use the verified lines.

### Legacy symptom

```java
// CrearSesion.java:337 — the CLI path (verified verbatim)
if (lst.isEmpty() || lst.get(0).IdCliente == null || !lst.get(0).IdEstado.equals("ACT") && !lst.get(0).IdEstado.equals("BLO")) {
```

### Root cause

`&&` binds tighter than `||`, so the final clause evaluates as
`(!ACT && !BLO)`. For `IdEstado = "BLO"` that is `(true && false)` = `false`, the whole guard is
`false`, and **login succeeds for a blocked client.** The EMP path (L314) and PER path (L324) both
use the correct single check, so this is an outlier, not a convention.

### Modernized fix design

The entity-status rule is identical for all three entity types, so it becomes one domain policy
rather than three inline conditions:

```java
// domain/EntidadEstado.java — BcsPlataformaWeb
public enum EntidadEstado {
    ACT, BLO, ELI, PEN;

    /** RULE-018: only ACT may authenticate — for EMP, PER and CLI alike. */
    public boolean puedeAutenticar() { return this == ACT; }
}
```

```java
// service/CrearSesionService.java
private void validarEntidad(Entidad e) {
    if (e == null || e.idCliente() == null || !e.estado().puedeAutenticar()) {
        throw new AutenticacionException("Entidad no habilitada para autenticar");
    }
}
```

The CLI, EMP and PER paths all call `validarEntidad`. The `&& !BLO` clause is not carried over.

**Fail-closed on unknown values.** The legacy treats an unrecognised `IdEstado` as permitted. Parsing
into an enum makes an unknown value a rejection instead. That is a deliberate deviation from legacy
behaviour, justified by the reviewer's note, and must be recorded in `TRANSFORMATION_NOTES.md` as
such rather than presented as equivalence.

### Tests

| Test | Input | Expected |
|---|---|---|
| `rule018_cliBloqueado_noPuedeIngresar` | CLI, `IdEstado=BLO` | rejected — **fails on legacy code, by design** |
| `rule018_cliActivo_puedeIngresar` | CLI, `IdEstado=ACT` | accepted |
| `rule018_empBloqueado_noPuedeIngresar` | EMP, `IdEstado=BLO` | rejected (legacy parity) |
| `rule018_perBloqueado_noPuedeIngresar` | PER, `IdEstado=BLO` | rejected (legacy parity) |
| `rule018_estadoDesconocido_noPuedeIngresar` | CLI, `IdEstado=XXX` | rejected — deliberate deviation |

---

# Part C — RULE-052 fix design

**CallBatch LIFO job queue; jobs discarded on shutdown.** P0. Source:
`BcsPlataformaWeb/src/cl/bcs/plataforma/services/CallBatch.java:46,125-198`.
Reviewer verdict: **`wrong`** — *"Defect — batch jobs should be processed in FIFO order. LIFO could
cause financial sequencing issues."*

### Legacy symptom

Jobs are held in a `java.util.Stack` and drained by a background thread (outer period 10 s, inner
1 s sleep per empty check), so the **last** job submitted runs **first**. On
`contextDestroyed()` the remaining jobs are logged `"WARNING: No se va ejecutar"` and dropped.

Two defects: wrong order, and no durability.

### Root cause

`Stack` was most likely chosen for its convenient `push`/`pop` rather than for LIFO semantics. The
in-memory queue has no persistence, so a redeploy silently discards queued financial work.

### Modernized fix design

Ordering and durability are separate concerns and get separate fixes.

**C.1 — FIFO.** Replace `Stack` with a bounded `BlockingQueue`, and replace the sleep-poll loop with
a blocking take:

```java
// service/CallBatchQueue.java
// RULE-052: FIFO, not the legacy Stack's LIFO.
private final BlockingQueue<BatchJob> queue = new LinkedBlockingQueue<>(capacity);

public void submit(BatchJob job) {
    if (!queue.offer(job)) {
        throw new ResponseStatusException(TOO_MANY_REQUESTS, "Cola de batch llena");
    }
}
```

**C.2 — Durability.** Persist on submit and drive the queue from the table, so a restart resumes
rather than discards:

```sql
-- BAT.TrabajoBatch: PEN -> EJE -> (FIN | ERR)
-- Claimed with the same atomic single-row UPDATE pattern adopted for RULE-057,
-- so two nodes cannot claim one job.
```

**C.3 — Graceful shutdown.** Stop accepting, drain in flight, leave the rest `PEN` for the next start:

```java
@PreDestroy
public void shutdown() throws InterruptedException {
    accepting = false;
    executor.shutdown();
    if (!executor.awaitTermination(drainTimeout, SECONDS)) {
        log.warn("Batch drain timed out; {} job(s) remain PEN and will resume on restart",
                 queue.size());
        executor.shutdownNow();
    }
}
```

Nothing is dropped silently: a job is either `FIN`, `ERR`, or still `PEN` and will run.

**Reuse.** C.2's claim pattern is the one already designed for RULE-057 in
`PHASE3_FIX_DESIGNS.md`. Implement it once as a shared component rather than twice.

### Tests

| Test | Input | Expected |
|---|---|---|
| `rule052_ordenFifo` | submit A, B, C | executed A, B, C — **fails against legacy LIFO, by design** |
| `rule052_trabajosSobrevivenReinicio` | submit, restart before drain | job still `PEN`, then runs |
| `rule052_apagadoNoDescartaTrabajos` | submit 5, shut down | none lost; each `FIN` or `PEN` |
| `rule052_colaLlenaRechaza` | submit beyond capacity | HTTP 429, no silent drop |
| `rule052_dosNodosNoDuplican` | two workers, one job | executed exactly once |

Both RULE-018 and RULE-052 fixes intentionally diverge from legacy behaviour on the reviewer's
instruction. Their tests must therefore be labelled deliberate deviations, not equivalence
cases — otherwise `modernize-verify` will read a corrected defect as a regression.

---

# Part D — Phase 4 entry criteria status

| Criterion (`MODERNIZATION_BRIEF.md:229-236`) | State |
|---|---|
| Phase 3 exit criteria all checked — VERIFICATION.md signed | ⚠️ **ticked in the brief, not evidenced on disk** — see below |
| God-object decomposition plan written and SME-reviewed | 📝 **written here; SME review pending** |
| DCV Settlement test sandbox confirmed available | ❌ open — human/infra |
| SII DTE test environment confirmed available | ❌ open — human/infra |
| Crystal Reports migration option chosen | ✅ decided 2026-10-08 (Java 11 sidecar) |
| RULE-018 fix design confirmed | 📝 **designed in Part B; confirmation pending** |
| RULE-052 fix design confirmed | 📝 **designed in Part C; confirmation pending** |

### On the Phase 3 signature

The brief records *"VERIFICATION.md signed by Mauricio Villagran Mora (Architect) 2026-10-08,
decision: accept with conditions."* The current `VERIFICATION.md` contains that name **zero** times
and its sign-off block is blank.

`proof_pack.py` rewrites the sign-off block blank on every run, so any signature is lost each time
verification is regenerated — and it was regenerated on 2026-10-10. The signature must be
re-applied by a person; it is not something this document or any agent may tick. Consider recording
sign-offs outside `VERIFICATION.md`, since that file is machine-generated.

### Also outstanding

The brief's approval block reads **"Approval covers: Phase 1 only"** (`MODERNIZATION_BRIEF.md:447`).
Phase 4 has no approval yet, independent of its entry criteria.

---

## SME review

Left blank deliberately — a person fills this in.

| | |
|---|---|
| Reviewed by | ________________ |
| Role | ________________ |
| Date | __________ |
| Decomposition plan (Part A) | approve / approve with changes / reject |
| RULE-018 design (Part B) | approve / approve with changes / reject |
| RULE-052 design (Part C) | approve / approve with changes / reject |
| Answers to A.6 | ________________ |
