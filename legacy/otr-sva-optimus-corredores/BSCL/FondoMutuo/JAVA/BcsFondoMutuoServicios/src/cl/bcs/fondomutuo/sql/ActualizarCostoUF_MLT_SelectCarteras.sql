SELECT 
CAR."IdCartera"
,CAR."IdPortafolio"
,CAR."FolioCartera"
,CAR."IdOperacion"
,CAR."EGRIdOperacion"
,CAR."Cantidad"
,CAR."EGRCantidad"
,CAR."CostoInversionEnFactorAjuste"
,CAR."ValorFactorAjusteaFechaCurse"
,CAR."INGCostoInversionEnFactorAjuste"
,CAR."INGValorFactorAjusteaFechaCurse"
,CAR."EGRValorFactorAjusteaFechaCurse"
,CAR."EGRCostoInversionEnFactorAjuste"
,CAR."MontoCompra"
,CAR."INGMontoATasaMercadoActual"
,SOL."TipoSolicitud"
,SOL."IdTipoMovimientoFondoMutuo"
FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
INNER JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" SOL ON SOL."IdSolicitudAporteRescate" = CAR."IdOperacion" OR SOL."IdSolicitudAporteRescate" = CAR."EGRIdOperacion"
WHERE SOL."FechaCertificacion" BETWEEN :FechaDesde AND :FechaHasta
AND CAR."IdClasificacionTipoInstrumento" = 'FM'
AND (SOL."TipoSolicitud" = 'RES' OR SOL."IdTipoMovimientoFondoMutuo" = 'APOREINV')
AND SOL."IdPortafolio" IN (:Portafolio)
--AND SOL."FolioContrato" = 786429
ORDER BY "IdCartera" ASC;