SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarConfiguracionPropositoPortafolio";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarConfiguracionPropositoPortafolio"
AS
SELECT
  CPP."IdRegimenFondoMutuo"
, CPP."IdPropositoPortafolio"
, CPPO."Descripcion" AS "DescripcionPropositoSerieRelacionadoOrigen"
, CPPD."Descripcion" AS "DescripcionPropositoSerieRelacionadoDestino"
FROM "BSCL.FondoMutuo.BD::FMM.ConfiguracionPropositoPortafolio" CPP
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" CPPO ON (CPPO."IdRegimenFondoMutuo" = CPP."IdRegimenFondoMutuo")
LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPropositoPortafolio" CPPD ON (CPPD."IdPropositoPortafolio" = CPP."IdPropositoPortafolio");

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarConfiguracionPropositoPortafolio" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
