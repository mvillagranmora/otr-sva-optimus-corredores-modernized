SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarLogAdministracionLiquidez";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarLogAdministracionLiquidez"
AS
SELECT
"LogAdministracionLiquidez"."IdLogAdministracionLiquidez"					AS "IdLogAdministracionLiquidez"
,"LogAdministracionLiquidez"."IdCliente"									AS "IdCliente"
,"LogAdministracionLiquidez"."IdPortafolio"									AS "IdPortafolio"
,"LogAdministracionLiquidez"."IdSerieFondo"									AS "IdSerieFondo"
,"LogAdministracionLiquidez"."IdMoneda"										AS "IdMoneda"
,"LogAdministracionLiquidez"."Saldo"										AS "Saldo"
,"LogAdministracionLiquidez"."MontoMinimo"									AS "MontoMinimo"
,"LogAdministracionLiquidez"."IdSolicitudAporteRescate"						AS "IdSolicitudAporteRescate"
,"LogAdministracionLiquidez"."FechaIngreso"									AS "FechaIngreso"
,"LogAdministracionLiquidez"."FechaIngresoRegistro"							AS "FechaIngresoRegistro"
,"LogAdministracionLiquidez"."Codigo"										AS "Codigo"
, "ConsultarPortafolio"."CodigoPortafolio"									AS "CodigoPortafolio"
, "ConsultarPortafolio"."DescripcionPortafolio"								AS "DescripcionPortafolio"
, "ConsultarPortafolio"."Proposito" 										AS "PropositoPortafolio"
, "ConsultarCliente"."RazonSocial"											AS "RazonSocial"
, "ConsultarCliente"."NombreCompleto"										AS "NombreCompleto"
, "ConsultarCliente"."DocumentoIdentidad"									AS "DocumentoIdentidad"
, "ConsultarCliente"."IdPersona"											AS "IdPersona"
, "LogAdministracionLiquidez"."IdEmpresa"									AS "IdEmpresa"
FROM "BSCL.FondoMutuo.BD::FMM.LogAdministracionLiquidez" AS "LogAdministracionLiquidez"
LEFT OUTER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS "ConsultarPortafolio" ON  ( "ConsultarPortafolio"."IdPortafolio" = "LogAdministracionLiquidez"."IdPortafolio" )
LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "ConsultarCliente" ON  ( "ConsultarCliente"."IdCliente" = "ConsultarPortafolio"."IdCliente" );
GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarLogAdministracionLiquidez" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 