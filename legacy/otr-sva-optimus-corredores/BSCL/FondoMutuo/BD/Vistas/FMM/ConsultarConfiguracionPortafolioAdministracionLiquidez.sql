SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarConfiguracionPortafolioAdministracionLiquidez";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarConfiguracionPortafolioAdministracionLiquidez"
AS
SELECT
  "IdConfiguracionPortafolioAdministracionLiquidez"							AS "IdConfiguracionPortafolioAdministracionLiquidez"
, COALESCE("ConfiguracionFondoAdministracionLiquidez"."IdCliente","PORT"."IdCliente")	    AS "IdCliente"
, COALESCE("ConfiguracionFondoAdministracionLiquidez"."IdPortafolio","PORT"."IdPortafolio") AS "IdPortafolio"
, "ConfiguracionFondoAdministracionLiquidez"."FechaIngreso"					AS "FechaIngreso"
, "ConfiguracionFondoAdministracionLiquidez"."FechaIngresoRegistro"			AS "FechaIngresoRegistro"
, "ConfiguracionFondoAdministracionLiquidez"."FechaModificacion"			AS "FechaModificacion"
, "ConfiguracionFondoAdministracionLiquidez"."FechaModificacionRegistro"	AS "FechaModificacionRegistro"
, COALESCE("ConfiguracionFondoAdministracionLiquidez"."IdMoneda","PORT"."IdMoneda")		AS "IdMoneda"
, COALESCE("ConfiguracionFondoAdministracionLiquidez"."IdSerieFondo","PORT"."IdSerieFondo")	AS "IdSerieFondo"
, COALESCE("ConfiguracionFondoAdministracionLiquidez"."MontoMinimo","PORT"."MontoMinimo")		AS "MontoMinimo"
, "PORT"."CodigoPortafolio"									                AS "CodigoPortafolio"
, "PORT"."DescripcionPortafolio"									AS "DescripcionPortafolio"
, "PORT"."Proposito" 										    	AS "PropositoPortafolio"
, "PORT"."RazonSocial"												AS "RazonSocial"
, "PORT"."NombreCompleto"											AS "NombreCompleto"
, "PORT"."DocumentoIdentidad"										AS "DocumentoIdentidad"
, "PORT"."IdPersona"												AS "IdPersona"
, "PORT"."IdEmpresa"	
, "PORT"."IdSocioDeNegocio"                                         AS "IdSocioDeNegocio"  
, CASE COALESCE("ConfiguracionFondoAdministracionLiquidez"."IdCliente",0) WHEN 0 THEN 'UI_NO' ELSE 'UI_SI' END AS "EsConfiguracionPersonalizada"
FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarConfiguracionDefaultPortafolioAdministracionLiquidez" AS "PORT"
LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.ConfiguracionPortafolioAdministracionLiquidez" AS "ConfiguracionFondoAdministracionLiquidez" ON ( "ConfiguracionFondoAdministracionLiquidez"."IdPortafolio" = "PORT"."IdPortafolio" AND "ConfiguracionFondoAdministracionLiquidez"."IdEstado" = 'ACT' AND "ConfiguracionFondoAdministracionLiquidez"."IdMoneda" = "PORT"."IdMoneda");

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarConfiguracionPortafolioAdministracionLiquidez" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
