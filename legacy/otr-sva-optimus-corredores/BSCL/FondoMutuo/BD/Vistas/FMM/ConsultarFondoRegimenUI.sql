SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondoRegimenUI";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondoRegimenUI"
AS

SELECT
 "Fondo"."IdFondo"
, "Fondo"."IdSerieFondo"
, "Fondo"."NombreFondo"
, "Fondo"."NombreSerieFondo"
, "Fondo"."NombreFamilia"
, "Fondo"."ValorCuota"
, "Fondo"."FechaValorCuota"
, "Fondo"."IdMoneda"
, "Fondo"."RazonSocialEmisor"
, "Fondo"."IdEmisor"
, "Fondo"."IdPersona"
, "Fondo"."NombreTipoInstrumento"
, "Fondo"."IdEmpresa"
, "Fondo"."NombreEstado"
, "Fondo"."DecimalesValorCuota"
, "Fondo"."ValorDolarAporte"
, "Fondo"."ValorDolarRescate"
, "Fondo"."NombreCorto"
, "Fondo"."EsDiferidoUI"
, "Fondo"."MontoMinimo"
, "Fondo"."MontoMaximo"
, "Fondo"."EsMoneyMarketUI"
, "Fondo"."IdTipoInstrumento"
, "Fondo"."IdEstado"
, "Fondo"."IdFamiliaFondo"
, "Fondo"."FondoExtranjero"
, "Regimen"."IdRegimenFondoMutuo"
FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondo" AS "Fondo"
INNER JOIN "BSCL.FondoMutuo.BD::FMM.RegimenSerieDeFondo" AS "Regimen" ON "Regimen"."IdSerieFondo" = "Fondo"."IdSerieFondo";

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondoRegimenUI" TO  BCS_DESARROLLADOR WITH GRANT OPTION;   
