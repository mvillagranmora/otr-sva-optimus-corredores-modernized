SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarConfiguracionAperturaCierre";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarConfiguracionAperturaCierre"
AS 
SELECT
 "ConfiguracionAperturaCierre"."IdConfiguracion"
, "ConfiguracionAperturaCierre"."AlcanceConfiguracion"
, "ConfiguracionAperturaCierre"."HoraCierreRescate"
, "ConfiguracionAperturaCierre"."HoraCierreAporte"
, "ConfiguracionAperturaCierre"."UltimoFechaHoraCierreRescate"
, "ConfiguracionAperturaCierre"."UltimoFechaHoraCierreAporte"
, "ConfiguracionAperturaCierre"."IdFondo"
, "ConfiguracionAperturaCierre"."IdEmisor"
, "Fondo"."Nemotecnico"
, "Fondo"."NombreFondo"
, "Fondo"."IdTipoFondo"
, "Fondo"."IdPaisOrigen"
, "Fondo"."CodigoSVS"
, "Fondo"."MoneyMarket"
, "Fondo"."IdCategoriaPerfilFondoMutuo"
, "Fondo"."FechaCreacion"
, "Fondo"."FechaConstitucion"
, "Fondo"."FechaCierre"
, "Fondo"."Vigencia"
, "TipoFondo"."NombreCorto"
, "TipoFondo"."Descripcion"
FROM "BSCL.FondoMutuo.BD::FMM.ConfiguracionAperturaCierre" AS "ConfiguracionAperturaCierre"
LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS "Fondo" ON ("ConfiguracionAperturaCierre"."IdFondo" = "Fondo"."IdFondo")
LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.TipoFondo" AS "TipoFondo" ON ("TipoFondo"."IdTipoFondo" = "Fondo"."IdTipoFondo");

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarConfiguracionAperturaCierre" TO  BCS_DESARROLLADOR WITH GRANT OPTION;