# BAT.* real DDL (columns used by job pattern H4)

Delivered by the DBA from HANA QA with a read-only query on 2026-10-21 (`Result_2.csv`: tables; `Result_3.csv`: view). Only name, type, length and scale are known: primary keys, NOT NULL and defaults were NOT part of the result.

### `BSCL.Plataforma.BD::BAT.Agenda`

Source: HANA QA, read-only query, 2026-10-21 (Result_2.csv).

| # | Column | HANA type | Length | Scale |
|---|---|---|---|---|
| 1 | IdAgenda | BIGINT | 19 | 0 |
| 2 | IdTipoPrograma | NVARCHAR | 40 |  |
| 3 | EsDemonio | NVARCHAR | 1 |  |
| 4 | DiaDeMes | NVARCHAR | 2 |  |
| 5 | DiaDeSemana | NVARCHAR | 1 |  |
| 6 | Mes | NVARCHAR | 2 |  |
| 7 | Hora | NVARCHAR | 2 |  |
| 8 | Minuto | NVARCHAR | 2 |  |
| 9 | SoloDiasHabiles | NVARCHAR | 1 |  |
| 10 | SoloPrincipioMes | NVARCHAR | 1 |  |
| 11 | SoloFinDeMes | NVARCHAR | 1 |  |
| 12 | habilitado | NVARCHAR | 1 |  |
| 13 | SiguienteFechaDeEjecucion | SECONDDATE | 19 | 0 |
| 14 | UltimaFechaDeEjecucion | SECONDDATE | 19 | 0 |
| 15 | EsProcesoManual | CHAR | 1 |  |

### `BSCL.Plataforma.BD::BAT.LogPrograma`

Source: HANA QA, read-only query, 2026-10-21 (Result_2.csv).

| # | Column | HANA type | Length | Scale |
|---|---|---|---|---|
| 1 | FechaLog | TIMESTAMP | 27 | 7 |
| 2 | IdTipoPrograma | NVARCHAR | 100 |  |
| 3 | IdPrograma | BIGINT | 19 | 0 |
| 4 | Mensaje | NVARCHAR | 5000 |  |

### `BSCL.Plataforma.BD::BAT.ParametrosPrograma`

Source: HANA QA, read-only query, 2026-10-21 (Result_2.csv).

| # | Column | HANA type | Length | Scale |
|---|---|---|---|---|
| 1 | IdSecuencia | BIGINT | 19 | 0 |
| 2 | IdGrupoParametro | BIGINT | 19 | 0 |
| 3 | IdPrograma | BIGINT | 19 | 0 |
| 4 | Orden | INTEGER | 10 | 0 |
| 5 | NombreParametro | NVARCHAR | 500 |  |
| 6 | Valor | NVARCHAR | 5000 |  |
| 7 | ValorBlob | BLOB | 2147483647 |  |

### `BSCL.Plataforma.BD::BAT.Programa`

Source: HANA QA, read-only query, 2026-10-21 (Result_2.csv).

| # | Column | HANA type | Length | Scale |
|---|---|---|---|---|
| 1 | IdPrograma | BIGINT | 19 | 0 |
| 2 | IdGrupoParametro | BIGINT | 19 | 0 |
| 3 | IdAgenda | BIGINT | 19 | 0 |
| 4 | IdTipoPrograma | NVARCHAR | 40 |  |
| 5 | Intento | INTEGER | 10 | 0 |
| 6 | IdUsuario | NVARCHAR | 40 |  |
| 7 | idEstado | NVARCHAR | 3 |  |
| 8 | Resultado | NVARCHAR | 2000 |  |
| 9 | fechaCreacion | SECONDDATE | 19 | 0 |
| 10 | fechaEjecucion | SECONDDATE | 19 | 0 |
| 11 | fechaFin | SECONDDATE | 19 | 0 |
| 12 | fechaSistema | DATE | 10 |  |
| 13 | Nodo | NVARCHAR | 1 |  |
| 14 | Bloqueo | NVARCHAR | 1 |  |
| 15 | ZonaHoraria | BIGINT | 19 | 0 |
| 16 | IdProgramaReprocesado | BIGINT | 19 | 0 |
| 17 | RutaSalida | NVARCHAR | 100 |  |
| 18 | RutaLog | NVARCHAR | 100 |  |
| 19 | Comentario | NVARCHAR | 3000 |  |

### `BSCL.Plataforma.BD::BAT.TipoPrograma`

Source: HANA QA, read-only query, 2026-10-21 (Result_2.csv).

| # | Column | HANA type | Length | Scale |
|---|---|---|---|---|
| 1 | IdTipoPrograma | NVARCHAR | 40 |  |
| 2 | Descripcion | NVARCHAR | 80 |  |
| 3 | MaximoIntentos | INTEGER | 10 | 0 |
| 4 | Modulo | NVARCHAR | 40 |  |
| 5 | NombreClasePrograma | NVARCHAR | 200 |  |
| 6 | TipoClasePrograma | NVARCHAR | 40 |  |
| 7 | Nodo | NVARCHAR | 1 |  |
| 8 | EsPauta | NVARCHAR | 1 |  |
| 9 | TipoSalida | NVARCHAR | 1 |  |
| 10 | FormatoSalida | NVARCHAR | 10 |  |
| 11 | TieneLog | NVARCHAR | 1 |  |

### `BSCL.Plataforma.BD.Vistas::BAT.ConsultarParametrosProgramas`

Source: HANA QA, read-only query, 2026-10-21 (Result_3.csv).

| # | Column | HANA type | Length | Scale |
|---|---|---|---|---|
| 1 | IdSecuencia | BIGINT | 19 | 0 |
| 2 | IdGrupoParametro | BIGINT | 19 | 0 |
| 3 | IdPrograma | BIGINT | 19 | 0 |
| 4 | Orden | INTEGER | 10 | 0 |
| 5 | NombreParametro | NVARCHAR | 500 |  |
| 6 | Valor | NVARCHAR | 5000 |  |
| 7 | ValorBlob | BLOB | 2147483647 |  |
| 8 | idEstado | NVARCHAR | 3 |  |
| 9 | IdTipoPrograma | NVARCHAR | 40 |  |
| 10 | fechaEjecucion | SECONDDATE | 19 | 0 |

## Inferred before vs confirmed now

| Item | Before (inferred in the H4 test schema) | Now (real DDL) |
|---|---|---|
| `IdTipoPrograma` in Programa, TipoPrograma, Agenda | BIGINT; the code read it as a number (`getLong`) | NVARCHAR(40); String in the code and tests (it would have failed on HANA) |
| `LogPrograma` | had an `IdLog` auto-increment column; `IdTipoPrograma` BIGINT; `Mensaje` 2000 | no `IdLog`; `FechaLog` TIMESTAMP, `IdTipoPrograma` NVARCHAR(100), `Mensaje` NVARCHAR(5000) |
| `Programa.IdUsuario` | BIGINT | NVARCHAR(40) |
| `Programa` dates | TIMESTAMP, `fechaSistema` TIMESTAMP | `fechaCreacion/fechaEjecucion/fechaFin` SECONDDATE (TIMESTAMP(0) in H2), `fechaSistema` DATE |
| `Programa` columns | no `ZonaHoraria`, `RutaLog`, `Comentario` | all three exist (BIGINT, NVARCHAR(100), NVARCHAR(3000)); `RutaSalida` is NVARCHAR(100) (H2 oracle tests use 500/1000, untouched) |
| `Programa.Nodo`, `Bloqueo`, `Intento`, `IdProgramaReprocesado` | inferred from legacy Java | CONFIRMED to exist: NVARCHAR(1), NVARCHAR(1), INTEGER, BIGINT |
| `TipoPrograma` | 7 columns (Modulo 50, NombreClasePrograma 500, TipoClasePrograma 50) | 11 columns: adds Descripcion, TipoSalida, FormatoSalida, TieneLog; lengths Modulo 40, NombreClasePrograma 200, TipoClasePrograma 40 |
| `Agenda` | 6 columns, `Minuto` INT | 15 columns; `Minuto`, `Hora`, `DiaDeMes`, `Mes` are NVARCHAR(2), `EsProcesoManual` CHAR(1), dates SECONDDATE |
| `ParametrosPrograma` | `NombreParametro` 100, `Valor` 3000 | 500 and 5000; `ValorBlob` BLOB CONFIRMED |
| View `ConsultarParametrosProgramas` | columns unknown (taken from the repository SQL) | exactly 10 columns (table above); the repository query uses only NombreParametro, Valor, ValorBlob and IdPrograma: all exist |
| `Resultado` NVARCHAR(2000) | assumed (truncation to 1999) | CONFIRMED, truncation unchanged |

## Still unknown (not in the DBA result)

* Primary keys, NOT NULL and DEFAULT values (`Bloqueo` 'N', `Intento` 0, `IdAgenda` 0, `Nodo`): the H2 test schema keeps them as assumptions for the fixtures.
* The definition (join) of the view: the H2 view is a stand-in that exposes exactly the 10 real columns.
* Whether `BAT.Agenda` / `TipoPrograma` PK is only IdAgenda / IdTipoPrograma.
