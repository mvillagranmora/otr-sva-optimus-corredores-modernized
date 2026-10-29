SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarRegistroHistoricoDeSeriesDeFondos";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarRegistroHistoricoDeSeriesDeFondos"
AS
SELECT
 "RegistroHistoricoSeriesFondo"."IdRegistroHistorico"
, "RegistroHistoricoSeriesFondo"."FechaInicioCambio"
, "RegistroHistoricoSeriesFondo"."IdFondo"
, "RegistroHistoricoSeriesFondo"."IdSerieFondo"
, "RegistroHistoricoSeriesFondo"."NombreSerieFondo"
, "RegistroHistoricoSeriesFondo"."TipoFondoSVS"
, "RegistroHistoricoSeriesFondo"."IdPais"
, "RegistroHistoricoSeriesFondo"."CodigoSVS"
, "RegistroHistoricoSeriesFondo"."IdInstrumento"
, "Instrumento"."Nemotecnico" as "Instrumento"
, "Fondo"."Nemotecnico"
, "Fondo"."NombreFondo"
, "Fondo"."IdTipoFondo"
, "Fondo"."IdPaisOrigen"
, "Fondo"."MoneyMarket"
, "Fondo"."IdCategoriaPerfilFondoMutuo"
, "Fondo"."FechaCreacion"
, "Fondo"."FechaConstitucion"
, "Fondo"."FechaCierre"
, "Fondo"."Vigencia"
, "TipoFondo"."NombreCorto"
, "TipoFondo"."Descripcion"
FROM "BSCL.FondoMutuo.BD::FMM.RegistroHistoricoDeSeriesDeFondos" AS "RegistroHistoricoSeriesFondo"
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS "Instrumento" ON ("Instrumento"."IdInstrumento" = "RegistroHistoricoSeriesFondo"."IdInstrumento")
LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS "Fondo" ON ("Fondo"."IdFondo" = "RegistroHistoricoSeriesFondo"."IdFondo")
LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.TipoFondo" AS "TipoFondo" ON ("Fondo"."IdTipoFondo" = "TipoFondo"."IdTipoFondo");

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarRegistroHistoricoDeSeriesDeFondos" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
