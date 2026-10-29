SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarHorarioFondo";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarHorarioFondo"  
AS
SELECT
 "IdFondo"
,"Nemotecnico"
, CASE WHEN "Fondo"."UsaHorarioAporteDefault" 		= 'N' THEN 'N' ELSE 'S' END AS "UsaHorarioAporteDefault"
, CASE WHEN "Fondo"."UsaHorarioRescateDefault" 		= 'N' THEN 'N' ELSE 'S' END AS "UsaHorarioRescateDefault"
, CASE WHEN "Fondo"."UsaHorarioReinversionDefault" 	= 'N' THEN 'N' ELSE 'S' END AS "UsaHorarioReinversionDefault"
, CASE WHEN "Fondo"."UsaHorarioLiquidezDefault" 	= 'N' THEN 'N' ELSE 'S' END AS "UsaHorarioLiquidezDefault"
, CASE "Fondo"."UsaHorarioAporteDefault" 	 		WHEN 'N' THEN 'UI_SI' ELSE 'UI_NO' END AS "UsaHorarioAporteNoDefaultUI"
, CASE "Fondo"."UsaHorarioRescateDefault"	 		WHEN 'N' THEN 'UI_SI' ELSE 'UI_NO' END AS "UsaHorarioRescateNoDefaultUI"
, CASE "Fondo"."UsaHorarioReinversionDefault" 		WHEN 'N' THEN 'UI_SI' ELSE 'UI_NO' END AS "UsaHorarioReinversionNoDefaultUI"
, CASE "Fondo"."UsaHorarioLiquidezDefault" 	 		WHEN 'N' THEN 'UI_SI' ELSE 'UI_NO' END AS "UsaHorarioLiquidezNoDefaultUI"
, "Fondo"."HoraCierreAporteComercial"
, "Fondo"."HoraCierreRescateComercial"
, "Fondo"."HoraCierreReinversionComercial"
, "Fondo"."HoraCierreLiquidezComercial"
, "Fondo"."HoraCierreAporteBackoffice"
, "Fondo"."HoraCierreRescateBackoffice"
, "Fondo"."HoraCierreReinversionBackoffice"
, "Fondo"."HoraCierreLiquidezBackoffice"
FROM "BSCL.FondoMutuo.BD::FMM.Fondo" AS "Fondo";

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarHorarioFondo" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
