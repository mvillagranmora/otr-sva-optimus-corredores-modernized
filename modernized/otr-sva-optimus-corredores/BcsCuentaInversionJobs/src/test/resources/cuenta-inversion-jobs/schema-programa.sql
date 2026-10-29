-- H2 (MODE=LEGACY) schema of the BAT objects that the job pattern H4 reads and writes (ProgramaRepositorioJdbc, ProgramaStatusListener).
-- Loaded AFTER the oracle schema, ONLY by the H4 tests (support/ProgramaH2). It is a separate file ON PURPOSE: the recorded goldens dump
-- "BSCL.Plataforma.BD::BAT.Programa" with SELECT *, so adding columns to the table in the oracle schema would change every dump; the legacy
-- oracle and the golden tests keep using the oracle schema unchanged.
--
-- REAL DDL: column names, types and lengths below are the ones the DBA read from HANA QA (read-only query, 2026-10-21; Result_2.csv for the tables,
-- Result_3.csv for the view); see analysis/otr-sva-optimus-corredores/BAT_DDL_REAL.md. NVARCHAR is VARCHAR here, SECONDDATE is TIMESTAMP(0).
-- NOT in the DBA's result and therefore still assumptions: primary keys / NOT NULL, and the DEFAULTs (Bloqueo 'N', Intento 0, IdAgenda 0, Nodo ''),
-- which only serve the test fixtures. No database change of any kind is made by this module.
-- schema-jobs.sql ALREADY creates BAT.Programa (IdPrograma PK, RutaSalida, RutaLog, Comentario, all VARCHAR(1000)): narrowed/widened to the real lengths.
ALTER TABLE "BSCL.Plataforma.BD::BAT.Programa" ALTER COLUMN "RutaSalida" VARCHAR(100);
ALTER TABLE "BSCL.Plataforma.BD::BAT.Programa" ALTER COLUMN "RutaLog" VARCHAR(100);
ALTER TABLE "BSCL.Plataforma.BD::BAT.Programa" ALTER COLUMN "Comentario" VARCHAR(3000);
ALTER TABLE "BSCL.Plataforma.BD::BAT.Programa" ADD COLUMN "IdGrupoParametro" BIGINT;
ALTER TABLE "BSCL.Plataforma.BD::BAT.Programa" ADD COLUMN "IdAgenda" BIGINT DEFAULT 0;
ALTER TABLE "BSCL.Plataforma.BD::BAT.Programa" ADD COLUMN "IdTipoPrograma" VARCHAR(40);
ALTER TABLE "BSCL.Plataforma.BD::BAT.Programa" ADD COLUMN "Intento" INTEGER DEFAULT 0;
ALTER TABLE "BSCL.Plataforma.BD::BAT.Programa" ADD COLUMN "IdUsuario" VARCHAR(40);
ALTER TABLE "BSCL.Plataforma.BD::BAT.Programa" ADD COLUMN "idEstado" VARCHAR(3);
ALTER TABLE "BSCL.Plataforma.BD::BAT.Programa" ADD COLUMN "Resultado" VARCHAR(2000);
ALTER TABLE "BSCL.Plataforma.BD::BAT.Programa" ADD COLUMN "fechaCreacion" TIMESTAMP(0);
ALTER TABLE "BSCL.Plataforma.BD::BAT.Programa" ADD COLUMN "fechaEjecucion" TIMESTAMP(0);
ALTER TABLE "BSCL.Plataforma.BD::BAT.Programa" ADD COLUMN "fechaFin" TIMESTAMP(0);
ALTER TABLE "BSCL.Plataforma.BD::BAT.Programa" ADD COLUMN "fechaSistema" DATE;
ALTER TABLE "BSCL.Plataforma.BD::BAT.Programa" ADD COLUMN "Nodo" VARCHAR(1) DEFAULT '';
ALTER TABLE "BSCL.Plataforma.BD::BAT.Programa" ADD COLUMN "Bloqueo" VARCHAR(1) DEFAULT 'N';
ALTER TABLE "BSCL.Plataforma.BD::BAT.Programa" ADD COLUMN "ZonaHoraria" BIGINT;
ALTER TABLE "BSCL.Plataforma.BD::BAT.Programa" ADD COLUMN "IdProgramaReprocesado" BIGINT;

CREATE TABLE "BSCL.Plataforma.BD::BAT.TipoPrograma" ("IdTipoPrograma" VARCHAR(40) NOT NULL PRIMARY KEY, "Descripcion" VARCHAR(80),
  "MaximoIntentos" INTEGER DEFAULT 0, "Modulo" VARCHAR(40), "NombreClasePrograma" VARCHAR(200), "TipoClasePrograma" VARCHAR(40),
  "Nodo" VARCHAR(1) DEFAULT '', "EsPauta" VARCHAR(1), "TipoSalida" VARCHAR(1), "FormatoSalida" VARCHAR(10), "TieneLog" VARCHAR(1));
CREATE TABLE "BSCL.Plataforma.BD::BAT.Agenda" ("IdAgenda" BIGINT NOT NULL PRIMARY KEY, "IdTipoPrograma" VARCHAR(40), "EsDemonio" VARCHAR(1),
  "DiaDeMes" VARCHAR(2), "DiaDeSemana" VARCHAR(1), "Mes" VARCHAR(2), "Hora" VARCHAR(2), "Minuto" VARCHAR(2), "SoloDiasHabiles" VARCHAR(1),
  "SoloPrincipioMes" VARCHAR(1), "SoloFinDeMes" VARCHAR(1), "habilitado" VARCHAR(1), "SiguienteFechaDeEjecucion" TIMESTAMP(0),
  "UltimaFechaDeEjecucion" TIMESTAMP(0), "EsProcesoManual" CHAR(1));
-- ValorBlob: the legacy reads it with getBytes and decodes UTF-8 (TransactionManager.java:490-495).
CREATE TABLE "BSCL.Plataforma.BD::BAT.ParametrosPrograma" ("IdSecuencia" BIGINT NOT NULL PRIMARY KEY, "IdGrupoParametro" BIGINT, "IdPrograma" BIGINT NOT NULL,
  "Orden" INTEGER, "NombreParametro" VARCHAR(500) NOT NULL, "Valor" VARCHAR(5000), "ValorBlob" BLOB);
-- The HANA view has EXACTLY these 10 columns (DBA, Result_3.csv): IdSecuencia, IdGrupoParametro, IdPrograma, Orden, NombreParametro, Valor, ValorBlob,
-- idEstado, IdTipoPrograma, fechaEjecucion. Its definition (join with Programa) is a stand-in; the repository selects only NombreParametro, Valor, ValorBlob.
CREATE VIEW "BSCL.Plataforma.BD.Vistas::BAT.ConsultarParametrosProgramas" AS
SELECT pp."IdSecuencia", pp."IdGrupoParametro", p."IdPrograma", pp."Orden", pp."NombreParametro", pp."Valor", pp."ValorBlob", p."idEstado",
       p."IdTipoPrograma", p."fechaEjecucion"
FROM "BSCL.Plataforma.BD::BAT.ParametrosPrograma" pp
LEFT JOIN "BSCL.Plataforma.BD::BAT.Programa" p ON p."IdPrograma" = pp."IdPrograma"
WHERE pp."IdPrograma" = p."IdPrograma";
-- Real DDL: no IdLog column (DBA, Result_2.csv).
CREATE TABLE "BSCL.Plataforma.BD::BAT.LogPrograma" ("FechaLog" TIMESTAMP(7), "IdTipoPrograma" VARCHAR(100), "IdPrograma" BIGINT, "Mensaje" VARCHAR(5000));
