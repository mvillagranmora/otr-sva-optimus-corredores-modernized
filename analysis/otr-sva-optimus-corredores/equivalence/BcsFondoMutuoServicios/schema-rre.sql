-- F2 W1 front 2 (CrearSolicitudRescateReinversion): H2 stand-in of the HANA objects the RR SQL touch that schema-fmm.sql (F1) does not define.
-- H2 ONLY, never executed against HANA. Loaded by the FMM-RRE cases through "esquemasExtra" (after the F1 schema and base seed, before the case seed).
-- REAL = types from the HANA QA catalog (hana_qa_results/f2_columnas_reinversion.csv, f2b_columnas_bloqueo_reinversion.csv, f2b_default_idcarga.csv,
-- f2b_secuencias.csv; PLAN_FONDOMUTUO_F2 13/15). ASSUMED = no catalog row: only the columns the RR SQL name, plain types.

-- Session local table of CrearReinversionRescate_Tabla_Bloqueo.sql (types taken from that DDL). As in schema-session.sql: the table always exists in H2 and
-- the oracle session tracks CREATE LOCAL TEMPORARY TABLE / DROP TABLE per session (tablasLocalesSinDrop).
CREATE TABLE "#p_BloqueoProcesarReinversion" (
  "IdBloqueaReinversion" NVARCHAR(100),
  "IdPortafolio" BIGINT,
  "IdSerieFondo" NVARCHAR(40),
  "IdRegimenTributario" NVARCHAR(40),
  "IdCliente" BIGINT,
  "NumeroCuotas" DECIMAL(26,8),
  "Monto" DECIMAL(26,8),
  "FechaSistema" DATE,
  "IdGrupoRegimen" NVARCHAR(40)
);

-- REAL (f2b_columnas_bloqueo_reinversion.csv): persistent COLUMN table, not temporary (f2b_tipo_bloqueo_reinversion.csv); written only by the two AUTONOMOUS
-- procedures (oracle: runner/cl/bcs/oracle/ReinversionLock.java). No key in the catalog export.
CREATE TABLE "BSCL.FondoMutuo.BD::FMM.ProcesarReinversionRescate_BloqueoReinversion" (
  "IdBloqueaReinversion" NVARCHAR(70) NOT NULL,
  "IdPortafolio" BIGINT NOT NULL,
  "IdRegimenTributario" NVARCHAR(40) NOT NULL,
  "IdSerieFondo" NVARCHAR(40) NOT NULL,
  "Monto" DECIMAL(26,8) DEFAULT 0 NOT NULL,
  "NumeroCuotas" DECIMAL(26,8) DEFAULT 0 NOT NULL,
  "RescateTotal" NVARCHAR(1) DEFAULT 'N',
  "FechaSistema" DATE DEFAULT DATE '0001-01-01' NOT NULL,
  "IdCliente" BIGINT NOT NULL,
  "IdGrupoRegimen" NVARCHAR(40) NOT NULL
);

-- REAL (f2_columnas_reinversion.csv, f2b_default_idcarga.csv): the INSERT of ComunesSolicitudes_InsertReinversionSolicitud.sql leaves IdCarga to its default 0.
CREATE TABLE "BSCL.FondoMutuo.BD::FMM.ReinversionSerieFondoDestino" (
  "IdReinversionSerieFondo" BIGINT NOT NULL,
  "IdSolicitudAporteRescate" BIGINT NOT NULL,
  "IdSerieFondoDestino" NVARCHAR(40) NOT NULL,
  "Porcentaje" DECIMAL(26,8) NOT NULL,
  "IdRegimenTributario" NVARCHAR(40) NOT NULL,
  "IdCarga" BIGINT DEFAULT 0 NOT NULL,
  "Switch" NVARCHAR(1) NOT NULL,
  "PorcentajeReal" DECIMAL(26,8) NOT NULL,
  "FechaFinalReinversion" DATE NOT NULL
);

-- ASSUMED (no column catalog): table read by ComunesSolicitudes_SelectConfiguracionRegimenFondoMutuo.sql.
CREATE TABLE "BSCL.FondoMutuo.BD::FMM.ConfiguracionRegimenFondoMutuo" (
  "IdRegimenFondoMutuoOrigen" NVARCHAR(40),
  "IdRegimenFondoMutuoDestino" NVARCHAR(40)
);

-- ASSUMED (no column catalog; it is a view in HANA, a table here as the other views of schema-fmm.sql): read by
-- ComunesSolicitudes_SelectRegimenSerieDeFondoMandato.sql.
CREATE TABLE "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarRegimenSerieDeFondo" (
  "IdEmpresa" BIGINT,
  "IdSerieFondo" NVARCHAR(40),
  "RescateConMandato" NVARCHAR(1),
  "IdEstadoRegimenSerieDeFondo" NVARCHAR(3)
);

-- REAL (f2b_secuencias.csv)
CREATE SEQUENCE "BSCL.FondoMutuo.BD.Secuencias::IdAgrupador" START WITH 99857636 MINVALUE 1 MAXVALUE 4611686018427387903 INCREMENT BY 1 NO CYCLE;
CREATE SEQUENCE "BSCL.FondoMutuo.BD.Secuencias::IdReinversionSerieFondo" START WITH 1 MINVALUE 1 MAXVALUE 4611686018427387903 INCREMENT BY 1 NO CYCLE;
