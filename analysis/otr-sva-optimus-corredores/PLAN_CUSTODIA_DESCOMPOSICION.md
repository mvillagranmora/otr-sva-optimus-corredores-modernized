# Decomposition plan — `cl.bcs.custodia.ConsultarCustodiaPortafolio` (god-object, Phase 4 entry criterion)

Written 2026-10-25 for the Phase 4 entry criterion "God-object decomposition plan written and SME-reviewed". **Status: written, NOT yet SME-reviewed.**
A person (SME) reviews it and records the review at the end; the transformation of this class does not start before that.

Source: `legacy/otr-sva-optimus-corredores/BSCL/Custodia/JAVA/BcsCustodiaServicios/src/cl/bcs/custodia/ConsultarCustodiaPortafolio.java`
(4 001 lines; 74 SQL resources; `logica` :1295-3657, about 2 363 lines). Usage: 86 632 calls (the most used class of Custodia, 80 % of the
module's traffic; `PRIORIDAD_POR_USO.md`). Sibling with the same shape: `ConsultarCustodiaPortafolioWS` (2 799 lines),
`ConsultarCustodiaPortafolioAgente` (11 962 calls).

## 1. What the method does today (structure found)

| Lines | Block | Notes |
|---|---|---|
| :51 | 15 search types `CUS_NEG, CUS, VENPAC, COMPAC, SIM, PRT_VC_CON_CUS, PRT_VC_SIN_CUS, PRT_AD_FN, EMP, EMP_EXT, GTA, TRA, TRANOM, REP, SUB` | the request asks for a subset |
| :59-484 | SQL resources and per-type field lists ("OPTIMIZADOS", per position type) | 74 files |
| :1311-1394 | defaults (system date), related parties of the broker, own-account desk data | shared context |
| :1395-2490 | ONE loop over the search types, one `if (Obj.equals(...))` block per type: CUS_NEG :1430, CUS :1462 (with cash dividends :1638 and coupon cuts / lottery :1741), VENPAC :1819, COMPAC :1853, SIM :1889 (+ underlyings :1937), PRT :1969, EMP :2072, EMP_EXT :2105, GTA :2138, TRA :2175 (uncovered sale split :2242), TRANOM :2256, REP :2441 | each block reads current or historic tables ("OPTIMIZACION" sub-branches) |
| :2491-2728 | hash indexes: distinct portfolios, clients, risk factors, instruments in custody, matrix / product groupers, custody accounts | work tables with IDTRABAJO (:2520, :2682) |
| :2725-3274 | per-position assembly: custody, transit in/out, nominal purchases/sales, nominal pacts, presumed, nominal simultaneous, reports | |
| :3275-3304 | valuation input | calls valuation |
| :3305-3657 | output table: total custody, market amount, available custody, currency checks | |
| :3658-3937 | helpers: market difference, currency validation, to-hash, result processing / grouping (:3775), last business day (:3882), weighted average price (:3918) | pure or nearly pure |

## 2. Proposed target structure (Spring Boot 4.1.1, same jar `bcs-custodia-servicios`, library first, REST adapter second)

| Target | Responsibility | From |
|---|---|---|
| `portafolio.ConsultaCustodiaPortafolioService` | orchestration only: context, chosen search types, assembly, output | :1295-1394, :3305-3657 |
| `portafolio.contexto.ContextoConsulta` (record) + `ContextoConsultaReader` | system date, go-live date, broker related parties, own-account desk | :1311-1394, :1419 |
| `portafolio.busqueda.BusquedaPosicion` (interface) + one implementation per search type (15) | each reads its rows (current / historic) and returns typed positions | the 13 `if` blocks :1430-2490 |
| `portafolio.busqueda.CustodiaBusqueda` (CUS) split further: `DividendosEnDinero`, `CortesCuponSorteoLetras` | the largest block | :1462-1818 |
| `portafolio.indices.IndicesConsulta` | the hash indexes and work tables (one owner of IDTRABAJO) | :2491-2728 |
| `portafolio.ensamble.EnsamblePosiciones` | per-position assembly (transit, nominal, pacts, presumed, reports) | :2725-3274 |
| `portafolio.calculo.CalculosCustodia` (pure) | market difference, PPP, currency check, grouping | :3658-3937 |
| `portafolio.valorizacion.ValorizacionPort` | port to `ValorizarInstrumento` (its own slice) | :3275-3304 |
| `adapter.out.jdbc.*` | the 74 SQL files, copied byte for byte and SHA-256 pinned (as FondoMutuo) | resources |
| `adapter.in.javacall` | JavaCall facade, same JSON contract (key order, BigDecimal scale) | :3939 |

Rules for the split: no change of the order of checks or of output rows inside a block; shared mutable state of `logica` becomes the per-call
`ContextoConsulta` / `IndicesConsulta` (no static state); every block is characterised BEFORE it is moved (oracle goldens per search type).

## 3. Order of work (each step its own verify)

1. Characterise the pure helpers (:3658-3937) and the context (:1311-1394).
2. One search type per step, by usage of the type in production (to be measured in QA: the request's search-type list per call) — proposal:
   CUS, then TRA/TRANOM, SIM, PRT, EMP/EMP_EXT, GTA, pacts, REP, CUS_NEG, SUB.
3. Assembly and output, once every needed type exists.
4. `ConsultarCustodiaPortafolioAgente` and `...WS` reuse the same services (they differ in input / output shape: to be confirmed).

## 4. Proof

The same oracle approach as `ConsultarFlujoCustodia` and FondoMutuo: the unmodified legacy class compiled from `legacy/` into scratch and run on H2
MODE=LEGACY with recorded doubles; goldens per search type; fresh inputs per type; canary per step. Limits to declare: H2 not HANA, ASSUMED
column types where no catalog export exists, the valuation as a double until its own slice.

## 5. Questions for the SME

- Which search types are really used together, and which are dead (e.g. `PRT_AD_FN`, `SUB`)?
- `ConsultarCustodiaPortafolioAgente` / `WS`: same rules, different shape — or different rules?
- Acceptable to compare totals when the legacy row order is `HashMap` iteration (as in `ConsultarFlujoCustodia`, D-CUS-05)?

## SME review

| Name | Role | Date | Decision (approve / approve with changes / reject) | Notes |
|---|---|---|---|---|
| | | | | |
