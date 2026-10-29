SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSerieDeFondo";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSerieDeFondo"
AS
SELECT
"SerieDeFondo"."IdSerieFondo"
, "SerieDeFondo"."IdInstrumento"
, "SerieDeFondo"."IdFondo"
, "SerieDeFondo"."NombreSerieFondo"
, "SerieDeFondo"."CodigoSerie"
, "SerieDeFondo"."Alias"
, "SerieDeFondo"."MontoMinimo"
, "SerieDeFondo"."MontoMaximo"
, "SerieDeFondo"."DiasRetencionesRescate"
, "SerieDeFondo"."ExcluyeGarantia"
, "SerieDeFondo"."IdSerieFondoOrigen"
, "SerieDeFondo"."Omnibus"
, "SerieDeFondo"."CobraComision"
, "SerieDeFondo"."Diferido"
, "SerieDeFondo"."IdCliente"
, "SerieDeFondo"."IdPortafolio"
, "Fondo"."IdEmisor"
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
, "Instrumento"."Nemotecnico" as "Instrumento"
, "FamiliaFondo"."NombreFamilia"
, "FamiliaFondo"."IdFamiliaFondo"
FROM "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieDeFondo"
LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS "Fondo" ON ("Fondo"."IdFondo" = "SerieDeFondo"."IdFondo")
LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.TipoFondo" AS "TipoFondo" ON ("Fondo"."IdTipoFondo" = "TipoFondo"."IdTipoFondo")
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS "Instrumento" ON ("SerieDeFondo"."IdInstrumento" = "Instrumento"."IdInstrumento")
LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.FamiliaFondo" AS "FamiliaFondo" ON ("SerieDeFondo"."IdFamiliaFondo" = "FamiliaFondo"."IdFamiliaFondo");

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSerieDeFondo" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
