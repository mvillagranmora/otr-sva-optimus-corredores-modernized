SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarResumenCargaSolicitudesAporteRescate";
 
CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarResumenCargaSolicitudesAporteRescate" AS

SELECT
 "CARGA"."IdCargaSolicitudesAporteRescate" AS "IdCarga"
, "CARGA"."FechaCarga"
, "CARGA"."FechaMovimiento"
, "CARGA"."TipoMovimiento"
, "TipoMovimiento"."Descripcion" AS "NombreTipoMovimiento"
, "CARGA"."RutEmisor"
, "CARGA"."IdEmisor"
, "CARGA"."IdMoneda"
, "CARGA"."IdMoneda" AS "IdMonedaFondo"
, "CARGA"."IdEmpresa"
, COALESCE("SerieFondo"."DecimalesCuota",0) AS "DecimalesCuota"
, COUNT(CASE "CARGA"."Estado" WHEN 'PRO' THEN 1 ELSE NULL END) AS "RegistrosProcesado"
, COUNT(CASE "CARGA"."Estado" WHEN 'ERR' THEN 1 ELSE NULL END) AS "RegistrosError"
, COUNT(CASE "CARGA"."EstadoProceso" WHEN 'PEN' THEN 1 ELSE NULL END) AS "RegistrosProcesoPendiente"
, COUNT(CASE  WHEN "CARGA"."EstadoProceso" IN ('ERR', 'RZD') THEN 1 ELSE NULL END) AS "RegistrosProcesoError"
, COUNT(CASE "CARGA"."EstadoProceso" WHEN 'PRO' THEN 1 ELSE NULL END) AS "RegistrosProcesoProcesado"
, SUM(COALESCE("CARGA"."Monto",0)) AS "Monto"
, SUM(COALESCE("CARGA"."NumeroCuotas",0)) AS "NumeroCuotas"
FROM "BSCL.FondoMutuo.BD::FMM.DetalleCargaSolicitudesAporteRescate" AS "CARGA"
LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" AS "TipoMovimiento" ON ("CARGA"."TipoMovimiento" = "TipoMovimiento"."IdTipoMovimientoFondoMutuo")
LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieFondo" ON ("CARGA"."IdSerieFondo" = "SerieFondo"."IdSerieFondo")
WHERE "CARGA"."Eliminado" = 'N'
GROUP BY "CARGA"."IdCargaSolicitudesAporteRescate", "CARGA"."FechaCarga", "CARGA"."FechaMovimiento", "CARGA"."TipoMovimiento", "TipoMovimiento"."Descripcion", "CARGA"."RutEmisor", "CARGA"."IdEmisor", "CARGA"."IdMoneda", "SerieFondo"."DecimalesCuota", "CARGA"."IdEmpresa";

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarResumenCargaSolicitudesAporteRescate" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
