SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarInstrumentoAperturaCierreUI";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarInstrumentoAperturaCierreUI"
AS
SELECT
 "IdFondo"
, "Nemotecnico"
, "IdEmisor"
, CASE "FondoExtranjero" WHEN 'S' THEN 'N' ELSE 'S' END AS "EsFondoNacional"
, CASE "FondoExtranjero" WHEN 'S' THEN 'UI_NO' ELSE 'UI_SI' END AS "EsFondoNacionalUI" 
, CASE WHEN "Perfil"."Key" = 'PERFIL_COMERCIAL'  THEN "EstadoAporteComercial" 
	   WHEN "Perfil"."Key" = 'PERFIL_BACKOFFICE' THEN "EstadoAporteBackoffice" END AS "EstadoAporte"
, CASE WHEN "Perfil"."Key" = 'PERFIL_COMERCIAL'  THEN CASE "EstadoAporteComercial"  WHEN 'C' THEN 'UI_CERRADO' ELSE 'UI_ABIERTO' END 
	   WHEN "Perfil"."Key" = 'PERFIL_BACKOFFICE' THEN CASE "EstadoAporteBackoffice" WHEN 'C' THEN 'UI_CERRADO' ELSE 'UI_ABIERTO' END
	   END AS "EstadoAporteUI"
, CASE WHEN "Perfil"."Key" = 'PERFIL_COMERCIAL'  THEN "EstadoRescateComercial" 
	   WHEN "Perfil"."Key" = 'PERFIL_BACKOFFICE' THEN "EstadoRescateBackoffice" END AS "EstadoRescate"
, CASE WHEN "Perfil"."Key" = 'PERFIL_COMERCIAL'  THEN CASE "EstadoRescateComercial"   WHEN 'C' THEN 'UI_CERRADO' ELSE 'UI_ABIERTO' END 
	   WHEN "Perfil"."Key" = 'PERFIL_BACKOFFICE' THEN CASE "EstadoRescateBackoffice"  WHEN 'C' THEN 'UI_CERRADO' ELSE 'UI_ABIERTO' END 
	   END AS "EstadoRescateUI"
, CASE WHEN "Perfil"."Key" = 'PERFIL_COMERCIAL'  THEN "EstadoReinversionComercial" 
	   WHEN "Perfil"."Key" = 'PERFIL_BACKOFFICE' THEN "EstadoReinversionBackoffice" END AS "EstadoReinversion"
, CASE WHEN "Perfil"."Key" = 'PERFIL_COMERCIAL'  THEN CASE "EstadoReinversionComercial"   WHEN 'C' THEN 'UI_CERRADO' ELSE 'UI_ABIERTO' END 
	   WHEN "Perfil"."Key" = 'PERFIL_BACKOFFICE' THEN CASE "EstadoReinversionBackoffice"  WHEN 'C' THEN 'UI_CERRADO' ELSE 'UI_ABIERTO' END 
	   END AS "EstadoReinversionUI"
, CASE WHEN "Perfil"."Key" = 'PERFIL_COMERCIAL'  THEN "EstadoLiquidezComercial" 
	   WHEN "Perfil"."Key" = 'PERFIL_BACKOFFICE' THEN "EstadoLiquidezBackoffice" END AS "EstadoLiquidez"
, CASE WHEN "Perfil"."Key" = 'PERFIL_COMERCIAL'  THEN CASE "EstadoLiquidezComercial"   WHEN 'C' THEN 'UI_CERRADO' ELSE 'UI_ABIERTO' END 
	   WHEN "Perfil"."Key" = 'PERFIL_BACKOFFICE' THEN CASE "EstadoLiquidezBackoffice"  WHEN 'C' THEN 'UI_CERRADO' ELSE 'UI_ABIERTO' END 
	   END AS "EstadoLiquidezUI"
, CASE WHEN "Perfil"."Key" = 'PERFIL_COMERCIAL'  THEN "UltimoFechaHoraCierreAporteComercial" 
	   WHEN "Perfil"."Key" = 'PERFIL_BACKOFFICE' THEN "UltimoFechaHoraCierreAporteBackOffice"
	   END AS "UltimoFechaHoraCierreAporte"
, CASE WHEN "Perfil"."Key" = 'PERFIL_COMERCIAL'  THEN "UltimoFechaHoraCierreRescateComercial" 
	   WHEN "Perfil"."Key" = 'PERFIL_BACKOFFICE' THEN "UltimoFechaHoraCierreRescateBackOffice"
	   END AS "UltimoFechaHoraCierreRescate"
, CASE WHEN "Perfil"."Key" = 'PERFIL_COMERCIAL'  THEN "UltimoFechaHoraCierreReinversionComercial" 
	   WHEN "Perfil"."Key" = 'PERFIL_BACKOFFICE' THEN "UltimoFechaHoraCierreReinversionBackOffice"
	   END AS "UltimoFechaHoraCierreReinversion"
, "BloqueoAporte"
, CASE "BloqueoAporte" WHEN 'S' THEN 'UI_SI' ELSE 'UI_NO' END AS "BloqueoAporteUI"
, "BloqueoRescate"
, CASE "BloqueoRescate" WHEN 'S' THEN 'UI_SI' ELSE 'UI_NO' END AS "BloqueoRescateUI"
, "Perfil"."Key" as "Perfil"
, "Fondo"."IdMoneda"
, "Fondo"."IdPaisOrigen" AS "IdPais"
, "Fondo"."IdEmpresa"
, "Fondo"."FechaFondo"
FROM "BSCL.FondoMutuo.BD::FMM.Fondo" AS "Fondo"
LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.ConfiguracionFondoMutuo" AS "Perfil" ON ("Fondo"."IdEmpresa" = "Perfil"."IdEmpresa")
WHERE "Perfil"."Key" IN ('PERFIL_COMERCIAL', 'PERFIL_BACKOFFICE');


GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarInstrumentoAperturaCierreUI" TO  BCS_DESARROLLADOR WITH GRANT OPTION;   
