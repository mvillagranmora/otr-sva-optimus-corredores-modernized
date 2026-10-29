-- H2 test schema aligned with real HANA QA column names (2026-10-08)
-- Real HANA: BSCL."BSCL.Plataforma.BD::PLA.Configuracion" — same columns, different table name
CREATE TABLE IF NOT EXISTS CONFIGURACION_PLA (
  "IdEmpresa"        BIGINT        NOT NULL,
  "CodigoParametro"  VARCHAR(40)   NOT NULL,
  "Valor"            VARCHAR(5000),
  PRIMARY KEY ("IdEmpresa", "CodigoParametro")
);

-- H2 test schema for custody positions
-- Real HANA: BSCL."BSCL.Custodia.BD::CUS.CustodiaPortafolio" — user-defined types, not JDBC-queryable
-- This table simulates the flattened view with key columns only
CREATE TABLE IF NOT EXISTS CUSTODIA_PORTAFOLIO (
  "IdPortafolio"     BIGINT        NOT NULL,
  "IdInstrumento"    BIGINT        NOT NULL,
  "Nemotecnico"      VARCHAR(40),
  "CantidadCustodia" DECIMAL(26,6),
  PRIMARY KEY ("IdPortafolio", "IdInstrumento")
);
