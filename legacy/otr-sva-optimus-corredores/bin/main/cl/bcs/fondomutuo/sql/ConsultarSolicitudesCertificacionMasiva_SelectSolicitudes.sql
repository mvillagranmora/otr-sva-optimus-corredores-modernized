SELECT
"Solicitud"."TipoSolicitud"
, "Concepto"."NombreConcepto" AS "NombreTipoSolicitud"
, "Solicitud"."IdTipoMovimientoFondoMutuo"
, "TipoMovimientoFondoMutuo"."Descripcion" AS "DescripcionTipoMovimiento"
, "SerieFondo"."IdSerieFondo"
, "Solicitud"."FechaCertificacion"
, "Solicitud"."FechaValorCuota" AS "FechaCierre"
, "Fondo"."IdMoneda" AS "IdMonedaFondo"
, "Solicitud"."IdMoneda"
, COUNT(1) AS "CantidadRegistros"
, SUM("Solicitud"."NumeroCuotas") AS "NumeroCuotas"
, SUM("Solicitud"."Monto") AS "Monto"
, "SerieFondo"."IdEmpresa"
, "SerieFondo"."DecimalesValorCuota"
, "Fondo"."IdEmisor"
, CASE "Fondo"."FondoExtranjero" WHEN 'S' THEN 'N' ELSE 'S' END AS "EsFondoNacional"
, CASE "Fondo"."FondoExtranjero" WHEN 'S' THEN 'UI_NO' ELSE 'UI_SI' END AS "EsFondoNacionalUI"
, "Fondo"."MoneyMarket"
, CASE "Fondo"."MoneyMarket" WHEN 'S' THEN 'UI_SI' ELSE 'UI_NO' END AS "EsMoneyMarketUI"
, "Solicitud"."CalculoPorMontoCuota"
FROM "BSCL.FondoMutuo.BD::FMM.Fondo" AS "Fondo"
INNER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieFondo" ON ("Fondo"."IdFondo" = "SerieFondo"."IdFondo" AND "Fondo"."IdEmpresa" = "SerieFondo"."IdEmpresa")
INNER JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS "Solicitud" ON ("Solicitud"."IdSerieFondo" = "SerieFondo"."IdSerieFondo" AND "Solicitud"."IdEmpresa" = "SerieFondo"."IdEmpresa")
INNER JOIN "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" AS "TipoMovimientoFondoMutuo" ON ("TipoMovimientoFondoMutuo"."IdTipoMovimientoFondoMutuo" = "Solicitud"."IdTipoMovimientoFondoMutuo")
INNER JOIN "BSCL.FondoMutuo.BD::FMM.Concepto" AS "Concepto" ON ("GrupoConcepto" = 'TIPOOPERACION' AND "Solicitud"."TipoSolicitud" = "Concepto"."CodigoConcepto")
WHERE "Solicitud"."Estado" = 'ING'
AND "Solicitud"."FechaCertificacion" = :FechaCertificacion
AND (:FiltraPorIdEmisor = 0 OR "Fondo"."IdEmisor" = :IdEmisor)
AND (:FiltraPorIdMonedaFondo = 0 OR "Fondo"."IdMoneda" = :IdMonedaFondo)
AND (:FiltraPorIdSerieFondo = 0 OR "SerieFondo"."IdSerieFondo" = :IdSerieFondo)
AND (:FiltraPorEsNacional = 0 OR :EsNacional = CASE "Fondo"."FondoExtranjero" WHEN 'S' THEN 'N' ELSE 'S' END)
AND (:FiltraPorIdTipoMovimientoFondoMutuo = 0 OR "Solicitud"."IdTipoMovimientoFondoMutuo" = :IdTipoMovimientoFondoMutuo)
AND (:FiltraPorMoneyMarket = 0 OR "Fondo"."MoneyMarket" = :MoneyMarket)
GROUP BY "Solicitud"."TipoSolicitud", "Concepto"."NombreConcepto", "Solicitud"."IdTipoMovimientoFondoMutuo", "TipoMovimientoFondoMutuo"."Descripcion", "SerieFondo"."IdSerieFondo", 
"Solicitud"."FechaCertificacion", "Solicitud"."FechaValorCuota", "Fondo"."IdMoneda", "Solicitud"."IdMoneda",
"SerieFondo"."IdEmpresa", "SerieFondo"."DecimalesValorCuota", "Fondo"."IdEmisor", "Fondo"."FondoExtranjero", "Fondo"."MoneyMarket", "Solicitud"."CalculoPorMontoCuota";
