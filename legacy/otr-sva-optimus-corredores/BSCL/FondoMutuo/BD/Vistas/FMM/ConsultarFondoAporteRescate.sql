SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondoAporteRescate";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondoAporteRescate"  
AS
SELECT
"Solicitud"."TipoSolicitud"
, "Concepto"."NombreConcepto" AS "NombreTipoSolicitud"
, "Solicitud"."IdTipoMovimientoFondoMutuo"
, "TipoMovimientoFondoMutuo"."Descripcion" AS "DescripcionTipoMovimiento"
, "SerieFondo"."IdSerieFondo"
, "Solicitud"."FechaCertificacion"
, COALESCE("ValorCuota"."ValorCuota",0) AS "ValorCuota"
, "ValorCuota"."FechaCierre"
, "Fondo"."IdMoneda" AS "IdMonedaFondo"
, COUNT(1) AS "CantidadRegistros"
, SUM(
  CASE
    WHEN "TipoMovimientoFondoMutuo"."IndicadorAporteRescate" = 'RES' And "Solicitud"."CalculoPorMontoCuota" = 'C' And "Solicitud"."Estado" != 'CRT' THEN ((CASE WHEN "Solicitud"."Estado" != 'CRT' THEN "ValorCuota"."ValorCuota" ELSE "Solicitud"."ValorCuota" END) * "Solicitud"."NumeroCuotas")
  ELSE "Solicitud"."Monto"
  END
 ) AS "Monto"
, "SerieFondo"."IdEmpresa"
, "SerieFondo"."DecimalesValorCuota"
, "Fondo"."IdEmisor"
, CASE "Fondo"."FondoExtranjero" WHEN 'S' THEN 'N' ELSE 'S' END AS "EsFondoNacional"
, CASE "Fondo"."FondoExtranjero" WHEN 'S' THEN 'UI_NO' ELSE 'UI_SI' END AS "EsFondoNacionalUI"
, "Fondo"."MoneyMarket"
, CASE "Fondo"."MoneyMarket" WHEN 'S' THEN 'UI_SI' ELSE 'UI_NO' END AS "EsMoneyMarketUI"
FROM "BSCL.FondoMutuo.BD::FMM.Fondo" AS "Fondo"
INNER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieFondo" ON ("Fondo"."IdFondo" = "SerieFondo"."IdFondo" AND "Fondo"."IdEmpresa" = "SerieFondo"."IdEmpresa")
INNER JOIN "BSCL.FondoMutuo.BD::FMM.ValorCuota" AS "ValorCuota" ON ("ValorCuota"."IdSerieFondo" = "SerieFondo"."IdSerieFondo" AND "ValorCuota"."IdEmpresa" = "SerieFondo"."IdEmpresa")
INNER JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS "Solicitud" ON ("Solicitud"."IdSerieFondo" = "SerieFondo"."IdSerieFondo" AND "Solicitud"."IdEmpresa" = "SerieFondo"."IdEmpresa")
INNER JOIN "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" AS "TipoMovimientoFondoMutuo" ON ("TipoMovimientoFondoMutuo"."IdTipoMovimientoFondoMutuo" = "Solicitud"."IdTipoMovimientoFondoMutuo")
INNER JOIN "BSCL.FondoMutuo.BD::FMM.Concepto" AS "Concepto" ON ("GrupoConcepto" = 'TIPOOPERACION' AND "Solicitud"."TipoSolicitud" = "Concepto"."CodigoConcepto")
WHERE "Solicitud"."Estado" = 'ING'
GROUP BY "Solicitud"."TipoSolicitud", "Concepto"."NombreConcepto", "Solicitud"."IdTipoMovimientoFondoMutuo", "TipoMovimientoFondoMutuo"."Descripcion", "SerieFondo"."IdSerieFondo", 
"Solicitud"."FechaCertificacion", "ValorCuota"."ValorCuota", "ValorCuota"."FechaCierre", "Fondo"."IdMoneda", 
"SerieFondo"."IdEmpresa", "SerieFondo"."DecimalesValorCuota", "Fondo"."IdEmisor", "Fondo"."FondoExtranjero", "Fondo"."MoneyMarket";

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondoAporteRescate" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
