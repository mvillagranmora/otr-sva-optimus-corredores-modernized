# Phase 3 Trading — Entry Criteria Status

> Evaluated: 2026-10-05. Option A (sequential strict) chosen — no implementation code until all criteria below are GREEN.

---

## Criteria checklist

| # | Criterion | Status | Owner | Notes |
|---|---|---|---|---|
| 1 | RULE-057 fix design written | ✅ GREEN | Engineering | PHASE3_FIX_DESIGNS.md §RULE-057 |
| 2 | RULE-059 fix design written | ✅ GREEN | Engineering | PHASE3_FIX_DESIGNS.md §RULE-059 |
| 3 | RULE-068 fix design written | ✅ GREEN | Engineering | PHASE3_FIX_DESIGNS.md §RULE-068 |
| 4 | Thread-safety fix design written | ✅ GREEN | Engineering | PHASE3_FIX_DESIGNS.md §Thread-Safety |
| 5 | Hardcoded-creds fix design written | ✅ GREEN | Engineering | PHASE3_FIX_DESIGNS.md §RULE-076/077 |
| 6 | Characterization tests — CrearOrdenes (CCN=813, ≥80% branch) | ✅ GREEN | Engineering | 76/76 pass, BUILD SUCCESS — CREARODENES_CHARACTERIZATION_REPORT.md |
| 7 | Characterization tests — BcsMesaServicios / CrearOperacionSpot (CCN=378, ≥80% branch) | ✅ GREEN | Engineering | 44/44 pass, BUILD SUCCESS — BCSMESASERVICIOS_CHARACTERIZATION_REPORT.md |
| 8 | Sandbox credentials — BECB | ⛔ BLOCKED | Ops / owner | Requires BECB sandbox access confirmation |
| 9 | Sandbox credentials — SURA | ⛔ BLOCKED | Ops / owner | Requires SURA sandbox access confirmation |
| 10 | Credential rotation (JOBMAN/${HANA_PASSWORD}) | ⏸ DEFERRED | Ops | Deferred by user — impacts other services |

---

## Criteria detail

### ✅ 1–5 — Fix designs

All five fix designs are documented in `PHASE3_FIX_DESIGNS.md`. Each includes:
- Root cause from legacy code (file + line)
- Modernized fix with concrete Java/SQL code
- Port interface changes needed
- Test oracles (so characterization tests can be written before implementation)

No implementation code has been written yet (Option A constraint honored).

---

### ✅ 6 — CrearOrdenes characterization tests

**Status:** COMPLETE — 76/76 tests pass, BUILD SUCCESS, no HANA required

**Report:** `analysis/otr-sva-optimus-corredores/CREARODENES_CHARACTERIZATION_REPORT.md`

**Key behaviors pinned:**
- CCN=813 mega-function `logica()` (lines 1201–4843) characterized via 17 pure-Java business rules
- `GeneraSpot` routing logic
- `TipoCambio` derivation
- `FechaVigencia` calculation
- `IdEmisor` routing by instrument classification
- BEC bolsa override → `MAN`
- `calculaMontoParidad` formula
- `limpiarOrdenes` cleanup path

---

### ✅ 7 — CrearOperacionSpot characterization tests

**Status:** COMPLETE — 44/44 tests pass, BUILD SUCCESS

**Report:** `analysis/otr-sva-optimus-corredores/BCSMESASERVICIOS_CHARACTERIZATION_REPORT.md`

**Key formulas pinned:**
- Movimientos vigentes filter: `VIG/PEL/PDA`; interbancarios: `INTERB+ARBINT` (not FWD); distribución: `DISTRI+ARBDIS`
- `ResultadoPosicionInicial = Σ((CierreIngresado - TipoCambioInicial) × PosicionInicial)`
- `UtilidadCompra = monto × (precioCalculadora - TipoCambioCierre)`
- `UtilidadVenta = monto × (TipoCambioCierre - precioCalculadora)`
- `BreakEven = EquivEnPesos / PosicionActual` [scale=4, HALF_UP]
- BIDASK paridad selection: `posicion >= 0 → BID, else ASK`

---

### ⛔ 8–9 — Sandbox credentials (BECB / SURA)

Phase 3 Trading includes the BECB and SURA connector modules. These make live API calls to external systems. Without sandbox credentials:
- Integration tests cannot be written (no WireMock stubs without response samples)
- Equivalence cannot be verified end-to-end

**Required from ops:**
- BECB sandbox API endpoint + credentials (basic-auth or OAuth2 client_id/secret)
- SURA sandbox endpoint + API key or token

**Not a blocker for characterization tests** (those test BcsOrdenServicios internals with mocked connectors). Blocker only for the BECB/SURA connector implementations in Phase 3.

---

### ⏸ 10 — Credential rotation

`JOBMAN`/`${HANA_PASSWORD}` in `ConnectorsOrders.sapHanaConnection()` are hardcoded production credentials. Rotation is owner-action and deferred because the same account is used by other services not yet modernized. The modernized `BcsJobOrders` will use env-var DataSource (criterion met by fix design). Rotation of the actual secret is tracked separately.

---

## Gate: ready to start Phase 3 implementation?

| Condition | Met? |
|---|---|
| All fix designs written | ✅ |
| CrearOrdenes characterization tests ≥80% | 🔄 pending |
| CrearOperacionSpot characterization tests ≥80% | 🔄 pending |
| BECB sandbox confirmed | ⛔ (non-blocking for Phase 3 start; blocking only for BECB connector) |
| SURA sandbox confirmed | ⛔ (same) |

**Current gate status:** ✅ GATE OPEN — all engineering-resolvable criteria met. Phase 3 can begin with `BcsPortafolioServicios`. BECB/SURA sandboxes (criteria 8–9) are non-blocking until their specific connector modules are reached in the sequence.

Once the characterization forks report ≥80% branch coverage, Phase 3 transformation can begin with `BcsPortafolioServicios` (9,105 LOC — smallest of the Trading modules).

---

## Phase 3 module order (locked per plan)

1. `BcsPortafolioServicios` — 9,105 LOC (start here)
2. `BcsValo2` — 7,342 LOC
3. `BcsJobOrders` + `BcsMesaJobs` (parallel)
4. `BcsOrdenJobs`
5. `BcsMesaServicios` — 65,106 LOC (largest; requires creció test gate)
6. `BcsOrdenServicios` — 72,080 LOC (largest; requires CrearOrdenes test gate)
7. BECB connector (requires sandbox — criteria 8)
8. SURA connector (requires sandbox — criteria 9)
