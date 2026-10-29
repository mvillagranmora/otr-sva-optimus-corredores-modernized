SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarConfiguracionRegimenFondoMutuo";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarConfiguracionRegimenFondoMutuo"
AS
SELECT
  CRFM."IdRegimenFondoMutuoOrigen"
, CRFM."IdRegimenFondoMutuoDestino"
, RFMO."Descripcion" as "DescripcionRegimenFondoMutuoOrigen"
, RFMD."Descripcion" as "DescripcionRegimenFondoMutuoDestino"
FROM "BSCL.FondoMutuo.BD::FMM.ConfiguracionRegimenFondoMutuo" CRFM
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" RFMO ON (RFMO."IdRegimenFondoMutuo" = CRFM."IdRegimenFondoMutuoOrigen")
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" RFMD ON (RFMD."IdRegimenFondoMutuo" = CRFM."IdRegimenFondoMutuoDestino");

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarConfiguracionRegimenFondoMutuo" TO  BCS_DESARROLLADOR WITH GRANT OPTION;