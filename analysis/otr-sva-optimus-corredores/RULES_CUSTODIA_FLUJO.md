# Rules — `cl.bcs.custodia.ConsultarFlujoCustodia` (BcsCustodiaServicios, first slice)

Extracted 2026-10-25 from `legacy/otr-sva-optimus-corredores/BSCL/Custodia/JAVA/BcsCustodiaServicios/src/cl/bcs/custodia/ConsultarFlujoCustodia.java`
(2 612 lines; `logica` :937-2122, about 1 190 lines) and its 29 SQL files under `src/cl/bcs/custodia/sql/FlujoCustodia_*.sql`. All line numbers below
refer to that file unless another file is named. Cards are `CUS-FLU-nn`; defects kept or to decide are `D-CUS-nn`; questions for the SME are `Q-CUS-nn`.
`BUSINESS_RULES.md` has no card for Custodia (the module answers only for RULE-076 / RULE-077): these cards are the oracle for this slice.

Usage (PRIORIDAD_POR_USO.md): 2 731 calls through the gateway; 18 in-process callers in 7 modules (Custodia x10, FondoMutuo x4, Facturacion,
Orden, Plataforma, SocioNegocio). FondoMutuo's gate G-CUS stays closed until this service is served in process.

## 1. Entry and transport

| Card | Given / When / Then | Source |
|---|---|---|
| CUS-FLU-01 | Given a JSON request, when `call` runs, then it is parsed with the module Gson into `FlujoCustodiaEntrada`, `logica` runs, `P_OUT_ERRORESCONTADOR` is recomputed from the messages and the output is re-serialised with the same Gson | :916-924 |
| CUS-FLU-02 | Given `p_Uso` empty, then the output carries one ERROR message `SIN_INFORMAR_USO_FLUJO_CUSTODIA` (id 0) and nothing else is read or written | :953-962 |
| CUS-FLU-03 | Given `p_Uso` null, then `NullPointerException` (no message, HTTP 500) | :953 (D-CUS-01) |
| CUS-FLU-04 | Defaults of the input: `p_DiaHabil = "S"`; `p_IdPais = -1` means "the company's country" (`ConsultarEmpresa.IdPais`) | :105-107, :975 |

## 2. Dates

| Card | Given / When / Then | Source |
|---|---|---|
| CUS-FLU-05 | The system date is `PLA.FechaSistema` (module PLATAFORMA); `CUS.AgregarSimultaneasEventoCapitalCli` (module CUSTODIA) switches the SUBYACENTE read | :964-965 |
| CUS-FLU-06 | The projected dates are `p_FechaSaldo` plus, for i = 1..`p_DiasProyeccion`, `ObtenerDiaSiguiente(p_FechaSaldo, i, p_DiaHabil, country)`; duplicates are dropped keeping first-seen order; min and max are taken as strings (`yyyy-MM-dd`) | :983-1011 |
| CUS-FLU-07 | Every source has a "historic" variant: it is read when the MAXIMUM projected date is strictly before the system date (`compareTo == -1`), else the current table | :1241, :1260, :1280, :1306, :1346, :1380 |

## 3. Usage matrix (configuration matching)

| Card | Given / When / Then | Source |
|---|---|---|
| CUS-FLU-08 | `p_Uso` and `p_FiltrosBusqueda` are de-duplicated (`distinct`, by `equals`) and crossed (every use x every filter) | :1063-1077 |
| CUS-FLU-09 | Only configured uses (`FlujoCustodia_SelectUsoFlujoCustodia.sql`) whose `UsoTipoOperacion` was requested are kept | :1080 |
| CUS-FLU-10 | Similarity score per (requested, configured) pair: base 2 (1 when the requested operation type is ""); +2 per equal dimension, +1 when the requested dimension is empty / null / 0; dimensions: classification, instrument type, instrument, client, portfolio, specific search | :1096-1138 |
| CUS-FLU-11 | Per requested key (10 fields) the configured use with the HIGHEST score wins; on a tie the first one met wins (strict `<`) | :1139-1144 |
| CUS-FLU-12 | Each winner is expanded with every configured item (`FlujoCustodia_SelectItemsFlujoCustodia.sql`) of its use into the base table (`ItemCalculo`, `ItemOrigen`, `FuncionOperador`), de-duplicated by a 21-field key | :1162-1195 |
| CUS-FLU-13 | Item -> source map (`setItemCalculoHash`, 42 items -> 15 sources) decides which sources are read; an item outside the map reads nothing | :2218-2279 |
| CUS-FLU-14 | The winners are inserted in `CUS.FlujoCustodia_MatrizUso` (batch, `IdTrabajo` random) before the reads and DELETED in `finally`, also after an exception | :1218, :2113-2115 |

## 4. Sources (one read per source, rows tagged with ItemCalculo / ItemOrigen)

| Card | Source -> ItemCalculo | Source |
|---|---|---|
| CUS-FLU-15 | CARTERA -> `INFORMACION_CARTERA`; current: dynamic SQL built from the FIRST winner only (predicates for its non-null dimensions; `IdEmisor` compared with `rtrim`; maturity on instrument for IF/RF, on operation otherwise; `(1==1)` when none); paged by 100 000 above 500 000 rows | :1238-1257, :2128-2215 |
| CUS-FLU-16 | CARTERA_TRANSITO -> `CARTERA_TRANSITO_` + Accion | :1258-1276 |
| CUS-FLU-17 | TRANSITO -> `MOV_TRANSITO_RETIRO_VENTA_DESCUBIERTA` for uncovered-sale withdrawals, else `INFORMACION_TRANSITO_INGRESO` / `_RETIRO` / `INFORMACION_TRANSITO` | :1278-1303 |
| CUS-FLU-18 | GARANTIA -> `INFORMACION_GARANTIA` | :1304-1323 |
| CUS-FLU-19 | SUBYACENTE (only when `CUS.AgregarSimultaneasEventoCapitalCli = S`) -> SIMULTANEA/REPORTE/PRESTAMO/EMPRESTITO/PACTO by operation type; the operation-type list depends on the FIRST winner's `BusquedaEspecifica` | :1324-1376 |
| CUS-FLU-20 | CARTERA_RESERVADA -> RESERVA_COMPRA_PACTOS / RESERVA_VENTA_PACTO / RESERVA_COMPRA_TERMINO / RESERVA_VENTA_TERMINO | :1378-1403 |
| CUS-FLU-21 | ORDENES_POR_AUTORIZAR, ORDENES_INGRESADAS (with FechaMinima), ORDENES_REPORTE_*, ASIGNACIONES_*, MOVIMIENTOS_* -> prefix + Accion + suffix (no historic variant) | :1404-1496 |
| CUS-FLU-22 | EVENTOS_DE_CAPITAL -> SUBSCRIPCIONES / SORTEO_LETRAS / PREPAGO_BONO by Accion SU/SL/PB; `ItemOrigen = ItemCalculo` | :1497-1513 |
| CUS-FLU-23 | FONDOS_MUTUOS -> APORTE_/RESCATE_FONDOS_MUTUOS by APORFM/RESCFM; `ItemOrigen = "FONDOSMUTUOS"` | :1514-1531 |

## 5. Discard, projection, grouping

| Card | Given / When / Then | Source |
|---|---|---|
| CUS-FLU-24 | Every read row (TipoRegistro R): liquidation for the process = max(row liquidation, minimum projected date) (strictly after); own-account portfolio flag; operator from (use, item); quantity and rescue value times -1 when the operator is "-" | :1543-1560 |
| CUS-FLU-25 | Discard (TipoRegistro D), first match wins: no operator -> `CONFIGURACION_SIN_FUNCION_OPERADOR`; committed portfolio `CC` -> `SE_EXCLUYE_CARTERA_COMPROMETIDA`; no operation type and no specific search -> `CONFIGURACION_TIPO_OPERACION_EXCLUYE_INSTRUMENTO`; `SOLO_GARANTIZABLE` without a guarantee type (except `INGGPR`) -> `CONFIGURACION_GARANTIA_EXCLUYE_INSTRUMENTO` | :1565-1577 |
| CUS-FLU-26 | `SOLO_SUBYACENTE`: a row whose instrument is not an underlying is discarded `INSTRUMENTO_NO_SUBYACENTE` (only when at least one underlying was found) | :1593-1619 |
| CUS-FLU-27 | The detailed output (`p_out_FlujoDetallado`) is every R row, sorted by `IdRegistro` | :1631-1635, :1658-1663 |
| CUS-FLU-28 | Projection: each R row is copied (TipoRegistro P) to every projected date different from its process liquidation; on a date before the liquidation its quantities, prices and rates are 0 | :1636-1656 |
| CUS-FLU-29 | Grouping per (base row, projected date): the rows of that date / use / origin / item that match the base row's non-null dimensions; amounts only when the row's item is the base row's item; a base row with no match yields an empty line | :1691-1848 |
| CUS-FLU-30 | Counters per line (CantidadItem, SItemCalculo, SItemOrigen, SREG) decide `ConsiderarRegistro` (4 branches) | :1865-1909 |
| CUS-FLU-31 | Totalised output (`p_out_FlujoTotalizado`): one line per 31-field key for every `p_EsAgrupadoPor` entry (a dimension is kept only when its flag is "S"); only lines considered "S"; sums of quantity, rescue value and the two market amounts; `IdProcesamientoInterno` 1..n in iteration order; `Dias` from the projected date | :1864-2018 |
| CUS-FLU-32 | With no `p_EsAgrupadoPor` the totalised output is EMPTY (the loop has nothing to iterate) | :1872 |

## 6. Valuation (`p_ValorizarFlujo = "S"`)

| Card | Given / When / Then | Source |
|---|---|---|
| CUS-FLU-33 | Lines with quantity 0, or IF with rescue value 0, are not valued; the others go to `ValorizarInstrumento` with `OrigenProcesamiento = "FLUJOCUSTODIA"` and the use's `UsoValorizacion` as source table | :2027-2075 |
| CUS-FLU-34 | Results with `DimensionTasa = MERC` overwrite price, rate and the two market amounts of the line with the same internal id | :2087-2102 |
| CUS-FLU-35 | A valuation with errors sets `P_OUT_ERRORESCONTADOR = 1` and REPLACES the messages with the valuation's, but the output is still returned (and then `call` recomputes the counter from those messages) | :2081-2085, :920 |
| CUS-FLU-36 | The flow key `p_out_KeyFlujoCustodia` comes from sequence `FlujoCustodia`; the projected rows draw `rows x dates` values of `DatosFlujoCustodia` | :980-981, :1624 |

## 7. Defects and oddities (kept bug for bug unless a person decides)

| Id | What | Source | Proposal |
|---|---|---|---|
| D-CUS-01 | `p_Uso` null -> NPE | :953 | keep |
| D-CUS-02 | Similarity: `ufc.IdInstrumento.equals(...)` (also client, portfolio) throws NPE when the configured value is null and the requested one is not | :1114, :1120, :1126 | keep (pin with a case) |
| D-CUS-03 | SUBYACENTE `break` is inside the `if`: when `CUS.AgregarSimultaneasEventoCapitalCli` is not "S" the switch FALLS THROUGH into CARTERA_RESERVADA and reads the reserved portfolio | :1345-1378 | keep; Q-CUS-01 |
| D-CUS-04 | The current CARTERA query and the SUBYACENTE operation list use only the FIRST winner (`matrizUso.get(0)`) although several uses can be requested; `get(0)` of a HashMap iteration | :1248, :1328 | keep; Q-CUS-02 |
| D-CUS-05 | Order of sources, of base rows, of `IdDatosFlujoCustodia` and of the totalised lines (`IdProcesamientoInterno`) follows `HashMap` / `HashSet` iteration of String keys | :1162, :1236, :1965 | keep the same iteration (same key strings in a `java.util.HashMap`) or a person approves a sorted order |
| D-CUS-06 | Grouping runs in `parallelStream` and updates the counters with non-atomic get+put on `ConcurrentHashMap` and a plain `HashMap` (`instrumentos`): counts and the instrument kept for valuation may vary between runs | :1704-1785 | new code is sequential (as DEV-4A-5 of FondoMutuo); equivalence on cases where the race cannot change the result |
| D-CUS-07 | `CARTERA_VENTA_TRANSITO_RPA` is mapped but has no case: never read (`FlujoCustodia_SelectCarterasVentaTransito.sql`, 193 lines, is dead) | :2223, switch :1237 | keep; not migrated |
| D-CUS-08 | `PrecioMercadoProceso` / `TasaMercadoProceso` of a totalised line are never set: the 31-field key always ends in `null#null#` | :1945-1946 | keep |
| D-CUS-09 | `idTrabajo = Math.random()*1e10`: two concurrent calls can collide in `FlujoCustodia_MatrizUso` | :943 | keep the random id; Q-CUS-03 |
| D-CUS-10 | Valuation errors replace (not add to) the messages | :2083 | keep |

## 8. Questions for the SME

- Q-CUS-01: D-CUS-03 — is reading the reserved portfolio when simultaneous trades are switched off intended?
- Q-CUS-02: D-CUS-04 — with several uses, should the CARTERA predicates come from each use, not from an arbitrary first one?
- Q-CUS-03: D-CUS-09 — may the work id come from a sequence instead of `Math.random()`?
- Q-CUS-04: the 18 in-process callers: which of them must be served in process first (FondoMutuo G-CUS is the plan's candidate)?

## 9. Out of this slice

`ValorizarInstrumento` (3 172 lines) is a collaborator port in this slice: the oracle and the new code answer it from the same scripted double and the
request built for it is compared (its own migration is a later slice). `ObtenerDiaSiguiente`, `ConsultarEmpresa`, `ConfiguracionSistema`,
`Utils.getSecuencia` are Plataforma reads: the oracle runs the real legacy classes on H2; the new code has JDBC adapters with the same SQL.
HANA column types: no catalog export exists for most of the 35 tables (QA unreachable on 2026-10-25): the H2 schema is ASSUMED from the SQL and the
pojo field types, and says so.
