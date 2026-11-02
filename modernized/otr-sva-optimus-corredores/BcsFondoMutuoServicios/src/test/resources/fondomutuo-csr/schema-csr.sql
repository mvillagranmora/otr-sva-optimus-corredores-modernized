-- F3 W3 T4-A (CrearSolicitudAporteReinversion): H2 stand-in of the ONE HANA object the AR SQL touch that schema-fmm.sql (F1) does not define: the table of the destination series of a
-- reinversion, read by CrearSolicitudAporteReinversion_SelectReinversionSerieFondoDestino.sql and updated by ..._UpdateFechaFinalReinversion.sql (AR:415-445).
-- H2 ONLY, never executed against HANA. Loaded by the FMM-CSR cases through "esquemasExtra" (after the F1 schema and base seed, before seed/base-csr.sql and the case seed).
-- REAL (hana_qa_results/f2_columnas_reinversion.csv, f2b_default_idcarga.csv): same definition as schema-rre.sql (the FMM-RRE oracle); this file is a copy so that FMM-CSR does not
-- depend on the RRE fixtures.
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

-- view FMM.ConsultarIndicadoresPortafolioFondoMutuo (ComunesSolicitudes_SelectIndicadoresPortafolioFFMM.sql, AR:544): ASSUMED columns, the two the SELECT reads (same stand-in as schema-cer.sql,
-- FM-CER-15; the object is not in the HANA catalog export CATALOGO_HANA_W3.txt: Q-T4A-4). A table in H2, as the other views of schema-fmm.sql.
CREATE TABLE "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarIndicadoresPortafolioFondoMutuo" ("IdPortafolio" BIGINT, "OperaLineaSobregiro" NVARCHAR(1));
